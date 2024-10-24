package eu.essi_lab.accessor.ecv;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

/**
 * @author roncella
 *         USED FIELDS FOR MAPPING
 *         Responder name,Responder E-mail,Co-editor E-mail (optional), Data record name and version
 *         (optional),Responsible Organisation, Status of peer-review
 *         Maintenance and user support commitment, QA Process, ECV,ECV Product,Physical quantity,SI units, Satellites
 *         and Instruments (Data),
 *         Link to source,Extent (Lat/Long),Domain,Horizontal resolution,Vertical resolution,Temporal resolution, Start
 *         date of TCDR,End-date of TCDR,
 *         CDR-generation documentation (link),Data documentation (link),Scientific-review process (link),Access point,
 *         Data record (link),FCDR availability (link),Data format
 *         ALL OTHER FIELDS
 *         RecordID,Published,Existing or Planned data record,Observer E-mail (optional),
 *         Data record identifier,,TCDR family,Official citation reference (optional),
 *         Collection Organisation,Calibration Organisation,FCDR Organisation,Inter-calibration,Organisation,TCDR
 *         Organisation,GCOS Requirements Organisation,
 *         Peer Review Organisation,Archiving Organisation,User-service organisation,User-feedback organisation,Level of
 *         commitment,
 *         Assessment Body,QA Organisation,GCOS-requirements compliance assessment,
 *         GCOS-guidelines peer-review compliance assessment,Quantitative maturity index assessment,
 *         Satellite/Instrument combination,Comments (Optional),Satellites and Instruments (Inter-calibration),
 *         Inter-calibration Satellite/Instrument combination Comments (Optional),Ground-based network,
 *         Domain Comment (Optional),,Accuracy,Stability,
 *         TCDR heritage,,Type of access,
 *         Restrictions to access,Registration / ordering ,Metadata standard,Dissemination mechanisms,
 *         Release date (yyyy),Climate applications,Users
 */

public class ECVInventoryMapperTest {

    private ECVInventoryMapper mapper;
    private ECVInventoryOceanConnector oceanConnector;
    private ECVInventoryLandConnector landConnector;
    private ECVInventoryConnector ecvConnector;
    private String endpoint = "http://climatemonitoring.info/wp-content/uploads/2019/01/ECV_Inventory_v2.0.xlsx";

    @Before
    public void init() {
	this.mapper = new ECVInventoryMapper();
	this.oceanConnector = new ECVInventoryOceanConnector();
	this.landConnector = new ECVInventoryLandConnector();
	this.ecvConnector = new ECVInventoryConnector();
    }

    @Test
    public void testOnlineLinksFromExample() throws Exception {
	InputStream stream = ECVInventoryMapperTest.class.getClassLoader().getResourceAsStream("ECV_Inventory_v2.0.xlsx");

	TestCase.assertNotNull(stream);

	XSSFWorkbook wb = new XSSFWorkbook(stream);

	XSSFSheet sheet = wb.getSheetAt(0);

	XSSFRow r = sheet.getRow(0);

	//int totalRows = sheet.getPhysicalNumberOfRows();

	Map<String, Integer> map = oceanConnector.getECVHeaderRow(r);
	
	ArrayList<Integer> idxList = ecvConnector.createIdxList(map);
	
	Row row = sheet.getRow(28);
	
	ECVInventorySatellite fields = ecvConnector.getECVInventoryFields(idxList, row);
	
	int idxForColumn14 = map.get("Link to source");
	int idxForColumn21 = map.get("Data record (link)");
	int idxForColumn22 = map.get("FCDR availability (link)");
	int idxForColumn23 = map.get("Data record identifier");

	Cell cell1 = row.getCell(idxForColumn14); // Get the cells for each of the indexes
	Cell cell2 = row.getCell(idxForColumn21);
	Cell cell3 = row.getCell(idxForColumn22);
	Cell cell4 = row.getCell(idxForColumn23);

	String res1 = mapper.extractLink(cell1.getStringCellValue());
	String res2 = mapper.extractLink(cell2.getStringCellValue());
	String res3 = mapper.extractLink(cell3.getStringCellValue());
	String res4 = mapper.extractLink(cell4.getStringCellValue());
	
	Assert.assertTrue(res1 == null);
	Assert.assertTrue(res2.equals("http://ceres.larc.nasa.gov/products.php?product=ebaf-toa"));
	Assert.assertTrue(res3.equals("https://eosweb.larc.nasa.gov/project/ceres/bds_terra-fm1_ed3_table"));
	Assert.assertTrue(res4.equals("http://dx.doi.org/10.5067/terra+aqua/ceres/ebaf-toa_l3b.002.8"));

	stream.close();
	wb.close();

    }

    @Test
    public void testMinimalMapperFromExample() throws Exception {
	InputStream stream = ECVInventoryMapperTest.class.getClassLoader().getResourceAsStream("ECV_Inventory_v2.0.xlsx");

	TestCase.assertNotNull(stream);

	XSSFWorkbook wb = new XSSFWorkbook(stream);

	XSSFSheet sheet = wb.getSheetAt(0);

	XSSFRow r = sheet.getRow(0);

	int totalRows = sheet.getPhysicalNumberOfRows();

	String test = "";
	int i = 0;

	Map<String, Integer> map = new HashMap<String, Integer>();
	int minColIx = r.getFirstCellNum(); // get the first column index for a row
	int maxColIx = r.getLastCellNum(); // get the last column index for a row
	for (int colIx = minColIx; colIx < maxColIx; colIx++) { // loop from first to last index
	    Cell cell = r.getCell(colIx); // get the cell
	    map.put(cell.getStringCellValue(), cell.getColumnIndex()); // add the cell contents (name of column) and
								       // cell index to the map
	}
	
	
	
	int idxForColumn1 = map.get("Domain");
	int idxForColumn2 = map.get("Responder name");
	int idxForColumn3 = map.get("Responder E-mail");
	int idxForColumn4 = map.get("Co-editor E-mail (optional)");
	int idxForColumn5 = map.get("Responsible Organisation");
	int idxForColumn6 = map.get("Status of peer-review");
	int idxForColumn7 = map.get("Maintenance and user support commitment");
	int idxForColumn8 = map.get("QA Process");
	int idxForColumn9 = map.get("ECV");
	int idxForColumn10 = map.get("ECV Product");
	int idxForColumn11 = map.get("Physical quantity");
	int idxForColumn12 = map.get("SI units");
	int idxForColumn13 = map.get("Satellites and Instruments (Data)");
	int idxForColumn14 = map.get("Link to source");
	int idxForColumn15 = map.get("Extent (Lat/Long)");
	int idxForColumn16 = map.get("Horizontal resolution");
	int idxForColumn17 = map.get("Vertical resolution");
	int idxForColumn18 = map.get("Temporal resolution");
	int idxForColumn19 = map.get("Start date of TCDR");
	int idxForColumn20 = map.get("End-date of TCDR");
	int idxForColumn21 = map.get("Data record (link)");
	int idxForColumn22 = map.get("FCDR availability (link)");
	int idxForColumn23 = map.get("Data format");
	System.out.println("Number of rows: " + totalRows);
	for (int x = 1; x < totalRows; x++) {
	    Row row = sheet.getRow(x);
	    int cellnum;
	    // Cell c = r.getCell(1);

	    Cell cell1 = row.getCell(idxForColumn1); // Get the cells for each of the indexes
	    Cell cell2 = row.getCell(idxForColumn2);
	    String res = cell1.getStringCellValue();
	    System.out.println("Row " + x + ":" + res);

	    String res2 = cell2.getStringCellValue();
	    System.out.println("Responder name " + x + ":" + res2);
	    // String[] splittedTemp = temp.split(",", -1);
	    // String test = splittedTemp[0].replace("\"", "") + "," + splittedTemp[2].replace("\"", "") + ","
	    // +splittedTemp[6].replace("\"", "") + "," +splittedTemp[7].replace("\"", "") + ","
	    // +splittedTemp[8].replace("\"", "") + "," +splittedTemp[9].replace("\"", "") + "," +
	    // splittedTemp[10].replace("\"", "") + "," +splittedTemp[5].replace("\"", "") + ","
	    // +splittedTemp[4].replace("\"", "") + "," +splittedTemp[3].replace("\"", "") + "," + endpoint + "," +
	    // endpoint + "/thunder_data_GSOD/" + splittedTemp[0].replace("\"", "") + ".txt";
	    //
	    // System.out.println(test);

	}

	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(test);

	// GSResource resource = mapper.map(originalMD, new GSSource());
	//
	// HarmonizedMetadata result = resource.getHarmonizedMetadata();
	//
	// TestCase.assertNotNull(result);
	//
	// CoreMetadata core = result.getCoreMetadata();
	//
	// MIMetadata metadata = core.getMIMetadata();
	//
	// DataIdentification dataIdentification = metadata.getDataIdentification();
	// TestCase.assertNotNull(dataIdentification);
	//
	// // title
	// TestCase.assertEquals("Acquisitions of Monthly Thunder Count (TD) at station: WXPOD 7018",
	// dataIdentification.getCitationTitle());
	//
	// // bbox
	// GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	// TestCase.assertEquals(0.0, bbox.getEast());
	// TestCase.assertEquals(0.0, bbox.getWest());
	// TestCase.assertEquals(0.0, bbox.getNorth());
	// TestCase.assertEquals(0.0, bbox.getSouth());
	//
	// //elevation
	// VerticalExtent verticalExtent = dataIdentification.getVerticalExtent();
	// TestCase.assertEquals(7018.0, verticalExtent.getMaximumValue());
	// TestCase.assertEquals(7018.0, verticalExtent.getMinimumValue());
	// // id
	// TestCase.assertEquals(Optional.empty(), resource.getOriginalId());
	//
	// // responsible party
	// ResponsibleParty originator = dataIdentification.getPointOfContact("pointOfContact");
	//
	// TestCase.assertEquals("Tripura University", originator.getOrganisationName());
	// TestCase.assertEquals("Anirban Guha", originator.getIndividualName());
	//
	// // time
	// TemporalExtent time = dataIdentification.getTemporalExtent();
	// TestCase.assertEquals("AFTER", time.getIndeterminateBeginPosition().toString());
	// TestCase.assertEquals("NOW", time.getIndeterminateEndPosition().toString());
	// TestCase.assertEquals("2011-03-09T00:00:00Z", time.getBeginPosition());
	// TestCase.assertEquals("2013-07-30T00:00:00Z", time.getEndPosition());
	//
	// // online
	//
	// TestCase.assertEquals("ftp://18.18.83.11/thunder_data_GSOD/007018.txt",
	// metadata.getDistribution().getDistributionOnline().getLinkage());
	//
	// // platform
	// MIPlatform platform = metadata.getMIPlatform();
	// TestCase.assertEquals("WXPOD 7018", platform.getDescription());
	//
	// // coverage
	// CoverageDescription coverage = metadata.getCoverageDescription();
	// TestCase.assertEquals("Monthly Thunder Count Units: count Resolution: YEARLY",
	// coverage.getAttributeDescription());
	// TestCase.assertEquals("Monthly Thunder Count_YEARLY", coverage.getAttributeIdentifier());
	stream.close();
	wb.close();
    }

}
