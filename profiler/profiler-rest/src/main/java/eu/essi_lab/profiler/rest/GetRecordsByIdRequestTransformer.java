package eu.essi_lab.profiler.rest;

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

import java.util.Arrays;

import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.rest.handler.info.MessageFormat;
import eu.essi_lab.profiler.rest.handler.info.RestInfoHandler;
import eu.essi_lab.profiler.rest.handler.info.RestParameter;

/**
 * @author Fabrizio
 */
public class GetRecordsByIdRequestTransformer extends DiscoveryRequestTransformer {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	KeyValueParser parser = new KeyValueParser(request.getQueryString());

	if (!parser.isValid(RestParameter.IDENTIFIER.getName())) {

	    message.setError("Mandatory " + RestParameter.IDENTIFIER.getName() + " parameter missing");
	    message.setLocator(RestParameter.IDENTIFIER.getName());
	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    return message;
	}

	if (parser.isValid(RestParameter.INLCUDE_ORIGINAL.getName())) {

	    String value = parser.getValue(RestParameter.INLCUDE_ORIGINAL.getName());
	    if (!value.equals("true") && !value.equals("false")) {

		message.setError(
			"Invalid value for " + RestParameter.INLCUDE_ORIGINAL.getName() + " parameter. Allowed values are: true, false");
		message.setLocator(RestParameter.INLCUDE_ORIGINAL.getName());
		message.setResult(ValidationResult.VALIDATION_FAILED);
		return message;
	    }
	}

	if (parser.isValid(RestParameter.RESPONSE_FORMAT.getName())) {

	    message = RestInfoHandler.validateResponseFormat(parser.getValue(RestParameter.RESPONSE_FORMAT.getName()));
	    if (message.getResult() == ValidationResult.VALIDATION_FAILED) {
		return message;
	    }

	    if (parser.getValue(RestParameter.RESPONSE_FORMAT.getName()).equals(MessageFormat.JSON.getFormat())) {

		message.setError("JSON response format not yet supported!");
		message.setLocator(RestParameter.RESPONSE_FORMAT.getName());
		message.setResult(ValidationResult.VALIDATION_FAILED);
		return message;
	    }
	}

	if (parser.isValid(RestParameter.RESOURCE_SUBSET.getName())) {

	    String value = parser.getValue(RestParameter.RESOURCE_SUBSET.getName());
	    try {
		ResourceSubset.valueOf(value);

	    } catch (IllegalArgumentException ex) {
		message.setError("Invalid value for " + RestParameter.RESOURCE_SUBSET.getName() + " parameter. Allowed values are: "
			+ Arrays.asList(ResourceSubset.values()));
		message.setLocator(RestParameter.RESOURCE_SUBSET.getName());
		message.setResult(ValidationResult.VALIDATION_FAILED);
		return message;
	    }

	}

	String[] indexNames = new String[] {};
	if (parser.isValid(RestParameter.INDEX_NAME.getName())) {
	    String indexNameValue = parser.getValue(RestParameter.INDEX_NAME.getName());
	    indexNames = indexNameValue.split(",");
	}

	if (indexNames.length > 0 && parser.isValid(RestParameter.INDEXES_POLICY.getName())) {

	    message.setError(
		    "Both " + RestParameter.INDEXES_POLICY.getName() + " and " + RestParameter.INDEX_NAME.getName() + " were specified");
	    message.setLocator(RestParameter.INDEXES_POLICY.getName() + ", " + RestParameter.INDEX_NAME.getName());
	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    return message;
	}

	if (indexNames.length > 0) {

	    for (String name : indexNames) {

		boolean notMetadataElement = false;
		boolean notPropertyElement = false;

		try {

		    MetadataElement.fromName(name);

		} catch (IllegalArgumentException ex) {

		    notMetadataElement = true;
		}

		try {

		    ResourceProperty.fromName(name);

		} catch (IllegalArgumentException ex) {

		    notPropertyElement = true;
		}

		if (notMetadataElement && notPropertyElement) {

		    message.setError("Invalid value for " + RestParameter.INDEX_NAME.getName() + " parameter. Allowed values are: "
			    + Arrays.asList(MetadataElement.values()) + " and " + Arrays.asList(ResourceProperty.values()));
		    message.setLocator(RestParameter.INDEX_NAME.getName());
		    message.setResult(ValidationResult.VALIDATION_FAILED);
		    return message;
		}
	    }
	}

	if (parser.isValid(RestParameter.INDEXES_POLICY.getName())) {

	    String value = parser.getValue(RestParameter.INDEXES_POLICY.getName());
	    try {
		IndexesPolicy.valueOf(value);

	    } catch (IllegalArgumentException ex) {
		message.setError("Invalid value for " + RestParameter.INDEXES_POLICY.getName() + " parameter. Allowed values are: "
			+ Arrays.asList(IndexesPolicy.values()));
		message.setLocator(RestParameter.INDEXES_POLICY.getName());
		message.setResult(ValidationResult.VALIDATION_FAILED);
		return message;
	    }
	}

	return message;
    }

    @Override
    protected Bond getUserBond(WebRequest request) throws GSException {

	KeyValueParser parser = new KeyValueParser(request.getQueryString());
	String idValue = parser.getDecodedValue(RestParameter.IDENTIFIER.getName());
	String[] ids = idValue.split(",");

	Bond out = null;
	if (ids.length == 1) {

	    out = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, ids[0]);

	} else {

	    LogicalBond orBond = BondFactory.createOrBond();

	    for (String id : ids) {
		orBond.getOperands().add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, id));
	    }

	    out = orBond;
	}

	return out;
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	KeyValueParser parser = new KeyValueParser(request.getQueryString());
	ResourceSubset subset = ResourceSubset.valueOf(//
		parser.getValue(RestParameter.RESOURCE_SUBSET.getName(), ResourceSubset.FULL.name()));

	String[] indexNames = new String[] {};
	if (parser.isValid(RestParameter.INDEX_NAME.getName())) {
	    String indexNameValue = parser.getValue(RestParameter.INDEX_NAME.getName());
	    indexNames = indexNameValue.split(",");
	}

	Boolean includeOriginal = Boolean.valueOf(parser.getValue(RestParameter.INLCUDE_ORIGINAL.getName(), "true"));

	IndexesPolicy policy = IndexesPolicy.valueOf(//
		parser.getValue(RestParameter.INDEXES_POLICY.getName(), IndexesPolicy.NONE.name()));

	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(subset);
	selector.setIncludeOriginal(includeOriginal);

	if (indexNames.length == 0) {

	    selector.setIndexesPolicy(policy);

	} else {

	    for (int i = 0; i < indexNames.length; i++) {

		String name = indexNames[i];
		MetadataElement element = MetadataElement.fromName(name);
		selector.addIndex(element);
	    }
	}

	return selector;
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	KeyValueParser parser = new KeyValueParser(request.getQueryString());
	String idValue = parser.getDecodedValue(RestParameter.IDENTIFIER.getName());
	String[] ids = idValue.split(",");

	return new Page(1, ids.length);
    }

    @Override
    public String getProfilerType() {

	return new RestProfilerSetting().getServiceType();
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
