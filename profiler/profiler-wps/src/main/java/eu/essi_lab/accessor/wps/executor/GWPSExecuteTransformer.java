package eu.essi_lab.accessor.wps.executor;

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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import eu.essi_lab.accessor.wps.GWPSProfiler;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.pdk.wrt.AccessRequestTransformer;

public class GWPSExecuteTransformer extends AccessRequestTransformer {

    private static final String GWPS_EXECUTE_ERROR = "GWPS_EXECUTE_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	message.setError(null);
	return message;
    }

    @Override
    public Provider getProvider() {
	return new ESSILabProvider();
    }

    @Override
    protected Optional<DataDescriptor> getTargetDescriptor(WebRequest request) throws GSException {

	Map<String, String[]> parameters = request.getServletRequest().getParameterMap();

	String dataInputs = null;

	for (String key : parameters.keySet()) {

	    String[] values = parameters.get(key);

	    if (key.toLowerCase().equals("datainputs") && values.length > 0) {
		dataInputs = values[0];
	    }
	}

	if (dataInputs != null) {
	    try {
		dataInputs = URLDecoder.decode(dataInputs, "UTF-8");
	    } catch (UnsupportedEncodingException e1) {
		e1.printStackTrace();
	    }
	    DataDescriptor ret = new DataDescriptor();
	    String[] splitDataInputs = dataInputs.split(";");
	    String temporalBegin = null;
	    String temporalEnd = null;
	    Long size1 = null;
	    Long size2 = null;
	    Number res1 = null;
	    Number res2 = null;
	    Double lower1 = null;
	    Double lower2 = null;
	    Double upper1 = null;
	    Double upper2 = null;
	    CRS crs = null;
	    for (String dataInput : splitDataInputs) {

		String[] children = dataInput.split("=");
		String[] splitSplit;
		if (children.length > 1) {
		    String key = children[0];
		    String value = children[1];
		    if (value == null) {
			continue;
		    } else {
			try {
			    value = URLDecoder.decode(value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
			    e.printStackTrace();
			}
		    }

		    switch (key.toLowerCase()) {
		    case "outputcrs":
			crs = CRS.fromIdentifier(value);
			ret.setCRS(crs);
			break;
		    case "outputformat":
			ret.setDataFormat(DataFormat.fromIdentifier(value));
			break;
		    case "outputtemporalbegin":
			temporalBegin = value;
			break;
		    case "outputtemporalend":
			temporalEnd = value;
			break;
		    case "outputsize":
		    case "outputsizes":
			if (value.contains(",")) {
			    splitSplit = value.split(",");
			} else {
			    splitSplit = new String[] { value, value };
			}
			size1 = parseNumber(splitSplit[0]).longValue();
			size2 = parseNumber(splitSplit[1]).longValue();
			break;
		    case "outputresolution":
		    case "outputresolutions":
			if (value.contains(",")) {
			    splitSplit = value.split(",");
			} else {
			    splitSplit = new String[] { value, value };
			}
			res1 = parseNumber(splitSplit[0]);
			res2 = parseNumber(splitSplit[1]);
			break;
		    case "lowercorner":
			splitSplit = value.split(",");
			lower1 = parseNumber(splitSplit[0]).doubleValue();
			lower2 = parseNumber(splitSplit[1]).doubleValue();
			break;
		    case "uppercorner":
			splitSplit = value.split(",");
			upper1 = parseNumber(splitSplit[0]).doubleValue();
			upper2 = parseNumber(splitSplit[1]).doubleValue();
			break;
		    case "outputsubset":
		    case "outputsubsets":
			splitSplit = value.split(",");
			if (splitSplit.length == 4) {
			    lower1 = parseNumber(splitSplit[0]).doubleValue();
			    lower2 = parseNumber(splitSplit[1]).doubleValue();
			    upper1 = parseNumber(splitSplit[2]).doubleValue();
			    upper2 = parseNumber(splitSplit[3]).doubleValue();
			} else {
			    GSLoggerFactory.getLogger(getClass()).error("Missing coordinates in outputSubset parameter");
			}
			break;
		    default:
			break;
		    }

		}
	    }

	    DataDimension dimension1 = null;
	    DataDimension dimension2 = null;

	    if (lower1 != null && lower2 != null && upper1 != null && upper2 != null) {
		dimension1 = createDimension1(crs);
		dimension2 = createDimension2(crs);
		if (lower1 > upper1) {
		    String message = "Upper corner is lower than lower corner";
		    throw getGSException(new IllegalArgumentException(message), message);
		}
		if (lower2 > upper2) {
		    String message = "Upper corner is lower than lower corner";
		    throw getGSException(new IllegalArgumentException(message), message);
		}
		dimension1.getContinueDimension().setLower(lower1);
		dimension1.getContinueDimension().setUpper(upper1);
		dimension2.getContinueDimension().setLower(lower2);
		dimension2.getContinueDimension().setUpper(upper2);
	    }

	    if (size1 != null && size2 != null) {
		dimension1 = dimension1 != null ? dimension1 : createDimension1(crs);
		dimension2 = dimension2 != null ? dimension2 : createDimension2(crs);
		dimension1.getContinueDimension().setSize(size1);
		dimension2.getContinueDimension().setSize(size2);
	    }

	    if (res1 != null && res2 != null) {
		dimension1 = dimension1 != null ? dimension1 : createDimension1(crs);
		dimension2 = dimension2 != null ? dimension2 : createDimension2(crs);
		dimension1.getContinueDimension().setResolution(res1);
		dimension2.getContinueDimension().setResolution(res2);
	    }

	    if (dimension1 != null && dimension2 != null) {
		List<DataDimension> spatialDimensions = new ArrayList<>();
		spatialDimensions.add(dimension1);
		spatialDimensions.add(dimension2);
		ret.setSpatialDimensions(spatialDimensions);
	    }

	    if (temporalBegin != null && temporalEnd != null) {
		Optional<Date> begin = ISO8601DateTimeUtils.parseISO8601ToDate(temporalBegin);
		Optional<Date> end = ISO8601DateTimeUtils.parseISO8601ToDate(temporalEnd);
		if (begin.isPresent() && end.isPresent()) {
		    ret.setTemporalDimension(begin.get(), end.get());
		}
	    }

	    return Optional.of(ret);
	}

	return Optional.empty();
    }

    private DataDimension createDimension1(CRS crs) {
	String name1 = crs == null ? "dim1" : crs.getFirstAxisName();
	ContinueDimension dimension1 = new ContinueDimension(name1);
	return dimension1;
    }

    private DataDimension createDimension2(CRS crs) {
	String name2 = crs == null ? "dim2" : crs.getSecondAxisName();
	ContinueDimension dimension2 = new ContinueDimension(name2);
	return dimension2;
    }

    private Number parseNumber(String string) {
	try {
	    return Long.parseLong(string);
	} catch (Exception e) {
	    try {
		return Double.parseDouble(string);
	    } catch (Exception e2) {
		return null;
	    }
	}

    }

    @Override
    protected String getOnlineId(WebRequest webRequest) throws GSException {

	try {

	    if (webRequest.isGetRequest()) {

		String servletRequest = webRequest.getUriInfo().getAbsolutePath().toString();

		String[] split = servletRequest.split("/");

		boolean dataset = false;
		String onlineResourceId = null;
		for (String s : split) {
		    if (dataset) {
			onlineResourceId = s;
			break;
		    }
		    if (s.equals("dataset")) {
			dataset = true;
		    } else {
			dataset = false;
		    }
		}

		if (onlineResourceId == null) {
		    throw getGSException(null, "Missing online resource id: " + webRequest.getQueryString());
		} else {
		    return onlineResourceId;
		}

	    } else {
		throw getGSException(null, "Not a valid request: " + webRequest.getQueryString());
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn("Can't read online resource id", e);

	}

	return null;
    }

    @Override
    public String getProfilerType() {

	return GWPSProfiler.GWPS_SERVICE_INFO.getServiceType();
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	return new Page(1, 1);
    }

    private GSException getGSException(Exception e, String message) {
	return GSException.createException(//
		getClass(), //
		message, //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		GWPS_EXECUTE_ERROR, e);
    }

}
