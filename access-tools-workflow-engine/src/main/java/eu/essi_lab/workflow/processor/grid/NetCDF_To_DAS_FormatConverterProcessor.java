package eu.essi_lab.workflow.processor.grid;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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
import java.io.PrintWriter;
import java.util.List;

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.TargetHandler;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class NetCDF_To_DAS_FormatConverterProcessor extends DataProcessor {

    private static Logger logger = GSLoggerFactory.getLogger(NetCDF_To_DAS_FormatConverterProcessor.class);

    @Override
    public DataObject process(DataObject inputData, TargetHandler handler) throws Exception {
	File inputFile = inputData.getFile();
	File outputFile = File.createTempFile(NetCDF_To_DAS_FormatConverterProcessor.class.getSimpleName(), ".dds");
	outputFile.deleteOnExit();
	DataObject outputData = new DataObject();

	NetcdfDataset dataset = NetcdfDataset.openDataset(inputFile.getAbsolutePath());

	String das = "Attributes {\n";

	List<Variable> variables = dataset.getVariables();

	for (Variable variable : variables) {
	    das += "    " + variable.getShortName() + " {\n";
	    das += getAttributes(variable.getAttributes());
	    das += "    " + "}\n";
	}
	das += "    NC_GLOBAL {\n";
	das += getAttributes(dataset.getGlobalAttributes());
	das += "    " + "}\n";

	DataDescriptor inputDescriptor = inputData.getDataDescriptor();

	PrintWriter out = new PrintWriter(outputFile);
	das += "}";
	out.print(das);
	out.close();

	outputData.setFile(outputFile);
	DataDescriptor outputDescriptor = inputDescriptor.clone();
	outputDescriptor.setDataFormat(DataFormat.DDS());
	outputData.setDataDescriptor(outputDescriptor);
	return outputData;
    }

    private String getAttributes(List<Attribute> attributes) {
	String ret = "";
	for (Attribute attribute : attributes) {
	    String bt = OPeNDAPUtils.getBaseType(attribute.getDataType());
	    if (bt != null) {
		String val = attribute.getValue(0).toString();
		val = val.replace("\"", "'").replace("\n", " ");
		String attrName = attribute.getShortName();
		if (!attrName.startsWith("_")) {
		    if (bt.equals("String")) {
			val = "\"" + val + "\"";
		    }
		    ret += "        " + bt + " " + attrName + " " + val + ";\n";

		}

	    }
	}
	return ret;
    }

    public boolean scaleOutput() {
	return false;
    }

    /**
     * Sub classes may implement this method to correct possible problems in the output from GDAL. The source data is
     * also passed.
     * 
     * @param ret
     * @throws GSException
     */
    public DataObject postProcessCorrections(DataObject input, DataObject output) throws GSException {
	return output;
    }

}
