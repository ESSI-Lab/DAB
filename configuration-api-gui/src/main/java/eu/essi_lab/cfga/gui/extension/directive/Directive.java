package eu.essi_lab.cfga.gui.extension.directive;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
public abstract class Directive {

    private String name;

    /**
     * @author Fabrizio
     */
    public enum ConfirmationPolicy {

	/**
	 * 
	 */
	NEVER,
	/**
	 * 
	 */
	ALWAYS,
	/**
	 * 
	 */
	ON_WARNINGS;
    };

    private ConfirmationPolicy confirmationPolicy;

    public Directive() {
    }

    /**
     * @param name
     * @param settingClass
     */
    public Directive(String name) {

	this.name = name;
    }

    /**
     * @param name
     * @param confirmationPolicy
     */
    public Directive(String name, ConfirmationPolicy confirmationPolicy) {

	this.name = name;
	this.confirmationPolicy = confirmationPolicy;
    }

    /**
     * @return the name
     */
    public String getName() {

	return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {

	this.name = name;
    }

    /**
     * @return
     */
    public ConfirmationPolicy getConfirmationPolicy() {

	return confirmationPolicy;
    }

    /**
     * @param confirmationPolicy
     */
    public void setConfirmationPolicy(ConfirmationPolicy confirmationPolicy) {

	this.confirmationPolicy = confirmationPolicy;
    }
}
