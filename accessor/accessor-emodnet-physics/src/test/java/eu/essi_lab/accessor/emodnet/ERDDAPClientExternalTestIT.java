package eu.essi_lab.accessor.emodnet;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class ERDDAPClientExternalTestIT {

    @Test
    public void test() {
	String baseURL = "https://data-erddap.emodnet-physics.eu/erddap/tabledap/index";
	ERDDAPClient client = new ERDDAPClient(baseURL);
	List<ERDDAPRow> rows = client.getRows();
	for (ERDDAPRow row : rows) {
	    String title = (String) row.getValue("Title");
	    String dap = (String) row.getValue("tabledap");
	    if (title.toLowerCase().contains("river")) {
		System.out.println("FOUND " + dap);
	    }
	}

	// client = new
	// ERDDAPClient("https://data-erddap.emodnet-physics.eu/erddap/tabledap/ERD_EP_RIVER_INSITU_METADATA");
	// rows = client.getRows();
	// assertTrue(rows.size()>10);
	// ERDDAPRow row = rows.get(0);
	// System.out.println(row.getValue("PLATFORMCODE"));

    }

}
