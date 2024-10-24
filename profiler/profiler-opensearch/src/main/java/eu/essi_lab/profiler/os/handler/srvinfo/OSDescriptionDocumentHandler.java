package eu.essi_lab.profiler.os.handler.srvinfo;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.io.ByteStreams;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.pdk.wrt.WebRequestParameter;
import eu.essi_lab.profiler.os.OSParameter;
import eu.essi_lab.profiler.os.OSParameters;

/**
 * This handler is in charge to present the description document; it is selected by the
 * {@link OSGetDescriptionDocFilter}. Example URL: <a href=
 * "http://localhost:8085/gs-service/services/essi/opensearch?getDescriptionDocument">http://localhost:8085/gs-service/services/essi/opensearch?getDescriptionDocument</a>
 * 
 * @see OSGetDescriptionDocFilter
 * @author Fabrizio
 */
public class OSDescriptionDocumentHandler extends DefaultRequestHandler {

    private static final String OS_DESCRIPTION_DOC_ERROR = "OS_DESCRIPTION_DOC_ERROR";
    private static final String QUERY_PATH = "/query";

    private enum DocTokens {
	/**
	 * 
	 */
	PARAMETERS("PARAMETERS"),
	/**
	 * 
	 */
	SHORT_NAME("SHORT_NAME", "Short name"),
	/**
	 * 
	 */
	LONG_NAME("LONG_NAME", "Long name"),
	/**
	 * 
	 */
	DESCRIPTION("DESCRIPTION", "Description"),
	/**
	 * 
	 */
	TEMPLATE_URL("TEMPLATE_URL");

	private String paramName;
	private String paramValue;

	private DocTokens(String name) {

	    this(name, null);
	}

	private DocTokens(String name, String value) {
	    this.paramName = name;
	    this.paramValue = value;
	}

	public String getName() {
	    return paramName;
	}

	public String getValue() {
	    return paramValue;
	}
    }

    /**
     * Returns a {@link Response} having the description doc as {@link String} entity with "text/xml;charset=UTF-8"
     * media type
     * 
     * @throws GSException
     */
    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	try {

	    InputStream docStream = getClass().getClassLoader().getResourceAsStream("descriptionDoc.xml");

	    String doc = new String(ByteStreams.toByteArray(docStream));
	    String params = "";

	    List<OSParameter> parameters = WebRequestParameter.findParameters(OSParameters.class);

	    for (OSParameter param : parameters) {

		String paramName = param.getName();
		String paramValue = param.getTemplateValue();

		if (param.isReplaceable()) {
		    params += "<parameters:Parameter name=\"" + paramName + "\" value=\"" + paramValue + "\"/>\n";
		}
	    }

	    doc = doc.replace(DocTokens.PARAMETERS.getName(), params);
	    doc = doc.replace(DocTokens.SHORT_NAME.getName(), DocTokens.SHORT_NAME.getValue());
	    doc = doc.replace(DocTokens.LONG_NAME.getName(), DocTokens.LONG_NAME.getValue());
	    doc = doc.replace(DocTokens.DESCRIPTION.getName(), DocTokens.DESCRIPTION.getValue());
	    String queryURL = webRequest.getUriInfo().getAbsolutePath().toString();
	    int lastSlash = queryURL.lastIndexOf("/");
	    queryURL = queryURL.substring(0, lastSlash) + QUERY_PATH;

	    doc = doc.replace(DocTokens.TEMPLATE_URL.getName(), queryURL);

	    return doc;

	} catch (IOException e) {
	    throw GSException.createException(getClass(), e.getMessage(), null, ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR,
		    OS_DESCRIPTION_DOC_ERROR, e);
	}
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	return message;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.valueOf("text/xml;charset=UTF-8");
    }

}
