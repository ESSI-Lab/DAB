package eu.essi_lab.accessor.emodnet;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;

public class ERDDAPClient {
    private String url;

    public ERDDAPClient(String url) {
	this.url = url;

    }

    public List<ERDDAPRow> getRows() {
	return getRows(new String[] {}, new String[] {});

    }

    public List<ERDDAPRow> getRows(String[] pars, String[] constraints) {
	Downloader downloader = new Downloader();
	String parameters = "";
	if (pars == null) {
	    pars = new String[] {};
	}
	if (constraints == null) {
	    constraints = new String[] {};
	}
	if (pars.length > 0 || constraints.length > 0) {
	    for (int i = 0; i < pars.length; i++) {
		String par = pars[i];
		parameters += par;
		if (i < pars.length - 1) {
		    parameters += ",";
		} else {
		    parameters = URLEncoder.encode(parameters, StandardCharsets.UTF_8);
		    parameters += "&";
		}
	    }
	    for (int i = 0; i < constraints.length; i++) {
		String constraint = URLEncoder.encode(constraints[i], StandardCharsets.UTF_8);
		constraint = constraint.replace("%3D", "=");
		parameters += constraint + "&";
	    }
	    parameters = parameters.substring(0, parameters.length() - 1);
	    parameters = "?" + parameters;
	}
	String das = downloader.downloadOptionalString(url + ".json" + parameters).get();
	JSONObject json = new JSONObject(das);
	JSONObject table = json.getJSONObject("table");
	JSONArray columnNames = table.getJSONArray("columnNames");
	String[] headers = new String[columnNames.length()];
	for (int i = 0; i < columnNames.length(); i++) {
	    headers[i] = columnNames.getString(i);
	}
	List<ERDDAPRow> ret = new ArrayList<ERDDAPRow>();
	JSONArray rows = table.getJSONArray("rows");
	for (int i = 0; i < rows.length(); i++) {
	    JSONArray row = rows.getJSONArray(i);
	    Object[] values = new Object[columnNames.length()];
	    for (int j = 0; j < columnNames.length(); j++) {
		values[j] = row.get(j);
	    }
	    ret.add(new ERDDAPRow(headers, values));
	}
	return ret;
    }
}
