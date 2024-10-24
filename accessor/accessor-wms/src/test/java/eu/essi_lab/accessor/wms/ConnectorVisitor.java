package eu.essi_lab.accessor.wms;

import java.util.Iterator;

import eu.essi_lab.accessor.wms._1_3_0.WMS_1_3_0Connector;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class ConnectorVisitor {

    private WMS_1_3_0Connector connector;
    private Integer resourcesNumber = 0;

    public ConnectorVisitor(WMS_1_3_0Connector connector) {
	this.connector = connector;
    }

    public void visit() {
	visit(null);

    }

    private void visit(String token) {
	System.out.println("Visiting " + token);
	ListRecordsRequest request = new ListRecordsRequest();
	request.setResumptionToken(token);
	try {
	    ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	    Iterator<OriginalMetadata> iterator = response.getRecords();
	    while (iterator.hasNext()) {
		OriginalMetadata record = (OriginalMetadata) iterator.next();
		// System.out.println(record.getMetadata());
		resourcesNumber++;
	    }
	    if (response.getResumptionToken() == null) {
		return;
	    }
	    visit(response.getResumptionToken());
	} catch (GSException e) {
	    e.printStackTrace();
	}

    }

    public Integer getResourcesNumber() {
	return resourcesNumber;
    }

}
