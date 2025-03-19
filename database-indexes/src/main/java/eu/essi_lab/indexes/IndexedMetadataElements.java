package eu.essi_lab.indexes;

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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.datatype.XMLGregorianCalendar;

import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.indexes.marklogic.MarkLogicIndexTypes;
import eu.essi_lab.indexes.marklogic.MarkLogicScalarType;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.index.IndexedElementInfo;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.resource.BNHSProperty;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.SatelliteScene;
import eu.essi_lab.model.resource.worldcereal.WorldCerealItem;
import eu.essi_lab.model.resource.worldcereal.WorldCerealMap;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * This class groups all the available {@link IndexedMetadataElement}s
 * 
 * @see MetadataElement
 * @author Fabrizio
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
		getValues().add(anyText);

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

		getValues().add(dateStamp);

	    } else {
		XMLGregorianCalendar dateTimeStamp = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDateTimeStamp();
		if (dateTimeStamp != null) {

		    getValues().add(dateTimeStamp.toString());
		}
	    }
	}
    };

    public static final IndexedMetadataElement IDENTIFIER = new IndexedMetadataElement(MetadataElement.IDENTIFIER) {
	@Override
	public void defineValues(GSResource resource) {

	    String identifier = resource.getHarmonizedMetadata().getCoreMetadata().getIdentifier();

	    if (checkStringValue(identifier)) {
		getValues().add(identifier);
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
		    getValues().add(codeListValue);
		}
	    }
	}
    };
    public static final IndexedMetadataElement TITLE = new IndexedMetadataElement(MetadataElement.TITLE) {
	@Override
	public void defineValues(GSResource resource) {

	    MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	    Iterator<DataIdentification> identifications = miMetadata.getDataIdentifications();
	    while (identifications.hasNext()) {
		DataIdentification id = identifications.next();
		String title = id.getCitationTitle();
		if (checkStringValue(title)) {
		    getValues().add(title);
		}
	    }
	}
    };
    public static final IndexedMetadataElement PARENT_IDENTIFIER = new IndexedMetadataElement(MetadataElement.PARENT_IDENTIFIER) {
	@Override
	public void defineValues(GSResource resource) {

	    String identifier = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getParentIdentifier();
	    if (checkStringValue(identifier)) {

		getValues().add(identifier);
	    }
	}
    };

    public static final IndexedMetadataElement ABSTRACT = new IndexedMetadataElement(MetadataElement.ABSTRACT) {
	@Override
	public void defineValues(GSResource resource) {

	    MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	    Iterator<DataIdentification> identifications = miMetadata.getDataIdentifications();
	    while (identifications.hasNext()) {
		DataIdentification id = identifications.next();
		String abs = id.getAbstract();
		if (checkStringValue(abs)) {
		    getValues().add(abs);
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

	    Iterator<DataIdentification> dataIds = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getDataIdentifications();

	    while (dataIds.hasNext()) {
		DataIdentification dataId = (DataIdentification) dataIds.next();

		Iterator<TemporalExtent> extents = dataId.getTemporalExtents();

		while (extents.hasNext()) {

		    TemporalExtent te = (TemporalExtent) extents.next();

		    if (te.isBeforeNowBeginPosition()) {

			String beginPosition = te.getBeforeNowBeginPosition().get().name();

			getValues().add(beginPosition);
		    }
		}
	    }
	}
    };

    public static final IndexedElement TEMP_EXTENT_BEGIN = new IndexedMetadataElement(MetadataElement.TEMP_EXTENT_BEGIN) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<DataIdentification> dataIds = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getDataIdentifications();

	    if (!dataIds.hasNext()) {
		resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_BEGIN_NULL);
		return;
	    }

	    while (dataIds.hasNext()) {
		DataIdentification dataId = (DataIdentification) dataIds.next();

		Iterator<TemporalExtent> extents = dataId.getTemporalExtents();
		if (!extents.hasNext()) {
		    resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_BEGIN_NULL);
		    return;
		}

		while (extents.hasNext()) {

		    TemporalExtent te = (TemporalExtent) extents.next();

		    if (te.isBeginPositionIndeterminate() && te.getIndeterminateBeginPosition() == TimeIndeterminateValueType.NOW) {

			resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_BEGIN_NOW);

		    } else if (!te.isBeforeNowBeginPosition()) {

			String beginPosition = te.getBeginPosition();

			if (checkStringValue(beginPosition)) {

			    if (beginPosition.equals("now")) {

				resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_BEGIN_NOW);

			    } else {

				getValues().add(beginPosition);
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

	    Iterator<DataIdentification> dataIds = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getDataIdentifications();

	    if (!dataIds.hasNext()) {
		resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_END_NULL);
		return;
	    }

	    while (dataIds.hasNext()) {
		DataIdentification dataId = (DataIdentification) dataIds.next();

		Iterator<TemporalExtent> extents = dataId.getTemporalExtents();
		if (!extents.hasNext()) {
		    resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_END_NULL);
		    return;
		}

		while (extents.hasNext()) {

		    TemporalExtent te = (TemporalExtent) extents.next();

		    if (te.isEndPositionIndeterminate() && te.getIndeterminateEndPosition() == TimeIndeterminateValueType.NOW) {

			resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_END_NOW);

		    } else {

			String endPosition = te.getEndPosition();

			if (checkStringValue(endPosition)) {

			    if (endPosition.equals("now")) {

				resource.getIndexesMetadata().write(IndexedElements.TEMP_EXTENT_END_NOW);

			    } else {

				getValues().add(endPosition);
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

		    getValues().add(linkage);
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

		    getValues().add(id);
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

		    getValues().add(name);
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

		    getValues().add(protocol);
		}
	    }

	}
    };

    public static final IndexedMetadataElement LANGUAGE = new IndexedMetadataElement(MetadataElement.LANGUAGE) {
	@Override
	public void defineValues(GSResource resource) {

	    String language = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getLanguage();

	    if (checkStringValue(language)) {
		getValues().add(language);
	    }
	}
    };

    public static final IndexedMetadataElement CREATION_DATE = new IndexedMetadataElement(MetadataElement.CREATION_DATE) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<DataIdentification> identifications = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getDataIdentifications();
	    while (identifications.hasNext()) {

		DataIdentification next = identifications.next();

		String creationDate = next.getCitationCreationDate();
		if (checkStringValue(creationDate)) {

		    getValues().add(creationDate);
		} else {
		    XMLGregorianCalendar dateTime = next.getCitationCreationDateTime();
		    if (dateTime != null) {
			getValues().add(dateTime.toString());
		    }
		}
	    }
	}
    };

    public static final IndexedMetadataElement PUBLICATION_DATE = new IndexedMetadataElement(MetadataElement.PUBLICATION_DATE) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<DataIdentification> identifications = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getDataIdentifications();
	    while (identifications.hasNext()) {

		DataIdentification next = identifications.next();

		String pubDate = next.getCitationPublicationDate();
		if (checkStringValue(pubDate)) {

		    getValues().add(pubDate);
		} else {
		    XMLGregorianCalendar dateTime = next.getCitationPublicationDateTime();
		    if (dateTime != null) {
			getValues().add(dateTime.toString());
		    }
		}
	    }
	}
    };

    public static final IndexedElement REVISION_DATE = new IndexedMetadataElement(MetadataElement.REVISION_DATE) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<DataIdentification> identifications = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getDataIdentifications();
	    while (identifications.hasNext()) {

		DataIdentification next = identifications.next();

		String revDate = next.getCitationRevisionDate();
		if (checkStringValue(revDate)) {

		    getValues().add(revDate);
		} else {
		    XMLGregorianCalendar dateTime = next.getCitationRevisionDateTime();
		    if (dateTime != null) {
			getValues().add(dateTime.toString());
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
			getValues().add(topic);
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
		    getValues().add(resourceIdentifier.toString());
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
			getValues().add(lan);
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

	    Iterator<DataIdentification> identifications = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getDataIdentifications();

	    while (identifications.hasNext()) {
		DataIdentification dataId = identifications.next();
		ResponsibleParty party = dataId.getPointOfContact("author");
		if (party != null) {
		    String individualName = party.getIndividualName();
		    if (checkStringValue(individualName)) {
			getValues().add(individualName);
		    }
		}
	    }
	}
    };

    public static final IndexedMetadataElement ORGANISATION_NAME = new IndexedMetadataElement(MetadataElement.ORGANISATION_NAME) {
	@Override
	public void defineValues(GSResource resource) {

	    List<ResponsibleParty> parties = new ArrayList<ResponsibleParty>();

	    parties.addAll(resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getPointOfContactParty());

	    parties.addAll(resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getCitedParty());

	    for (ResponsibleParty party : parties) {
		if (party != null) {
		    String orgName = party.getOrganisationName();
		    if (checkStringValue(orgName)) {
			getValues().add(orgName);
		    }
		}
	    }

	    defineBNHSProperty(BNHSProperty.INSTITUTE, resource);

	}
    };

    public static final IndexedMetadataElement ORGANISATION_ROLE = new IndexedMetadataElement(MetadataElement.ORGANISATION_ROLE) {
	@Override
	public void defineValues(GSResource resource) {

	    List<ResponsibleParty> parties = new ArrayList<ResponsibleParty>();

	    parties.addAll(resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getPointOfContactParty());

	    parties.addAll(resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getCitedParty());
	    for (ResponsibleParty party : parties) {
		if (party != null) {
		    String role = party.getRoleCode();
		    if (checkStringValue(role)) {
			getValues().add(role);
		    }
		}
	    }

	}
    };

    public static final IndexedMetadataElement ORGANISATION_URI = new IndexedMetadataElement(MetadataElement.ORGANISATION_URI) {
	@Override
	public void defineValues(GSResource resource) {

	    List<ResponsibleParty> parties = new ArrayList<ResponsibleParty>();

	    parties.addAll(resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getPointOfContactParty());

	    parties.addAll(resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getCitedParty());

	    for (ResponsibleParty party : parties) {
		if (party != null) {
		    String orgURI = party.getOrganisationURI();
		    if (checkStringValue(orgURI)) {
			getValues().add(orgURI);
		    }
		}
	    }

	}
    };

    public static final IndexedMetadataElement ORIGINATOR_ORGANISATION_IDENTIFIER = new IndexedMetadataElement(
	    MetadataElement.ORIGINATOR_ORGANISATION_IDENTIFIER) {
	@Override
	public void defineValues(GSResource resource) {

	    ExtensionHandler handler = resource.getExtensionHandler();
	    List<String> identifiers = handler.getOriginatorOrganisationIdentifiers();
	    for (String identifier : identifiers) {
		if (checkStringValue(identifier)) {
		    getValues().add(identifier);
		}
	    }
	}
    };
    public static final IndexedMetadataElement ORIGINATOR_ORGANISATION_DESCRIPTION = new IndexedMetadataElement(
	    MetadataElement.ORIGINATOR_ORGANISATION_DESCRIPTION) {
	@Override
	public void defineValues(GSResource resource) {

	    HashSet<String> names = new HashSet<>();

	    List<ResponsibleParty> originators = resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification()
		    .getOriginatorParty();
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

	    getValues().addAll(names);

	}
    };
    public static final IndexedMetadataElement TEAM_CATEGORY = new IndexedMetadataElement(MetadataElement.THEME_CATEGORY) {
	@Override
	public void defineValues(GSResource resource) {

	    ExtensionHandler handler = resource.getExtensionHandler();
	    Optional<String> themeCategoryOpt = handler.getThemeCategory();
	    if (themeCategoryOpt.isPresent()) {
		if (checkStringValue(themeCategoryOpt.get())) {
		    getValues().add(themeCategoryOpt.get());
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
		    getValues().add(code);
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
		    getValues().add(version);
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
		    getValues().add(authority);
		}
	    }
	}
    };

    public static final IndexedMetadataElement GEOGRAPHIC_DESCRIPTION_CODE = new IndexedMetadataElement(
	    MetadataElement.GEOGRAPHIC_DESCRIPTION_CODE) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<DataIdentification> identifications = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getDataIdentifications();

	    while (identifications.hasNext()) {
		DataIdentification dataId = identifications.next();
		Iterator<String> codes = dataId.getGeographicDescriptionCodes();
		while (codes.hasNext()) {
		    String code = codes.next();
		    if (checkStringValue(code)) {
			getValues().add(code);
		    }
		}
	    }
	}
    };

    public static final IndexedMetadataElement ALTERNATE_TITLE = new IndexedMetadataElement(MetadataElement.ALTERNATE_TITLE) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<DataIdentification> identifications = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getDataIdentifications();

	    while (identifications.hasNext()) {
		DataIdentification dataId = identifications.next();
		String alternateTitle = dataId.getCitationAlternateTitle();
		if (checkStringValue(alternateTitle)) {
		    getValues().add(alternateTitle);
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
		    getValues().add(identifier);
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
		    getValues().add(value);
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
		    getValues().add(description);
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
		    getValues().add(title);
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
		    getValues().add(identifier);
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
			getValues().add(title);
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
		    getValues().add(value);
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
			getValues().add("" + value);
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
			getValues().add("" + dataSize);
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
		    getValues().add(description);
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
		    getValues().add(description);
		}
	    }
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
		    getValues().add(description);
		}
	    }
	}
    };

    public static final IndexedMetadataElement OBSERVED_PROPERTY_URI = new IndexedMetadataElement(MetadataElement.OBSERVED_PROPERTY_URI) {
	@Override
	public void defineValues(GSResource resource) {

	    Optional<String> optional = resource.getExtensionHandler().getObservedPropertyURI();

	    if (optional.isPresent()) {

		String value = optional.get();
		if (checkStringValue(value)) {
		    getValues().add(value);
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
			getValues().add(duration.toString());
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
			getValues().add(resolution.toString());
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
		    getValues().add(value);
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
		    getValues().add(value);
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
		    getValues().add(value);
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
		    getValues().add(description);
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
		    getValues().add(kwdType);
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
		    getValues().add(title);
		}
	    }
	}
    };

    public static final IndexedElement HAS_SECURITY_CONSTRAINTS = new IndexedMetadataElement(MetadataElement.HAS_SECURITY_CONSTRAINTS) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<DataIdentification> iterator = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getDataIdentifications();

	    boolean found = false;
	    while (iterator.hasNext()) {
		if (iterator.next().hasSecurityConstraints()) {
		    found = true;
		    break;
		}
	    }
	    getValues().add(String.valueOf(found));
	}
    };
    public static final IndexedElement HAS_USE_LEGAL_CONSTRAINTS = new IndexedMetadataElement(MetadataElement.HAS_USE_LEGAL_CONSTRAINTS) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<DataIdentification> iterator = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getDataIdentifications();

	    boolean found = false;
	    while (iterator.hasNext()) {
		if (iterator.next().hasUseLegalConstraints()) {
		    found = true;
		    break;
		}
	    }
	    getValues().add(String.valueOf(found));
	}
    };
    public static final IndexedElement HAS_ACCESS_LEGAL_CONSTRAINTS = new IndexedMetadataElement(
	    MetadataElement.HAS_ACCESS_LEGAL_CONSTRAINTS) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<DataIdentification> iterator = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getDataIdentifications();

	    boolean found = false;
	    while (iterator.hasNext()) {
		if (iterator.next().hasAccessLegalConstraints()) {
		    found = true;
		    break;
		}
	    }
	    getValues().add(String.valueOf(found));
	}
    };
    public static final IndexedElement HAS_OTHER_LEGAL_CONSTRAINTS = new IndexedMetadataElement(
	    MetadataElement.HAS_OTHER_LEGAL_CONSTRAINTS) {
	@Override
	public void defineValues(GSResource resource) {

	    Iterator<DataIdentification> iterator = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getDataIdentifications();

	    boolean found = false;
	    while (iterator.hasNext()) {
		if (iterator.next().hasOtherLegalConstraints()) {
		    found = true;
		    break;
		}
	    }
	    getValues().add(String.valueOf(found));
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
		    getValues().add(String.valueOf(value));
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
		    getValues().add(String.valueOf(den));
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
		    getValues().add(prodType);
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
		    getValues().add(sensorOpMode);
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
		    getValues().add(sensorSwath);
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
		    getValues().add(s3InstrumentIdx);
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
		    getValues().add(s3ProductLevel);
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
		    getValues().add(s3Timeless);
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
		    getValues().add(sarPolCh);
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
		    getValues().add(orbit.toString());
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
		    getValues().add(row.toString());
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
		    getValues().add(path.toString());
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
		    getValues().add(value.toString());
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
		getValues().add(optional.get());
	    }
	    defineBNHSProperty(BNHSProperty.COUNTRY, resource);
	}
    };

    public static final IndexedMetadataElement COUNTRY_ISO3 = new IndexedMetadataElement(MetadataElement.COUNTRY_ISO3) {
	@Override
	public void defineValues(GSResource resource) {
	    Optional<String> optional = resource.getExtensionHandler().getCountryISO3();
	    if (optional.isPresent()) {
		getValues().add(optional.get());
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
		    getValues().add(value);
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
		    getValues().add(minimum.toString());
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
		if (minimum != null ) {
		    getValues().add(minimum.toString());
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
		if (maximum != null ) {
		    getValues().add(maximum.toString());
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
		    getValues().add(value);
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
			    getValues().add(worldCerealItem.getCode());
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
			    getValues().add(worldCerealItem.getCode());
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
			    getValues().add(worldCerealItem.getCode());
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
			    getValues().add(worldCerealItem.getCode());
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
		    getValues().add(cropConfidence.toString());
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
		    getValues().add(lcConfidence.toString());
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
		    getValues().add(irrConfidence.toString());
		}
	    }

	}
    };

    public static final IndexedMetadataElement IN_SITU = new IndexedMetadataElement(MetadataElement.IN_SITU) {
	@Override
	public void defineValues(GSResource resource) {

	    boolean found = resource.getExtensionHandler().isInSitu();
	    getValues().add(String.valueOf(found));
	}
    };

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
