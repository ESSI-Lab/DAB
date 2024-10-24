package eu.essi_lab.accessor.inmet;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import eu.essi_lab.lib.net.utils.FTPDownloader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;

public class INMETExternalTestIT {

    private INMETConnector connector;
    private GSSource source;
    private INMETMapper mapper;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init() throws Exception {
	this.connector = new INMETConnector();
	this.mapper = new INMETMapper();
	this.source = Mockito.mock(GSSource.class);

    }

    @Test
    @Ignore // obsolete accessor
    public void testHarvestRecords() throws GSException, IOException {
	Mockito.when(source.getEndpoint()).thenReturn("ftp://ftp.inmet.gov.br/");
	connector.setSourceURL("ftp://ftp.inmet.gov.br/");

	this.connector.setDownloader(new FTPDownloader());

	// List<String> value = new ArrayList<String>();
	// value.add("http://dd.weather.gc.ca/hydrometric/csv/");
	// value.add("http://dd.weather.gc.ca/hydrometric/doc/");
	//
	// Mockito.when(connector.getWebConnector().getHrefs(canadaHTML)).thenReturn(value);

	connector.getINMETFiles();

	List<String> res = connector.getINMETfileNames();
	ListRecordsRequest listRecords = new ListRecordsRequest();
	ListRecordsResponse<OriginalMetadata> response;
	int count = 0;
	response = connector.listRecords(listRecords);
	if (res != null && !res.isEmpty()) {
	    for (String s : res) {
		if (count == 0) {
		    count++;
		    continue;
		}
		if (count == 10)
		    break;

		listRecords.setResumptionToken(s);
		response = connector.listRecords(listRecords);
		count++;
	    }
	}
	System.out.println(count);
	Assert.assertTrue(count == 10);
	Assert.assertEquals("PREC_A015_20180927.HIS.CSV", response.getResumptionToken());
	if (response.getRecords().hasNext()) {
	    OriginalMetadata om = response.getRecords().next();
	    HarmonizedMetadata result = mapper.execMapping(om, new GSSource()).getHarmonizedMetadata();
	    Assert.assertNotNull(result);
	}

	// first record

	// ListRecordsResponse<OriginalMetadata> response = connector.listRecords(listRecords);
	// Assert.assertEquals(secondId, response.getResumptionToken());
	//
	// listRecords.setResumptionToken(secondId);
	// String thirdId = "PREC_A003_20180927.HIS.CSV";
	// response = connector.listRecords(listRecords);
	// Assert.assertEquals(thirdId, response.getResumptionToken());

	// int stationsnumber = connector.getStationCount();
	// int var = stationsnumber / 10;
	// var = var * 10;

	// add max number of records
	// connector.setMaxRecords(50);
	// listRecords.setResumptionToken("51");
	// response = connector.listRecords(listRecords);
	// Assert.assertEquals(null, response.getResumptionToken());
	//
	// connector.setMaxRecords(10);
	// listRecords.setResumptionToken(null);
	// response = connector.listRecords(listRecords);
	// Assert.assertEquals("10", response.getResumptionToken());

    }
    //
    //// @Test
    //// public void testVariables() throws GSException, IOException {
    ////
    //// Mockito.when(source.getEndpoint()).thenReturn("http://dd.weather.gc.ca/hydrometric/");
    //// connector.setSourceURL("http://dd.weather.gc.ca/hydrometric/");
    //// String canadaHTML =
    // IOUtils.toString(CANADAMSCConnectorTest.class.getClassLoader().getResourceAsStream("wafhtml"));
    //// this.connector.setDownloader(new MockedDownloader(canadaHTML));
    ////
    //// connector.getCANADAStations();
    //// List<ECStation> res = connector.getStations();
    //// System.out.println(res.size());
    ////
    //// int result = 0;
    ////
    //// for(ECStation station: res) {
    ////
    //// List<String> variables = connector.checkVariable(station);
    //// result += variables.size();
    //// }
    ////
    //// System.out.println(result);
    ////
    //// }
    //
    // @Test
    // public void testWaterVariable() throws GSException, IOException {
    //
    // InputStream stream =
    // INMETConnectorTestIT.class.getClassLoader().getResourceAsStream("AB_05EC005_daily_hydrometric.csv");
    //
    // TestCase.assertNotNull(stream);
    //
    // BufferedReader bfReader = null;
    //
    // bfReader = new BufferedReader(new InputStreamReader(stream));
    // //String temp = null;
    // bfReader.readLine(); // skip header line
    // String temp = bfReader.readLine();
    // int i = 0;
    // boolean waterVar = false;
    // boolean dischargeVar = false;
    //
    //
    // while ((temp = bfReader.readLine()) != null && i < 10) {
    // String[] split = temp.split(",", -1);
    // String waterVariable = split[2];
    // String dischargeVariable = split[6];
    //
    // if(waterVariable != null && !waterVariable.isEmpty()) {
    // waterVar = true;
    //
    // }
    //
    // if(dischargeVariable != null && !dischargeVariable.isEmpty()) {
    // dischargeVar = true;
    //
    // }
    // i++;
    // }
    //
    // if(bfReader != null)
    // bfReader.close();
    // Assert.assertTrue(waterVar);
    // Assert.assertFalse(dischargeVar);
    //
    // }
    //
    // @Test
    // public void testAllVariable() throws GSException, IOException {
    //
    // InputStream stream =
    // INMETConnectorTestIT.class.getClassLoader().getResourceAsStream("BC_08HD018_daily_hydrometric.csv");
    //
    // TestCase.assertNotNull(stream);
    //
    // BufferedReader bfReader = null;
    //
    // bfReader = new BufferedReader(new InputStreamReader(stream));
    // //String temp = null;
    // bfReader.readLine(); // skip header line
    // String temp = bfReader.readLine();
    // int i = 0;
    // boolean waterVar = false;
    // boolean dischargeVar = false;
    //
    //
    // while ((temp = bfReader.readLine()) != null && i < 10) {
    // String[] split = temp.split(",", -1);
    // String waterVariable = split[2];
    // String dischargeVariable = split[6];
    //
    // if(waterVariable != null && !waterVariable.isEmpty()) {
    // waterVar = true;
    //
    // }
    //
    // if(dischargeVariable != null && !dischargeVariable.isEmpty()) {
    // dischargeVar = true;
    //
    // }
    // i++;
    // }
    //
    // if(bfReader != null)
    // bfReader.close();
    // Assert.assertTrue(waterVar);
    // Assert.assertTrue(dischargeVar);
    //
    // }
    //

}
