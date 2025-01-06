package eu.essi_lab.profiler.bnhs;

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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.lib.net.utils.whos.HydroOntology;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WHOSOntology;
import eu.essi_lab.lib.net.utils.whos.WMOOntology;
import eu.essi_lab.lib.net.utils.whos.WMOUnit;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.BNHSProperty;
import eu.essi_lab.model.resource.BNHSPropertyReader;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.handler.WebRequestHandler;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * This handler is responsible for the station page
 * 
 * @author boldrini
 */
public class BNHSStationHandler implements WebRequestHandler, WebRequestValidator {

    /**
     * 
     */
    private static final int DEFAULT_PAGE_SIZE = 10;
    private String viewId;

    /**
     * @param setting
     */
    public BNHSStationHandler(WebRequest request) {

	viewId = BNHSProfiler.readViewId(request);
    }

    @Override
    public Response handle(WebRequest webRequest) throws GSException {

	ResponseBuilder builder = Response.status(Status.OK);

	// station/C4EE9FB0F47E8B247EE17F597A2F1105A0006399
	String request = webRequest.getRequestPath();
	String[] split = request.split("/");
	String stationId = null;
	for (int i = 0; i < split.length; i++) {
	    String string = split[i];
	    if (string.equals("station")) {
		if (i < (split.length - 1)) {
		    stationId = split[i + 1];
		}
	    }
	}

	if (stationId == null) {

	    // main page

	    String html = "<html><head><title>WHOS-broker: Station page</title>\n" //
		    + "</head><body>\n"//
		    + "<h2>No station id provided</body></html>\n";

	    builder = builder.entity(html).type(new MediaType("text", "html"));

	    return builder.build();

	} else if (request.endsWith("timeseries")) {

	    // time series information

	    // String json = "[\n";

	    ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	    IDiscoveryExecutor executor = loader.iterator().next();

	    DiscoveryMessage discoveryMessage = new DiscoveryMessage();

	    discoveryMessage.setRequestId(webRequest.getRequestId());

	    discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.ALL);
	    discoveryMessage.getResourceSelector().setSubset(ResourceSubset.FULL);
	    discoveryMessage.setPage(new Page(1, DEFAULT_PAGE_SIZE));
	    discoveryMessage.setIteratedWorkflow(IterationMode.FULL_RESPONSE);
	    discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
	    discoveryMessage.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());

	    Set<Bond> operands = new HashSet<>();

	    // we are interested only on downloadable datasets
	    ResourcePropertyBond accessBond = BondFactory.createIsExecutableBond(true);
	    operands.add(accessBond);

	    // we are interested only on downloadable datasets
	    ResourcePropertyBond downBond = BondFactory.createIsDownloadableBond(true);
	    operands.add(downBond);

	    // we are interested only on TIME SERIES datasets
	    ResourcePropertyBond timeSeriesBond = BondFactory.createIsTimeSeriesBond(true);
	    operands.add(timeSeriesBond);

	    // we are interested only on datasets from a specific platform
	    SimpleValueBond platformBond = BondFactory.createSimpleValueBond(//
		    BondOperator.EQUAL, //
		    MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, //
		    stationId);
	    operands.add(platformBond);

	    LogicalBond bond = BondFactory.createAndBond(operands);

	    StorageInfo storageUri = ConfigurationWrapper.getDatabaseURI();

	    Optional<View> optionalView = WebRequestTransformer.findView(storageUri, viewId);

	    if (optionalView.isPresent()) {

		//discoveryMessage.setView(optionalView.get());
	    }

	    discoveryMessage.setPermittedBond(bond);
	    discoveryMessage.setUserBond(bond);
	    discoveryMessage.setNormalizedBond(bond);

	    ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);

	    List<GSResource> resources = resultSet.getResultsList();

	    JSONArray array = new JSONArray();

	    for (int i = 0; i < resources.size(); i++) {

		GSResource resource = resources.get(i);

		String platformId = resource.getExtensionHandler().getUniquePlatformIdentifier().isPresent() ? //
			resource.getExtensionHandler().getUniquePlatformIdentifier().get() : "";
		String platformLabel = "";
		try {
		    platformLabel = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform().getCitation()
			    .getTitle();
		} catch (Exception e) {
		}
		String attributeId = resource.getExtensionHandler().getUniqueAttributeIdentifier().isPresent() ? //
			resource.getExtensionHandler().getUniqueAttributeIdentifier().get() : "";
		String attributeLabel = "";
		try {
		    attributeLabel = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getCoverageDescription()
			    .getAttributeTitle();
		} catch (Exception e) {
		}
		// if attribute URI is present, is preferred, to have an harmonized set of attributes
		Optional<String> optionalAttributeURI = resource.getExtensionHandler().getObservedPropertyURI();
		if (optionalAttributeURI.isPresent()) {
		    String uri = optionalAttributeURI.get();
		    if (uri != null) {
			HydroOntology ontology = new WHOSOntology();
			SKOSConcept concept = ontology.getConcept(uri);
			if (concept != null) {
			    attributeLabel = concept.getPreferredLabel().getKey();
			    HashSet<String> closeMatches = concept.getCloseMatches();
			    if (closeMatches != null && !closeMatches.isEmpty()) {
				try {
				    WMOOntology wmoOntology = new WMOOntology();
				    for (String closeMatch : closeMatches) {
					SKOSConcept variable = wmoOntology.getVariable(closeMatch);
					if (variable != null) {
					    SimpleEntry<String, String> preferredLabel = variable.getPreferredLabel();
					    if (preferredLabel != null) {
						attributeLabel = preferredLabel.getKey();
					    }
					}
				    }
				} catch (Exception e) {
				    e.printStackTrace();
				}

			    }
			}
		    }
		}
		String attributeMissingValue = resource.getExtensionHandler().getAttributeMissingValue().isPresent() ? //
			resource.getExtensionHandler().getAttributeMissingValue().get() : "";
		String attributeUnits = resource.getExtensionHandler().getAttributeUnits().isPresent() ? //
			resource.getExtensionHandler().getAttributeUnits().get() : "";
		String attributeUnitsAbbreviation = resource.getExtensionHandler().getAttributeUnitsAbbreviation().isPresent() ? //
			resource.getExtensionHandler().getAttributeUnitsAbbreviation().get() : "";
		// if attribute URI is present, is preferred, to have an harmonized set of attribute units
		Optional<String> optionalAttributeUnitsURI = resource.getExtensionHandler().getAttributeUnitsURI();
		if (optionalAttributeUnitsURI.isPresent()) {
		    String uri = optionalAttributeUnitsURI.get();
		    if (uri != null) {

			try {
			    WMOOntology codes = new WMOOntology();
			    WMOUnit unit = codes.getUnit(uri);
			    if (unit != null) {
				attributeUnits = unit.getPreferredLabel().getKey();
				attributeUnitsAbbreviation = unit.getAbbreviation();
			    }
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    }

		}
		String timeInterpolation = resource.getExtensionHandler().getTimeInterpolation().isPresent() ? //
			resource.getExtensionHandler().getTimeInterpolation().get().toString() : "";
		String timeSupport = resource.getExtensionHandler().getTimeSupport().isPresent() ? //
			resource.getExtensionHandler().getTimeSupport().get().toString() : "";
		String timeResolution = resource.getExtensionHandler().getTimeResolution().isPresent() ? //
			resource.getExtensionHandler().getTimeResolution().get() : "";
		String timeUnits = resource.getExtensionHandler().getTimeUnits().isPresent() ? //
			resource.getExtensionHandler().getTimeUnits().get() : "";
		String timeUnitsAbbreviation = resource.getExtensionHandler().getTimeUnitsAbbreviation().isPresent() ? //
			resource.getExtensionHandler().getTimeUnitsAbbreviation().get() : "";
		String timeStart = "";
		String timeEnd = "";
		String nearRealTime = "no";
		String timeEndRecent = "";
		String timeEndLastYear = "";
		String timeEndMinus30Days = "";
		TemporalExtent temporalExtent = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
			.getTemporalExtent();
		if (temporalExtent != null) {
		    timeStart = temporalExtent.getBeginPosition();
		    timeEnd = temporalExtent.getEndPosition();
		    if (temporalExtent.isEndPositionIndeterminate()
			    && temporalExtent.getIndeterminateEndPosition().equals(TimeIndeterminateValueType.NOW)) {
			timeEnd = ISO8601DateTimeUtils.getISO8601DateTime();
			nearRealTime = "yes";
		    }
		    Date end = ISO8601DateTimeUtils.parseISO8601(timeEnd);
		    Date start = ISO8601DateTimeUtils.parseISO8601(timeStart);

		    long oneDay = 1000 * 60 * 60 * 24l;
		    long oneWeek = oneDay * 7l;
		    long oneMonth = oneDay * 30l;
		    long twoMonth = oneMonth * 2l;
		    long oneYear = oneDay * 365l;
		    timeEndRecent = ISO8601DateTimeUtils.getISO8601DateTime(new Date(end.getTime() - twoMonth));
		    timeEndLastYear = (end.getTime() - oneYear) < start.getTime() ? ISO8601DateTimeUtils.getISO8601DateTime(start)
			    : ISO8601DateTimeUtils.getISO8601DateTime(new Date(end.getTime() - oneYear));
		}

		VerticalExtent verticalExtent = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
			.getVerticalExtent();

		Double verticalExt = null;

		if (verticalExtent != null) {

		    verticalExt = verticalExtent.getMaximumValue();
		}

		//
		// platform
		//

		JSONObject object = new JSONObject();

		object = create(object, "platform_id", platformId, "Station ID");

		object = create(object, "platform_label", platformLabel, "Station/platform name");

		//
		// attribute
		//
		object = create(object, "attribute_id", attributeId, "Observed variable ID");

		object = create(object, "attribute_label", attributeLabel, "Observed variable");

		object = create(object, "attribute_missing_value", attributeMissingValue, "Attribute missing value");

		//
		// attribute units
		//
		object = create(object, "attribute_units", attributeUnits, "Measurement unit");

		object = create(object, "attribute_units_abbreviation", attributeUnitsAbbreviation, "Measurement unit (abbreviation)");

		//
		// time
		//
		object = create(object, "time_interpolation", timeInterpolation, "Time interpolation");

		object = create(object, "time_support", timeSupport, "Time support");

		object = create(object, "time_resolution", timeResolution, "Time resolution");

		object = create(object, "time_units", timeUnits, "Time units");

		object = create(object, "time_units_abbreviation", timeUnitsAbbreviation, "Time units abbreviation");

		//
		// temp extent
		//
		object = create(object, "time_start", timeStart, "Time start");

		object = create(object, "time_end_recent", timeEndRecent, "time_end_recent");

		object = create(object, "time_end_last_year", timeEndLastYear, "time_end_last_year");

		object = create(object, "time_end", timeEnd, "Time end");

		object = create(object, "near_real_time", nearRealTime, "Near real time");

		//
		// vertical extent
		//
		if (verticalExt != null) {

		    object = create(object, "vertical_extent", verticalExt.toString(), "vertical extent");
		}

		//
		// other properties
		//

		List<SimpleEntry<BNHSProperty, String>> properties = BNHSPropertyReader.readProperties(resource);

		for (SimpleEntry<BNHSProperty, String> entry : properties) {

		    object = create(object, entry.getKey().name(), entry.getValue(), entry.getKey().getLabel());
		}

		array.put(object);
	    }

	    builder = builder.entity(array.toString(3)).type(MediaType.APPLICATION_JSON_TYPE);

	    return builder.build();

	} else {

	    //
	    // main page
	    //

	    InputStream stream = null;

	    switch (viewId) {
	    case "whos-arctic":
		stream = BNHSStationHandler.class.getClassLoader().getResourceAsStream("bnhs/station.html");
		break;
	    case "whos":
		stream = BNHSStationHandler.class.getClassLoader().getResourceAsStream("bnhs-whos/station.html");
		break;
	    case "his-central":
		stream = BNHSStationHandler.class.getClassLoader().getResourceAsStream("bnhs-his-central/station.html");
		break;
	    }

	    String html = "";

	    try {
		html = IOUtils.toString(stream, StandardCharsets.UTF_8);
		stream.close();
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }

	    builder = builder.entity(html).type(new MediaType("text", "html"));

	    return builder.build();
	}
    }

    /**
     * @param value
     * @param label
     * @return
     */
    private static JSONObject create(JSONObject object, String name, String value, String label) {

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("value", value);
	jsonObject.put("label", label);

	object.put(name, jsonObject);

	return object;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    public static void main(String[] args) {

	String s = "[";
	s += "{";
	s += "     'platform_id': {";
	s += "       'label': 'Station ID',";
	s += "    'value': 'F41D68BEB24C35F060034A538106458D4CE9712B'";
	s += "  },";

	s += "  'platform_label': {";
	s += "     'label': 'Station/platform name',";
	s += "     'value': 'MOSSY RIVER BELOW OUTLET OF DAUPHIN LAKE'";
	s += " },";
	s += " 'attribute_id': {";
	s += "    'label': 'Observed variable ID',";
	s += "  'value': '045A60FA1C36A46F2DDF7EFDCAB311D8E9942303'";
	s += " }";
	s += "}   ";
	s += "]";

	JSONArray array = new JSONArray();

	JSONObject object = new JSONObject();

	object = create(object, "platform_id", "dsaads", "Station ID");

	object = create(object, "platform_label", "czxcxzx", "Station/platform name");

	array.put(object);

	System.out.println(array.toString(3));

    }

}
