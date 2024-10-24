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
import eu.essi_lab.validator.netcdf.classic.NetCDF3TimeSeriesValidator;

public class NetCDFTimeSeriesSubsetProcessTest extends AbstractTimeSeriesSubsetTest {
    private NetCDF_TimeSeries_Subset_Processor process;

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
	this.process = new NetCDF_TimeSeries_Subset_Processor();
    }

    @Override
    public File getInputData() throws IOException {
	File tmpFile = File.createTempFile(getClass().getSimpleName(), ".nc");
	InputStream stream = NetCDFTimeSeriesSubsetProcessTest.class.getClassLoader().getResourceAsStream("netcdf-his4values.nc");
	FileOutputStream fos = new FileOutputStream(tmpFile);
	IOUtils.copy(stream, fos);
	fos.close();
	return tmpFile;

    }

    @Override
    public DataValidatorImpl getValidator() {
	DataValidatorImpl ret = new NetCDF3TimeSeriesValidator();
	return ret;
    }

    @Override
    public AbstractTimeSubsetProcessor getProcess() {
	return process;
    }

    @Override
    public DataFormat getDataFormat() {
	return DataFormat.NETCDF();
    }

}
