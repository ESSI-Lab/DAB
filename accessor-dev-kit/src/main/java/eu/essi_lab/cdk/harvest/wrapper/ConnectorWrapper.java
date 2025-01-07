package eu.essi_lab.cdk.harvest.wrapper;

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

import java.util.List;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 * @param <W>
 */
@SuppressWarnings("rawtypes")
public abstract class ConnectorWrapper<W extends WrappedConnector> extends HarvestedQueryConnector<ConnectorWrapperSetting> {

    private W wrappedConnector;

    /**
     * 
     */
    public ConnectorWrapper() {

    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    private W getWrappedConnector() {

	if (wrappedConnector == null) {

	    wrappedConnector = (W) getSetting().getSelectedConnector();

	    GSLoggerFactory.getLogger(getClass()).info("Created wrapped connector: {}", wrappedConnector.getClass());

	    wrappedConnector.getSetting().setPageSize(getSetting().getPageSize());

	    getSetting().getMaxRecords().ifPresent(maxRecords -> wrappedConnector.getSetting().setMaxRecords(maxRecords));
	}

	return wrappedConnector;
    }

    @Override
    public ConnectorWrapperSetting getSetting() {

	return (ConnectorWrapperSetting) super.getSetting();
    }

    @Override
    public boolean supports(GSSource source) {

	return getWrappedConnector().supports(source);
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	return getWrappedConnector().listRecords(request);
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return getWrappedConnector().listMetadataFormats();
    }

    @Override
    public void setSourceURL(String url) {

	getWrappedConnector().setSourceURL(url);
    }

    @Override
    public String getSourceURL() {

	return getWrappedConnector().getSourceURL();
    }

}
