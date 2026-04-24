package eu.essi_lab.profiler.geodcat;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.json.JSONObject;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;

/**
 * Formats a single discovered record as a GeoDCAT-oriented JSON-LD dataset description.
 */
public class GeoDcatDatasetJsonLdFormatter extends DiscoveryResultSetFormatter<GSResource> {

    public static final String ENCODING_NAME = "geodcat-dataset-jsonld";
    public static final String ENCODING_VERSION = "1.0";

    public static final FormattingEncoding FORMATTING_ENCODING = new FormattingEncoding();

    static {
	FORMATTING_ENCODING.setEncoding(ENCODING_NAME);
	FORMATTING_ENCODING.setEncodingVersion(ENCODING_VERSION);
	FORMATTING_ENCODING.setMediaType(new MediaType("application", "ld+json"));
    }

    @Override
    public Response format(DiscoveryMessage message, ResultSet<GSResource> resultSet) throws GSException {

	List<GSResource> resources = resultSet.getResultsList();
	if (resources == null || resources.isEmpty() || resources.get(0) == null) {

	    return Response.status(Status.NOT_FOUND).type(GeoDcatJsonLd.MEDIA_TYPE).entity("{}").build();
	}

	GSResource resource = resources.get(0);
	JSONObject doc = GeoDcatJsonLd.datasetDescription(message.getWebRequest(), resource);

	return Response.status(Status.OK).type(GeoDcatJsonLd.MEDIA_TYPE).entity(doc.toString(2)).build();
    }

    @Override
    public FormattingEncoding getEncoding() {

	return FORMATTING_ENCODING;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
