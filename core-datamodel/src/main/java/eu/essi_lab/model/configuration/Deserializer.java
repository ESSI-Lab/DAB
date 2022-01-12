package eu.essi_lab.model.configuration;

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

import java.io.InputStream;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class Deserializer {

    private static final String ERR_ID_DESERIALIZATION = "ERR_ID_DESERIALIZATION";

    private transient Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public <T> T deserialize(String serialized, Class<T> valueType) throws GSException {
	try {

	    JsonNode node = new ObjectMapper().reader().forType(valueType).readTree(serialized);

	    T parsed = new ObjectMapper().treeToValue(node, valueType);

	    return parsed;

	} catch (Exception e) {

	    throw handleDeserializeException(e);

	}
    }

    private GSException handleDeserializeException(Exception e) {	Throwable cause = e.getCause();

	if (cause != null && GSException.class.isAssignableFrom(cause.getClass()))
	    return (GSException) e.getCause();

	logger.error("Error during  deserialization: " + e.getMessage());

	if (e instanceof InvalidFormatException) {
	    GSException ex = new GSException();

	    ErrorInfo ei = new ErrorInfo();

	    ei.setContextId(this.getClass().getName());
	    ei.setErrorId(ERR_ID_DESERIALIZATION);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    ei.setErrorDescription(e.getMessage());

	    ex.addInfo(ei);
	    return ex;
	}

	GSException ex = new GSException();

	ErrorInfo ei = new ErrorInfo();

	ei.setCause(e);

	ei.setContextId(this.getClass().getName());
	ei.setErrorId(ERR_ID_DESERIALIZATION);
	ei.setErrorType(ErrorInfo.ERRORTYPE_INTERNAL);

	ex.addInfo(ei);
	return ex;
    }

    public <T> T deserialize(InputStream serialized, Class<T> valueType) throws GSException {
	try {

	    JsonNode node = new ObjectMapper().reader().forType(valueType).readTree(serialized);

	    T parsed = new ObjectMapper().treeToValue(node, valueType);

	    return parsed;

	} catch (Exception e) {

	    throw handleDeserializeException(e);

	}
    }
}
