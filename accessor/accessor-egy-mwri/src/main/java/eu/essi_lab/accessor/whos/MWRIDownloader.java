package eu.essi_lab.accessor.whos;

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
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
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
 * @author boldrini
 */
public class MWRIDownloader extends WMLDataDownloader {

    public static final String MWRI_NS = "mwri.gov.eg";

    @Override
    public boolean canConnect() {
	return online.getLinkage().contains("mwri.gov.eg");
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return false;
    }

    @Override
    public boolean canDownload() {
	return (online.getProtocol() != null && online.getProtocol().equals("mwri.gov.eg"));
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	try {
	    String name = online.getName();
	    MWRIIdentifierMangler mangler = new MWRIIdentifierMangler();
	    mangler.setMangling(name);
	    String stationCode = mangler.getPlatformIdentifier();
	    MWRIClient client = new MWRIClient(online.getLinkage());

	    MWRIStation station = client.getStation(stationCode);

	    List<DataDescriptor> ret = new ArrayList<>();
	    DataDescriptor descriptor = new DataDescriptor();

	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_1_1());
	    descriptor.setCRS(CRS.EPSG_4326());

	    Date begin = station.getDate();
	    Date end = station.getDate();

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
		    "MWRI_DOWNLOADER_GET_REMOTE_DESCRIPTOR_ERROR", //
		    e);
	}
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	try {
	    String name = online.getName();
	    MWRIIdentifierMangler mangler = new MWRIIdentifierMangler();
	    mangler.setMangling(name);
	    String stationCode = mangler.getPlatformIdentifier();
	    String parameterCode = mangler.getParameterIdentifier();
	    MWRIClient client = new MWRIClient(online.getLinkage());
	    MWRIStation station = client.getStation(stationCode);

	    MWRIParameter p = MWRIParameter.decode(parameterCode);

	    BigDecimal value = null;
	    switch (p) {
	    case WATER_LEVEL_DOWNSTREAM:
		value = station.getWaterLevelDownstream();
		break;
	    case WATER_LEVEL_UPSTREAM:
		value = station.getWaterLevelUpstream();
		break;
	    default:
		break;
	    }

	    ObjectFactory factory = new ObjectFactory();
	    TimeSeriesResponseType tsrt = getTimeSeriesTemplate();

	    // for (MWRIObservation observation : observations) {

	    ValueSingleVariable v = new ValueSingleVariable();

	    Date parsed = station.getDate();
	    BigDecimal dValue = value;

	    v.setValue(dValue);
	    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	    c.setTime(parsed);
	    XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
	    v.setDateTimeUTC(date2);
	    addValue(tsrt, v);
	    // }

	    JAXBElement<TimeSeriesResponseType> response = factory.createTimeSeriesResponse(tsrt);
	    File tmpFile = File.createTempFile(getClass().getSimpleName(), ".wml");
	    tmpFile.deleteOnExit();
	    JAXBWML.getInstance().marshal(response, tmpFile);

	    return tmpFile;
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "MWRI_DOWNLOADER_DOWNLOAD_ERROR", //
		    e);
	}

    }

}
