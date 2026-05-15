package eu.essi_lab.accessor.hiscentral.emilia.simc;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.lib.utils.KVPMangler;

/**
 * Encodes ARPAE-SIMC open-data download identifiers (station, dataset resource, B-code).
 */
public class HISCentralEmiliaSimcMangler extends KVPMangler {

    private static final String PLATFORM_KEY = "platform";
    private static final String PARAMETER_KEY = "parameter";
    private static final String BCODE_KEY = "bcode";
    private static final String SOURCE_KEY = "source";

    public HISCentralEmiliaSimcMangler() {
	super(";");
    }

    public void setPlatformIdentifier(String stationId) {
	setParameter(PLATFORM_KEY, stationId);
    }

    public String getPlatformIdentifier() {
	return getParameterValue(PLATFORM_KEY);
    }

    public void setParameterIdentifier(String datasetResource) {
	setParameter(PARAMETER_KEY, datasetResource);
    }

    public String getParameterIdentifier() {
	return getParameterValue(PARAMETER_KEY);
    }

    public void setBcode(String bcode) {
	setParameter(BCODE_KEY, bcode);
    }

    public String getBcode() {
	return getParameterValue(BCODE_KEY);
    }

    public void setSourceIdentifier(String sourceIdentifier) {
	setParameter(SOURCE_KEY, sourceIdentifier);
    }

    public String getSourceIdentifier() {
	return getParameterValue(SOURCE_KEY);
    }
}
