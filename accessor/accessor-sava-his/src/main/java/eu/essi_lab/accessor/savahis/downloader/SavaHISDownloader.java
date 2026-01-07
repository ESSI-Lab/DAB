package eu.essi_lab.accessor.savahis.downloader;

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

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.w3c.dom.Node;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.accessor.savahis.SavaHISIdentifierMangler;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;

public class SavaHISDownloader extends DataDownloader {

    private static final String SAVA_HIS_DOWNLOAD_ERROR = "SAVA_HIS_DOWNLOAD_ERROR";
    private static final String SAVA_HIS_DOWNLOADER_GET_REMOTE_DESCRIPTORS_ERROR = "SAVA_HIS_DOWNLOADER_GET_REMOTE_DESCRIPTORS_ERROR";
    private String linkage;

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	this.linkage = online.getLinkage();
    }

    @Override
    public boolean canConnect() throws GSException {

	try {
	    return HttpConnectionUtils.checkConnectivity(linkage);
	} catch (URISyntaxException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return false;
    }

    @Override
    public boolean canDownload() {
	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocolWrapper.SAVAHIS.getCommonURN()));

    }

    @Override
    public boolean canSubset(String dimensionName) {

	return false;

    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	try {
	    List<DataDescriptor> ret = new ArrayList<>();
	    DataDescriptor descriptor = new DataDescriptor();
	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_2_0());
	    descriptor.setCRS(CRS.EPSG_4326());
	    String name = online.getName();
	    // we expect a name encoded by the SavaHISIdentifierMangler
	    if (name != null) {
		SavaHISIdentifierMangler mangler = new SavaHISIdentifierMangler();
		mangler.setMangling(name);
		String site = mangler.getPlatformIdentifier();
		String variable = mangler.getParameterIdentifier();
		String monitoringType = mangler.getMonitoringType();

		String url = linkage + "monitoringType=" + monitoringType + "&monitoringPoints=" + site + "&observedProperties=" + variable
			+ "&singleSerie=true";

		InputStream stream = downloadStreamWithRetry(url.trim());

		XMLDocumentReader reader = new XMLDocumentReader(stream);

		// Double lat = Double.parseDouble(reader.evaluateString(xpathExpression)mySite.getLatitude());
		// Double lon = Double.parseDouble(mySite.getLongitude());

		// descriptor.setEPSG4326SpatialDimensions(lat, lon);

		// TimeSeries timeSeries = mySite.getSeries(variableCode);

		Node[] nodes = reader.evaluateNodes("//*:time");
		Date begin = null;
		Date end = null;
		for (Node node : nodes) {
		    XMLNodeReader nodeReader = new XMLNodeReader(node);
		    String time = nodeReader.evaluateString(".");
		    Date date = parseDate(time);
		    if (begin == null || begin.after(date)) {
			begin = date;
		    }
		    if (end == null || end.before(date)) {
			end = date;
		    }

		}
		// long oneDay = 1000 * 60 * 60 * 24l;
		descriptor.setTemporalDimension(begin, end);
		// descriptor.getTemporalDimension().getContinueDimension().setLowerType(LimitType.CONTAINS);
		// descriptor.getTemporalDimension().getContinueDimension().setUpperType(LimitType.CONTAINS);

		ret.add(descriptor);

	    }

	    return ret;
	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SAVA_HIS_DOWNLOADER_GET_REMOTE_DESCRIPTORS_ERROR, //
		    e);
	}

    }

    private Date parseDate(String dateString) {

	if (dateString != null && !dateString.equals("")) {
	    Optional<Date> dateOptional = ISO8601DateTimeUtils.parseISO8601ToDate(dateString.replace(" ", "T"));
	    if (dateOptional.isPresent()) {
		Date date = dateOptional.get();
		return date;
	    }
	}
	return null;
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	String name = online.getName();
	// we expect a SavaHIS online resource name in the form as encoded by SavaHISIdentifierMangler
	if (name != null) {
	    try {
		SavaHISIdentifierMangler mangler = new SavaHISIdentifierMangler();
		mangler.setMangling(name);
		String site = mangler.getPlatformIdentifier();
		String variable = mangler.getParameterIdentifier();
		String monitoringType = mangler.getMonitoringType();

		String url = linkage + "monitoringType=" + monitoringType + "&monitoringPoints=" + site + "&observedProperties=" + variable
			+ "&singleSerie=true";

		InputStream stream = downloadStreamWithRetry(url);

		XMLDocumentReader reader = new XMLDocumentReader(stream);

		String stationCode = reader
			.evaluateString("*:Collection/*:observationMember/*:OM_Observation/*:observedProperty/@MonitoringPoint");
		String variableCode = reader
			.evaluateString("*:Collection/*:observationMember/*:OM_Observation/*:observedProperty/@ObservedProperty");

		XMLDocumentWriter writer = new XMLDocumentWriter(reader);

		Node[] timeNodes = reader.evaluateNodes("//*:time");
		for (Node timeNode : timeNodes) {
		    String time = reader.evaluateString(timeNode, ".");
		    time = time.replace(" ", "T");
		    writer.setText(timeNode, ".", time);
		}

		Map<String, String> map = new HashMap<>();
		map.put("xlink", CommonNameSpaceContext.XLINK_NS_URI);
		reader.setNamespaces(map);

		writer.addAttributesNS(
			"*:Collection/*:observationMember/*:OM_Observation/*:featureOfInterest/*:MonitoringPoint/*:sampledFeature",
			CommonNameSpaceContext.XLINK_NS_URI, "xlink:href", stationCode);
		writer.addAttributesNS("*:Collection/*:observationMember/*:OM_Observation/*:observedProperty",
			CommonNameSpaceContext.XLINK_NS_URI, "xlink:href", variableCode);

		File tmp = IOStreamUtils.tempFilefromStream(reader.asStream(), "SAVAHIS-downloader", ".xml");
		
		return tmp;

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error("Error downloading {}", name, e);

		throw GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			SAVA_HIS_DOWNLOAD_ERROR, //
			e);
	    }

	}

	throw GSException.createException(//
		getClass(), //
		"Error occurred, unable to download data", //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		SAVA_HIS_DOWNLOAD_ERROR);

    }

    private InputStream downloadStreamWithRetry(String url) throws Exception {
	HttpResponse<InputStream> response;

	for (int i = 0; i < 10; i++) {

	    response = new Downloader().downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url.trim()));

	    int statusCode = response.statusCode();

	    if (statusCode == 200) {

		InputStream stream = response.body();

		return stream;
	    } else {
		Thread.sleep(2000);
	    }

	}

	return null;

    }

}
