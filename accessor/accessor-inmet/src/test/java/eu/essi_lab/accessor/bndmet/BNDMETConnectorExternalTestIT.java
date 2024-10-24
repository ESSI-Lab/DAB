package eu.essi_lab.accessor.bndmet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.accessor.bndmet.model.BNDMETParameter;
import eu.essi_lab.accessor.bndmet.model.BNDMETParameter.BNDMET_Parameter_Code;
import eu.essi_lab.accessor.bndmet.model.BNDMETStation;
import eu.essi_lab.accessor.bndmet.model.BNDMETStation.BNDMET_Station_Code;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.resource.OriginalMetadata;

public class BNDMETConnectorExternalTestIT {

    @Test
    public void test() throws Exception {
	BNDMETConnector connector = new BNDMETConnector();
	connector.setSourceURL("https://apitempo.inmet.gov.br/BNDMET/");
	ListRecordsRequest request = new ListRecordsRequest();
	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	Iterator<OriginalMetadata> iterator = response.getRecords();
	while (iterator.hasNext()) {
	    OriginalMetadata originalMetadata = (OriginalMetadata) iterator.next();
	    String metadata = originalMetadata.getMetadata();
	    System.out.println("*********");
	    System.out.println(metadata);
	    JSONObject object = new JSONObject(metadata);
	    BNDMETStation station = new BNDMETStation(object);
	    System.out.println(station.getValue(BNDMET_Station_Code.DC_NOME));
	    BNDMETParameter parameter = station.getParameters().get(0);
	    String description = parameter.getValue(BNDMET_Parameter_Code.DESCRICAO);
	    System.out.println(description);
	    assertNotNull(description);
	    return;
	}
	fail();

    }

}
