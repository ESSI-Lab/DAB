package eu.essi_lab.demo.profiler;

import java.util.Optional;

import eu.essi_lab.demo.extensions.indexes.CustomQueryable;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.CustomBondFactory;
import eu.essi_lab.pdk.wrt.WebRequestParameter;

/**
 * @author Fabrizio
 */
public interface DemoParameters {

    /**
     *
     */
    public static final WebRequestParameter START = new WebRequestParameter("start", "int", "1") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {
	    return Optional.empty();
	}
    };
    /**
     *
     */
    public static final WebRequestParameter COUNT = new WebRequestParameter("count", "int", "10") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {
	    return Optional.empty();
	}
    };
    /**
     *
     */
    public static final WebRequestParameter ONLINE_NAME = new WebRequestParameter( //
	    CustomQueryable.ONLINE_NAME.getName(), "string", null) {

	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(CustomBondFactory.createCustomBond(CustomQueryable.ONLINE_NAME, BondOperator.EQUAL, value));
	}
    };
    /**
     *
     */
    public static final WebRequestParameter CONTACT_CITY = new WebRequestParameter(//
	    CustomQueryable.CONTACT_CITY.getName(), "string", null) {

	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(CustomBondFactory.createCustomBond(CustomQueryable.CONTACT_CITY, BondOperator.EQUAL, value));
	}
    };
}
