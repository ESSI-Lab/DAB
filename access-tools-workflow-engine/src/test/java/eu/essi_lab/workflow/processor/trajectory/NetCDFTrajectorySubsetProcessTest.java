package eu.essi_lab.workflow.processor.trajectory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;

import eu.essi_lab.access.DataValidatorImpl;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.validator.netcdf.NetCDFTrajectoryValidator;
import eu.essi_lab.workflow.processor.timeseries.AbstractTimeSubsetProcessor;
import eu.essi_lab.workflow.processor.timeseries.AbstractTrajectorySubsetTest;
import eu.essi_lab.workflow.processor.timeseries.NetCDF_Trajectory_Subset_Processor;

public class NetCDFTrajectorySubsetProcessTest extends AbstractTrajectorySubsetTest {
    private NetCDF_Trajectory_Subset_Processor process;

    @Override
    protected long getOriginalStart() {
        return 1687185699915l;
    }
    
    @Override
    protected long getOriginalEnd() {
        return 1687185740915l;
    }
    @Override
    protected int getOriginalSize() {
	return 42;
    }
    
    @Before
    public void init() {
	this.process = new NetCDF_Trajectory_Subset_Processor();
    }

    @Override
    public File getInputData() throws IOException {
	File tmpFile = File.createTempFile(getClass().getSimpleName(), ".nc");
	InputStream stream = NetCDFTrajectorySubsetProcessTest.class.getClassLoader().getResourceAsStream("trajectory.nc");
	FileOutputStream fos = new FileOutputStream(tmpFile);
	IOUtils.copy(stream, fos);
	fos.close();
	return tmpFile;

    }

    @Override
    public DataValidatorImpl getValidator() {
	DataValidatorImpl ret = new NetCDFTrajectoryValidator();
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
