/**
 *
 */
package eu.essi_lab.configuration;

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

import java.util.Arrays;
import java.util.Optional;

import eu.essi_lab.lib.utils.*;

/**
 * @author Fabrizio
 */
public enum ClusterType implements LabeledEnum {

    /**
     *
     */
    PRODUCTION("Production"),
    /**
     *
     */
    PRE_PRODUCTION("Preproduction"),
    /**
     *
     */
    TEST("Test"),
    /**
     *
     */
    LOCAL("Local");

    /**
     *
     */
    public static final String CLUSTER_TYPE_KEY = "cluster";

    /**
     *
     */
    private String name;

    /**
     * @param name
     */
    private ClusterType(String name) {

	this.name = name;
    }

    @Override
    public String toString() {

	return getLabel();
    }

    @Override
    public String getLabel() {

	return name;
    }

    /**
     * @return
     */
    public static ClusterType get() {

	return ClusterType.decode(SystemPropertyReader.read(CLUSTER_TYPE_KEY).orElse(null)). //
		orElse(ClusterType.LOCAL);
    }

    /**
     * @param value
     * @return
     */
    public static Optional<ClusterType> decode(String value) {

	return Arrays.stream(values()).//
		filter(e -> e.getLabel().equals(value)).//
		findFirst();
    }
}
