package eu.essi_lab.cfga.gs.setting;

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

import org.joda.time.DateTimeZone;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.Validator;

/**
 * @author Fabrizio
 */
public interface BrokeringSetting extends EditableSetting {

    /**
     * @return
     */
    Setting getAccessorsSetting();

    /**
     * @return
     * @throws Exception
     */
    default AccessorSetting getSelectedAccessorSetting() {

	return getAccessorsSetting().//
		getSettings(AccessorSetting.class).//
		stream().//
		filter(Setting::isSelected).//
		findFirst().//
		orElse(null);
    }

    /**
     * @author Fabrizio
     */
    class BrokeringSettingValidator implements Validator {

	@Override
	public ValidationResponse validate(Configuration configuration, Setting setting, ValidationContext context) {

	    BrokeringSetting brokeringSetting = (BrokeringSetting) SettingUtils.downCast(setting, setting.getSettingClass());

	    ValidationResponse validationResponse = brokeringSetting.getSelectedAccessorSetting().validate(configuration, context).get();

	    if (brokeringSetting instanceof HarvestingSetting harv) {

		Scheduling scheduling = harv.getScheduling();

		DateTimeZone userDateTimeZone = ConfigurationWrapper.getSchedulerSetting().getUserDateTimeZone();

		Scheduling.validate(scheduling, userDateTimeZone, validationResponse);
	    }

	    return validationResponse;
	}
    }

    /**
     * @param scheduling
     * @return
     */
    static boolean compareScheduling(Scheduling scheduling, String identifier) {

	Scheduling currentScheduling = ConfigurationWrapper.getHarvestingSettings().//
		stream().//
		filter(s -> s.getIdentifier().equals(identifier)).//
		findFirst().//
		get().//
		getScheduling();

	Setting clone = scheduling.clone();

	SelectionUtils.deepClean(clone);

	return !currentScheduling.equals(clone);
    }

    /**
     * @return
     */
    default Validator createValidator() {

	return new BrokeringSettingValidator();
    }
}
