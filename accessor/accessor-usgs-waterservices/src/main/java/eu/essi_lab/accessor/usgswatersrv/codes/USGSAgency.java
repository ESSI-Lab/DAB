package eu.essi_lab.accessor.usgswatersrv.codes;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.IOException;
import java.util.HashMap;

public class USGSAgency extends USGSCode {

    private static USGSAgency instance = null;

    public static USGSAgency getInstance() {
	if (instance == null) {
	    instance = new USGSAgency();
	}
	return instance;
    }

    private USGSAgency() {
	super("agency_cd");

    }

    public static void main(String[] args) throws IOException {

	HashMap<String, String> properties = USGSAgency.getInstance().getProperties("USGS");
	System.out.println(properties.keySet());
	System.out.println(properties.values());
    }

    @Override
    public String getRetrievalURL() {
	return "https://help.waterdata.usgs.gov/code/agency_cd_query?fmt=rdb";
    }

    @Override
    public String getLocalResource() {
	return "usgs/agency_cd.txt";
    }

}