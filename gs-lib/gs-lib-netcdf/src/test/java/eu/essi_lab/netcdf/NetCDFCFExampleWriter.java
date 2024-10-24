package eu.essi_lab.netcdf;

import java.io.IOException;

/**
 * This class writes the NetCDF that show up in the NetCDF-CF conventions.
 * These can then be used as test references, putting them in the netcdf-examples folder.
 * 
 * @author boldrini
 */
public abstract class NetCDFCFExampleWriter {

    public NetCDFCFExampleWriter() {

    }

    public abstract void write(String location) throws IOException;

    // PointDatasetStandardFactory pdsf = new PointDatasetStandardFactory();
    // try {
    // FeatureDataset point = pdsf.open(FeatureType.STATION, NetcdfDataset.openDataset(""), null, null, null);
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }

}
