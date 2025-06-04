package eu.essi_lab.cdk.harvest.wrapper;

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

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.lib.utils.StreamUtils;

/**
 * @author Fabrizio
 */
public abstract class ConnectorWrapperSetting<W extends WrappedConnector> extends HarvestedConnectorSetting {

    /**
     * 
     */
    public ConnectorWrapperSetting() {

	Option<String> option = StringOptionBuilder.get().//
		withKey(getOptionKey()).//
		withLabel(getConnectorsTypeOptionLabel()).//
		withSingleSelection().//
		withValues(getConnectorTypes()).//
		withSelectedValue(getDefaultConnectorType()).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(option);
    }

    /**
     * @return
     */
    protected abstract String getDefaultConnectorType();

    /**
     * @return
     */
    protected abstract String getConnectorsTypeOptionLabel();

    /**
     * @return
     */
    protected abstract String getOptionKey();

    /**
     * @return
     */
    protected abstract Class<W> getWrappedConnectorClass();

    /**
     * @param type
     */
    public void selectConnectorType(String type) {

	getOption(getOptionKey(), String.class).get().select(t -> t.equals(type));
    }

    /**
     * @return
     */
    public List<String> getConnectorTypes() {

	ServiceLoader<? extends W> loader = ServiceLoader.load(getWrappedConnectorClass());

	List<String> types = StreamUtils.iteratorToStream(loader.iterator()).//
		map(c -> c.getSetting().getName()).//
		sorted().//
		collect(Collectors.toList());

	return types;
    }

    /**
     * @return
     */
    public W getSelectedConnector() {

	String connectorType = getOption(getOptionKey(), String.class).get().getSelectedValue();

	ServiceLoader<? extends W> loader = ServiceLoader.load(getWrappedConnectorClass());

	W connector = StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSetting().getName().equals(connectorType)).//
		findFirst().//
		get();

	return connector;
    }
}
