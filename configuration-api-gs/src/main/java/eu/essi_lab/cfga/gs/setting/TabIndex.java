package eu.essi_lab.cfga.gs.setting;

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

/**
 * @author Fabrizio
 */
public enum TabIndex {

    /**
     * 
     */
    HARVESTING_SETTING(0),
    /***
     * 
     */
    SOURCES_INSPECTION(1),

    /**
     * 
     */
    DISTRIBUTION_SETTING(2),
    /**
     * 
     */
    PROFILER_SETTING(3),
    /**
    * 
    */
    DATABASE_SETTING(4),
    /**
     * 
     */
    SCHEDULER_VIEW_SETTING(5),
    /**
     * 
     */
    AUGMENTER_WORKER_SETTING(6),
    /**
     * 
     */
    DRIVER_SETTING(7),
    /**
     * 
     */
    OAUTH_SETTING(8),
    /**
     * 
     */
    CREDENTIALS_SETTING(9),
    /**
     * 
     */
    DOWNLOAD_SETTING(10),
    /**
     * 
     */
    SOURCE_STORAGE_SETTING(11),

    /**
     * 
     */
    SOURCE_PRIORITY_SETTING(12),
    /**
     * 
     */
    GDC_SOURCES_SETTING(13),
    /**
     * 
     */
    CUSTOM_TASKS_SETTING(14),
    /**
     * 
     */
    DATA_CACHE_CONNECTOR_SETTING(15),
    /**
     * 
     */
    RATE_LIMITER_SETTING(16),
    /**
     * 
     */
    CONFIG_UPLOADER(17),
    /**
     * 
     */
    SYSTEM_SETTING(18),
    /**
     * 
     */
    ABOUT(19);

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
