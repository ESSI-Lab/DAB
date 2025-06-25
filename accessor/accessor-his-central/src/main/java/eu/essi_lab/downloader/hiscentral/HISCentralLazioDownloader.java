package eu.essi_lab.downloader.hiscentral;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.IOUtils;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.json.JSONArray;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.hiscentral.lazio.HISCentralLazioConnector;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * @author Roberto
 */

/**
 * GET DATA REQUEST EXMPLE:
 * http://rlazio.dynalias.org/datascape/v1/data/42705?from=2015-06-01T00%3A00%3A00&type=Plausible&part=IsoTime&part=Value&part=Quality&part=QualityDescr&timing=Original&elab=None
 */

public class HISCentralLazioDownloader extends WMLDataDownloader {

    private static final String HISCENTRAL_LAZIO_DOWNLOAD_ERROR = "HISCENTRAL_LAZIO_DOWNLOAD_ERROR";

    private HISCentralLazioConnector connector;
    private Downloader downloader;

    /**
     * 
     */
    public HISCentralLazioDownloader() {

	connector = new HISCentralLazioConnector();
	downloader = new Downloader();
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	List<DataDescriptor> ret = new ArrayList<>();

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());

	//
	// spatial extent
	//
	GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();

	Double lat = bbox.getNorth();
	Double lon = bbox.getEast();

	descriptor.setEPSG4326SpatialDimensions(lat, lon);
	descriptor.getFirstSpatialDimension().getContinueDimension().setSize(1l);
	descriptor.getSecondSpatialDimension().getContinueDimension().setSize(1l);
	descriptor.getFirstSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
	descriptor.getFirstSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
	descriptor.getSecondSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
	descriptor.getSecondSpatialDimension().getContinueDimension().setUpperTolerance(0.01);

	//
	// temp extent
	//
	TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();

	String startDate = extent.getBeginPosition();
	String endDate = extent.getEndPosition();

	if (extent.isEndPositionIndeterminate()) {
	    endDate = ISO8601DateTimeUtils.getISO8601DateTime();
	}

	Optional<Date> optionalBegin = ISO8601DateTimeUtils.parseISO8601ToDate(startDate);
	Optional<Date> optionalEnd = ISO8601DateTimeUtils.parseISO8601ToDate(endDate);

	if (optionalBegin.isPresent() && optionalEnd.isPresent()) {

	    Date begin = optionalBegin.get();
	    Date end = optionalEnd.get();

	    descriptor.setTemporalDimension(begin, end);

	    DataDimension temporalDimension = descriptor.getTemporalDimension();
	    Long oneDayInMilliseconds = 1000 * 60 * 60 * 24l;

	    temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
	    temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
	}

	ret.add(descriptor);

	return ret;
    }

    @Override
    public File download(DataDescriptor targetDescriptor) throws GSException {

	Exception ex = null;

	try {

	    Date begin = null;
	    Date end = null;

	    String startString = null;
	    String endString = null;

	    DataDimension dimension = targetDescriptor.getTemporalDimension();

	    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {

		ContinueDimension sizedDimension = dimension.getContinueDimension();

		begin = new Date(sizedDimension.getLower().longValue());
		end = new Date(sizedDimension.getUpper().longValue());

		startString = ISO8601DateTimeUtils.getISO8601DateTime(begin);
		endString = ISO8601DateTimeUtils.getISO8601DateTime(end);
	    }

	    if (startString == null || endString == null) {

		startString = ISO8601DateTimeUtils.getISO8601Date(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L));
		endString = ISO8601DateTimeUtils.getISO8601Date(new Date());
	    }

	    startString = convertDate(startString);
	    endString = convertDate(endString);
	    String link = online.getLinkage().contains("?") ? online.getLinkage().split("\\?")[0] : online.getLinkage();
	    String linkage = link + "?from=" + startString + "&to=" + endString
		    + "&type=Plausible&part=IsoTime&part=Value&part=Quality&part=QualityDescr&timing=Original&elab=None";

	    File tmpDataFile = getData(linkage);

	    if (tmpDataFile != null) {

		// JSONArray valuesData = jsonArray.optJSONArray("data");

		File outputFile = File.createTempFile(getClass().getSimpleName(), ".xml");

		TimeSeriesTemplate template = getTimeSeriesTemplate(outputFile);

		GSLoggerFactory.getLogger(getClass()).info("Total size: {}", tmpDataFile.length());
		int i = 0;
		DateFormat iso8601OutputFormat = null;
		DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

		JsonFactory jsonFactory = new JsonFactory();
		JsonParser parser = jsonFactory.createParser(tmpDataFile);
		ObjectMapper mapper = new ObjectMapper();

		if (parser.nextToken() != JsonToken.START_ARRAY) {
		    throw new IllegalStateException("Expected start of top-level array");
		}

		while (parser.nextToken() != JsonToken.END_ARRAY) {
		    // Read current token (which is START_ARRAY for each sub-array)
		    JsonNode subArray = mapper.readTree(parser);
		    String rawJson = mapper.writeValueAsString(subArray);

		    if (i++ % 100000 == 0) {
			GSLoggerFactory.getLogger(getClass()).info("Partial size: {}", i);
		    }
		    JSONArray data = new JSONArray(rawJson);

		    ValueSingleVariable variable = new ValueSingleVariable();

		    //
		    // value
		    //

		    BigDecimal dataValue = data.optBigDecimal(1, new BigDecimal("-9999.0"));
		    variable.setValue(dataValue);

		    //
		    // date
		    //

		    String date = data.optString(0);// data.optString("datetime");

		    if (iso8601OutputFormat == null) {
			iso8601OutputFormat = date.contains(" ") ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALIAN)
				: new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ITALIAN);
			iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		    }

		    Date parsed = iso8601OutputFormat.parse(date);

		    if (begin != null && parsed.before(begin)) {
			continue;
		    }

		    if (end != null && parsed.after(end)) {
			continue;
		    }

		    GregorianCalendar gregCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		    gregCal.setTime(parsed);

		    XMLGregorianCalendar xmlGregCal = xmlFactory.newXMLGregorianCalendar(gregCal);
		    variable.setDateTimeUTC(xmlGregCal);

		    //
		    //
		    //

		    addValue(template, variable);

		}

		parser.close();
		tmpDataFile.delete();

		return template.getDataFile();
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    ex = e;
	}

	throw GSException.createException(//
		getClass(), //
		ex.getMessage(), //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		HISCENTRAL_LAZIO_DOWNLOAD_ERROR);

    }

    private File getData(String linkage) throws GSException {
	GSLoggerFactory.getLogger(getClass()).info("Getting BEARER TOKEN from Lazio Datascape service");

	String result = null;
	String token = null;

	try {

	    if (HISCentralLazioConnector.BEARER_TOKEN == null) {
		HISCentralLazioConnector.BEARER_TOKEN = HISCentralLazioConnector.getBearerToken(connector.getSourceURL());
	    }
	    GSLoggerFactory.getLogger(getClass()).info("Getting " + linkage);

	    int timeout = 120;
	    int responseTimeout = 200;
	    InputStream stream = null;

	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.SECONDS, timeout);
	    downloader.setResponseTimeout(TimeUnit.SECONDS, responseTimeout);

	    HttpResponse<InputStream> streamResponse = downloader.downloadResponse(//
		    linkage.trim(), //
		    HttpHeaderUtils.build("Authorization", "Bearer " + HISCentralLazioConnector.BEARER_TOKEN));

	    stream = streamResponse.body();

	    GSLoggerFactory.getLogger(getClass()).info("Got " + linkage);

	    int responseCode = streamResponse.statusCode();
	    if (responseCode > 400) {
		// repeat again
		HISCentralLazioConnector.BEARER_TOKEN = HISCentralLazioConnector.getBearerToken(connector.getSourceURL());

		streamResponse = downloader.downloadResponse(//
			linkage.trim(), //
			HttpHeaderUtils.build("Authorization", "Bearer " + HISCentralLazioConnector.BEARER_TOKEN));

		stream = streamResponse.body();

		GSLoggerFactory.getLogger(getClass()).info("Got " + linkage);
	    }

	    if (stream != null) {
		Path tmpFile = Files.createTempFile(getClass().getSimpleName(), ".json");
		FileOutputStream fos = new FileOutputStream(tmpFile.toFile());
		IOUtils.copy(stream, fos);
		stream.close();
		return tmpFile.toFile();
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + linkage);
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + linkage + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HISCENTRAL_LAZIO_DOWNLOAD_ERROR);
	}

	return null;
    }

    /**
     * CONVERT DATE FROM ISO TO CAE LAZIO SERVICE PARAMETER (YYYYMMDDHHmm)
     **/
    private String convertDate(String date) {
	// TODO Auto-generated method stub
	// 2022-09-12T09:36:00Z -> 2022-09-12T09:36:00
	// 2022-09-19T09:36:00Z -> 2022-09-19T09:36:00
	String result;
	result = date.substring(0, date.length() - 1);
	return result;
    }

    @Override
    public boolean canSubset(String dimensionName) {

	if (dimensionName == null) {
	    return false;
	}

	return DataDescriptor.TIME_DIMENSION_NAME.equalsIgnoreCase(dimensionName);
    }

    @Override
    public boolean canDownload() {

	return (online.getFunctionCode() != null && //
		online.getFunctionCode().equals("download") && //
		online.getLinkage() != null && //
		online.getLinkage().contains(HISCentralLazioConnector.BASE_URL) && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.HISCENTRAL_LAZIO_NS_URI));
    }

    @Override
    public boolean canConnect() throws GSException {

	try {
	    return HttpConnectionUtils.checkConnectivity(online.getLinkage());
	} catch (URISyntaxException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return false;
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	this.connector.setSourceURL(resource.getSource().getEndpoint());
    }
}
