package eu.essi_lab.jobs.report;

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

import com.google.common.net.MediaType;

import eu.essi_lab.jobs.GSJobStatus;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.shared.SHARED_CONTENT_TYPES;
import eu.essi_lab.shared.model.SharedContent;
import eu.essi_lab.shared.model.SharedContentType;
import eu.essi_lab.shared.serializer.IGSScharedContentSerializer;
public class GSJobStatusSerializer implements IGSScharedContentSerializer {

    private final SharedContentType type;

    public GSJobStatusSerializer() {

	type = new SharedContentType();

	type.setType(SHARED_CONTENT_TYPES.GS_JOB_STATUS);

    }

    @Override
    public InputStream toStream(SharedContent content) throws GSException {

	return ((GSJobStatus) content.getContent()).serializeToInputStream();

    }

    @Override
    public SharedContent fromStream(InputStream stream) throws GSException {

	GSJobStatus status = new Deserializer().deserialize(stream, GSJobStatus.class);

	SharedContent content = new SharedContent();

	content.setContent(status);

	content.setType(type);

	content.setIdentifier(status.getExecutionid());

	return content;
    }

    @Override
    public boolean supports(SharedContentType type, MediaType mediaType) {

	return type != null && SHARED_CONTENT_TYPES.GS_JOB_STATUS.equals(type.getType()) && MediaType.JSON_UTF_8.equals(mediaType);

    }
}
