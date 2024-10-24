package eu.essi_lab.workflow.processor.grid;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.workflow.processor.ProcessorCapabilities;
import eu.essi_lab.workflow.processor.TargetHandler;

public class GDAL_To_PNG_ProcessorTest {

    private DataObject input;
    private DataDescriptor source;

    @Before
    public void before() throws IOException {
	this.input = new DataObject();
	InputStream stream = GDAL_To_PNG_ProcessorTest.class.getClassLoader().getResourceAsStream("nc_to_png_test.nc");
	input.setFileFromStream(stream, getClass().getSimpleName() + ".nc");
	stream.close();
	this.source = new DataDescriptor();
	source.setDataFormat(DataFormat.NETCDF());
	this.input.setDataDescriptor(source);

    }

    @Test
    public void testConversion() throws Exception {
	GDAL_To_PNG_Processor processor = new GDAL_To_PNG_Processor();

	DataDescriptor target = new DataDescriptor();
	target.setDataFormat(DataFormat.IMAGE_PNG());
	ProcessorCapabilities capabilities = new ProcessorCapabilities();
	TargetHandler targetHandler = new TargetHandler(source, target, capabilities);
	DataObject result = processor.process(input, targetHandler);

	File output = result.getFile();
	assertTrue(output.length() > 0);
	output.delete();

    }

    @After
    public void after() {
	if (input.getFile().exists()) {
	    input.getFile().delete();
	}

    }

}
