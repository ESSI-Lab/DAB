/**
 * 
 */
package eu.essi_lab.shared.serializer;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;

/**
 * @author Fabrizio
 */
public class JSONSerializer implements SharedContentSerializer {

    private static final String DESERIALIZATION_ERROR_EXCEPTION = "JSON_SERIALIZER_DESERIALIZATION_ERROR";

    @SuppressWarnings("rawtypes")
    @Override
    public InputStream toStream(SharedContent content) throws GSException {

	Object payload = content.getContent();

	if (payload instanceof JSONObject) {

	    JSONObject json = (JSONObject) payload;
	    return IOStreamUtils.asStream(json.toString());
	}

	return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public SharedContent fromStream(String contentIdentifier, InputStream stream) throws GSException {

	try {
	    String string = IOStreamUtils.asUTF8String(stream);

	    JSONObject object = new JSONObject(string);

	    SharedContent<Object> sharedContent = new SharedContent<>();

	    sharedContent.setContent(object);

	    sharedContent.setType(SharedContentType.JSON_TYPE);

	    return sharedContent;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DESERIALIZATION_ERROR_EXCEPTION, //
		    e);
	}
    }

    @Override
    public boolean supports(SharedContentType type) {

	return type != null && type == SharedContentType.JSON_TYPE;
    }
}
