package eu.essi_lab.augmenter.worker;

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

import eu.essi_lab.cfga.gs.ConfiguredGmailClient;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSetting;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class AugmentationReportsHandler {

    private static boolean enabled;

    /**
     * 
     */
    public AugmentationReportsHandler() {
    }

    /**
     * 
     */
    public static void enable() {

	enabled = true;
    }

    /**
     * @param start
     * @param source
     * @param isRecovering
     * @param harvestingProperties
     * @throws GSException
     */
    static void sendAugmentationEmail(//
	    boolean start, //
	    Boolean isRecovering, //
	    AugmenterWorkerSetting setting) {

	if (!enabled) {

	    return;
	}

	String subject = ConfiguredGmailClient.MAIL_REPORT_SUBJECT + ConfiguredGmailClient.MAIL_AUGMENTATION_SUBJECT
		+ (start ? "[STARTED]" : "[ENDED]");

	String message = "--- \n\n";

	message += "Name: " + setting.getAugmentationJobName() + "\n";
	message += "Max records: " + setting.getMaxRecords() + "\n";
	message += "Less recent ordering ordering set: " + setting.isLessRecentSortSet() + "\n";
	message += "Time back: " + setting.getMaxAge() + "\n";
	message += "\nSelected sources: \n";

	for (GSSource source : setting.getSelectedSources()) {

	    message += "- " + source.getLabel() + "\n";
	}
	
	message += "\nSelected augmenters: \n";

	for(AugmenterSetting set : setting.getSelectedAugmenterSettings()){
	    
	    message += "- " + set.getName() + "\n";
	}

	message += "\nRecovering: " + isRecovering + "\n";

	message += "\n---";

	ConfiguredGmailClient.sendEmail(subject, message);
    }
}
