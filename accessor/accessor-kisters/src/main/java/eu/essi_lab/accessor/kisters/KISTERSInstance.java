package eu.essi_lab.accessor.kisters;

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

import java.util.Arrays;
import java.util.Optional;

/**
 * KiWIS instances that need specific metadata (organisation, country, use limitation), recognized by the host of their endpoint
 * 
 * @author boldrini
 */
public enum KISTERSInstance {

    /**
     * Vlaamse Milieumaatschappij, published by waterinfo.be
     */
    FLANDERS_VMM("download.waterinfo.be", //
	    "Vlaamse Milieumaatschappij (VMM) - waterinfo.be", //
	    "Belgium", //
	    "Data for personal use, the source must be mentioned: \"bron: waterinfo.be\""),

    /**
     * Hydrologisch Informatiecentrum (Flanders Hydraulics), published by waterinfo.be
     */
    FLANDERS_HIC("hicws.vlaanderen.be", //
	    "Hydrologisch Informatiecentrum (HIC) - Waterbouwkundig Laboratorium - waterinfo.be", //
	    "Belgium", //
	    "Data for personal use, the source must be mentioned: \"bron: waterinfo.be\""),

    /**
     * Service public de Wallonie, hydrometrie.wallonie.be
     */
    WALLONIA_SPW("hydrometrie.wallonie.be", //
	    "Service public de Wallonie (SPW)", //
	    "Belgium", //
	    "Reproduction allowed with attribution: \"Sources des données : Service public de Wallonie\"");

    private String host;
    private String organisation;
    private String country;
    private String useLimitation;

    /**
     * @param host
     * @param organisation
     * @param country
     * @param useLimitation
     */
    private KISTERSInstance(String host, String organisation, String country, String useLimitation) {

	this.host = host;
	this.organisation = organisation;
	this.country = country;
	this.useLimitation = useLimitation;
    }

    /**
     * @param endpoint
     * @return
     */
    public static Optional<KISTERSInstance> fromEndpoint(String endpoint) {

	if (endpoint == null) {

	    return Optional.empty();
	}

	return Arrays.stream(values()).//
		filter(i -> endpoint.contains("://" + i.host + "/")).//
		findFirst();
    }

    /**
     * @return
     */
    public String getOrganisation() {

	return organisation;
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
    public String getUseLimitation() {

	return useLimitation;
    }
}
