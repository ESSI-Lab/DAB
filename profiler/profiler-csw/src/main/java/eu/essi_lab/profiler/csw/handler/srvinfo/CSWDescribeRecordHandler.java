package eu.essi_lab.profiler.csw.handler.srvinfo;

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

import javax.ws.rs.core.MediaType;

import com.google.common.io.ByteStreams;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.csw._2_0_2.DescribeRecordResponse;
import eu.essi_lab.jaxb.csw._2_0_2.SchemaComponentType;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.profiler.csw.CSWRequestConverter;
import eu.essi_lab.profiler.csw.CSWRequestConverter.CSWRequest;
import eu.essi_lab.profiler.csw.handler.discover.CSWRequestValidator;
import eu.essi_lab.profiler.csw.profile.CSWProfile;

/**
 * @author Fabrizio
 */
public class CSWDescribeRecordHandler extends DefaultRequestHandler {

    public CSWDescribeRecordHandler() {
    }

    /**
     * 
     */
    public static final List<String> SUPPORTED_SCHEMA_LANGUAGES = new ArrayList<>();
    static {

	// as from 10.6.4.4
	SUPPORTED_SCHEMA_LANGUAGES.add("XMLSCHEMA");
	// this are indeed shortcuts, added for schema compatibility
	SUPPORTED_SCHEMA_LANGUAGES.add("http://www.w3.org/XML/Schema");
	SUPPORTED_SCHEMA_LANGUAGES.add("http://www.w3.org/2001/XMLSchema");
    }

    private static final String CSW_DESCRIBE_RECORDS_ERROR = "CSW_DESCRIBE_RECORDS_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	CSWRequestValidator validator = new CSWRequestValidator();
	ValidationMessage validate = validator.validate(request);

	return validate;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	String out = null;

	try {

	    ClonableInputStream docStream = new ClonableInputStream(
		    getClass().getClassLoader().getResourceAsStream("templates/DescribeRecordResponse.xml"));
	    out = new String(ByteStreams.toByteArray(docStream.clone()));

	    String queryString = null;
	    if (webRequest.isGetRequest()) {
		queryString = webRequest.getURLDecodedQueryString().get();
	    } else {
		CSWRequestConverter converter = new CSWRequestConverter();
		queryString = converter.convert(CSWRequest.DESCRIBE_RECORD, webRequest.getBodyStream());
	    }

	    KeyValueParser parser = new KeyValueParser(queryString);
	    String typeName = parser.getDecodedValue("TypeName", true);

	    DescribeRecordResponse response = CommonContext.unmarshal(docStream.clone(), DescribeRecordResponse.class);
	    List<SchemaComponentType> schemaComponents = response.getSchemaComponents();

	    if (typeName != null) {

		String[] split = typeName.split(",");
		for (String name : split) {
		    SchemaComponentType component = CSWProfile.findSchemaComponent(name);
		    schemaComponents.add(component);
		}
		// srv:ServiceMetadata cannot be requested directly, and it is always added except when
		// the requested type name is the csw:Record
		if (!(split.length == 1 && split[0].equals("csw:Record"))) {
		    SchemaComponentType component = CSWProfile.findSchemaComponent("srv:ServiceMetadata");
		    schemaComponents.add(component);
		}
	    } else {

		List<SchemaComponentType> components = CSWProfile.getAllSupportedSchemaComponents();
		for (SchemaComponentType component : components) {
		    schemaComponents.add(component);
		}
	    }

	    out = CommonContext.asString(response, false);

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_DESCRIBE_RECORDS_ERROR, e);
	}

	return out;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.valueOf("text/xml;charset=UTF-8");
    }
}
