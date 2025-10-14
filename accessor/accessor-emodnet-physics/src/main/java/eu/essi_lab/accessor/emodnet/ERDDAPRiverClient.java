package eu.essi_lab.accessor.emodnet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class ERDDAPRiverClient {
    private String baseURL;
    private List<String> metadataURLs = new ArrayList<String>();
    private String dataURL = null;

    public ERDDAPRiverClient(String url) {
	this.baseURL = url;
	ERDDAPClient client = new ERDDAPClient(baseURL);
	List<ERDDAPRow> rows = client.getRows();
	for (ERDDAPRow row : rows) {
	    String title = (String) row.getValue("Title");
	    String dap = (String) row.getValue("tabledap");
	    if (title.toLowerCase().contains("river") && title.toLowerCase().contains("flow")) {
		if (dap.toLowerCase().contains("metadata")) {
		    GSLoggerFactory.getLogger(getClass()).info("Found metadata URL " + dap);
		    metadataURLs.add(dap);
		} else {
		    GSLoggerFactory.getLogger(getClass()).info("Found data URL " + dap);
		    dataURL = dap;
		}
	    }
	}
    }

    public List<ERDDAPRow> getMetaData() {
	List<ERDDAPRow> ret = new ArrayList<ERDDAPRow>();
	for (String dap : metadataURLs) {
	    ERDDAPClient client = new ERDDAPClient(dap);
	    ret.addAll(client.getRows());
	}
	return ret;
    }

    public List<ERDDAPRow> getData(String stationCode, Date begin, Date end) {
	ERDDAPClient client = new ERDDAPClient(dataURL);
	String beginDate = ISO8601DateTimeUtils.getISO8601DateTime(begin);
	String endDate = ISO8601DateTimeUtils.getISO8601DateTime(end);
	return client.getRows(new String[] { "PLATFORMCODE", "time", "RVFL", "RVFL_QC" },
		new String[] { "PLATFORMCODE=\"" + stationCode + "\"", "time>=" + beginDate, "time<=" + endDate });
    }
}
