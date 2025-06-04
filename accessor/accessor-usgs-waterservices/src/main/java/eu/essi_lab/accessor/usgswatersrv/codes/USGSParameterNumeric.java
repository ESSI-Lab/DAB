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

public class USGSParameterNumeric extends USGSCode {

    private static USGSParameterNumeric instance = null;

    public static USGSParameterNumeric getInstance() {
	if (instance == null) {
	    instance = new USGSParameterNumeric();
	}
	return instance;
    }

    private USGSParameterNumeric() {
	super("parm_cd");

	// values:
	// parm_cd group parm_nm epa_equivalence result_statistical_basis result_time_basis result_weight_basis
	// result_particle_size_basis result_sample_fraction result_temperature_basis CASRN SRSName parm_unit

    }

    @Override
    public String getRetrievalURL() {
	return "https://help.waterdata.usgs.gov/code/parameter_cd_query?fmt=rdb&group_cd=%25";
    }

    @Override
    public String getLocalResource() {
	return "usgs/parms_numeric_cd.txt";
    }

}
