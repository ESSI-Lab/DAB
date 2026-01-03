package eu.essi_lab.augmenter;

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

import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author Fabrizio
 */
public class OrganizationNameSetting extends AugmenterSetting {

    /**
     * 
     */
    private static final String ORGANIZATION_NAME_OPTION_KEY = "organizationName";

    /**
     * 
     */
    public OrganizationNameSetting() {

	super();

	Option<String> option = StringOptionBuilder.get().//
		withKey(ORGANIZATION_NAME_OPTION_KEY).//
		withLabel("Organization name").//
		cannotBeDisabled().//
		build();

	addOption(option);
    }

    /**
     * @param object
     */
    public OrganizationNameSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public OrganizationNameSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public Optional<String> getOrganizationName() {

	return getOption(ORGANIZATION_NAME_OPTION_KEY, String.class).get().getOptionalValue();
    }
}
