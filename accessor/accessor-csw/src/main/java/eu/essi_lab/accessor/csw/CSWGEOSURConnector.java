package eu.essi_lab.accessor.csw;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

public class CSWGEOSURConnector extends CSWGetConnector {
    private static final long serialVersionUID = -4959047650181541981L;

    public CSWGEOSURConnector() {
    }

    @Override
    public String getLabel() {

	return "CSW GEO SUR Connector";
    }

    /**
     * GEO SUR doesn't want this parameter in the GET request.
     */
    @Override
    protected String getConstraintLanguageParameter() {
	return "";
    }

}
