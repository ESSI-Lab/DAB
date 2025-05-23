package eu.essi_lab.accessor.eurobis.ld;

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

/**
 * @author boldrini
 */
public enum RDFElement {

    IDENTIFIER(false, false, getSimpleQueryTemplate("dct:identifier")), //
    TITLE(false, false, getSimpleQueryTemplate("dct:title")), //    
    ALTERNATENAME(false, false, getSimpleQueryTemplate("schema:alternateName")), //
    ABSTRACT(false, false, getSimpleQueryTemplate("dct:abstract")), //
    CREATED(false, false, getSimpleQueryTemplate("dct:created")), //
    MODIFIED(false, false, getSimpleQueryTemplate("dct:modified")), //
    ISPARTOF(true, false, getMultipleQueryTemplate("dct:isPartOf")), //
    HASPART(true, false, getMultipleQueryTemplate("dct:hasPart")), //
    LICENSE(false, false, getSimpleQueryTemplate("dct:license")), //
    CITATIONS(true, false, getMultipleQueryTemplate("dc:bibliographicCitation")), //
    STARTDATE(false, false, getTemporalMinQuery()), //
    ENDDATE(false, false, getTemporalMaxQuery()), //
    ENDDATEINPROGRESS(false, false, getTemporalInProgressQuery()), //
    BBOX(false, false, getSpatialQueryTemplate("dcat:bbox")), //
    THEMES(true, false, getMultipleQueryTemplate("dcat:theme")), //
    KEYWORDS(true, false, getMultipleLiteralQueryTemplate("schema:keywords")), //
    KEYWORDLABELSANDURISANDTYPES(true, true, getKeywordsURIQuery()), //
    PARAMETERS(true, false, getParametersQuery()), //
    PARAMETERLABELSANDURIS(true, true, getParametersURIQuery()), //
    INSTRUMENTS(true, false, getInstrumentsQuery()), //
    INSTRUMENTLABELSANDURIS(true, true, getInstrumentsURIQuery()), //
    URLS_AND_TYPES(true, true, getDistributionsQuery()), //
    CREATORS(true, true, getCreatorURIQuery()), //
    //
    ORGNAME(false, false, getSimpleQueryTemplate("schema:Organization", "schema:name")), //
    ORGALTERNATENAME(false, false, getSimpleQueryTemplate("schema:Organization", "schema:alternateName")), //
    ORGADDRESS(false, false, getSimpleQueryTemplate("schema:Organization", "schema:address")), //
    ORGTELEPHONES(true, false, getTelephoneTemplate("vcard:hasValue")), //
    ORGEMAILS(true, false, getMultipleQueryTemplate("schema:Organization", "vcard:hasEmail")), //
    ORGWEBPAGES(true, false, getMultipleQueryTemplate("schema:Organization", "schema:mainEntityOfPage")), //

    ORGCREATED(false, false, getSimpleQueryTemplate("schema:Organization", "dct:created")), //
    ORGMODIFIED(false, false, getSimpleQueryTemplate("schema:Organization", "dct:modified")), //

    ;

    String query;
    boolean multiple;
    boolean list;

    public boolean isList() {
	return list;
    }

    public boolean isMultiple() {
	return multiple;
    }

    private static final String PREFIXES = "PREFIX dcat: <http://www.w3.org/ns/dcat#> \n" + //
	    "PREFIX mi: <http://www.marineinfo.org/ns/ontology#> \n" + //
	    "PREFIX dct: <http://purl.org/dc/terms/> \n" + //
	    "PREFIX vcard: <http://www.w3.org/2006/vcard/ns#> \n" + //
	    "PREFIX dc: <http://purl.org/dc/elements/1.1/> \n" + //
	    "PREFIX schema: <https://schema.org/> \n"; //

    private static final String VAR_PLACEHOLDER = "{VARNAME}";

    private static String getSimpleQueryTemplate(String elementName) {
	return getSimpleQueryTemplate("dcat:Dataset", elementName);
    }

    private static String getSimpleQueryTemplate(String type, String elementName) {
	return getSimpleQueryHeader() + //
		" WHERE { ?res a " + type + "; \n" + //
		" OPTIONAL { ?res " + elementName + " ?" + VAR_PLACEHOLDER + "}}";
    }

    private static String getSimpleQueryHeader() {
	return PREFIXES + "SELECT ?" + VAR_PLACEHOLDER;
    }

    private static String getMultipleQueryHeader() {
	return PREFIXES + "SELECT (GROUP_CONCAT(DISTINCT ?" + VAR_PLACEHOLDER + "1; SEPARATOR='" + RDFResource.SEPARATOR1 + "') AS ?"
		+ VAR_PLACEHOLDER + ")";
    }

    private static String getTelephoneTemplate(String elementName) {
	return getMultipleQueryHeader() + //
		" WHERE { ?resource a dcat:Resource; \n" + //
		"  OPTIONAL {\n" + //
		"    ?resource vcard:hasTelephone ?telephone .\n" + //
		"    ?telephone " + elementName + "  ?" + VAR_PLACEHOLDER + "1" + //
		"  }}";
    }

    private static String getMultipleQueryTemplate(String elementName) {
	return getMultipleQueryTemplate("dcat:Dataset", elementName);
    }

    private static String getMultipleQueryTemplate(String type, String elementName) {
	return getMultipleQueryHeader() + //
		" WHERE { ?dataset a " + type + "; \n" + //
		" OPTIONAL { ?dataset " + elementName + " ?" + VAR_PLACEHOLDER + "1}}";
    }

    private static String getMultipleLiteralQueryTemplate(String elementName) {
	return getMultipleQueryHeader() + //
		" WHERE { ?dataset a dcat:Dataset; \n" + //
		" OPTIONAL { ?dataset " + elementName + " ?" + VAR_PLACEHOLDER + "1 .\n" + //
		"FILTER(isLiteral(?" + VAR_PLACEHOLDER + "1))" + "}}";
    }

    private static String getTemporalMinQuery() {
	return PREFIXES + "SELECT (MIN(?start) AS ?" + VAR_PLACEHOLDER + ")" + //
		" WHERE { ?dataset a dcat:Dataset; \n" + //
		" OPTIONAL { ?dataset dct:temporal ?temporal . \n" + //
		" OPTIONAL { ?temporal dcat:startDate ?start} \n" + //
		"}}";
    }

    private static String getTemporalMaxQuery() {
	return PREFIXES + "SELECT (MAX(?end) AS ?" + VAR_PLACEHOLDER + ")" + //
		" WHERE { ?dataset a dcat:Dataset; \n" + //
		" OPTIONAL { ?dataset dct:temporal ?temporal . \n" + //
		" OPTIONAL { ?temporal dcat:endDate ?end} \n" + //
		"}}";
    }

    private static String getTemporalInProgressQuery() {
	return getSimpleQueryHeader() + //
		" WHERE { ?dataset a dcat:Dataset; \n" + //
		" OPTIONAL { ?dataset dct:temporal ?temporal . \n" + //
		" OPTIONAL { ?res mi:progress  ?" + VAR_PLACEHOLDER + "}}}";

    }

    private static String getSpatialQueryTemplate(String elementName) {
	return getSimpleQueryHeader() + //
		" WHERE { ?dataset a dcat:Dataset; \n" + //
		" OPTIONAL { ?dataset dct:spatial ?spatial . \n" + //
		" OPTIONAL { ?spatial " + elementName + " ?" + VAR_PLACEHOLDER + "} \n" + //
		"}}";
    }

    private static String getParametersQuery() {
	return getMultipleQueryHeader() + //
		" WHERE { ?dataset a dcat:Dataset; \n" + //
		"  OPTIONAL {\n" + //
		"    ?dataset schema:variableMeasured ?definedVariable .\n" + //
		"    ?definedVariable a schema:PropertyValue .\n" + //
		"    ?definedVariable schema:name ?" + VAR_PLACEHOLDER + "1 \n" + //
		"  }" + //
		"  }"; //
    }

    private static String getKeywordsURIQuery() {
	return getMultipleQueryHeader() + //

		" WHERE { ?dataset a dcat:Dataset; \n" + //
		"  OPTIONAL {\n" + //
		"    ?dataset schema:keywords ?definedTerm .\n" + //
		"    ?definedTerm a schema:DefinedTerm ;\n" + //
		"                OPTIONAL{ ?definedTerm schema:name ?keywordlabel }\n" + //
		"                OPTIONAL{ ?definedTerm schema:name ?keywordlabel . " + //
		" ?definedTerm schema:identifier ?keyworduri . " + //
		"}\n" + //
		"                OPTIONAL{ ?definedTerm schema:inDefinedTermSet ?schemak. " + //
		" 					?schemak schema:name ?keywordtype" + //
		"		}\n" + //
		"    BIND(CONCAT(COALESCE(str(?keywordlabel),''), '" + RDFResource.SEPARATOR2 + "', COALESCE(str(?keyworduri),''), '"
		+ RDFResource.SEPARATOR2 + "', COALESCE(str(?keywordtype),'')) AS ?" + VAR_PLACEHOLDER + "1)" + //
		"  }}"; //
    }

    private static String getParametersURIQuery() {
	return getMultipleQueryHeader() + //

		" WHERE { ?dataset a dcat:Dataset; \n" + //
		"  OPTIONAL {\n" + //
		"    ?dataset schema:variableMeasured ?definedVariable .\n" + //
		"    ?definedVariable a schema:PropertyValue .\n" + //
		"               ?definedVariable schema:name       ?parameterlabel .  \n" + //
		"               ?definedVariable schema:identifier ?parameteruri . \n" + //
		"    BIND(CONCAT(str(?parameterlabel), '" + RDFResource.SEPARATOR2 + "', str(?parameteruri)) AS ?" + VAR_PLACEHOLDER + "1)"
		+ //
		"  }}";
    }
    
    private static String getCreatorURIQuery() {
 	return getMultipleQueryHeader() + //

 		" WHERE { ?dataset a dcat:Dataset; \n" + //
 		"  OPTIONAL {\n" + //
 		"    ?dataset dct:creator ?creator.\n" + //
 		"    ?creator a schema:Organization .\n" + //
		"                OPTIONAL{?creator schema:identifier ?" + VAR_PLACEHOLDER + "1 }\n" + // 		
 		"  }}";
     }

    private static String getInstrumentsQuery() {
	return getMultipleQueryHeader() + //

		" WHERE { ?dataset a dcat:Dataset; \n" + //
		"  OPTIONAL {\n" + //
		"    ?dataset schema:instrument ?definedInstrument .\n" + //
		"    ?definedInstrument a schema:Thing .\n" + //
		"                OPTIONAL{?definedInstrument schema:name       ?" + VAR_PLACEHOLDER + "1 }\n" + //
		"  }}";
    }

    private static String getInstrumentsURIQuery() {
	return getMultipleQueryHeader() + //

		" WHERE { ?dataset a dcat:Dataset; \n" + //
		"  OPTIONAL {\n" + //
		"    ?dataset schema:instrument ?definedInstrument .\n" + //
		"    ?definedInstrument a schema:Thing .\n" + //
		"                OPTIONAL{?definedInstrument schema:name       ?instrumentlabel . " + //
		"                         ?definedInstrument schema:identifier ?instrumenturi ." + //
		"    BIND(CONCAT(str(?instrumentlabel), '" + RDFResource.SEPARATOR2 + "', str(?instrumenturi)) AS ?" + VAR_PLACEHOLDER
		+ "1)" + //
		"}\n" + //
		"  }}";
    }

    private static String getDistributionsQuery() {
	return getMultipleQueryHeader() + //

		" WHERE { ?dataset a dcat:Dataset; \n" + //
		"  OPTIONAL {\n" + //
		"    ?dataset dcat:distribution ?distribution .\n" + //
		"    ?distribution dcat:accessURL ?url . \n" + //
		"    ?distribution dct:type ?type . \n" + //
		"    BIND(CONCAT(str(?url), '" + RDFResource.SEPARATOR2 + "', str(?type)) AS ?" + VAR_PLACEHOLDER + "1)" + //
		"  }}";
    }

    private RDFElement(boolean multiple, boolean list, String queryTemplate) {
	this.multiple = multiple;
	this.list = list;
	query = queryTemplate.replace(VAR_PLACEHOLDER, name());
    }

    public static RDFElement decode(String type) {
	for (RDFElement element : values()) {
	    if (element.name().toLowerCase().equals(type.toLowerCase())) {
		return element;
	    }
	}
	return null;
    }

    public String getQuery() {
	return query;
    }

}
