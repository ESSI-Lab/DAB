package eu.essi_lab.shared.yellowpage;

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

import com.google.common.net.MediaType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.shared.SHARED_CONTENT_TYPES;
import eu.essi_lab.shared.model.SharedContent;
import eu.essi_lab.shared.model.SharedContentType;
import eu.essi_lab.shared.serializer.IGSScharedContentSerializer;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javax.xml.bind.JAXBException;
public class YPPageSerializer implements IGSScharedContentSerializer {

    private final SharedContentType type;
    private static final String YPSERIALIZER_UNMARSHLA_ERROR = "YPSERIALIZER_UNMARSHLA_ERROR";
    private static final String YPSERIALIZER_MARSHLA_ERROR = "YPSERIALIZER_MARSHLA_ERROR";

    public YPPageSerializer() {

	type = new SharedContentType();

	type.setType(SHARED_CONTENT_TYPES.GS_YELLOW_PAGE_TYPE);

    }

    @Override
    public InputStream toStream(SharedContent content) throws GSException {

	try {

	    return ((GSResource) content.getContent()).asStream();

	} catch (JAXBException | UnsupportedEncodingException e) {

	    GSLoggerFactory.getLogger(getClass()).warn("Can't serialize GSResource", e);

	    throw GSException.createException(getClass(), "Mmarshal error", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, YPSERIALIZER_MARSHLA_ERROR, e);

	}

    }

    @Override
    public SharedContent fromStream(InputStream stream) throws GSException {

	GSResource resource = null;

	try {

	    resource = GSResource.create(stream);

	} catch (JAXBException e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Can't deserialize GSResource", e);

	    throw GSException.createException(getClass(), "Unmarshal error", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, YPSERIALIZER_UNMARSHLA_ERROR, e);
	}

	SharedContent content = new SharedContent();

	content.setContent(resource);

	content.setType(type);

	content.setIdentifier(resource.getPrivateId());

	return content;
    }

    @Override
    public boolean supports(SharedContentType type, MediaType mediaType) {

	return type != null && SHARED_CONTENT_TYPES.GS_YELLOW_PAGE_TYPE.equals(type.getType()) && MediaType.XML_UTF_8.equals(mediaType);

    }

}
