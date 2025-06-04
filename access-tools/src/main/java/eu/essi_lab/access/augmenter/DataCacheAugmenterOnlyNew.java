package eu.essi_lab.access.augmenter;

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

import java.util.List;

import eu.essi_lab.access.datacache.DataRecord;

/**
 * @author boldrini
 * @author Fabrizio
 */
public class DataCacheAugmenterOnlyNew extends DataCacheAugmenter {

    @Override
    protected boolean checkObtainedRecords(List<DataRecord> cachedRecords) {
	if (!cachedRecords.isEmpty()) {
	    // existing records in the cache, so let's exit
	    return true;
	}
	return false;
    }

    @Override
    public String getType() {

	return "DataCacheAugmenterOnlyNew";
    }

    @Override
    protected String initName() {

	return "Data cache augmenter only new";
    }
}
