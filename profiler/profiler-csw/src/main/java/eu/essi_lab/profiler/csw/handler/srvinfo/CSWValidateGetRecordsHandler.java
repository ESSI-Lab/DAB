package eu.essi_lab.profiler.csw.handler.srvinfo;

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

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.csw._2_0_2.Acknowledgement;
import eu.essi_lab.jaxb.csw._2_0_2.EchoedRequestType;
import eu.essi_lab.jaxb.ows._1_0_0.ExceptionReport;
import eu.essi_lab.lib.utils.XMLGregorianCalendarUtils;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.profiler.csw.CSWProfiler;
import eu.essi_lab.profiler.csw.handler.discover.CSWRequestValidator;

/**
 * @author Fabrizio
 */
public class CSWValidateGetRecordsHandler extends DefaultRequestHandler {

    private static final String CSW_VALIDATE_GET_RECORDS_HANDLER_ERROR = "CSW_VALIDATE_GET_RECORDS_HANDLER_ERROR";
 
    public CSWValidateGetRecordsHandler() {
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	return message;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	try {

	    CSWRequestValidator validator = new CSWRequestValidator();
	    ValidationMessage message = validator.validate(webRequest);
	    Acknowledgement acknowledgement = new Acknowledgement();
	    acknowledgement.setTimeStamp(XMLGregorianCalendarUtils.createGregorianCalendar());

	    EchoedRequestType requestType = new EchoedRequestType();

	    if (message.getResult() == ValidationResult.VALIDATION_SUCCESSFUL) {

		DocumentBuilderFactory factory = XMLFactories.newDocumentBuilderFactory();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(webRequest.getBodyStream().clone());
		requestType.setAny(doc.getDocumentElement());

		acknowledgement.setEchoedRequest(requestType);

		return CommonContext.asString(acknowledgement, false);

	    } else {

		ExceptionReport report = CSWProfiler.createExceptionReport(message);

		return CommonContext.asString(report, false);
	    }
	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_VALIDATE_GET_RECORDS_HANDLER_ERROR, e);
	}
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.valueOf("text/xml;charset=UTF-8");
    }

}
