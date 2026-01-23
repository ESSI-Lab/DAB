/**
 *
 */
package eu.essi_lab.profiler.os;

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

import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.lib.utils.*;

/**
 * @author Fabrizio
 */
public class OSProfilerSetting extends ProfilerSetting {

    /**
     * @author Fabrizio
     */
    public enum KeyValueOptionKeys implements LabeledEnum {

	COVERING_MODE_ENABLED("coveringMode"),//
	COVERING_MODE_MAX_ITERATIONS("coveringModeMaxIterations"), //
	COVERING_MODE_PARTITION_SIZE("coveringModePartitionSize"), //
	COVERING_MODE_PAGE_SIZE("coveringModePageSize"), //
	COVERING_MODE_COVERING_TRESHOLD("coveringModeCoveringTreshold"), //
	COVERING_MODE_PRODUCT_TYPE("coveringModeProductType"), //
	COVERING_MODE_VIEW_ONLY("coveringModeViewOnly"), //
	COVERING_MODE_TEMPORAL_CONSTRAINT("coveringModeTemporalConstraint"),

	EIFFEL_FORCE_API_DISCOVERY_OPTION("forceEiffelAPIDiscoveryOption"), //
	EIFFEL_SORT_AND_FILTER_PARTITION_SIZE("eiffelSortAndFilterPartitionSize"), //
	EIFFEL_SORT_AND_FILTER_API("eiffelSortAndFilterAPI"), //
	EIFFEL_USE_FILTER_API_CACHE("eiffelUseFilterAPICache"), //
	EIFFEL_USE_MERGED_IDS_CACHE("eiffelUseMergedIdsCache"), //
	EIFFEL_API_MAX_SORT_IDENTIFIERS("eiffelAPIMaxSortIdentifiers"), //
	EIFFEL_FILTER_AND_SORT_SPLIT_TRESHOLD("eiffelFilterAndSortSplitTreshold"),

	MAX_RESULT_WINDOW_SIZE("maxResultWindowSize");

	private String name;

	/**
	 * @param name
	 */
	private KeyValueOptionKeys(String name) {

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
    }

    /**
     *
     */
    public static final String OPEN_SEARCH_PROFILER_TYPE = "OpenSearch";

    /**
     *
     */
    public OSProfilerSetting() {

	setServiceName("OpenSearch Service");
	setServiceType(OPEN_SEARCH_PROFILER_TYPE);
	setServicePath("opensearch");
	setServiceVersion("1.1");
    }
}
