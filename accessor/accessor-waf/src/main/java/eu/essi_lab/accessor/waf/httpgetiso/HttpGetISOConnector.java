package eu.essi_lab.accessor.waf.httpgetiso;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.w3c.dom.Node;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * 
 * @author Fabrizio
 *
 */
public class HttpGetISOConnector extends HarvestedQueryConnector<HttpGetISOConnectorSetting> {

    public static final String TYPE = "HttpGetISOConnector";

    @Override
    
    public boolean supports(GSSource source) {
	try {
	    String endpoint = source.getEndpoint();
	    Downloader d = new Downloader();
	    Optional<InputStream> result = d.downloadOptionalStream(endpoint);
	    if (result.isPresent()) {
		XMLDocumentReader reader = new XMLDocumentReader(result.get());
		Boolean present = reader.evaluateBoolean("count(//*:MD_Metadata>0)");
		if (present) {
		    return true;
		}
		present = reader.evaluateBoolean("count(//*:MI_Metadata>0)");
		if (present) {
		    return true;
		}
	    }
	} catch (Exception e) {
	}
	return false;
    }

    @Override
    
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
	try {
	    String endpoint = getSourceURL();
	    Downloader d = new Downloader();
	    Optional<InputStream> result = d.downloadOptionalStream(endpoint);
	    if (result.isPresent()) {
		XMLDocumentReader reader = new XMLDocumentReader(result.get());
		Node node = reader.evaluateNode("//*:MD_Metadata[1]");
		if (node != null) {
		    XMLNodeReader nodeReader = new XMLNodeReader(node);
		    OriginalMetadata metadataRecord = new OriginalMetadata();
		    metadataRecord.setMetadata(nodeReader.asString());
		    metadataRecord.setSchemeURI(CommonNameSpaceContext.GMD_NS_URI);
		    ret.addRecord(metadataRecord);
		} else {
		    node = reader.evaluateNode("//*:MI_Metadata[1]");
		    XMLNodeReader nodeReader = new XMLNodeReader(node);
		    OriginalMetadata metadataRecord = new OriginalMetadata();
		    metadataRecord.setMetadata(nodeReader.asString());
		    metadataRecord.setSchemeURI(CommonNameSpaceContext.GMI_NS_URI);
		    ret.addRecord(metadataRecord);
		}

	    }
	} catch (Exception e) {
	}
	return ret;
    }

    @Override
    
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	try {
	    String endpoint = getSourceURL();
	    Downloader d = new Downloader();
	    Optional<InputStream> result = d.downloadOptionalStream(endpoint);
	    if (result.isPresent()) {
		XMLDocumentReader reader = new XMLDocumentReader(result.get());
		String name = reader.evaluateString("localname(/*[1])");
		switch (name) {
		case "MI_Metadata":
		    ret.add(CommonNameSpaceContext.GMI_NS_URI);
		    break;
		case "MD_Metadata":
		    ret.add(CommonNameSpaceContext.GMD_NS_URI);
		    break;
		}
	    }
	} catch (Exception e) {
	}
	return ret;
    }

   

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected HttpGetISOConnectorSetting initSetting() {

	return new HttpGetISOConnectorSetting();
    }

}
