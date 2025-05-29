package eu.essi_lab.accessor.usgswatersrv.codes;

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

public class USGSSiteType extends USGSCode {

    private static USGSSiteType instance = null;

    public static USGSSiteType getInstance() {
	if (instance == null) {
	    instance = new USGSSiteType();
	}
	return instance;
    }

    private USGSSiteType() {
	super("site_tp_cd");
	// this list of agency codes can be retrieved from
	//
	// headers: site_tp_cd site_tp_srt_nu site_tp_vld_fg site_tp_prim_fg site_tp_nm site_tp_ln site_tp_ds

    }

    @Override
    public String getRetrievalURL() {
	return "https://help.waterdata.usgs.gov/code/site_tp_query?fmt=rdb";
    }

    @Override
    public String getLocalResource() {
	return "usgs/site_tp_cd.txt";
    }

}
