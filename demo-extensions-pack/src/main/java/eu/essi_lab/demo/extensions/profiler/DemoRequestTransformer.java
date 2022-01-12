package eu.essi_lab.demo.extensions.profiler;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.List;
import java.util.Optional;

import eu.essi_lab.demo.extensions.DemoProvider;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.pdk.wrt.WebRequestParameter;
public class DemoRequestTransformer extends DiscoveryRequestTransformer {

    @Override
    protected Bond getUserBond(WebRequest request) throws GSException {

	// creates the parser
	KeyValueParser parser = new KeyValueParser(request.getQueryString());

	// finds all the parameters declared in the test container class
	List<WebRequestParameter> parameters = WebRequestParameter.findParameters(CustomParameters.class);

	// creates the bond list
	ArrayList<Bond> bondList = new ArrayList<>();

	for (WebRequestParameter param : parameters) {

	    // get the current parameter value
	    String value = parser.getValue(param.getName());

	    // if value is null, the current parameter is not used in the web request
	    if (value == null) {
		// using a default value
		value = param.getDefaultValue();
	    }

	    if (value != null) {

		// get the bond representation of the parameter
		Optional<Bond> bond = Optional.empty();
		try {
		    bond = param.asBond(value);
		} catch (Exception e) {    
		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}

		if (bond.isPresent()) {
		    bondList.add(bond.get());
		}
	    }
	}

	Bond bond = null;
	if (bondList.size() > 1) {
	    // groups all the bonds in a logical bond
	    bond = BondFactory.createAndBond(bondList.toArray(new Bond[] {}));
	} else if (bondList.size() == 1) {
	    bond = bondList.get(0);
	}

	return bond;
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	if (request.getQueryString() == null) {
	    return new Page(1, 10);
	}

	KeyValueParser parser = new KeyValueParser(request.getQueryString());

	int startIndex = parseInt(parser, CustomParameters.START);
	int count = parseInt(parser, CustomParameters.COUNT);

	return new Page(startIndex, count);
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();

	selector.setIndexesPolicy(IndexesPolicy.NONE);
	selector.setSubset(ResourceSubset.HARMONIZED);

	return selector;
    }

    private int parseInt(KeyValueParser parser, WebRequestParameter param) throws NumberFormatException {

	String value = parser.getValue(param.getName());
	if (value == null || value.equals("") || value.equals(KeyValueParser.UNDEFINED)) {
	    if (param.getDefaultValue() != null) {
		return Integer.parseInt(param.getDefaultValue());
	    }
	}

	return Integer.parseInt(value);
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	if (request.getQueryString() != null) {

	    KeyValueParser parser = new KeyValueParser(request.getQueryString());

	    try {
		parseInt(parser, CustomParameters.START);

	    } catch (NumberFormatException ex) {

		message.setLocator(CustomParameters.START.getName());
		message.setError(ex.getMessage());
		message.setResult(ValidationResult.VALIDATION_FAILED);
	    }

	    try {
		parseInt(parser, CustomParameters.COUNT);

	    } catch (NumberFormatException ex) {

		message.setLocator(CustomParameters.COUNT.getName());
		message.setError(ex.getMessage());
		message.setResult(ValidationResult.VALIDATION_FAILED);
	    }
	}

	return message;
    }

    @Override
    public Provider getProvider() {

	return DemoProvider.getInstance();
    }

    @Override
    public String getProfilerType() {

	return DemoProfiler.SERVICE_TYPE;
    }

}
