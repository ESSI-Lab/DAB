package eu.essi_lab.profiler.semantic;

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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.handler.WebRequestHandler;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

public class CountriesTableHandler implements WebRequestHandler, WebRequestValidator {

    /**
     * 
     */
    private static final int DEFAULT_PAGE_SIZE = 1000;

    @Override
    public Response handle(WebRequest webRequest) throws GSException {

	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();

	discoveryMessage.setRequestId(webRequest.getRequestId());

	discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	discoveryMessage.getResourceSelector().setSubset(ResourceSubset.FULL);
	discoveryMessage.setPage(new Page(1, DEFAULT_PAGE_SIZE));
	discoveryMessage.setIteratedWorkflow(IterationMode.FULL_RESPONSE);
	discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
	StorageInfo uri = ConfigurationWrapper.getDatabaseURI();
	discoveryMessage.setDataBaseURI(uri);

	String viewId = null;
	Optional<String> optionalView = webRequest.extractViewId();

	if (optionalView.isPresent()) {
	    viewId = optionalView.get();
	    WebRequestTransformer.setView(viewId, uri, discoveryMessage);
	} else {
	    ByteArrayInputStream stream = new ByteArrayInputStream("<html><body>No view provided!</body></html>".getBytes());
	    return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_HTML).entity(stream).build();
	}

	discoveryMessage.setDistinctValuesElement(ResourceProperty.SOURCE_ID);
	ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);

	List<GSResource> resources = resultSet.getResultsList();

	String str = "<html>";
	List<String> sources = new ArrayList<String>();
	List<String> labels = new ArrayList<String>();
	for (int i = 0; i < resources.size(); i++) {

	    GSResource resource = resources.get(i);
	    String id = resource.getSource().getUniqueIdentifier();
	    System.out.println(id);

	    sources.add(id);
	    labels.add(resource.getSource().getLabel());
	}

	discoveryMessage.setDistinctValuesElement(MetadataElement.COUNTRY);

	for (int i = 0; i < sources.size(); i++) {
	    String id = sources.get(i);
	    String label = labels.get(i);
	    System.out.println(id);
	    str += label + ":<br/>";

	    discoveryMessage.setUserBond(BondFactory.createSourceIdentifierBond(id));

	    resultSet = executor.retrieve(discoveryMessage);

	    resources = resultSet.getResultsList();
	    if (resources.isEmpty()) {
		str += "-info unavailable<br/>";
	    }
	    for (int j = 0; j < resources.size(); j++) {

		GSResource resource = resources.get(j);
		Optional<String> oc = resource.getExtensionHandler().getCountry();
		if (oc.isPresent()) {
		    String country = oc.get();
		    System.out.println("-" + country);
		    str += "-" + country + "<br/>";
		} else {
		    str += "-info unavailable<br/>";
		}

	    }

	    str += "<br/>";
	    str += "<br/>";
	}

	ByteArrayInputStream stream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
	return Response.status(Status.OK).type(MediaType.TEXT_HTML).entity(stream).build();

    }

    private String getHeader(String header) {
	return "<th>" + header + "</th>";
    }

    private String getRow(String value) {
	return getRow(value, null);
    }

    private String getRow(String value, String href) {
	if (href == null) {
	    return "<td>" + value + "</td>";
	} else {
	    return "<td><a href='" + href + "'>" + value + "</a></td>";
	}
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

}
