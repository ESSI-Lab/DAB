package eu.essi_lab.workflow.processor.timeseries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.Before;

import eu.essi_lab.access.DataValidatorImpl;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.validator.wof.WML_1_1Validator;

public class WML_1_1SubsetProcessTest extends AbstractTimeSeriesSubsetTest {

    private WML11_Subset_Processor process;

    @Override
    protected long getOriginalStart() {
        return 1123180200000l;
    }
    
    @Override
    protected long getOriginalEnd() {
        return 1123180200000l + TimeUnit.DAYS.toMillis(1);
    }
    
    @Override
    protected int getOriginalSize() {
	return 49;
    }
    
    @Before
    public void init() {
	this.process = new WML11_Subset_Processor();
    }

    @Override
    public File getInputData() throws IOException {
	File tmpFile = File.createTempFile(getClass().getSimpleName(), ".xml");
	InputStream stream = NetCDFTimeSeriesSubsetProcessTest.class.getClassLoader().getResourceAsStream("wml_1_1-his4values.xml");
	FileOutputStream fos = new FileOutputStream(tmpFile);
	IOUtils.copy(stream, fos);
	fos.close();
	return tmpFile;

    }

    @Override
    public DataValidatorImpl getValidator() {
	DataValidatorImpl ret = new WML_1_1Validator();
	return ret;
    }

    @Override
    public AbstractTimeSubsetProcessor getProcess() {
	return process;
    }

    @Override
    public DataFormat getDataFormat() {
	return DataFormat.WATERML_1_1();
    }
}
