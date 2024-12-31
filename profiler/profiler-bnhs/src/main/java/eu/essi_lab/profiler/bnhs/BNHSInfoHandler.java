package eu.essi_lab.profiler.bnhs;

import java.util.Optional;
import java.util.Properties;

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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.WebRequestHandler;
import eu.essi_lab.pdk.validation.WebRequestValidator;

public class BNHSInfoHandler implements WebRequestHandler, WebRequestValidator {

    private String viewId;

    /**
     * 
     */
    public BNHSInfoHandler() {
    }

    /**
     * @param setting
     */
    public BNHSInfoHandler(WebRequest request) {

	viewId = BNHSProfiler.readViewId(request);
    }

    @Override
    public Response handle(WebRequest webRequest) throws GSException {

	ResponseBuilder builder = Response.status(Status.OK);
	String html = null;

	switch (viewId) {

	case "whos-arctic":

	    html = "<html><head><title>WHOS-broker: Arctic resources</title><body><h2>WHOS-broker Arctic page</h2><p>"
		    + "<a href='https://hydrohub.wmo.int/en/projects/Arctic-HYCOS'>Arctic-HYCOS page</a><br/><br/>" //
		    + "<a href='https://docs.google.com/spreadsheets/d/1ni9_BNcgoWD5HcU0sT_E20CwcOpOjsdek7_fQd4DWtI/edit?usp=sharing'>BNHS list - original Google Sheet</a> Click to open the station list as compiled by WMO experts<br/><br/>" //
		    + "<a href='bnhs/csv'>BNHS list - Broker modified CSV table</a> Click to download the same list as before, but augmented with links to real time station data, provided by WHOS-broker</p></body></html>";
	    break;

	case "whos":

	    html = "<html><head><title>WHOS resources</title><body><h2>WHOS-broker page</h2><p>"
		    + "<a href='https://hydrohub.wmo.int/en/projects/Arctic-HYCOS'>Arctic-HYCOS page</a><br/><br/>" //
		    + "<a href='https://docs.google.com/spreadsheets/d/1ni9_BNcgoWD5HcU0sT_E20CwcOpOjsdek7_fQd4DWtI/edit?usp=sharing'>BNHS list - original Google Sheet</a> Click to open the station list as compiled by WMO experts<br/><br/>" //
		    + "<a href='bnhs/csv'>BNHS list - Broker modified CSV table</a> Click to download the same list as before, but augmented with links to real time station data, provided by WHOS-broker</p></body></html>";
	    break;

	case "his-central":

	    html = "<html><head><title>HIS-Central resources</title><body><h2>HIS-Central page</h2><p>"
		    + "<a href='https://hydrohub.wmo.int/en/projects/Arctic-HYCOS'>Arctic-HYCOS page</a><br/><br/>" //
		    + "<a href='https://docs.google.com/spreadsheets/d/1ni9_BNcgoWD5HcU0sT_E20CwcOpOjsdek7_fQd4DWtI/edit?usp=sharing'>BNHS list - original Google Sheet</a> Click to open the station list as compiled by WMO experts<br/><br/>" //
		    + "<a href='bnhs/csv'>BNHS list - Broker modified CSV table</a> Click to download the same list as before, but augmented with links to real time station data, provided by WHOS-broker</p></body></html>";
	    break;
	}

	builder = builder.entity(html).type(new MediaType("text", "html"));

	return builder.build();
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

}
