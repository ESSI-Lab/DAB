package eu.essi_lab.accessor.ana.download;

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
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import org.cuahsi.waterml._1.TsValuesSingleVariableType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;
import org.w3c.dom.Node;

import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.ana.ANAConnector;
import eu.essi_lab.accessor.ana.ANAVariable;
import eu.essi_lab.accessor.ana.StationListDocument;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLNodeReader;
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

public class ANADownloader extends WMLDataDownloader {

    private ANAConnector connector = new ANAConnector();
    private String linkage;

    StationListDocument stations;

    private static final String ANA_DOWNLOADER_ERROR = "ANA_DOWNLOADER_ERROR";

    @Override
    public boolean canConnect() {

	String wsdl = connector.getSourceURL() + "?WSDL";
	try {
	    return HttpConnectionUtils.checkConnectivity(wsdl);
	} catch (URISyntaxException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return false;
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	this.linkage = online.getLinkage();
	this.connector.setSourceURL(resource.getSource().getEndpoint());
    }

    @Override
    public boolean canDownload() {
	return (online.getProtocol() != null && online.getProtocol().equals(CommonNameSpaceContext.ANA_URI));

    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	List<DataDescriptor> ret = new ArrayList<>();
	DataDescriptor descriptor = new DataDescriptor();
	try {

	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_1_1());
	    descriptor.setCRS(CRS.EPSG_4326());
	    String name = online.getName();

	    if (name != null && name.contains("@")) {
		String variableId = name.split("@")[0];
		String stationCode = name.split("@")[1];
		String stationName = name.split("@")[2];

		ANAVariable variable = ANAVariable.decode(variableId);

		TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();

		if (extent != null) {

		    String startDate = extent.getBeginPosition();
		    String endDate = extent.getEndPosition();
		    if (extent.isEndPositionIndeterminate()) {
			endDate = ISO8601DateTimeUtils.getISO8601DateTime();
		    }
		    Date begin = ISO8601DateTimeUtils.parseISO8601ToDate(startDate).get();
		    Date end = ISO8601DateTimeUtils.parseISO8601ToDate(endDate).get();

		    if (startDate != null && endDate != null) {

			descriptor.setTemporalDimension(begin, end);
			DataDimension temporalDimension = descriptor.getTemporalDimension();
			Long oneDayInMilliseconds = 1000 * 60 * 60 * 24l;
			temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
			temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
		    }
		}

		ret.add(descriptor);
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error downloading {}", online.getName(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ANA_DOWNLOADER_ERROR, //
		    e);

	}

	return ret;
    }

    private static DateFormat iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static {
	iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("GMT-3"));
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {

	try {

	    String name = online.getName();

	    if (name != null && name.contains("@")) {

		String variableName = name.split("@")[0];
		String stationCode = name.split("@")[1];
		String stationName = name.split("@")[2];

		ANAVariable anaVar = ANAVariable.decode(variableName);

		ObjectFactory factory = new ObjectFactory();
		TimeSeriesResponseType tsrt = getTimeSeriesTemplate();

		DataDimension dimension = descriptor.getTemporalDimension();
		Date begin = null;
		Date end = null;
		String startString = null;
		String endString = null;
		if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		    ContinueDimension sizedDimension = dimension.getContinueDimension();
		    begin = new Date(sizedDimension.getLower().longValue());
		    end = new Date(sizedDimension.getUpper().longValue());
		    startString = ISO8601DateTimeUtils.getISO8601Date(begin);
		    endString = ISO8601DateTimeUtils.getISO8601Date(end);
		}

		if (startString == null || endString == null) {
		    startString = ISO8601DateTimeUtils.getISO8601Date(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L));
		    endString = ISO8601DateTimeUtils.getISO8601Date(new Date());
		}

		InputStream dataStream = connector.getData(linkage, stationCode, startString, endString);

		XMLDocumentReader xdoc = new XMLDocumentReader(dataStream);

		TsValuesSingleVariableType value = new TsValuesSingleVariableType();

		List<Node> nodeList = xdoc.evaluateOriginalNodesList("//*:DadosHidrometereologicos");

		for (Node n : nodeList) {

		    ValueSingleVariable v = new ValueSingleVariable();

		    XMLNodeReader reader = new XMLNodeReader(n);
		    String variableValue = reader.evaluateString("*:" + variableName);
		    if (variableValue != null && !variableValue.isEmpty()) {

			try {

			    BigDecimal dataValue = new BigDecimal(variableValue);
			    v.setValue(dataValue);

			    String date = reader.evaluateString("*:DataHora");

			    Date parsed = iso8601OutputFormat.parse(date);

			    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			    c.setTime(parsed);

			    XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			    v.setDateTimeUTC(date2);

			    addValue(tsrt, v);

			} catch (Exception ex) {
			    // to avoid java.lang.NumberFormatException: For input string: "" when
			    // parsing the date
			    GSLoggerFactory.getLogger(getClass()).error(ex);
			}
		    }
		}

		if (dataStream != null) {
		    dataStream.close();
		}

		JAXBElement<TimeSeriesResponseType> response = factory.createTimeSeriesResponse(tsrt);
		File tmpFile = File.createTempFile(getClass().getSimpleName(), ".wml");
		tmpFile.deleteOnExit();
		JAXBWML.getInstance().marshal(response, tmpFile);

		return tmpFile;
	    }

	    throw GSException.createException(//
		    getClass(), //
		    "Invalid online found", null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ANA_DOWNLOADER_ERROR //
	    );

	} catch (Exception ex) {

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ANA_DOWNLOADER_ERROR, //
		    ex);
	}
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return true;
    }

}
