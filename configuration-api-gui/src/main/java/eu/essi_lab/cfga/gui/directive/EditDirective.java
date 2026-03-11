package eu.essi_lab.cfga.gui.directive;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
public class EditDirective extends Directive {

    private boolean tabView;

    /**
     *
     */
    public EditDirective() {

	setName("Edit setting");

	setConfirmationPolicy(ConfirmationPolicy.ON_WARNINGS);
    }

    /**
     * @param name
     */
    public EditDirective(String name) {

	this(name, null, ConfirmationPolicy.ON_WARNINGS, false);
    }

    /**
     * @param name
     * @param tabView
     */
    public EditDirective(String name, boolean tabView) {

	this(name, null, ConfirmationPolicy.ON_WARNINGS, tabView);
    }

    /**
     * @param name
     * @param confirmationPolicy
     */
    public EditDirective(String name, ConfirmationPolicy confirmationPolicy) {

	this(name, null, confirmationPolicy, false);
    }

    /**
     * @param name
     * @param confirmationPolicy
     */
    public EditDirective(String name, ConfirmationPolicy confirmationPolicy, boolean tabView) {

	this(name, null, confirmationPolicy, tabView);
    }

    /**
     * @param name
     * @param description
     */
    public EditDirective(String name, String description) {

	this(name, description, ConfirmationPolicy.ON_WARNINGS, false);
    }

    /**
     * @param name
     * @param description
     * @param tabView
     */
    public EditDirective(String name, String description, boolean tabView) {

	this(name, description, ConfirmationPolicy.ON_WARNINGS, tabView);
    }

    /**
     * @param name
     * @param description
     * @param confirmationPolicy
     */
    public EditDirective(String name, String description, ConfirmationPolicy confirmationPolicy) {

	this(name, description, confirmationPolicy, false);
    }

    /**
     * @param name
     * @param description
     * @param confirmationPolicy
     * @param tabView
     */
    public EditDirective(String name, String description, ConfirmationPolicy confirmationPolicy, boolean tabView) {

	super(name, confirmationPolicy);

	if (description != null) {
	    setDescription(description);
	}
	setTabView(tabView);
    }

    /**
     * @return
     */
    public boolean isTabView() {

	return tabView;
    }

    /**
     * @param tabView
     */
    public void setTabView(boolean tabView) {

	this.tabView = tabView;
    }
}
