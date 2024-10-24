//package eu.essi_lab.workflow.processor.grid;
//
//import static org.junit.Assert.assertEquals;
//
//import java.io.File;
//import java.io.InputStream;
//import java.util.Vector;
//
//import org.gdal.gdal.gdal;
//import org.junit.Test;
//
//import eu.essi_lab.lib.utils.GSLoggerFactory;
//import eu.essi_lab.model.resource.data.CRS;
//import eu.essi_lab.model.resource.data.DataDescriptor;
//import eu.essi_lab.model.resource.data.DataFormat;
//import eu.essi_lab.model.resource.data.DataObject;
//import eu.essi_lab.workflow.processor.CapabilityElement;
//import eu.essi_lab.workflow.processor.ProcessorCapabilities;
//import eu.essi_lab.workflow.processor.TargetHandler;
//import eu.essi_lab.workflow.processor.grid.GDALConstants.Implementation;
//import ucar.ma2.Array;
//import ucar.nc2.dataset.NetcdfDataset;
//
//public class GDAL_NetCDF_CRS_Converter_ProcessorTest {
//
//    org.slf4j.Logger logger = GSLoggerFactory.getLogger(this.getClass());
//
//    @Test
//    public void testDifferentImplementations2() throws Exception {
//	// we need to be sure that the results are constant iteration after iteration: this is not the current case
//	// using GDAL JNI! GDAL JNI gives correct result at the first iteration, then it gives wrong results.
//	for (int i = 0; i < 3; i++) {
//
//	    InputStream stream = GDAL_NetCDF_CRS_Converter_ProcessorTest.class.getClassLoader().getResourceAsStream("input.nc");
//
//	    DataObject object = new DataObject();
//	    object.setFileFromStream(stream, "GDAL_NetCDF_CRS_Converter_ProcessorTest.nc");
//	    File inputFile = object.getFile();
//	    inputFile.deleteOnExit();
//
//	    GDAL_NetCDF_CRS_Converter_Processor processor = new GDAL_NetCDF_CRS_Converter_Processor();
//
//	    DataDescriptor target = new DataDescriptor();
//	    target.setCRS(CRS.EPSG_4326());
//	    target.setDataFormat(DataFormat.NETCDF_3());
//	    ProcessorCapabilities capabilities = new ProcessorCapabilities();
//	    capabilities.setCRSCapability(CapabilityElement.anyFromCRS(CRS.EPSG_4326()));
//
//	    int size = 5;
//
//	    double resX = 0.09497903331625168;
//	    double resX2 = resX / 2.0;
//	    double resY = 0.05431911269926099;
//	    double resY2 = resY / 2.0;
//	    double w = -55.445568416658126;
//	    double s = -36.11064995050771;
//	    double e = w + resX * size;
//	    double n = s + resY * size;
//	    s = s + resY2;
//	    n = n - resY2;
//	    w = w + resX2;
//	    e = e - resX2;
//
//	    TargetHandler handler = new TargetHandler(null, target, capabilities);
//
//	    target.setEPSG4326SpatialDimensions(n, e, s, w);
//	    target.getFirstSpatialDimension().getContinueDimension().setResolution(resY);
//	    target.getSecondSpatialDimension().getContinueDimension().setResolution(resX);
//
//	    GDALConstants.IMPLEMENTATION = Implementation.JNI;
//
//	    DataObject result = processor.process(object, handler);
//	    File outputFile = result.getFile();
//	    outputFile.deleteOnExit();
//
//	    NetcdfDataset nc = NetcdfDataset.openDataset(outputFile.getAbsolutePath());
//	    Array latArray = nc.findVariable("lat").read();
//	    double valueWithJNI = latArray.getDouble(0);
//	    nc.close();
//
//	    GDALConstants.IMPLEMENTATION = Implementation.RUNTIME;
//
//	    result = processor.process(object, handler);
//	    File outputFile2 = result.getFile();
//	    outputFile.deleteOnExit();
//
//	    outputFile2.deleteOnExit();
//
//	    nc = NetcdfDataset.openDataset(outputFile2.getAbsolutePath());
//	    latArray = nc.findVariable("lat").read();
//	    double valueWithRuntime = latArray.getDouble(0);
//
//	    System.out.println(valueWithJNI + " " + valueWithRuntime);
//	    // the check starts from the second iteration because the first iteration will strangely give good results
//	    // ...
//	    if (i > 0) {
//		assertValues(valueWithJNI, valueWithRuntime);
//	    }
//
//	    nc.close();
//	    inputFile.delete();
//	    outputFile.delete();
//	    outputFile2.delete();
//	}
//
//    }
//
//    @Test
//    public void testDifferentImplementations() throws Exception {
//	for (int i = 0; i < 3; i++) {
//	    InputStream stream = GDAL_NetCDF_CRS_Converter_ProcessorTest.class.getClassLoader().getResourceAsStream("input.nc");
//
//	    DataObject object = new DataObject();
//	    object.setFileFromStream(stream, "GDAL_NetCDF_CRS_Converter_ProcessorTest.nc");
//	    File inputFile = object.getFile();
//	    inputFile.deleteOnExit();
//
//	    int size = 5;
//
//	    Vector<String> vector = new Vector<>(20, 10);
//	    vector.add("-of");
//	    vector.add("netCDF");
//	    vector.add("-t_srs");
//	    vector.add("EPSG:4326");
//	    vector.add("-tr");
//	    double resX = 0.09497903331625168;
//	    vector.add("" + resX);
//	    double resY = 0.05431911269926099;
//	    vector.add("" + resY);
//	    vector.add("-te");
//	    double minX = -55.445568416658126;
//	    vector.add("" + minX);
//	    double minY = -36.11064995050771;
//	    vector.add("" + minY);
//	    vector.add("" + (minX + resX * size));
//	    vector.add("" + (minY + resY * size));
//	    gdal.AllRegister();
//
//	    File outputFile = File.createTempFile("GDAL_NetCDF_CRS_Converter_ProcessorTest", ".nc");
//	    outputFile.deleteOnExit();
//
//	    GDAL_NetCDF_CRS_Converter_Processor.executeWithJNI(inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), vector);
//
//	    NetcdfDataset nc = NetcdfDataset.openDataset(outputFile.getAbsolutePath());
//	    Array latArray = nc.findVariable("lat").read();
//	    double valueWithJNI = latArray.getDouble(0);
//	    nc.close();
//
//	    File outputFile2 = File.createTempFile("GDAL_NetCDF_CRS_Converter_ProcessorTest", ".nc");
//	    outputFile2.deleteOnExit();
//
//	    GDAL_NetCDF_CRS_Converter_Processor.executeWithRuntime(inputFile.getAbsolutePath(), outputFile2.getAbsolutePath(), vector);
//
//	    nc = NetcdfDataset.openDataset(outputFile2.getAbsolutePath());
//	    latArray = nc.findVariable("lat").read();
//	    double valueWithRuntime = latArray.getDouble(0);
//
//	    assertValues(valueWithJNI, valueWithRuntime);
//
//	    nc.close();
//	    inputFile.delete();
//	    outputFile.delete();
//	    outputFile2.delete();
//	}
//
//    }
//
//    /**
//     * checks that the value obtained with JNI is wrong, and the value obtained with runtime is correct
//     * 
//     * @param valueWithJNI
//     * @param valueWithRuntime
//     */
//    private void assertValues(double valueWithJNI, double valueWithRuntime) {
//	System.out.println(valueWithJNI + " " + valueWithRuntime);
//
//	double badValue = 9.969209968386869E36;
//
//	double goodValue = -36.083490394158076;
//
//	assertEquals("The JNI implementation is expected to give the expected bad value because of a bug in GDAL."
//		+ "If instead this assertion fails and the actual value turns out to be: " + goodValue
//		+ ", then it's good, it means the JNI bug has been fixed. Then this assertion should be updated changing badValue with goodValue and the constant "
//		+ GDALConstants.class.getSimpleName() + ".IMPLEMENTATION could be turned to JNI.", badValue, valueWithJNI, 0.00000000001);
//
//	assertEquals("The RUNTIME implementation should give the expected value as the result", goodValue, valueWithRuntime, 0.00000000001);
//
//    }
//
//}
