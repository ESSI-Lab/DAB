package eu.essi_lab.accessor.wof;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class CUAHSIHISCentralTest {

    public static void main(String[] args) throws GSException {

	String endpoint1 = "http://hiscentral.cuahsi.org/webservices/hiscentral.asmx";
	String endpoint2 = "http://193.206.192.247/hiscentral/webservices/hiscentral.asmx";

	CUAHSIHISCentralConnector connector = new CUAHSIHISCentralConnector();
	connector.setSourceURL(endpoint2);
	connector.setFirstSiteOnly(true);

	ListRecordsRequest request = new ListRecordsRequest();

	String token = null;
//	 token = "44:";
	int count = 0;
	long end;

	Long start = null;
	while (start == null || token != null) {
	    if (start == null) {
		start = System.currentTimeMillis();
	    }
	    System.out.println("Current token: " + token);

	    request.setResumptionToken(token);
	    ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	    Iterator<OriginalMetadata> iterator = response.getRecords();
	    List<OriginalMetadata> list = Lists.newArrayList(iterator);
	    count++;
	    token = response.getResumptionToken();

	    end = System.currentTimeMillis();
	    System.out.println("Current time: " + (end - start) / 1000);
	}
	end = System.currentTimeMillis();
	System.out.println("Current time: " + (end - start) / 1000);
	System.out.println("Retrieved " + count + " original metadata records.");
    }

}
