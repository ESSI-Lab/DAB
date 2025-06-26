package eu.essi_lab.accessor.odatahidro.client.download;

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
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ValueSingleVariable;
import org.json.JSONObject;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.odatahidro.ODataHidrologyMapper;
import eu.essi_lab.accessor.odatahidro.client.ClientResponseWrapper;
import eu.essi_lab.accessor.odatahidro.client.ODataHidrologyClient;
import eu.essi_lab.accessor.odatahidro.client.ODataHidrologyClient.Variable;
import eu.essi_lab.accessor.odatahidro.client.SYKEIdentifierMangler;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
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
import eu.essi_lab.model.resource.data.dimension.ContinueDimension.LimitType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public class ODataDataDownloader extends WMLDataDownloader {

    private static final String ODATA_DOWNLOAD_ERROR = "ODATA_DOWNLOAD_ERROR";

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
    }

    @Override
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocols.ODATA_SYKE.getCommonURN()));

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

	try {
	    return HttpConnectionUtils.checkConnectivity(online.getLinkage());
	} catch (URISyntaxException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return false;
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
	try {
	    if (name != null) {
		SYKEIdentifierMangler mangler = new SYKEIdentifierMangler();
		mangler.setMangling(name);
		String site = mangler.getPlatformIdentifier();
		String variable = mangler.getParameterIdentifier();
		if (site != null && variable != null) {

		    ODataHidrologyClient client = new ODataHidrologyClient();
		    Optional<Variable> v = Variable.fromId(variable);
		    if (v.isPresent()) {
			JSONObject

			varAscQueryResponse = client.execVariableQuery(v.get(), site, "asc", 0, 2, null, null);

			ClientResponseWrapper ascWrapper = new ClientResponseWrapper(varAscQueryResponse);
			Optional<String> begin = ascWrapper.getDate(0);

			JSONObject varDescQueryResponse = client.execVariableQuery(v.get(), site, "desc", 0, 1, null, null);
			ClientResponseWrapper descWrapper = new ClientResponseWrapper(varDescQueryResponse);
			Optional<String> end = descWrapper.getDate(0);

			JSONObject placeResponse = client.execPlaceQuery(0, 1, site);

			ClientResponseWrapper placeWrapper = new ClientResponseWrapper(placeResponse);

			int responseSize = placeWrapper.getResponseSize();

			GSLoggerFactory.getLogger(getClass()).debug("Response size {}", responseSize);

			Optional<String> optLat = placeWrapper.getLat(0);
			Optional<String> optLon = placeWrapper.getLon(0);

			if (optLat.isPresent() && optLon.isPresent() && begin.isPresent() && end.isPresent()) {

			    double koordErTmPohj = Double.valueOf(optLat.get());
			    double koordErTmIta = Double.valueOf(optLon.get());

			    SimpleEntry<Double, Double> latLon = ODataHidrologyMapper.getLatLon(koordErTmPohj, koordErTmIta);

			    Double lat = latLon.getKey();
			    Double lon = latLon.getValue();

			    descriptor.setEPSG4326SpatialDimensions(lat, lon);

			    Optional<Date> beginDate = ISO8601DateTimeUtils.parseISO8601ToDate(begin.get());
			    Optional<Date> endDate = ISO8601DateTimeUtils.parseISO8601ToDate(end.get());

			    if (beginDate.isPresent() && endDate.isPresent()) {

				descriptor.setTemporalDimension(beginDate.get(), endDate.get());

				ret.add(descriptor);
			    }
			}
		    }
		}

	    }
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return ret;

    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {

	String name = online.getName();
	// we expect a CUAHSI Hydro Server online resource name in the form as encoded by WOFIdentifierMangler
	if (name != null) {
	    SYKEIdentifierMangler mangler = new SYKEIdentifierMangler();
	    mangler.setMangling(name);
	    String site = mangler.getPlatformIdentifier();
	    String variable = mangler.getParameterIdentifier();

	    ODataHidrologyClient client = new ODataHidrologyClient();
	    Optional<Variable> v = Variable.fromId(variable);

	    if (site != null && variable != null && v.isPresent()) {

		try {

		    JSONObject placeResponse = client.execPlaceQuery(0, 1, site);

		    ClientResponseWrapper placeWrapper = new ClientResponseWrapper(placeResponse);

		    Optional<String> optLat = placeWrapper.getLat(0);
		    Optional<String> optLon = placeWrapper.getLon(0);
		    Double lat = null;
		    Double lon = null;
		    if (optLat.isPresent() && optLon.isPresent()) {

			double koordErTmPohj = Double.valueOf(optLat.get());
			double koordErTmIta = Double.valueOf(optLon.get());

			SimpleEntry<Double, Double> latLon = ODataHidrologyMapper.getLatLon(koordErTmPohj, koordErTmIta);

			lat = latLon.getKey();
			lon = latLon.getValue();
		    }

		    DataDimension dimension = descriptor.getTemporalDimension();
		    Date begin = null;
		    Date end = null;
		    boolean onlyLastValue = false;
		    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
			ContinueDimension sizedDimension = dimension.getContinueDimension();
			Number lower = sizedDimension.getLower();
			Number upper = sizedDimension.getUpper();
			if (lower != null && upper != null) {
			    begin = new Date(lower.longValue());
			    end = new Date(upper.longValue());
			}
			if (sizedDimension.getUpperType().equals(LimitType.MAXIMUM)
				&& sizedDimension.getLowerType().equals(LimitType.MAXIMUM)) {
			    onlyLastValue = true;
			}
		    }

		    JSONObject response;

		    if (onlyLastValue) {

			response = client.execVariableQuery(v.get(), site, "desc", 0, 1, null, null);

		    } else

		    if (begin != null) {

			response = client.execVariableQuery(v.get(), site, "desc", null, null, begin, end);

		    } else {

			response = client.execVariableQuery(v.get(), site, "desc", null, null, null, null);
		    }

		    ClientResponseWrapper wrapper = new ClientResponseWrapper(response);

		    int size = wrapper.getResponseSize();

		    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

		    for (int i = 0; i < size; i++) {
			Optional<String> value = wrapper.getValue(i);
			Optional<String> date = wrapper.getDate(i);

			if (value.isPresent() && date.isPresent()) {
			    ValueSingleVariable myValue = new ValueSingleVariable();
			    myValue.setValue(new BigDecimal(value.get()));
			    // myValue.setCensorCode("nc");
			    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			    c.setTime(ISO8601DateTimeUtils.parseISO8601ToDate(date.get()).get());
			    XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

			    myValue.setDateTimeUTC(date2);
			    addValue(tsrt, myValue);
			}
		    }

		    return tsrt.getDataFile();

		} catch (Exception e) {
		    throw GSException.createException(//

			    getClass(), //
			    "Error occurred, unable to download data", //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    ODATA_DOWNLOAD_ERROR);
		}

	    }
	}

	throw GSException.createException(//

		getClass(), //
		"Error occurred, unable to download data", //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		ODATA_DOWNLOAD_ERROR);

    }

}
