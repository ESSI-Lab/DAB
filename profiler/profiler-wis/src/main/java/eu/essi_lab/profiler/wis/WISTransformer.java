package eu.essi_lab.profiler.wis;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.wis.WISRequest.Parameter;
import eu.essi_lab.profiler.wis.metadata.collection.WISValidator;

public class WISTransformer extends DiscoveryRequestTransformer {

    /**
     * 
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    // empty constructor for service loader
    public WISTransformer() {
    }

    @Override
    public DiscoveryMessage transform(WebRequest webRequest) throws GSException {

	DiscoveryMessage message = super.transform(webRequest);

	WISRequest wis = new WISRequest(webRequest);

	String request;
	try {
	    InputStream stream = wis.getBody();
	    String view = webRequest.extractViewId().get();
	    if (stream!=null) {
	    request = IOUtils.toString(stream, StandardCharsets.UTF_8);
	    JSONObject json = new JSONObject(request);
	    JSONObject inputs = json.getJSONObject("inputs");
	    view = inputs.getString("collection");
	    }
	    StorageInfo storageUri = ConfigurationWrapper.getStorageInfo();
	    setView(view, storageUri, message);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	String countValue = wis.getParameterValue(Parameter.LIMIT);

	if (countValue == null || countValue.isEmpty()) {

	    message.setIteratedWorkflow(IterationMode.FULL_RESPONSE);
	}
	message.setDistinctValuesElement(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);

	return message;
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(ResourceSubset.NONE);
	selector.setIndexesPolicy(IndexesPolicy.NONE);
	 selector.addIndex(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
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
	return new WISValidator();
    }

    @Override
    protected Bond getUserBond(WebRequest webRequest) throws GSException {

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

	// Optional<SpatialBond> areaBond = request.getSpatialBond();
	// if (areaBond.isPresent()) {
	// operands.add(areaBond.get());
	// }
	//
	// Optional<SimpleValueBond> beginBond = request.getBeginBond();
	// if (beginBond.isPresent()) {
	// operands.add(beginBond.get());
	// }
	//
	// Optional<SimpleValueBond> endBond = request.getEndBond();
	// if (endBond.isPresent()) {
	// operands.add(endBond.get());
	// }
	//
	// String variableCode = request.getParameterValue(APIParameters.VARIABLE);
	// if (variableCode != null) {
	// operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL,
	// MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, variableCode));
	// }
	//
	// String siteCode = request.getParameterValue(APIParameters.PLATFORM_CODE);
	// if (siteCode != null) {
	// operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL,
	// MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, siteCode));
	// }
	//
	// String countryCode = request.getParameterValue(APIParameters.COUNTRY);
	// if (countryCode != null) {
	// operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.COUNTRY_ISO3,
	// countryCode));
	// }
	//
	// String providerCode = request.getParameterValue(APIParameters.PROVIDER);
	// if (providerCode != null) {
	// operands.add(BondFactory.createSourceIdentifierBond(providerCode));
	// }

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

	WISRequest request = new WISRequest(webRequest);
	int start = 1;
	int count = DEFAULT_PAGE_SIZE;

	String startValue = request.getParameterValue(Parameter.OFFSET);
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

	String countValue = request.getParameterValue(Parameter.LIMIT);
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

	return new WISProfilerSetting().getServiceType();
    }

}
