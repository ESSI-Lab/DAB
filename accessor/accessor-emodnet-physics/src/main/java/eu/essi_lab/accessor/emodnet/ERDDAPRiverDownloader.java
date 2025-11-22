package eu.essi_lab.accessor.emodnet;

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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.access.wml.WML2DataDownloader;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.wml._2_0.CollectionType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType.Point;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.wml._2.JAXBWML2;

public class ERDDAPRiverDownloader extends WML2DataDownloader {

    @Override
    public boolean canConnect() throws GSException {
	ERDDAPRiverClient client = new ERDDAPRiverClient(online.getLinkage());
	return client.getDataURL() != null;
    }

    @Override
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals(CommonNameSpaceContext.EMODNET_PHYSICS_RIVER_NS_URI));

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
	    ERDDAPRiverIdentifierMangler mangler = new ERDDAPRiverIdentifierMangler();
	    if (name != null) {
		mangler.setMangling(name);
		String parameterId = mangler.getParameterIdentifier();
		String stationId = mangler.getPlatformIdentifier();

		ERDDAPRiverClient client = new ERDDAPRiverClient(online.getLinkage());

		List<ERDDAPRow> metadatas = client.getMetaData();
		for (ERDDAPRow metadata : metadatas) {
		    String station = metadata.getValue("PLATFORMCODE").toString();
		    if (station.equals(stationId)) {
			String firstDateObservation = metadata.getValue("firstDateObservation").toString();
			String lastDateObservation = metadata.getValue("lastDateObservation").toString();
			Optional<Date> optionalBegin = ISO8601DateTimeUtils.parseISO8601ToDate(firstDateObservation);
			Optional<Date> optionalEnd = ISO8601DateTimeUtils.parseISO8601ToDate(lastDateObservation);
			if (optionalBegin.isPresent() && optionalEnd.isPresent()) {
			    descriptor.setTemporalDimension(optionalBegin.get(), optionalEnd.get());
			    ret.add(descriptor);
			    return ret;
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
		    "ERROR", //
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
	    ERDDAPRiverIdentifierMangler mangler = new ERDDAPRiverIdentifierMangler();
	    if (name != null) {
		mangler.setMangling(name);
		String parameterId = mangler.getParameterIdentifier();
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

		ERDDAPRiverClient client = new ERDDAPRiverClient(online.getLinkage());

		List<ERDDAPRow> datas = client.getData(stationId, begin, end);

		CollectionType collection = getCollectionTemplate();

		List<Point> points = new ArrayList<>();
		for (ERDDAPRow data : datas) {
		    try {

			Optional<Date> oTime = ISO8601DateTimeUtils.parseISO8601ToDate(data.getValue("time").toString());
			if (oTime.isPresent()) {
			    Point point = new Point();

			    String ov = data.getValue("RVFL") == null ? null : data.getValue("RVFL").toString();

			    try {
				BigDecimal dataValue = new BigDecimal(ov);
				point = createPoint(oTime.get(), dataValue.doubleValue());
			    } catch (Exception e) {
				point = createPoint(oTime.get(), null);
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

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error downloading ");

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "ERROR", //
		    e);
	}

    }

}
