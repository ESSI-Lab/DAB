package eu.essi_lab.accessor.ecv;

import java.io.InputStream;
import java.util.Optional;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.net.downloader.Downloader;
import junit.framework.TestCase;

public class ECVInventoryConnectorTestExternalTestIT {
    @Test
    public void testExcelDataFileExist() throws Exception {

	String endpoint = "http://climatemonitoring.info/wp-content/uploads/2019/01/ECV_Inventory_v2.0.xlsx";

	Downloader downloader = new Downloader();

	Optional<InputStream> res = downloader.downloadOptionalStream(endpoint);

	Assert.assertTrue(res.isPresent());

	InputStream is = res.get();

	TestCase.assertNotNull(is);

	XSSFWorkbook wb = new XSSFWorkbook(is);

	XSSFSheet sheet = wb.getSheetAt(0);

	TestCase.assertNotNull(wb);

	TestCase.assertNotNull(sheet);

	wb.close();

	is.close();

    }

}
