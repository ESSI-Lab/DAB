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

import junit.framework.TestCase;

public class ECVInventoryConnectorTest {

    private ECVInventoryConnector connector;

    private ECVInventoryOceanConnector oceanConnector;

    private ECVInventoryLandConnector landConnector;


    @Before
    public void init() {

	this.connector = new ECVInventoryConnector();

	this.landConnector = new ECVInventoryLandConnector();

	this.oceanConnector = new ECVInventoryOceanConnector();

    }

    @Test
    public void testExcelSize() throws Exception {
	InputStream is = ECVInventoryConnectorTest.class.getClassLoader().getResourceAsStream("ECV_Inventory_v2.0.xlsx");
	TestCase.assertNotNull(is);

	XSSFWorkbook wb = new XSSFWorkbook(is);

	XSSFSheet sheet = wb.getSheetAt(0);

	int rowint = sheet.getPhysicalNumberOfRows();

	TestCase.assertEquals(914, rowint);
	int noOfColumns = sheet.getRow(0).getPhysicalNumberOfCells();
	TestCase.assertEquals(67, noOfColumns);

	wb.close();
	is.close();

    }

    @Test
    public void testECVDomain() throws Exception {
	InputStream is = ECVInventoryConnectorTest.class.getClassLoader().getResourceAsStream("ECV_Inventory_v2.0.xlsx");
	TestCase.assertNotNull(is);

	XSSFWorkbook wb = new XSSFWorkbook(is);

	XSSFSheet sheet = wb.getSheetAt(0);

	XSSFRow r = sheet.getRow(0);

	Map<String, Integer> map = new HashMap<String, Integer>();
	int minColIx = r.getFirstCellNum(); // get the first column index for a row
	int maxColIx = r.getLastCellNum(); // get the last column index for a row
	for (int colIx = minColIx; colIx < maxColIx; colIx++) { // loop from first to last index
	    Cell cell = r.getCell(colIx); // get the cell
	    map.put(cell.getStringCellValue(), cell.getColumnIndex()); // add the cell contents (name of column) and
								       // cell index to the map
	}

	int idx1 = map.get("Domain");

	Row row1 = sheet.getRow(1);

	Cell c = row1.getCell(idx1);

	String domain = c.getStringCellValue();

	Assert.assertTrue(ECVDomain.OCEAN.getLabel().toLowerCase().equals(domain.toLowerCase()));

	Row row2 = sheet.getRow(2);

	Cell c2 = row2.getCell(idx1);

	String domain2 = c2.getStringCellValue();

	Assert.assertTrue(ECVDomain.ALL_DOMAINS.getLabel().toLowerCase().equals(domain2.toLowerCase()));

	wb.close();
	is.close();

    }

    @Test
    public void testgetECVHeaderRow() throws Exception {
	InputStream is = ECVInventoryConnectorTest.class.getClassLoader().getResourceAsStream("ECV_Inventory_v2.0.xlsx");
	TestCase.assertNotNull(is);

	XSSFWorkbook wb = new XSSFWorkbook(is);

	XSSFSheet sheet = wb.getSheetAt(0);

	XSSFRow headerRow = sheet.getRow(0);

	Map<String, Integer> mapResult = connector.getECVHeaderRow(headerRow);

	Assert.assertTrue(mapResult.size() == 67);

	wb.close();
	is.close();

    }

    @Test
    public void testCreateIdxList() throws Exception {
	InputStream is = ECVInventoryConnectorTest.class.getClassLoader().getResourceAsStream("ECV_Inventory_v2.0.xlsx");
	TestCase.assertNotNull(is);

	XSSFWorkbook wb = new XSSFWorkbook(is);

	XSSFSheet sheet = wb.getSheetAt(0);

	XSSFRow headerRow = sheet.getRow(0);

	Map<String, Integer> mapResult = connector.getECVHeaderRow(headerRow);

	ArrayList<Integer> idxList = connector.createIdxList(mapResult);

	Assert.assertTrue(idxList.size() == 27);

	wb.close();
	is.close();

    }

    @Test
    public void testECVInventorySatelliteDomainNumbers() throws Exception {
	InputStream is = ECVInventoryConnectorTest.class.getClassLoader().getResourceAsStream("ECV_Inventory_v2.0.xlsx");
	TestCase.assertNotNull(is);

	XSSFWorkbook wb = new XSSFWorkbook(is);

	XSSFSheet sheet = wb.getSheetAt(0);

	XSSFRow headerRow = sheet.getRow(0);

	Map<String, Integer> mapResult = connector.getECVHeaderRow(headerRow);

	ArrayList<Integer> idxList = connector.createIdxList(mapResult);

	Assert.assertTrue(idxList.size() == 27);

	int totalRows = sheet.getPhysicalNumberOfRows();

	int j = 0;

	for (int i = 1; i < totalRows; i++) {

	    Row row = sheet.getRow(i);

	    ECVInventorySatellite res = connector.getECVInventoryFields(idxList, row);

	    if (res != null)
		j++;
	}

	Assert.assertTrue("NUMBER OF ALL DOMAIN RECORDS", j == 308);

	j = 0;
	for (int i = 1; i < totalRows; i++) {

	    Row row = sheet.getRow(i);

	    ECVInventorySatellite res = oceanConnector.getECVInventoryFields(idxList, row);

	    if (res != null)
		j++;
	}

	Assert.assertTrue("NUMBER OF OCEAN DOMAIN RECORDS", j == 99);

	j = 0;
	for (int i = 1; i < totalRows; i++) {

	    Row row = sheet.getRow(i);

	    ECVInventorySatellite res = landConnector.getECVInventoryFields(idxList, row);

	    if (res != null)
		j++;
	}

	Assert.assertTrue("NUMBER OF LAND DOMAIN RECORDS", j == 55);

	wb.close();
	is.close();

    }

}
