package eu.essi_lab.indexes;

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

import eu.essi_lab.api.database.Database.*;
import eu.essi_lab.indexes.marklogic.*;
import eu.essi_lab.iso.datamodel.classes.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.index.*;
import eu.essi_lab.model.resource.*;
import eu.essi_lab.model.resource.worldcereal.*;
import net.opengis.gml.v_3_2_0.*;
import net.opengis.iso19139.gco.v_20060504.*;

import javax.xml.datatype.*;
import java.math.*;
import java.util.*;
import java.util.stream.*;

/**
 * This class groups all the available {@link IndexedMetadataElement}s
 *
 * @author Fabrizio
 * @see MetadataElement
 */
public final class IndexedMetadataElements extends IndexedElementsGroup {

    /**
     * Retrieves all the {@link IndexedMetadataElement}s declared in this class
     *
     * @return
     */
    public static List<IndexedMetadataElement> getIndexes() {

	return getIndexes(IndexedMetadataElements.class).//
		stream().//
		map(index -> (IndexedMetadataElement) index).//
		collect(Collectors.toList());
    }

    /**
     * Retrieves all the {@link IndexedElementInfo}s owned by the {@link IndexedMetadataElement}s declared in this class
     *
     * @param impl
     * @return
     */
    public static List<IndexedElementInfo> getIndexesInfo(DatabaseImpl impl) {

	return getIndexes().//
		stream().//
		// this check excludes the bbox index which is in fact not directly indexed
			filter(el -> el.getInfo(impl.getName()).getIndexType() != null).//
			map(el -> el.getInfo(impl.getName())).//
			collect(Collectors.toList());//
    }

    // ----------------------------------------------
    //
    // index type: range element
    // scalar type: string (except bounding box, having no scalar type)
    // scalar type: double (except bounding box, having no scalar type)
    //

    // this element body is empty, since its value is the composition of TOPIC_CATEGORY and KEYWORD.
    // it is provided only for completeness
    public static final IndexedMetadataElement SUBJECT = new IndexedMetadataElement(MetadataElement.SUBJECT) {
	@Override
	public void defineValues(GSResource resource) {
	}
    };

    public static final IndexedMetadataElement ANY_TEXT = new IndexedMetadataElement(MetadataElement.ANY_TEXT) {

	@Override
	public void defineValues(GSResource resource) {

	    try {

		String anyText = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getTextContent();
		addValue(anyText);

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(IndexedElementsWriter.class).warn("Unable to get metadata text content");
		GSLoggerFactory.getLogger(IndexedElementsWriter.class).warn(e.getMessage(), e);
	    }
	}
    };

    public static final IndexedMetadataElement DATE_STAMP = new IndexedMetadataElement(MetadataElement.DATE_STAMP) {
	@Override
	public void defineValues(GSResource resource) {

	    String dateStamp = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDateStamp();
	    if (checkStringValue(dateStamp)) {

		addValue(dateStamp);

	    } else {
		XMLGregorianCalendar dateTimeStamp = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDateTimeStamp();
		if (dateTimeStamp != null) {

		    addValue(dateTimeStamp.toString());
		}
	    }
	}
    };

    public static final IndexedMetadataElement IDENTIFIER = new IndexedMetadataElement(MetadataElement.IDENTIFIER) {
	@Override
	public void defineValues(GSResource resource) {

	    String identifier = resource.getHarmonizedMetadata().getCoreMetadata().getIdentifier();

	    if (checkStringValue(identifier)) {
		addValue(identifier);
	    }
	}
    };

    public static final IndexedMetadataElement HIERARCHY_LEVEL_CODE_LIST_VALUE = new IndexedMetadataElement(
	    MetadataElement.HIERARCHY_LEVEL_CODE_LIST_VALUE) {
	@Override
	public void defineValues(GSResource resource) {

	    MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	    Iterator<String> codeListValues = miMetadata.getHierarchyLevelScopeCodeListValues();
	    while (codeListValues.hasNext()) {

		String codeListValue = codeListValues.next();
		if (checkStringValue(codeListValue)) {
		    addValue(codeListValue);
		}
	    }
	}
    };

    public static final IndexedMetadataElement SPATIAL_REPRESENTATION_TYPE = new IndexedMetadataElement(
	    MetadataElement.SPATIAL_REPRESENTATION_TYPE) {
	@Override
	public void defineValues(GSResource resource) {

	    MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	    miMetadata.getDataIdentifications().forEachRemaining(dataId -> {

		String codeListValue = dataId.getSpatialRepresentationTypeCodeListValue();
		if (checkStringValue(codeListValue)) {
		    addValue(codeListValue);
		}
	    });
	}
    };

    public static final IndexedMetadataElement TITLE = new IndexedMetadataElement(MetadataElement.TITLE) {
	@Override
	public void defineValues(GSResource resource) {

	    for (Identification id : getIdentifications(resource)) {
		String title = id.getCitationTitle();
		if (checkStringValue(title)) {
		    addValue(title);
		}
	    }
	}
    };

    public static final IndexedMetadataElement PARENT_IDENTIFIER = new IndexedMetadataElement(MetadataElement.PARENT_IDENTIFIER) {
	@Override
	public void defineValues(GSResource resource) {

	    String identifier = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getParentIdentifier();
	    if (checkStringValue(identifier)) {

		addValue(identifier);
	    }
	}
    };

    public static final IndexedMetadataElement ABSTRACT = new IndexedMetadataElement(MetadataElement.ABSTRACT) {
	@Override
	public void defineValues(GSResource resource) {

	    for (Identification id : getIdentifications(resource)) {
		String abs = id.getAbstract();
		if (checkStringValue(abs)) {
		    addValue(abs);
		}
	    }
	}
    };

    public static final IndexedElement BOUNDING_BOX = new IndexedMetadataElement(MetadataElement.BOUNDING_BOX) {
	@Override
	public void defineValues(GSResource resource) {

	    SpatialIndexHelper.addBBoxes(resource, this);
	}
    };

    public static final IndexedElement TEMP_EXTENT_BEGIN_BEFORE_NOW = new IndexedMetadataElement(
	    MetadataElement.TEMP_EXTENT_BEGIN_BEFORE_NOW) {
	@Override
	public void defineValues(GSResource resource) {

	    for (Identification id : getIdentifications(resource)) {

		Iterator<TemporalExtent> extents = id.getTemporalExtents();

		while (extents.hasNext()) {

		    TemporalExtent te = extents.next();

		    if (te.isBeforeNowBeginPosition()) {

			String beginPosition = te.getBeforeNowBeginPosition().get().name();

			addValue(beginPosition);
		    }
		}
	    }
	}
    };

    public static final IndexedElement TEMP_EXTENT_BEGIN = new IndexedMetadataElement(MetadataElement.TEMP_EXTENT_BEGIN) {
	@Override
	public void defineValues(GSResource resource) {

	    List<Identification> dataIds = getIdentifications(resource);

	    if (dataIds.isEmpty()) {
		resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_BEGIN_NULL);
		return;
	    }

	    for (Identification id : dataIds) {

		Iterator<TemporalExtent> extents = id.getTemporalExtents();
		if (!extents.hasNext()) {
		    resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_BEGIN_NULL);
		    return;
		}

		while (extents.hasNext()) {

		    TemporalExtent te = extents.next();

		    if (te.isBeginPositionIndeterminate() && te.getIndeterminateBeginPosition() == TimeIndeterminateValueType.NOW) {

			resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_BEGIN_NOW);

		    } else if (!te.isBeforeNowBeginPosition()) {

			String beginPosition = te.getBeginPosition();

			if (checkStringValue(beginPosition)) {

			    if (beginPosition.equals("now")) {

				resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_BEGIN_NOW);

			    } else {

				addValue(beginPosition);
			    }
			} else {

			    resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_BEGIN_NULL);
			}
		    }
		}
	    }
	}
    };

    public static final IndexedElement TEMP_EXTENT_END = new IndexedMetadataElement(MetadataElement.TEMP_EXTENT_END) {
	@Override
	public void defineValues(GSResource resource) {

	    List<Identification> dataIds = getIdentifications(resource);
	    if (dataIds.isEmpty()) {
		resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_BEGIN_NULL);
		return;
	    }

	    for (Identification id : dataIds) {

		Iterator<TemporalExtent> extents = id.getTemporalExtents();
		if (!extents.hasNext()) {
		    resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_END_NULL);
		    return;
		}

		while (extents.hasNext()) {

		    TemporalExtent te = extents.next();

		    if (te.isEndPositionIndeterminate() && te.getIndeterminateEndPosition() == TimeIndeterminateValueType.NOW) {

			resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_END_NOW);

		    } else {

			String endPosition = te.getEndPosition();

			if (checkStringValue(endPosition)) {

			    if (endPosition.equals("now")) {

				resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_END_NOW);

			    } else {

				addValue(endPosition);
			    }
			} else {

			    resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_END_NULL);
			}
		    }
		}
	    }
	}
    };

    public static final IndexedElement ONLINE_LINKAGE = new IndexedMetadataElement(MetadataElement.ONLINE_LINKAGE) {
	@Override
	public void defineValues(GSResource resource) {

	    Distribution distribution = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution();

	    if (distribution == null) {
		return;
	    }

	    Iterator<Online> onlines = distribution.getDistributionOnlines();

	    while (onlines.hasNext()) {

		Online next = onlines.next();
		String linkage = next.getLinkage();

		if (checkStringValue(linkage)) {

		    addValue(linkage);
		}
	    }
	}
    };

    public static final IndexedElement ONLINE_ID = new IndexedMetadataElement(MetadataElement.ONLINE_ID) {
	@Override
	public void defineValues(GSResource resource) {

	    Distribution distribution = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution();

	    if (distribution == null) {
		return;
	    }

	    Iterator<Online> onlines = distribution.getDistributionOnlines();

	    while (onlines.hasNext()) {

		Online next = onlines.next();
		String id = next.getIdentifier();

		if (checkStringValue(id)) {

		    addValue(id);
		}
	    }
	}
    };

    public static final IndexedMetadataElement DISTRIBUTION_FORMAT = new IndexedMetadataElement(MetadataElement.DISTRIBUTION_FORMAT) {
	@Override
	public void defineValues(GSResource resource) {

	    Distribution distribution = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution();

	    if (distribution == null) {
		return;
	    }

	    Iterator<Format> formats = distribution.getFormats();
	    while (formats.hasNext()) {

		Format format = formats.next();
		String name = format.getName();

		if (checkStringValue(name)) {

		    addValue(name);
		}
	    }
	}
    };

    public static final IndexedMetadataElement ONLINE_PROTOCOL = new IndexedMetadataElement(MetadataElement.ONLINE_PROTOCOL) {
	@Override
	public void defineValues(GSResource resource) {

	    Distribution distribution = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution();

	    if (distribution == null) {
		return;
	    }

	    Iterator<Online> onlines = distribution.getDistributionOnlines();

	    while (onlines.hasNext()) {

		Online next = onlines.next();
		String protocol = next.getProtocol();

		if (checkStringValue(protocol)) {

		    addValue(protocol);
		}
	    }

	}
    };

    public static final IndexedMetadataElement LANGUAGE = new IndexedMetadataElement(MetadataElement.LANGUAGE) {
	@Override
	public void defineValues(GSResource resource) {

	    String language = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getLanguage();

	    if (checkStringValue(language)) {
		addValue(language);
	    }
	}
    };

    /**
     *
     */
    public static final IndexedMetadataElement CREATION_DATE = new IndexedMetadataElement(MetadataElement.CREATION_DATE) {
	@Override
	public void defineValues(GSResource resource) {

	    defineCodeListValues(this, resource, "creation");
	}
    };

    /**
     *
     */
    public static final IndexedMetadataElement PUBLICATION_DATE = new IndexedMetadataElement(MetadataElement.PUBLICATION_DATE) {
	@Override
	public void defineValues(GSResource resource) {

	    defineCodeListValues(this, resource, "publication");
	}
    };

    /**
     *
     */
    public static final IndexedElement REVISION_DATE = new IndexedMetadataElement(MetadataElement.REVISION_DATE) {
	@Override
	public void defineValues(GSResource resource) {

	    defineCodeListValues(this, resource, "revision");
	}
    };

    /**
     * @param el
     * @param resource
     * @param codeListValue
     */
    private static void defineCodeListValues(IndexedMetadataElement el, GSResource resource, String codeListValue) {

	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	List<Identification> identifications = miMetadata.getIdentifications();

	identifications.forEach(id -> {

	    String date = switch (codeListValue) {
		case "revision" -> id.getCitationRevisionDate();
		case "publication" -> id.getCitationPublicationDate();
		case "creation" -> id.getCitationCreationDate();
		default -> null;
	    };

	    if (checkStringValue(date)) {

		el.addValue(date);

	    } else {

		XMLGregorianCalendar dateTime = switch (codeListValue) {
		    case "revision" -> id.getCitationRevisionDateTime();
		    case "publication" -> id.getCitationPublicationDateTime();
		    case "creation" -> id.getCitationCreationDateTime();
		    default -> null;
		};

		if (dateTime != null) {

		    el.addValue(dateTime.toString());
		}
	    }
	});
    }

    public static final IndexedElement REFERENCE_DATE = new IndexedMetadataElement(MetadataElement.REFERENCE_DATE) {
	@Override
	public void defineValues(GSResource resource) {

	    for (Identification id : getIdentifications(resource)) {

		Iterator<String> dates = id.getCitationDates();
		while (dates.hasNext()) {
		    String date = dates.next();
		    if (checkStringValue(date)) {
			addValue(date);
		    }
		}
	    }
	}
    };

    public static final IndexedMetadataElement TOPIC_CATEGORY = new IndexedMetadataElement(MetadataElement.TOPIC_CATEGORY) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<DataIdentification> dataIdentifications = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getDataIdentifications();

	    while (dataIdentifications.hasNext()) {

		DataIdentification dataId = dataIdentifications.next();
		Iterator<String> topics = dataId.getTopicCategoriesStrings();

		while (topics.hasNext()) {
		    String topic = topics.next();
		    if (checkStringValue(topic)) {
			addValue(topic);
		    }
		}
	    }
	}
    };

    public static final IndexedMetadataElement RESOURCE_IDENTIFIER = new IndexedMetadataElement(MetadataElement.RESOURCE_IDENTIFIER) {
	@Override
	public void defineValues(GSResource resource) {

	    DataIdentification dataId = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification();

	    if (dataId != null) {

		String resourceIdentifier = dataId.getResourceIdentifier();
		if (checkStringValue(resourceIdentifier)) {
		    addValue(resourceIdentifier.toString());
		}
	    }
	}
    };

    public static final IndexedMetadataElement RESOURCE_LANGUAGE = new IndexedMetadataElement(MetadataElement.RESOURCE_LANGUAGE) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<DataIdentification> identifications = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getDataIdentifications();

	    while (identifications.hasNext()) {
		DataIdentification dataId = identifications.next();
		Iterator<String> languages = dataId.getLanguages();
		while (languages.hasNext()) {
		    String lan = languages.next();
		    if (checkStringValue(lan)) {
			addValue(lan);
		    }
		}
	    }
	}
    };

    public static final IndexedMetadataElement KEYWORD_URI_BLUE_CLOUD = new IndexedMetadataElement(MetadataElement.KEYWORD_URI_BLUE_CLOUD) {
	@Override
	public void defineValues(GSResource resource) {

	    addKeywordsURI(resource, null, new String[] { "platform", "platform_class", "parameter", "instrument", "cruise", "project" });

	}
    };

    public static final IndexedMetadataElement KEYWORD_BLUE_CLOUD = new IndexedMetadataElement(MetadataElement.KEYWORD_BLUE_CLOUD) {
	@Override
	public void defineValues(GSResource resource) {

	    addKeywords(resource, null, new String[] { "platform", "platform_class", "parameter", "instrument", "cruise", "project" });

	}
    };

    public static final IndexedMetadataElement KEYWORD = new IndexedMetadataElement(MetadataElement.KEYWORD) {
	@Override
	public void defineValues(GSResource resource) {

	    addKeywords(resource, null);

	}
    };

    public static final IndexedMetadataElement KEYWORD_URI = new IndexedMetadataElement(MetadataElement.KEYWORD_URI) {
	@Override
	public void defineValues(GSResource resource) {

	    addKeywordsURI(resource, null);
	}
    };

    public static final IndexedMetadataElement AUTHOR = new IndexedMetadataElement(MetadataElement.AUTHOR) {
	@Override
	public void defineValues(GSResource resource) {

	    for (Identification dataId : getIdentifications(resource)) {

		ResponsibleParty party = dataId.getPointOfContact("author");
		if (party != null) {
		    String individualName = party.getIndividualName();
		    if (checkStringValue(individualName)) {
			addValue(individualName);
		    }
		}
	    }
	}
    };

    /**
     *
     */
    public static final IndexedMetadataElement OWNER_ORGANISATION_NAME = new IndexedMetadataElement(//
	    MetadataElement.OWNER_ORGANISATION_NAME) {
	@Override
	public void defineValues(GSResource resource) {

	    getPartyNamesByRoleCode(resource, "owner").forEach(this::addValue);
	}
    };

    /**
     *
     */
    public static final IndexedMetadataElement DIST_ORGANISATION_NAME = new IndexedMetadataElement(//
	    MetadataElement.DISTRIBUTOR_ORGANISATION_NAME) {
	@Override
	public void defineValues(GSResource resource) {

	    getPartyNamesByRoleCode(resource, "distributor").forEach(this::addValue);
	}
    };

    /**
     * @param resource
     * @param code
     * @return
     */
    private static List<String> getPartyNamesByRoleCode(GSResource resource, String code) {

	List<ResponsibleParty> parties = getAllParties(resource);

	ArrayList<String> out = new ArrayList<>();

	for (ResponsibleParty party : parties) {

	    if (party != null) {
		String roleCode = party.getRoleCode();
		if (roleCode != null && roleCode.equals(code)) {
		    String orgName = party.getOrganisationName();
		    if (checkStringValue(orgName)) {
			out.add(orgName);
		    }
		}
	    }
	}

	return out;
    }

    public static final IndexedMetadataElement ORGANISATION_NAME = new IndexedMetadataElement(MetadataElement.ORGANISATION_NAME) {
	@Override
	public void defineValues(GSResource resource) {

	    List<ResponsibleParty> parties = getAllParties(resource);

	    for (ResponsibleParty party : parties) {

		if (party != null) {
		    String orgName = party.getOrganisationName();
		    if (checkStringValue(orgName)) {
			addValue(orgName);
		    }
		}
	    }

	    defineBNHSProperty(BNHSProperty.INSTITUTE, resource);
	}
    };

    public static final IndexedMetadataElement ORGANISATION_ROLE = new IndexedMetadataElement(MetadataElement.ORGANISATION_ROLE) {
	@Override
	public void defineValues(GSResource resource) {

	    List<ResponsibleParty> parties = getAllParties(resource);

	    for (ResponsibleParty party : parties) {
		if (party != null) {

		    String role = party.getRoleCode();

		    if (checkStringValue(role)) {
			addValue(role);
		    }
		}
	    }
	}
    };

    public static final IndexedMetadataElement ORGANISATION_URI = new IndexedMetadataElement(MetadataElement.ORGANISATION_URI) {
	@Override
	public void defineValues(GSResource resource) {

	    List<ResponsibleParty> parties = getAllParties(resource);

	    for (ResponsibleParty party : parties) {
		if (party != null) {
		    String orgURI = party.getOrganisationURI();
		    if (checkStringValue(orgURI)) {
			addValue(orgURI);
		    }
		}
	    }
	}
    };

    /**
     * @param resource
     * @return
     */
    private static List<ResponsibleParty> getAllParties(GSResource resource) {

	List<ResponsibleParty> parties = new ArrayList<ResponsibleParty>();

	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	Iterator<ResponsibleParty> contacts = miMetadata.getContacts();//

	while (contacts != null && contacts.hasNext()) {

	    ResponsibleParty responsibleParty = contacts.next();

	    parties.add(responsibleParty);
	}

	List<Identification> identifications = miMetadata.getIdentifications();

	identifications.forEach(id -> {

	    parties.addAll(id.getCitedParty());

	    id.getPointOfContacts().forEachRemaining(parties::add);
	});

	parties.addAll(miMetadata.getDistribution().getDistributorParties());

	return parties;
    }

    public static final IndexedMetadataElement ORIGINATOR_ORGANISATION_IDENTIFIER = new IndexedMetadataElement(
	    MetadataElement.ORIGINATOR_ORGANISATION_IDENTIFIER) {
	@Override
	public void defineValues(GSResource resource) {

	    ExtensionHandler handler = resource.getExtensionHandler();

	    List<String> identifiers = handler.getOriginatorOrganisationIdentifiers();

	    for (String identifier : identifiers) {

		if (checkStringValue(identifier)) {
		    addValue(identifier);
		}
	    }
	}
    };
    public static final IndexedMetadataElement ORIGINATOR_ORGANISATION_DESCRIPTION = new IndexedMetadataElement(
	    MetadataElement.ORIGINATOR_ORGANISATION_DESCRIPTION) {
	@Override
	public void defineValues(GSResource resource) {

	    HashSet<String> names = new HashSet<>();

	    List<ResponsibleParty> originators = resource.getHarmonizedMetadata(). //
		    getCoreMetadata().//
		    getDataIdentification().//
		    getOriginatorParty();//

	    for (ResponsibleParty originator : originators) {

		String orgName = originator.getOrganisationName();

		if (checkStringValue(orgName)) {
		    names.add(orgName);
		}
	    }

	    ExtensionHandler handler = resource.getExtensionHandler();

	    List<String> descriptions = handler.getOriginatorOrganisationDescriptions();

	    for (String description : descriptions) {
		if (checkStringValue(description)) {
		    names.add(description);
		}
	    }

	    for (String name : names) {
		addValue(name);
	    }
	}
    };
    public static final IndexedMetadataElement TEAM_CATEGORY = new IndexedMetadataElement(MetadataElement.THEME_CATEGORY) {
	@Override
	public void defineValues(GSResource resource) {

	    ExtensionHandler handler = resource.getExtensionHandler();
	    Optional<String> themeCategoryOpt = handler.getThemeCategory();
	    if (themeCategoryOpt.isPresent()) {
		if (checkStringValue(themeCategoryOpt.get())) {
		    addValue(themeCategoryOpt.get());
		}
	    }
	}
    };
    public static final IndexedMetadataElement CRS_ID = new IndexedMetadataElement(MetadataElement.CRS_ID) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<ReferenceSystem> infos = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getReferenceSystemInfos();

	    while (infos.hasNext()) {
		ReferenceSystem refSys = infos.next();
		String code = refSys.getCode();
		if (checkStringValue(code)) {
		    addValue(code);
		}
	    }
	}
    };
    public static final IndexedMetadataElement CRS_VERSION = new IndexedMetadataElement(MetadataElement.CRS_VERSION) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<ReferenceSystem> infos = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getReferenceSystemInfos();

	    while (infos.hasNext()) {
		ReferenceSystem refSys = infos.next();
		String version = refSys.getVersion();
		if (checkStringValue(version)) {
		    addValue(version);
		}
	    }
	}
    };
    public static final IndexedMetadataElement CRS_AUTHORITY = new IndexedMetadataElement(MetadataElement.CRS_AUTHORITY) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<ReferenceSystem> infos = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getReferenceSystemInfos();

	    while (infos.hasNext()) {
		ReferenceSystem refSys = infos.next();
		String authority = refSys.getCodeSpace();
		if (checkStringValue(authority)) {
		    addValue(authority);
		}
	    }
	}
    };

    public static final IndexedMetadataElement GEOGRAPHIC_DESCRIPTION_CODE = new IndexedMetadataElement(
	    MetadataElement.GEOGRAPHIC_DESCRIPTION_CODE) {
	@Override
	public void defineValues(GSResource resource) {

	    for (Identification dataId : getIdentifications(resource)) {

		Iterator<String> codes = dataId.getGeographicDescriptionCodes();
		while (codes.hasNext()) {
		    String code = codes.next();
		    if (checkStringValue(code)) {
			addValue(code);
		    }
		}
	    }
	}
    };

    public static final IndexedMetadataElement ALTERNATE_TITLE = new IndexedMetadataElement(MetadataElement.ALTERNATE_TITLE) {
	@Override
	public void defineValues(GSResource resource) {

	    for (Identification id : getIdentifications(resource)) {

		String alternateTitle = id.getCitationAlternateTitle();
		if (checkStringValue(alternateTitle)) {
		    addValue(alternateTitle);
		}
	    }
	}
    };

    public static final IndexedMetadataElement INSTRUMENT_IDENTIFIER = new IndexedMetadataElement(MetadataElement.INSTRUMENT_IDENTIFIER) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<MIInstrument> miInstruments = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIInstruments();

	    while (miInstruments.hasNext()) {
		MIInstrument next = miInstruments.next();
		String identifier = next.getMDIdentifierCode();
		if (checkStringValue(identifier)) {
		    addValue(identifier);
		}
	    }
	}
    };

    public static final IndexedMetadataElement UNIQUE_INSTRUMENT_IDENTIFIER = new IndexedMetadataElement(
	    MetadataElement.UNIQUE_INSTRUMENT_IDENTIFIER) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> optional = resource.getExtensionHandler().getUniqueInstrumentIdentifier();
	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    addValue(value);
		}
	    }
	}
    };

    public static final IndexedMetadataElement INSTRUMENT_DESCRIPTION = new IndexedMetadataElement(MetadataElement.INSTRUMENT_DESCRIPTION) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<MIInstrument> miInstruments = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIInstruments();

	    while (miInstruments.hasNext()) {
		MIInstrument next = miInstruments.next();
		String description = next.getDescription();
		if (checkStringValue(description)) {
		    addValue(description);
		}
	    }
	}
    };

    public static final IndexedMetadataElement INSTRUMENT_TITLE = new IndexedMetadataElement(MetadataElement.INSTRUMENT_TITLE) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<MIInstrument> miInstruments = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIInstruments();

	    while (miInstruments.hasNext()) {
		MIInstrument next = miInstruments.next();
		String title = next.getTitle();
		if (checkStringValue(title)) {
		    addValue(title);
		}
	    }

	    defineBNHSProperty(BNHSProperty.EQUIPMENT, resource);
	}
    };

    public static final IndexedMetadataElement INSTRUMENT_URI = new IndexedMetadataElement(MetadataElement.INSTRUMENT_URI) {
	@Override
	public void defineValues(GSResource resource) {

	    addKeywordsURI(resource, "instrument");
	}
    };

    public static final IndexedMetadataElement CRUISE_NAME = new IndexedMetadataElement(MetadataElement.CRUISE_NAME) {
	@Override
	public void defineValues(GSResource resource) {

	    addKeywords(resource, "cruise");
	}
    };

    public static final IndexedMetadataElement CRUISE_URI = new IndexedMetadataElement(MetadataElement.CRUISE_URI) {
	@Override
	public void defineValues(GSResource resource) {

	    addKeywordsURI(resource, "cruise");
	}
    };

    public static final IndexedMetadataElement PROJECT_NAME = new IndexedMetadataElement(MetadataElement.PROJECT_NAME) {
	@Override
	public void defineValues(GSResource resource) {

	    addKeywords(resource, "project");
	}
    };

    public static final IndexedMetadataElement PROJECT_URI = new IndexedMetadataElement(MetadataElement.PROJECT_URI) {
	@Override
	public void defineValues(GSResource resource) {

	    addKeywordsURI(resource, "project");
	}
    };

    public static final IndexedMetadataElement PLATFORM_IDENTIFIER = new IndexedMetadataElement(MetadataElement.PLATFORM_IDENTIFIER) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<MIPlatform> miPlatforms = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatforms();

	    while (miPlatforms.hasNext()) {
		MIPlatform next = miPlatforms.next();
		String identifier = next.getMDIdentifierCode();
		if (checkStringValue(identifier)) {
		    addValue(identifier);
		}
	    }
	}
    };
    public static final IndexedMetadataElement PLATFORM_URI = new IndexedMetadataElement(MetadataElement.PLATFORM_URI) {
	@Override
	public void defineValues(GSResource resource) {

	    addKeywordsURI(resource, "platform");
	    addKeywordsURI(resource, "platform_class");

	}
    };
    public static final IndexedMetadataElement PLATFORM_TITLE = new IndexedMetadataElement(MetadataElement.PLATFORM_TITLE) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<MIPlatform> miPlatforms = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatforms();

	    while (miPlatforms.hasNext()) {
		MIPlatform next = miPlatforms.next();
		Citation citation = next.getCitation();
		if (citation != null) {
		    String title = citation.getTitle();
		    if (checkStringValue(title)) {
			addValue(title);
		    }
		}
	    }

	    defineBNHSProperty(BNHSProperty.STATION_NAME, resource);
	}
    };

    public static final IndexedMetadataElement UNIQUE_PLATFORM_IDENTIFIER = new IndexedMetadataElement(
	    MetadataElement.UNIQUE_PLATFORM_IDENTIFIER) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> optional = resource.getExtensionHandler().getUniquePlatformIdentifier();
	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    addValue(value);
		}
	    }
	}
    };

    public static final IndexedMetadataElement DATA_DISCLAIMER = new IndexedMetadataElement(MetadataElement.DATA_DISCLAIMER) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> optional = resource.getExtensionHandler().getDataDisclaimer();
	    if (optional.isPresent()) {

		String value = optional.get();
		if (value != null) {
		    addValue("" + value);
		}
	    }
	}
    };

    public static final IndexedMetadataElement DATA_SIZE = new IndexedMetadataElement(MetadataElement.DATA_SIZE) {
	@Override
	public void defineValues(GSResource resource) {

	    try {
		Optional<Long> optional = resource.getExtensionHandler().getDataSize();
		if (optional.isPresent()) {

		    Long value = optional.get();
		    if (value != null) {
			addValue("" + value);
		    }
		} else {
		    GridSpatialRepresentation spatialRepresentation = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
			    .getGridSpatialRepresentation();
		    if (spatialRepresentation != null) {
			Iterator<Dimension> iterator = spatialRepresentation.getAxisDimensions();
			Long dataSize = 1l;
			while (iterator.hasNext()) {
			    Dimension dimension = (Dimension) iterator.next();
			    BigInteger size = dimension.getDimensionSize();
			    if (size != null) {
				dataSize = dataSize * size.longValue();
			    }
			}
			addValue("" + dataSize);
		    }
		}
	    } catch (Exception ex) {
		GSLoggerFactory.getLogger(getClass()).error(ex);
	    }
	}
    };

    public static final IndexedMetadataElement PLATFORM_DESCRIPTION = new IndexedMetadataElement(MetadataElement.PLATFORM_DESCRIPTION) {
	@Override
	public void defineValues(GSResource resource) {

	    MIPlatform miPlatform = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform();
	    if (miPlatform != null) {
		String description = miPlatform.getDescription();
		if (checkStringValue(description)) {
		    addValue(description);
		}
	    }
	}
    };

    public static final IndexedMetadataElement ATTRIBUTE_IDENTIFIER = new IndexedMetadataElement(MetadataElement.ATTRIBUTE_IDENTIFIER) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<CoverageDescription> descriptions = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getCoverageDescriptions();

	    while (descriptions.hasNext()) {
		CoverageDescription next = descriptions.next();
		String description = next.getAttributeIdentifier();
		if (checkStringValue(description)) {
		    addValue(description);
		}
	    }

	    addKeywordsURI(resource, "theme");
	    addKeywordsURI(resource, "parameter"); // even if it is outside of ISO 19115..

	}
    };

    public static final IndexedMetadataElement ATTRIBUTE_TITLE = new IndexedMetadataElement(MetadataElement.ATTRIBUTE_TITLE) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<CoverageDescription> descriptions = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getCoverageDescriptions();

	    while (descriptions.hasNext()) {
		CoverageDescription next = descriptions.next();
		String description = next.getAttributeTitle();
		if (checkStringValue(description)) {
		    addValue(description);
		}
	    }

	    addKeywords(resource, "theme");
	    addKeywords(resource, "parameter"); // even if it is outside of ISO 19115..

	}

    };

    public static final IndexedMetadataElement OBSERVED_PROPERTY_URI = new IndexedMetadataElement(MetadataElement.OBSERVED_PROPERTY_URI) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> optional = resource.getExtensionHandler().getObservedPropertyURI();

	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    addValue(value);
		}
	    }
	}
    };

    public static final IndexedMetadataElement TIME_AGGREGATION_DURATION_8601 = new IndexedMetadataElement(
	    MetadataElement.TIME_AGGREGATION_DURATION_8601) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> value = resource.getExtensionHandler().getTimeAggregationDuration8601();
	    if (value.isPresent()) {
		String duration = value.get();
		if (duration != null) {
		    if (checkStringValue(duration.toString())) {
			addValue(duration.toString());
		    }
		}
	    }

	}
    };

    public static final IndexedMetadataElement TIME_RESOLUTION_DURATION_8601 = new IndexedMetadataElement(
	    MetadataElement.TIME_RESOLUTION_DURATION_8601) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> value = resource.getExtensionHandler().getTimeResolutionDuration8601();
	    if (value.isPresent()) {
		String resolution = value.get();
		if (resolution != null) {
		    if (checkStringValue(resolution.toString())) {
			addValue(resolution.toString());
		    }
		}
	    }

	}
    };

    public static final IndexedMetadataElement UNIQUE_ATTRIBUTE_IDENTIFIER = new IndexedMetadataElement(
	    MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> optional = resource.getExtensionHandler().getUniqueAttributeIdentifier();
	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    addValue(value);
		}
	    }
	}
    };

    public static final IndexedMetadataElement WIS_TOPIC_HIERARCHY = new IndexedMetadataElement(MetadataElement.WIS_TOPIC_HIERARCHY) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> optional = resource.getExtensionHandler().getWISTopicHierarchy();

	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    addValue(value);
		}
	    }
	}
    };

    public static final IndexedMetadataElement TIME_INTERPOLATION = new IndexedMetadataElement(MetadataElement.TIME_INTERPOLATION) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> optional = resource.getExtensionHandler().getTimeInterpolationString();

	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    addValue(value);
		}
	    }
	}
    };

    public static final IndexedMetadataElement TIME_SUPPORT = new IndexedMetadataElement(MetadataElement.TIME_SUPPORT) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> optional = resource.getExtensionHandler().getTimeSupport();

	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    addValue(value);
		}
	    }
	}
    };

    public static final IndexedMetadataElement TIME_RESOLUTION = new IndexedMetadataElement(MetadataElement.TIME_RESOLUTION) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> optional = resource.getExtensionHandler().getTimeResolution();

	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    addValue(value);
		}
	    }
	}
    };

    public static final IndexedMetadataElement TIME_UNITS = new IndexedMetadataElement(MetadataElement.TIME_UNITS) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> optional = resource.getExtensionHandler().getTimeUnits();

	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    addValue(value);
		}
	    }
	}
    };

    public static final IndexedMetadataElement ATTRIBUTE_UNITS_URI = new IndexedMetadataElement(MetadataElement.ATTRIBUTE_UNITS_URI) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> optional = resource.getExtensionHandler().getAttributeUnitsURI();

	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    addValue(value);
		}
	    }
	}
    };

    public static final IndexedMetadataElement ATTRIBUTE_MISSING_VALUE = new IndexedMetadataElement(
	    MetadataElement.ATTRIBUTE_MISSING_VALUE) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> optional = resource.getExtensionHandler().getAttributeMissingValue();

	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    addValue(value);
		}
	    }
	}
    };

    public static final IndexedMetadataElement ATTRIBUTE_UNITS_ABBREVIATION = new IndexedMetadataElement(
	    MetadataElement.ATTRIBUTE_UNITS_ABBREVIATION) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> optional = resource.getExtensionHandler().getAttributeUnitsAbbreviation();

	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    addValue(value);
		}
	    }
	}
    };

    public static final IndexedMetadataElement ATTRIBUTE_UNITS = new IndexedMetadataElement(MetadataElement.ATTRIBUTE_UNITS) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> optional = resource.getExtensionHandler().getAttributeUnits();

	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    addValue(value);
		}
	    }
	}
    };

    public static final IndexedMetadataElement TIME_UNITS_ABBREVIATION = new IndexedMetadataElement(
	    MetadataElement.TIME_UNITS_ABBREVIATION) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> optional = resource.getExtensionHandler().getTimeUnitsAbbreviation();

	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    addValue(value);
		}
	    }
	}
    };

    public static final IndexedMetadataElement ATTRIBUTE_DESCRIPTION = new IndexedMetadataElement(MetadataElement.ATTRIBUTE_DESCRIPTION) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<CoverageDescription> descriptions = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getCoverageDescriptions();

	    while (descriptions.hasNext()) {
		CoverageDescription next = descriptions.next();
		String description = next.getAttributeDescription();
		if (checkStringValue(description)) {
		    addValue(description);
		}
	    }
	}
    };

    public static final IndexedMetadataElement KEYWORD_TYPE = new IndexedMetadataElement(MetadataElement.KEYWORD_TYPE) {
	@Override
	public void defineValues(GSResource resource) {

	    DataIdentification dataIdentification = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getDataIdentification();
	    if (dataIdentification == null) {
		return;
	    }

	    Iterator<String> keywordTypes = dataIdentification.getKeywordTypes();

	    while (keywordTypes.hasNext()) {
		String kwdType = keywordTypes.next();
		if (checkStringValue(kwdType)) {
		    addValue(kwdType);
		}
	    }
	}
    };

    public static final IndexedMetadataElement AGGREGATED_RESOURCE_IDENTIFIER = new IndexedMetadataElement(
	    MetadataElement.AGGREGATED_RESOURCE_IDENTIFIER) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<String> identifiers = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getAggregatedResourcesIdentifiers();

	    while (identifiers.hasNext()) {
		String title = identifiers.next();
		if (checkStringValue(title)) {
		    addValue(title);
		}
	    }
	}
    };

    public static final IndexedElement HAS_ACCESS_LEGAL_CONSTRAINTS = new IndexedMetadataElement(
	    MetadataElement.HAS_ACCESS_LEGAL_CONSTRAINTS) {
	@Override
	public void defineValues(GSResource resource) {

	    boolean match = resource.getHarmonizedMetadata(). //
		    getCoreMetadata().//
		    getMIMetadata().//
		    getIdentifications().//
		    stream().//
		    anyMatch(Identification::hasAccessLegalConstraints);

	    addValue(String.valueOf(match));
	}
    };

    public static final IndexedElement HAS_OTHER_LEGAL_CONSTRAINTS = new IndexedMetadataElement(
	    MetadataElement.HAS_OTHER_LEGAL_CONSTRAINTS) {
	@Override
	public void defineValues(GSResource resource) {

	    boolean match = resource.getHarmonizedMetadata(). //
		    getCoreMetadata().//
		    getMIMetadata().//
		    getIdentifications().//
		    stream().//
		    anyMatch(Identification::hasOtherLegalConstraints);

	    addValue(String.valueOf(match));
	}
    };

    public static final IndexedElement HAS_SECURITY_CONSTRAINTS = new IndexedMetadataElement(MetadataElement.HAS_SECURITY_CONSTRAINTS) {
	@Override
	public void defineValues(GSResource resource) {

	    boolean match = resource.getHarmonizedMetadata(). //
		    getCoreMetadata().//
		    getMIMetadata().//
		    getIdentifications().//
		    stream().//
		    anyMatch(Identification::hasSecurityConstraints);

	    addValue(String.valueOf(match));
	}
    };
    public static final IndexedElement HAS_USE_LEGAL_CONSTRAINTS = new IndexedMetadataElement(MetadataElement.HAS_USE_LEGAL_CONSTRAINTS) {
	@Override
	public void defineValues(GSResource resource) {

	    boolean match = resource.getHarmonizedMetadata(). //
		    getCoreMetadata().//
		    getMIMetadata().//
		    getIdentifications().//
		    stream().//
		    anyMatch(Identification::hasUseLegalConstraints);

	    addValue(String.valueOf(match));
	}
    };
    public static final IndexedElement USE_LEGAL_CONSTRAINTS = new IndexedMetadataElement(MetadataElement.USE_LEGAL_CONSTRAINTS) {
	@Override
	public void defineValues(GSResource resource) {

	    List<Identification> iterator = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getIdentifications();

	    for (Identification id : iterator) {

		Iterator<LegalConstraints> constraints = id.getLegalConstraints();
		while (constraints.hasNext()) {
		    LegalConstraints legalConstraints = constraints.next();
		    List<CharacterStringPropertyType> others = legalConstraints.getElementType().getOtherConstraints();
		    for (CharacterStringPropertyType cspt : others) {
			String text = DataIdentification.getStringFromCharacterString(cspt);
			if (text != null && !text.isEmpty()) {
			    addValue(text);
			}
		    }
		}
	    }
	}
    };

    public static final IndexedElement USE_LEGAL_CONSTRAINTS_URI = new IndexedMetadataElement(MetadataElement.USE_LEGAL_CONSTRAINTS_URI) {
	@Override
	public void defineValues(GSResource resource) {

	    List<Identification> iterator = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getIdentifications();

	    for (Identification id : iterator) {

		Iterator<LegalConstraints> constraints = id.getLegalConstraints();
		while (constraints.hasNext()) {
		    LegalConstraints legalConstraints = (LegalConstraints) constraints.next();
		    List<CharacterStringPropertyType> others = legalConstraints.getElementType().getOtherConstraints();
		    for (CharacterStringPropertyType cspt : others) {
			String text = DataIdentification.getHREFStringFromCharacterString(cspt);
			if (text != null && !text.isEmpty()) {
			    addValue(String.valueOf(text));
			}
		    }
		}
	    }
	}
    };

    public static final IndexedElement DISTANCE_VALUE = new IndexedMetadataElement(MetadataElement.DISTANCE_VALUE) {
	@Override
	public void defineValues(GSResource resource) {

	    try {
		Iterator<Double> values = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
			.getDistanceValues();
		while (values.hasNext()) {
		    double value = values.next();
		    addValue(String.valueOf(value));
		}
	    } catch (NullPointerException e) {
	    }

	}
    };

    public static final IndexedElement DENOMINATOR = new IndexedMetadataElement(MetadataElement.DENOMINATOR) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<DataIdentification> di = resource.//
		    getHarmonizedMetadata().//
		    getCoreMetadata().//
		    getMIMetadata().//
		    getDataIdentifications();

	    while (di.hasNext()) {
		DataIdentification next = di.next();
		Iterator<Integer> denominators = next.getDenominators();
		while (denominators.hasNext()) {
		    Integer den = denominators.next();
		    addValue(String.valueOf(den));
		}
	    }
	}
    };

    // -------------------------------------------------------------------------------------------------
    //
    // SENTINEL SPECIFIC INDEXES (available in SatelliteScene except CLOUD_COVER_PERC in the MIMetadata)
    //
    //
    public static final IndexedMetadataElement PRODUCT_TYPE = new IndexedMetadataElement(MetadataElement.PRODUCT_TYPE) {
	@Override
	public void defineValues(GSResource resource) {
	    Optional<SatelliteScene> optional = resource.getExtensionHandler().getSatelliteScene();
	    if (optional.isPresent()) {
		SatelliteScene scene = optional.get();
		String prodType = scene.getProductType();
		if (checkStringValue(prodType)) {
		    addValue(prodType);
		}
	    }
	}
    };
    public static final IndexedMetadataElement SENSOR_OP_MODE = new IndexedMetadataElement(MetadataElement.SENSOR_OP_MODE) {
	@Override
	public void defineValues(GSResource resource) {
	    Optional<SatelliteScene> optional = resource.getExtensionHandler().getSatelliteScene();
	    if (optional.isPresent()) {
		SatelliteScene scene = optional.get();
		String sensorOpMode = scene.getSensorOpMode();
		if (checkStringValue(sensorOpMode)) {
		    addValue(sensorOpMode);
		}
	    }
	}
    };
    public static final IndexedMetadataElement SENSOR_SWATH = new IndexedMetadataElement(MetadataElement.SENSOR_SWATH) {
	@Override
	public void defineValues(GSResource resource) {
	    Optional<SatelliteScene> optional = resource.getExtensionHandler().getSatelliteScene();
	    if (optional.isPresent()) {
		SatelliteScene scene = optional.get();
		String sensorSwath = scene.getSensorSwath();
		if (checkStringValue(sensorSwath)) {
		    addValue(sensorSwath);
		}
	    }
	}
    };
    public static final IndexedMetadataElement S3_INSTRUMENT_IDX = new IndexedMetadataElement(MetadataElement.S3_INSTRUMENT_IDX) {
	@Override
	public void defineValues(GSResource resource) {
	    Optional<SatelliteScene> optional = resource.getExtensionHandler().getSatelliteScene();
	    if (optional.isPresent()) {
		SatelliteScene scene = optional.get();
		String s3InstrumentIdx = scene.getS3InstrumentIdx();
		if (checkStringValue(s3InstrumentIdx)) {
		    addValue(s3InstrumentIdx);
		}
	    }
	}
    };
    public static final IndexedMetadataElement S3_PRODUCT_LEVEL = new IndexedMetadataElement(MetadataElement.S3_PRODUCT_LEVEL) {
	@Override
	public void defineValues(GSResource resource) {
	    Optional<SatelliteScene> optional = resource.getExtensionHandler().getSatelliteScene();
	    if (optional.isPresent()) {
		SatelliteScene scene = optional.get();
		String s3ProductLevel = scene.getS3ProductLevel();
		if (checkStringValue(s3ProductLevel)) {
		    addValue(s3ProductLevel);
		}
	    }
	}
    };
    public static final IndexedMetadataElement S3_TIMELINESS = new IndexedMetadataElement(MetadataElement.S3_TIMELINESS) {
	@Override
	public void defineValues(GSResource resource) {
	    Optional<SatelliteScene> optional = resource.getExtensionHandler().getSatelliteScene();
	    if (optional.isPresent()) {
		SatelliteScene scene = optional.get();
		String s3Timeless = scene.getS3Timeliness();
		if (checkStringValue(s3Timeless)) {
		    addValue(s3Timeless);
		}
	    }
	}
    };
    public static final IndexedMetadataElement SAR_POL_CH = new IndexedMetadataElement(MetadataElement.SAR_POL_CH) {
	@Override
	public void defineValues(GSResource resource) {
	    Optional<SatelliteScene> optional = resource.getExtensionHandler().getSatelliteScene();
	    if (optional.isPresent()) {
		SatelliteScene scene = optional.get();
		String sarPolCh = scene.getSarPolCh();
		if (checkStringValue(sarPolCh)) {
		    addValue(sarPolCh);
		}
	    }
	}
    };
    public static final IndexedMetadataElement RELATIVE_ORBIT = new IndexedMetadataElement(MetadataElement.RELATIVE_ORBIT) {
	@Override
	public void defineValues(GSResource resource) {
	    Optional<SatelliteScene> optional = resource.getExtensionHandler().getSatelliteScene();
	    if (optional.isPresent()) {
		SatelliteScene scene = optional.get();
		Integer orbit = scene.getRelativeOrbit();
		if (orbit != null) {
		    addValue(orbit.toString());
		}
	    }
	}
    };
    public static final IndexedMetadataElement ROW = new IndexedMetadataElement(MetadataElement.ROW) {
	@Override
	public void defineValues(GSResource resource) {
	    Optional<SatelliteScene> optional = resource.getExtensionHandler().getSatelliteScene();
	    if (optional.isPresent()) {
		SatelliteScene scene = optional.get();
		Integer row = scene.getRow();
		if (row != null) {
		    addValue(row.toString());
		}
	    }
	}
    };
    public static final IndexedMetadataElement PATH = new IndexedMetadataElement(MetadataElement.PATH) {
	@Override
	public void defineValues(GSResource resource) {
	    Optional<SatelliteScene> optional = resource.getExtensionHandler().getSatelliteScene();
	    if (optional.isPresent()) {
		SatelliteScene scene = optional.get();
		Integer path = scene.getPath();
		if (path != null) {
		    addValue(path.toString());
		}
	    }
	}
    };
    public static final IndexedElement CLOUD_COVER_PERC = new IndexedMetadataElement(MetadataElement.CLOUD_COVER_PERC) {
	@Override
	public void defineValues(GSResource resource) {

	    MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	    List<Double> list = miMetadata.getCloudCoverPercentageList();
	    for (Double value : list) {
		if (value != null) {
		    addValue(value.toString());
		}
	    }
	}
    };

    public static final IndexedMetadataElement HYCOS_IDENTIFIER = new IndexedMetadataElement(MetadataElement.HYCOS_IDENTIFIER) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.HYCOSID, resource);
	}
    };

    public static final IndexedMetadataElement STATION_IDENTIFIER = new IndexedMetadataElement(MetadataElement.STATION_IDENTIFIER) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.STATION_ID, resource);
	}
    };

    public static final IndexedMetadataElement COUNTRY = new IndexedMetadataElement(MetadataElement.COUNTRY) {
	@Override
	public void defineValues(GSResource resource) {
	    Optional<String> optional = resource.getExtensionHandler().getCountry();
	    if (optional.isPresent()) {
		addValue(optional.get());
	    }
	    defineBNHSProperty(BNHSProperty.COUNTRY, resource);
	}
    };

    public static final IndexedMetadataElement COUNTRY_ISO3 = new IndexedMetadataElement(MetadataElement.COUNTRY_ISO3) {
	@Override
	public void defineValues(GSResource resource) {
	    Optional<String> optional = resource.getExtensionHandler().getCountryISO3();
	    if (optional.isPresent()) {
		addValue(optional.get());
	    }
	}
    };

    public static final IndexedMetadataElement RIVER = new IndexedMetadataElement(MetadataElement.RIVER) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.RIVER, resource);

	    Optional<String> optional = resource.getExtensionHandler().getRiver();
	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    addValue(value);
		}
	    }

	}
    };

    public static final IndexedMetadataElement RIVER_BASIN = new IndexedMetadataElement(MetadataElement.RIVER_BASIN) {
	@Override
	public void defineValues(GSResource resource) {
	    Optional<String> optional = resource.getExtensionHandler().getRiverBasin();
	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    addValue(value);
		}
	    }

	}
    };

    public static final IndexedMetadataElement LAKE_STATION = new IndexedMetadataElement(MetadataElement.LAKE_STATION) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.LAKE_STATION, resource);
	}
    };

    public static final IndexedMetadataElement GRDC_ID = new IndexedMetadataElement(MetadataElement.GRDC_ID) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.GRDC_ID, resource);
	}
    };
    public static final IndexedMetadataElement GRDC_ARDB = new IndexedMetadataElement(MetadataElement.GRDC_ARDB) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.GRDC_ARDB, resource);
	}
    };
    public static final IndexedMetadataElement WMO_REGION = new IndexedMetadataElement(MetadataElement.WMO_REGION) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.WMO_REGION, resource);
	}
    };
    public static final IndexedMetadataElement LATITUDE = new IndexedMetadataElement(MetadataElement.LATITUDE) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.LATITUDE, resource);
	}
    };
    public static final IndexedMetadataElement LONGITUDE = new IndexedMetadataElement(MetadataElement.LONGITUDE) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.LONGITUDE, resource);
	}
    };
    public static final IndexedMetadataElement LATITUDE_OF_DISCHARGE = new IndexedMetadataElement(MetadataElement.LATITUDE_OF_DISCHARGE) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.LATITUDE_OF_DISCHARGE, resource);
	}
    };
    public static final IndexedMetadataElement LONGITUDE_OF_DISCHARGE = new IndexedMetadataElement(MetadataElement.LONGITUDE_OF_DISCHARGE) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.LONGITUDE_OF_DISCHARGE, resource);
	}
    };
    public static final IndexedMetadataElement STATUS = new IndexedMetadataElement(MetadataElement.STATUS) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.STATUS, resource);
	}
    };
    public static final IndexedMetadataElement DRAINAGE_AREA = new IndexedMetadataElement(MetadataElement.DRAINAGE_AREA) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.DRAINAGE_AREA, resource);
	}
    };
    public static final IndexedMetadataElement EFFECTIVE_DRAINAGE_AREA = new IndexedMetadataElement(
	    MetadataElement.EFFECTIVE_DRAINAGE_AREA) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.EFFECTIVE_DRAINAGE_AREA, resource);
	}
    };
    public static final IndexedMetadataElement DRAINAGE_SHAPEFILE = new IndexedMetadataElement(MetadataElement.DRAINAGE_SHAPEFILE) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.DRAINAGE_SHAPEFILE, resource);
	}
    };
    public static final IndexedMetadataElement ALTITUDE = new IndexedMetadataElement(MetadataElement.ALTITUDE) {
	@Override
	public void defineValues(GSResource resource) {
	    MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	    VerticalExtent vertical = miMetadata.getDataIdentification().getVerticalExtent();
	    if (vertical != null) {
		Double minimum = vertical.getMinimumValue();
		Double maximum = vertical.getMaximumValue();
		double tolerance = 1e-10; // Define your tolerance level here
		if (maximum != null && minimum != null && Math.abs(maximum - minimum) < tolerance) {
		    addValue(minimum.toString());
		}
	    }
	    defineBNHSProperty(BNHSProperty.DATUM_ALTITUDE, resource);
	}
    };
    public static final IndexedMetadataElement ELEVATION_MIN = new IndexedMetadataElement(MetadataElement.ELEVATION_MIN) {
	@Override
	public void defineValues(GSResource resource) {
	    MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	    VerticalExtent vertical = miMetadata.getDataIdentification().getVerticalExtent();
	    if (vertical != null) {
		Double minimum = vertical.getMinimumValue();
		if (minimum != null) {
		    addValue(minimum.toString());
		}
	    }
	}
    };
    public static final IndexedMetadataElement ELEVATION_MAX = new IndexedMetadataElement(MetadataElement.ELEVATION_MAX) {
	@Override
	public void defineValues(GSResource resource) {
	    MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	    VerticalExtent vertical = miMetadata.getDataIdentification().getVerticalExtent();
	    if (vertical != null) {
		Double maximum = vertical.getMaximumValue();
		if (maximum != null) {
		    addValue(maximum.toString());
		}
	    }
	}
    };
    public static final IndexedMetadataElement ALTITUDE_DATUM = new IndexedMetadataElement(MetadataElement.ALTITUDE_DATUM) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.DATUM_NAME, resource);
	}
    };
    public static final IndexedMetadataElement FLOW_TO_OCEAN = new IndexedMetadataElement(MetadataElement.FLOW_TO_OCEAN) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.FLOW_TO_OCEAN, resource);
	}
    };
    public static final IndexedMetadataElement DOWNSTREAM_HYCOS_STATION = new IndexedMetadataElement(
	    MetadataElement.DOWNSTREAM_HYCOS_STATION) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.DOWNSTREAM_HYCOS_STATION, resource);
	}
    };
    public static final IndexedMetadataElement REGULATION = new IndexedMetadataElement(MetadataElement.REGULATION) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.REGULATION, resource);
	}
    };
    public static final IndexedMetadataElement REGULATION_START_DATE = new IndexedMetadataElement(MetadataElement.REGULATION_START_DATE) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.REGULATION_START_DATE, resource);
	}
    };
    public static final IndexedMetadataElement REGULATION_END_DATE = new IndexedMetadataElement(MetadataElement.REGULATION_END_DATE) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.REGULATION_END_DATE, resource);
	}
    };
    public static final IndexedMetadataElement LAND_USE_CHANGE = new IndexedMetadataElement(MetadataElement.LAND_USE_CHANGE) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.LAND_USE_CHANGE, resource);
	}
    };
    public static final IndexedMetadataElement SURFACE_COVER = new IndexedMetadataElement(MetadataElement.SURFACE_COVER) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.SURFACE_COVER, resource);
	}
    };
    public static final IndexedMetadataElement DATA_QUALITY_ICE = new IndexedMetadataElement(MetadataElement.DATA_QUALITY_ICE) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.DATA_QUALITY_ICE, resource);
	}
    };
    public static final IndexedMetadataElement DATA_QUALITY_OPEN = new IndexedMetadataElement(MetadataElement.DATA_QUALITY_OPEN) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.DATA_QUALITY_OPEN, resource);
	}
    };
    public static final IndexedMetadataElement DISCHARGE_AVAILABILITY = new IndexedMetadataElement(MetadataElement.DISCHARGE_AVAILABILITY) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.DISCHARGE_AVAILABILITY, resource);
	}
    };
    public static final IndexedMetadataElement WATER_LEVEL_AVAILABILITY = new IndexedMetadataElement(
	    MetadataElement.WATER_LEVEL_AVAILABILITY) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.WATER_LEVEL_AVAILABILITY, resource);
	}
    };
    public static final IndexedMetadataElement WATER_TEMPERATURE_AVAILABILITY = new IndexedMetadataElement(
	    MetadataElement.WATER_TEMPERATURE_AVAILABILITY) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.WATER_TEMPERATURE_AVAILABILITY, resource);
	}
    };
    public static final IndexedMetadataElement ICE_ON_OFF_AVAILABILITY = new IndexedMetadataElement(
	    MetadataElement.ICE_ON_OFF_AVAILABILITY) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.ICE_ON_OFF_AVAILABILITY, resource);
	}
    };
    public static final IndexedMetadataElement ICE_THICKNESS_AVAILABILITY = new IndexedMetadataElement(
	    MetadataElement.ICE_THICKNESS_AVAILABILITY) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.ICE_THICKNESS_AVAILABILITY, resource);
	}
    };
    public static final IndexedMetadataElement SNOW_DEPTH_AVAILABILITY = new IndexedMetadataElement(
	    MetadataElement.SNOW_DEPTH_AVAILABILITY) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.SNOW_DEPTH_AVAILABILITY, resource);
	}
    };
    public static final IndexedMetadataElement MEASUREMENT_METHOD_DISCHARGE = new IndexedMetadataElement(
	    MetadataElement.MEASUREMENT_METHOD_DISCHARGE) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.MEASUREMENT_METHOD_DISCHARGE, resource);
	}
    };
    public static final IndexedMetadataElement MEASUREMENT_METHOD_WATER_TEMPERATURE = new IndexedMetadataElement(
	    MetadataElement.MEASUREMENT_METHOD_WATER_TEMPERATURE) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.MEASUREMENT_METHOD_WATER_TEMPERATURE, resource);
	}
    };

    public static final IndexedMetadataElement MEASUREMENT_METHOD_ICE_ON_OFF = new IndexedMetadataElement(
	    MetadataElement.MEASUREMENT_METHOD_ICE_ON_OFF) {
	@Override
	public void defineValues(GSResource resource) {
	    defineBNHSProperty(BNHSProperty.MEASUREMENT_METHOD_ICE_ON_OFF, resource);
	}
    };

    // ----------------------------------------------
    //
    // TODO
    //
    //
    public static final IndexedMetadataElement SERVICE_TYPE = new IndexedMetadataElement(MetadataElement.SERVICE_TYPE) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedMetadataElement SERVICE_TYPE_VERSION = new IndexedMetadataElement(MetadataElement.SERVICE_TYPE_VERSION) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedMetadataElement OPERATION = new IndexedMetadataElement(MetadataElement.OPERATION) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedMetadataElement OPERATES_ON_IDENTIFIER = new IndexedMetadataElement(MetadataElement.OPERATES_ON_IDENTIFIER) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedMetadataElement OPERATES_ON = new IndexedMetadataElement(MetadataElement.OPERATES_ON) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedMetadataElement OPERATES_ON_NAME = new IndexedMetadataElement(MetadataElement.OPERATES_ON_NAME) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedMetadataElement SITE_INFO = new IndexedMetadataElement(MetadataElement.SITE_INFO) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedMetadataElement BNHS_INFO = new IndexedMetadataElement(MetadataElement.BNHS_INFO) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> optional = resource.getExtensionHandler().getBNHSInfo();
	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    addValue(value);
		}
	    }
	}
    };
    public static final IndexedMetadataElement VARIABLE = new IndexedMetadataElement(MetadataElement.VARIABLE) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedMetadataElement EOP_POLARIZATION_MODE = new IndexedMetadataElement(MetadataElement.EOP_POLARIZATION_MODE) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedMetadataElement PROCESSING_LEVEL_CODE = new IndexedMetadataElement(MetadataElement.PROCESSING_LEVEL_CODE) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedMetadataElement COVERAGE_CONTENT_TYPE_CODE = new IndexedMetadataElement(
	    MetadataElement.COVERAGE_CONTENT_TYPE_CODE) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };

    public static final IndexedMetadataElement COUPLING_TYPE = new IndexedMetadataElement(MetadataElement.COUPLING_TYPE) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedMetadataElement DISTANCE_UOM = new IndexedMetadataElement(MetadataElement.DISTANCE_UOM) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedElement QML_DEPTH_VALUE = new IndexedMetadataElement(MetadataElement.QML_DEPTH_VALUE) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedElement QML_MAGNITUDE_VALUE = new IndexedMetadataElement(MetadataElement.QML_MAGNITUDE_VALUE) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedMetadataElement QML_MAGNITUDE_TYPE = new IndexedMetadataElement(MetadataElement.QML_MAGNITUDE_TYPE) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedElement BAND_RESOLUTION = new IndexedMetadataElement(MetadataElement.BAND_RESOLUTION) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedElement ESSI_TEMPORAL_RESOLUTION = new IndexedMetadataElement(MetadataElement.ESSI_TEMPORAL_RESOLUTION) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };

    public static final IndexedElement ILLUMINATION_AZIMUTH_ANGLE = new IndexedMetadataElement(MetadataElement.ILLUMINATION_AZIMUTH_ANGLE) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedElement ILLUMINATION_ZENITH_ANGLE = new IndexedMetadataElement(MetadataElement.ILLUMINATION_ZENITH_ANGLE) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedElement BAND_PEAK_RESPONSE_WL = new IndexedMetadataElement(MetadataElement.BAND_PEAK_RESPONSE_WL) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedElement ESSI_SPATIAL_RESOLUTION_X = new IndexedMetadataElement(MetadataElement.ESSI_SPATIAL_RESOLUTION_X) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };
    public static final IndexedElement ESSI_SPATIAL_RESOLUTION_Y = new IndexedMetadataElement(MetadataElement.ESSI_SPATIAL_RESOLUTION_Y) {
	@Override
	public void defineValues(GSResource resource) {

	}
    };

    public static final IndexedMetadataElement RASTER_MOSAIC = new IndexedMetadataElement(MetadataElement.RASTER_MOSAIC) {
	@Override
	public void defineValues(GSResource resource) {

	    resource.getExtensionHandler().getRasterMosaic().ifPresent(mosaic -> {

		addValue(String.valueOf(mosaic));

	    });
	}
    };

    public static final IndexedMetadataElement METADATA_VERSION = new IndexedMetadataElement(MetadataElement.METADATA_VERSION) {
	@Override
	public void defineValues(GSResource resource) {

	    resource.getExtensionHandler().getMetadataVersion().ifPresent(version -> {

		addValue(version);

	    });
	}
    };

    public static final IndexedMetadataElement METADATA_ORIGINAL_VERSION = new IndexedMetadataElement(
	    MetadataElement.METADATA_ORIGINAL_VERSION) {
	@Override
	public void defineValues(GSResource resource) {

	    resource.getExtensionHandler().getMetadataOriginalVersion().ifPresent(originalVersion -> {

		addValue(originalVersion);

	    });
	}
    };

    public static final IndexedMetadataElement CROP_TYPES = new IndexedMetadataElement(MetadataElement.CROP_TYPES) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<WorldCerealMap> optional = resource.getExtensionHandler().getWorldCereal();
	    if (optional.isPresent()) {

		WorldCerealMap map = optional.get();

		List<WorldCerealItem> cropTypes = map.getCropTypes();
		if (cropTypes != null) {

		    for (WorldCerealItem worldCerealItem : cropTypes) {

			if (checkStringValue(worldCerealItem.getCode())) {
			    addValue(worldCerealItem.getCode());
			}
		    }
		}
	    }
	}
    };

    public static final IndexedMetadataElement QUANTITY_TYPES = new IndexedMetadataElement(MetadataElement.QUANTITY_TYPES) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<WorldCerealMap> optional = resource.getExtensionHandler().getWorldCereal();
	    if (optional.isPresent()) {

		WorldCerealMap map = optional.get();

		List<WorldCerealItem> quantityTypes = map.getQuantityTypes();
		if (quantityTypes != null) {

		    for (WorldCerealItem worldCerealItem : quantityTypes) {

			if (checkStringValue(worldCerealItem.getCode())) {
			    addValue(worldCerealItem.getCode());
			}
		    }
		}
	    }
	}
    };

    public static final IndexedMetadataElement LAND_COVER_TYPES = new IndexedMetadataElement(MetadataElement.LAND_COVER_TYPES) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<WorldCerealMap> optional = resource.getExtensionHandler().getWorldCereal();
	    if (optional.isPresent()) {

		WorldCerealMap map = optional.get();

		List<WorldCerealItem> lcTypes = map.getLandCoverTypes();

		if (lcTypes != null) {

		    for (WorldCerealItem worldCerealItem : lcTypes) {

			if (checkStringValue(worldCerealItem.getCode())) {
			    addValue(worldCerealItem.getCode());
			}
		    }
		}
	    }
	}
    };

    public static final IndexedMetadataElement IRRIGATION_TYPES = new IndexedMetadataElement(MetadataElement.IRRIGATION_TYPES) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<WorldCerealMap> optional = resource.getExtensionHandler().getWorldCereal();
	    if (optional.isPresent()) {

		WorldCerealMap map = optional.get();

		List<WorldCerealItem> irrTypes = map.getIrrigationTypes();

		if (irrTypes != null) {

		    for (WorldCerealItem worldCerealItem : irrTypes) {

			if (checkStringValue(worldCerealItem.getCode())) {
			    addValue(worldCerealItem.getCode());
			}
		    }
		}
	    }
	}
    };

    public static final IndexedElement CROP_CONFIDENCE = new IndexedMetadataElement(MetadataElement.CONFIDENCE_CROP_TYPE) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<WorldCerealMap> optional = resource.getExtensionHandler().getWorldCereal();
	    if (optional.isPresent()) {

		WorldCerealMap map = optional.get();
		Double cropConfidence = map.getCropTypeConfidence();
		if (cropConfidence != null) {
		    addValue(cropConfidence.toString());
		}
	    }

	}
    };

    public static final IndexedElement LAND_COVER_CONFIDENCE = new IndexedMetadataElement(MetadataElement.CONFIDENCE_LC_TYPE) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<WorldCerealMap> optional = resource.getExtensionHandler().getWorldCereal();
	    if (optional.isPresent()) {

		WorldCerealMap map = optional.get();
		Double lcConfidence = map.getLcTypeConfidence();
		if (lcConfidence != null) {
		    addValue(lcConfidence.toString());
		}
	    }

	}
    };

    public static final IndexedElement IRRIGATION_CONFIDENCE = new IndexedMetadataElement(MetadataElement.CONFIDENCE_IRR_TYPE) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<WorldCerealMap> optional = resource.getExtensionHandler().getWorldCereal();
	    if (optional.isPresent()) {

		WorldCerealMap map = optional.get();
		Double irrConfidence = map.getIrrigationTypeConfidence();
		if (irrConfidence != null) {
		    addValue(irrConfidence.toString());
		}
	    }

	}
    };

    public static final IndexedMetadataElement IN_SITU = new IndexedMetadataElement(MetadataElement.IN_SITU) {
	@Override
	public void defineValues(GSResource resource) {

	    boolean found = resource.getExtensionHandler().isInSitu();
	    addValue(String.valueOf(found));
	}
    };

    public static final IndexedMetadataElement ORGANIZATON = new IndexedMetadataElement(MetadataElement.ORGANIZATION) {

	@Override
	public void defineValues(GSResource resource) {

	    List<ResponsibleParty> parties = new ArrayList<ResponsibleParty>();

	    Iterator<ResponsibleParty> contactIterator = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getContacts();
	    while (contactIterator != null && contactIterator.hasNext()) {
		ResponsibleParty responsibleParty = (ResponsibleParty) contactIterator.next();
		parties.add(responsibleParty);
	    }

	    parties.addAll(resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getPointOfContactParty());
	    parties.addAll(resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getCitedParty());

	    parties = parties.stream().filter(p -> p.getElementType() != null).toList();

	    for (ResponsibleParty party : parties) {
		if (party != null) {
		    OrganizationElementWrapper wrapper = OrganizationElementWrapper.get();
		    String role = party.getRoleCode();
		    if (checkStringValue(role)) {
			wrapper.setRole(role);
		    }
		    Contact contact = party.getContact();
		    if (contact != null) {
			Address address = contact.getAddress();
			Online online = contact.getOnline();
			if (online != null) {
			    String linkage = online.getLinkage();
			    if (linkage != null) {
				wrapper.setHomePageURL(linkage);
			    }
			}
			if (address != null) {
			    String email = address.getElectronicMailAddress();
			    if (email != null) {
				wrapper.setEmail(email);
			    }
			}
		    }
		    String orgName = party.getOrganisationName();
		    if (checkStringValue(orgName)) {
			wrapper.setOrgName(orgName);
		    }
		    String orgURI = party.getOrganisationURI();
		    if (checkStringValue(orgURI)) {
			wrapper.setOrgURI(orgURI);
		    }
		    String indName = party.getIndividualName();
		    if (checkStringValue(indName)) {
			wrapper.setIndividualName(indName);
		    }
		    String indURI = party.getIndividualURI();
		    if (checkStringValue(indURI)) {
			wrapper.setIndividualURI(indURI);
		    }

		    wrapper.setHash();

		    addComposedElement(wrapper.getElement());

		}
	    }

	}
    };

    //
    // composed elements: TO BE DEFINED
    //

    public static final IndexedMetadataElement KEYWORD_SA = new IndexedMetadataElement(MetadataElement.KEYWORD_SA) {

	@Override
	public void defineValues(GSResource resource) {

	    SA_ElementWrapper wrapper1 = SA_ElementWrapper.of(MetadataElement.KEYWORD_SA);

	    wrapper1.setValue(UUID.randomUUID().toString());
	    wrapper1.setUri(UUID.randomUUID().toString());
	    wrapper1.setUriTitle(UUID.randomUUID().toString());
	    wrapper1.setSA_MatchType(UUID.randomUUID().toString());
	    wrapper1.setSA_Uri(UUID.randomUUID().toString());
	    wrapper1.setSA_UriTitle(UUID.randomUUID().toString());

	    // addComposedElement(wrapper1.getElement());

	    SA_ElementWrapper wrapper2 = SA_ElementWrapper.of(MetadataElement.KEYWORD_SA);

	    wrapper2.setValue(UUID.randomUUID().toString());
	    wrapper2.setUri(UUID.randomUUID().toString());
	    wrapper2.setUriTitle(UUID.randomUUID().toString());
	    wrapper2.setSA_MatchType(UUID.randomUUID().toString());
	    wrapper2.setSA_Uri(UUID.randomUUID().toString());
	    wrapper2.setSA_UriTitle(UUID.randomUUID().toString());

	    // addComposedElement(wrapper2.getElement());
	}
    };

    public static final IndexedMetadataElement PARAMETER_SA = new IndexedMetadataElement(MetadataElement.PARAMETER_SA) {

	@Override
	public void defineValues(GSResource resource) {

	    Iterator<CoverageDescription> descriptions = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getCoverageDescriptions();

	    while (descriptions.hasNext()) {
		CoverageDescription next = descriptions.next();
		String description = next.getAttributeTitle();
		boolean add = false;
		SA_ElementWrapper attributeWrapper = SA_ElementWrapper.of(MetadataElement.PARAMETER_SA);
		if (checkStringValue(description)) {
		    attributeWrapper.setUriTitle(description);
		    attributeWrapper.setValue(description);
		    add = true;
		}
		String id = next.getAttributeIdentifier();
		if (checkStringValue(id)) {
		    attributeWrapper.setUri(id);
		    add = true;
		}
		if (add) {
		    addComposedElement(attributeWrapper.getElement());
		}

	    }
	    {
		SA_ElementWrapper attributeWrapper = addComposedKeywords(resource, "theme", MetadataElement.PARAMETER_SA);
		if (attributeWrapper != null) {
		    addComposedElement(attributeWrapper.getElement());
		}
	    }
	    {
		SA_ElementWrapper attributeWrapper = addComposedKeywords(resource, "parameter", MetadataElement.PARAMETER_SA);
		if (attributeWrapper != null) {
		    addComposedElement(attributeWrapper.getElement());
		}
	    }
	}

    };

    public static final IndexedMetadataElement INSTRUMENT_SA = new IndexedMetadataElement(MetadataElement.INSTRUMENT_SA) {

	@Override
	public void defineValues(GSResource resource) {

	}
    };

    public static final IndexedMetadataElement RESPONSIBLE_ORG_SA = new IndexedMetadataElement(MetadataElement.RESPONSIBLE_ORG_SA) {

	@Override
	public void defineValues(GSResource resource) {

	}
    };

    public static final IndexedMetadataElement CRUISE_SA = new IndexedMetadataElement(MetadataElement.CRUISE_SA) {

	@Override
	public void defineValues(GSResource resource) {

	}
    };

    /**
     * @param resource
     * @return
     */
    private static List<Identification> getIdentifications(GSResource resource) {

	return resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getIdentifications();
    }

    static {

	// ----------------------------------------------------------------
	//
	// index type: range element
	// scalar type: string (except bounding box, having no scalar type)
	//

	for (IndexedMetadataElement index : getIndexes()) {

	    switch (index.getMetadataElement().get().getContentType()) {
	    /**
	     * this is in fact not directly indexed, it is indexed through its accessory indexes
	     */
	    case SPATIAL:

		index.getInfoList().add(//
			new IndexedElementInfo(//
				index.getMetadataElement().get().getName(), //
				DatabaseImpl.MARK_LOGIC.getName(), //
				null, //
				null));//
		break;

	    /**
	     * double values
	     */
	    case DOUBLE:
		index.getInfoList().add(//
			new IndexedElementInfo(//
				index.getMetadataElement().get().getName(), //
				DatabaseImpl.MARK_LOGIC.getName(), //
				MarkLogicIndexTypes.RANGE_ELEMENT_INDEX.getType(), //
				MarkLogicScalarType.DOUBLE.getType()));//

		break;

	    /**
	     * integer values
	     */
	    case INTEGER:
		index.getInfoList().add(//
			new IndexedElementInfo(//
				index.getMetadataElement().get().getName(), //
				DatabaseImpl.MARK_LOGIC.getName(), //
				MarkLogicIndexTypes.RANGE_ELEMENT_INDEX.getType(), //
				MarkLogicScalarType.INT.getType()));//

		break;

	    /**
	     * long values
	     */
	    case LONG:
		index.getInfoList().add(//
			new IndexedElementInfo(//
				index.getMetadataElement().get().getName(), //
				DatabaseImpl.MARK_LOGIC.getName(), //
				MarkLogicIndexTypes.RANGE_ELEMENT_INDEX.getType(), //
				MarkLogicScalarType.LONG.getType()));//

		break;

	    /**
	     * string values
	     */
	    default:
		index.getInfoList().add(//
			new IndexedElementInfo(//
				index.getMetadataElement().get().getName(), //
				DatabaseImpl.MARK_LOGIC.getName(), //
				MarkLogicIndexTypes.RANGE_ELEMENT_INDEX.getType(), //
				MarkLogicScalarType.STRING.getType()));//
	    }
	}
    }
}
