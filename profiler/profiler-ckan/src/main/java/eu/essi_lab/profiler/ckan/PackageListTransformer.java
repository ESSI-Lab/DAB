package eu.essi_lab.profiler.ckan;

import java.util.AbstractMap.SimpleEntry;

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
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
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
import eu.essi_lab.profiler.ckan.CKANRequest.APIParameters;

public class PackageListTransformer extends DiscoveryRequestTransformer {

    public PackageListTransformer() {
	// empty constructor for service loader
    }

    @Override
    public DiscoveryMessage transform(WebRequest request) throws GSException {

	DiscoveryMessage message = super.transform(request);

	CKANRequest ckanRequest = new CKANRequest(request);
	String limit = ckanRequest.getParameterValue(APIParameters.LIMIT);

	if (limit == null || limit.isEmpty()) {

	    message.setIteratedWorkflow(IterationMode.FULL_RESPONSE);
	}

	message.setSortedFields(new SortedFields(Arrays.asList(new SimpleEntry(MetadataElement.IDENTIFIER, SortOrder.ASCENDING))));

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

	ValidationMessage msg = new ValidationMessage();
	msg.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return msg;

    }

    @Override
    protected Bond getUserBond(WebRequest webRequest) throws GSException {

	Set<Bond> operands = new HashSet<>();

	CKANRequest request = new CKANRequest(webRequest);

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

	CKANRequest request = new CKANRequest(webRequest);
	int start = 1;
	int count = 10;

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

	return new CKANProfilerSetting().getServiceType();
    }

}
