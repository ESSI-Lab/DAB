/**
 * 
 */
package eu.essi_lab.accessor.gsfc;

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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
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
public class NasaGSFCConnector extends HarvestedQueryConnector<NasaGSFCConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "NasaGSFCConnector";

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	try {
	    OriginalMetadata massdeficit = createMetadata("massdeficit.xml");
	    OriginalMetadata ozminn = createMetadata("ozminn.xml");
	    OriginalMetadata ozoneholearea = createMetadata("ozoneholearea.xml");
	    OriginalMetadata ozonemaxn = createMetadata("ozonemaxn.xml");
	    OriginalMetadata ozoneminimum = createMetadata("ozoneminimum.xml");
	    OriginalMetadata polarcap = createMetadata("polarcap.xml");

	    response.addRecord(massdeficit);
	    response.addRecord(ozminn);
	    response.addRecord(ozoneholearea);
	    response.addRecord(ozonemaxn);
	    response.addRecord(ozoneminimum);
	    response.addRecord(polarcap);

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	}

	return response;
    }

    private OriginalMetadata createMetadata(String fileName)
	    throws UnsupportedEncodingException, TransformerException, SAXException, IOException {

	String metadata = new XMLDocumentReader(getClass().getClassLoader().getResourceAsStream("datasets/" + fileName)).asString();
	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(metadata);
	originalMetadata.setSchemeURI(CommonNameSpaceContext.GMD_NS_URI);

	return originalMetadata;
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("http://ozonewatch.gsfc.nasa.gov/meteorology");
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
    protected NasaGSFCConnectorSetting initSetting() {

	return new NasaGSFCConnectorSetting();
    }

}
