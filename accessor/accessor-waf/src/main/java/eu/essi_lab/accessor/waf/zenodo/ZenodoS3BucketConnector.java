/**
 * 
 */
package eu.essi_lab.accessor.waf.zenodo;

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

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.w3c.dom.Node;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Roberto
 */
public class ZenodoS3BucketConnector extends HarvestedQueryConnector<ZenodoS3BucketConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "ZenodoS3BucketConnector";
    
    public static final String BASE_ZENODO_URL = "https://zenodo.org/oai2d";

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<OriginalMetadata>();

	Downloader downloader = new Downloader();
	Optional<InputStream> stream = downloader.downloadOptionalStream(getSourceURL());

	if (stream.isPresent()) {

	    InputStream inputStream = stream.get();
	    final String url = getSourceURL().endsWith("/") ? getSourceURL() : getSourceURL() + "/";

	    try {
		XMLDocumentReader reader = new XMLDocumentReader(inputStream);

		Node[] nodes = reader.evaluateNodes("//*:Key/text()");

		String u = url + nodes[0].getNodeValue();

		Downloader down = new Downloader();
		Optional<InputStream> txt = down.downloadOptionalStream(u);

		if (txt.isPresent()) {
		    String meta = IOStreamUtils.asUTF8String(txt.get());

		    String[] splittedList = meta.split("\\r?\\n");
		    for (int i = 0; i < splittedList.length; i++) {

			String dataCiteRequest = BASE_ZENODO_URL + "?verb=GetRecord&metadataPrefix=datacite4&identifier=oai:zenodo.org:" + splittedList[i];
			Optional<InputStream> dataCiteResp = down.downloadOptionalStream(dataCiteRequest);
			if (dataCiteResp.isPresent()) {
			    OriginalMetadata originalMetadata = new OriginalMetadata();
			    XMLDocumentReader dataciteRead = new XMLDocumentReader(dataCiteResp.get());
			    //String om = dataciteRead.evaluateString("//*:record");
			    
			    Node node = dataciteRead.evaluateNode("//*:record");
			    
			    String om = XMLDocumentReader.asString(node);
			    //String om = IOStreamUtils.asUTF8String(dataCiteResp.get());
			    originalMetadata.setMetadata(om);
			    originalMetadata.setSchemeURI(CommonNameSpaceContext.OAI_DATACITE_NS_URI);
			    response.addRecord(originalMetadata);

			}
		    }

		}

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	}

	return response;
    }

    @Override
    public boolean supports(GSSource source) {
	return source.getEndpoint().startsWith("https://s3.amazonaws.com/zenodo-iatlantic");
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(CommonNameSpaceContext.OAI_DATACITE_NS_URI);
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected ZenodoS3BucketConnectorSetting initSetting() {

	return new ZenodoS3BucketConnectorSetting();
    }
}
