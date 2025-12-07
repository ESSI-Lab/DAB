package eu.essi_lab.accessor.dataloggers;

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

import eu.essi_lab.lib.utils.KVPMangler;

/**
 * The Dataloggers identifier mangler is primarily used by the mapper to create the file identifier and the distribution
 * online resource name for Dataloggers resources.
 * 
 * @author Generated
 */
public class DataloggersIdentifierMangler extends KVPMangler {
    private static final String DATALOGGER_KEY = "datalogger";
    private static final String DATASTREAM_KEY = "datastream";
    private static final String VARIABLE_KEY = "variable";
    private static final String DATAPROVIDER_KEY = "dataprovider";

    public DataloggersIdentifierMangler() {
	super(";");
    }

    public void setDataloggerIdentifier(String dataloggerIdentifier) {
	setParameter(DATALOGGER_KEY, dataloggerIdentifier);
    }

    public String getDataloggerIdentifier() {
	return getParameterValue(DATALOGGER_KEY);
    }

    public void setDatastreamIdentifier(String datastreamIdentifier) {
	setParameter(DATASTREAM_KEY, datastreamIdentifier);
    }

    public String getDatastreamIdentifier() {
	return getParameterValue(DATASTREAM_KEY);
    }

    public void setVariableIdentifier(String variableIdentifier) {
	setParameter(VARIABLE_KEY, variableIdentifier);
    }

    public String getVariableIdentifier() {
	return getParameterValue(VARIABLE_KEY);
    }

    public void setDataproviderIdentifier(String dataproviderIdentifier) {
	setParameter(DATAPROVIDER_KEY, dataproviderIdentifier);
    }

    public String getDataproviderIdentifier() {
	return getParameterValue(DATAPROVIDER_KEY);
    }
}

