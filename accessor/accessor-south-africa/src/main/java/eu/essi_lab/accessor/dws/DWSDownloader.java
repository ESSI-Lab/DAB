package eu.essi_lab.accessor.dws;

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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;

import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.dws.client.DWSClient;
import eu.essi_lab.accessor.dws.client.DWSData;
import eu.essi_lab.accessor.dws.client.DWSFlowData;
import eu.essi_lab.accessor.dws.client.DWSPrimaryData;
import eu.essi_lab.accessor.dws.client.DWSStation.DWSVariable;
import eu.essi_lab.accessor.dws.client.DWSVolumeData;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
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

/**
 * @author roncella
 * @author boldrini
 */
public class DWSDownloader extends WMLDataDownloader {

    private static final String DWS_DOWNLOADER_DOWNLOAD_ERROR = "DWS_DOWNLOADER_DOWNLOAD_ERROR";
    private static final String DWS_DOWNLOADER_GET_REMOTE_DESCRIPTORS_ERROR = "DWS_DOWNLOADER_GET_REMOTE_DESCRIPTORS_ERROR";

    @Override
    public boolean canConnect() {
	return online.getLinkage().contains(CommonNameSpaceContext.DWS_URI);
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return true;
    }

    @Override
    public boolean canDownload() {
	return (online.getProtocol() != null && online.getProtocol().equals(CommonNameSpaceContext.DWS_URI));
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	try {
	    String name = online.getName();
	    DWSIdentifierMangler mangler = new DWSIdentifierMangler();
	    mangler.setMangling(name);
	    String stationCode = mangler.getPlatformIdentifier();
	    String seriesCode = mangler.getSeriesIdentifier();

	    TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
	    Date begin = null;
	    Date end = null;
	    if (extent != null) {

		String startDate = extent.getBeginPosition();
		String endDate = extent.getEndPosition();
		if (extent.isEndPositionIndeterminate()) {
		    endDate = ISO8601DateTimeUtils.getISO8601DateTime();
		}
		begin = ISO8601DateTimeUtils.parseISO8601ToDate(startDate).get();
		end = ISO8601DateTimeUtils.parseISO8601ToDate(endDate).get();

	    }

	    List<DataDescriptor> ret = new ArrayList<>();
	    DataDescriptor descriptor = new DataDescriptor();

	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_1_1());
	    descriptor.setCRS(CRS.EPSG_4326());

	    //
	    //
	    //

	    descriptor.setTemporalDimension(begin, end);

	    //
	    //
	    //

	    DataDimension temporalDimension = descriptor.getTemporalDimension();
	    long oneHourInMilliseconds = 1000 * 60 * 60l;
	    Long oneDayInMilliseconds = oneHourInMilliseconds * 24l;
	    temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
	    temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
	    // temporalDimension.getContinueDimension().setResolution(oneDayInMilliseconds);
	    // temporalDimension.getContinueDimension().setResolutionTolerance(oneDayInMilliseconds);

	    ret.add(descriptor);
	    return ret;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DWS_DOWNLOADER_GET_REMOTE_DESCRIPTORS_ERROR, //
		    e);
	}

    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	try {
	    String name = online.getName();
	    DWSIdentifierMangler mangler = new DWSIdentifierMangler();
	    mangler.setMangling(name);
	    String stationCode = mangler.getPlatformIdentifier();
	    String seriesCode = mangler.getSeriesIdentifier();
	    String dataType = mangler.getDataType();

	    DWSVariable var = DWSVariable.decode(seriesCode);
	    String type = var.getDataType();
	    DWSClient client = new DWSClient(online.getLinkage());
	    Date begin = null;
	    Date end = null;
	    DataDimension dimension = descriptor.getTemporalDimension();
	    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		ContinueDimension sizedDimension = dimension.getContinueDimension();
		begin = new Date(sizedDimension.getLower().longValue());
		end = new Date(sizedDimension.getUpper().longValue());
	    }

	    InputStream observationStream = client.getData(stationCode, type, begin, end);
	    List<DWSData> data = new ArrayList<DWSData>();
	    switch (var) {
	    case VOLUME:
		DWSVolumeData volumeData = new DWSVolumeData(observationStream);
		data = volumeData.getData();
		break;
	    case FLOW_RATE:
		DWSFlowData flowData = new DWSFlowData(observationStream);
		data = flowData.getData();
		break;
	    case WATER_LEVEL:
		DWSPrimaryData waterData = new DWSPrimaryData(observationStream);
		data = waterData.getLevelData();
		break;
	    case DISCHARGE:
		DWSPrimaryData dischrgeData = new DWSPrimaryData(observationStream);
		data = dischrgeData.getDischargeData();
	    default:
		break;
	    }
	    // DWSStation station = client.getStation(stationCode);
	    // List<Variable> variables = station.getVariables();
	    // Variable series = null;
	    // for (Variable variable : variables) {
	    // if (variable.getAbbreviation().equals(seriesCode)) {
	    // series = variable;
	    // }
	    // }

	    // DWSFlowData flowData = new DWSFlowData(observationStream);

	    ObjectFactory factory = new ObjectFactory();
	    TimeSeriesResponseType tsrt = getTimeSeriesTemplate();

	    for (DWSData value : data) {

		ValueSingleVariable v = new ValueSingleVariable();

		try {
		    v.setValue(value.getValue());
		    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		    c.setTime(value.getDate());
		    XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		    v.setDateTimeUTC(date2);
		    addValue(tsrt, v);
		} catch (NumberFormatException e) {
		    GSLoggerFactory.getLogger(getClass()).error("unparsable value: {}", value);
		    continue;
		}
	    }

	    JAXBElement<TimeSeriesResponseType> response = factory.createTimeSeriesResponse(tsrt);
	    File tmpFile = File.createTempFile(getClass().getSimpleName(), ".wml");
	    tmpFile.deleteOnExit();
	    JAXBWML.getInstance().marshal(response, tmpFile);
	    if (observationStream != null)
		observationStream.close();
	    return tmpFile;
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DWS_DOWNLOADER_DOWNLOAD_ERROR, //
		    e);
	}
	// return null;

    }

}
