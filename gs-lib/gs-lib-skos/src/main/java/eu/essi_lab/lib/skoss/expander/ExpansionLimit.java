/**
 * 
 */
package eu.essi_lab.lib.skoss.expander;

/**
 * @author Fabrizio
 */
public class ExpansionLimit {

    /**
     * @author Fabrizio
     */
    public enum LimitTarget {

	/**
	 * 
	 */
	CONCEPTS,

	/**
	 * 
	 */
	PREF_LABELS,

	/**
	 * 
	 */
	ALT_LABELS,

	/**
	 * 
	 */
	LABELS;
    }

    private LimitTarget target;
    private int limit;

    /**
     * @param target
     * @param limit
     * @return
     */
    public static ExpansionLimit of(LimitTarget target, int limit) {

	ExpansionLimit out = new ExpansionLimit();
	out.target = target;
	out.limit = limit;
	return out;
    }

    /**
     * @return the target
     */
    public LimitTarget getTarget() {

	return target;
    }

    /**
     * @return the limit
     */
    public int getLimit() {

	return limit;
    }

    /**
     * 
     */
    private ExpansionLimit() {

    }

}
