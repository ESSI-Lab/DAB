package eu.essi_lab.accessor.wod;

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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class WODConnector extends HarvestedQueryConnector<WODConnectorSetting> {

    // Example source URL: "https://data.nodc.noaa.gov/ncei/wod/";

    /**
     * 
     */
    public static final String TYPE = "WODConnector";
    private static final String WOD_CONNECTOR_GET_NC_LINKS_ERROR = "WOD_CONNECTOR_GET_NC_LINKS_ERROR";
    private static final String WOD_CONNECTOR_DOWNLOAD_FAILED_ERROR = "WOD_CONNECTOR_DOWNLOAD_FAILED_ERROR";
    private static final String WOD_CONNECTOR_LIST_RECORDS_ERROR = "WOD_CONNECTOR_LIST_RECORDS_ERROR";

    @Override
    public boolean supports(GSSource source) {
	return source.getEndpoint().toLowerCase().contains("wod");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	String token = request.getResumptionToken();

	initializeFileList();

	if (token == null) {
	    token = "0";
	}

	Integer index = Integer.parseInt(token);

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();

	Optional<Integer> optionalMaxRecords = getSetting().getMaxRecords();

	if (optionalMaxRecords.isPresent()) {
	    Integer maxRecords = optionalMaxRecords.get();
	    if (maxRecords > 0 && index.equals(maxRecords)) {
		ret.setResumptionToken(null);
		return ret;
	    }
	}

	if (index == linkages.size()) {
	    ret.setResumptionToken(null);
	    return ret;
	} else {
	    ret.setResumptionToken("" + (index + 1));
	}
	String linkage = linkages.get(index);

	Optional<InputStream> optionalFileStream = downloadWithRetry(linkage);
	if (!optionalFileStream.isPresent()) {

	    throw GSException.createException(//
		    getClass(), //
		    "Download failed", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    WOD_CONNECTOR_DOWNLOAD_FAILED_ERROR//
	    );
	}
	try {
	    InputStream fileStream = optionalFileStream.get();
	    File ncFile = File.createTempFile(linkage.substring(linkage.lastIndexOf('/') + 1), ".nc");
	    FileOutputStream fos = new FileOutputStream(ncFile);
	    IOUtils.copy(fileStream, fos);
	    fileStream.close();
	    fos.close();
	    OriginalMetadata metadataRecord = new OriginalMetadata();
	    metadataRecord.setSchemeURI(CommonNameSpaceContext.GMI_NS_URI);
	    WODMetadataCreator creator = new WODMetadataCreator(ncFile, linkage);
	    MIMetadata metadata = creator.mapMetadata();
	    String mdString = metadata.asString(true);
	    metadataRecord.setMetadata(mdString);
	    ret.addRecord(metadataRecord);
	    ncFile.delete();
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    WOD_CONNECTOR_LIST_RECORDS_ERROR, //
		    e);
	}

	return ret;
    }

    public void gunzipIt(File gunzipped, File plain) throws IOException {

	GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(gunzipped));

	FileOutputStream out = new FileOutputStream(plain);

	IOUtils.copy(gzis, out);

	gzis.close();
	out.close();

    }

    private List<String> linkages = new ArrayList<String>();

    private void initializeFileList() throws GSException {

	if (!linkages.isEmpty()) {
	    return;
	}

	String endpoint = getSourceURL();
	List<String> ncLinks = getNCLinks(endpoint);

	linkages.addAll(ncLinks);

	try {
	    GSLoggerFactory.getLogger(getClass()).info("Sleeping 2 minutes before starting with downloads");
	    Thread.sleep(120000);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

    }

    private List<String> getNCLinks(String endpoint) throws GSException {
	try {
	    int mss = (int) (Math.random() * 1000.0);
	    GSLoggerFactory.getLogger(getClass()).info("Taking a little sleep first to prevent banning (" + mss + "ms)");
	    Thread.sleep(mss);
	} catch (InterruptedException e1) {
	    e1.printStackTrace();
	}
	List<String> ret = new ArrayList<String>();
	Optional<InputStream> optionalStream = downloadWithRetry(endpoint);
	if (!optionalStream.isPresent()) {
	    return ret;
	}
	List<String> links = new ArrayList<String>();
	URL base;
	try {
	    InputStream stream = optionalStream.get();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    IOUtils.copy(stream, baos);
	    String html = new String(baos.toByteArray());
	    baos.close();
	    stream.close();
	    links = extractLinks(html);
	    base = new URL(endpoint);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    WOD_CONNECTOR_GET_NC_LINKS_ERROR, //
		    e);
	}
	for (String link : links) {
	    try {
		URL childURL = new URL(base, link);
		// System.out.println("examining link: " + link);
		if (link.endsWith(".nc")) {
		    // System.out.println("NetCDF link, o.k.");
		    ret.add(childURL.toExternalForm());
		} else {
		    // System.out.println("other, exploring");
		    List<String> childNetCDFs = getNCLinks(childURL.toExternalForm());
		    ret.addAll(childNetCDFs);
		}
	    } catch (MalformedURLException e) {
		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		throw GSException.createException(//
			getClass(), //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			WOD_CONNECTOR_GET_NC_LINKS_ERROR, //
			e);
	    }
	}
	return ret;
    }

    private Optional<InputStream> downloadWithRetry(String endpoint) {
	Integer timeout = 20;
	for (int i = 0; i < 10; i++) {
	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.SECONDS, timeout);
	    Optional<InputStream> optionalHtml = downloader.downloadOptionalStream(endpoint);
	    if (optionalHtml.isPresent()) {
		return optionalHtml;
	    }
	    GSLoggerFactory.getLogger(getClass()).info("Timeout during retrieval, sleeping and retrying. #retry " + i);
	    try {
		Thread.sleep(TimeUnit.SECONDS.toMillis(20));
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
	return Optional.empty();
    }

    private List<String> extractLinks(String html) {
	List<String> ret = new ArrayList<>();
	String hrefToken = "href=\"";
	while (true) {
	    int hrefIndex = html.indexOf(hrefToken);
	    if (hrefIndex == -1) {
		break;
	    }
	    html = html.substring(hrefIndex + hrefToken.length());
	    String hrefValue = html.substring(0, html.indexOf('"'));
	    if (!hrefValue.startsWith("?") && !hrefValue.endsWith("/ncei/") && !hrefValue.endsWith("/ncei/wod/")) {
		ret.add(hrefValue);
	    }
	}
	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.GMI_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected WODConnectorSetting initSetting() {

	return new WODConnectorSetting();
    }

}
