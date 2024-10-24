package eu.essi_lab.profiler.csw;

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
import java.util.List;
import java.util.Set;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.jaxb.csw._2_0_2.ResultType;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter;

/**
 * @author Fabrizio
 */
public class CSWRequestFilter extends GETRequestFilter {

    private static final String CSW_REQUEST_FILTER_ERROR = "CSW_REQUEST_FILTER_ERROR";
    private List<ResultType> types;

    /**
     * 
     */
    public CSWRequestFilter() {
	types = new ArrayList<>();
    }

    /**
     * @param type
     */
    public CSWRequestFilter(ResultType type) {
	types = new ArrayList<>();
	types.add(type);
    }

    /**
     * @param queryString
     * @param strategy
     */
    public CSWRequestFilter(String queryString, InspectionStrategy strategy) {
	super(queryString, strategy);
	types = new ArrayList<>();
    }

    /**
     * @param type
     */
    public void addResultTypeCondition(ResultType type) {

	types.add(type);
    }

    @Override
    public boolean accept(WebRequest request) throws GSException {

	if (request.isGetRequest()) {

	    if (CSWRequestUtils.isGetRecordsFromGET(request)) {

		CSWRequestConverter converter = new CSWRequestConverter();

		try {
		    GetRecords getRecords = converter.convert(request);

		    if (types != null && !types.isEmpty()) {

			ResultType resultType = getRecords.getResultType();
			return types.contains(resultType);
		    }

		} catch (Exception e) {

		    throw GSException.createException(//
			    getClass(), //
			    e.getMessage(), //
			    e.getMessage(), //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    CSW_REQUEST_FILTER_ERROR, e);

		}

	    } else if (request.isGetRequest()) {

		return super.accept(request);
	    }
	}

	try {

	    if (CSWRequestUtils.isGetRecordByIdFromPOST(request)) {

		return true;
	    }

	    XMLDocumentReader reader = new XMLDocumentReader(request.getBodyStream().clone());
	    reader.setNamespaceContext(new CommonNameSpaceContext());

	    Boolean isGetRecords = reader.evaluateBoolean("exists(//csw:GetRecords)");

	    if (isGetRecords && types != null && !types.isEmpty()) {

		GetRecords getRecords = CommonContext.unmarshal(request.getBodyStream().clone(), GetRecords.class);
		ResultType resultType = getRecords.getResultType();
		return types.contains(resultType);
	    }

	    Set<String> keySet = queryConditions.keySet();
	    if (keySet.isEmpty()) {
		return false;
	    }

	    String operationName = keySet.toArray()[0].toString();
	    return reader.evaluateBoolean("exists(//csw:" + operationName + ")");

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    e.getMessage(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_REQUEST_FILTER_ERROR, e);

	}
    }

    /**
     * @param request
     * @return
     */
    @Override
    protected String getQueryString(WebRequest request) throws GSException {

	return request.getURLDecodedQueryString();
    }
}
