package eu.essi_lab.accessor.bndmet;

import java.util.List;

import org.junit.Test;

import eu.essi_lab.accessor.bndmet.model.BNDMETStation;
import eu.essi_lab.accessor.bndmet.model.BNDMETStation.BNDMET_Station_Code;

public class BNDMETClientExternalTestIT {

    @Test
    public void test() {
	BNDMETClient client = new BNDMETClient();
	List<BNDMETStation> stations = client.getStations();
	for (BNDMETStation station : stations) {
	    System.out.println(
		    "Station:" + station.getValue(BNDMET_Station_Code.DC_NOME) + ":" + station.getValue(BNDMET_Station_Code.CD_WSI)+ ":" + station.getValue(BNDMET_Station_Code.CD_ESTACAO)+":");
	}
    }

}
