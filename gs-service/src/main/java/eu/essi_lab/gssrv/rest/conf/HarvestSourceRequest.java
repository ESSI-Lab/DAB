/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class HarvestSourceRequest extends ConfigRequest {

    /**
     * @author Fabrizio
     */
    public enum RepeatInterval implements LabeledEnum {

	/**
	 * 
	 */
	MINUTES("Minutes"),
	/**
	 * 
	 */
	DAYS("Days"),
	/**
	 * 
	 */
	WEEKS("Weeks"),
	/**
	 * 
	 */
	MONTHS("Months");

	private String label;

	/**
	 * @param label
	 */
	private RepeatInterval(String label) {

	    this.label = label;
	}

	@Override
	public String getLabel() {

	    return label;
	}

	@Override
	public String toString() {

	    return getLabel();
	}
    }

    /**
     * 
     */
    public HarvestSourceRequest() {

	super("HarvestSourceRequest");
    }

    @Override
    protected List<Parameter> getSupportedParameters() {

	ArrayList<Parameter> list = new ArrayList<>();

	list.add(Parameter.of("sourceId", ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE, true));

	list.add(Parameter.of("startTime", ContentType.ISO8601_DATE_TIME, false));

	list.add(Parameter.of("repeatInterval", ContentType.INTEGER, false));

	list.add(Parameter.of("repeatIntervalUnit", ContentType.TEXTUAL, RepeatInterval.class, false));

	return list;
    }

    @Override
    protected void mandatoryCheck() {

	super.mandatoryCheck();

	List<String> parameters = readParameters();

	if (!parameters.contains("repeatInterval") && parameters.contains("repeatIntervalUnit")) {

	    throw new IllegalArgumentException(
		    "Missing parameter 'repeatInterval' which is mandatory when 'repeatIntervalUnit' is provided");
	}

	if (parameters.contains("repeatInterval") && !parameters.contains("repeatIntervalUnit")) {

	    throw new IllegalArgumentException(
		    "Missing parameter 'repeatIntervalUnit' which is mandatory when 'repeatInterval' is provided");
	}
    }
}
