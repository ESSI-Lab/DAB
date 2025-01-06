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

public class USGSAltitudeDatum extends USGSCode {

    private static USGSAltitudeDatum instance = null;

    public static USGSAltitudeDatum getInstance() {
	if (instance == null) {
	    instance = new USGSAltitudeDatum();
	}
	return instance;
    }

    private USGSAltitudeDatum() {
	super("Code");

    }

    @Override
    public String getRetrievalURL() {
	return "https://help.waterdata.usgs.gov/code/alt_datum_cd_query?fmt=rdb";
    }

    @Override
    public String getLocalResource() {
	return "usgs/alt_datum_cd.txt";
    }

}
