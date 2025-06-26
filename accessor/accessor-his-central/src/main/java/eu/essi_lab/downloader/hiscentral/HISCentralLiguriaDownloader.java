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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.IOUtils;
import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.hiscentral.liguria.HISCentralLiguriaConnector;
import eu.essi_lab.downloader.hiscentral.HISCentralLiguriaJSONPagedArrayReader.Link;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.json.JSONArrayReader;
import eu.essi_lab.lib.net.downloader.Downloader;
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
 * @author Fabrizio
 */
public class HISCentralLiguriaDownloader extends WMLDataDownloader {

    private static final String HISCENTRAL_LIGURIA_DOWNLOAD_ERROR = "HISCENTRAL_LIGURIA_DOWNLOAD_ERROR";

    private HISCentralLiguriaConnector connector;
    private Downloader downloader;

    /**
     * 
     */
    public HISCentralLiguriaDownloader() {

	connector = new HISCentralLiguriaConnector();
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

	    ObjectFactory factory = new ObjectFactory();

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

	    startString = transform(startString);
	    endString = transform(endString);
	    String linkage = online.getLinkage() + "&dtrf_beg=" + startString + "&dtrf_end=" + endString;
	    // linkage = linkage.replaceAll("Z", "");

	    boolean finished = false;
	    String var = online.getName().split("_")[2];

	    TimeSeriesTemplate template = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");
	    DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

	    while (!finished) {
		Optional<InputStream> dataResponse = downloader.downloadOptionalStream(linkage);
		if (dataResponse.isPresent()) {
		    File tmpDataFile = File.createTempFile(getClass().getSimpleName(), ".json");

		    FileOutputStream fos = new FileOutputStream(tmpDataFile);
		    InputStream stream = dataResponse.get();
		    IOUtils.copy(stream, fos);
		    stream.close();
		    HISCentralLiguriaJSONPagedArrayReader parser = new HISCentralLiguriaJSONPagedArrayReader(tmpDataFile);

		    
		    while (parser.hasNextValue()) {
			String tmpJSON = parser.nextValue();
			JSONObject data = new JSONObject(tmpJSON);

			ValueSingleVariable variable = new ValueSingleVariable();

			// TODO: get variable of interest -- see HISCentralLiguriaConnector class
			String valueString = data.optString(var);

			if (valueString != null && !valueString.isEmpty()) {

			    //
			    // value
			    //
			    // temperature and wind values need to be divided by 10
			    if (var.toLowerCase().contains("temp") || var.toLowerCase().contains("wspd")) {
				double d = Double.valueOf(valueString) / 10;
				valueString = String.valueOf(d);
			    }
			    // creek level values need to be divided by 100
			    if (var.toLowerCase().contains("crlvm")) {
				double d = Double.valueOf(valueString) / 100;
				valueString = String.valueOf(d);
			    }

			    BigDecimal dataValue = new BigDecimal(valueString);
			    variable.setValue(dataValue);

			    //
			    // date
			    //

			    String date = data.optString("dtrf");
			    Optional<Date> optionalDate = ISO8601DateTimeUtils.parseNotStandard2ToDate(date);
			    // DateFormat iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
			    // iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

			    if (optionalDate.isPresent()) {
				Date parsed = optionalDate.get();// iso8601OutputFormat.parse(date);

				GregorianCalendar gregCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
				gregCal.setTime(parsed);

				XMLGregorianCalendar xmlGregCal = xmlFactory.newXMLGregorianCalendar(gregCal);
				variable.setDateTimeUTC(xmlGregCal);

				//
				//
				//

				addValue(template, variable);
			    }
			}
		    }

		    if (!parser.hasMore()) {
			finished = true;
		    } else {
			List<Link> links = parser.getLinks();
			if (links != null) {
			    for (Link l : links) {
				String rel = l.rel;
				if (rel.contains("next")) {
				    linkage = l.href;
				    break;
				}
			    }
			} else {
			    finished = true;
			}
		    }
		    parser.close();
		    tmpDataFile.delete();
		}

	    }

	    return template.getDataFile();

	} catch (

	Exception e) {

	    ex = e;
	}

	throw GSException.createException(//

		getClass(), //
		ex.getMessage(), //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		HISCENTRAL_LIGURIA_DOWNLOAD_ERROR);

    }

    private String transform(String startString) {
	String ret = null;
	String s = startString.replace("-", "").replace("T", "");
	String[] splittedTime = s.split(":");
	if (splittedTime.length > 1) {
	    ret = splittedTime[0] + splittedTime[1];
	}
	return ret;
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
		online.getLinkage().contains(HISCentralLiguriaConnector.BASE_URL) && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.HISCENTRAL_LIGURIA_NS_URI));
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
