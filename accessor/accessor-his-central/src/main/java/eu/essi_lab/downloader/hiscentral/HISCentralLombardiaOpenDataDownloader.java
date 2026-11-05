package eu.essi_lab.downloader.hiscentral;

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
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.xml.datatype.DatatypeFactory;

import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.json.JSONObject;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.hiscentral.lombardia.opendata.HISCentralLombardiaOpenDataConnectorSetting;
import eu.essi_lab.accessor.hiscentral.lombardia.opendata.HISCentralLombardiaOpenDataMangler;
import eu.essi_lab.accessor.hiscentral.lombardia.opendata.HISCentralLombardiaOpenDataMapper;
import eu.essi_lab.accessor.hiscentral.lombardia.opendata.LombardiaOpenDataClient;
import eu.essi_lab.accessor.hiscentral.lombardia.opendata.LombardiaOpenDataProperty;
import eu.essi_lab.accessor.hiscentral.lombardia.opendata.LombardiaOpenDataProperty.Archive;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
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
import eu.essi_lab.wml._2.WML2QualityCategory;

/**
 * Downloads a Lombardia open data series as WaterML 1.1, merging the validated archives with the real time dataset
 * (archive values are preferred where both are available)
 *
 * @author boldrini
 */
public class HISCentralLombardiaOpenDataDownloader extends WMLDataDownloader {

    private static final String HISCENTRAL_LOMBARDIA_OPEN_DATA_DOWNLOAD_ERROR = "HISCENTRAL_LOMBARDIA_OPEN_DATA_DOWNLOAD_ERROR";

    /**
     * Requested interval when the target descriptor has none
     */
    private static final long DEFAULT_INTERVAL = 30L * 24 * 60 * 60 * 1000;

    private String appToken;

    @Override
    public boolean canConnect() {
	return online.getLinkage() != null;
    }

    @Override
    public boolean canDownload() {

	return online.getFunctionCode() != null && //
		online.getFunctionCode().equals("download") && //
		online.getLinkage() != null && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.HISCENTRAL_LOMBARDIA_OPEN_DATA_NS_URI);
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	try {
	    String sourceId = resource.getSource().getUniqueIdentifier();
	    Optional<HarvestingSetting> harvestingSetting = ConfigurationWrapper.getHarvestingSettings(sourceId);
	    if (harvestingSetting.isPresent()) {
		HISCentralLombardiaOpenDataConnectorSetting setting = SettingUtils.downCast(
			harvestingSetting.get().getSelectedAccessorSetting().getHarvestedConnectorSetting(),
			HISCentralLombardiaOpenDataConnectorSetting.class);
		appToken = setting.getAppToken();
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Unable to read the application token from the source settings: {}", e.getMessage());
	}
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return dimensionName != null && DataDescriptor.TIME_DIMENSION_NAME.equalsIgnoreCase(dimensionName);
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	List<DataDescriptor> ret = new ArrayList<>();
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());

	GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
	if (bbox != null) {
	    Double lat = bbox.getNorth();
	    Double lon = bbox.getEast();
	    if (lon != null && lat != null) {
		descriptor.setEPSG4326SpatialDimensions(lat, lon);
		descriptor.getFirstSpatialDimension().getContinueDimension().setSize(1l);
		descriptor.getSecondSpatialDimension().getContinueDimension().setSize(1l);
		descriptor.getFirstSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
		descriptor.getFirstSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
		descriptor.getSecondSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
		descriptor.getSecondSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
	    }
	}

	TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
	if (extent != null) {
	    String startDate = extent.getBeginPosition();
	    String endDate = extent.getEndPosition();
	    if (extent.isEndPositionIndeterminate()) {
		endDate = ISO8601DateTimeUtils.getISO8601DateTime();
	    }
	    Optional<Date> optionalBegin = ISO8601DateTimeUtils.parseISO8601ToDate(startDate);
	    Optional<Date> optionalEnd = ISO8601DateTimeUtils.parseISO8601ToDate(endDate);
	    if (optionalBegin.isPresent() && optionalEnd.isPresent()) {
		descriptor.setTemporalDimension(optionalBegin.get(), optionalEnd.get());
		DataDimension temporalDimension = descriptor.getTemporalDimension();
		long oneDayInMilliseconds = 1000 * 60 * 60 * 24l;
		temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
		temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
	    }
	}

	VerticalExtent verticalExtent = resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getVerticalExtent();
	if (verticalExtent != null) {
	    double elevation = verticalExtent.getMinimumValue();
	    descriptor.setVerticalDimension(elevation, elevation);
	}

	ret.add(descriptor);
	return ret;
    }

    @Override
    public File download(DataDescriptor targetDescriptor) throws GSException {

	try {
	    HISCentralLombardiaOpenDataMangler mangler = new HISCentralLombardiaOpenDataMangler();
	    mangler.setMangling(online.getName());
	    String sensorId = mangler.getSensorIdentifier();
	    LombardiaOpenDataProperty property = LombardiaOpenDataProperty.valueOf(mangler.getProperty());

	    Date begin = null;
	    Date end = null;
	    DataDimension dimension = targetDescriptor.getTemporalDimension();
	    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		ContinueDimension sizedDimension = dimension.getContinueDimension();
		begin = new Date(sizedDimension.getLower().longValue());
		end = new Date(sizedDimension.getUpper().longValue());
	    }
	    if (begin == null || end == null) {
		end = new Date();
		begin = new Date(end.getTime() - DEFAULT_INTERVAL);
	    }

	    LombardiaOpenDataClient client = new LombardiaOpenDataClient(online.getLinkage(), appToken);
	    Map<Instant, JSONObject> observations = getObservations(client, sensorId, property, begin.toInstant(), end.toInstant());

	    TimeSeriesResponseType template = getJaxbTimeSeriesTemplate();
	    template.getTimeSeries().get(0).getVariable().setNoDataValue(Double.valueOf(HISCentralLombardiaOpenDataMapper.MISSING_VALUE));
	    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(template, getClass().getSimpleName(), ".wml");

	    DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

	    for (Map.Entry<Instant, JSONObject> entry : observations.entrySet()) {

		JSONObject observation = entry.getValue();
		BigDecimal value = observation.getBigDecimal("valore");

		ValueSingleVariable vsv = new ValueSingleVariable();
		vsv.setValue(value);
		GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		calendar.setTime(Date.from(entry.getKey()));
		vsv.setDateTimeUTC(xmlFactory.newXMLGregorianCalendar(calendar));

		WML2QualityCategory quality = getQuality(observation.optString("stato"));
		if (quality != null) {
		    vsv.setQualityControlLevelCode(quality.getUri());
		}
		addValue(tsrt, vsv);
	    }

	    return tsrt.getDataFile();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to download Lombardia open data: " + e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HISCENTRAL_LOMBARDIA_OPEN_DATA_DOWNLOAD_ERROR);
	}
    }

    /**
     * @return the valid observations in the given interval, by UTC time
     */
    static Map<Instant, JSONObject> getObservations(LombardiaOpenDataClient client, String sensorId, LombardiaOpenDataProperty property,
	    Instant begin, Instant end) throws Exception {

	LocalDateTime localBegin = LocalDateTime.ofInstant(begin, LombardiaOpenDataClient.DATA_OFFSET);
	LocalDateTime localEnd = LocalDateTime.ofInstant(end, LombardiaOpenDataClient.DATA_OFFSET);

	TreeMap<Instant, JSONObject> ret = new TreeMap<>();

	// validated archives first, so that they are preferred to the real time data
	for (Archive archive : property.getArchives()) {
	    if (archive.overlaps(localBegin.getYear(), localEnd.getYear())) {
		addObservations(ret, property, client.getObservations(archive.getDatasetId(), sensorId, null, localBegin, localEnd));
	    }
	}
	addObservations(ret, property,
		client.getObservations(LombardiaOpenDataClient.REAL_TIME_DATASET, sensorId, property.getOperator(), localBegin, localEnd));

	return ret;
    }

    private static void addObservations(TreeMap<Instant, JSONObject> map, LombardiaOpenDataProperty property, List<JSONObject> observations) {

	for (JSONObject observation : observations) {
	    BigDecimal value = observation.optBigDecimal("valore", null);
	    if (value == null || !isValid(property, value)) {
		continue;
	    }
	    Instant time = LombardiaOpenDataClient.parse(observation.getString("data")).atOffset(LombardiaOpenDataClient.DATA_OFFSET)
		    .toInstant();
	    map.putIfAbsent(time, observation);
	}
    }

    /**
     * Discards missing values (-9999 in real time data, -999 in some archives) and, for wind direction, the codes of
     * variable wind (888, 8888) and calm (777, 7777)
     */
    static boolean isValid(LombardiaOpenDataProperty property, BigDecimal value) {

	if (value.compareTo(new BigDecimal("-999")) <= 0) {
	    return false;
	}
	if (property.isWindDirection()) {
	    int code = value.intValue();
	    return value.compareTo(new BigDecimal(code)) != 0 || (code != 777 && code != 888 && code != 7777 && code != 8888);
	}
	return true;
    }

    /**
     * @param stato quality flag of the real time data; archives use numeric codes, which are not mapped
     */
    static WML2QualityCategory getQuality(String stato) {

	if (stato == null) {
	    return null;
	}
	switch (stato.trim().toUpperCase()) {
	case "VA":
	case "VV":
	    return WML2QualityCategory.GOOD;
	case "NI":
	    return WML2QualityCategory.SUSPECT;
	case "NA":
	case "NV":
	case "NC":
	    return WML2QualityCategory.POOR;
	case "ND":
	    return WML2QualityCategory.MISSING;
	default:
	    return null;
	}
    }
}
