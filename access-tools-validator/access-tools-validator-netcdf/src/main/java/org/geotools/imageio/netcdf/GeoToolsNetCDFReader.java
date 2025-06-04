package org.geotools.imageio.netcdf;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.coverage.io.netcdf.NetCDFReader;
import org.geotools.coverage.io.netcdf.crs.NetCDFProjection;
import org.geotools.referencing.CRS;
import org.geotools.util.factory.Hints;

import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.NetcdfFileWriter.Version;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.CF;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;

public class GeoToolsNetCDFReader {

    static {
	try {	    
	    // WORKAROUND for a GeoTools bug on line 361 of NetCDFProjection!
	    // was: sp.put(CF.MERCATOR + "_1SP", NetCDFProjection.MERCATOR_2SP);
	    Field field = NetCDFProjection.class.getDeclaredField("supportedProjections");
	    field.setAccessible(true);	    
	    Object supportedProjections = field.get(null);
	    if (supportedProjections instanceof Map<?, ?>) {
		Map<String, NetCDFProjection> sp = (Map<String, NetCDFProjection>) supportedProjections;
		sp.put(CF.MERCATOR + "_2SP", NetCDFProjection.MERCATOR_2SP);		
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public static org.geotools.api.referencing.crs.CoordinateReferenceSystem extractCRS(NetcdfDataset dataset, Variable variable) throws Exception {
	File tmpFile = File.createTempFile(GeoToolsNetCDFReader.class.getSimpleName(), ".nc");
	tmpFile.deleteOnExit();
	NetcdfFileWriter writer = NetcdfFileWriter.createNew(Version.netcdf4, tmpFile.getAbsolutePath());

	List<CoordinateAxis> axes = dataset.getCoordinateAxes();
	Variable xAxis = null;
	Variable yAxis = null;
	Variable lonAxis = null;
	Variable latAxis = null;
	for (CoordinateAxis axe : axes) {
	    AxisType type = axe.getAxisType();
	    if (type != null) {
		switch (type) {
		case GeoX:
		    xAxis = axe.getOriginalVariable();
		    break;
		case Lon:
		    xAxis = axe.getOriginalVariable();
		    lonAxis = axe.getOriginalVariable();
		    break;
		case GeoY:
		    yAxis = axe.getOriginalVariable();
		    break;
		case Lat:
		    yAxis = axe.getOriginalVariable();
		    latAxis = axe.getOriginalVariable();
		default:
		    break;
		}
	    }
	}

	if (latAxis!=null && lonAxis!=null) {
	    // likely EPSG:4326
	    return CRS.decode("EPSG:4326", true);
	}
	if (xAxis == null || yAxis == null) {
	    return null;
	}

	writer.addDimension(null, xAxis.getShortName(), (int) xAxis.getSize());
	writer.addDimension(null, yAxis.getShortName(), (int) yAxis.getSize());

	Variable xVariable = writer.addVariable(null, xAxis.getShortName(), xAxis.getDataType(), xAxis.getDimensionsString());
	Variable yVariable = writer.addVariable(null, yAxis.getShortName(), yAxis.getDataType(), yAxis.getDimensionsString());

	Variable mainVariable = writer.addVariable(null, variable.getShortName(), variable.getDataType(),
		yAxis.getShortName() + " " + xAxis.getShortName());
	for (Attribute att : variable.getAttributes()) {
	    mainVariable.addAttribute(att);
	}
	HashMap<String, Variable> oldVariables = new HashMap<>();
	HashMap<String, Variable> newVariables = new HashMap<>();

	for (Variable oldVariable : dataset.getVariables()) {
	    // to copy the grid mapping variable
	    if (oldVariable.getDimensions() == null || oldVariable.getDimensions().isEmpty()) {
		oldVariables.put(oldVariable.getShortName(), oldVariable);
		Variable newVariable = writer.addVariable(null, oldVariable.getShortName(), oldVariable.getDataType(),
			oldVariable.getDimensions());
		for (Attribute att : oldVariable.getAttributes()) {
		    newVariable.addAttribute(att);
		}
		newVariables.put(newVariable.getShortName(), newVariable);
	    }
	}

	writer.create();

	writer.write(xVariable, xAxis.read());
	writer.write(yVariable, yAxis.read());

	for (String variableName : newVariables.keySet()) {
	    Variable oldVariable = oldVariables.get(variableName);
	    Variable newVariable = newVariables.get(variableName);
	    writer.write(newVariable, oldVariable.read());

	}

	int[] origin = variable.getShape();
	int[] shape = variable.getShape();
	for (int i = 0; i < origin.length; i++) {
	    origin[i] = 0;
	    if (i < origin.length - 2) {
		shape[i] = 1;
	    }
	}
	Array array = variable.read(origin, shape);
	array = array.section(origin, shape);
	writer.write(mainVariable, array);
	writer.close();

	NetcdfDataset newDataset = NetcdfDataset.openDataset(tmpFile.getAbsolutePath());

	mainVariable = newDataset.findVariable(mainVariable.getShortName());
	
	NetCDFReader netCDFReader = new NetCDFReader(tmpFile.toURI().toURL(), null);

	org.geotools.api.referencing.crs.CoordinateReferenceSystem ret = netCDFReader.getCoordinateReferenceSystem();
	
	netCDFReader.dispose();

	newDataset.close();
	tmpFile.delete();
	return ret;

    }

}
