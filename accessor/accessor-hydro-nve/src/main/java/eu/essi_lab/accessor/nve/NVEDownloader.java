package eu.essi_lab.accessor.nve;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.access.wml.WML2DataDownloader;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.wml._2_0.CollectionType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType.Point;
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
import eu.essi_lab.wml._2.JAXBWML2;

public class NVEDownloader extends WML2DataDownloader {

    private static final String NVE_DOWNLOAD_ERROR = "NVE_DOWNLOAD_ERROR";
    private NVEClient client = null;
    private static String TOKEN = null;

    public static String getTOKEN() {
	return TOKEN;
    }

    public static void setTOKEN(String tOKEN) {
	TOKEN = tOKEN;
    }

    @Override
    public boolean canConnect() throws GSException {
	return !getClient().getStations().isEmpty();
    }

    private NVEClient getClient() {
	if (client == null) {
	    client = new NVEClient();
	}
	String token = getTOKEN();
	if (token == null) {
	    token = ConfigurationWrapper.getCredentialsSetting().getNVEToken().orElse(null);
	}
	client.setAuthorizationKey(token);
	return client;
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);

    }

    @Override
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals(CommonNameSpaceContext.NVE_URI));

    }

    @Override
    public boolean canSubset(String dimensionName) {

	return true;

    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	List<DataDescriptor> ret = new ArrayList<>();
	DataDescriptor descriptor = new DataDescriptor();
	try {

	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_2_0());
	    descriptor.setCRS(CRS.EPSG_4326());
	    String name = online.getName();
	    // we expect a BUFR online resource name, as encoded by the
	    // BUFRIdentifierMangler
	    NVEIdentifierMangler mangler = new NVEIdentifierMangler();
	    if (name != null) {
		mangler.setMangling(name);
		String parameterId = mangler.getParameterIdentifier();
		String resolutionId = mangler.getResolutionIdentifier();
		String stationId = mangler.getPlatformIdentifier();

		NVEStation station = getClient().getStations(stationId).get(stationId);

		List<NVESeries> series = station.getSeries();
		for (NVESeries serie : series) {
		    String parameter = serie.getParameterId();
		    if (parameter.equals(parameterId)) {
			List<NVEResolution> resolutions = serie.getResolutions();
			for (NVEResolution resolution : resolutions) {
			    String restime = resolution.getResTime();
			    if (restime.equals(resolutionId)) {

				Optional<Date> oBegin = ISO8601DateTimeUtils.parseISO8601ToDate(resolution.getDataFromTime());
				Optional<Date> oEnd = ISO8601DateTimeUtils.parseISO8601ToDate(resolution.getDataToTime());
				if (oBegin.isPresent() && oEnd.isPresent()) {
				    descriptor.setTemporalDimension(oBegin.get(), oEnd.get());
				    ret.add(descriptor);
				    return ret;
				}
			    }
			}
		    }
		}

	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error downloading {}", online.getName(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    NVE_DOWNLOAD_ERROR, //
		    e);

	}
	return ret;

    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	try {
	    String name = online.getName();
	    // we expect a BUFR online resource name, as encoded by the
	    // BUFRIdentifierMangler
	    NVEIdentifierMangler mangler = new NVEIdentifierMangler();
	    if (name != null) {
		mangler.setMangling(name);
		String parameterId = mangler.getParameterIdentifier();
		String resolutionId = mangler.getResolutionIdentifier();
		String stationId = mangler.getPlatformIdentifier();

		DataDimension dimension = descriptor.getTemporalDimension();
		Date begin = null;
		Date end = null;
		if (dimension != null && dimension instanceof ContinueDimension) {
		    Unit uom = ((ContinueDimension) dimension).getUom();

		    if (uom != null) {
			if (uom.equals(Unit.SECOND) || uom.equals(Unit.MILLI_SECOND)) {
			    long factor = 1;
			    if (uom.equals(Unit.SECOND)) {
				factor = 1000;
			    }
			    ContinueDimension continueDimension = (ContinueDimension) dimension;
			    begin = new Date(continueDimension.getLower().longValue() * factor);
			    end = new Date(continueDimension.getUpper().longValue() * factor);
			}

		    }
		}

		NVEObservations observations = getClient().getObservations(stationId, parameterId, resolutionId, begin, end);

		CollectionType collection = getCollectionTemplate();

		List<NVEObservation> obs = observations.getObservations();

		List<Point> points = new ArrayList<>();
		for (NVEObservation o : obs) {
		    try {
			Optional<Date> oTime = ISO8601DateTimeUtils.parseISO8601ToDate(o.getTime());
			if (oTime.isPresent()) {
			    Point point = new Point();

			    String ov = o.getValue();
			    if (ov == null) {
				point = createPoint(oTime.get(), null);
			    } else {
				try {
				    BigDecimal dataValue = new BigDecimal(ov);
				    point = createPoint(oTime.get(), dataValue.doubleValue());
				} catch (Exception e) {
				    point = createPoint(oTime.get(), null);
				}
			    }
			    points.add(point);
			}

		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}

		addPoints(collection, points);

		File tmp = File.createTempFile(getClass().getSimpleName(), ".wml");
		tmp.deleteOnExit();
		FileOutputStream fos = new FileOutputStream(tmp);

		JAXBWML2.getInstance().marshal(collection, fos);

		return tmp;

	    }

	    return null;

	} catch (GSException gse) {
	    throw gse;
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error downloading ");

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    NVE_DOWNLOAD_ERROR, //
		    e);
	}

    }

}
