/**
 * 
 */
package eu.essi_lab.pdk;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.jaxrs.utils.HttpUtils;

import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.rip.RuntimeInfoProvider;

/**
 * @author Fabrizio
 */
public class ResponseInfoProvider implements RuntimeInfoProvider {

    @Override
    public String getBaseType() {

	return "Response";
    }

    private Response response;

    /**
     * @param response
     */
    public ResponseInfoProvider(Response response) {

	this.response = response;
    }

    private List<String> toListOfStrings(String headerName, List<Object> values) {
	if (values == null) {
	    return null;
	} else {
	    List<String> stringValues = new ArrayList<String>(values.size());
	    HeaderDelegate<Object> hd = HttpUtils.getHeaderDelegate(values.get(0));
	    for (Object value : values) {
		String actualValue = hd == null ? value.toString() : hd.toString(value);
		stringValues.add(actualValue);
	    }
	    return stringValues;
	}
    }

    @Override
    public HashMap<String, List<String>> provideInfo() {

	HashMap<String, List<String>> out = new HashMap<>();

	MultivaluedMap<String, Object> metadata = new MetadataMap<>(response.getMetadata());
	MetadataMap<String, String> headers = new MetadataMap<>(metadata.size());
	for (Map.Entry<String, List<Object>> entry : metadata.entrySet()) {
	    String headerName = entry.getKey();
	    headers.put(headerName, toListOfStrings(headerName, entry.getValue()));
	}

	//
	//
	// these headers have at the moment no related statistical element
	//
	//
	headers.forEach((k, v) -> out.put(getName() + RuntimeInfoElement.NAME_SEPARATOR + k, v));
	// headers.keySet().forEach(key -> out.put(getName() + RuntimeInfoElement.NAME_SEPARATOR + key,
	// headers.get(key)));

	int length = response.getLength();
	out.put(RuntimeInfoElement.RESPONSE_LENGTH.getName(), Arrays.asList(String.valueOf(length)));

	MediaType mediaType = response.getMediaType();
	if (mediaType != null) {
	    out.put(RuntimeInfoElement.RESPONSE_MEDIA_TYPE.getName(), Arrays.asList(mediaType.toString()));
	}

	int status = response.getStatus();
	out.put(RuntimeInfoElement.RESPONSE_STATUS.getName(), Arrays.asList(String.valueOf(status)));

	return out;
    }

    @Override
    public String getName() {

	return "RESPONSE";
    }
}
