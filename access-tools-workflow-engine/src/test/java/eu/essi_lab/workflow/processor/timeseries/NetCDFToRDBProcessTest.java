
package eu.essi_lab.workflow.processor.timeseries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.model.resource.data.DataObject;

public class NetCDFToRDBProcessTest {

    private NetCDF_To_RDB_Processor process;
    private DataObject dataObject;

    @Before
    public void init() throws Exception {
	this.process = new NetCDF_To_RDB_Processor();
	this.dataObject = new DataObject();

    }

    @Test
    public void testNetCDFToRDB() throws Exception {

	InputStream stream = NetCDFToRDBProcessTest.class.getClassLoader().getResourceAsStream("netcdf-his4values.nc");
	dataObject.setFileFromStream(stream, getClass().getSimpleName() + ".nc");
	dataObject.getFile().deleteOnExit();
	stream.close();

	commonTest();

    }

    @Test
    public void testEmptyNetCDFToRDB() throws Exception {

	InputStream stream = NetCDFToRDBProcessTest.class.getClassLoader().getResourceAsStream("netcdf-empty.nc");
	dataObject.setFileFromStream(stream, getClass().getSimpleName() + ".nc");
	dataObject.getFile().deleteOnExit();
	stream.close();

	commonTest();

    }

    private void commonTest() throws Exception {
	DataObject rdbObject = process.process(null, dataObject, null);

	File file = rdbObject.getFile();
	FileInputStream fis = new FileInputStream(file);
	InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
	BufferedReader reader = new BufferedReader(isr);
	String line;
	while ((line = reader.readLine()) != null) {
	    System.out.println(line);
	}
	reader.close();
	isr.close();
	fis.close();

    }

    @After
    public void after() {
	if (dataObject != null && dataObject.getFile() != null) {
	    dataObject.getFile().delete();
	}
    }

}
