package eu.essi_lab.workflow.processor.grid;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.io.PrintWriter;
import java.util.List;

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.netcdf.timeseries.NetCDFUtils;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.TargetHandler;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class NetCDF_To_DDS_FormatConverterProcessor extends DataProcessor {

    private static Logger logger = GSLoggerFactory.getLogger(NetCDF_To_DDS_FormatConverterProcessor.class);

    @Override
    public DataObject process(DataObject inputData, TargetHandler handler) throws Exception {
	File inputFile = inputData.getFile();
	File outputFile = File.createTempFile(NetCDF_To_DDS_FormatConverterProcessor.class.getSimpleName(), ".dds");
	outputFile.deleteOnExit();
	DataObject outputData = new DataObject();

	NetcdfDataset dataset = NetcdfDataset.openDataset(inputFile.getAbsolutePath());

	String dds = "Dataset {\n";

	List<Variable> variables = dataset.getVariables();
	List<Variable> geoVariables = NetCDFUtils.getGeographicVariables(dataset);

	vars: for (Variable variable : variables) {
	    for (Variable geoVariable : geoVariables) {
		if (variable.getShortName().equals(geoVariable.getShortName())) {
		    continue vars;
		}
	    }
	    String bt = OPeNDAPUtils.getBaseType(variable.getDataType());
	    if (bt != null) {
		dds += "    " + bt + " " + variable.getShortName() + "[" + variable.getShortName() + " = " + variable.getSize() + "];\n";
	    }
	}

	for (Variable geoVariable : geoVariables) {
	    String bt = OPeNDAPUtils.getBaseType(geoVariable.getDataType());
	    if (bt != null) {
		dds += "    Grid {\n";
		dds += "     ARRAY:\n";
		String dimensions = "";
		List<Dimension> dims = geoVariable.getDimensions();
		for (Dimension dim : dims) {
		    dimensions += "[" + dim.getShortName() + " = " + dim.getLength() + "]";
		}
		dds += "        " + bt + " " + geoVariable.getShortName() + dimensions + ";\n";
		dds += "     MAPS:\n";
		for (Dimension dim : dims) {
		    Variable dimVar = dataset.findVariable(null, dim.getShortName());
		    String baseType = OPeNDAPUtils.getBaseType(dimVar.getDataType());
		    if (baseType != null) {
			dds += "        " + baseType + " " + dim.getShortName() + "[" + dim.getShortName() + " = " + dim.getLength()
				+ "];\n";
		    }
		}
		dds += "    } " + geoVariable.getShortName() + ";\n";
	    }
	}

	DataDescriptor inputDescriptor = inputData.getDataDescriptor();

	PrintWriter out = new PrintWriter(outputFile);
	dds += "} ;";
	out.print(dds);
	out.close();

	outputData.setFile(outputFile);
	DataDescriptor outputDescriptor = inputDescriptor.clone();
	outputDescriptor.setDataFormat(DataFormat.DDS());
	outputData.setDataDescriptor(outputDescriptor);
	return outputData;
    }

    public boolean scaleOutput() {
	return false;
    }
    public DataObject postProcessCorrections(DataObject input, DataObject output) throws GSException {
	return output;
    }

}
