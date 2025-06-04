package eu.essi_lab.lib.net.googlesheet;

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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.api.services.sheets.v4.model.AppendCellsRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.ValueRange;

/**
 * Client for a Google Sheet composed by a table, whose first line is the header
 * 
 * @author boldrini
 */
public abstract class GoogleTableSheetClient extends GoogleSheetClient {

    private List<String> headers;
    private List<List<Object>> values;
    private HashMap<String, List<Object>> rowsByKey;
    private String keyHeader;

    /**
     * 
     */
    public GoogleTableSheetClient() throws Exception {

	super();
	rowsByKey = new HashMap<>();
	headers = new ArrayList<>();
    }

    /**
     * @param spreadsheetId
     * @param range
     * @throws Exception
     */
    public GoogleTableSheetClient(String spreadsheetId, String range) throws Exception {

	this(spreadsheetId, range, null);
    }

    /**
     * @param stylesheetURL
     * @param range
     * @throws Exception
     */
    public GoogleTableSheetClient(URL stylesheetURL, String range) throws Exception {

	this(extractStylesheetID(stylesheetURL), range, null);
    }

    /**
     * @param spreadsheetId google spreadsheet id
     * @param range A1 formatted range
     * @param keyHeader an header which serves as the key for rows, to index the rows according to the specified
     *        header
     * @throws Exception
     */
    public GoogleTableSheetClient(String spreadsheetId, String range, String keyHeader) throws Exception {

	this();
	this.keyHeader = keyHeader;

	setSpreadsheetId(spreadsheetId);
	setRange(range);
	parseTable();
    }

    /**
     * @param stylesheetURL
     * @param range A1 formatted range
     * @param keyHeader an header which serves as the key for rows, to index the rows according to the specified
     *        header
     * @throws Exception
     */
    public GoogleTableSheetClient(URL stylesheetURL, String range, String keyHeader) throws Exception {

	this(extractStylesheetID(stylesheetURL), range, keyHeader);
    }

    /**
     * example given, from
     * https://docs.google.com/spreadsheets/d/1ni9_BNcgoWD5HcU0sT_E20CwcOpOjsdek7_fQd4DWtI/edit#gid=253032876
     * to 1ni9_BNcgoWD5HcU0sT_E20CwcOpOjsdek7_fQd4DWtI
     * 
     * @param stylesheetURL
     * @return
     */
    protected static String extractStylesheetID(URL url) {

	String stylesheetURL = url.toString();

	if (stylesheetURL == null) {
	    return null;
	}

	if (!stylesheetURL.contains("/")) {
	    return stylesheetURL;
	}

	stylesheetURL = stylesheetURL.replace("https://docs.google.com/spreadsheets/d/", "");
	stylesheetURL = stylesheetURL.substring(0, stylesheetURL.indexOf("/"));
	return stylesheetURL;
    }

    /**
     * @return
     */
    public String getKeyHeader() {

	return keyHeader;
    }

    /**
     * @param keyHeader
     */
    public void setKeyHeader(String keyHeader) {

	this.keyHeader = keyHeader;
    }

    /**
     * @param row
     * @return
     */
    public List<String> getRow(Integer row) {

	List<String> ret = new ArrayList<>();
	List<Object> r = this.values.get(row);

	for (Object o : r) {
	    if (o == null) {
		ret.add(null);
	    }
	    ret.add(o.toString());
	}

	return ret;
    }

    /**
     * @throws IOException
     */
    public void parseTable() throws IOException {
	ValueRange data = getData();
	this.values = data.getValues();
	for (Object header : values.get(0)) {
	    headers.add(header.toString());
	}
	if (keyHeader != null) {
	    Integer i = getHeaderColumn(keyHeader);
	    // start from 1, to avoid indexing the header row
	    for (int j = 1; j < values.size(); j++) {
		List<Object> vv = values.get(j);
		Object valueKey = vv.get(i);
		if (valueKey != null) {
		    String k = valueKey.toString();
		    rowsByKey.put(k, vv);
		}
	    }
	}
    }

    /**
     * @param keyValue
     * @param header
     * @return
     */
    public String getValueByKey(String keyValue, String header) {

	List<Object> row = rowsByKey.get(keyValue);

	if (row != null) {
	    Integer i = getHeaderColumn(header);
	    if (i > (row.size() - 1)) {
		return null;
	    }
	    Object v = row.get(i);
	    if (v != null) {
		return v.toString();
	    }
	}

	return null;
    }

    /**
     * @param key
     * @return
     */
    public List<String> getRowByKey(String key) {

	List<Object> row = rowsByKey.get(key);
	List<String> ret = new ArrayList<>();

	if (row != null) {
	    for (Object object : row) {
		if (object == null) {
		    object = "";
		}
		ret.add(object.toString());
	    }
	}

	return ret;
    }

    /**
     * @return
     */
    public Set<String> getKeys() {

	if (keyHeader != null) {
	    return rowsByKey.keySet();
	}

	return null;
    }

    /**
     * @return
     */
    public List<String> getHeaders() {

	return headers;
    }

    /**
     * Returns a value from the table. Note, row 0 is headers
     * 
     * @param row
     * @param column
     * @return
     */
    public String getValue(int row, int column) {

	if (row >= values.size()) {
	    return null;
	}

	List<Object> r = values.get(row);
	if (r == null) {
	    return null;
	}

	if (column >= r.size()) {
	    return null;
	}

	Object c = r.get(column);
	if (c == null) {
	    return null;
	}

	return c.toString();
    }

    /**
     * Returns the value from specified row and column with given header
     * 
     * @param row
     * @param header
     * @return
     */
    public String getValue(Integer row, String myHeader) {

	Integer column = getHeaderColumn(myHeader);
	return getValue(row, column);
    }

    /**
     * Returns the column for this header
     * 
     * @param myHeader
     * @return
     */
    public Integer getHeaderColumn(String myHeader) {

	for (int i = 0; i < headers.size(); i++) {
	    String header = headers.get(i);
	    if (header.equals(myHeader)) {
		return i;
	    }
	}

	return null;
    }

    /**
     * @return
     */
    public Integer getColumnSize() {

	return headers.size();
    }

    /**
     * @return
     */
    public Integer getRowSize() {

	return values.size();
    }

    /**
     * @param cells
     * @param sheetId
     * @return
     * @throws IOException
     */
    public BatchUpdateSpreadsheetResponse appendRow(List<CellData> cells, int sheetId) throws IOException {

	List<RowData> rowData = new ArrayList<RowData>();
	rowData.add(new RowData().setValues(cells));

	AppendCellsRequest appendCellReq = new AppendCellsRequest();
	appendCellReq.setSheetId(sheetId);
	appendCellReq.setRows(rowData);
	appendCellReq.setFields("*");

	List<Request> requests = new ArrayList<Request>();
	requests.add(new Request().setAppendCells(appendCellReq));

	BatchUpdateSpreadsheetRequest batchRequests = new BatchUpdateSpreadsheetRequest();
	batchRequests.setRequests(requests);

	return getService().//
		spreadsheets().//
		batchUpdate(getSpreadsheetId(), batchRequests).//
		execute();
    }

    /**
     * @param value
     * @return
     */
    public static CellData createStringCell(String value) {

	CellData cell = new CellData();
	cell.setUserEnteredValue(new ExtendedValue().setStringValue(value));
	return cell;
    }

    /**
     * @param value
     * @return
     */
    public static CellData createIntCell(int value) {

	CellData cell = new CellData();
	cell.setUserEnteredValue(new ExtendedValue().setNumberValue(Double.valueOf(value)));
	return cell;
    }

}
