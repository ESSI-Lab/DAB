package eu.essi_lab.demo.profiler;

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

/**
 * @author Fabrizio
 */
public class DemoRequestTransformer extends DiscoveryRequestTransformer {

    @Override
    protected Bond getUserBond(WebRequest request) throws GSException {

	// creates the parser
	KeyValueParser parser = new KeyValueParser(request.getQueryString());

	// finds all the parameters declared in the test container class
	List<WebRequestParameter> parameters = WebRequestParameter.findParameters(DemoParameters.class);

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

	int startIndex = parseInt(parser, DemoParameters.START);
	int count = parseInt(parser, DemoParameters.COUNT);

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
		parseInt(parser, DemoParameters.START);

	    } catch (NumberFormatException ex) {

		message.setLocator(DemoParameters.START.getName());
		message.setError(ex.getMessage());
		message.setResult(ValidationResult.VALIDATION_FAILED);
	    }

	    try {
		parseInt(parser, DemoParameters.COUNT);

	    } catch (NumberFormatException ex) {

		message.setLocator(DemoParameters.COUNT.getName());
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
