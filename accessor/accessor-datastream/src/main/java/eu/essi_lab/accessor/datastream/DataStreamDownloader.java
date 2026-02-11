package eu.essi_lab.accessor.datastream;

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
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.setting.SettingUtils;
import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.TsValuesSingleVariableType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
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
 * Data downloader for DataStream logical time series.
 *
 * It uses the {@link DataStreamClient} to retrieve observations and exposes
 * them as WaterML 1.1 time series.
 */
public class DataStreamDownloader extends WMLDataDownloader {

    private static final String DATASTREAM_DOWNLOAD_ERROR = "DATASTREAM_DOWNLOAD_ERROR";

    private static final BigDecimal NO_DATA_VALUE = new BigDecimal("-9999.0");

    private DataStreamClient client;

    private final DataStreamConnector connector = new DataStreamConnector();

    @Override
    public boolean canConnect() throws GSException {

	try {
	    return online != null && online.getLinkage() != null;
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return false;
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {

	super.setOnlineResource(resource, onlineResourceId);
	if (online.getProtocol().equals(CommonNameSpaceContext.DATASTREAM_NS_URI)) {
	    this.connector.setSourceURL(resource.getSource().getEndpoint());
	    String sourceId = resource.getSource().getUniqueIdentifier();
	    HarvestingSetting harvestingSetting = ConfigurationWrapper.getHarvestingSettings(sourceId).get();
	    AccessorSetting selectedAccessorSetting = harvestingSetting.getSelectedAccessorSetting();
	    HarvestedConnectorSetting harvestedConnectorSetting = selectedAccessorSetting.getHarvestedConnectorSetting();
	    DataStreamConnectorSetting set = SettingUtils.downCast(harvestedConnectorSetting, DataStreamConnectorSetting.class);
	    String apiKey = set.getApiKey();
	    this.client = new DataStreamClient(resource.getSource().getEndpoint(), apiKey);
	}
    }

    @Override
    public boolean canDownload() {

	return (online.getLinkage() != null && online.getProtocol() != null
		&& online.getProtocol().equals(CommonNameSpaceContext.DATASTREAM_NS_URI));

    }

    @Override
    public boolean canSubset(String dimensionName) {
	if (dimensionName == null) {
	    return false;
	}

	return DataDescriptor.TIME_DIMENSION_NAME.equalsIgnoreCase(dimensionName);

    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	List<DataDescriptor> ret = new ArrayList<>();
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());

	String name = online.getName();
	if (name != null) {

	    GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();

	    if (bbox != null) {
		Double lat = bbox.getNorth();
		Double lon = bbox.getEast();

		descriptor.setEPSG4326SpatialDimensions(lat, lon);
		descriptor.getFirstSpatialDimension().getContinueDimension().setSize(1l);
		descriptor.getSecondSpatialDimension().getContinueDimension().setSize(1l);

		descriptor.getFirstSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
		descriptor.getFirstSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
		descriptor.getSecondSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
		descriptor.getSecondSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
	    }

	    TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();

	    if (extent != null) {

		String startDate = extent.getBeginPosition();
		String endDate = extent.getEndPosition();
		if (extent.isEndPositionIndeterminate()) {
		    endDate = ISO8601DateTimeUtils.getISO8601DateTime();
		}

		java.util.Optional<Date> optionalBegin = ISO8601DateTimeUtils.parseISO8601ToDate(startDate);
		java.util.Optional<Date> optionalEnd = ISO8601DateTimeUtils.parseISO8601ToDate(endDate);

		if (optionalBegin.isPresent() && optionalEnd.isPresent()) {
		    Date begin = optionalBegin.get();
		    Date end = optionalEnd.get();
		    descriptor.setTemporalDimension(begin, end);
		    DataDimension temporalDimension = descriptor.getTemporalDimension();
		    Long oneDayInMilliseconds = 1000 * 60 * 60 * 24l;
		    temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
		    temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
		}
	    }

	    ret.add(descriptor);
	}
	return ret;

    }

    @Override
    public File download(DataDescriptor targetDescriptor) throws GSException {

	try {

	    String name = online.getName();

	    if (name != null) {

		// Identifier encodes DOI, location and characteristic name
		DataStreamIdentifierMangler mangler = new DataStreamIdentifierMangler();
		mangler.setMangling(name);
		String doi = mangler.getDoi();
		String locationIdStr = mangler.getLocationId();
		String characteristicName = mangler.getCharacteristicName();

		Integer locationId = locationIdStr != null ? Integer.parseInt(locationIdStr) : null;

		DataDimension dimension = targetDescriptor.getTemporalDimension();
		Date begin = null;
		Date end = null;
		String startString = null;
		String endString = null;
		if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		    ContinueDimension sizedDimension = dimension.getContinueDimension();
		    begin = new Date(sizedDimension.getLower().longValue());
		    end = new Date(sizedDimension.getUpper().longValue());
		    startString = ISO8601DateTimeUtils.getISO8601Date(begin);
		    endString = ISO8601DateTimeUtils.getISO8601Date(end);
		}

		if (startString == null || endString == null) {
		    startString = ISO8601DateTimeUtils
			    .getISO8601Date(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L));
		    endString = ISO8601DateTimeUtils.getISO8601Date(new Date());
		}

		// Derive years for rough server-side filtering if possible
		Integer fromYear = null;
		Integer toYear = null;
		java.util.Optional<Date> optBegin = ISO8601DateTimeUtils.parseISO8601ToDate(startString);
		java.util.Optional<Date> optEnd = ISO8601DateTimeUtils.parseISO8601ToDate(endString);
		if (optBegin.isPresent() && optEnd.isPresent()) {
		    @SuppressWarnings("deprecation")
		    int bYear = optBegin.get().getYear() + 1900;
		    @SuppressWarnings("deprecation")
		    int eYear = optEnd.get().getYear() + 1900;
		    fromYear = bYear;
		    toYear = eYear;
		}

		// Retrieve observations
		List<DataStreamClient.Observation> observations = client.getObservations(doi, locationId, characteristicName,
			fromYear, toYear, 1000);

		TimeSeriesResponseType jtst = getJaxbTimeSeriesTemplate();
		jtst.getTimeSeries().get(0).getVariable().setNoDataValue(NO_DATA_VALUE.doubleValue());
		TimeSeriesTemplate tsrt = getTimeSeriesTemplate(jtst, getClass().getSimpleName(), ".wml");

		DatatypeFactory xmlFactory = DatatypeFactory.newInstance();
		DateFormat iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		if (observations != null) {

		    ObjectFactory factory = new ObjectFactory();

		    for (DataStreamClient.Observation obs : observations) {

			if (obs.activityStartDate != null) {

			    String dateTime = obs.activityStartDate;
			    if (obs.activityStartTime != null && !obs.activityStartTime.isEmpty()) {
				dateTime = obs.activityStartDate + "T" + obs.activityStartTime;
			    }

			    java.util.Optional<Date> parsedOpt = ISO8601DateTimeUtils.parseISO8601ToDate(dateTime);
			    if (parsedOpt.isPresent()) {
				Date parsed = parsedOpt.get();

				ValueSingleVariable v = new ValueSingleVariable();

				if (obs.value != null) {
				    BigDecimal dataValue = BigDecimal.valueOf(obs.value);
				    v.setValue(dataValue);
				} else {
				    v.setValue(NO_DATA_VALUE);
				}

				GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
				c.setTime(parsed);
				XMLGregorianCalendar date2 = xmlFactory.newXMLGregorianCalendar(c);
				v.setDateTime(date2);

				addValue(tsrt, v);
			    }
			}
		    }


		    return tsrt.getDataFile();
		}
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error retrieving DataStream data", e);
	    throw GSException.createException(//
		    getClass(), //
		    DATASTREAM_DOWNLOAD_ERROR + ": " + e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DATASTREAM_DOWNLOAD_ERROR);
	}

	throw GSException.createException(//
		getClass(), //
		DATASTREAM_DOWNLOAD_ERROR + ": unable to download data", //
		null, //
		ErrorInfo.ERRORTYPE_SERVICE, //
		ErrorInfo.SEVERITY_ERROR, //
		DATASTREAM_DOWNLOAD_ERROR);
    }
}

