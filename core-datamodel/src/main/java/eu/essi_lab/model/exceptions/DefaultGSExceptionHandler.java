package eu.essi_lab.model.exceptions;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

public class DefaultGSExceptionHandler {

    private DefaultGSExceptionReader reader;

    public DefaultGSExceptionHandler(DefaultGSExceptionReader exReader) {

	reader = exReader;

    }

    public DefaultGSExceptionReader getReader() {

	return reader;

    }

    public String getErrorMessageForUser() {

	String msg = reader.getLastUserDescription();

	String code = createGSErrorCode();

	String message = msg + " (GI-suite Error Code: " + code + ")";

	return message;

    }

    public String createGSErrorCode() {

	List<ErrorIdentifier> ids = reader.getErrorIdentifiers();

	String code = "" + ids.size() + ";";

	for (ErrorIdentifier id : ids) {
	    code += id.getGSCode() + "";
	}

	return code;
    }

}
