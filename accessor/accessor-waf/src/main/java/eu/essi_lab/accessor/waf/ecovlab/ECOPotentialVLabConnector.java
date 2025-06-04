/**
 * 
 */
package eu.essi_lab.accessor.waf.ecovlab;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

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
 * @author Fabrizio
 */
public class ECOPotentialVLabConnector extends HarvestedQueryConnector<ECOPotentialVlabConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "ECOPotentialVLabConnector";

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

		Arrays.asList(reader.evaluateNodes("//*:Key/text()")).//
			stream().//

			map(n -> {

			    OriginalMetadata originalMetadata = null;

			    String u = url + n.getNodeValue();
			    Downloader down = new Downloader();
			    Optional<InputStream> xml = down.downloadOptionalStream(u);

			    if (xml.isPresent()) {

				try {
				    String meta = IOStreamUtils.asUTF8String(xml.get());

				    originalMetadata = new OriginalMetadata();
				    originalMetadata.setMetadata(meta);
				    originalMetadata.setSchemeURI(CommonNameSpaceContext.GMD_NS_URI);

				} catch (IOException e) {

				    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
				}
			    }

			    return originalMetadata;
			}).//
			filter(Objects::nonNull).//
			forEach(o -> response.addRecord(o));

	    } catch (SAXException | IOException | XPathExpressionException e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	}

	return response;
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("https://s3.amazonaws.com/ecopotentialmeta");
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(CommonNameSpaceContext.GMD_NS_URI);
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected ECOPotentialVlabConnectorSetting initSetting() {

	return new ECOPotentialVlabConnectorSetting();
    }
}
