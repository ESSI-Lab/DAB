package eu.essi_lab.accessor.datalakes;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
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
 * Downloads 1D Datalakes time series as WaterML 1.1.
 *
 * Grid/profile datasets are intentionally excluded.
 */
public class DatalakesDownloader extends WMLDataDownloader {

    private static final String DATALAKES_DOWNLOAD_ERROR = "DATALAKES_DOWNLOAD_ERROR";
    private static final BigDecimal NO_DATA_VALUE = new BigDecimal("-9999.0");

    private DatalakesClient client;

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
	if (online.getProtocol().equals(CommonNameSpaceContext.DATALAKES_NS_URI)) {
	    String sourceId = resource.getSource().getUniqueIdentifier();
	    HarvestingSetting harvestingSetting = ConfigurationWrapper.getHarvestingSettings(sourceId).get();
	    AccessorSetting selectedAccessorSetting = harvestingSetting.getSelectedAccessorSetting();
	    HarvestedConnectorSetting harvestedConnectorSetting = selectedAccessorSetting.getHarvestedConnectorSetting();
	    DatalakesConnectorSetting settings = SettingUtils.downCast(harvestedConnectorSetting,
		    DatalakesConnectorSetting.class);
	    this.client = new DatalakesClient(resource.getSource().getEndpoint(), settings.getApiKey());
	}
    }

    @Override
    public boolean canDownload() {
	return online.getLinkage() != null && online.getProtocol() != null
		&& online.getProtocol().equals(CommonNameSpaceContext.DATALAKES_NS_URI)
		&& "download".equalsIgnoreCase(online.getFunctionCode());
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return dimensionName != null && DataDescriptor.TIME_DIMENSION_NAME.equalsIgnoreCase(dimensionName);
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	List<DataDescriptor> descriptors = new ArrayList<>();
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());

	GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
	if (bbox != null) {
	    Double lat = bbox.getNorth();
	    Double lon = bbox.getEast();
	    descriptor.setEPSG4326SpatialDimensions(lat, lon);
	    descriptor.getFirstSpatialDimension().getContinueDimension().setSize(1L);
	    descriptor.getSecondSpatialDimension().getContinueDimension().setSize(1L);
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
		descriptor.setTemporalDimension(optionalBegin.get(), optionalEnd.get());
		Long oneDayInMilliseconds = 1000L * 60L * 60L * 24L;
		DataDimension temporalDimension = descriptor.getTemporalDimension();
		temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
		temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
	    }
	}

	descriptors.add(descriptor);
	return descriptors;
    }

    @Override
    public File download(DataDescriptor targetDescriptor) throws GSException {
	try {
	    String name = online.getName();
	    if (name == null) {
		throw new IllegalStateException("Missing online resource identifier");
	    }

	    DatalakesIdentifierMangler mangler = new DatalakesIdentifierMangler();
	    mangler.setMangling(name);
	    int datasetId = Integer.parseInt(mangler.getDatasetId());
	    String axis = mangler.getAxis();

	    Date begin = null;
	    Date end = null;
	    DataDimension dimension = targetDescriptor.getTemporalDimension();
	    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		ContinueDimension sizedDimension = dimension.getContinueDimension();
		begin = new Date(sizedDimension.getLower().longValue());
		end = new Date(sizedDimension.getUpper().longValue());
	    }
	    if (begin == null || end == null) {
		TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
		if (extent != null) {
		    begin = ISO8601DateTimeUtils.parseISO8601ToDate(extent.getBeginPosition()).orElse(null);
		    end = ISO8601DateTimeUtils.parseISO8601ToDate(extent.getEndPosition()).orElse(null);
		}
	    }
	    if (begin == null || end == null) {
		end = new Date();
		begin = new Date(end.getTime() - 7L * 24L * 60L * 60L * 1000L);
	    }

	    final Date rangeBegin = begin;
	    final Date rangeEnd = end;

	    List<JSONObject> files = client.listFiles(datasetId);
	    files.removeIf(file -> !"json".equalsIgnoreCase(file.optString("filetype"))
		    || !DatalakesClient.overlaps(rangeBegin, rangeEnd, file));
	    files.sort(Comparator.comparingLong(file -> DatalakesClient.parseIso8601Millis(file.optString("mindatetime"))));

	    TimeSeriesResponseType template = getJaxbTimeSeriesTemplate();
	    template.getTimeSeries().get(0).getVariable().setNoDataValue(NO_DATA_VALUE.doubleValue());
	    TimeSeriesTemplate timeSeriesTemplate = getTimeSeriesTemplate(template, getClass().getSimpleName(), ".wml");
	    DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

	    for (JSONObject file : files) {
		JSONObject payload = client.downloadJsonFile(file.getInt("id"));
		if (!payload.has("x") || !payload.has(axis)) {
		    continue;
		}
		JSONArray times = payload.getJSONArray("x");
		JSONArray values = payload.getJSONArray(axis);
		int length = Math.min(times.length(), values.length());
		for (int i = 0; i < length; i++) {
		    if (values.isNull(i)) {
			continue;
		    }
		    long epochSeconds = (long) times.getDouble(i);
		    Date timestamp = new Date(epochSeconds * 1000L);
		    if (timestamp.before(rangeBegin) || timestamp.after(rangeEnd)) {
			continue;
		    }

		    ValueSingleVariable value = new ValueSingleVariable();
		    value.setValue(BigDecimal.valueOf(values.getDouble(i)));
		    GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		    calendar.setTime(timestamp);
		    XMLGregorianCalendar xmlDate = xmlFactory.newXMLGregorianCalendar(calendar);
		    value.setDateTime(xmlDate);
		    addValue(timeSeriesTemplate, value);
		}
	    }

	    return timeSeriesTemplate.getDataFile();
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error retrieving Datalakes data", e);
	    throw GSException.createException(getClass(), DATALAKES_DOWNLOAD_ERROR + ": " + e.getMessage(), null,
		    ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, DATALAKES_DOWNLOAD_ERROR);
	}
    }
}
