package eu.essi_lab.accessor.inumet;

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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ValueSingleVariable;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public class INUMETDownloader extends WMLDataDownloader {

    private static final String INUMET_DOWNLOADER_GET_REMOTE_DESCRIPTORS_ERROR = "INUMET_DOWNLOADER_GET_REMOTE_DESCRIPTORS_ERROR";
    private static final String INUMET_DOWNLOADER_DOWNLOAD_ERROR = "INUMET_DOWNLOADER_DOWNLOAD_ERROR";

    @Override
    public boolean canConnect() {
	return online.getLinkage().contains(CommonNameSpaceContext.INUMET_URI);
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return false;
    }

    @Override
    public boolean canDownload() {
	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocolWrapper.INUMET.getCommonURN()));
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	try {
	    String name = online.getName();
	    INUMETIdentifierMangler mangler = new INUMETIdentifierMangler();
	    mangler.setMangling(name);
	    String stationCode = mangler.getPlatformIdentifier();
	    String parameterCode = mangler.getParameterIdentifier();

	    INUMETClient client = new INUMETClient(online.getLinkage());

	
	    List<DataDescriptor> ret = new ArrayList<>();
	    DataDescriptor descriptor = new DataDescriptor();

	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_1_1());
	    descriptor.setCRS(CRS.EPSG_4326());

	    List<SimpleEntry<Date, BigDecimal>> data = client.getLastData(parameterCode, stationCode);

	    Date begin = data.get(0).getKey();
	    Date end = data.get(data.size() - 1).getKey();

	    descriptor.setTemporalDimension(begin, end);
	    DataDimension temporalDimension = descriptor.getTemporalDimension();
	    long oneHourInMilliseconds = 1000 * 60 * 60l;
	    Long oneDayInMilliseconds = oneHourInMilliseconds * 24l;
	    temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
	    temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
	    temporalDimension.getContinueDimension().setResolution(oneDayInMilliseconds);
	    temporalDimension.getContinueDimension().setResolutionTolerance(oneDayInMilliseconds);

	    ret.add(descriptor);
	    return ret;
	} catch (Exception e) {
	    
	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    INUMET_DOWNLOADER_GET_REMOTE_DESCRIPTORS_ERROR, //
		    e//
	    );
	}
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	try {
	    String name = online.getName();
	    INUMETIdentifierMangler mangler = new INUMETIdentifierMangler();
	    mangler.setMangling(name);
	    String stationCode = mangler.getPlatformIdentifier();
	    String parameterCode = mangler.getParameterIdentifier();

	    INUMETClient client = new INUMETClient(online.getLinkage());

	
	    List<SimpleEntry<Date, BigDecimal>> datas = client.getLastData(parameterCode, stationCode);

	    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

	    for (SimpleEntry<Date, BigDecimal> data : datas) {
		ValueSingleVariable v = new ValueSingleVariable();

		Date parsed = data.getKey();
		BigDecimal dValue = data.getValue();

		v.setValue(dValue);
		GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		c.setTime(parsed);
		XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		v.setDateTimeUTC(date2);
		addValue(tsrt, v);
	    }

	    return tsrt.getDataFile();
	    
	} catch (Exception e) {
	    
	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    INUMET_DOWNLOADER_DOWNLOAD_ERROR, //
		    e//
	    );
	}

    }

}
