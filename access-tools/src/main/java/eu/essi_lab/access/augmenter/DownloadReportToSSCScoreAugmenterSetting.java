package eu.essi_lab.access.augmenter;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;

/**
 * @author Fabrizio
 */
public class DownloadReportToSSCScoreAugmenterSetting extends AugmenterSetting {

    /**
     * 
     */
    private static final String DOWNLOAD_TIME_TRESHOLD_KEY = "DOWNLOAD_TIME_TRESHOLD_KEY";
    /**
     * 
     */
    private static final String EXECUTION_TIME_TRESHOLD_KEY = "EXECUTION_TIME_TRESHOLD_KEY";

    /**
     * 
     */
    private static final int DEFAULT_DOWNLOAD_TIME_TRESHOLD = 15;
    /**
     * 
     */
    private static final int DEFAULT_EXECUTION_TIME_TRESHOLD = 5;

    /**
     * 
     */
    public DownloadReportToSSCScoreAugmenterSetting() {

	Option<Integer> option1 = new IntegerOptionBuilder().//
		withKey(DOWNLOAD_TIME_TRESHOLD_KEY).//
		withLabel("Download time (seconds) sometimes/frequently unavailable treshold").//
		withValue(DEFAULT_DOWNLOAD_TIME_TRESHOLD).//
		withSelectedValue(DEFAULT_DOWNLOAD_TIME_TRESHOLD).//
		cannotBeDisabled().//
		build();

	addOption(option1);

	Option<Integer> option2 = new IntegerOptionBuilder().//
		withKey(EXECUTION_TIME_TRESHOLD_KEY).//
		withLabel("Execution time (seconds) mostly/very reliable treshold").//
		withValue(DEFAULT_EXECUTION_TIME_TRESHOLD).//
		withSelectedValue(DEFAULT_EXECUTION_TIME_TRESHOLD).//
		cannotBeDisabled().//
		build();

	addOption(option2);
    }

    /**
     * @return
     */
    public int getDownloadTimeTreshold() {

	return getOption(DOWNLOAD_TIME_TRESHOLD_KEY, Integer.class).get().getSelectedValue();
    }

    /**
     * @return
     */
    public int getExecutionTimeTreshold() {

	return getOption(EXECUTION_TIME_TRESHOLD_KEY, Integer.class).get().getSelectedValue();
    }

    /**
     * @param object
     */
    public DownloadReportToSSCScoreAugmenterSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public DownloadReportToSSCScoreAugmenterSetting(String object) {

	super(object);
    }

}
