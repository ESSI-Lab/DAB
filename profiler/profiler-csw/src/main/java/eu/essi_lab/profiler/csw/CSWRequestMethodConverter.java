package eu.essi_lab.profiler.csw;

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

import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.csw._2_0_2.DescribeRecord;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetName;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.jaxb.csw._2_0_2.GetCapabilities;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecordById;
import eu.essi_lab.jaxb.ows._1_0_0.AcceptFormatsType;
import eu.essi_lab.jaxb.ows._1_0_0.AcceptVersionsType;
import eu.essi_lab.jaxb.ows._1_0_0.SectionsType;
import eu.essi_lab.lib.utils.ClonableInputStream;

/**
 * 
 * @author Fabrizio
 *
 */
public class CSWRequestMethodConverter {

    public enum CSWRequest {

	GET_CAPABILITIES, //
	DESCRIBE_RECORD, //
	GET_RECORD_BY_ID//
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
