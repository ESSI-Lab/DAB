package eu.essi_lab.pdk.rsm.impl.json.jsapi;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

import javax.ws.rs.core.MediaType;
import javax.xml.datatype.XMLGregorianCalendar;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.Identification;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.ServiceIdentification;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;

/**
 * Result set mapper implementation which maps the {@link GSResource}s according to a JSON schema encoding defined for the <a
 * href="http://api.eurogeoss-broker.eu/docs/index.html">JavaScript API</a>. The {@link #JS_API_MAPPING_SCHEMA} has the following
 * properties:
 * <ul>
 * <li>schema uri: {@link GSNameSpaceContext#GS_DATA_MODEL_SCHEMA_URI}</li>
 * <li>schema name: {@link GSNameSpaceContext#GS_DATA_MODEL_SCHEMA_NAME}</li>
 * <li>schema version: {@link NameSpace#GS_DATA_MODEL_SCHEMA_VERSION}</li>
 * <li>encoding name: {@value #JS_API_DATA_MODEL_ENCODING_NAME}</li>
 * <li>encoding version: {@value #JS_API_DATA_MODEL_ENCODING_NAME_VERSION}</li>
 * <li>encoding media type: {@link MediaType#APPLICATION_JSON}</li>
 * </ul>
 *
 * @author Fabrizio
 */
public class JS_API_ResultSetMapper extends DiscoveryResultSetMapper<String> {

    /**
     * The encoding name of {@link #JS_API_MAPPING_SCHEMA}
     */
    public static final String JS_API_DATA_MODEL_ENCODING_NAME = "js-api-dm-enc";

    /**
     * The encoding version of {@link #JS_API_MAPPING_SCHEMA}
     */
    public static final String JS_API_DATA_MODEL_ENCODING_NAME_VERSION = "1.0";

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema JS_API_MAPPING_SCHEMA = new MappingSchema();

    static {

	JS_API_MAPPING_SCHEMA.setUri(NameSpace.GS_DATA_MODEL_SCHEMA_URI);
	JS_API_MAPPING_SCHEMA.setName(NameSpace.GS_DATA_MODEL_SCHEMA_NAME);
	JS_API_MAPPING_SCHEMA.setVersion(NameSpace.GS_DATA_MODEL_SCHEMA_VERSION);

	JS_API_MAPPING_SCHEMA.setEncoding(JS_API_DATA_MODEL_ENCODING_NAME);
	JS_API_MAPPING_SCHEMA.setEncodingVersion(JS_API_DATA_MODEL_ENCODING_NAME_VERSION);
	JS_API_MAPPING_SCHEMA.setEncodingMediaType(MediaType.APPLICATION_JSON_TYPE);
    }

    private static final ArrayList<String> GEOSS_CATEGORIES = new ArrayList<String>();

    static {
	GEOSS_CATEGORIES.add("dataSetDataBase");
	GEOSS_CATEGORIES.add("observingSystemSensorNetwork");
	GEOSS_CATEGORIES.add("computationModel");
	GEOSS_CATEGORIES.add("initiativeProgramme");
	GEOSS_CATEGORIES.add("documentFileGraphic");
	GEOSS_CATEGORIES.add("modelingDataProcessingCenter");
	GEOSS_CATEGORIES.add("feedRSSAlert");
	GEOSS_CATEGORIES.add("catalogRegistryMetadataCollection");
	GEOSS_CATEGORIES.add("softwareApplication");
    }

    private static final String SOS_TAHMO_PROXY_PATH = "sos-tahmo-proxy";

    private static final String SOS_TAHMO_URL = "http://hnapi.hydronet.com/api/service/sos";

    private static final String SOS_TWIGA_URL = "http://hn4s.hydronet.com/api/service/TWIGA/sos";

    @Override
    public String map(DiscoveryMessage message, GSResource resource) {

	GSSource gsSource = resource.getSource();

	if (message.isOutputSources()) {

	    BrokeringStrategy strategy = gsSource.getBrokeringStrategy();
	    String endpoint = gsSource.getEndpoint();
	    String label = gsSource.getLabel();
	    String identifier = gsSource.getUniqueIdentifier();
	    String version = gsSource.getVersion();

	    JSONObject source = new JSONObject();
	    source.put("id", identifier);
	    source.put("type", "composed");
	    source.put("title", label);
	    source.put("harvested", strategy == BrokeringStrategy.HARVESTED ? true : false);

	    // online
	    JSONObject online = new JSONObject();
	    online.put("url", endpoint);
	    online.put("function", "download");
	    online.put("accessType", "unknown");

	    // onlines array
	    JSONArray onlines = new JSONArray();
	    onlines.put(online);
	    source.put("online", onlines);

	    // service
	    JSONObject service = new JSONObject();
	    service.put("title", label);
	    service.put("type", "unknown");
	    service.put("version", version);
	    service.put("source", true);

	    // operation
	    JSONObject operation = new JSONObject();
	    operation.put("binding", "HTTP_GET");
	    operation.put("online", onlines);

	    // operation array
	    JSONArray operations = new JSONArray();
	    operations.put(operation);
	    source.put("operation", operations);

	    return source.toString();

	}

	JSONObject report = new JSONObject();

	MIMetadata md_Metadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	List<Identification> diList = Lists.newArrayList(md_Metadata.getDataIdentifications());

	// ---
	// id
	// ---

	String id = md_Metadata.getFileIdentifier();

	report.put("id", id);

	// ---
	// type
	// ---
	if (resource instanceof DatasetCollection) {
	    report.put("type", "composed");
	} else {
	    report.put("type", "simple");
	}

	// -----------------
	// parent identifier
	// -----------------
	String parent = md_Metadata.getParentIdentifier();
	if (parent != null) {
	    report.put("parentId", parent);
	}

	// --------------------
	// hierarchy level code
	// --------------------
	md_Metadata.getHierarchyLevelScopeCodeListValues().forEachRemaining(value -> {

	    report.put("hierarchyLevel", value);
	});

	// --------------------
	// coverageDescription (API TO IMPLEMENT)
	// --------------------
	CoverageDescription covDesc = md_Metadata.getCoverageDescription();
	if (covDesc != null) {
	    String coverageDescription = covDesc.getAttributeDescription();
	    report.put("coverageDescription", coverageDescription);
	}

	if (gsSource != null) {

	    String sourceLabel = gsSource.getLabel();
	    String sourceId = gsSource.getUniqueIdentifier();

	    JSONObject source = new JSONObject();
	    source.put("id", sourceId);
	    source.put("title", sourceLabel);

	    report.put("source", source);
	}

	Distribution distribution = md_Metadata.getDistribution();

	if (distribution != null) {

	    // ----------------------------
	    // distributor parties org.name
	    // ----------------------------

	    distribution.getDistributorParties().forEach(party -> {

		final String name = party.getOrganisationName();
		if (name != null && !name.isEmpty()) {

		    report.put("distributorOrgName", name);
		}
	    });

	    // -------
	    // format
	    // -------

	    List<Format> formats = Lists.newArrayList(distribution.getFormats());

	    if (!formats.isEmpty()) {
		JSONArray array = new JSONArray();
		for (Format format : formats) {
		    if (format.getName() != null && !format.getName().equals("")) {
			array.put(format.getName());
		    }
		}
		report.put("format", array);
	    }

	    // -------
	    // online
	    // -------

	    List<Online> onlineList = Lists.newArrayList(distribution.getDistributionOnlines());

	    onlineList.addAll(Lists.newArrayList(distribution.getDistributorOnlines()));

	    if (!onlineList.isEmpty()) {

		JSONArray online = new JSONArray();

		for (Online on : onlineList) {
		    JSONObject obj = createOnline(on, message);
		    if (obj != null) {
			online.put(obj);
		    }
		}

		ReportsMetadataHandler handler = new ReportsMetadataHandler(resource);
		List<DataComplianceReport> reports = handler.getReports();
		for (DataComplianceReport r : reports) {
		    DataComplianceTest lastSucceededTest = r.getLastSucceededTest();
		    if (lastSucceededTest.equals(DataComplianceTest.EXECUTION)) {
			if (r.getFullDataDescriptor().getDataType().equals(DataType.TIME_SERIES)) {
			    try {
				String onlineId = r.getOnlineId();
				JSONObject jsonOnline = new JSONObject();
				jsonOnline.put("protocol", "GWIS");
				jsonOnline.put("function", "info");
				URL base = new URL(message.getRequestAbsolutePath());
				Optional<String> token = message.getWebRequest().extractTokenId();
				String tokenString = "";
				if (token.isPresent()) {
				    tokenString = "&token=" + token.get();
				}
				URL url = new URL(base, "../gwis?request=plot&onlineId=" + onlineId + tokenString);
				jsonOnline.put("url", url.toExternalForm());
				online.put(jsonOnline);
			    } catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }
			    break;
			}
		    }

		}

		if (online.length() > 0) {
		    report.put("online", online);
		}
	    }

	}

	// -------------------------------------------
	//
	// Single value, from the first identification
	//

	if (!diList.isEmpty()) {

	    Identification firstId = diList.get(0);

	    // -----
	    // title
	    // -----
	    String title = firstId.getCitationTitle();
	    if (title != null && !title.equals("")) {
		title = normalizeText(title);
		report.put("title", title);
	    } else {
		report.put("title", "none");
	    }

	    // ---------------
	    // alternate title
	    // ---------------
	    String alternateTitle = firstId.getCitationAlternateTitle();
	    if (alternateTitle != null && !alternateTitle.equals("")) {
		report.put("alternateTitle", alternateTitle);
	    }

	    // -----------
	    // description
	    // -----------
	    String description = firstId.getAbstract();
	    if (description != null && !description.equals("")) {
		description = normalizeText(description);
		report.put("description", description);
	    }

	    // --------
	    // updated
	    // --------
	    String revDate = firstId.getCitationRevisionDate();

	    String revDateTime = null;
	    XMLGregorianCalendar dateTime = firstId.getCitationRevisionDateTime();
	    if (dateTime != null) {
		revDateTime = dateTime.toString();
	    }

	    if (revDate != null || revDateTime != null) {
		if (revDate != null) {
		    report.put("update", revDate);
		} else {
		    report.put("update", revDateTime);
		}
	    }

	    // --------
	    // created
	    // --------
	    String crDate = firstId.getCitationCreationDate();

	    String crDateTime = null;
	    dateTime = firstId.getCitationCreationDateTime();
	    if (dateTime != null) {
		crDateTime = dateTime.toString();
	    }

	    String created = crDate != null ? crDate : crDateTime != null ? crDateTime : null;
	    if (created != null) {
		report.put("created", created);
	    }

	    // --------------------------
	    // spatialRepresentationType
	    // --------------------------
	    if (firstId instanceof DataIdentification) {
		DataIdentification dataId = (DataIdentification) firstId;
		String spatialRepresentationType = dataId.getSpatialRepresentationTypeCodeListValue();
		if (spatialRepresentationType != null) {
		    report.put("spatialRepresentationType", spatialRepresentationType);
		}
	    }

	    // --------------------------
	    // raster mosaic
	    // --------------------------
	    resource.getExtensionHandler().getRasterMosaic().ifPresent(mosaic -> {

		report.put("rasterMosaic", mosaic);
	    });

	    // --------------
	    // dataAuthority (API TO IMPLEMENT)
	    // --------------
	    // String dataAuthority = firstId.getCitationMD_Authority();
	    // if (dataAuthority != null) {
	    // report.put("dataAuthority", dataAuthority);
	    // }

	    // ----------------
	    // dataIdentifiers (API TO IMPLEMENT)
	    // ----------------
	    // String md_Code = firstId.getCitationMD_Code();
	    // if (md_Code != null) {
	    // JSONArray array = new JSONArray();
	    // array.put(md_Code);
	    // report.put("dataIdentifiers", array);
	    // }

	    // -------
	    // service
	    // -------
	    if (firstId instanceof ServiceIdentification) {

		JSONObject service = new JSONObject();

		ServiceIdentification srv = (ServiceIdentification) firstId;

		String srvTitle = srv.getCitationTitle();
		if (srvTitle != null) {
		    service.put("title", srvTitle);
		}

		String srvDesc = srv.getAbstract();
		if (srvDesc != null) {
		    service.put("description", srvDesc);
		}

		// (API TO IMPLEMENT)
		// String srvType = srv.getServiceType();
		// if (srvType != null) {
		// service.put("type", srvType);
		// }

		// (API TO IMPLEMENT)
		// String srvVersion = srv.getServiceTypeVersion();
		// if (srvVersion != null) {
		// service.put("version", srvVersion);
		// }

		// (API TO IMPLEMENT)
		// String supInfo = srv.getSupplementalInformation();
		// if (supInfo != null && supInfo.equals("DAB-SOURCE")) {
		// service.put("dabSource", true);
		// } else {
		// service.put("dabSource", false);
		// }

		// (API TO IMPLEMENT)
		// List<OperationMetadata> opList = Lists.newArrayList(srv.getOperationMetadatas());
		// if (!opList.isEmpty()) {
		// JSONArray operation = new JSONArray();
		// for (OperationMetadata op : opList) {
		//
		// JSONObject opObject = new JSONObject();
		//
		// String opName = op.getOperationName();
		// if (opName != null) {
		// opObject.put("name", opName);
		// }
		//
		// List<Binding> bindings = op.getBindings();
		// if (!bindings.isEmpty()) {
		// JSONArray binding = new JSONArray();
		//
		// for (Binding b : bindings) {
		// binding.put(b.name());
		// }
		//
		// if (binding.length() > 0) {
		// opObject.put("binding", binding);
		// }
		// }
		//
		// onlineList = op.getOnlineResources();
		// if (!onlineList.isEmpty()) {
		//
		// JSONArray online = new JSONArray();
		//
		// for (Online on : onlineList) {
		// JSONObject obj = createOnline(on);
		// if (obj != null) {
		// online.put(obj);
		// }
		// }
		//
		// if (online.length() > 0) {
		// opObject.put("online", online);
		// }
		// }
		//
		// operation.put(opObject);
		// }
		//
		// if (operation.length() > 0) {
		// service.put("operation", operation);
		// }
		// }

	    }
	}

	// ---------------------------------------------
	//
	// array of values, from all the identifications
	//
	//
	JSONArray rightsArray = new JSONArray();
	JSONArray authorsArray = new JSONArray();
	JSONArray contributorsArray = new JSONArray();
	JSONArray whereArray = new JSONArray();
	JSONArray verticalExtentArray = new JSONArray();
	JSONArray whenArray = new JSONArray();
	JSONArray keywordArray = new JSONArray();
	JSONArray keywordTypeArray = new JSONArray();
	JSONArray topicArray = new JSONArray();
	JSONArray overviewArray = new JSONArray();
	JSONArray geossCategoryArray = new JSONArray();

	// -------
	// ORIGINATOR ORGANIZATION IDENTIFIER
	// -------
	ExtensionHandler handler = resource.getExtensionHandler();
	TreeSet<String> originatorOrganisationDescriptions = new TreeSet<>();
	originatorOrganisationDescriptions.addAll(handler.getOriginatorOrganisationDescriptions());
	JSONArray jsonOriginatorOrganisationDescription = new JSONArray();
	for (String originatorOrganisationDescription : originatorOrganisationDescriptions) {
	    if (originatorOrganisationDescription != null) {
		jsonOriginatorOrganisationDescription.put(originatorOrganisationDescription);
	    }
	}
	if (jsonOriginatorOrganisationDescription.length() > 0) {
	    report.put("origOrgDesc", jsonOriginatorOrganisationDescription);
	}
	Optional<String> themeCategoryOpt = handler.getThemeCategory();

	if (themeCategoryOpt.isPresent()) {
	    report.put("themeCategory", themeCategoryOpt.get());
	}

	for (Identification identification : diList) {

	    // -------
	    // rights
	    // -------
	    List<String> rights = Lists.newArrayList(identification.getLegalConstraintsAccessCodes());
	    List<String> rights2 = Lists.newArrayList(identification.getLegalConstraintsUseLimitations());
	    rights.addAll(rights2);

	    if (!rights.isEmpty()) {
		for (String r : rights) {
		    rightsArray.put(r);
		}
	    }

	    // -----------------------
	    // author and contributor
	    // -----------------------

	    if (identification instanceof DataIdentification) {

		Iterator<ResponsibleParty> pointOfContacts = ((DataIdentification) identification).getPointOfContacts();

		for (Iterator<ResponsibleParty> iterator = pointOfContacts; iterator.hasNext(); ) {

		    ResponsibleParty party = iterator.next();
		    if (party != null) {
			String orgName = party.getOrganisationName();
			String posName = party.getPositionName();
			String individualName = party.getIndividualName();

			Contact contact = party.getContact();
			JSONObject partyObj = new JSONObject();
			if (orgName != null) {
			    partyObj.put("orgName", orgName);
			}
			if (posName != null) {
			    partyObj.put("posName", posName);
			}
			if (individualName != null) {
			    partyObj.put("individualName", individualName);
			}
			if (contact != null) {

			    Address address = contact.getAddress();
			    if (address != null) {
				String email = address.getElectronicMailAddress();
				String city = address.getCity();

				if (email != null) {
				    partyObj.put("email", email);
				}
				if (city != null) {
				    partyObj.put("city", city);
				}
			    }

			    Online on = contact.getOnline();
			    JSONArray onlineArray = new JSONArray();
			    if (on != null) {
				JSONObject online = createOnline(on, message);
				onlineArray.put(online);
			    }

			    if (onlineArray.length() > 0) {
				partyObj.put("online", onlineArray);
			    }
			}

			String value = party.getRoleCode();
			if (value != null) {
			    switch (value) {
			    case "author":
				authorsArray.put(partyObj);
				break;
			    case "contributor":
				contributorsArray.put(partyObj);
				break;
			    }
			}
		    }
		}
	    }

	    // -----
	    // where
	    // -----

	    if (identification instanceof DataIdentification) {

		DataIdentification dataId = (DataIdentification) identification;

		List<GeographicBoundingBox> boundingBoxList = Lists.newArrayList(dataId.getGeographicBoundingBoxes());
		if (!boundingBoxList.isEmpty()) {

		    for (GeographicBoundingBox bbox : boundingBoxList) {
			Double south = bbox.getSouth();
			Double west = bbox.getWest();
			Double north = bbox.getNorth();
			Double east = bbox.getEast();

			if (south != null && west != null && north != null && east != null) {
			    JSONObject bboxObj = new JSONObject();
			    bboxObj.put("south", south);
			    bboxObj.put("west", west);
			    bboxObj.put("north", north);
			    bboxObj.put("east", east);

			    whereArray.put(bboxObj);
			}
		    }
		}
	    }

	    // ---------------
	    // verticalExtent
	    // ---------------
	    if (identification instanceof DataIdentification) {

		DataIdentification dataId = (DataIdentification) identification;
		List<VerticalExtent> verticalExtentList = Lists.newArrayList(dataId.getVerticalExtents());

		if (!verticalExtentList.isEmpty()) {

		    for (VerticalExtent extent : verticalExtentList) {

			Double max = extent.getMaximumValue();
			Double min = extent.getMinimumValue();

			if (max != null && min != null && !Double.isNaN(max) && !Double.isNaN(min)) {
			    JSONObject ext = new JSONObject();

			    ext.put("min", min);
			    ext.put("max", max);

			    verticalExtentArray.put(ext);
			}
		    }
		}
	    }

	    // -----
	    // when
	    // -----

	    if (identification instanceof DataIdentification) {

		DataIdentification dataId = (DataIdentification) identification;

		List<TemporalExtent> temporalExtentList = Lists.newArrayList(dataId.getTemporalExtents());
		if (!temporalExtentList.isEmpty()) {

		    for (TemporalExtent temp : temporalExtentList) {

			String beginPosition = temp.getBeginPosition();
			String endPosition = temp.getEndPosition();
			addWhen(whenArray, beginPosition, endPosition);

			String timeInstantBegin = temp.getTimeInstantBegin();
			String timeInstantEnd = temp.getTimeInstantEnd();
			addWhen(whenArray, timeInstantBegin, timeInstantEnd);
		    }
		}
	    }

	    // --------
	    // keyword
	    // --------
	    List<Keywords> keywordList = Lists.newArrayList(identification.getKeywords());
	    if (!keywordList.isEmpty()) {

		for (Keywords kwd : keywordList) {
		    String type = kwd.getTypeCode();
		    Iterator<String> keywords = kwd.getKeywords();
		    while (keywords.hasNext()) {
			String keyword = (String) keywords.next();
			keywordArray.put(normalizeText(keyword));
			keywordTypeArray.put(normalizeText(type));
		    }

		}
	    }

	    // ------
	    // topic
	    // ------
	    List<String> topicList = new ArrayList<String>();
	    if (identification instanceof DataIdentification) {
		DataIdentification dataId = (DataIdentification) identification;

		List<String> topicCategoryList = Lists.newArrayList(dataId.getTopicCategoriesStrings());
		if (!topicCategoryList.isEmpty()) {

		    for (String code : topicCategoryList) {
			if (code != null) {
			    topicList.add(code);
			    topicArray.put(code);
			}
		    }
		}
	    }

	    // ---------
	    // overview
	    // ---------
	    if (identification instanceof DataIdentification) {

		DataIdentification dataId = (DataIdentification) identification;

		List<BrowseGraphic> graphicOverviewList = Lists.newArrayList(dataId.getGraphicOverviews());

		if (!graphicOverviewList.isEmpty()) {

		    for (BrowseGraphic graphic : graphicOverviewList) {
			String fileName = graphic.getFileName();
			if (fileName != null) {
			    overviewArray.put(fileName);
			}
		    }
		}
	    }

	    // --------------
	    // geossCategory
	    // --------------
	    if (!topicList.isEmpty()) {

		for (String topic : topicList) {
		    if (GEOSS_CATEGORIES.contains(topic)) {
			geossCategoryArray.put(topic);
		    } else {
			geossCategoryArray.put("documentFileGraphic");
		    }
		}
	    }
	}

	// ---------------
	// inserts rights
	if (rightsArray.length() > 0) {
	    report.put("rights", rightsArray);
	}

	// ----------------
	// inserts authors
	if (authorsArray.length() > 0) {
	    report.put("author", authorsArray);
	}

	// ---------------------
	// inserts contributors
	if (contributorsArray.length() > 0) {
	    report.put("contributors", contributorsArray);
	}

	// --------------
	// inserts where
	if (whereArray.length() > 0) {
	    report.put("where", whereArray);
	}

	// -----------------------
	// inserts verticalExtent
	if (verticalExtentArray.length() > 0) {
	    report.put("verticalExtent", verticalExtentArray);
	}

	// --------------
	// inserts when
	if (whenArray.length() > 0) {
	    report.put("when", whenArray);
	}

	// ----------------
	// inserts keyword
	if (keywordArray.length() > 0) {
	    report.put("keyword", keywordArray);
	}
	if (keywordTypeArray.length() > 0) {
	    report.put("keyword_type", keywordTypeArray);
	}

	// --------------
	// inserts topic
	if (topicArray.length() > 0) {
	    report.put("topic", topicArray);
	}

	// -----------------
	// inserts overview
	if (overviewArray.length() > 0) {
	    report.put("overview", overviewArray);
	}

	// ----------------------
	// inserts geossCategory
	if (geossCategoryArray.length() > 0) {
	    report.put("geossCategory", geossCategoryArray);
	}

	// --------------------
	// attributeDescription
	// --------------------
	JSONArray attributeDescription = new JSONArray();
	JSONArray attributeTitle = new JSONArray();
	List<CoverageDescription> coverageDescriptions = Lists.newArrayList(md_Metadata.getCoverageDescriptions());
	TreeSet<String> sortedAttributeDescriptions = new TreeSet<>();
	TreeSet<String> sortedAttributeTitles = new TreeSet<>();
	//	for (CoverageDescription coverageDescription : coverageDescriptions) {
	//	    String attribute = coverageDescription.getAttributeDescription();
	//	    String title = coverageDescription.getAttributeTitle();
	//	    if (attribute != null) {
	//		sortedAttributeDescriptions.add(attribute);
	//	    }
	//	    if (title != null) {
	//		sortedAttributeTitles.add(title);
	//	    }
	//	}

	List<String> titles = resource.getIndexesMetadata().read(MetadataElement.ATTRIBUTE_TITLE);
	sortedAttributeTitles.addAll(titles);

	List<String> descriptions = resource.getIndexesMetadata().read(MetadataElement.ATTRIBUTE_DESCRIPTION);
	sortedAttributeDescriptions.addAll(descriptions);

	for (String sortedDescription : sortedAttributeDescriptions) {
	    attributeDescription.put(sortedDescription);
	}
	if (attributeDescription.length() > 0) {
	    report.put("attributeDescription", attributeDescription);
	}

	for (String sortedTitle : sortedAttributeTitles) {
	    attributeTitle.put(sortedTitle);
	}
	if (attributeTitle.length() > 0) {
	    report.put("attributeTitle", attributeTitle);
	}

	// --------------------
	// platformDescription & platformIdentifier
	// --------------------
	List<MIPlatform> platformList = Lists.newArrayList(md_Metadata.getMIPlatforms());
	TreeSet<String> sortedPlatformDescriptions = new TreeSet<>();
	TreeSet<String> sortedPlatformTitles = new TreeSet<>();
	if (!platformList.isEmpty()) {

	    // JSONArray platformIdentifier = new JSONArray();

	    for (MIPlatform platform : platformList) {

		String pDesc = platform.getDescription();
		if (pDesc != null) {
		    sortedPlatformDescriptions.add(pDesc);
		}
		Citation citation = platform.getCitation();
		String pTitle = citation != null ? citation.getTitle() : null;
		if (pTitle != null) {
		    sortedPlatformTitles.add(pTitle);
		}
		// String pId = platform.getMDIdentifierCode();
		// if (pId != null) {
		// platformIdentifier.put(pId);
		// }
	    }
	}

	JSONArray platformDescription = new JSONArray();
	for (String sortedDescription : sortedPlatformDescriptions) {
	    platformDescription.put(sortedDescription);
	}

	if (platformDescription.length() > 0) {
	    report.put("platformDescription", platformDescription);
	}

	JSONArray platformTitle = new JSONArray();
	for (String sortedTitle : sortedPlatformTitles) {
	    platformTitle.put(sortedTitle);
	}

	if (platformTitle.length() > 0) {
	    report.put("platformTitle", platformTitle);
	}
	// if (platformIdentifier.length() > 0) {
	// report.put("platformIdentifier", platformIdentifier);
	// }

	// ------------------
	// instrumentDescription & instrumentIdentifier
	// ------------------
	List<MIInstrument> instrList = Lists.newArrayList(md_Metadata.getMIInstruments());
	if (!instrList.isEmpty()) {

	    JSONArray instrumentIdentifier = new JSONArray();
	    JSONArray instrumentDescription = new JSONArray();
	    JSONArray instrumentTitle = new JSONArray();

	    TreeSet<String> sortedInstrumentDescriptions = new TreeSet<>();
	    TreeSet<String> sortedInstrumentTitles = new TreeSet<>();

	    for (MIInstrument instrument : instrList) {

		String iDesc = instrument.getDescription();
		if (iDesc != null) {
		    sortedInstrumentDescriptions.add(iDesc);
		}
		String iTitle = instrument.getTitle();
		if (iTitle != null) {
		    sortedInstrumentTitles.add(iTitle);
		}
		// String iId = instrument.getMDIdentifierCode();
		// if (iId != null) {
		// instrumentIdentifier.put(iId);
		// }
	    }

	    for (String sortedDescription : sortedInstrumentDescriptions) {
		instrumentDescription.put(sortedDescription);
	    }

	    if (instrumentDescription.length() > 0) {
		report.put("instrumentDescription", instrumentDescription);
	    }

	    for (String sortedTitle : sortedInstrumentTitles) {
		instrumentTitle.put(sortedTitle);
	    }

	    if (instrumentTitle.length() > 0) {
		report.put("instrumentTitle", instrumentTitle);
	    }
	    // if (instrumentIdentifier.length() > 0) {
	    // report.put("instrumentIdentifier", instrumentIdentifier);
	    // }
	}

	return report.toString();
    }

    private String normalizeText(String text) {
	if (text == null) {
	    return "";
	}
	return text.trim().replaceAll("\\s+", " ");
    }

    private void addWhen(JSONArray when, String from, String to) {

	if (from != null || to != null) {
	    JSONObject obj = new JSONObject();
	    if (from != null) {
		obj.put("from", normalizeTime(from));
	    }
	    if (to != null) {
		obj.put("to", normalizeTime(to));
	    }
	    when.put(obj);
	}
    }

    private String normalizeTime(String time) {

	return time.contains("T") ? time : time + "T00:00:00";
    }

    private JSONObject createOnline(Online on, DiscoveryMessage message) {

	String url = on.getLinkage();
	String name = on.getName();
	String description = on.getDescription();
	String protocol = on.getProtocol();
	String function = on.getFunctionCode();
	String anchor = on.getDescriptionGmxAnchor();

	JSONObject online = new JSONObject();
	boolean addOnline = false;
	if (url != null) {

	    // filter old gi-axe urls
	    if (url.contains("axe.geodab.eu") || url.contains("geodab-gi-axe")) {
		return null;
	    }
	    // china geoss case: endpoint changed
	    String token = "ChinaGEOSS-2018@124.16.184.25";
	    if (url.contains(token)) {
		url = url.replace(token, "ChinaGEOSS@124.16.184.9");
	    }

	    // Trans-African Hydro-Meteorological Observatory (TAHMO) case: use proxy
	    if (url.startsWith(SOS_TAHMO_URL)) {
		try {
		    // check if it is the base endpoint only or getobservation request
		    URL checkURL = new URL(url);
		    if (checkURL.getQuery() != null && !checkURL.getQuery().isEmpty()) {
			String instanceBaseUrl = message.getWebRequest().getUriInfo().getBaseUri().toString();
			String replaceUrl = instanceBaseUrl.endsWith("/")
				? instanceBaseUrl + SOS_TAHMO_PROXY_PATH
				: instanceBaseUrl + "/" + SOS_TAHMO_PROXY_PATH;
			url = url.replace(SOS_TAHMO_URL, replaceUrl);
		    }

		} catch (MalformedURLException e) {
		    e.printStackTrace();
		}

	    }

	    // TWIGA use case: use proxy (should replace the TAHMO SOS)
	    if (url.startsWith(SOS_TWIGA_URL)) {
		try {
		    // check if it is the base endpoint only or getobservation request
		    URL checkURL = new URL(url);
		    if (checkURL.getQuery() != null && !checkURL.getQuery().isEmpty()) {
			String instanceBaseUrl = message.getWebRequest().getUriInfo().getBaseUri().toString();
			String replaceUrl = instanceBaseUrl.endsWith("/")
				? instanceBaseUrl + SOS_TAHMO_PROXY_PATH
				: instanceBaseUrl + "/" + SOS_TAHMO_PROXY_PATH;
			url = url.replace(SOS_TWIGA_URL, replaceUrl);
		    }

		} catch (MalformedURLException e) {
		    e.printStackTrace();
		}

	    }

	    online.put("url", url);
	    addOnline = true;
	}
	if (name != null) {
	    online.put("name", name);
	    addOnline = true;
	}
	if (description != null) {
	    online.put("description", description);
	    addOnline = true;
	}
	if (protocol != null) {
	    online.put("protocol", protocol);
	    addOnline = true;
	}
	if (function != null) {
	    online.put("function", function);
	    addOnline = true;
	}
	if (anchor != null && anchor.startsWith("http://www.essi-lab.eu/broker/accesstypes/")) {
	    addOnline = true;
	    anchor = anchor.replace("http://www.essi-lab.eu/broker/accesstypes/", "");
	    if (anchor.equals("simple")) {
		anchor = "direct";
	    }
	    online.put("accessType", anchor);
	}

	if (addOnline) {
	    return online;
	}

	return null;
    }

    /**
     * Returns the {@link ESSILabProvider}
     */
    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public MappingSchema getMappingSchema() {

	return JS_API_MAPPING_SCHEMA;
    }

}
