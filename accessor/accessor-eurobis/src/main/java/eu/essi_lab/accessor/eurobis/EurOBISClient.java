package eu.essi_lab.accessor.eurobis;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class EurOBISClient {

    private String endpoint = "http://ipt.vliz.be/eurobis/dcat";

    private Logger logger;

    public String getEndpoint() {
	return endpoint;
    }

    public void setEndpoint(String endpoint) {
	this.endpoint = endpoint;
    }

    public EurOBISClient() {
	this.logger = GSLoggerFactory.getLogger(getClass());

    }

    public EurOBISClient(String endpoint) {
	this();
	setEndpoint(endpoint);
    }

    private List<String> datasetUrls = new ArrayList<>();

    public List<String> getDatasetUrls() {
	return datasetUrls;
    }

    public void setDatasetUrls(List<String> datasetUrls) {
	this.datasetUrls = datasetUrls;
    }

    private synchronized void init() {
	if (datasetUrls.isEmpty()) {
	    Downloader downloader = new Downloader();
	    Optional<String> dcatOptional = downloader.downloadOptionalString(endpoint);
	    if (dcatOptional.isPresent()) {
		String dcat = dcatOptional.get();
		dcat = dcat.substring(dcat.indexOf("dcat:dataset"));
		dcat = dcat.substring(dcat.indexOf(" "), dcat.indexOf(";"));
		String[] split = dcat.split(",");
		datasetUrls.clear();
		for (String datasetURL : split) {
		    datasetURL = datasetURL.replace("<", "").replace(">", "").trim();
		    datasetUrls.add(datasetURL);
		}
	    }
	}
    }

    public Integer getSize() {
	init();
	return datasetUrls.size();
    }

    /**
     * Returns EML metadata associated with the given dataset index
     * 
     * @param index
     * @return
     * @throws Exception
     */
    public XMLDocumentReader getMetadata(int index) throws Exception {
	String url = getMetadataURL(index);
	Downloader downloader = new Downloader();
	Optional<InputStream> optionalStream = downloader.downloadOptionalStream(url);
	if (optionalStream.isPresent()) {
	    InputStream stream = optionalStream.get();
	    XMLDocumentReader reader = new XMLDocumentReader(stream);
	    stream.close();
	    return reader;
	}
	return null;
    }

    public String getMetadataString(int index) {
	try {
	    String url = getMetadataURL(index);
	    Downloader downloader = new Downloader();
	    Optional<String> optionalString = downloader.downloadOptionalString(url);
	    if (optionalString.isPresent()) {
		return optionalString.get();
	    }
	    return null;
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public String getMetadataURL(int index) {
	init();
	String url = datasetUrls.get(index);
	url = url.replace("resource?", "eml?");
	return url;
    }

    public String getDownloadURL(int index) throws Exception {
	init();
	String url = datasetUrls.get(index);
	url = url.replace("resource?", "archive?");
	return url;
    }

}
