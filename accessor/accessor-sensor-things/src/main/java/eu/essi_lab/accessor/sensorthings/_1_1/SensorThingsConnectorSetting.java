package eu.essi_lab.accessor.sensorthings._1_1;

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

import org.json.JSONObject;

import eu.essi_lab.accessor.sensorthings._1_1.mapper.HydroServer2Mapper;
import eu.essi_lab.accessor.sensorthings._1_1.mapper.SensorThingsMapper;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public class SensorThingsConnectorSetting extends HarvestedConnectorSetting {

    /**
     * 
     */
    private static final String QUOTE_IDS_OPTION_KEY = "quoteIdsOptionKey";

    /**
     * 
     */
    private static final String MAPPING_PROFILE_OPTION_KEY = "mappingProfileOptionKey";

    /**
     * 
     */
    public SensorThingsConnectorSetting() {

	Option<BooleanChoice> quoteIdsOption = BooleanChoiceOptionBuilder.get().//
		withKey(QUOTE_IDS_OPTION_KEY).//
		withLabel("Set this option to quote the entities identifiers in the SensorThings requests").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.FALSE).//
		cannotBeDisabled().//
		build();

	addOption(quoteIdsOption);

	Option<String> mappingOption = StringOptionBuilder.get().//
		withLabel("Mapping profile").//
		withKey(MAPPING_PROFILE_OPTION_KEY).//
		withSingleSelection().//
		withValues(SensorThingsMapper.getNames()).//
		withSelectedValue(new HydroServer2Mapper().getProfileName()).//
		cannotBeDisabled().//
		build();

	addOption(mappingOption);
    }

    /**
     * @param object
     */
    public SensorThingsConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public SensorThingsConnectorSetting(String object) {

	super(object);
    }

    @Override
    protected String initConnectorType() {

	return SensorThingsConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "SensorThings Connector settings";
    }

    /**
     * @param set
     */
    public void setQuoteIdentifiers(boolean set) {

	getOption(QUOTE_IDS_OPTION_KEY, BooleanChoice.class).get().select(v -> v == BooleanChoice.fromBoolean(set));
    }

    /**
     * @return
     */
    public boolean isQuoteIdentifiersSet() {

	return BooleanChoice.toBoolean(getOption(QUOTE_IDS_OPTION_KEY, BooleanChoice.class).get().getSelectedValue());
    }

    /**
     * @return
     */
    public String getProfileSchema() {

	String selName = getOption(MAPPING_PROFILE_OPTION_KEY, String.class).get().getValue();

	return SensorThingsMapper.getSchema(selName);
    }

    public static void main(String[] args) {

	SensorThingsConnectorSetting setting = new SensorThingsConnectorSetting();
	SelectionUtils.deepClean(setting);

	System.out.println(setting);
    }

}
