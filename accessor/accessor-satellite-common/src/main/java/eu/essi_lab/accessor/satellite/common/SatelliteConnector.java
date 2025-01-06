package eu.essi_lab.accessor.satellite.common;

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

import java.util.Arrays;
import java.util.List;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public abstract class SatelliteConnector<S extends HarvestedConnectorSetting> extends HarvestedQueryConnector<S> {

    private static final String SATELLITE_CONNECTOR_COLLECTION_ADDING_ERROR = "SATELLITE_CONNECTOR_COLLECTION_ADDING_ERROR";

    /**
     * @param response
     * @throws GSException
     */
    protected void addCollections(ListRecordsResponse<OriginalMetadata> response) throws GSException {

	try {

	    List<GSResource> collections = getCollections();

	    for (GSResource collection : collections) {

		OriginalMetadata metadata = new OriginalMetadata();
		metadata.setSchemeURI(SatelliteCollectionMapper.SATELLITE_COLLECTION_SCHEME_URI);

		//
		// this is required to avoid problems with the CDATA. see GIP-152
		//
		collection.getOriginalMetadata().setMetadata("");

		metadata.setMetadata(collection.asString(true));

		response.addRecord(metadata);
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SATELLITE_CONNECTOR_COLLECTION_ADDING_ERROR, //
		    e);
	}

    }

    /**
     * @return
     */
    protected abstract List<GSResource> getCollections() throws Exception;

    /**
     * @return
     */
    protected abstract String getMetadataFormat();

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(getMetadataFormat(), SatelliteCollectionMapper.SATELLITE_COLLECTION_SCHEME_URI);
    }

    @Override
    public boolean supportsIncrementalHarvesting() throws GSException {

	return true;
    }
}
