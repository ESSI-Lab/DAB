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

public class USGSStatistics extends USGSCode {

    private static USGSStatistics instance = null;

    public static USGSStatistics getInstance() {
	if (instance == null) {
	    instance = new USGSStatistics();
	}
	return instance;
    }

    private USGSStatistics() {
	super("stat_CD");
	// this list of agency codes can be retrieved from
	//
	// values:
	// * code name description

    }

    @Override
    public String getRetrievalURL() {
	return "https://help.waterdata.usgs.gov/code/stat_cd_nm_query?stat_nm_cd=%25&fmt=rdb";
    }

    @Override
    public String getLocalResource() {
	return "usgs/stat_cd.txt";
    }

}
