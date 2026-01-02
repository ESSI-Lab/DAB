/**
 * 
 */
package eu.essi_lab.accessor.test;

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

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.w3c.dom.Node;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class OAIPMHWrapperConnector extends HarvestedQueryConnector<OAIPMHWrapperConnectorSetting> {

    public static final String CONNECTOR_TYPE = "OAIPMHWrapperConnector";
    private List<String> sets;
    private int emptySets;

    private static final int SETS_STEP = 10;

    @Override
    public String getType() {

	return CONNECTOR_TYPE;
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("wrap:");
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(OAIPMHWrapperAccessorMapper.SCHEMA);
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	Downloader downloader = new Downloader();

	int index = request.getResumptionToken() != null ? Integer.valueOf(request.getResumptionToken()) : 0;

	try {

	    if (sets == null) {

		Optional<InputStream> stream = downloader
			.downloadOptionalStream("https://gs-service-production.geodab.eu/gs-service/services/oaipmh?verb=ListSets");

		XMLDocumentReader doc = new XMLDocumentReader(stream.get());

		sets = doc.evaluateTextContent("//*:setSpec/text()");
	    }

	    int end = Math.min(index + SETS_STEP, sets.size());

	    for (int i = index; i < end; i++) {

		String set = sets.get(i);

		GSLoggerFactory.getLogger(getClass()).info("SET [{}/{}] '{}' STARTED", (i + 1), sets.size(), set);

		Optional<InputStream> respStream = downloader.downloadOptionalStream(//
			"https://gs-service-production.geodab.eu/gs-service/services/oaipmh?verb=ListRecords&set=" + set
				+ "&metadataPrefix=ISO19139-2006-GMI", //
			HttpHeaderUtils.build("client_dentifier", "ESSILabClient"));

		XMLDocumentReader doc = new XMLDocumentReader(respStream.get());
		Node node = doc.evaluateNode("(//*:MI_Metadata)[1]");

		if (node == null) {
		    emptySets++;
		    continue;
		}

		MIMetadata miMetadata = new MIMetadata(node);

		OriginalMetadata metadataRecord = new OriginalMetadata();

		metadataRecord.setSchemeURI(OAIPMHWrapperAccessorMapper.SCHEMA);

		String metadata = miMetadata.asString(true);

		metadataRecord.setMetadata(metadata);

		response.addRecord(metadataRecord);

		GSLoggerFactory.getLogger(getClass()).info("SET [{}/{}] '{}' ENDED", (i + 1), sets.size(), set);
	    }

	    if (end == sets.size()) {

		response.setResumptionToken(null);
		
	    } else {

		index += SETS_STEP;

		response.setResumptionToken(String.valueOf(index));
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Dataset count: {}", response.getRecordsAsList().size());

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	}

	// GSLoggerFactory.getLogger(getClass()).info("Total sets: {}", sets.size());
	// GSLoggerFactory.getLogger(getClass()).info("Empty sets: {}", emptySets);

	return response;
    }

    @Override
    protected OAIPMHWrapperConnectorSetting initSetting() {

	return new OAIPMHWrapperConnectorSetting();
    }

}
