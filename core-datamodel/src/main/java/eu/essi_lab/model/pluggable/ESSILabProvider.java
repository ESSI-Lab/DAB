package eu.essi_lab.model.pluggable;

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
 * 
 * @author Fabrizio
 *
 */
public class ESSILabProvider extends Provider {

    /**
     * Static final field to which defines the ESSI-Lab organization 
     */
    public static final String ESSI_LAB_ORGANIZATION = "ESSI-Lab";
    
    /**
     * Static final field to which defines the ESSI-Lab email 
     */
    public static final String ESSI_LAB_EMAIL = "uosfirenze@iia.cnr.it";

    public ESSILabProvider() {

	setEmail(ESSI_LAB_EMAIL);
	setOrganization(ESSI_LAB_ORGANIZATION);
    }
}
