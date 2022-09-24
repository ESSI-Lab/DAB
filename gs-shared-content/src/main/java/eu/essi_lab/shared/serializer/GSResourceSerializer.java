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
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;

/**
 * @author Fabrizio
 */
public class GSResourceSerializer implements SharedContentSerializer {

    private static final String UNMARSHAL_ERROR = "GS_RESOURCE_SERIALIZER_UNMARSHAL_ERROR";
    private static final String MARSHAL_ERROR = "GS_RESOURCE_SERIALIZER_MARSHAL_ERROR";

    public GSResourceSerializer() {

    }

    @SuppressWarnings("rawtypes")
    @Override
    public InputStream toStream(SharedContent content) throws GSException {

	try {

	    return ((GSResource) content.getContent()).asStream();

	} catch (JAXBException | UnsupportedEncodingException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARSHAL_ERROR, //
		    e);

	}
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public SharedContent fromStream(String contentIdentifier, InputStream stream) throws GSException {

	GSResource resource = null;

	try {

	    resource = GSResource.create(stream);

	} catch (JAXBException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    "Unmarshal error", //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, //
		    UNMARSHAL_ERROR, e);
	}

	SharedContent content = new SharedContent();

	content.setContent(resource);

	content.setType(SharedContentType.GS_RESOURCE_TYPE);

	content.setIdentifier(resource.getPrivateId());

	return content;
    }

    @Override
    public boolean supports(SharedContentType type) {

	return type != null && type == SharedContentType.GS_RESOURCE_TYPE;
    }
}
