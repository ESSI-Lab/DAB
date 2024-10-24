package eu.essi_lab.netcdf.examples.h2.timeseries;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.netcdf.timeseries.H4SingleTimeSeriesWriter;
import eu.essi_lab.netcdf.timeseries.NetCDFVariable;
import eu.essi_lab.netcdf.timeseries.SimpleStation;
import ucar.ma2.DataType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.nc2.ft.point.StationFeature;
import ucar.nc2.ft.point.StationPointFeature;
import ucar.nc2.time.CalendarDateRange;

public class H4SingleTimeSeriesWriterTest {

    private H4SingleTimeSeriesWriter writer;
    private File tmp;

    @Before
    public void init() throws IOException {
	this.tmp = File.createTempFile("test", ".nc");
	this.writer = new H4SingleTimeSeriesWriter(tmp.getAbsolutePath());
    }

    @Test
    public void testName() throws Exception {

	String timeUnits = "days since 1970-01-01 00:00:00";

	SimpleStation station = new SimpleStation();
	station.setLatitude(32.);
	station.setLongitude(45.);
	station.setAltitude(34.);
	String name = "My station Luketići";
//	name = eu.essi_lab.lib.utils.StringUtils.removeAccents(name);
	station.setName(name);

	List<Long> timeValues = new ArrayList<>();
	timeValues.add(1l);
	timeValues.add(2l);
	timeValues.add(3l);
	NetCDFVariable<Long> timeVariable = new NetCDFVariable<Long>("time", timeValues, timeUnits, DataType.LONG);

	List<Double> values = new ArrayList<>();
	values.add(1.);
	values.add(1.4);
	values.add(1.2);
	NetCDFVariable<Double> vrb = new NetCDFVariable<Double>("temp", values, "Celsius", DataType.DOUBLE);
	vrb.setStandardName("air_temperature");
	vrb.setMissingValue(-999.9);

	this.writer.write(station, timeVariable, vrb);

	FeatureDataset dataset = FeatureDatasetFactoryManager.open(FeatureType.STATION, tmp.getAbsolutePath(), null, null);

	FeatureDatasetPoint fdp = (FeatureDatasetPoint) dataset;

	StationTimeSeriesFeatureCollection fc = (StationTimeSeriesFeatureCollection) fdp.getPointFeatureCollectionList().get(0);
	ucar.nc2.ft.PointFeatureCollection pfc = fc.flatten(null, (CalendarDateRange) null); // LOOK
	if (pfc.hasNext()) {
	    PointFeature pf = pfc.next();
	    StationPointFeature spf = (StationPointFeature) pf;
	    StationFeature s = spf.getStation();
	    String stationName = s.getName(); //
	    System.out.println(stationName);
	    assertEquals(name, stationName);
	}


    }


    @After
    public void finish() {
	tmp.delete();
    }
}
