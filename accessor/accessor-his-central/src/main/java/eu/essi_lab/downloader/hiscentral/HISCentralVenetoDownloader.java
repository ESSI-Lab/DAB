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
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.hiscentral.veneto.HISCentralVenetoConnector;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
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
 * @author Roberto
 */

/**
 * GET DATA REQUEST EXMPLE:
 * https://api.arpa.veneto.it/REST/v1/meteo_storici_tabella?anno=2022&codseq=300000313 -- precipitation data with a
 * single value value
 * https://api.arpa.veneto.it/REST/v1/meteo_storici_tabella?anno=2021&codseq=300000 -- data with min,max,medium values
 * 301
 */

public class HISCentralVenetoDownloader extends WMLDataDownloader {

    private static final String HISCENTRAL_VENETO_DOWNLOAD_ERROR = "HISCENTRAL_VENETO_DOWNLOAD_ERROR";

    private HISCentralVenetoConnector connector;

    private Downloader downloader;

    private SimpleDateFormat iso8601Format;

    /**
     * 
     */
    public HISCentralVenetoDownloader() {

	connector = new HISCentralVenetoConnector();
	downloader = new Downloader();
	this.iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	iso8601Format.setTimeZone(TimeZone.getTimeZone("GMT"));
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

	    int beginYear = 0;
	    int endYear = 0;

	    DataDimension dimension = targetDescriptor.getTemporalDimension();

	    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {

		ContinueDimension sizedDimension = dimension.getContinueDimension();

		begin = new Date(sizedDimension.getLower().longValue());
		end = new Date(sizedDimension.getUpper().longValue());
		XMLGregorianCalendar sadsad = ISO8601DateTimeUtils.getXMLGregorianCalendar(begin);

		beginYear = ISO8601DateTimeUtils.getXMLGregorianCalendar(begin).getYear();
		endYear = ISO8601DateTimeUtils.getXMLGregorianCalendar(end).getYear();

	    }

	    // if (startString == null || endString == null) {
	    //
	    // startString = ISO8601DateTimeUtils.getISO8601Date(new Date(System.currentTimeMillis() - 24 * 60 * 60 *
	    // 1000L));
	    // endString = ISO8601DateTimeUtils.getISO8601Date(new Date());
	    // }
	    //
	    // startString = convertDate(startString);
	    // endString = convertDate(endString);

	    // only one get data to call
	    String getDataURL = null;
	    List<String> getUrls = new ArrayList<String>();
	    if (beginYear < 2010) {
		beginYear = 2010;
	    }
	    if (endYear == 0 || endYear < beginYear || endYear > Calendar.getInstance().get(Calendar.YEAR)) {
		endYear = Calendar.getInstance().get(Calendar.YEAR);
	    }
	    if (beginYear == endYear) {
		if (online.getLinkage().contains("anno=" + beginYear)) {
		    getDataURL = online.getLinkage();
		    getUrls.add(getDataURL);
		} else {
		    // replace year to get data
		    String[] splittedYear = online.getLinkage().split("anno=");
		    String[] toReplace = splittedYear[1].split("&");
		    getDataURL = splittedYear[0] + "anno=" + beginYear + "&" + toReplace[1];
		    getUrls.add(getDataURL);

		}
	    } else {
		// get data for each year
		getDataURL = online.getLinkage();
		String[] splittedYear = online.getLinkage().split("anno=");
		String[] toReplace = splittedYear[1].split("&");
		for (int j = beginYear; j <= endYear; j++) {
		    String newGetDataURL = splittedYear[0] + "anno=" + j + "&" + toReplace[1];
		    getUrls.add(newGetDataURL);
		}

	    }

	    List<JSONObject> jsonObjList = getData(getUrls);

	    if (jsonObjList != null && !jsonObjList.isEmpty()) {

		TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");
		
	        DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

		for (JSONObject jsonObj : jsonObjList) {

		    JSONArray valuesData = jsonObj.optJSONArray("data");

		    if (valuesData != null) {

			for (Object arr : valuesData) {

			    JSONObject data = (JSONObject) arr;

			    String time = data.optString("dataora");

			    Date isodate = iso8601Format.parse(time);

			    if (isodate.before(begin) || isodate.after(end)) {
				continue;
			    }

			    String valueString = data.optString("valore");

			    if (valueString != null && !valueString.isEmpty()) {
				BigDecimal dataValue = null;
				if (valueString.contains(",") && valueString.contains(":")) {
				    JSONObject jsonValues = new JSONObject(valueString);
				    String name = online.getName();
				    String valueType = name.split("\\(")[1].split("\\)")[0];
				    dataValue = jsonValues.getBigDecimal(valueType);
				}else {
				    dataValue = new BigDecimal(valueString); 
				}

				ValueSingleVariable variable = new ValueSingleVariable();

				//
				// value
				//

				if (dataValue != null) {
				    variable.setValue(dataValue);
				}

				//
				// date
				//

				// String date = data.optString("datetime");
				//
				// DateFormat iso8601OutputFormat = date.contains(" ")
				// ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALIAN)
				// : new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
				//
				// iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
				//
				// Date parsed = iso8601OutputFormat.parse(date);

				GregorianCalendar gregCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
				gregCal.setTime(isodate);
			        
				XMLGregorianCalendar xmlGregCal = xmlFactory.newXMLGregorianCalendar(gregCal);
				variable.setDateTimeUTC(xmlGregCal);

				//
				//
				//

				addValue(tsrt, variable);
			    }
			}
		    }
		}

		return tsrt.getDataFile();
	    }

	} catch (Exception e) {

	    ex = e;
	}

	throw GSException.createException(//
		getClass(), //
		ex.getMessage(), //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		HISCENTRAL_VENETO_DOWNLOAD_ERROR);

    }

    private List<JSONObject> getData(List<String> linkages) throws GSException {
	GSLoggerFactory.getLogger(getClass()).info("Getting data from ARPAV Veneto:");

	List<JSONObject> jsonList = new ArrayList<JSONObject>();

	try {

	    for (String link : linkages) {

		GSLoggerFactory.getLogger(getClass()).info("Got " + link);

		Optional<String> dataResponse = downloader.downloadOptionalString(link);

		if (dataResponse.isPresent()) {
		    JSONObject obj = new JSONObject(dataResponse.get());
		    jsonList.add(obj);
		}
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + linkages);
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + linkages + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HISCENTRAL_VENETO_DOWNLOAD_ERROR);
	}

	return jsonList;
    }

    /**
     * CONVERT DATE FROM ISO TO OMNIA SERVICE PARAMETER (YYYYMMDDHHmm)
     **/
    private String convertDate(String date) {
	// TODO Auto-generated method stub
	// 2022-09-12T09:36:00Z -> 202209120936
	// 2022-09-19T09:36:00Z -> 202209190936
	String result;
	result = date.substring(0, date.length() - 4).replaceAll("-", "").replaceAll(":", "").replace("T", "");
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
		online.getLinkage().contains(HISCentralVenetoConnector.BASE_URL) && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.HISCENTRAL_VENETO_NS_URI));
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
