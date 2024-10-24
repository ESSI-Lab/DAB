package eu.essi_lab.accessor.bnhs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;

import eu.essi_lab.lib.net.googlesheet.GoogleTableSheetClient;

public class GoogleSheetClientExternalTestIT {

    @Test
    public void test() throws Exception {

	GoogleTableSheetClient client = new GoogleTableSheetClient("1ni9_BNcgoWD5HcU0sT_E20CwcOpOjsdek7_fQd4DWtI",
		"Station List - operational", "StationID") {

	    @Override
	    public Optional<String> getCredentialsFilePath() {
		return Optional.of("bnhs/serviceAccount.json");
	    }

	    @Override
	    public String getApplicationName() {
		return "BNHS";
	    }
	};
	Integer size = client.getRowSize();
	assertTrue(size > 100);
	try {
	    client = new GoogleTableSheetClient("1RG-_ke-5Sa_7TMdv2FjEWXvKN1IvVxNXVGj3EqThPEQ", "Ip Lab") {

		@Override
		public Optional<String> getCredentialsFilePath() {
		    return Optional.of("bnhs/serviceAccount.json");
		}

		@Override
		public String getApplicationName() {
		    return "BNHS";
		}
	    };

	    client.getRowSize();
	} catch (GoogleJsonResponseException e) {
	    int sc = e.getStatusCode();
	    assertEquals(403, sc); // should be indeed forbidden!
	}

    }

}
