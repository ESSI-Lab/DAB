package eu.essi_lab.accessor.emobon;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class EMOBONConnector extends HarvestedQueryConnector<EMOBONConnectorSetting> {

	/**
	 * 
	 */
	public static final String TYPE = "EMOBONConnector";

	private EMOBONClient client = null;

	private Logger logger = GSLoggerFactory.getLogger(getClass());

	public EMOBONClient getClient() {
		return client;
	}

	public void setClient(EMOBONClient client) {
		this.client = client;
	}

	@Override
	public boolean supports(GSSource source) {
		String endpoint = source.getEndpoint();
		return endpoint != null && (endpoint.contains("emobon.embrc.eu") || endpoint.contains("data.emobon"));
	}

	@Override
	public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
		logger.info("Initialization EMOBON Client");

		initClient();

		int step = 10;
		String token = request.getResumptionToken();
		Integer i = null;
		if (token == null || token.equals("")) {
			i = 0;
		} else {
			i = Integer.parseInt(token);
		}

		int end = Math.min(i + step, client.getDatasetURIs().size());
		logger.info("EMOBON Collections SIZE: " + client.getDatasetURIs().size());
		ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

		Optional<Integer> optionalMaxRecords = getSetting().getMaxRecords();
		if (!getSetting().isMaxRecordsUnlimited() && optionalMaxRecords.isPresent()) {
			Integer maxRecords = optionalMaxRecords.get();
			if (i > maxRecords) {
				ret.setResumptionToken(null);
				return ret;
			}
		}

		for (; i < end; i++) {

			InputStream result = null;
			try {
				// Get combined metadata (TTL + JSON)
				String combinedMetadata = client.getCombinedMetadata(i);
				if (combinedMetadata != null && !combinedMetadata.isEmpty()) {
					OriginalMetadata metadataRecord = new OriginalMetadata();
					metadataRecord.setMetadata(combinedMetadata);
					metadataRecord.setSchemeURI(CommonNameSpaceContext.EMOBON_NS_URI);
					ret.addRecord(metadataRecord);
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Unable to download dataset", e);
			}

		}
		if (i >= client.getDatasetURIs().size() - 1) {
			ret.setResumptionToken(null);
			logger.info("TOTAL NUMBER of records added: " + i);
		} else {
			ret.setResumptionToken("" + i);
		}

		logger.info("PARTIAL NUMBER of records added: " + i);
		return ret;

	}

	@Override
	public List<String> listMetadataFormats() throws GSException {
		List<String> ret = new ArrayList<>();
		ret.add(CommonNameSpaceContext.EMOBON_NS_URI);
		return ret;
	}

	private void initClient() throws GSException {
		if (client == null)
			try {
				client = new EMOBONClient(getSourceURL());
			} catch (Exception e) {
				logger.error("Error initializing EMOBON client", e);
			    GSException ex = GSException.createException(getClass(), "client error", e);
			    throw  ex;
			}

		int size = client.getSize();
		logger.debug("Number of EMOBON datasets: " + size);

	}

	@Override
	public String getType() {

		return TYPE;
	}

	@Override
	protected EMOBONConnectorSetting initSetting() {

		return new EMOBONConnectorSetting();
	}

}

