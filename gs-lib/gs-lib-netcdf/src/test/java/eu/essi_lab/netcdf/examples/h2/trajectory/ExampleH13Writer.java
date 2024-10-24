package eu.essi_lab.netcdf.examples.h2.trajectory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.essi_lab.netcdf.NetCDFCFExampleWriter;
import eu.essi_lab.netcdf.timeseries.NetCDFVariable;
import eu.essi_lab.netcdf.trajectory.H13SingleTrajectoryWriter;
import eu.essi_lab.netcdf.trajectory.SimpleTrajectory;
import ucar.ma2.DataType;

/**
 * H.4.2. Single trajectory
 * Example H.13. A single trajectory recording atmospheric composition.
 * 
 * @author boldrini
 */
public class ExampleH13Writer extends NetCDFCFExampleWriter {
    public static void main(String[] args) throws IOException {
	ExampleH13Writer ew = new ExampleH13Writer();
	ew.write("/tmp/exampleh13.nc");
	System.out.println("Wrote");
    }

    public void write(String location) throws IOException {

	H13SingleTrajectoryWriter writer = new H13SingleTrajectoryWriter(location);

	SimpleTrajectory trajectory = new SimpleTrajectory();
	trajectory.setIdentifier("trajectory_id");
	trajectory.setName("trajectory name");
	trajectory.setDescription("trajectory description");
	List<Long> times = new ArrayList<>();
	List<Double> lats = new ArrayList<>();
	List<Double> lons = new ArrayList<>();
	List<Double> alts = new ArrayList<>();
	List<Double> o3s = new ArrayList<>();
	List<Double> no3s = new ArrayList<>();

	int size = 42;
	double lat = 34;
	double lon = 23;
	double alt = 0;

	long ms = new Date().getTime();
	for (int i = 0; i < size; i++) {
	    times.add(ms);
	    ms = ms + 1000;
	    lats.add(lat);
	    lat = lat + (double) (0.04 * Math.random());
	    lons.add(lon);
	    lon = lon + (double) (0.06 * Math.random());
	    alts.add(alt);
	    alt = (double) (0.06 * Math.random());
	    o3s.add((double)(Math.random()*4));
	    no3s.add((double)(Math.random()*2));
	}
	NetCDFVariable<Long> timeVariable = new NetCDFVariable<>("time", times, "milliseconds since 1970-01-01 00:00:00", DataType.LONG);
	NetCDFVariable<Double> latVariable = new NetCDFVariable<>("lat", lats, "degrees_north", DataType.DOUBLE);
	NetCDFVariable<Double> lonVariable = new NetCDFVariable<>("lon", lons, "degrees_east", DataType.DOUBLE);
	NetCDFVariable<Double> altVariable = new NetCDFVariable<>("z", alts, "m", DataType.DOUBLE);
	NetCDFVariable<Double> o3= new NetCDFVariable<>("O3", o3s, "1e-9", DataType.DOUBLE);
	o3.addAttribute("long_name", "ozone concentration");
	NetCDFVariable<Double> no3= new NetCDFVariable<>("NO3", no3s, "1e-9", DataType.DOUBLE);
	no3.addAttribute("long_name", "NO3 concentration");
	NetCDFVariable[] variables = new NetCDFVariable[] {o3,no3};
	writer.write(trajectory, timeVariable, latVariable, lonVariable, altVariable, variables);
    }

}
