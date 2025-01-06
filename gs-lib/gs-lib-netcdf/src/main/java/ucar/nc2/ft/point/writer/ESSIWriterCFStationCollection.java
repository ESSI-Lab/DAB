package ucar.nc2.ft.point.writer;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ucar.ma2.StructureData;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Structure;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.ft.point.StationFeature;
import ucar.nc2.ft.point.StationPointFeature;
import ucar.nc2.units.DateUnit;

/**
 * Extends the WriterCFStationCollection from Unidata to fix a bug: the coordinates are always fixed, instead we want to
 * modify them
 * 
 * @author boldrini
 */
public class ESSIWriterCFStationCollection extends WriterCFStationCollection {

    List<String> coords = new ArrayList<>();

    public ESSIWriterCFStationCollection(String fileOut, List<Attribute> atts, List<VariableSimpleIF> dataVars, List<Variable> extra,
	    DateUnit timeUnit, String altUnits, CFPointWriterConfig config) throws IOException {
	super(fileOut, atts, dataVars, extra, timeUnit, altUnits, config);
    }

    protected void addDataVariablesExtended(StructureData obsData, String coordVars) throws IOException {
	coordVars = getCoordinates(coordVars);
	super.addDataVariablesExtended(obsData, coordVars);
    }

    protected void addDataVariablesClassic(Dimension recordDim, StructureData stnData, Map<String, Variable> varMap, String coordVars)
	    throws IOException {
	coordVars = getCoordinates(coordVars);
	super.addDataVariablesClassic(recordDim, stnData, varMap, coordVars);
    }

    private String getCoordinates(String coordVars) {
	for (String coord : coords) {
	    coordVars += " " + coord;
	}
	return coordVars;
    }

    public void addCoordinate(String coord) {
	coords.add(coord);
    }

    protected void addCoordinatesClassic(Dimension recordDim, List<VariableSimpleIF> coords, Map<String, Variable> varMap)
	    throws IOException {
	for (VariableSimpleIF coord : coords) {
	    tweakCoord(coord);
	}
	super.addCoordinatesClassic(recordDim, coords, varMap);

    }

    // added as members of the given structure
    protected void addCoordinatesExtended(Structure parent, List<VariableSimpleIF> coords) throws IOException {
	for (VariableSimpleIF coord : coords) {
	    tweakCoord(coord);
	}
	super.addCoordinatesExtended(parent, coords);
    }

    private List<Attribute> timeAttribues = new ArrayList<>();
    private boolean wroteHeader = false;

    public boolean isWroteHeader() {
	return wroteHeader;
    }

    public List<Attribute> getTimeAttribues() {
	return timeAttribues;
    }

    private void tweakCoord(VariableSimpleIF coord) {
	if (coord.getShortName().equals(timeName)) {
	    for (Attribute attr : timeAttribues) {
		coord.getAttributes().add(attr);
	    }
	}

    }

    @Override
    public void writeHeader(List<StationFeature> stns, StationPointFeature spf) throws IOException {
	super.writeHeader(stns, spf);
	wroteHeader = true;
    }

    public NetcdfFile getNetCDF() {
	return  writer.getNetcdfFile();
	
    }

}
