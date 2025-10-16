/**
 * 
 */
package eu.essi_lab.lib.skos.expander;

import eu.essi_lab.lib.utils.LabeledEnum;

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

/**
 * @author Fabrizio
 */
public class ExpansionLimit {

    /**
     * @author Fabrizio
     */
    public enum LimitTarget implements LabeledEnum {

	/**
	 * 
	 */
	CONCEPTS("Concepts"),

	/**
	 * 
	 */
	ALT_LABELS("Alternate labels"),

	/**
	 * 
	 */
	LABELS("Labels");

	private String label;

	/**
	 * @param label
	 */
	private LimitTarget(String label) {

	    this.label = label;
	}

	@Override
	public String getLabel() {

	    return this.label;
	}
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
	out.limit = limit == 0 ? Integer.MAX_VALUE : limit;
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

    @Override
    public String toString() {

	return "[" + target + ":" + limit + "]";
    }

    /**
     * 
     */
    private ExpansionLimit() {

    }

}
