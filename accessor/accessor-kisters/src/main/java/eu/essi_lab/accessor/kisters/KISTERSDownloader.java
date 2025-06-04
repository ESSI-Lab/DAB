package eu.essi_lab.accessor.kisters;

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
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * @author Fabrizio
 */
public class KISTERSDownloader extends WMLDataDownloader {

    /**
     * 
     */
    private String linkage;

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	List<DataDescriptor> ret = new ArrayList<>();

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());

	String name = online.getName();

	KISTERSClient client = new KISTERSClient(online.getLinkage());

	KISTERSEntity entity = client.retrieveTimeSeries(name);

	String from = entity.getObject().getString(KISTERSClient.TS_FROM);
	String to = entity.getObject().getString(KISTERSClient.TS_TO);

	descriptor.setTemporalDimension(//
		ISO8601DateTimeUtils.parseISO8601ToDate(from).get(), //
		ISO8601DateTimeUtils.parseISO8601ToDate(to).get());

	ret.add(descriptor);

	return ret;
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {

	String name = online.getName();

	String begin = null;
	String end = null;

	DataDimension temporalDimension = descriptor.getTemporalDimension();

	try {
	    ContinueDimension dimension = temporalDimension.getContinueDimension();

	    Number lower = dimension.getLower();
	    Number upper = dimension.getUpper();

	    Date beginDate = new Date(lower.longValue());
	    Date endDate = new Date(upper.longValue());

	    begin = ISO8601DateTimeUtils.getISO8601DateTime(beginDate);
	    end = ISO8601DateTimeUtils.getISO8601DateTime(endDate);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	KISTERSClient client = new KISTERSClient(online.getLinkage());
	List<KISTERSEntity> values = client.retrieveTimeSeriesValues(name, begin, end);

	TimeSeriesResponseType tsrt = getTimeSeriesTemplate();
	ObjectFactory factory = new ObjectFactory();

	if (!values.isEmpty()) {

	    values.forEach(entity -> {

		String phenomenonTime = entity.getObject().getString("Timestamp");
		double result = entity.getObject().getDouble("Value");

		ValueSingleVariable v = new ValueSingleVariable();

		try {
		    v.setValue(new BigDecimal(result));

		    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		    c.setTime(ISO8601DateTimeUtils.parseISO8601ToDate(phenomenonTime).get());

		    XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

		    v.setDateTimeUTC(date2);
		    addValue(tsrt, v);

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e);
		}
	    });

	} else {
	    GSLoggerFactory.getLogger(getClass()).warn("No results found between [{}/{}]", begin, end);
	}

	try {

	    JAXBElement<TimeSeriesResponseType> response = factory.createTimeSeriesResponse(tsrt);

	    File tmpFile = File.createTempFile(getClass().getSimpleName(), ".wml");
	    tmpFile.deleteOnExit();

	    JAXBWML.getInstance().marshal(response, tmpFile);
	    return tmpFile;

	} catch (Exception ex) {

	    throw GSException.createException(getClass(), getClass().getSimpleName() + "_FileCreationError", ex);
	}
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	this.linkage = online.getLinkage();
    }

    @Override
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocols.KISTERS.getCommonURN()));
    }

    @Override
    public boolean canSubset(String dimensionName) {

	if (dimensionName == null) {
	    return false;
	}

	return DataDescriptor.TIME_DIMENSION_NAME.equalsIgnoreCase(dimensionName);
    }

    @Override
    public boolean canConnect() throws GSException {
	GSSource source = new GSSource();
	source.setEndpoint(linkage);
	return new KISTERSConnector().supports(source);
    }
}
