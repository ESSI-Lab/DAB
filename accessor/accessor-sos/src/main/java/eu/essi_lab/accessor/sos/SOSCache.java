package eu.essi_lab.accessor.sos;

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

import java.util.HashMap;

import eu.essi_lab.jaxb.sos._2_0.CapabilitiesType;
import eu.essi_lab.jaxb.sos._2_0.GetFeatureOfInterestResponseType;
import eu.essi_lab.jaxb.sos._2_0.gda.GetDataAvailabilityResponseType;

public class SOSCache {
	protected CapabilitiesType capabilities;
	protected HashMap<String, GetFeatureOfInterestResponseType> featuresCache = new HashMap<>();
	protected HashMap<String, GetDataAvailabilityResponseType> availabilityCache = new HashMap<>();

	public CapabilitiesType getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(CapabilitiesType capabilities) {
		this.capabilities = capabilities;
	}

	public HashMap<String, GetFeatureOfInterestResponseType> getFeaturesCache() {
		return featuresCache;
	}

	public void setFeaturesCache(HashMap<String, GetFeatureOfInterestResponseType> featuresCache) {
		this.featuresCache = featuresCache;
	}

	public HashMap<String, GetDataAvailabilityResponseType> getAvailabilityCache() {
		return availabilityCache;
	}

	public void setAvailabilityCache(HashMap<String, GetDataAvailabilityResponseType> availabilityCache) {
		this.availabilityCache = availabilityCache;
	}

}
