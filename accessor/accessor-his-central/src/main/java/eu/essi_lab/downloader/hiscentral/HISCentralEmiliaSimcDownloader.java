package eu.essi_lab.downloader.hiscentral;

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
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ValueSingleVariable;
import org.json.JSONObject;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.hiscentral.emilia.simc.ArpaeSimcMeteoOpenDataClient;
import eu.essi_lab.accessor.hiscentral.emilia.simc.HISCentralEmiliaSimcConnector;
import eu.essi_lab.accessor.hiscentral.emilia.simc.HISCentralEmiliaSimcMangler;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
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
 * Downloads time series from the ARPAE-SIMC Eve REST API and returns WaterML 1.1.
 */
public class HISCentralEmiliaSimcDownloader extends WMLDataDownloader {

    private static final String HISCENTRAL_EMILIA_SIMC_DOWNLOAD_ERROR = "HISCENTRAL_EMILIA_SIMC_DOWNLOAD_ERROR";

    private static final int OBSERVATIONS_PAGE_SIZE = 500;

    private HISCentralEmiliaSimcConnector connector;

    public HISCentralEmiliaSimcDownloader() {
	connector = new HISCentralEmiliaSimcConnector();
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	List<DataDescriptor> ret = new ArrayList<>();

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());

	GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
	Double lat = bbox.getNorth();
	Double lon = bbox.getEast();

	descriptor.setEPSG4326SpatialDimensions(lat, lon);
	descriptor.getFirstSpatialDimension().getContinueDimension().setSize(1L);
	descriptor.getSecondSpatialDimension().getContinueDimension().setSize(1L);
	descriptor.getFirstSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
	descriptor.getFirstSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
	descriptor.getSecondSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
	descriptor.getSecondSpatialDimension().getContinueDimension().setUpperTolerance(0.01);

	TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
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
	    Long oneDayMs = 1000L * 60 * 60 * 24;
	    temporalDimension.getContinueDimension().setResolution(oneDayMs);
	    temporalDimension.getContinueDimension().setLowerTolerance(oneDayMs);
	    temporalDimension.getContinueDimension().setUpperTolerance(oneDayMs);
	}

	ret.add(descriptor);
	return ret;
    }

    @Override
    public File download(DataDescriptor targetDescriptor) throws GSException {
	Exception ex = null;
	try {
	    Date begin;
	    Date end;

	    DataDimension dimension = targetDescriptor.getTemporalDimension();
	    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		ContinueDimension sizedDimension = dimension.getContinueDimension();
		begin = new Date(sizedDimension.getLower().longValue());
		end = new Date(sizedDimension.getUpper().longValue());
	    } else {
		end = new Date();
		begin = new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);
	    }

	    HISCentralEmiliaSimcMangler mangler = new HISCentralEmiliaSimcMangler();
	    mangler.setMangling(online.getName());
	    String stationId = mangler.getPlatformIdentifier();
	    String datasetResource = mangler.getParameterIdentifier();
	    if (stationId == null || datasetResource == null) {
		throw new IllegalStateException("Missing station or dataset resource in online name: " + online.getName());
	    }

	    String baseUrl = connector.getSourceURL();
	    if (baseUrl == null || baseUrl.isEmpty()) {
		baseUrl = HISCentralEmiliaSimcConnector.BASE_URL;
	    }
	    ArpaeSimcMeteoOpenDataClient client = new ArpaeSimcMeteoOpenDataClient(baseUrl);

	    JSONObject where = ArpaeSimcMeteoOpenDataClient.mergeWhere(ArpaeSimcMeteoOpenDataClient.whereStationId(stationId),
		    reftimeRange(begin, end));

	    List<ArpaeSimcMeteoOpenDataClient.SimcObservation> observations = client.listAllObservations(datasetResource, where,
		    "reftime", OBSERVATIONS_PAGE_SIZE);

	    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");
	    DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

	    for (ArpaeSimcMeteoOpenDataClient.SimcObservation obs : observations) {
		if (obs.valueOrNull() == null || obs.reftime() == null) {
		    continue;
		}
		Optional<Instant> instant = ArpaeSimcMeteoOpenDataClient.parseReftime(obs.reftime());
		if (instant.isEmpty()) {
		    continue;
		}

		ValueSingleVariable variable = new ValueSingleVariable();
		variable.setValue(BigDecimal.valueOf(obs.valueOrNull()));

		GregorianCalendar gregCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		gregCal.setTime(Date.from(instant.get()));
		XMLGregorianCalendar xmlGregCal = xmlFactory.newXMLGregorianCalendar(gregCal);
		variable.setDateTimeUTC(xmlGregCal);

		addValue(tsrt, variable);
	    }

	    return tsrt.getDataFile();

	} catch (Exception e) {
	    ex = e;
	}

	throw GSException.createException(getClass(), ex != null ? ex.getMessage() : "Download failed", null,
		ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, HISCENTRAL_EMILIA_SIMC_DOWNLOAD_ERROR, ex);
    }

    private static JSONObject reftimeRange(Date begin, Date end) {
	JSONObject range = new JSONObject();
	range.put("$gte", ArpaeSimcMeteoOpenDataClient.formatReftime(begin.toInstant()));
	range.put("$lte", ArpaeSimcMeteoOpenDataClient.formatReftime(end.toInstant()));
	return new JSONObject().put("reftime", range);
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return dimensionName != null && DataDescriptor.TIME_DIMENSION_NAME.equalsIgnoreCase(dimensionName);
    }

    @Override
    public boolean canDownload() {
	return online.getFunctionCode() != null && online.getFunctionCode().equals("download") && online.getLinkage() != null
		&& online.getLinkage().contains("apps.arpae.it") && online.getProtocol() != null
		&& online.getProtocol().equals(CommonNameSpaceContext.HISCENTRAL_EMILIA_SIMC_NS_URI);
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
