package eu.essi_lab.netcdf;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.netcdf.examples.h1.points.ExampleH1Writer;
import eu.essi_lab.netcdf.examples.h2.timeseries.ExampleH2Writer;
import eu.essi_lab.netcdf.examples.h2.timeseries.ExampleH3Writer;
import eu.essi_lab.netcdf.examples.h2.timeseries.ExampleH4Writer;
import eu.essi_lab.netcdf.examples.h2.timeseries.ExampleH5Writer;
import eu.essi_lab.netcdf.examples.h2.timeseries.ExampleH6Writer;
import eu.essi_lab.netcdf.examples.h2.timeseries.ExampleH7Writer;
import junit.framework.Assert;
import ucar.nc2.NetcdfFileWriter.Version;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.nc2.ft.point.StationPointFeature;
import ucar.nc2.ft.point.writer.CFPointWriter;
import ucar.nc2.ft.point.writer.CFPointWriterConfig;
import ucar.nc2.ft.point.writer.WriterCFStationCollection;
import ucar.nc2.time.CalendarDateRange;
import ucar.nc2.units.DateRange;

public class NetCDFWriterTest {

    private static final String PREFIX = "test-writer";
    private static final String OUT_PREFIX = "test-writer-out";
    private static final String SUFFIX = ".nc";
    private FeatureDataset dataset;
    private File outputFile;
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @After
    public void after() {
	// String location = dataset.getNetcdfFile().getLocation();
	// File file = new File(location);
	// file.delete();
    }

    @Test
    public void testH1() throws Exception {
	exception.expect(Exception.class);
	openAndTestType(new ExampleH1Writer(), FeatureType.POINT);
	testSubset("/home/boldrini/h1.nc", "/home/boldrini/sub-h1.nc");
    }

    @Test
    public void testH2() throws Exception {
	openAndTestType(new ExampleH2Writer(), FeatureType.STATION);
	testSubset("/home/boldrini/h2.nc", "/home/boldrini/sub-h2.nc");
    }

    @Test // some problems exist with incomplete multidimensional representation
    public void testH3() throws Exception {
	exception.expect(Exception.class);
	openAndTestType(new ExampleH3Writer(), FeatureType.STATION);
	testSubset("/home/boldrini/h3.nc", "/home/boldrini/sub-h3.nc");
    }

    @Test
    public void testH4() throws Exception {
	openAndTestType(new ExampleH4Writer(), FeatureType.STATION);
	testSubset("/home/boldrini/h4.nc", "/home/boldrini/sub-h4.nc");
    }

    @Test // some problems exist with precise lat representation
    public void testH5() throws Exception {
	exception.expect(Exception.class);
	openAndTestType(new ExampleH5Writer(), FeatureType.STATION);
	testSubset("/home/boldrini/h5.nc", "/home/boldrini/sub-h5.nc");
    }

    @Test
    public void testH6() throws Exception {
	openAndTestType(new ExampleH6Writer(), FeatureType.STATION);
	testSubset("/home/boldrini/h6.nc", "/home/boldrini/sub-h6.nc");
    }

    @Test
    public void testH7() throws Exception {
	openAndTestType(new ExampleH7Writer(), FeatureType.STATION);
	testSubset("/home/boldrini/h7.nc", "/home/boldrini/sub-h7.nc");
    }

    public void testSubset(String inputLocation, String location) throws Exception {

	boolean randomFile = true;
	File file1 = null;
	File file2 = null;
	if (randomFile) {
	    file1 = File.createTempFile(PREFIX, SUFFIX);
	    inputLocation = file1.getAbsolutePath();
	    file2 = File.createTempFile(OUT_PREFIX, SUFFIX);
	    location = file2.getAbsolutePath();
	}

	if (dataset instanceof FeatureDatasetPoint) {
	    FeatureDatasetPoint fdp = (FeatureDatasetPoint) dataset;

	    CFPointWriter.writeFeatureCollection(fdp, inputLocation, Version.netcdf3);

	    StationTimeSeriesFeatureCollection fc = (StationTimeSeriesFeatureCollection) fdp.getPointFeatureCollectionList().get(0);

	    // int total = 0;
	    Long start = null;
	    Long end = null;
	    ucar.nc2.ft.PointFeatureCollection pfc = fc.flatten(null, (CalendarDateRange) null); // LOOK
	    while (pfc.hasNext()) {
		PointFeature pf = pfc.next();
		StationPointFeature spf = (StationPointFeature) pf;
		long time = spf.getNominalTimeAsCalendarDate().getMillis();
		if (start == null || time < start) {
		    start = time;
		}
		if (end == null || time > end) {
		    end = time;
		}
		// System.out.println(total++ + " start " + start + " end " + end);
	    }
	    
	    DateRange dateRange = new DateRange(new Date(start), new Date(end - (end - start) / 2));
	    CalendarDateRange cdr = CalendarDateRange.of(dateRange);
	    // this.outputFile = File.createTempFile(OUT_PREFIX, SUFFIX);
	    subset(fdp, location, Version.netcdf3, dataset, fc, cdr);
	} else {
	    Assert.fail();
	}

	if (randomFile) {
	    file1.delete();
	    file2.delete();
	}

    }

    private int subset(FeatureDatasetPoint fdp, String fileOut, Version version, FeatureDataset dataset,
	    StationTimeSeriesFeatureCollection fc, CalendarDateRange dateRange) throws IOException {
	try (WriterCFStationCollection cfWriter = new WriterCFStationCollection(fileOut, dataset.getGlobalAttributes(),
		dataset.getDataVariables(),  fc.getTimeUnit(), fc.getAltUnits(), new CFPointWriterConfig(version))) {
	    ucar.nc2.ft.PointFeatureCollection pfc = fc.flatten(null, dateRange); // LOOK

	    int count = 0;
	    while (pfc.hasNext()) {
		PointFeature pf = pfc.next();
		StationPointFeature spf = (StationPointFeature) pf;
		if (count == 0)
		    cfWriter.writeHeader(fc.getStationFeatures(), spf);

		cfWriter.writeRecord(spf.getStation(), pf, pf.getFeatureData());
		count++;
	    }

	    cfWriter.finish();
	    return count;
	}
    }

    // @Test
    // public void testWrite() throws IOException {
    // NetcdfDataset dataset = NetcdfDataset.openDataset("/tmp/exampleh3.nc");
    // FeatureDatasetPoint featureDataset = (FeatureDatasetPoint) FeatureDatasetFactoryManager.wrap(FeatureType.STATION,
    // dataset, null,
    // null);
    // CFPointWriter.writeFeatureCollection(featureDataset, "/tmp/exampleh3-out.nc", Version.netcdf3);
    // }

    private void openAndTestType(NetCDFCFExampleWriter exampleWriter, FeatureType featureType) throws IOException {
	File tmpFile = File.createTempFile(PREFIX, SUFFIX);
	exampleWriter.write(tmpFile.getAbsolutePath());
	// NetcdfDataset dataset = NetcdfDataset.acquireDataset(tmpFile.getAbsolutePath(), null);
	// FeatureType featureType = FeatureDatasetFactoryManager.findFeatureType(dataset);
	this.dataset = FeatureDatasetFactoryManager.open(featureType, tmpFile.getAbsolutePath(), null, null);
	// System.out.println(ret.getClass().getSimpleName());

    }

}
