package eu.essi_lab.configuration;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
 * @author ilsanto
 */
public enum ExecutionMode {

    /**
     * This mode disables execution of batch jobs and enables execution of incoming requests
     */
    FRONTEND,

    /**
     * This mode disables execution of incoming requests and enables execution of batch jobs in general (harvestings,
     * bulk download requests)
     */
    BATCH,

    /**
     * This mode disables execution of incoming requests and enables execution of single download jobs
     */
    ACCESS,
    /**
     * 
     */
    CONFIGURATION,
    /**
     * 
     */
    LOCAL_PRODUCTION, 

    /**
     * (default) This mode enables execution of batch jobs AND of incoming requests
     */
    MIXED;

    /**
     * @param value
     * @return
     */
    public static Optional<ExecutionMode> decode(String value) {

	return Arrays.asList(values()).//
		stream().//
		filter(e -> e.toString().equalsIgnoreCase(value)).//
		findFirst();
    }
}
