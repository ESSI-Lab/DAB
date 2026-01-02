package eu.essi_lab.profiler.wof.wsdl;

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

import javax.ws.rs.core.MediaType;

import org.apache.cxf.helpers.IOUtils;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;

/**
 * @author boldrini
 */
public class HISCentralWSDLHandler extends DefaultRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	WSDLRequestValidator validator = new WSDLRequestValidator();

	return validator.validate(request);
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	String ret = "<info>Reading WSDL from template</info>";
	try {
	    ret = IOUtils.readStringFromStream(HISCentralWSDLHandler.class.getClassLoader().getResourceAsStream("cuahsi/hiscentral.wsdl"));
	    String defaultCentral = "https://hiscentral.cuahsi.org/webservices/hiscentral.asmx";
	    ret = ret.replace(defaultCentral, webRequest.getServletRequest().getRequestURL());
	} catch (IOException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
	return ret;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return new MediaType("text", "xml", "UTF-8");
    }

}
