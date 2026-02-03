package eu.essi_lab.iso.datamodel;

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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gco.v_20060504.CodeListValueType;
import net.opengis.iso19139.gmx.v_20060504.AnchorType;
import net.opengis.iso19139.gmx.v_20060504.MimeFileTypeType;

/**
 * @author Fabrizio
 */
public class ISOMetadata<T> extends DOMSerializer {

    /**
     * ISO code space constants
     */
    public static final String ISO_19115_CODESPACE = "ISOTC211/19115";
    public static final String ISO_19119_CODESPACE = "ISOTC211/19119";
    /**
     * Constants from official gmx catalog at: http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml
     */
    public static final String CI_DATE_TYPE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode";
    public static final String CI_ON_LINE_FUNCTION_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#CI_OnLineFunctionCode";
    public static final String CI_PRESENTATION_FORM_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#CI_PresentationFormCode";
    public static final String CI_ROLE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode";
    public static final String DQ_EVALUATION_METHOD_TYPE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#DQ_EvaluationMethodTypeCode";
    public static final String DS_ASSOCIATION_TYPE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#DS_AssociationTypeCode";
    public static final String DS_INITIATIVE_TYPE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#DS_InitiativeTypeCode";
    public static final String MD_CELL_GEOMETRY_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_CellGeometryCode";
    public static final String MD_CHARACTER_SET_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_CharacterSetCode";
    public static final String MD_CLASSIFICATION_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_ClassificationCode";
    public static final String MD_COVERAGE_CONTENT_TYPE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_CoverageContentTypeCode";
    public static final String MD_DATATYPE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_DatatypeCode";
    public static final String MD_DIMENSION_NAME_TYPE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_DimensionNameTypeCode";
    public static final String MD_GEOMETRIC_OBJECT_TYPE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_GeometricObjectTypeCode";
    public static final String MD_IMAGING_CONDITION_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_ImagingConditionCode";
    public static final String MD_KEYWORD_TYPE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode";
    public static final String MD_MAINTENANCE_FREQUENCY_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_MaintenanceFrequencyCode";
    public static final String MD_MEDIUM_FORMAT_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_MediumFormatCode";
    public static final String MD_MEDIUM_NAME_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_MediumNameCode";
    public static final String MD_OBLIGATION_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_ObligationCode";
    public static final String MD_PIXEL_ORIENTATION_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_PixelOrientationCode";
    public static final String MD_PROGRESS_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_ProgressCode";
    public static final String MD_RESTRICTION_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_RestrictionCode";
    public static final String MD_SCOPE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode";
    public static final String MD_SPATIAL_REPRESENTATION_TYPE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_SpatialRepresentationTypeCode";
    public static final String MD_TOPIC_CATEGORY_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_TopicCategoryCode";
    public static final String MD_TOPOLOGY_LEVEL_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MD_TopologyLevelCode";
    public static final String MX_SCOPE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/codeList.xml#MX_ScopeCode";
    /**
     * Constants from official gmi catalog at: http://www.isotc211.org/2005/resources/Codelist/gmiCodelists.xml
     */
    public static final String MI_BAND_DEFINITION_CODELIST = "http://www.isotc211.org/2005/resources/Codelist/gmiCodelists.xml#MI_BandDefinition";
    public static final String MI_CONTEXT_CODE_CODELIST = "http://www.isotc211.org/2005/resources/Codelist/gmiCodelists.xml#MI_ContextCode";
    public static final String MI_GEOMETRY_TYPE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/Codelist/gmiCodelists.xml#MI_GeometryTypeCode";
    public static final String MI_OBJECTIVE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/Codelist/gmiCodelists.xml#MI_ObjectiveTypeCode";
    public static final String MI_OPERATION_TYPE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/Codelist/gmiCodelists.xml#MI_OperationTypeCode";
    public static final String MI_POLARIZATION_ORIENTATION_CODELIST = "http://www.isotc211.org/2005/resources/Codelist/gmiCodelists.xml#MI_PolarizationOrientationCode";
    public static final String MI_PRIORITY_CODE_CODELIST = "http://www.isotc211.org/2005/resources/Codelist/gmiCodelists.xml#MI_PriorityCode";
    public static final String MI_SEQUENCE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/Codelist/gmiCodelists.xml#MI_SequenceCode";
    public static final String MI_TRANSFER_FUNCTION_TYPE_CODE_CODELIST = "http://www.isotc211.org/2005/resources/Codelist/gmiCodelists.xml#MI_TransferFunctionTypeCode";
    public static final String MI_TRIGGER_CODE_CODELIST = "http://www.isotc211.org/2005/resources/Codelist/gmiCodelists.xml#MI_TriggerCode";
    /**
     * Unfortunately we don't have an official ISO srv catalog
     */
    public static final String SV_COUPLING_TYPE_CODELIST = "#SV_CouplingType";
    public static final String SV_DCP_LIST_CODELIST = "#DCPList";

    protected T type;
    private XMLDocumentReader xmlDoc;

    public ISOMetadata() {
    }

    public ISOMetadata(T type) {

	this.type = type;
    }

    public ISOMetadata(InputStream stream) throws JAXBException {

	this.type = fromStream(stream);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T fromStream(InputStream stream) throws JAXBException {

	Unmarshaller unmarshaller = CommonContext.createUnmarshaller();
	return ((JAXBElement<T>) unmarshaller.unmarshal(stream)).getValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T fromNode(Node node) throws JAXBException {

	Unmarshaller unmarshaller = CommonContext.createUnmarshaller();
	T value = null;
	try {
	    value = ((JAXBElement<T>) unmarshaller.unmarshal(node)).getValue();

	} catch (JAXBException ex) {

	    if (ex instanceof UnmarshalException) {

		StackTraceElement[] stackTrace = ex.getStackTrace();
		
		String message = ex.getMessage();

		if (message != null && message.contains("unexpected element")) {

		    message = message.substring(0, message.indexOf(")") + 1);

		    ex = new UnmarshalException(message);
		    ex.setStackTrace(stackTrace);
		}
	    }

	    throw ex;
	}

	return value;
    }

    public T getElementType() {

	return this.type;
    }

    public String getTextContent() throws ParserConfigurationException, IOException, SAXException, JAXBException, XPathExpressionException {

	if (xmlDoc == null) {
	    xmlDoc = new XMLDocumentReader(asDocument(true));
	}

	return xmlDoc.evaluateString("normalize-space( string( /* ) )");
    }

    protected void setElementType(T type) {

	this.type = type;
    }

    public JAXBElement<? extends T> getElement() {
	throw new UnsupportedOperationException();
    }

    protected Unmarshaller createUnmarshaller() throws JAXBException {

	return CommonContext.createUnmarshaller();
    }

    protected Marshaller createMarshaller() throws JAXBException {

	Marshaller marshaller = CommonContext.createMarshaller(true);
	marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
		"http://www.isotc211.org/2005/gmi http://www.isotc211.org/2005/gmi/gmi.xsd");

	return marshaller;
    }

    /*
     * Utility methods
     */

    public static CharacterStringPropertyType createCharacterStringPropertyType(String value) {
	if (value == null) {
	    return null;
	}
	CharacterStringPropertyType pType = new CharacterStringPropertyType();
	pType.setCharacterString(ObjectFactories.GCO().createCharacterString(value));

	return pType;
    }

    public static CharacterStringPropertyType createAnchorPropertyType(String value) {

	return createAnchorPropertyType(value, null);
    }

    public static CharacterStringPropertyType createAnchorPropertyType(String value, String title) {

	AnchorType anchorType = new AnchorType();
	anchorType.setHref(value);
	anchorType.setTitle(title);
	anchorType.setValue(title);

	JAXBElement<AnchorType> anchor = ObjectFactories.GMX().createAnchor(anchorType);

	CharacterStringPropertyType pType = new CharacterStringPropertyType();
	pType.setCharacterString(anchor);

	return pType;
    }

    public static CodeListValueType createCodeListValueType(String codeList, String codeListValue, String codeSpace, String value) {
	CodeListValueType codeListValueType = new CodeListValueType();
	codeListValueType.setCodeListValue(codeListValue);
	codeListValueType.setCodeList(codeList);
	codeListValueType.setValue(value);
	codeListValueType.setCodeSpace(codeSpace);
	return codeListValueType;
    }

    public static String getStringFromCharacterString(CharacterStringPropertyType characterStringPropertyType) {
	if (characterStringPropertyType != null && //
		characterStringPropertyType.isSetCharacterString() && //
		characterStringPropertyType.getCharacterString().getValue() != null) {
	    Object value = characterStringPropertyType.getCharacterString().getValue();
	    if (value instanceof AnchorType) {
		AnchorType anchor = (AnchorType) value;
		return anchor.getValue();
	    } else if (value instanceof CodeListValueType) {
		CodeListValueType clvt = (CodeListValueType) value;
		return clvt.getValue();
	    } else if (value instanceof MimeFileTypeType) {
		MimeFileTypeType mftt = (MimeFileTypeType) value;
		return mftt.getValue();
	    } else {
		return value.toString();
	    }
	}
	return null;
    }
    
    public static String getHREFStringFromCharacterString(CharacterStringPropertyType characterStringPropertyType) {
	if (characterStringPropertyType != null && //
		characterStringPropertyType.isSetCharacterString() && //
		characterStringPropertyType.getCharacterString().getValue() != null) {
	    Object value = characterStringPropertyType.getCharacterString().getValue();
	    if (value instanceof AnchorType) {
		AnchorType anchor = (AnchorType) value;
		return anchor.getHref();
	    } else if (value instanceof CodeListValueType) {
		CodeListValueType clvt = (CodeListValueType) value;
		return clvt.getValue();
	    } else {
		return null;
	    }
	}
	return null;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof ISOMetadata<?>) {
	    ISOMetadata<?> isoObject = (ISOMetadata<?>) obj;
	    Object elementType = isoObject.getElementType();
	    return elementType.equals(getElementType());
	}
	return false;
    }

    @Override
    public int hashCode() {
	return super.hashCode();
    }

}
