package eu.essi_lab.gssrv.conf.task.bluecloud;

import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

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

public enum BlueCloudMetadataElement {
    IDENTIFIER(MetadataElement.IDENTIFIER, "//gmd:fileIdentifier/gco:CharacterString"), //
    TITLE(MetadataElement.TITLE,
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/*[1]"), //
    KEYWORD(MetadataElement.KEYWORD,
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[not(gmd:type) or not(contains('platform parameter instrument cruise project',gmd:type/gmd:MD_KeywordTypeCode/@codeListValue))]/gmd:keyword/*[1]"), //
    KEYWORD_URI(MetadataElement.KEYWORD_URI,
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[not(gmd:type) or not(contains('platform parameter instrument cruise project',gmd:type/gmd:MD_KeywordTypeCode/@codeListValue))]/gmd:keyword/*[1]/@xlink:href"), //
    KEYWORD_TYPE(MetadataElement.KEYWORD_TYPE,
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[not(contains('platform parameter instrument cruise project',gmd:type/gmd:MD_KeywordTypeCode/@codeListValue))]/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue"), //
    BOUNDING_BOX(MetadataElement.BOUNDING_BOX,
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal", //
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal", //
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal", //
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal"

    ), //
    TEMPORAL_EXTENT(MetadataElement.TEMP_EXTENT_BEGIN,
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:beginPosition", //
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:endPosition"), //
    PARAMETER(MetadataElement.ATTRIBUTE_TITLE,
	    "/gmi2019:MI_Metadata/gmd:contentInfo/gmi2019:MI_CoverageDescription/gmd:attributeDescription/gco:RecordType"), //
    PARAMETER_URI(MetadataElement.OBSERVED_PROPERTY_URI,
	    "/gmi2019:MI_Metadata/gmd:contentInfo/gmi2019:MI_CoverageDescription/gmd:attributeDescription/gco:RecordType/@xlink:href"), //
    INSTRUMENT(MetadataElement.INSTRUMENT_TITLE,
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='instrument']/gmd:keyword/*[1]"), //
    INSTRUMENT_URI(MetadataElement.INSTRUMENT_URI,
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='instrument']/gmd:keyword/*[1]/@xlink:href"), //
    PLATFORM(MetadataElement.PLATFORM_TITLE,
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='platform']/gmd:keyword/*[1]"), //
    PLATFORM_URI(MetadataElement.PLATFORM_URI,
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='platform']/gmd:keyword/*[1]/@xlink:href"), //
    ORGANIZATION(MetadataElement.ORGANISATION_NAME, "//gmd:CI_ResponsibleParty/gmd:organisationName/*[1]"), //
    ORGANIZATION_ROLE(MetadataElement.ORGANISATION_ROLE, "//gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue"), //
    ORGANIZATION_URI(MetadataElement.ORGANISATION_URI, "//gmd:CI_ResponsibleParty/gmd:organisationName/*[1]/@xlink:href"), //
    CRUISE(MetadataElement.CRUISE_NAME,
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='cruise']/gmd:keyword/*[1]"), //
    CRUISE_URI(MetadataElement.CRUISE_URI,
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='cruise']/gmd:keyword/*[1]/@xlink:href"), //
    PROJECT(MetadataElement.PROJECT_NAME,
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='project']/gmd:keyword/*[1]"), //
    PROJECT_URI(MetadataElement.PROJECT_URI,
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='project']/gmd:keyword/*[1]/@xlink:href"), //
    DATESTAMP(MetadataElement.DATE_STAMP, "/gmi2019:MI_Metadata/gmd:dateStamp/gco:Date"), //
    REVISION_DATE(MetadataElement.REVISION_DATE,
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date"), //
    RESOURCE_IDENTIFIER(MetadataElement.RESOURCE_IDENTIFIER,
	    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString"), LICENSE_USE_LIMITAION(
		    null,
		    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation/gco:CharacterString"), LICENSE_ACCESS_CONSTRAINTS(
			    null,
			    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue"), LICENSE_USE_CONSTRAINTS(
				    null,
				    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useConstraints/gmd:MD_RestrictionCode/@codeListValue"), LICENSE_OTHER_CONSTRAINTS(
					    null,
					    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString");

    private BlueCloudMetadataElement(MetadataElement queryable, String... path) {
	this.queryable = queryable;
	this.paths = path;
    }

    private MetadataElement queryable;

    public MetadataElement getQueryable() {
	return queryable;
    }

    private String[] paths;

    public String[] getPaths() {
	return paths;
    }

    public String getPathHtml() {
	String ret = "";
	for (String path : paths) {
	    ret += path + "\n";
	}
	return ret;
    }

}
