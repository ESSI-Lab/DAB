package eu.essi_lab.accessor.polytope.metadata;

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

import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author Fabrizio
 */
public class PolytopeIonBeamMetadataConnectorSetting extends HarvestedConnectorSetting {

    private static final String POLYTOPE_PASSWORD_OPTION_KEY = "polytopepwdoptionkey";
    private static final String POLYTOPE_USERNAME_OPTION_KEY = "polytopeuseroptionkey";
    private static final int DEFAULT_PAGE_SIZE = 50;

    /**
     * 
     */
    public PolytopeIonBeamMetadataConnectorSetting() {

	Option<String> option = StringOptionBuilder.//
		get().//
		withKey(POLYTOPE_PASSWORD_OPTION_KEY).//
		withLabel("Polytope IonBeam password").//
		required().//
		cannotBeDisabled().//
		build();
	
	Option<String> optionName = StringOptionBuilder.//
		get().//
		withKey(POLYTOPE_USERNAME_OPTION_KEY).//
		withLabel("Polytope IonBeam username").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(optionName);
	addOption(option);
    }

    /**
     * @param password
     */
    public void setPolytopePassword(String password) {

	getOption(POLYTOPE_PASSWORD_OPTION_KEY, String.class).get().setValue(password);
    }

    /**
     * @return
     */
    public Optional<String> getPolytopePassword() {

	return getOption(POLYTOPE_PASSWORD_OPTION_KEY, String.class).get().getOptionalValue();
    }
    
    /**
     * @param password
     */
    public void setPolytopeUsername(String name) {

	getOption(POLYTOPE_USERNAME_OPTION_KEY, String.class).get().setValue(name);
    }

    /**
     * @return
     */
    public Optional<String> getPolytopeUsername() {

	return getOption(POLYTOPE_USERNAME_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param object
     */
    public PolytopeIonBeamMetadataConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public PolytopeIonBeamMetadataConnectorSetting(String object) {

	super(object);
    }

    @Override
    protected String initConnectorType() {

	return PolytopeIonBeamMetadataConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "Polytope Metadata Connector settings";
    }
}
