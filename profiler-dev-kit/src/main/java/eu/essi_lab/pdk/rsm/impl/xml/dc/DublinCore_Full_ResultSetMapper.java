package eu.essi_lab.pdk.rsm.impl.xml.dc;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.jaxb.csw._2_0_2.BriefRecordType;
import eu.essi_lab.jaxb.csw._2_0_2.RecordType;
import eu.essi_lab.jaxb.csw._2_0_2.SummaryRecordType;
import eu.essi_lab.jaxb.csw._2_0_2.org.purl.dc.elements._1.SimpleLiteral;
import eu.essi_lab.jaxb.ows._1_0_0.BoundingBoxType;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;

public class DublinCore_Full_ResultSetMapper extends DiscoveryResultSetMapper<Element> {

    /**
     * The schema uri of {@link #DC_MAPPING_SCHEMA}
     */
    public static final String DC_SCHEMA_URI = CommonNameSpaceContext.DC_NS_URI;

    /**
     * The schema name of {@link #DC_MAPPING_SCHEMA}
     */
    public static final String DC_SCHEMA_NAME = "DublinCore";

    /**
     * The schema version of {@link #DC_MAPPING_SCHEMA}
     */
    public static final String DC_SCHEMA_VERSION = "1.1";

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema DC_MAPPING_SCHEMA = new MappingSchema();

    private static final String DC_RES_SET_MAPPPER_AS_DOCUMENT_ERROR = "DC_RES_SET_MAPPPER_AS_DOCUMENT_ERROR";

    private List<QName> elementNames = null;

    public DublinCore_Full_ResultSetMapper(List<QName> elementNames) {
	this.elementNames = elementNames;
    }

    public DublinCore_Full_ResultSetMapper() {
    }

    @Override
    public Element map(DiscoveryMessage message, GSResource resource) throws GSException {

	String schemeURI = resource.getOriginalMetadata().getSchemeURI();
	String original = resource.getOriginalMetadata().getMetadata();

	if (strategy == MappingStrategy.PRIORITY_TO_ORIGINAL_METADATA && //
		schemeURI.equals(CommonNameSpaceContext.CSW_NS_URI) && //
		isFullRecord(original)) {

	    try {
		return CommonContext.asDocument(original, true).getDocumentElement();
	    } catch (Exception e) {

		e.printStackTrace();
		throw GSException.createException( //
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			DC_RES_SET_MAPPPER_AS_DOCUMENT_ERROR);
	    }
	}

	RecordType record = new RecordType();

	try {
	    addIdentifier(resource, record, null, null);
	    addTitle(resource, record, null, null);
	    addSubject(resource, record, null);
	    addFormat(resource, record, null);
	    addRelation(resource, record, null);
	    addDate(resource, record, null);
	    addDescription(resource, record, null);
	    addType(resource, record, null, null);

	    addResponsibleParty(resource, record, "originator");
	    addResponsibleParty(resource, record, "publisher");
	    addResponsibleParty(resource, record, "author");

	    addLanguage(resource, record);
	    addSource(resource, record);

	    addBoundingBox(resource, record, null, null);

	    Element out = null;
	    if (elementNames == null) {

		out = CommonContext.asDocument(record, true).getDocumentElement();

	    } else {

		// get only the specified elements
		ArrayList<DublinCore_ElementName> list = new ArrayList<>();
		for (QName qName : elementNames) {
		    String localPart = qName.getLocalPart();
		    DublinCore_ElementName decode = DublinCore_ElementName.decode(localPart);
		    if (decode != null) {
			list.add(decode);
		    }
		}
		if (!list.isEmpty()) {
		    Document doc = CommonContext.asDocument(record, true);
		    out = DublinCore_ElementName.subset(doc, list.toArray(new DublinCore_ElementName[] {})).getDocumentElement();
		}
	    }

	    return out;

	} catch (Exception e) {

	    e.printStackTrace();
	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DC_RES_SET_MAPPPER_AS_DOCUMENT_ERROR);
	}
    }

    protected boolean isSummaryRecord(String originalMetadata) {

	return originalMetadata.toLowerCase().contains("summaryrecord");
    }

    protected boolean isBriefRecord(String originalMetadata) {

	return originalMetadata.toLowerCase().contains("briefrecord");
    }

    protected boolean isFullRecord(String originalMetadata) {

	return originalMetadata.toLowerCase().contains("record");
    }

    protected void addTitle(GSResource resource, RecordType record, BriefRecordType brief, SummaryRecordType summary) {

	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	Iterator<DataIdentification> identifications = miMetadata.getDataIdentifications();
	for (Iterator<DataIdentification> iterator = identifications; iterator.hasNext();) {
	    DataIdentification next = iterator.next();

	    // title
	    String title = next.getCitationTitle();
	    if (title == null) {
		title = "";
	    }

	    SimpleLiteral simpleLiteral = new SimpleLiteral();
	    simpleLiteral.getContent().add(title);
	    JAXBElement<SimpleLiteral> element = ObjectFactories.DCE().createTitle(simpleLiteral);

	    if (record != null) {
		record.getDCElements().add(element);
	    } else if (brief != null) {
		brief.getTitles().add(element);
	    } else {
		summary.getTitles().add(element);
	    }
	}
    }

    protected void addIdentifier(GSResource resource, RecordType record, BriefRecordType brief, SummaryRecordType summary) {

	SimpleLiteral simpleLiteral = new SimpleLiteral();
	simpleLiteral.getContent().add(resource.getHarmonizedMetadata().getCoreMetadata().getIdentifier());
	JAXBElement<SimpleLiteral> element = ObjectFactories.DCE().createIdentifier(simpleLiteral);

	if (record != null) {
	    record.getDCElements().add(element);
	} else if (brief != null) {
	    brief.getIdentifiers().add(element);
	} else {
	    summary.getIdentifiers().add(element);
	}
    }

    protected void addSubject(GSResource resource, RecordType record, SummaryRecordType summary) {

	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	ArrayList<String> values = new ArrayList<>();

	Iterator<DataIdentification> identifications = miMetadata.getDataIdentifications();
	for (Iterator<DataIdentification> iterator = identifications; iterator.hasNext();) {
	    DataIdentification next = iterator.next();
	    Iterator<String> keywordsValues = next.getKeywordsValues();
	    while (keywordsValues.hasNext()) {
		String kwd = keywordsValues.next();
		if (kwd != null && !kwd.equals("")) {
		    values.add(kwd);
		}
	    }

	    Iterator<String> topicCategoriesStrings = next.getTopicCategoriesStrings();
	    while (topicCategoriesStrings.hasNext()) {
		String topic = topicCategoriesStrings.next();
		if (topic != null && !topic.equals("")) {
		    values.add(topic);
		}
	    }
	}

	for (String value : values) {
	    SimpleLiteral simpleLiteral = new SimpleLiteral();
	    simpleLiteral.getContent().add(value);
	    JAXBElement<SimpleLiteral> element = ObjectFactories.DCE().createSubject(simpleLiteral);

	    if (record != null) {
		record.getDCElements().add(element);
	    } else {
		summary.getSubjects().add(simpleLiteral);
	    }
	}
    }

    protected void addFormat(GSResource resource, RecordType record, SummaryRecordType summary) {

	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	Distribution distribution = miMetadata.getDistribution();
	if (distribution != null) {

	    Iterator<Format> formats = distribution.getFormats();
	    while (formats.hasNext()) {
		Format format = formats.next();
		String name = format.getName();
		if (name != null && !name.equals("")) {
		    SimpleLiteral simpleLiteral = new SimpleLiteral();
		    simpleLiteral.getContent().add(name);
		    JAXBElement<SimpleLiteral> element = ObjectFactories.DCE().createFormat(simpleLiteral);

		    if (record != null) {
			record.getDCElements().add(element);
		    } else {
			summary.getFormats().add(element);
		    }
		}
	    }
	}
    }

    protected void addRelation(GSResource resource, RecordType record, SummaryRecordType summary) {

	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	Iterator<String> titles = miMetadata.getAggregatedResourcesIdentifiers();
	while (titles.hasNext()) {
	    String title = (String) titles.next();
	    SimpleLiteral simpleLiteral = new SimpleLiteral();
	    simpleLiteral.getContent().add(title);
	    JAXBElement<SimpleLiteral> element = ObjectFactories.DCE().createRelation(simpleLiteral);

	    if (record != null) {
		record.getDCElements().add(element);
	    } else {
		summary.getRelations().add(element);
	    }
	}
    }

    protected void addDate(GSResource resource, RecordType record, SummaryRecordType summary)
	    throws UnsupportedEncodingException, JAXBException {

	String dateStamp = resource.getHarmonizedMetadata().getCoreMetadata().getReadOnlyMDMetadata().getDateStamp();
	if (dateStamp == null) {
	    XMLGregorianCalendar dateTimeStamp = resource.getHarmonizedMetadata().getCoreMetadata().getReadOnlyMDMetadata()
		    .getDateTimeStamp();
	    if (dateTimeStamp != null) {
		dateStamp = dateTimeStamp.toString();
	    }
	}

	if (dateStamp != null) {
	    SimpleLiteral simpleLiteral = new SimpleLiteral();
	    simpleLiteral.getContent().add(dateStamp);

	    JAXBElement<SimpleLiteral> element = ObjectFactories.DCE().createDate(simpleLiteral);

	    if (record != null) {
		record.getDCElements().add(element);
	    } else {
		summary.getModifieds().add(simpleLiteral);
	    }
	}
    }

    protected void addDescription(GSResource resource, RecordType record, SummaryRecordType summary) {

	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	Iterator<DataIdentification> identifications = miMetadata.getDataIdentifications();
	for (Iterator<DataIdentification> iterator = identifications; iterator.hasNext();) {
	    DataIdentification next = iterator.next();
	    String abstract_ = next.getAbstract();
	    if (abstract_ != null && !abstract_.equals("")) {

		SimpleLiteral simpleLiteral = new SimpleLiteral();
		simpleLiteral.getContent().add(abstract_);
		JAXBElement<SimpleLiteral> element = ObjectFactories.DCT().createAbstract(simpleLiteral);

		if (record != null) {
		    record.getDCElements().add(element);
		} else {
		    summary.getAbstracts().add(simpleLiteral);
		}
	    }
	}
    }

    protected void addType(GSResource resource, RecordType record, BriefRecordType brief, SummaryRecordType summary) {

	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	String value = miMetadata.getHierarchyLevelScopeCodeListValue();
	if (value != null) {
	    SimpleLiteral simpleLiteral = new SimpleLiteral();
	    simpleLiteral.getContent().add(value);
	    JAXBElement<SimpleLiteral> element = ObjectFactories.DCE().createType(simpleLiteral);

	    if (record != null) {
		record.getDCElements().add(element);
	    } else if (brief != null) {
		brief.setType(simpleLiteral);
	    } else {
		summary.setType(simpleLiteral);
	    }
	}
    }

    protected void addBoundingBox(GSResource resource, RecordType record, BriefRecordType brief, SummaryRecordType summary) {

	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	Iterator<DataIdentification> identifications = miMetadata.getDataIdentifications();
	for (Iterator<DataIdentification> iterator = identifications; iterator.hasNext();) {
	    DataIdentification next = iterator.next();

	    // bounding box
	    Iterator<GeographicBoundingBox> boxes = next.getGeographicBoundingBoxes();
	    for (Iterator<GeographicBoundingBox> it = boxes; it.hasNext();) {
		GeographicBoundingBox bbox = it.next();

		BoundingBoxType boxType = new BoundingBoxType();
		boxType.getLowerCorner().add(String.valueOf(bbox.getWest()));
		boxType.getLowerCorner().add(String.valueOf(bbox.getSouth()));
		boxType.getUpperCorner().add(String.valueOf(bbox.getEast()));
		boxType.getUpperCorner().add(String.valueOf(bbox.getNorth()));

		JAXBElement<BoundingBoxType> createBoundingBox = ObjectFactories.OWS().createBoundingBox(boxType);

		if (record != null) {
		    record.getBoundingBoxes().add(createBoundingBox);
		} else if (brief != null) {
		    brief.getBoundingBoxes().add(createBoundingBox);
		} else {
		    summary.getBoundingBoxes().add(createBoundingBox);
		}
	    }
	}
    }

    private void addResponsibleParty(GSResource resource, RecordType record, String code) {

	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	Iterator<DataIdentification> identifications = miMetadata.getDataIdentifications();
	for (Iterator<DataIdentification> iterator = identifications; iterator.hasNext();) {
	    DataIdentification next = iterator.next();
	    ResponsibleParty pointOfContact = next.getPointOfContact(code);
	    if (pointOfContact != null) {
		String name = pointOfContact.getOrganisationName();
		if (name != null && !name.equals("")) {

		    SimpleLiteral simpleLiteral = new SimpleLiteral();
		    simpleLiteral.getContent().add(name);

		    JAXBElement<SimpleLiteral> element = null;
		    switch (code) {
		    case "originator":
			element = ObjectFactories.DCE().createCreator(simpleLiteral);
			break;
		    case "publisher":
			element = ObjectFactories.DCE().createPublisher(simpleLiteral);
			break;
		    case "author":
			element = ObjectFactories.DCE().createContributor(simpleLiteral);
			break;
		    }
		    record.getDCElements().add(element);
		}
	    }
	}
    }

    private void addSource(GSResource resource, RecordType record) {

	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	String identifier = miMetadata.getParentIdentifier();
	if (identifier != null && !identifier.equals("")) {

	    SimpleLiteral simpleLiteral = new SimpleLiteral();
	    simpleLiteral.getContent().add(identifier);

	    JAXBElement<SimpleLiteral> element = ObjectFactories.DCE().createSource(simpleLiteral);
	    record.getDCElements().add(element);
	}
    }

    private void addLanguage(GSResource resource, RecordType record) {

	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	String language = miMetadata.getLanguage();
	if (language != null && !language.equals("")) {
	    SimpleLiteral simpleLiteral = new SimpleLiteral();
	    simpleLiteral.getContent().add(language);
	    JAXBElement<SimpleLiteral> element = ObjectFactories.DCE().createLanguage(simpleLiteral);
	    record.getDCElements().add(element);
	}
    }

    @Override
    public MappingSchema getMappingSchema() {

	return DC_MAPPING_SCHEMA;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
