package eu.essi_lab.profiler.pubsub.handler.csw;

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

import java.io.StringReader;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;

import org.json.JSONObject;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.ogc.pubsub._1_0.UnsubscribeResponseType;
import eu.essi_lab.profiler.pubsub.handler.UnsubscribeHandler;

/**
 * @author Fabrizio
 */
public class CSWUnsubscribeHandler extends CSWSubscriptionsHandler {

    private static final String CSW_UNSUBSCRIBE_ERROR = "CSW_UNSUBSCRIBE_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	return message;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	UnsubscribeHandler handler = new UnsubscribeHandler();
	JSONObject object = new JSONObject(handler.getResponse(webRequest, getSubscriptionId(webRequest)));

	String out = null;

	try {
	    String message = "<gco:CharacterString xmlns:gco=\"http://www.isotc211.org/2005/gco\">" + object.getString("message")
		    + "</gco:CharacterString>";
	    Element extension = createExtension(message);

	    UnsubscribeResponseType responseType = ObjectFactories.PUB_SUB().createUnsubscribeResponseType();
	    responseType.getExtension().add(extension);

	    JAXBElement<UnsubscribeResponseType> response = ObjectFactories.PUB_SUB().createUnsubscribeResponse(responseType);

	    out = CommonContext.asString(response, false);

	} catch (Exception e) {

	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    e.getMessage(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_UNSUBSCRIBE_ERROR);
	}

	return out;
    }

    private Element createExtension(String message) throws Exception {

	DocumentBuilder builder = XMLFactories.newDocumentBuilderFactory().newDocumentBuilder();
	InputSource is = new InputSource();
	is.setCharacterStream(new StringReader(message));
	return builder.parse(is).getDocumentElement();
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_XML_TYPE;
    }

}
