package eu.essi_lab.shared.serializer;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import eu.essi_lab.lib.utils.Base64Utils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;

/**
 * @author Fabrizio
 */
public class GenericSerializer implements SharedContentSerializer {

    /**
     * 
     */
    private static final String SERIALIZATION_ERROR_EXCEPTION = "DEFAULT_SERIALIZER_SERIALIZATION_ERROR_EXCEPTION";
    /**
     * 
     */
    private static final String DESERIALIZATION_ERROR_EXCEPTION = "DEFAULT_SERIALIZER_DESERIALIZATION_ERROR_EXCEPTION";

    @SuppressWarnings("rawtypes")
    @Override
    public InputStream toStream(SharedContent content) throws GSException {

	Object payload = content.getContent();

	if (payload instanceof File) {

	    try {
		return new FileInputStream((File) payload);

	    } catch (FileNotFoundException e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
	    }
	}

	if (payload instanceof Serializable) {

	    try {
		String encodedPayload = Base64Utils.encodeObject((Serializable) payload);

		return IOStreamUtils.asStream(encodedPayload);

	    } catch (IOException e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage());

		throw GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			SERIALIZATION_ERROR_EXCEPTION, //
			e);
	    }
	}

	return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public SharedContent fromStream(String contentIdentifier, InputStream stream) throws GSException {

	try {
	    String string = IOStreamUtils.asUTF8String(stream);

	    Object object = null;

	    if (new File(string).exists()) {

		object = new File(string);

	    } else {

		object = Base64Utils.decodeToObject(string);
	    }

	    SharedContent<Object> sharedContent = new SharedContent<>();

	    sharedContent.setContent(object);

	    sharedContent.setType(SharedContentType.GENERIC_TYPE);

	    return sharedContent;

	} catch (IOException e) {

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

	return type != null && type == SharedContentType.GENERIC_TYPE;
    }
}
