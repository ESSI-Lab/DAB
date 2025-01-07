package eu.essi_lab.accessor.usgswatersrv.codes;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

public class USGSState extends USGSCode {

    private static USGSState instance = null;

    public static USGSState getInstance() {
	if (instance == null) {
	    instance = new USGSState();
	}
	return instance;
    }

    @Override
    public String getSeparator() {
	return "\\|";
    }

    public boolean hasSizeLine() {
	return false;
    }

    private USGSState() {
	super("STATE");
	// this list of agency codes can be retrieved from
	//
	// values:
	// * code name description

    }

    @Override
    public String getRetrievalURL() {
	return "https://www2.census.gov/geo/docs/reference/state.txt";
    }

    @Override
    public String getLocalResource() {
	return "usgs/state.txt";
    }

}
