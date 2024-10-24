package eu.essi_lab.downloader.hiscentral;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;

import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.hiscentral.lombardia.Dato;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_FUNZIONE;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_OPERATORE;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_PERIODO;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaIdentifierMangler;
import eu.essi_lab.accessor.hiscentral.lombardia.RendiDatiResult;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public class HISCentralLombardiaDownloader extends WMLDataDownloader {

    private HISCentralLombardiaClient client;

    @Override
    public boolean canConnect() {
	try {
	    HISCentralLombardiaClient client = new HISCentralLombardiaClient(new URL(online.getLinkage()));
	    XMLDocumentReader comuni = client.elencoComuni();
	    return (comuni != null);
	} catch (Exception e) {
	}
	return false;
    }

    @Override
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocols.ARPA_LOMBARDIA.getCommonURN()));

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

	GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
	if (bbox != null) {
	    Double lat = bbox.getNorth();
	    Double lon = bbox.getEast();
	    if (lon != null && lat != null) {
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
	    }
	}
	TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
	if (extent != null) {

	    String startDate = extent.getBeginPosition();
	    String endDate = extent.getEndPosition();
	    if (extent.isEndPositionIndeterminate()) {
		endDate = ISO8601DateTimeUtils.getISO8601DateTime();
	    }
	    Optional<Date> optionalBegin = ISO8601DateTimeUtils.parseISO8601ToDate(startDate);
	    Optional<Date> optionalEnd = ISO8601DateTimeUtils.parseISO8601ToDate(endDate);
	    if (optionalBegin.isPresent() && optionalEnd.isPresent()) {
		Date begin = optionalBegin.get();
		Date end = optionalEnd.get();
		descriptor.setTemporalDimension(begin, end);
		DataDimension temporalDimension = descriptor.getTemporalDimension();
		Long oneDayInMilliseconds = 1000 * 60 * 60 * 24l;
		temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
		temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
	    }
	}
	VerticalExtent verticalExtent = resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getVerticalExtent();
	if (verticalExtent != null) {
	    double elevationDouble = verticalExtent.getMinimumValue();
	    descriptor.setVerticalDimension(elevationDouble, elevationDouble);
	}
	ret.add(descriptor);

	return ret;

    }

    public void initClient() {
	try {
	    this.client = new HISCentralLombardiaClient(new URL(online.getLinkage()));
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}
    }

    @Override
    public File download(DataDescriptor targetDescriptor) throws GSException {
	initClient();
	try {

	    String name = online.getName();

	    ObjectFactory factory = new ObjectFactory();
	    TimeSeriesResponseType tsrt = getTimeSeriesTemplate();
	    
	    
	    DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

	    if (name != null) {
		HISCentralLombardiaIdentifierMangler mangler = new HISCentralLombardiaIdentifierMangler();
		mangler.setMangling(name);
		String sensor = mangler.getSensorIdentifier();
		String function = mangler.getFunctionIdentifier();
		String operator = mangler.getOperatorIdentifier();
		String period = mangler.getPeriodIdentifier();

		DataDimension dimension = targetDescriptor.getTemporalDimension();
		Date begin = null;
		Date end = null;
		if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		    ContinueDimension sizedDimension = dimension.getContinueDimension();
		    begin = new Date(sizedDimension.getLower().longValue());
		    end = new Date(sizedDimension.getUpper().longValue());
		}

		ID_FUNZIONE functionId = ID_FUNZIONE.decode(function);
		ID_OPERATORE operatorId = ID_OPERATORE.decode(operator);
		ID_PERIODO periodId = ID_PERIODO.decode(period);
		RendiDatiResult data = client.rendiDati(sensor, functionId, operatorId, periodId, begin, end);

		List<Dato> datas = data.getDati();

		for (Dato dato : datas) {

		    Date d = dato.getDate();
		    BigDecimal v = dato.getValue();

		    ValueSingleVariable vsv = new ValueSingleVariable();
		    vsv.setValue(v);
		    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		    c.setTime(d);
	            XMLGregorianCalendar date2 = xmlFactory.newXMLGregorianCalendar(c);
		    vsv.setDateTimeUTC(date2);
		    addValue(tsrt, vsv);

		}
	    }
	    JAXBElement<TimeSeriesResponseType> response = factory.createTimeSeriesResponse(tsrt);
	    File tmpFile = File.createTempFile(getClass().getSimpleName(), ".wml");
	    tmpFile.deleteOnExit();
	    JAXBWML.getInstance().marshal(response, tmpFile);

	    return tmpFile;

	} catch (

	Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw GSException.createException(//

		    getClass(), //
		    "Error occurred, unable to download data", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "HISCENTRAL_LOMBARDIA_DOWNLOAD_ERROR");
	}

    }

}
