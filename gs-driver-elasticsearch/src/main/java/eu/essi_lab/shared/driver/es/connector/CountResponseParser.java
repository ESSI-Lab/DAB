package eu.essi_lab.shared.driver.es.connector;

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

import org.json.JSONException;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author ilsanto
 */
public class CountResponseParser extends JSONDocumentParser {

    private static final String COUNT_KEY = "count";
    private static final String IOEXCEPTION_PARSING_ES_COUNT = "IOEXCEPTION_PARSING_ES_COUNT";

    public CountResponseParser(String jsonString) throws GSException {
	super(jsonString);
    }

    public Long getCount() throws GSException {

	try {

	    return jsonObject.getLong("count");

	} catch (JSONException e) {
	    
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    
	    throw GSException.createException(//
		    getClass(),//
		    e.getMessage(),//
		    null,//
		    ErrorInfo.ERRORTYPE_SERVICE,//
		    ErrorInfo.SEVERITY_ERROR,//
		    IOEXCEPTION_PARSING_ES_COUNT,//
		    e);
	}
    }
}
