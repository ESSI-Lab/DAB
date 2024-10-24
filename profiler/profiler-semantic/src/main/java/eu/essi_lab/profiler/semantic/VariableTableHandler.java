package eu.essi_lab.profiler.semantic;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
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
	discoveryMessage.setDistinctValuesElement(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER);

	ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);

	List<GSResource> resources = resultSet.getResultsList();

	HashMap<String, List<SimpleEntry<RowInfo, String>>> rowsBySource = new HashMap<>();
	HashMap<String, String> sourcesMap = new HashMap<>();
	for (int i = 0; i < resources.size(); i++) {

	    GSResource resource = resources.get(i);
	    GSSource s = resource.getSource();
	    sourcesMap.put(s.getLabel(), s.getUniqueIdentifier());
	    String source = s.getLabel();
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
	    String aggregationPeriod = null;
	    String interval = null;
	    String intendedObservationSpacing = null;
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
		if (resource.getExtensionHandler().getObservedPropertyURI().isPresent()) {
		    variableURI = resource.getExtensionHandler().getObservedPropertyURI().get();
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
		if (resource.getExtensionHandler().getTimeAggregationDuration8601().isPresent()) {
		    aggregationPeriod = resource.getExtensionHandler().getTimeAggregationDuration8601().get();
		}
		if (resource.getExtensionHandler().getTimeResolution().isPresent()) {
		    interval = resource.getExtensionHandler().getTimeResolution().get();
		}
		if (resource.getExtensionHandler().getTimeResolutionDuration8601().isPresent()) {
		    intendedObservationSpacing = resource.getExtensionHandler().getTimeResolutionDuration8601().get();
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

	    } catch (Exception e) {
	    }
	    List<SimpleEntry<RowInfo, String>> rows = rowsBySource.get(source);
	    if (rows == null) {
		rows = new ArrayList<SimpleEntry<RowInfo, String>>();
	    }
	    String lb = "%0D%0A";
	    String body = "Dear Hydrology-ontology admin,\n\n" + lb + lb + //
		    "I'd like to suggest semantic mapping(s) for the following variable(s) accessible through WHOS (from " + viewId
		    + " view):\n\n" + lb + lb + //
		    "Variable Name: " + variableName + "\n" + lb + //
		    "Variable Description: " + variableDescription + "\n" + lb + //
		    "Identifier at data provider web service: " + variableCode + "\n" + lb + //
		    "WHOS identifier: " + uniqueVariableCode + "\n" + lb + //
		    "From service: " + source + "\n\n" + lb + lb + //

		    "Reporter: -YOUR NAME HERE-\n" + lb + //
		    "Contact e-mail: -YOUR EMAIL HERE-\n" + lb + //
		    "Organization: -YOUR ORGANIZATION HERE-\n" + lb + //
		    "Country: -YOUR COUNTRY HERE-\n\n" + lb + lb + //
		    "EXAMPLE MAPPING(S) - MODIFY AS NEEDED\n\n" + lb + lb + //
		    "Mapping #1 (to WMO Codes Registry at http://codes.wmo.int/wmdr/_ObservedVariableTerrestrial):\n" + lb + //
		    "WMO Concept URI: (e.g. <http://codes.wmo.int/wmdr/ObservedVariableTerrestrial/_171>)\n" + lb + //
		    "WMO Concept label: (e.g. River discharge)\n" + lb + //
		    "WMO Concept description: (e.g. Volume of water flowing through a river per unit of time)\n\n" + lb + lb; //

	    RowInfo info = new RowInfo();
	    info.setUniqueVariableCode(uniqueVariableCode);
	    info.setVariableCode(variableCode);
	    info.setVariableName(variableName);
	    info.setVariableURI(variableURI);
	    info.setVariableDescription(variableDescription);
	    info.setVariableUnits(variableUnits);
	    info.setVariableUnitsURI(variableUnitsURI);
	    info.setInterpolation(interpolation);
	    info.setInterpolationSupport(interpolationSupport);
	    info.setAggregationPeriod(aggregationPeriod);
	    info.setInterval(interval);
	    info.setIntendedObservationSpacing(intendedObservationSpacing);
	    info.setInterpolationSupportUnits(interpolationSupportUnits);
	    info.setRealtime(realtime);
	    info.setCountry(country);
	    info.setCountryISO3(countryISO3);

	    String csv = uniqueVariableCode + "\t" + protocol + "\t" + variableCode + "\t" + variableName + "\t" + variableDescription
		    + "\t\n";
	    rows.add(new SimpleEntry<RowInfo, String>(info, csv));

	    rowsBySource.put(source, rows);

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

	for (String source : rowsBySource.keySet()) {
	    String sourceId = sourcesMap.get(source);

	    // STATISTICS
	    SourceStatistics sourceStats = null;
	    try {
		sourceStats = new SourceStatistics(sourceId, webRequest.extractViewId(), null);
	    } catch (Exception e1) {
		e1.printStackTrace();
	    }
	    Stats stats = sourceStats.getStatistics().get(null);
	    content += "<tr><td colspan='15'><br/>"//
		    + "Data provider: <b>" + source + "</b><br/>"//
		    + "#Platforms: " + stats.getSiteCount() + "<br/>"//
		    + "#Variables:" + stats.getAttributeCount() + "<br/>"//
		    + "#Timeseries:" + stats.getTimeSeriesCount() + "<br/>"//
		    + "Begin:" + stats.getBegin() + "<br/>"//
		    + "End:" + stats.getEnd() + "<br/>"//
		    + "BBOX(w,s,e,n): " + stats.getWest() + "," + stats.getSouth() + "," + stats.getEast() + "," + stats.getNorth()+"<br/>" //
		    + "Altitude:" + stats.getMinimumAltitude()+"/"+stats.getMaximumAltitude() + "<br/>"//		    
		    + "</td></tr>" + "" //
		    + "<tr>" + //
		    getHeader("#Platforms") + //
		    getHeader("#Variables") + // 1
		    getHeader("#Timeseries") + // 1
		    getHeader("Begin") + //
		    getHeader("End") + //
		    getHeader("BBOX(w,s,e,n)") + // 1
		    getHeader("DAB generated variable identifier") + //
		    getHeader("Data provider variable identifier") + //
		    getHeader("Variable name") + //
		    getHeader("Variable URI") + //
		    getHeader("Variable description") + //
		    getHeader("Variable units") + //
		    getHeader("Variable units URI") + //
		    getHeader("Interpolation type") + //
		    getHeader("Interpolation time support") + //
		    getHeader("Aggregation period") + //
		    getHeader("Time interval") + //
		    getHeader("Intended observation spacing") + //
		    getHeader("Time units") + //
		    getHeader("Real time") + //
		    getHeader("Country") + //
		    getHeader("Country ISO3") + //
		    "</tr>";
	    try {
		sourceStats = new SourceStatistics(sourceId, webRequest.extractViewId(), MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER);
	    } catch (Exception e1) {
		e1.printStackTrace();
	    }
	    List<SimpleEntry<RowInfo, String>> rows = rowsBySource.get(source);
	    // rows.sort(new Comparator<T>() {
	    // })
	    for (SimpleEntry<RowInfo, String> row : rows) {
		RowInfo ri = row.getKey();
		Stats vs = sourceStats.getStatistics().get(ri.getUniqueVariableCode());
		ri.setSiteCount(vs.getSiteCount());
		ri.setAttributeCount(vs.getAttributeCount());
		ri.setTimeseriesCount(vs.getTimeSeriesCount());
		ri.setBegin(vs.getBegin());
		ri.setEnd(vs.getEnd());
		ri.setWest(vs.getWest());
		ri.setEast(vs.getEast());
		ri.setSouth(vs.getSouth());
		ri.setNorth(vs.getNorth());

	    }
	    rows.sort(new Comparator<SimpleEntry<RowInfo, String>>() {

		@Override
		public int compare(SimpleEntry<RowInfo, String> o1, SimpleEntry<RowInfo, String> o2) {
		    String t1 = o1.getKey().getTimeseriesCount();
		    String t2 = o2.getKey().getTimeseriesCount();
		    return new Integer(Integer.parseInt(t2)).compareTo(Integer.parseInt(t1));
		}
	    });
	    for (SimpleEntry<RowInfo, String> row : rows) {
		RowInfo ri = row.getKey();
		content += "<tr>" + //
			getRow(ri.getSiteCount()) + //
			getRow(ri.getAttributeCount()) + // 1
			getRow(ri.getTimeseriesCount()) + // 1
			getRow(ri.getBegin()) + //
			getRow(ri.getEnd()) + //
			getRow(ri.getWest() + "," + ri.getSouth() + "," + ri.getEast() + "," + ri.getNorth()) + // 1
			getRow(ri.getUniqueVariableCode()) + //
			getRow(ri.getVariableCode()) + //
			getRow(ri.getVariableName()) + //
			getRow(ri.getVariableURI(), ri.getVariableURI()) + //
			getRow(ri.getVariableDescription()) + //
			getRow(ri.getVariableUnits()) + //
			getRow(ri.getVariableUnitsURI(), ri.getVariableUnitsURI()) + //
			getRow(ri.getInterpolation()) + //
			getRow(ri.getInterpolationSupport()) + //
			getRow(ri.getAggregationPeriod()) + //
			getRow(ri.getInterval()) + //
			getRow(ri.getIntendedObservationSpacing()) + //
			getRow(ri.getInterpolationSupportUnits()) + //
			getRow(ri.getRealtime()) + //
			getRow(ri.getCountry()) + //
			getRow(ri.getCountryISO3()) + //
			"</tr>";
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
	String str = "<html><head><meta charset=\"UTF-8\"></head><body><h1>Metadata content analysis for view: " + viewId + "</h1>" //
		+ content + "</table></body></html>";

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
