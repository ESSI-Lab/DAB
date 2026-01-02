package eu.essi_lab.accessor.imo;

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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.SiteInfoType;
import org.cuahsi.waterml._1.SiteInfoType.SiteCode;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.TimeSeriesType;
import org.cuahsi.waterml._1.TsValuesSingleVariableType;
import org.cuahsi.waterml._1.UnitsType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.VariableInfoType;
import org.cuahsi.waterml._1.VariableInfoType.VariableCode;
import org.cuahsi.waterml._1.essi.JAXBWML;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;

public class IMODownloader extends DataDownloader {

    private static final String IMO_DOWNLOAD_ERROR = "IMO_DOWNLOAD_ERROR";
    private String linkage;

    private IMOClient client = new IMOClient();

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	this.linkage = online.getLinkage();

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
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals(CommonNameSpaceContext.IMO_URI));

    }

    @Override
    public boolean canSubset(String dimensionName) {

	return false;

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
	    // we expect a BUFR online resource name, as encoded by the
	    // BUFRIdentifierMangler
	    IMOIdentifierMangler mangler = new IMOIdentifierMangler();
	    if (name != null) {
		mangler.setMangling(name);
		String parameterId = mangler.getParameterIdentifier();
		String stationId = mangler.getPlatformIdentifier();

		List<ZRXPDocument> docs = client.downloadAll();

		HashMap<String, List<ZRXPBlock>> map = ZRXPBlock.aggregateBlocks(docs);

		for (List<ZRXPBlock> blocks : map.values()) {

		    ZRXPBlock firstBlock = blocks.get(0);

		    if (firstBlock.getParameterName().equals(parameterId) && firstBlock.getStationIdentifier().equals(stationId)) {
			Date begin = null;
			Date end = null;
			for (ZRXPBlock block : blocks) {
			    SimpleEntry<Date, Double> first = block.getDataValues().get(0);
			    SimpleEntry<Date, Double> last = block.getDataValues().get(block.getDataValues().size() - 1);
			    if (begin == null || begin.after(first.getKey())) {
				begin = first.getKey();
			    }
			    if (end == null || end.before(last.getKey())) {
				end = last.getKey();
			    }
			}

			descriptor.setTemporalDimension(begin, end);
			ret.add(descriptor);
			return ret;

		    }

		}

		for (ZRXPDocument zrxp : docs) {
		    zrxp.getFile().delete();
		}

	    }

	} catch (

	Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error downloading {}", online.getName(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    IMO_DOWNLOAD_ERROR, //
		    e);

	}
	return ret;

    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	try {
	    String name = online.getName();

	    IMOIdentifierMangler mangler = new IMOIdentifierMangler();
	    if (name != null) {
		mangler.setMangling(name);
		String parameterName = mangler.getParameterIdentifier();
		String stationId = mangler.getPlatformIdentifier();

		List<ZRXPDocument> docs = client.downloadAll();

		HashMap<String, List<ZRXPBlock>> map = ZRXPBlock.aggregateBlocks(docs);

		for (List<ZRXPBlock> blocks : map.values()) {

		    ZRXPBlock firstBlock = blocks.get(0);

		    if (firstBlock.getParameterName().equals(parameterName) && firstBlock.getStationIdentifier().equals(stationId)) {
			List<SimpleEntry<Date, Double>> values = new ArrayList<>();
			for (ZRXPBlock block : blocks) {
			    values.addAll(block.getDataValues());
			}

			ObjectFactory factory = new ObjectFactory();
			TimeSeriesResponseType tsrt = new TimeSeriesResponseType();
			TimeSeriesType timeSeries = new TimeSeriesType();
			VariableInfoType variableType = new VariableInfoType();
			variableType.setVariableName(parameterName);
			VariableCode variableCode = new VariableCode();
			variableCode.setValue(parameterName);
			variableType.getVariableCode().add(variableCode);
			timeSeries.setVariable(variableType);

			TsValuesSingleVariableType value = new TsValuesSingleVariableType();

			values.sort(new Comparator<SimpleEntry<Date, Double>>() {

			    @Override
			    public int compare(SimpleEntry<Date, Double> o1, SimpleEntry<Date, Double> o2) {
				return o1.getKey().compareTo(o2.getKey());
			    }
			});

			for (SimpleEntry<Date, Double> vv : values) {
			    try {
				ValueSingleVariable v = new ValueSingleVariable();
				BigDecimal dataValue = new BigDecimal(vv.getValue());
				v.setValue(dataValue);
				GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
				c.setTime(vv.getKey());
				XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
				v.setDateTimeUTC(date2);
				value.getValue().add(v);

			    } catch (Exception e) {
				e.printStackTrace();
			    }
			}

			timeSeries.getValues().add(value);
			tsrt.getTimeSeries().add(timeSeries);

			SiteInfoType siteInfo = new SiteInfoType();
			SiteCode siteCode = new SiteCode();
			siteCode.setValue(stationId);
			siteInfo.getSiteCode().add(siteCode);
			siteInfo.setSiteName(firstBlock.getStationName());
			timeSeries.setSourceInfo(siteInfo);

			String unitsString = firstBlock.getUnit();
			UnitsType units = new UnitsType();
			units.setUnitName(unitsString);
			units.setUnitCode(unitsString);
			units.setUnitAbbreviation(unitsString);
			units.setUnitDescription(unitsString);

			variableType.setUnit(units);
			JAXBElement<TimeSeriesResponseType> response = factory.createTimeSeriesResponse(tsrt);
			File tmpFile = File.createTempFile(getClass().getSimpleName(), ".wml");
			tmpFile.deleteOnExit();
			JAXBWML.getInstance().marshal(response, tmpFile);

			return tmpFile;

		    }

		}

		for (ZRXPDocument zrxp : docs) {
		    zrxp.getFile().delete();
		}

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
		    IMO_DOWNLOAD_ERROR, //
		    e);
	}

    }

}
