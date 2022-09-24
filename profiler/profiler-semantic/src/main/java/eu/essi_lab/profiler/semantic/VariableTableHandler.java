package eu.essi_lab.profiler.semantic;

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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.handler.WebRequestHandler;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

public class VariableTableHandler implements WebRequestHandler, WebRequestValidator {

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
	StorageUri uri = ConfigurationWrapper.getDatabaseURI();
	discoveryMessage.setDataBaseURI(uri);
	// discoveryMessage.setSharedRepositoryInfo(ConfigurationUtils.getSharedRepositoryInfo());

	// Set<Bond> operands = new HashSet<>();
	//
	// // we are interested only on downloadable datasets
	// ResourcePropertyBond accessBond = BondFactory.createIsExecutableBond(true);
	// operands.add(accessBond);
	//
	// // we are interested only on downloadable datasets
	// ResourcePropertyBond downBond = BondFactory.createIsDownloadableBond(true);
	// operands.add(downBond);
	//
	// // we are interested only on TIME SERIES datasets
	// ResourcePropertyBond timeSeriesBond = BondFactory.createIsTimeSeriesBond(true);
	// operands.add(timeSeriesBond);
	//
	// // we are interested only on datasets from a specific platform
	//
	// LogicalBond bond = BondFactory.createAndBond(operands);

	String viewId = null;
	Optional<String> optionalView = webRequest.extractViewId();

	if (optionalView.isPresent()) {
	    viewId = optionalView.get();
	    WebRequestTransformer.setView(viewId, uri, discoveryMessage);
	} else {
	    ByteArrayInputStream stream = new ByteArrayInputStream("<html><body>No view provided!</body></html>".getBytes());
	    return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_HTML).entity(stream).build();
	}

	// discoveryMessage.setPermittedBond(bond);
	// discoveryMessage.setUserBond(bond);
	// discoveryMessage.setNormalizedBond(bond);

	discoveryMessage.setDistinctValuesElement(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER);

	ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);

	List<GSResource> resources = resultSet.getResultsList();

	HashMap<String, List<SimpleEntry<String, String>>> rowsByProtocol = new HashMap<>();
	for (int i = 0; i < resources.size(); i++) {

	    GSResource resource = resources.get(i);
	    String protocol = null;
	    String uniqueVariableCode = null;
	    String variableCode = null;
	    String variableName = null;
	    String variableURI = null;
	    String variableDescription = null;
	    String variableUnits = null;
	    String variableUnitsURI = null;
	    String interpolation = null;
	    String interpolationSupport = null;
	    String interval = null;
	    String interpolationSupportUnits = null;
	    String realtime = null;
	    String country = "";
	    String countryISO3 = "";
	    try {
		TemporalExtent temporal = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
		if (temporal != null) {
		    TimeIndeterminateValueType end = temporal.getIndeterminateEndPosition();
		    if (end != null && end.equals(TimeIndeterminateValueType.NOW)) {
			realtime = "yes";
		    }
		}
		if (resource.getExtensionHandler().getUniqueAttributeIdentifier().isPresent()) {
		    uniqueVariableCode = resource.getExtensionHandler().getUniqueAttributeIdentifier().get();
		}
		variableName = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getCoverageDescription()
			.getAttributeTitle();
		variableDescription = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getCoverageDescription()
			.getAttributeDescription();
		if (resource.getExtensionHandler().getAttributeURI().isPresent()) {
		    variableURI = resource.getExtensionHandler().getAttributeURI().get();
		}
		variableCode = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getCoverageDescription()
			.getAttributeIdentifier();
		if (resource.getExtensionHandler().getAttributeUnits().isPresent()) {
		    variableUnits = resource.getExtensionHandler().getAttributeUnits().get();
		}
		if (resource.getExtensionHandler().getAttributeUnitsURI().isPresent()) {
		    variableUnitsURI = resource.getExtensionHandler().getAttributeUnitsURI().get();
		}
		if (resource.getExtensionHandler().getTimeInterpolation().isPresent()) {
		    interpolation = resource.getExtensionHandler().getTimeInterpolation().get().name();
		}
		if (resource.getExtensionHandler().getTimeSupport().isPresent()) {
		    interpolationSupport = resource.getExtensionHandler().getTimeSupport().get();
		}
		if (resource.getExtensionHandler().getTimeResolution().isPresent()) {
		    interval = resource.getExtensionHandler().getTimeResolution().get();
		}
		if (resource.getExtensionHandler().getTimeUnits().isPresent()) {
		    interpolationSupportUnits = resource.getExtensionHandler().getTimeUnits().get();
		}
		if (resource.getExtensionHandler().getCountry().isPresent()) {
		    country = resource.getExtensionHandler().getCountry().get();
		}
		if (resource.getExtensionHandler().getCountryISO3().isPresent()) {
		    countryISO3 = resource.getExtensionHandler().getCountryISO3().get();
		}

		Iterator<Online> onlinesIterator = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution()
			.getDistributionOnlines();
		while (onlinesIterator.hasNext()) {
		    Online online = (Online) onlinesIterator.next();
		    protocol = online.getLinkage();
		    if (resource.getHarmonizedMetadata().getCoreMetadata().getOnline().getFunctionCode().equals("download")) {
			break;
		    }
		}

	    } catch (Exception e) {
		// TODO: handle exception
	    }
	    List<SimpleEntry<String, String>> rows = rowsByProtocol.get(protocol);
	    if (rows == null) {
		rows = new ArrayList<SimpleEntry<String, String>>();
	    }
	    String lb = "%0D%0A";
	    String body = "Dear Hydrology-ontology admin,\n\n" + lb + lb + //
		    "I'd like to suggest semantic mapping(s) for the following variable(s) accessible through WHOS (from " + viewId
		    + " view):\n\n" + lb + lb + //
		    "Variable Name: " + variableName + "\n" + lb + //
		    "Variable Description: " + variableDescription + "\n" + lb + //
		    "Identifier at data provider web service: " + variableCode + "\n" + lb + //
		    "WHOS identifier: " + uniqueVariableCode + "\n" + lb + //
		    "From service: " + protocol + "\n\n" + lb + lb + //

		    "Reporter: -YOUR NAME HERE-\n" + lb + //
		    "Contact e-mail: -YOUR EMAIL HERE-\n" + lb + //
		    "Organization: -YOUR ORGANIZATION HERE-\n" + lb + //
		    "Country: -YOUR COUNTRY HERE-\n\n" + lb + lb + //
		    "EXAMPLE MAPPING(S) - MODIFY AS NEEDED\n\n" + lb + lb + //
		    "Mapping #1 (to WMO Codes Registry at http://codes.wmo.int/wmdr/_ObservedVariableTerrestrial):\n" + lb + //
		    "WMO Concept URI: (e.g. <http://codes.wmo.int/wmdr/ObservedVariableTerrestrial/_171>)\n" + lb + //
		    "WMO Concept label: (e.g. River discharge)\n" + lb + //
		    "WMO Concept description: (e.g. Volume of water flowing through a river per unit of time)\n\n" + lb + lb; //
	    // "Mapping #2 (to CUAHSI ontology):\n" + lb + //
	    // "Concept URI: (e.g.
	    // <https://hiscentral.cuahsi.org/webservices/hiscentral.asmx/getOntologyTree?conceptKeyword=Discharge,%20stream>)\n"
	    // + lb + //
	    // "Concept label: (e.g. Discharge, stream)\n" + lb + //
	    // "Concept description: (e.g. Discharge, stream)\n\n"//

	    ;
	    String row = "<tr><td>" + uniqueVariableCode + "</td><td>" + variableCode + "</td><td>" + variableName + "</td><td><a href='"
		    + variableURI + "'>" + variableURI + "</a></td><td>" + variableDescription + "</td><td>" + variableUnits
		    + "</td><td><a href='" + variableUnitsURI + "'>" + variableUnitsURI + "</a></td><td>" + interpolation + "</td><td>"
		    + interpolationSupport + "</td><td>" + interval + "</td><td>" + interpolationSupportUnits + "</td><td>" + realtime
		    + "</td><td>" + country + "</td><td>" + countryISO3
		    + "</td><td><a href=\"mailto:admin-hydro-ontology@wmo.int?cc=ichernov@wmo.int,nravalitera@wmo.int,dberod@wmo.int,chy.vicepresident@gmail.com,francesco.delbuono@unimore.it,enrico.boldrini@cnr.it&subject=Mapping suggestion&body="
		    + body + "\">Suggest a mapping</a></td></tr>";
	    String csv = uniqueVariableCode + "\t" + protocol + "\t" + variableCode + "\t" + variableName + "\t" + variableDescription
		    + "\t\n";
	    rows.add(new SimpleEntry<String, String>(row, csv));

	    rowsByProtocol.put(protocol, rows);

	}

	String content = "<table border='1px'>";
	File file = new File("/home/boldrini/ontologiaFALSE");
	BufferedWriter writer = null;
	if (file.exists()) {
	    try {
		writer = new BufferedWriter(new FileWriter(new File(file, "whos-arctic.csv")));
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	if (writer != null) {
	    try {
		writer.write(
			"WHOS identifier\tData provider service\tData provider identifier\tVariable name\tVariable description\tHydro-ontology concept\n");
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}

	for (String protocol : rowsByProtocol.keySet()) {
	    List<SimpleEntry<String, String>> rows = rowsByProtocol.get(protocol);

	    content += "<tr><td colspan='15'><br/>From service at: " + protocol + "<br/></td></tr>" + "" //
		    + "<tr><th>Variable WHOS identifier</th><th>Variable identifier at data provider web service</th><th>Variable name</th><th>Variable URI</th><th>Variable description</th><th>Variable units</th><th>Variable units URI</th><th>Interpolation type</th><th>Interpolation time support</th><th>Time interval</th><th>Time units</th><th>Real time</th><th>Country</th><th>Country ISO3</th><th>Mappings</th></tr>";
	    for (SimpleEntry<String, String> row : rows) {
		content += row.getKey();
		if (writer != null) {
		    try {
			writer.write(row.getValue());
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}
	    }
	}
	if (writer != null) {
	    try {
		writer.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	String str = "<html><head><meta charset=\"UTF-8\"></head><body><h1>Variables available for view: " + viewId + "</h1>" //
		+ content + "</table></body></html>";

	ByteArrayInputStream stream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
	return Response.status(Status.OK).type(MediaType.TEXT_HTML).entity(stream).build();

    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

}
