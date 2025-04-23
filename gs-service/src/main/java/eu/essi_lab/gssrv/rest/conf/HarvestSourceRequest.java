/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class HarvestSourceRequest extends PutSourceRequest {

    public static final String START_TIME = "startTime";
    public static final String REPEAT_INTERVAL = "repeatInterval";
    public static final String REPEAT_INTERVAL_UNIT = "repeatIntervalUnit";

    /**
     * @author Fabrizio
     */
    public enum RepeatIntervalUnit implements LabeledEnum {

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
	private RepeatIntervalUnit(String label) {

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

    /**
     * 
     */
    public HarvestSourceRequest(String name) {

	super(name);
    }

    /**
     * @param object
     */
    public HarvestSourceRequest(JSONObject object) {

	super(object);
    }

    @Override
    public List<Parameter> getSupportedParameters() {

	ArrayList<Parameter> list = new ArrayList<>();

	list.add(Parameter.of(SOURCE_ID, ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE, true));
	list.add(Parameter.of(START_TIME, ContentType.ISO8601_DATE_TIME, false));
	list.add(Parameter.of(REPEAT_INTERVAL, ContentType.INTEGER, false));
	list.add(Parameter.of(REPEAT_INTERVAL_UNIT, ContentType.TEXTUAL, RepeatIntervalUnit.class, false));

	return list;
    }

    @Override
    protected void mandatoryCheck() {

	super.mandatoryCheck();

	List<String> parameters = readParameters();

	if (!parameters.contains(REPEAT_INTERVAL) && parameters.contains(REPEAT_INTERVAL_UNIT)) {

	    throw new IllegalArgumentException(
		    "Missing parameter 'repeatInterval' which is mandatory when 'repeatIntervalUnit' is provided");
	}

	if (parameters.contains(REPEAT_INTERVAL) && !parameters.contains(REPEAT_INTERVAL_UNIT)) {

	    throw new IllegalArgumentException(
		    "Missing parameter 'repeatIntervalUnit' which is mandatory when 'repeatInterval' is provided");
	}
    }
}
