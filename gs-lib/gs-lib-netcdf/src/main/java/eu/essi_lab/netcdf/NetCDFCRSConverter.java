package eu.essi_lab.netcdf;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.netcdf.timeseries.NetCDFUtils;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * @author Fabrizio
 */
public class NetCDFCRSConverter {

    private Number trueLat1Val;
    private Number standLonVal;
    private NetcdfDataset netCDFdataset;

    /**
     * @throws IOException
     */
    public NetCDFCRSConverter(String fileName) throws IOException {

	//
	//
	//

	netCDFdataset = NetcdfDataset.openDataset(fileName);

	//
	//
	//

	Attribute TRUELAT1 = netCDFdataset.findGlobalAttribute("TRUELAT1");
	Attribute STAND_LON = netCDFdataset.findGlobalAttribute("STAND_LON");

	trueLat1Val = TRUELAT1.getNumericValue();
	standLonVal = STAND_LON.getNumericValue();
    }

    /**
     * @param ncPath
     * @param varName
     * @return
     * @throws Exception
     */
    public File convert(String ncPath, String varName) throws Exception {

	//
	// 1)
	//

	String command1 = "gdalwarp NETCDF:\"" + ncPath + "nc1.nc\":" + varName + " " + ncPath
		+ "nc2.nc -to SRC_METHOD=NO_GEOTRANSFORM -to DST_METHOD=NO_GEOTRANSFORM";

	// System.out.println(command1);

	executeWithRuntime(command1);

	//
	// 2)
	//

	String[] values = getCoordinates(netCDFdataset);

	String xmin = values[0];
	String ymin = values[1];
	String xmax = values[2];
	String ymax = values[3];

	//
	// 3)
	//

	String command3 = "gdal_translate " + ncPath + "nc2.nc " + ncPath + "nc3.nc -a_srs \"+proj=merc +datum=WGS84 +units=km +lat_ts="
		+ trueLat1Val + " +lon_0=" + standLonVal + "\" -a_ullr " + xmin + " " + ymax + " " + xmax + " " + ymin + "";

	executeWithRuntime(command3);

	//
	// 4)
	//

	String command4 = "gdalwarp " + ncPath + "nc3.nc " + ncPath + varName + ".nc -s_srs \"+proj=merc +datum=WGS84 +units=km +lat_ts="
		+ trueLat1Val + " +lon_0=" + standLonVal + "\" -t_srs EPSG:4326";

	executeWithRuntime(command4);

	//
	//
	//

	new File(ncPath + "nc2.nc").delete();
	new File(ncPath + "nc3.nc").delete();

	//
	//
	//

	return new File(ncPath + varName + ".nc");
    }

    /**
     * @return
     */
    public List<String> get2dVariableNames() {

	List<Variable> variables = NetCDFUtils.getGeographicVariables(netCDFdataset);

	List<String> list = variables.stream().map(v -> v.getName()).collect(Collectors.toList());

	// List<Variable> variables = netCDFdataset.getVariables();
	//
	// //
	// //
	// //
	//
	// for (Variable v : variables) {
	//
	// List<Dimension> dimensions = v.getDimensions();
	//
	// boolean anyMatch = dimensions.stream()
	// .anyMatch(d -> d.getFullName().equals("south_north") || d.getFullName().equals("west_east"));
	//
	// if (anyMatch && dimensions.size() > 1) {
	//
	// String varName = v.getFullName();
	//
	// list.add(varName);
	// }
	// }

	return list;
    }

    /**
     * @throws IOException
     */
    public void close() throws IOException {

	netCDFdataset.close();
    }

    /**
     * @param netCDFdataset
     * @return
     */
    private String[] getCoordinates(NetcdfDataset netCDFdataset) {

	Double minx = null;
	Double maxx = null;
	Double miny = null;
	Double maxy = null;

	b1: for (CoordinateSystem system : netCDFdataset.getCoordinateSystems()) {

	    List<CoordinateAxis> axes = system.getCoordinateAxes();

	    for (CoordinateAxis axe : axes) {
		double min = axe.getMinValue();
		double max = axe.getMaxValue();
		if (axe.getAxisType().equals(AxisType.GeoX)) {
		    minx = min;
		    maxx = max;
		}
		if (axe.getAxisType().equals(AxisType.GeoY)) {
		    miny = min;
		    maxy = max;
		}
		if (minx != null & miny != null & maxx != null && maxy != null) {
		    break b1;
		}
	    }
	}

	return new String[] { String.valueOf(minx), String.valueOf(miny), String.valueOf(maxx), String.valueOf(maxy) };
    }

    /**
     * @param inputPath
     * @param outputPath
     * @param vector
     * @throws Exception
     */
    private void executeWithRuntime(String command) throws Exception {

	Runtime rt = Runtime.getRuntime();

	Process ps = rt.exec(command);

	int exitVal = ps.waitFor();

	if (exitVal > 0) {

	    GSLoggerFactory.getLogger(NetCDFCRSConverter.class).error(IOStreamUtils.asUTF8String(ps.getErrorStream()));
	}
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

	//
	//
	//

	String ncPath = "D:\\Desktop\\nc\\";

	NetCDFCRSConverter converter = new NetCDFCRSConverter(ncPath + "nc1.nc");

	//
	//
	//

	List<String> variableNames = converter.get2dVariableNames();

	for (String varName : variableNames) {

	    converter.convert(ncPath, varName);
	}

	converter.close();

	System.exit(0);
    }
}
