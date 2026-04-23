package eu.essi_lab.profiler.ckan;

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.UUID;

import eu.essi_lab.request.executor.*;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Charsets;

import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;

/**
 * CKAN-style {@code package_show}: returns one dataset dict for the given {@code id} (harmonized public identifier).
 */
public class PackageShowHandler extends StreamingRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	return new PackageShowTransformer().validate(request);
    }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {

	return new StreamingOutput() {

	    @Override
	    public void write(OutputStream output) throws IOException, WebApplicationException {

		OutputStreamWriter writer = new OutputStreamWriter(output, Charsets.UTF_8);
		PackageShowTransformer transformer = new PackageShowTransformer();
		DiscoveryMessage message;
		try {
		    message = transformer.transform(webRequest);
		} catch (GSException e) {
		    GSLoggerFactory.getLogger(PackageShowHandler.class).error(e);
		    writer.write(ckanNotFoundJson("Transform error").toString());
		    writer.close();
		    output.close();
		    return;
		}

		ServiceLoader<DiscoveryExecutor> loader = ServiceLoader.load(DiscoveryExecutor.class);
		DiscoveryExecutor executor = loader.iterator().next();
		ResultSet<GSResource> resultSet;
		try {
		    resultSet = executor.retrieve(message);
		} catch (GSException e) {
		    GSLoggerFactory.getLogger(PackageShowHandler.class).error(e);
		    writer.write(ckanNotFoundJson(e.getMessage()).toString());
		    writer.close();
		    output.close();
		    return;
		}

		List<GSResource> list = resultSet.getResultsList();
		if (list == null || list.isEmpty()) {
		    CKANRequest req = new CKANRequest(webRequest);
		    String id = req.getParameterValue(CKANRequest.APIParameters.ID);
		    writer.write(ckanNotFoundJson("Not found: " + id).toString());
		    writer.close();
		    output.close();
		    return;
		}

		GSResource resource = list.get(0);
		if (list.size() > 1) {
		    GSLoggerFactory.getLogger(PackageShowHandler.class).warn("package_show: multiple matches, returning first");
		}

		JSONObject ret = new JSONObject();
		ret.put("success", true);
		ret.put("help", "https://demo.ckan.org/api/3/action/help_show?name=package_show");
		ret.put("result", toCkanPackage(resource));

		writer.write(ret.toString());
		writer.flush();
		writer.close();
		output.close();
	    }
	};
    }

    static JSONObject ckanNotFoundJson(String message) {

	JSONObject err = new JSONObject();
	err.put("__type", "Not Found");
	err.put("message", message == null ? "Not found" : message);
	JSONObject ret = new JSONObject();
	ret.put("success", false);
	ret.put("error", err);
	return ret;
    }

    static JSONObject toCkanPackage(GSResource resource) {

	CoreMetadata core = resource.getHarmonizedMetadata().getCoreMetadata();
	String id = resource.getPublicId();
	String title = core.getTitle();
	String notes = core.getAbstract();

	JSONObject pkg = new JSONObject();
	pkg.put("id", id);
	pkg.put("name", id);
	if (title != null) {
	    pkg.put("title", title);
	} else {
	    pkg.put("title", id);
	}
	if (notes != null) {
	    pkg.put("notes", notes);
	} else {
	    pkg.put("notes", JSONObject.NULL);
	}
	pkg.put("type", "dataset");
	pkg.put("state", "active");
	pkg.put("tags", new JSONArray());

	JSONObject org = new JSONObject();
	org.put("name", resource.getSource().getUniqueIdentifier());
	org.put("title", resource.getSource().getLabel());
	pkg.put("organization", org);

	JSONArray resources = new JSONArray();
	Distribution distribution = core.getMIMetadata().getDistribution();
	if (distribution != null) {
	    Iterator<Online> onlines = distribution.getDistributionOnlines();
	    while (onlines.hasNext()) {
		Online online = onlines.next();
		String url = online.getLinkage();
		if (url == null || url.isEmpty()) {
		    continue;
		}
		JSONObject r = new JSONObject();
		r.put("id", UUID.nameUUIDFromBytes(url.getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString());
		r.put("url", url);
		String name = online.getName();
		r.put("name", name != null ? name : JSONObject.NULL);
		String protocol = online.getProtocol();
		r.put("format", protocol != null ? protocol : JSONObject.NULL);
		resources.put(r);
	    }
	}
	pkg.put("resources", resources);

	return pkg;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }
}
