package eu.essi_lab.cfga.setting.validation;

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
public class ValidationContext {

    /**
     * @return
     */
    public static ValidationContext put() {

	return new ValidationContext(PUT);
    }

    /**
     * @return
     */
    public static ValidationContext edit() {

	return new ValidationContext(EDIT);
    }

    /**
     * 
     */
    public static final String PUT = "PUT";
    /**
     * 
     */
    public static final String EDIT = "EDIT";

    private String context;

    /**
     *  
     */
    public ValidationContext() {
    }

    /**
     * @param context
     */
    public ValidationContext(String context) {

	this.context = context;
    }

    /**
     * @return
     */
    public String getContext() {

	return context;
    }

    /**
     * @param context
     */
    public void setContext(String context) {

	this.context = context;
    }
}
