package eu.essi_lab.profiler.om;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import eu.essi_lab.lib.net.utils.whos.HISCentralOntology;
import eu.essi_lab.lib.net.utils.whos.HydroOntology;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WHOSOntology;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.SortedFields;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.om.OMRequest.APIParameters;

public class OMTransformer extends DiscoveryRequestTransformer {

    /**
     * 
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    public OMTransformer() {
	// empty constructor for service loader
    }

    @Override
    public DiscoveryMessage transform(WebRequest request) throws GSException {

	DiscoveryMessage message = super.transform(request);

	OMRequest obsRequest = new OMRequest(request);
	String countValue = obsRequest.getParameterValue(APIParameters.LIMIT);

	if (countValue == null || countValue.isEmpty()) {

	    message.setIteratedWorkflow(IterationMode.FULL_RESPONSE);
	}

	message.setSortedFields(
		new SortedFields(Arrays.asList(new SimpleEntry(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, SortOrder.ASCENDING),
			new SimpleEntry(MetadataElement.ONLINE_ID, SortOrder.ASCENDING))));

	return message;
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(ResourceSubset.CORE_EXTENDED);
	selector.setIndexesPolicy(IndexesPolicy.ALL);
	// selector.addIndex(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
	// selector.addIndex(MetadataElement.PLATFORM_TITLE);
	// selector.addIndex(ResourceProperty.SOURCE_ID);
	// selector.addIndex(MetadataElement.BOUNDING_BOX);
	// selector.addIndex(MetadataElement.COUNTRY);
	//
	// selector.addIndex(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER);
	// selector.addIndex(MetadataElement.ATTRIBUTE_TITLE);
	//
	// selector.addIndex(MetadataElement.in);

	selector.setIncludeOriginal(false);
	return selector;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	WebRequestValidator validator = getValidator();

	return validator.validate(request);

    }

    public WebRequestValidator getValidator() {
	return new MonitoringPointsValidator();
    }

    @Override
    protected Bond getUserBond(WebRequest webRequest) throws GSException {

	Set<Bond> operands = new HashSet<>();

	// we are interested only on downloadable datasets
	ResourcePropertyBond accessBond = BondFactory.createIsExecutableBond(true);
	operands.add(accessBond);

	// we are interested only on downloadable datasets
	// ResourcePropertyBond downBond = BondFactory.createIsDownloadableBond(true);
	// operands.add(downBond);
	//
	// // we are interested only on TIME SERIES datasets
	// ResourcePropertyBond timeSeriesBond = BondFactory.createIsTimeSeriesBond(true);
	// operands.add(timeSeriesBond);

	OMRequest request = new OMRequest(webRequest);

	BondOperator spatialRelation = request.getSpatialRelation();
	if (spatialRelation == null) {
	    spatialRelation = BondOperator.CONTAINS;
	}

	Optional<SpatialBond> areaBond = request.getSpatialBond();
	if (areaBond.isPresent()) {
	    areaBond.get().setOperator(spatialRelation);
	    operands.add(areaBond.get());
	}
	
	Optional<String> wkt = request.getPredefinedLayer();
	if (wkt.isPresent()) {
	    eu.essi_lab.messages.bond.spatial.SpatialEntity entity = eu.essi_lab.messages.bond.spatial.SpatialEntity.of(wkt.get());
	    operands.add(BondFactory.createSpatialEntityBond(spatialRelation, entity));
	}

	Optional<SimpleValueBond> beginBond = request.getBeginBond();
	if (beginBond.isPresent()) {
	    operands.add(beginBond.get());
	}

	Optional<SimpleValueBond> endBond = request.getEndBond();
	if (endBond.isPresent()) {
	    operands.add(endBond.get());
	}

	// this is a hidden from swagger, deprecated functionality
	String variableCode = request.getParameterValue(APIParameters.VARIABLE);
	if (variableCode != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, variableCode));
	}

	String intendedObservationSpacing = request.getParameterValue(APIParameters.INTENDED_OBSERVATION_SPACING);
	if (intendedObservationSpacing != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TIME_RESOLUTION_DURATION_8601,
		    intendedObservationSpacing));
	}

	String aggregationPeriod = request.getParameterValue(APIParameters.AGGREGATION_PERIOD);
	if (aggregationPeriod != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TIME_AGGREGATION_DURATION_8601,
		    aggregationPeriod));
	}

	String observedProperty = request.getParameterValue(APIParameters.OBSERVED_PROPERTY);
	if (observedProperty != null) {
	    observedProperty = URLDecoder.decode(observedProperty, StandardCharsets.UTF_8);
	    String ontology = request.getParameterValue(APIParameters.ONTOLOGY);
	    HydroOntology ho = null;
	    if (ontology != null) {
		switch (ontology.toLowerCase()) {
		case "whos":
		    ho = new WHOSOntology();
		    break;
		case "his-central":
		    ho = new HISCentralOntology();
		    break;
		default:
		    break;
		}
	    }
	    if (ho == null) {
		SimpleValueBond b1 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ATTRIBUTE_TITLE,
			observedProperty);
		operands.add(b1);
	    } else {
		List<SKOSConcept> concepts = ho.findConcepts(observedProperty, true, false);
		HashSet<String> uris = new HashSet<String>();
		for (SKOSConcept concept : concepts) {
		    uris.add(concept.getURI());
		}
		if (concepts.isEmpty()) {
		    uris.add("notfounddd");
		}
		List<Bond> bonds = new ArrayList<>();
		for (String uri : uris) {
		    SimpleValueBond b = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.OBSERVED_PROPERTY_URI, uri);
		    bonds.add(b);
		}
		switch (bonds.size()) {
		case 0:
		    break;
		case 1:
		    operands.add(bonds.get(0));
		    break;
		default:
		    operands.add(BondFactory.createOrBond(bonds));
		}
	    }

	}

	String interpolation = request.getParameterValue(APIParameters.TIME_INTERPOLATION);
	if (interpolation != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TIME_INTERPOLATION, interpolation));
	}

	String timeseriesCode = request.getParameterValue(APIParameters.OBSERVATION);
	if (timeseriesCode != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ONLINE_ID, timeseriesCode));
	}

	String siteName = request.getParameterValue(APIParameters.PLATFORM_NAME);
	if (siteName != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.PLATFORM_TITLE, siteName));
	}

	String siteCode = request.getParameterValue(APIParameters.PLATFORM_CODE);
	if (siteCode != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, siteCode));
	}
	
	String localSiteCode = request.getParameterValue(APIParameters.LOCAL_PLATFORM_CODE);
	if (localSiteCode != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PLATFORM_IDENTIFIER, localSiteCode));
	}

	String countryCode = request.getParameterValue(APIParameters.COUNTRY);
	if (countryCode != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.COUNTRY_ISO3, countryCode));
	}

	String providerCode = request.getParameterValue(APIParameters.PROVIDER);
	if (providerCode != null) {
	    String[] split = providerCode.split(",");
	    for (String s : split) {
		operands.add(BondFactory.createSourceIdentifierBond(s));
	    }
	}

	switch (operands.size()) {
	case 0:
	    return null;
	case 1:
	    return operands.iterator().next();
	default:
	    return BondFactory.createAndBond(operands);
	}

    }

    @Override
    protected Page getPage(WebRequest webRequest) throws GSException {

	OMRequest request = new OMRequest(webRequest);
	int start = 1;
	int count = DEFAULT_PAGE_SIZE;

	String startValue = request.getParameterValue(APIParameters.OFFSET);
	if (startValue != null && !startValue.isEmpty()) {
	    try {
		start = Integer.parseInt(startValue);
	    } catch (NumberFormatException e) {
		String msg = "Not a valid offset: " + startValue;
		throw GSException.createException(getClass(), msg, msg, ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			"NOT_VALID_OFFSET");
	    }
	}

	String countValue = request.getParameterValue(APIParameters.LIMIT);
	if (countValue != null && !countValue.isEmpty()) {
	    try {
		count = Integer.parseInt(countValue);
	    } catch (NumberFormatException e) {
		String msg = "Not a valid limit: " + countValue;
		throw GSException.createException(getClass(), msg, msg, ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			"NOT_VALID_LIMIT");
	    }
	}

	return new Page(start, count);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String getProfilerType() {

	return new OMProfilerSetting().getServiceType();
    }

}
