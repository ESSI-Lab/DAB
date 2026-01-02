package eu.essi_lab.pdk.rsm.impl.xml.iso19139;

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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import com.google.common.base.CaseFormat;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.lib.xml.XMLPruner;

/**
 * @author Fabrizio
 */
public enum GMD_ElementName {

    BOUNDING_BOX("*/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox//*"), //
    IDENTIFIER("*/gmd:fileIdentifier//*"), //
    GRAPHIC_OVERVIEW("*/gmd:identificationInfo/*/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName//*"), //
    SERVICE_TYPE("*/gmd:identificationInfo/*/srv:serviceType//*"), //
    SERVICE_TYPE_VERSION("*/gmd:identificationInfo/*/srv:serviceTypeVersion//*"), //
    TITLE("*/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title//*"), //
    TYPE("*/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"), //
    ABSTRACT("*/gmd:identificationInfo/*/gmd:abstract//*"), //
    CHARACTER_SET("*/gmd:identificationInfo/*/gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue"), //
    CREATOR("*/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName[gmd:role/gmd:CI_RoleCode/@codeListValue='originator']//*"), //
    CONTRIBUTOR(
	    "*/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName[gmd:role/gmd:CI_RoleCode/@codeListValue='author']//*"), //
    COUPLING_TYPE("*/gmd:identificationInfo/*/srv:couplingType/srv:SV_CouplingType/@codeListValue"), //
    FORMAT("*/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name//*"), //
    FORMAT_VERSION("*/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:version//*"), //
    HIERARCHY_LEVEL_NAME("*/gmd:hierarchyLevelName//*"), //
    LANGUAGE("*/gmd:language//*"), //
    LINEAGE("*/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:LI_Lineage/gmd:statement//*"), //
    METADATA_CHARACTER_SET("*/gmd:characterSet/gmd:MD_ScopeCode/@codeListValue"), //
    METADATA_STANDARD_NAME("*/gmd:metadataStandardName//*"), //
    METADATA_STANDARD_VERSION("*/gmd:metadataStandardVersion//*"), //
    MODIFIED("*/gmd:dateStamp/*"), //
    ONLINE_RESOURCE(
	    "*/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"), //
    PARENT_IDENTIFIER("*/gmd:parentIdentifier//*"), //
    PUBLISHER(
	    "*/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName[gmd:role/gmd:CI_RoleCode/@codeListValue='publisher']//*"), //
    RESOURCE_IDENTIFIER("*/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code//*"), //
    RESOURCE_LANGUAGE("*/gmd:identificationInfo/*/gmd:language//*"), //
    REFERENCE_SYSTEM("*/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code//*"), //
    REVISION_DATE(
	    "*/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:DateTime"), //
    RIGHTS("*/gmd:identificationInfo/*/gmd:MD_LegalConstraints/gmd:accessConstraints/@codeListValue"), //
    SERVICE_OPERATION("*/gmd:identificationInfo/*/srv:containsOperations/srv:SV_OperationMetadata//*"), //
    SPATIAL_RESOLUTION(
	    "(*/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance|*/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance/@uom)"), //
    SPATIAL_REPRESENTATION_TYPE("*/gmd:identificationInfo/*/gmd:MD_SpatialRepresentationTypeCode/@codeListValue"), //
    TOPIC_CATEGORY("*/gmd:identificationInfo/*/gmd:topicCategory/gmd:MD_TopicCategoryCode");//

    /**
     * @return
     */
    public static GMD_ElementName[] getBriefSubset() {

	return new GMD_ElementName[] { //
		GMD_ElementName.BOUNDING_BOX, //
		GMD_ElementName.IDENTIFIER, //
		GMD_ElementName.GRAPHIC_OVERVIEW, //
		GMD_ElementName.SERVICE_TYPE, //
		GMD_ElementName.SERVICE_TYPE_VERSION, //
		GMD_ElementName.TITLE, //
		GMD_ElementName.TYPE };
    };

    /**
     * @return
     */
    public static GMD_ElementName[] getSummarySubset() {
	return new GMD_ElementName[] { //
		GMD_ElementName.ABSTRACT, //
		GMD_ElementName.CHARACTER_SET, //
		GMD_ElementName.CREATOR, //
		GMD_ElementName.CONTRIBUTOR, //
		GMD_ElementName.COUPLING_TYPE, //
		GMD_ElementName.BOUNDING_BOX, //
		GMD_ElementName.FORMAT, //
		GMD_ElementName.FORMAT_VERSION, //
		GMD_ElementName.GRAPHIC_OVERVIEW, //
		GMD_ElementName.HIERARCHY_LEVEL_NAME, //
		GMD_ElementName.IDENTIFIER, //
		GMD_ElementName.LANGUAGE, //
		GMD_ElementName.LINEAGE, //
		GMD_ElementName.METADATA_CHARACTER_SET, //
		GMD_ElementName.METADATA_STANDARD_NAME, //
		GMD_ElementName.METADATA_STANDARD_VERSION, //
		GMD_ElementName.MODIFIED, //
		GMD_ElementName.ONLINE_RESOURCE, //
		GMD_ElementName.PARENT_IDENTIFIER, //
		GMD_ElementName.PUBLISHER, //
		GMD_ElementName.RESOURCE_IDENTIFIER, //
		GMD_ElementName.RESOURCE_LANGUAGE, //
		GMD_ElementName.REFERENCE_SYSTEM, //
		GMD_ElementName.REVISION_DATE, //
		GMD_ElementName.RIGHTS, //
		GMD_ElementName.SERVICE_OPERATION, //
		GMD_ElementName.SERVICE_TYPE, //
		GMD_ElementName.SERVICE_TYPE_VERSION, //
		GMD_ElementName.SPATIAL_RESOLUTION, //
		GMD_ElementName.SPATIAL_REPRESENTATION_TYPE, //
		GMD_ElementName.TITLE, //
		GMD_ElementName.TOPIC_CATEGORY, //
		GMD_ElementName.TYPE };
    };

    /**
     * @param metadata
     * @param esn
     * @return
     * @throws Exception
     */
    public static Document subset(Document metadata, ElementSetType esn) throws Exception {

	GMD_ElementName[] properties;
	switch (esn) {
	case BRIEF:
	    properties = GMD_ElementName.getBriefSubset();
	    break;
	case SUMMARY:
	    properties = GMD_ElementName.getSummarySubset();
	    break;
	case FULL:
	default:
	    return metadata;
	}

	return subset(metadata, properties);
    }

    /**
     * 
     * @param metadata
     * @param properties
     * @return
     * @throws Exception
     */
    public static Document subset(Document metadata, GMD_ElementName[] properties) throws Exception {

	List<String> xpaths = new ArrayList<String>();
	for (GMD_ElementName property : properties) {
	    String mapping = property.getXPath();
	    xpaths.add(mapping);
	}

	XMLPruner pruner = new XMLPruner(xpaths, new CommonNameSpaceContext());
	Document ret = metadata;

	return pruner.prune(ret);
    }

    /**
     * @param name
     * @return
     */
    public static GMD_ElementName decode(String name) {

	name = name.toLowerCase();
	GMD_ElementName[] values = values();
	for (int i = 0; i < values.length; i++) {
	    GMD_ElementName set = values[i];
	    if (set.getName().toLowerCase().contains(name) || name.contains(set.getName().toLowerCase())) {
		return set;
	    }
	}
	return null;
    }

    private String xPath;

    private GMD_ElementName(String xPath) {

	this.xPath = xPath;
    }

    public String getName() {

	return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.name());
    }

    public String getXPath() {
	return xPath;
    }
}
