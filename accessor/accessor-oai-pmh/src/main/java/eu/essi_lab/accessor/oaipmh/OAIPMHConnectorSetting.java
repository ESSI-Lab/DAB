package eu.essi_lab.accessor.oaipmh;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.option.ValuesLoader;

/**
 * @author Fabrizio
 */
public class OAIPMHConnectorSetting extends HarvestedConnectorSetting {

    private static final String PREFERRED_PREFIX_OPTION_KEY = "preferredPrefix";
    private static final String SET_NAME_OPTION_KEY = "setName";

    public OAIPMHConnectorSetting() {

	setConfigurableType(OAIPMHConnector.CONNECTOR_TYPE);

	{

	    Option<String> option = StringOptionBuilder.get().//
		    withLabel("Preferred prefix").//
		    withKey(PREFERRED_PREFIX_OPTION_KEY).//
		    withSingleSelection().//
		    withValuesLoader(new PrefixLoader()).//
		    cannotBeDisabled().//
		    build();

	    addOption(option);
	}

	{
	    Option<String> option = StringOptionBuilder.get().//
		    withLabel("Set name").//
		    withKey(SET_NAME_OPTION_KEY).//
		    withSingleSelection().//
		    withValuesLoader(new SetNameLoader()).//
		    cannotBeDisabled().//
		    build();

	    addOption(option);
	}
    }

    /**
     * @author Fabrizio
     */
    public static class SetNameLoader extends ValuesLoader<String> {

	@Override
	protected List<String> loadValues(Optional<String> input) throws Exception {

	    String endpoint = input.get();
	    endpoint = endpoint.endsWith("?") ? endpoint : endpoint + "?";
	    List<String> sets = OAIPMHConnector.getSets(endpoint);
	    return sets;
	}

	@Override
	public String getRequestInputText() {

	    return "Please provide the service endpoint";
	}

	@Override
	public boolean requestInput() {

	    return true;
	}
    }

    /**
     * @author Fabrizio
     */
    public static class PrefixLoader extends ValuesLoader<String> {

	@Override
	protected List<String> loadValues(Optional<String> input) throws Exception {

	    String endpoint = input.get();
	    endpoint = endpoint.endsWith("?") ? endpoint : endpoint + "?";
	    List<String> metadataFormats = OAIPMHConnector.listMetadataFormats(endpoint);
	    return metadataFormats;
	}

	@Override
	public String getRequestInputText() {

	    return "Please provide the service endpoint";
	}

	@Override
	public boolean requestInput() {

	    return true;
	}
    }

    /**
     * @param object
     */
    public OAIPMHConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public OAIPMHConnectorSetting(String object) {

	super(object);
    }

    /**
     * @param name
     */
    public void setSetName(String name) {

	getOption(SET_NAME_OPTION_KEY, String.class).get().setValue(name);
    }

    /**
     * @return
     */
    public Optional<String> getSetName() {

	return getOption(SET_NAME_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param prefix
     */
    public void setPreferredPrefix(String prefix) {

	getOption(PREFERRED_PREFIX_OPTION_KEY, String.class).get().setValue(prefix);
    }

    /**
     * @return
     */
    public Optional<String> getPreferredPrefix() {

	return getOption(PREFERRED_PREFIX_OPTION_KEY, String.class).get().getOptionalValue();
    }

    @Override
    protected String initConnectorType() {

	return OAIPMHConnector.CONNECTOR_TYPE;
    }

    @Override
    protected String initSettingName() {

	return "OAIPMH Connector settings";
    }
}
