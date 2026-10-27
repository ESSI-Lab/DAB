package eu.essi_lab.accessor.enhydris;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.util.Optional;

/**
 * Known Enhydris instances, with the information (publisher, country, licence) not available from the Enhydris API
 *
 * @author boldrini
 */
public enum EnhydrisInstance {

    /**
     * OpenHi.net, the Greek open hydrological information system of the HIMIOFoTS national research infrastructure (coordinated by
     * NTUA, with HCMR, NOA and other partners)
     */
    OPENHI(//
	    "openhi.net", //
	    "OpenHi.net - Open Hydrological Information (HIMIOFoTS national research infrastructure)", //
	    "Greece", //
	    "Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0) - https://creativecommons.org/licenses/by-sa/4.0/"
		    + " - Data source: OpenHi.net (https://openhi.net/licence-el/)");

    private final String host;
    private final String publisher;
    private final String country;
    private final String license;

    /**
     * @param host
     * @param publisher
     * @param country
     * @param license
     */
    private EnhydrisInstance(String host, String publisher, String country, String license) {

	this.host = host;
	this.publisher = publisher;
	this.country = country;
	this.license = license;
    }

    /**
     * @return
     */
    public String getPublisher() {

	return publisher;
    }

    /**
     * @return
     */
    public String getCountry() {

	return country;
    }

    /**
     * @return
     */
    public String getLicense() {

	return license;
    }

    /**
     * @param endpoint
     * @return the instance of the given endpoint, if known
     */
    public static Optional<EnhydrisInstance> decode(String endpoint) {

	if (endpoint == null) {
	    return Optional.empty();
	}

	for (EnhydrisInstance instance : values()) {
	    if (endpoint.contains(instance.host)) {
		return Optional.of(instance);
	    }
	}

	return Optional.empty();
    }
}
