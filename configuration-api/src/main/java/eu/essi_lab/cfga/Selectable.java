/**
 * 
 */
package eu.essi_lab.cfga;

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

import java.util.function.Predicate;

import eu.essi_lab.cfga.option.UnsetSelectionModeException;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public interface Selectable<T> {

    /**
     * @author Fabrizio
     */
    public enum SelectionMode implements LabeledEnum {

	/**
	 * 
	 */
	MULTI("multi"),
	/**
	 * 
	 */
	SINGLE("single"),
	/**
	 * 
	 */
	UNSET("unset");

	private String label;

	/**
	 * @param label
	 */
	private SelectionMode(String label) {

	    this.label = label;
	}

	/**
	 * @return
	 */
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
     * @param mode
     */
    public void setSelectionMode(SelectionMode mode);

    /**
     * @return
     */
    public SelectionMode getSelectionMode();

    /**
     * Selects all the objects which satisfy the given <code>predicate</code> and
     * <i>deselects</i> all the others.<br>
     * If the {@link #getSelectionMode()} of this object is {@link SelectionMode#UNSET}, a
     * {@link UnsetSelectionModeException} <i>could</i> be thrown
     * 
     * @param predicate
     */
    public void select(Predicate<T> predicate) throws UnsetSelectionModeException;

    /**
     * Removes all the unselected objects and set the {@link #getSelectionMode()} to {@link SelectionMode#UNSET}.<br>
     * As consequence of this method call, since unselected objects have been removed, the cleaned object becomes
     * <i>read-only</i>. No changes to its
     * content <b>MUST</b> be done in order to avoid possible attempting to alter the state of removed objects.
     */
    public void clean();
}
