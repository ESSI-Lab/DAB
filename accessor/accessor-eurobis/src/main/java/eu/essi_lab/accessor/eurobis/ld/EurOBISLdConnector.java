package eu.essi_lab.accessor.eurobis.ld;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;

import com.amazonaws.util.IOUtils;

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
public class EurOBISLdConnector extends HarvestedQueryConnector<EurOBISLdConnectorSetting> {

	/**
	 * 
	 */
	public static final String TYPE = "EurOBISLdConnector";

	private EurOBISLdClient client = null;

	private Logger logger = GSLoggerFactory.getLogger(getClass());

	public EurOBISLdClient getClient() {
		return client;
	}

	public void setClient(EurOBISLdClient client) {
		this.client = client;
	}

	@Override
	public boolean supports(GSSource source) {
		String endpoint = source.getEndpoint();
		return endpoint.contains("marineinfo");
	}

	@Override
	public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
		logger.info("Initialization Client");

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
		logger.info("EUROBIS Collections SIZE: " + client.getDatasetURIs().size());
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
				result = client.getMetadata(i, ".ttl");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (result != null) {

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
					IOUtils.copy(result, baos);
					result.close();
					baos.close();
					String original = new String(baos.toByteArray());
					OriginalMetadata metadataRecord = new OriginalMetadata();
					metadataRecord.setMetadata(original);
					metadataRecord.setSchemeURI(CommonNameSpaceContext.EUROBIS_LD_NS_URI);
					ret.addRecord(metadataRecord);
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("Unable to download dataset",e);
				}
				
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
		ret.add(CommonNameSpaceContext.EUROBIS_LD_NS_URI);
		return ret;
	}

	private void initClient() throws GSException {
		if (client == null)
			try {
				client = new EurOBISLdClient(getSourceURL());
			} catch (Exception e) {
				e.printStackTrace();
			}

		int size = client.getSize();
		logger.debug("Number of EurOBIS LD datasets: " + size);

	}

	@Override
	public String getType() {

		return TYPE;
	}

	@Override
	protected EurOBISLdConnectorSetting initSetting() {

		return new EurOBISLdConnectorSetting();
	}

}
