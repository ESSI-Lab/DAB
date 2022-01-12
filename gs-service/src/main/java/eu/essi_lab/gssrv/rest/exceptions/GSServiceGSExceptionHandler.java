package eu.essi_lab.gssrv.rest.exceptions;

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

import javax.ws.rs.core.Response.Status;

import eu.essi_lab.gssrv.rest.ESSIAdminService;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.ErrorIdentifier;
import eu.essi_lab.model.exceptions.ErrorInfo;

public class GSServiceGSExceptionHandler {

    private DefaultGSExceptionReader reader;

    public GSServiceGSExceptionHandler(DefaultGSExceptionReader exReader) {

	reader = exReader;

    }

    public DefaultGSExceptionReader getReader() {

	return reader;

    }

    public GSErrorMessage getErrorMessageForUser() {

	String msg = reader.getLastUserDescription();

	String code = createGSErrorCode();

	GSErrorMessage gsmsg = new GSErrorMessage();

	gsmsg.setCode(code);
	gsmsg.setMessage(msg);

	return gsmsg;

    }

    public String createGSErrorCode() {

	List<ErrorIdentifier> ids = reader.getErrorIdentifiers();

	String code = "" + ids.size() + ";";

	for (ErrorIdentifier id : ids) {
	    code += id.getGSCode() + "";
	}

	return code;
    }

    public Status getStatus() {

	if (reader.getErrorType() == ErrorInfo.ERRORTYPE_INTERNAL)
	    return Status.INTERNAL_SERVER_ERROR;

	if (reader.getErrorType() == ErrorInfo.ERRORTYPE_CLIENT) {

	    String id = reader.getLastErrorIdentifier();

	    if (id != null && id.equalsIgnoreCase(ESSIAdminService.USER_NOT_AUTHRORIZED_FOR_ADMIN))
		return Status.UNAUTHORIZED;

	    return Status.BAD_REQUEST;
	}

	return Status.INTERNAL_SERVER_ERROR;
    }
}
