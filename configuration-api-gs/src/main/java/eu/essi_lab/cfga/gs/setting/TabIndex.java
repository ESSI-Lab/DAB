package eu.essi_lab.cfga.gs.setting;

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

/**
 * @author Fabrizio
 */
public enum TabIndex {

    /**
     * 
     */
    PROFILER_SETTING(2),
    /**
     * 
     */
    DISTRIBUTION_SETTING(1),
    /**
     * 
     */
    HARVESTING_SETTING(0),
    /**
     * 
     */
    DATABASE_SETTING(3),
    /**
     * 
     */
    SCHEDULER_VIEW_SETTING(4),
    /**
     * 
     */
    AUGMENTER_WORKER_SETTING(5),
    /**
     * 
     */
    DRIVER_SETTING(6),
    /**
     * 
     */
    OAUTH_SETTING(7),
    /**
     * 
     */
    CREDENTIALS_SETTING(8),
    /**
     * 
     */
    DOWNLOAD_SETTING(9),
    /**
     * 
     */
    SOURCE_STORAGE_SETTING(10),

    /**
     * 
     */
    SOURCE_PRIORITY_SETTING(11),
    /**
     * 
     */
    GDC_SOURCES_SETTING(12),
    /**
     * 
     */
    CUSTOM_TASKS_SETTING(13),
    /**
     * 
     */
    DATA_CACHE_CONNECTOR_SETTING(14),	
    /**
     * 
     */
    RATE_LIMITER_SETTING(15),
    /**
     * 
     */
    SYSTEM_SETTING(16);

    private int index;

    /**
     * @param index
     */
    private TabIndex(int index) {

	this.index = index;
    }

    /**
     * @return the index
     */
    public int getIndex() {

	return index;
    }
}
