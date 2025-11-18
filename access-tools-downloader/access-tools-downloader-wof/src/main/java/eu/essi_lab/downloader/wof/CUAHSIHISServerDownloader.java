package eu.essi_lab.downloader.wof;

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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.accessor.wof.client.CUAHSIHISServerClient;
import eu.essi_lab.accessor.wof.client.CUAHSIHISServerClient1_1;
import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.accessor.wof.client.datamodel.SitesResponseDocument;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeriesResponseDocument;
import eu.essi_lab.accessor.wof.utils.WOFIdentifierMangler;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Datum;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension.LimitType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public class CUAHSIHISServerDownloader extends DataDownloader {

    private static final String CUAHSIHISS_READER_AS_STREAM_ERROR = "CUAHSIHISS_READER_AS_STREAM_ERROR";
    private static final String CUAHSIHISS_DOWNLOAD_ERROR = "CUAHSIHISS_DOWNLOAD_ERROR";

    private CUAHSIHISServerClient connector;

    @Override
    public boolean canConnect() {

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
	String linkage = online.getLinkage();
	this.connector = new CUAHSIHISServerClient1_1(linkage);
    }

    public CUAHSIHISServerClient getConnector() {
	return connector;
    }

    protected void setConnector(CUAHSIHISServerClient connector) {
	this.connector = connector;
    }

    @Override
    public boolean canDownload() {

	return (!online.getLinkage().contains("alerta.ina.gob.ar") && !online.getLinkage().contains("correo.ina.gob.ar")
		&& online.getProtocol() != null && online.getProtocol().equals(NetProtocolWrapper.CUAHSI_WATER_ONE_FLOW_1_1.getCommonURN()));

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
	// we expect a CUAHSI Hydro Server online resource name, as encoded by the WOFIdentifierMangler
	if (name != null) {
	    WOFIdentifierMangler mangler = new WOFIdentifierMangler();
	    mangler.setMangling(name);
	    String site = mangler.getPlatformIdentifier();
	    String variable = mangler.getParameterIdentifier();
	    String method = mangler.getMethodIdentifier();
	    String quality = mangler.getQualityIdentifier();
	    String source = mangler.getSourceIdentifier();
	    if (site.contains(":") && variable.contains(":")) {
		String siteNetwork = site.split(":")[0];
		String siteCode = site.split(":")[1];

		String variableCode = variable.split(":")[1];
		SitesResponseDocument siteInfo = getConnector().getSiteInfo(siteNetwork, siteCode);

		SiteInfo mySite = siteInfo.getSitesInfo().get(0);

		Double lat = Double.parseDouble(mySite.getLatitude());
		Double lon = Double.parseDouble(mySite.getLongitude());

		descriptor.setEPSG4326SpatialDimensions(lat, lon);
		descriptor.getFirstSpatialDimension().getContinueDimension().setSize(1l);
		descriptor.getSecondSpatialDimension().getContinueDimension().setSize(1l);

		// because some HIS services like hydroportal.cuahsi.org have truncated lat-lon values (in the order of
		// the .01 part)
		// in the actual data (while more precise data in the site info) TODO: think if it is acceptable
		descriptor.getFirstSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
		descriptor.getFirstSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
		descriptor.getSecondSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
		descriptor.getSecondSpatialDimension().getContinueDimension().setUpperTolerance(0.01);

		TimeSeries timeSeries = mySite.getSeries(variableCode, method, quality, source);

		if (timeSeries == null) {

		    GSLoggerFactory.getLogger(getClass()).warn("No time series found");
		    return ret;
		}

		Date begin = timeSeries.getBeginTimePositionDate();
		Date end = timeSeries.getEndTimePositionDate();

		descriptor.setTemporalDimension(begin, end);
		DataDimension temporalDimension = descriptor.getTemporalDimension();

		boolean regularTimeDimension = timeSeries.isTimeScaleRegular();
		Number resolution = timeSeries.getTimeScaleTimeSupport();
		Number resolutionTolerance = 0;
		if (regularTimeDimension && resolution != null && Math.abs(resolution.doubleValue()) > 0.00000000000001) {
		    String unitCode = timeSeries.getTimeScaleUnitCode();
		    long oneDay = 1000 * 60 * 60 * 24l;
		    switch (unitCode) {
		    case "100": // second
			resolution = getUpdatedResolution(resolution, 1000l);
			break;
		    case "101": // millisecond
			// nothing to do
			resolution = getUpdatedResolution(resolution, 1l);
			break;
		    case "102": // minute
			resolution = getUpdatedResolution(resolution, 1000 * 60l);
			break;
		    case "103": // hour
			resolution = getUpdatedResolution(resolution, 1000 * 60 * 60l);
			break;
		    case "104": // day
			resolution = getUpdatedResolution(resolution, oneDay);
			break;
		    case "105": // week
			resolution = getUpdatedResolution(resolution, oneDay * 7l);
			break;
		    case "106": // month
			resolution = getUpdatedResolution(resolution, oneDay * 30l);
			resolutionTolerance = oneDay * 5l;
			break;
		    case "107": // year
			resolution = getUpdatedResolution(resolution, oneDay * 365l);
			resolutionTolerance = oneDay * 2l;
			break;
		    case "108": // leap year
			resolution = getUpdatedResolution(resolution, oneDay * 366l);
			resolutionTolerance = oneDay * 2l;
			break;
		    case "109": // Gregorian year
			resolution = getUpdatedResolution(resolution, oneDay * 365.2425);
			break;
		    default:
			break;
		    }
		    temporalDimension.getContinueDimension().setResolution(resolution);
		    temporalDimension.getContinueDimension().setResolutionTolerance(resolutionTolerance);
		    /**
		     * N.B.the value count is not used, as it only reports the number of non missing values and not the
		     * total number of values considering the temporal extent and the resolution
		     */
		    // temporalDimension.getContinueDimension().setSize(valueCount);

		    // Long valueCount = timeSeries.getValueCount();
		    // // a check also on value count is needed to assure that the time series has a regular resolution,
		    // // there are cases (e.g. USU4 of little bear river that the resolution is specified as regular,
		    // but
		    // // the specified temporal domain isn't completely filled with values
		    // if (valueCount != null) {
		    // long expectedResolution = (end.getTime() - begin.getTime()) / (valueCount - 1);
		    // if (expectedResolution == resolution.longValue()) {
		    // temporalDimension.getContinueDimension().setResolution(resolution);
		    // temporalDimension.getContinueDimension().setSize(valueCount);
		    // }
		    // }

		}

		String elevation = mySite.getElevationMetres();
		if (elevation != null && !elevation.isEmpty()) {
		    double elevationDouble = Double.parseDouble(elevation);
		    descriptor.setVerticalDimension(elevationDouble, elevationDouble);
		    String datum = mySite.getVerticalDatum();
		    if (datum != null && !datum.isEmpty()) {
			ContinueDimension verticalDimension = descriptor.getOtherDimensions().get(0).getContinueDimension();
			verticalDimension.setDatum(new Datum(datum));
		    }
		}

		ret.add(descriptor);
	    }
	}
	return ret;

    }

    private Long getUpdatedResolution(Number resolution, Number i) {
	if (resolution instanceof Long && i instanceof Long) {
	    return (Long) resolution * (Long) i;
	}
	return Math.round(resolution.doubleValue() * i.doubleValue());
    }

    @Override
    public File download(DataDescriptor targetDescriptor) throws GSException {
	String name = online.getName();
	// we expect a CUAHSI Hydro Server online resource name in the form as encoded by WOFIdentifierMangler
	if (name != null) {
	    WOFIdentifierMangler mangler = new WOFIdentifierMangler();
	    mangler.setMangling(name);
	    String site = mangler.getPlatformIdentifier();
	    String variable = mangler.getParameterIdentifier();
	    String methodId = mangler.getMethodIdentifier();
	    String qualityControlLevelCode = mangler.getQualityIdentifier();
	    String sourceId = mangler.getSourceIdentifier();
	    if (site.contains(":") && variable.contains(":")) {
		String networkName = site.split(":")[0];
		String siteCode = site.split(":")[1];

		String variableCode = variable.split(":")[1];

		DataDimension dimension = targetDescriptor.getTemporalDimension();
		Date begin = null;
		Date end = null;
		if (dimension != null && dimension.getContinueDimension().getUom() != null
			&& dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		    ContinueDimension continueDimension = dimension.getContinueDimension();

		    DataDescriptor remoteDescriptor = null;

		    Number lower = continueDimension.getLower();
		    LimitType lowerType = continueDimension.getLowerType();

		    Number upper = continueDimension.getUpper();
		    LimitType upperType = continueDimension.getUpperType();

		    if (lowerType == null || !lowerType.equals(LimitType.ABSOLUTE) || //
			    upperType == null || !upperType.equals(LimitType.ABSOLUTE)) {
			// the remote descriptor is retrieved only in case an absolute value
			// has not been provided
			remoteDescriptor = getRemoteDescriptors().get(0);
		    }

		    lower = getActualLimit(lower, lowerType, remoteDescriptor);
		    begin = new Date(lower.longValue());

		    upper = getActualLimit(upper, upperType, remoteDescriptor);
		    end = new Date(upper.longValue());
		}
		TimeSeriesResponseDocument values;
		if (begin != null) {
		    values = getConnector().getValues(networkName, siteCode, variableCode, methodId, qualityControlLevelCode, sourceId,
			    begin, end);
		} else {
		    values = getConnector().getValues(networkName, siteCode, variableCode, methodId, qualityControlLevelCode, sourceId);
		}

		try {
		    return IOStreamUtils.tempFilefromStream(values.getReader().asStream(), "CUAHSI-downloader", ".xml");

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error("Error downloading {}", name, e);

		    throw GSException.createException(//
			    getClass(), //
			    e.getMessage(), //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    CUAHSIHISS_READER_AS_STREAM_ERROR, //
			    e);
		}
	    }
	}

	throw GSException.createException(//
		getClass(), //
		"Error occurred, unable to download data", //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		CUAHSIHISS_DOWNLOAD_ERROR);

    }

    private Number getActualLimit(Number limit, LimitType limitType, DataDescriptor remoteDescriptor) {
	switch (limitType) {
	case MINIMUM:
	    return remoteDescriptor.getTemporalDimension().getContinueDimension().getLower();
	case MAXIMUM:
	    return remoteDescriptor.getTemporalDimension().getContinueDimension().getUpper();
	case ABSOLUTE:
	default:
	    return limit;
	}
    }

}
