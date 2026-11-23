package eu.essi_lab.accessor.stac.harvested;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import eu.essi_lab.cdk.harvest.wrapper.ConnectorWrapperSetting;
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.lib.utils.LabeledEnum;

public class STACConnectorWrapperSetting extends ConnectorWrapperSetting<STACConnector> {

    /**
     *
     */
    private static final String HARVEST_ITEMS_OPTION_KEY = "STACHarvestItems";

    public STACConnectorWrapperSetting() {

	super();

	Option<BooleanChoice> option = BooleanChoiceOptionBuilder.get().//
		withKey(HARVEST_ITEMS_OPTION_KEY).//
		withLabel("Full harvest: also harvest the STAC items of each collection, as child records "
			+ "(supported by: " + "BIGBANG STAC Connector" + ")").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.FALSE).//
		cannotBeDisabled().//
		build();

	addOption(option);
    }

    /**
     * @param harvestItems
     */
    public void setHarvestItems(boolean harvestItems) {

	getOption(HARVEST_ITEMS_OPTION_KEY, BooleanChoice.class).//
		ifPresent(o -> o.select(v -> v == BooleanChoice.fromBoolean(harvestItems)));
    }

    /**
     * Settings saved before the option was introduced do not have it: they default to <code>false</code>
     *
     * @return
     */
    public boolean isHarvestItems() {

	Optional<Option<BooleanChoice>> option = getOption(HARVEST_ITEMS_OPTION_KEY, BooleanChoice.class);

	return option.isPresent() && option.get().getSelectedValue() != null && BooleanChoice.toBoolean(option.get().getSelectedValue());
    }

    /**
     * Propagates the full harvest option to the selected connector
     */
    @Override
    public STACConnector getSelectedConnector() {

	STACConnector connector = super.getSelectedConnector();
	connector.setHarvestItems(isHarvestItems());

	return connector;
    }

    @Override
    protected String getDefaultConnectorType() {
	return STACConnector.TYPE;
    }

    @Override
    protected String getConnectorsTypeOptionLabel() {
	return "STAC Connector type";
    }

    @Override
    protected String getOptionKey() {
	return "STACConnectorType";
    }

    @Override
    protected Class<STACConnector> getWrappedConnectorClass() {
	return STACConnector.class;
    }

    @Override
    protected String initConnectorType() {
	return STACConnectorWrapper.TYPE;
    }

    @Override
    protected String initSettingName() {
	return "STAC Connector settings";
    }

}
