package eu.essi_lab.access.datacache;

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

import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ServiceLoader;

import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.lib.utils.LabeledEnum;

public class DataCacheConnectorFactory {

    /**
     * @author Fabrizio
     */
    public enum DataConnectorType implements LabeledEnum {

	OPEN_SEARCH_DOCKERHUB_1_3("OpenSearch Docker HUB 1.3"), //
	OPEN_SEARCH_AWS_1_3("OpenSearch AWS 1.3");

	/**
	 * @param name
	 * @return
	 */
	public static Optional<DataConnectorType> decode(String name) {

	    return Arrays.asList(values()).//
		    stream().//
		    filter(v -> v.name().equals(name)).//
		    findFirst();
	}

	private String name;

	/**
	 * @param name
	 */
	private DataConnectorType(String name) {

	    this.name = name;
	}

	@Override
	public String toString() {

	    return getLabel();
	}

	@Override
	public String getLabel() {

	    return name;
	}
    }

    /**
     * @param setting
     * @return
     * @throws Exception
     */
    public static DataCacheConnector newDataCacheConnector(DataCacheConnectorSetting setting) throws Exception {

	return newDataCacheConnector(//
		LabeledEnum.valueOf(DataConnectorType.class, setting.getDataConnectorType()).get(), //
		new URL(setting.getDatabaseUri()), //
		setting.getDatabaseUser(), //
		setting.getDatabasePassword(), //
		setting.getDatabaseName());
    }

    /**
     * @param type
     * @param url
     * @param user
     * @param password
     * @param databaseName
     * @return
     * @throws Exception
     */
    public static synchronized DataCacheConnector newDataCacheConnector(DataConnectorType type, URL url, String user, String password,
	    String databaseName) throws Exception {
	ServiceLoader<DataCacheConnector> connectors = ServiceLoader.load(DataCacheConnector.class);
	for (DataCacheConnector connector : connectors) {
	    if (connector.supports(type)) {
		connector.initialize(url, user, password, databaseName);
		return connector;
	    }
	}
	return null;
    }

    private static DataCacheConnector dataCacheConnector = null;

    public static void setDataCacheConnector(DataCacheConnector dataCacheConnector) {
	DataCacheConnectorFactory.dataCacheConnector = dataCacheConnector;
    }

    public static DataCacheConnector getDataCacheConnector() {
	return dataCacheConnector;
    }
}
