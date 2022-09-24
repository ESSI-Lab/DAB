package eu.essi_lab.pdk.rsf.impl.json.jsapi._2_0;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class JS_API_ResultSet_2_0 {

    private JSONObject resultSet;

    /**
     * @param exception
     */
    public JS_API_ResultSet_2_0(GSException exception) {

	init(1, 0, 0, 0, 0, exception, null);
    }

    /**
     * @param message
     * @param mappedResultSet
     */
    public JS_API_ResultSet_2_0(DiscoveryMessage message, MessageResponse<String, CountSet> mappedResultSet) {

	init(message.getPage().getStart(), //
		message.getPage().getSize(), //
		mappedResultSet.getCountResponse().getCount(), //
		mappedResultSet.getCountResponse().getPageCount(), //
		mappedResultSet.getCountResponse().getPageIndex(), //
		mappedResultSet.getException(), //
		message.getException());//
    }

    public JS_API_ResultSet_2_0(JSONObject resultSet) {

	this.resultSet = resultSet;
    }

    public JS_API_ResultSet_2_0(String resultSet) {

	this.resultSet = new JSONObject(resultSet);
    }

    public int getSize() {

	return resultSet.getInt("size");
    }

    public int getStart() {

	return resultSet.getInt("start");
    }

    public int getPageSize() {

	return resultSet.getInt("pageSize");
    }

    public int getPageCount() {

	return resultSet.getInt("pageCount");
    }

    public int getPageIndex() {

	return resultSet.getInt("pageIndex");
    }

    public JSONArray getErrors() {

	return resultSet.getJSONArray("errors");
    }

    public JSONArray getWarning() {

	return resultSet.getJSONArray("warnings");
    }

    public JSONObject asJSONObject() {

	return resultSet;
    }

    private void init(int start, int pageSize, int size, int pageCount, int pageIndex, GSException resultSetEx, GSException messageEx) {

	resultSet = new JSONObject();
	resultSet.put("start", start);
	resultSet.put("pageSize", pageSize);
	resultSet.put("size", size);
	resultSet.put("pageCount", pageCount);
	resultSet.put("pageIndex", pageIndex);

	JSONArray errors = new JSONArray();
	resultSet.put("errors", errors);

	JSONArray warnings = new JSONArray();
	resultSet.put("warnings", errors);

	List<ErrorInfo> list = resultSetEx.getErrorInfoList();

	if (messageEx != null) {
	    list.addAll(messageEx.getErrorInfoList());
	}

	for (ErrorInfo errorInfo : list) {

	    JSONObject jsonErrorInfo = new JSONObject();

	    String errorId = errorInfo.getErrorId();
	    if (errorId != null && !errorId.equals("")) {
		jsonErrorInfo.put("errorId", errorId);
	    }

	    String errorCorrection = errorInfo.getErrorCorrection();
	    if (errorCorrection != null && !errorCorrection.equals("")) {
		jsonErrorInfo.put("errorCorrection", errorCorrection);
	    }

	    String errorDescription = errorInfo.getErrorDescription();
	    if (errorDescription != null && !errorDescription.equals("")) {
		jsonErrorInfo.put("errorDescription", errorDescription);
	    }

	    String userErrorDescription = errorInfo.getUserErrorDescription();
	    if (userErrorDescription != null && !userErrorDescription.equals("")) {
		jsonErrorInfo.put("userErrorDescription", userErrorDescription);
	    }

	    String errorType = errorInfo.getErrorType();
	    jsonErrorInfo.put("errorType", errorType);

	    switch (errorInfo.getSeverity()) {
	    case ErrorInfo.SEVERITY_FATAL:
	    case ErrorInfo.SEVERITY_ERROR:
		errors.put(jsonErrorInfo);
		break;
	    case ErrorInfo.SEVERITY_WARNING:
		warnings.put(jsonErrorInfo);
		break;
	    }
	}
    }
}
