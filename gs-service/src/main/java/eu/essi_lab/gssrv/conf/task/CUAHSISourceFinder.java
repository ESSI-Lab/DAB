package eu.essi_lab.gssrv.conf.task;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.accessor.wof.client.CUAHSIHISCentralClient;
import eu.essi_lab.accessor.wof.client.datamodel.ServiceInfo;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;

public class CUAHSISourceFinder extends SourceFinder {

    @Override
    public List<HarvestingSetting> getSources(String endpoint, String identifierPrefix) {
	List<HarvestingSetting> ret = new ArrayList<HarvestingSetting>();
	List<String> augmenterTypes = Arrays.asList(//
		"EasyAccessAugmenter", //
		"WHOSUnitsAugmenter", //
		"WHOSVariableAugmenter");

	CUAHSIHISCentralClient client = new CUAHSIHISCentralClient(endpoint);

	List<ServiceInfo> tmp;
	try {
	    tmp = client.getServicesInBox("-180", "-90", "180", "90");
	} catch (GSException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e);
	    tmp = new ArrayList<ServiceInfo>();
	}
	for (ServiceInfo info : tmp) {
	    if (//
		//
	    info.getServiceURL().contains("www4.des.state.nh.us/WaterOneFlow/cuahsi_1_1.asmx")// 403 forbidden
	    // info.getServiceURL().contains("hydro1.gesdisc.eosdis.nasa.gov/daac-bin/his/1.0/NLDAS_FORA_002.cgi")||//
	    // info.getServiceURL().contains("hydro1.gesdisc.eosdis.nasa.gov/daac-bin/his/1.0/NLDAS_NOAH_002")||//
	    // info.getServiceURL().contains("hydroportal.cuahsi.org/nwisgw/cuahsi_1_1.asmx") // usgs ground water
	    ) {
		continue;
	    }
	    HarvestingSetting harvestingSetting = createSetting(//
		    "CUAHSIHISServer", //
		    Optional.empty(), //
		    identifierPrefix + "-" + info.getServiceID().trim(), //
		    "HIS-Central: " + info.getTitle().trim(), //
		    info.getServiceURL(), //
		    augmenterTypes);
	    ret.add(harvestingSetting);

	}

	return ret;
    }

}
