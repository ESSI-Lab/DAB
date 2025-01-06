package eu.essi_lab.accessor.nmdis.erddap;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.annotation.XmlTransient;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.common.ISO2014NameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.emod_pace.EMODPACEThemeCategory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.OriginalMetadata;
import net.opengis.iso19139.gmd.v_20060504.AbstractMDIdentificationType;
import net.opengis.iso19139.gmd.v_20060504.MDDataIdentificationType;
import net.opengis.iso19139.gmd.v_20060504.MDIdentificationPropertyType;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class NMDIS_ERDDAPConnector extends HarvestedQueryConnector<NMDIS_ERDDAPConnectorSetting> {

    /**
     * 
     */
    public static enum ERDDAP_STATION {
	EMODPACE_NMDIS_CCS("EMODPACE_NMDIS_CCS"), EMODPACE_NMDIS_CST("EMODPACE_NMDIS_CST"), EMODPACE_NMDIS_MSL("EMODPACE_NMDIS_MSL");

	private String id;

	ERDDAP_STATION(String id) {
	    this.id = id;
	}

	public String getId() {
	    return id;
	}

    }

    public static final String TYPE = "NMDIS_ERDDAPConnector";

    private static final String NMDIS_ERDDAP_CONNECTOR_ERROR = "NMDIS_ERDDAP_CONNECTOR_ERROR";

    @Override
    public boolean supports(GSSource source) {

	return (source.getEndpoint().startsWith("https://erddap.emodnet-physics.eu/erddap/tabledap/")
		|| source.getEndpoint().startsWith("https://prod-erddap.emodnet-physics.eu/nmdis/erddap/tabledap/"));
    }

    @XmlTransient

    private String metadataTemplate = null;

    @XmlTransient

    private XMLDocumentReader reader = null;

    private int countDataset = 0;

    private int countTimeDataset = 0;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	String rt = request.getResumptionToken();

	int start = 0;
	if (rt != null) {

	    start = Integer.valueOf(rt);
	}

	String station = "";

	Map<String, List<String>> variables = new HashMap<String, List<String>>();

	switch (start) {
	case 0:
	    // EMODPACE_NMDIS_CCS
	    station = ERDDAP_STATION.EMODPACE_NMDIS_CCS.name();
	    for (NMDIS_ERDDAP_CCSVariable var : NMDIS_ERDDAP_CCSVariable.values()) {
		List<String> list = new ArrayList<String>();
		list.add(var.getName());
		list.add(var.getUnits());
		variables.put(var.name(), list);
	    }
	    break;
	case 1:
	    // EMODPACE_NMDIS_CST
	    station = ERDDAP_STATION.EMODPACE_NMDIS_CST.name();
	    for (NMDIS_ERDDAP_CSTVariable var : NMDIS_ERDDAP_CSTVariable.values()) {
		List<String> list = new ArrayList<String>();
		list.add(var.getName());
		list.add(var.getUnits());
		variables.put(var.name(), list);
	    }
	    break;
	case 2:
	    // EMODPACE_NMDIS_MSL
	    station = ERDDAP_STATION.EMODPACE_NMDIS_MSL.name();
	    for (NMDIS_ERDDAP_MSLVariable var : NMDIS_ERDDAP_MSLVariable.values()) {
		List<String> list = new ArrayList<String>();
		list.add(var.getName());
		list.add(var.getUnits());
		variables.put(var.name(), list);
	    }
	    break;

	default:
	    break;
	}

	String metadataURL = getSourceURL().replace("tabledap", "metadata/iso19115/xml") + station + "_iso19115.xml";

	if (metadataTemplate == null) {
	    metadataTemplate = downloadURL(metadataURL);
	}

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();

	// Object variableCategory;
	// switch (rt) {
	// case "0":
	// variableCategory = ERDDAP_STATION.EMODPACE_NMDIS_CCS;
	// break;
	// case "1":
	// variableCategory = ERDDAP_STATION.EMODPACE_NMDIS_CST;
	// break;
	// case "2":
	// variableCategory = ERDDAP_STATION.EMODPACE_NMDIS_MLS;
	// break;
	// default:
	// break;
	// }

	// https://erddap.emodnet-physics.eu/erddap/tabledap/EMODPACE_NMDIS_CCS.geoJson?longitude%2Clatitude%2CStationName&DRYT!=NaN&distinct()
	try {
	    for (Map.Entry<String, List<String>> entry : variables.entrySet()) {
		List<String> findDuplicates = new ArrayList<String>();
		String variable = entry.getKey();
		List<String> values = entry.getValue();
		String variableDescription = "";
		String variableUnit = "";
		if (!values.isEmpty() && values.size() == 2) {
		    variableDescription = values.get(0);
		    variableUnit = values.get(1);
		}
		String stationURL = getSourceURL() + station + ".geoJson?longitude%2Clatitude%2CStationName&" + variable
			+ "!=NaN&distinct()";
		String stationString = downloadURL(stationURL);
		GSLoggerFactory.getLogger(getClass()).info("Parsing geoJSON...");
		JSONObject jsonStation = new JSONObject(stationString);
		JSONArray jsonArray = jsonStation.optJSONArray("features");
		GSLoggerFactory.getLogger(getClass()).info("Parsed...");

		if (jsonArray != null) {
		    for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject res = (JSONObject) jsonArray.get(i);
			JSONObject prop = res.optJSONObject("properties");
			JSONObject geometry = res.optJSONObject("geometry");

			if (prop != null && geometry != null) {
			    String stationName = prop.optString("StationName");
			    if (findDuplicates.contains(stationName.toLowerCase())) {
				continue;
			    }
			    findDuplicates.add(stationName.toLowerCase());
			    countDataset++;
			    GSLoggerFactory.getLogger(getClass()).info("Producing dataset {}", countDataset);
			    String coord = geometry.optString("coordinates");
			    JSONArray cc = geometry.optJSONArray("coordinates");
			    Double lon = cc.getDouble(0);
			    Double lat = cc.getDouble(1);

			    MDMetadata mdMetadata;

			    // remove MD_identifier from XML (because it seems that the clear() function for identifier
			    // element
			    // doesn't exist
			    XMLDocumentReader reader = new XMLDocumentReader(metadataTemplate);
			    XMLDocumentWriter writer = new XMLDocumentWriter(reader);
			    reader.setNamespaceContext(new ISO2014NameSpaceContext());
			    writer.remove(
				    "/*:MI_Metadata/*:identificationInfo/*:MD_DataIdentification/*:citation/*:CI_Citation/*:identifier");
			    mdMetadata = new MDMetadata(reader.asString());

			    List<MDIdentificationPropertyType> infos = mdMetadata.getElementType().getIdentificationInfo();
			    List<MDIdentificationPropertyType> infosToRemove = new ArrayList<>();
			    for (MDIdentificationPropertyType info : infos) {
				AbstractMDIdentificationType ainfo = info.getAbstractMDIdentification().getValue();
				if (ainfo instanceof MDDataIdentificationType) {
				    MDDataIdentificationType dataInfo = (MDDataIdentificationType) ainfo;
				    // dataInfo.getDescriptiveKeywords().clear();
				    dataInfo.getExtent().clear();
				} else {
				    infosToRemove.add(info);
				}
			    }
			    mdMetadata.getElementType().getIdentificationInfo().removeAll(infosToRemove);

			    MIMetadata miMetadata = new MIMetadata(mdMetadata.getElementType());

			    Dataset dataset = new Dataset();
			    dataset.getHarmonizedMetadata().getCoreMetadata().setMIMetadata(miMetadata);
			    ExtensionHandler extendedMetadataHandler = dataset.getExtensionHandler();

			    try {

				// HashMap<String, String> values = new HashMap<>();
				// Node recordNode = recordNodes[i];
				// Node[] valueNodes = reader.evaluateNodes(recordNode, "*:td");
				// for (int j = 0; j < valueNodes.length; j++) {
				// Node valueNode = valueNodes[j];
				// String value = reader.evaluateString(valueNode, ".");
				// if (value != null && !value.equals("")) {
				// values.put(headers[j], value);
				// }
				// }
				// String expocode = values.get("expocode");
				// String datasetName = values.get("dataset_name");
				// String platformName = values.get("platform_name");
				// String platformType = values.get("platform_type");
				// String organization = values.get("organization");
				// String geospatialLonMin = values.get("geospatial_lon_min");
				// String geospatialLonMax = values.get("geospatial_lon_max");
				// String geospatialLatMin = values.get("geospatial_lat_min");
				// String geospatialLatMax = values.get("geospatial_lat_max");
				// String timeCoverageStart = values.get("time_coverage_start");
				// String timeCoverageEnd = values.get("time_coverage_end");
				// String investigators = values.get("investigators");
				// String socatDOI = values.get("socat_doi");

				DataIdentification identification = miMetadata.getDataIdentification();
				// identification.setResourceIdentifier(socatDOI);
				identification.setCitationTitle(stationName + " - " + variableDescription);

				identification.addKeyword("EMOD-PACE project");

				// platformName
				if (stationName != null && !stationName.isEmpty()) {
				    MIPlatform platform = new MIPlatform();
				    platform.setMDIdentifierCode(stationName);
				    Citation citation = new Citation();
				    citation.setTitle(stationName);
				    platform.setCitation(citation);
				    // platform.setDescription(stat);
				    miMetadata.addMIPlatform(platform);
				    Keywords k = new Keywords();
				    k.setTypeCode("platform");
				    k.addKeyword(stationName);
				    identification.addKeywords(k);
				}

				if (lon != null && lat != null && lon < 180 && lon > -180 && lat < 90 && lat > -90) {
				    identification.clearGeographicBoundingBoxes();
				    identification.addGeographicBoundingBox(lat, lon, lat, lon);
				}
				identification.clearVerticalExtents();
				//
				// List<String> organizations = new ArrayList<>();
				// List<String> individuals = new ArrayList<>();
				// if (organization != null) {
				// String[] split;
				// if (organization.contains(":")) {
				// split = organization.split(":");
				// } else {
				// split = new String[] { organization };
				// }
				// for (String s : split) {
				// organizations.add(s);
				// }
				// }
				// if (investigators != null) {
				// String[] split;
				// if (investigators.contains(":")) {
				// split = investigators.split(":");
				// } else {
				// split = new String[] { investigators };
				// }
				// for (String s : split) {
				// individuals.add(s);
				// }
				// }
				// identification.clearPointOfContacts();
				// for (String individual : individuals) {
				// ResponsibleParty pointOfContact = new ResponsibleParty();
				// pointOfContact.setIndividualName(individual);
				// pointOfContact.setRoleCode("principalInvestigator");
				// identification.addPointOfContact(pointOfContact);
				// }
				// for (String o : organizations) {
				// ResponsibleParty pointOfContact = new ResponsibleParty();
				// pointOfContact.setOrganisationName(o);
				// pointOfContact.setRoleCode("author");
				// identification.addPointOfContact(pointOfContact);
				// }

				miMetadata.clearContentInfos();
				// String[] parameters = new String[] { "salinity", "sea surface temperature",
				// "sea-level
				// air pressure",
				// "WOCE flag for aqueous CO2", "fCO2" };
				// for (String parameter : parameters) {
				// }
				CoverageDescription description = new CoverageDescription();
				description.setAttributeIdentifier(variable);
				description.setAttributeDescription(variableDescription);
				description.setAttributeTitle(variableDescription);
				miMetadata.addCoverageDescription(description);

				// https://erddap.emodnet-physics.eu/erddap/tabledap/EMODPACE_NMDIS_CST.csv?StationName%2Ctime%2CTEMP_8&StationName=%22Shi%20Dao%22&distinct()
				String onlineUrl = getSourceURL() + station + ".csv?StationName%2Ctime%2C" + variable + "&StationName=%22"
					+ stationName + "%22&distinct()";
				Online online = new Online();
				online.setLinkage(onlineUrl);
				online.setProtocol("WWW:DOWNLOAD-1.0-http--download");
				online.setFunctionCode("download");
				online.setDescription("Direct Download");
				miMetadata.getDistribution().addDistributionOnline(online);
				extendedMetadataHandler.addThemeCategory(EMODPACEThemeCategory.OCEANOGRAPHY.getThemeCategory());

				try {
				    String identifier = StringUtils
					    .hashSHA1messageDigest("NMDIS_ERDDAP:" + stationName + ":" + variableDescription);
				    dataset.getHarmonizedMetadata().getCoreMetadata().setIdentifier(identifier);
				    miMetadata.setFileIdentifier(identifier);
				    // miMetadata.getDataIdentification().setResourceIdentifier(expocode);

				    // temporal extent
				    // https://erddap.emodnet-physics.eu/erddap/tabledap/EMODPACE_NMDIS_MSL.json?StationName%2Ctime&StationName=%22Dalian%22&SLEV!=NaN&orderBy(%22time%22)
				    String getTimeURL = getSourceURL() + station + ".json?time&StationName=%22"
					    + URLEncoder.encode(stationName, "UTF-8") + "%22&" + variable + "!=NaN&orderBy(%22time%22)";
				    String timeString = downloadURL(getTimeURL);
				    GSLoggerFactory.getLogger(getClass()).info("Parsing JSON for temporal extent...");
				    JSONObject jsonTime = new JSONObject(timeString);
				    GSLoggerFactory.getLogger(getClass()).info("Parsed...");
				    JSONObject jsonTable = jsonTime.optJSONObject("table");
				    if (jsonTable != null) {
					JSONArray timeArray = jsonTable.optJSONArray("rows");
					if (timeArray != null) {
					    int size = timeArray.length();
					    if (size > 1) {
						JSONArray startArray = timeArray.optJSONArray(0);
						JSONArray endArray = timeArray.optJSONArray(size - 1);
						String startDate = null;
						String endDate = null;
						if (!startArray.isNull(0)) {
						    startDate = startArray.getString(0);
						}
						if (!endArray.isNull(0)) {
						    endDate = endArray.getString(0);
						}
						GSLoggerFactory.getLogger(getClass()).info("Parsed time...");
						if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
						    identification.addTemporalExtent(startDate, endDate);
						    countTimeDataset++;
						    GSLoggerFactory.getLogger(getClass()).info("Producing dataset with time interval {}",
							    countTimeDataset);
						}

					    }
					}

				    }
				} catch (Exception e) {
				    e.printStackTrace();
				}
				// if (timeCoverageStart != null && timeCoverageEnd != null) {
				// if (timeCoverageEnd.contains("T")) {
				// miMetadata.setDateStampAsDate(timeCoverageEnd.substring(0,
				// timeCoverageEnd.indexOf("T")));
				// }
				// identification.setCitationRevisionDate(timeCoverageEnd);
				// identification.addTemporalExtent(timeCoverageStart, timeCoverageEnd);
				// }
				OriginalMetadata record = new OriginalMetadata();

				record.setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);

				record.setMetadata(dataset.asString(true));

				ret.addRecord(record);
			    } catch (Exception ee) {
				ee.printStackTrace();
			    }

			}
		    }
		}
	    }
	} catch (Exception e1) {
	    e1.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e1.getMessage(), e1);
	    throw GSException.createException(getClass(), ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR,
		    NMDIS_ERDDAP_CONNECTOR_ERROR, e1);
	}

	Optional<Integer> mr = getSetting().getMaxRecords();
	metadataTemplate = null;
	start = start + 1;
	if (start > 2) {
	    ret.setResumptionToken(null);
	    GSLoggerFactory.getLogger(getClass()).info("Dataset with time interval: {}", countTimeDataset);
	    GSLoggerFactory.getLogger(getClass()).info("Total number of dataset: {}", countDataset);
	} else {
	    ret.setResumptionToken(String.valueOf(start));
	}

	// if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && (index + tranche) > mr.get()) {
	// ret.setResumptionToken(null);
	// } else {
	// ret.setResumptionToken(rt);
	// }
	return ret;
    }

    private String downloadURL(String url) {
	Downloader d = new Downloader();
	return d.downloadOptionalString(url).get();
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected NMDIS_ERDDAPConnectorSetting initSetting() {

	return new NMDIS_ERDDAPConnectorSetting();
    }

    public static void main(String[] args) throws Exception {

	NetcdfDataset nc = NetcdfDataset
		.openDataset("https://prod-erddap.emodnet-physics.eu/nmdis//erddap/tabledap/EMODPACE_NMDIS_CST.das");
	List<Variable> var = nc.getVariables();

	for (Variable v : var) {
	    System.out.println(v.getShortName());
	}

    }
}
