package eu.essi_lab.accessor.sos.tahmo;

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

import java.util.Calendar;

import eu.essi_lab.accessor.sos.SOSConnector;
import eu.essi_lab.accessor.sos.downloader.SOSDownloader;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public class SOSTAHMODownloader extends SOSDownloader {
    @Override
    public String getSupportedProtocol() {
	return NetProtocols.SOS_2_0_0_TAHMO.getCommonURN();
    }
    @Override
    public SOSConnector getConnector() {
	return new SOSTAHMOConnector();
    }

    /**
     * This rewrite the generic reduceDimension method for TAHMO Sos Catalogue
     */
    @Override
    
    protected void reduceDimension(DataDimension dataDimension) {

	if (dataDimension instanceof ContinueDimension) {
		ContinueDimension continueDimension = dataDimension.getContinueDimension();

		Number upper = continueDimension.getUpper();
		

		Calendar delay = Calendar.getInstance();
		delay.setTimeInMillis(upper.longValue());
		int unroundedMinutes = delay.get(Calendar.MINUTE);
		int mod = unroundedMinutes % 5;
		delay.add(Calendar.MINUTE, unroundedMinutes == 0 ? -5 : -mod);
		delay.set(Calendar.SECOND, 0);
		delay.add(Calendar.MILLISECOND, 0);
		continueDimension.setUpper(delay.getTimeInMillis());
		delay.add(Calendar.HOUR, -12);
		continueDimension.setLower(delay.getTimeInMillis());
		continueDimension.setResolution(null);
	}
    }
}
