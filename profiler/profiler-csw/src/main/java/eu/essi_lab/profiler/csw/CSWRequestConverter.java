package eu.essi_lab.profiler.csw;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.jaxb.csw._2_0_2.DescribeRecord;
import eu.essi_lab.jaxb.csw._2_0_2.DistributedSearchType;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetName;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.jaxb.csw._2_0_2.GetCapabilities;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecordById;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.jaxb.csw._2_0_2.QueryType;
import eu.essi_lab.jaxb.csw._2_0_2.ResultType;
import eu.essi_lab.jaxb.filter._1_1_0.PropertyNameType;
import eu.essi_lab.jaxb.filter._1_1_0.SortByType;
import eu.essi_lab.jaxb.filter._1_1_0.SortOrderType;
import eu.essi_lab.jaxb.filter._1_1_0.SortPropertyType;
import eu.essi_lab.jaxb.ows._1_0_0.AcceptFormatsType;
import eu.essi_lab.jaxb.ows._1_0_0.AcceptVersionsType;
import eu.essi_lab.jaxb.ows._1_0_0.SectionsType;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;

/**
 * @author Fabrizio
 */
public class CSWRequestConverter {

    /**
     * @author Fabrizio
     */
    public enum CSWRequest {

	/**
	 * 
	 */
	GET_CAPABILITIES,
	/**
	 * 
	 */
	DESCRIBE_RECORD,
	/**
	 * 
	 */
	GET_RECORD_BY_ID,
	/**
	 * 
	 */
	GET_RECORD;

    }

    /**
     * @param request
     * @param stream
     * @return
     * @throws JAXBException
     */
    public String convert(CSWRequest request, ClonableInputStream stream) throws JAXBException {

	switch (request) {
	case DESCRIBE_RECORD:
	    return convertDescribeRecord(stream);
	case GET_CAPABILITIES:
	    return convertGetCapabilities(stream);
	default:
	case GET_RECORD_BY_ID:
	    return convertGetRecordById(stream);
	}
    }

    /**
     * Converts a GetRecords request GET in the related {@link GetRecords}
     * 
     * @param request
     * @return
     */
    public GetRecords convert(WebRequest request) throws IllegalArgumentException {

	GetRecords getRecords = new GetRecords();

	//
	//
	//

	String queryString = StringUtils.URLDecodeUTF8(request.getURLDecodedQueryString().get());

	KeyValueParser parser = new KeyValueParser(queryString);

	// --------------------
	// QueryType
	//

	QueryType queryType = new QueryType();

	JAXBElement<QueryType> query = ObjectFactories.CSW().createQuery(queryType);
	getRecords.setAbstractQuery(query);

	//
	// TypeNames
	//

	Optional<String> typeNames = parser.getOptionalValue("typeNames", false);

	if (typeNames.isPresent()) {

	    setTypeNames(typeNames.get(), queryType);
	}

	//
	// ElementNames or ElementSetName
	//

	Optional<String> elementNames = parser.getOptionalValue("elementName", true);
	if (elementNames.isPresent()) {

	    setElementNames(elementNames.get(), queryType);
	}

	Optional<String> elementSetName = parser.getOptionalValue("elementSetName", true);

	if (elementSetName.isPresent()) {

	    setElementSetName(elementSetName.get(), queryType);
	}

	//
	// SortBy
	//

	Optional<String> sortBy = parser.getOptionalValue("sortBy", false);

	if (sortBy.isPresent()) {

	    setSortBy(sortBy.get(), queryType);
	}

	//
	// Result type
	//

	Optional<String> resultType = parser.getOptionalValue("resultType", true);
	resultType.ifPresent(v -> getRecords.setResultType(ResultType.fromValue(v)));

	//
	// Start position
	//

	Optional<String> startPosition = parser.getOptionalValue("startPosition", true);
	startPosition.ifPresent(v -> getRecords.setStartPosition(new BigInteger(v)));

	//
	// Max records
	//

	Optional<String> maxRecords = parser.getOptionalValue("maxRecords", true);
	maxRecords.ifPresent(v -> getRecords.setMaxRecords(new BigInteger(v)));

	//
	// Output format
	//

	Optional<String> optionalValue = parser.getOptionalValue("outputFormat", true);
	optionalValue.ifPresent(v -> getRecords.setOutputFormat(v));

	//
	// Output schema
	//

	Optional<String> outputSchema = parser.getOptionalValue("outputSchema", true);
	outputSchema.ifPresent(v -> getRecords.setOutputSchema(v));

	//
	// Request id
	//

	Optional<String> requestId = parser.getOptionalValue("requestId", true);
	requestId.ifPresent(v -> getRecords.setRequestId(v));

	//
	// Distributed search
	//

	Optional<String> distributedSearch = parser.getOptionalValue("distributedSearch", true);
	if (distributedSearch.isPresent() && distributedSearch.get().equalsIgnoreCase("true")) {

	    DistributedSearchType distributedSearchType = new DistributedSearchType();

	    Optional<String> hopCount = parser.getOptionalValue("hopCount", true);
	    hopCount.ifPresent(v -> distributedSearchType.setHopCount(new BigInteger(v)));

	    getRecords.setDistributedSearch(distributedSearchType);
	}

	return getRecords;
    }

    /**
     * @param elementSetName
     * @param queryType
     */
    private void setElementSetName(String elementSetName, QueryType queryType) {

	ElementSetName name = new ElementSetName();

	//
	// As specification: 07-006r1
	// "In the case where the query includes more than one
	// entity name as the value of the typeName attribute on the Query element, the typeName
	// attribute on the ElementSetName element can be used to discriminate which element set
	// or sets should be presented. The names specified for the typeName attribute on the
	// ElementSetName element shall be a proper subset of the names specified as the value of
	// the typeName attribute on the Query element. If the typeName attribute is not included
	// on the ElementSetName element, then the named element sets for all entities specified
	// as the value of the typeName attribute on the Query element shall be presented".
	// Since I have no idea on how the type names are KVP encoded in the elementSetName parameter,
	// here I add all the type names from the queryType type names. As an alternative, we also avoid
	// to put the elementSetName type names
	//
	name.getTypeNames().addAll(queryType.getTypeNames());

	try {

	    name.setValue(ElementSetType.fromValue(elementSetName));

	} catch (IllegalArgumentException ex) {

	    throw new IllegalArgumentException("Unrecognize set name: " + elementSetName);
	}

	queryType.setElementSetName(name);
    }

    /**
     * @param elementNames
     * @param queryType
     */
    private void setElementNames(String elementNames, QueryType queryType) {

	Arrays.asList(elementNames.split(",")).//
		stream().//
		map(n -> fromString(n)).//
		forEach(q -> queryType.getElementNames().add(q));
    }

    /**
     * @param typeNames
     * @param queryType
     */
    private void setTypeNames(String typeNames, QueryType queryType) {

	Arrays.asList(typeNames.split(",")).//
		stream().//
		map(n -> fromString(n)).//
		forEach(q -> queryType.getTypeNames().add(q));
    }

    /**
     * @param qName
     * @return
     */
    private QName fromString(String qName) {

	String prefix = qName.trim().strip().split(":")[0];
	String localName = qName.trim().strip().split(":")[1];

	String nameSpaceUri = null;

	switch (prefix) {
	case "gmd":
	    nameSpaceUri = CommonNameSpaceContext.GMD_NS_URI;
	    break;
	case "gmi":
	    nameSpaceUri = CommonNameSpaceContext.GMI_NS_URI;
	    break;
	case "csw":
	    nameSpaceUri = CommonNameSpaceContext.CSW_NS_URI;
	    break;
	}

	return new QName(nameSpaceUri, localName, prefix);
    }

    /**
     * @param sortBy
     * @param queryType
     */
    private void setSortBy(String sortBy, QueryType queryType) {

	SortByType sortByType = new SortByType();
	queryType.setSortBy(sortByType);

	List<SortPropertyType> sortProperty = sortByType.getSortProperty();

	List<String> sortByElements = Arrays.asList(sortBy.split(","));

	sortByElements.forEach(el -> {

	    String[] split = el.trim().strip().split(":");
	    if (split.length != 2) {

		throw new IllegalArgumentException("SortBy element not valid: " + el);
	    }

	    //
	    // Metadata element
	    //

	    String elName = split[0];

	    SortPropertyType type = new SortPropertyType();

	    PropertyNameType propertyNameType = new PropertyNameType();
	    propertyNameType.getContent().add(elName);

	    type.setPropertyName(propertyNameType);

	    //
	    // Sort
	    //

	    String sort = split[1];

	    SortOrderType sortOrderType = null;
	    if (sort.equalsIgnoreCase("a")) {

		sortOrderType = SortOrderType.ASC;

	    } else if (sort.equalsIgnoreCase("d")) {

		sortOrderType = SortOrderType.DESC;

	    } else {

		throw new IllegalArgumentException("SortOrderType not valid: " + sort);
	    }

	    type.setSortOrder(sortOrderType);

	    //
	    //
	    //

	    sortProperty.add(type);
	});
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     */
    private String convertDescribeRecord(ClonableInputStream stream) throws JAXBException {

	String queryPart = "request=DescribeRecord&version=2.0.2&service=CSW";

	DescribeRecord dc = CommonContext.unmarshal(stream.clone(), DescribeRecord.class);

	String outputFormat = dc.getOutputFormat();
	if (outputFormat != null) {
	    queryPart += "&outputFormat=" + outputFormat;
	}

	String schemaLanguage = dc.getSchemaLanguage();
	if (schemaLanguage != null) {
	    queryPart += "&schemaLanguage=" + schemaLanguage;
	}

	List<QName> typeNames = dc.getTypeNames();
	if (!typeNames.isEmpty()) {

	    queryPart += "&TypeName=";

	    for (int i = 0; i < typeNames.size(); i++) {

		QName qName = typeNames.get(i);

		String prefix = qName.getPrefix();
		String localPart = qName.getLocalPart();

		if (!prefix.equals("")) {
		    queryPart += prefix + ":" + localPart;
		} else {
		    queryPart += localPart;
		}

		if (i < typeNames.size() - 1) {
		    queryPart += ',';
		}
	    }
	}

	return queryPart;
    }

    /**
     * @param unmarshal
     * @return
     * @throws JAXBException
     */
    private String convertGetCapabilities(ClonableInputStream stream) throws JAXBException {

	String queryPart = "request=GetCapabilities&version=2.0.2";

	GetCapabilities gc = CommonContext.unmarshal(stream.clone(), GetCapabilities.class);

	String service = gc.getService();
	if (service != null) {
	    queryPart += "&service=" + service;
	}

	SectionsType sections = gc.getSections();
	if (sections != null) {
	    List<String> sectionList = sections.getSection();

	    if (!sectionList.isEmpty()) {

		queryPart += "&Sections=";

		for (int i = 0; i < sectionList.size(); i++) {
		    queryPart += sectionList.get(i);

		    if (i < sectionList.size() - 1) {
			queryPart += ',';
		    }
		}
	    }
	}

	AcceptFormatsType acceptFormats = gc.getAcceptFormats();
	if (acceptFormats != null) {
	    List<String> formatsList = acceptFormats.getOutputFormat();

	    if (!formatsList.isEmpty()) {

		queryPart += "&AcceptFormats=";

		for (int i = 0; i < formatsList.size(); i++) {
		    queryPart += formatsList.get(i);

		    if (i < formatsList.size() - 1) {
			queryPart += ',';
		    }
		}
	    }
	}

	AcceptVersionsType acceptVersions = gc.getAcceptVersions();
	if (acceptVersions != null) {

	    List<String> versionsList = acceptVersions.getVersion();

	    if (!versionsList.isEmpty()) {

		queryPart += "&AcceptVersions=";

		for (int i = 0; i < versionsList.size(); i++) {
		    queryPart += versionsList.get(i);

		    if (i < versionsList.size() - 1) {
			queryPart += ',';
		    }
		}
	    }
	}

	return queryPart;
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     */
    private String convertGetRecordById(ClonableInputStream stream) throws JAXBException {

	String queryPart = "request=GetRecordById&version=2.0.2&service=CSW";

	GetRecordById gr = CommonContext.unmarshal(stream.clone(), GetRecordById.class);

	String outputFormat = gr.getOutputFormat();
	if (outputFormat != null) {
	    queryPart += "&outputFormat=" + outputFormat;
	}

	String outputSchema = gr.getOutputSchema();
	if (outputSchema != null) {
	    queryPart += "&outputSchema=" + outputSchema;
	}

	ElementSetName elementSetName = gr.getElementSetName();
	if (elementSetName != null) {
	    ElementSetType type = elementSetName.getValue();
	    if (type != null) {
		queryPart += "&ElementSetName=" + type.value();
	    }
	}

	List<String> ids = gr.getIds();
	if (!ids.isEmpty()) {

	    for (int i = 0; i < ids.size(); i++) {
		queryPart += "&id=" + ids.get(i);
		if (i < ids.size() - 1) {
		    queryPart += ",";
		}
	    }
	}

	return queryPart;
    }

}
