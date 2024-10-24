package eu.essi_lab.netcdf;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.StructureDS;

public class OpendapExternalTestIT {

    @Test
    public void test() throws IOException {

	List<Variable> variables = findVariables("https://prod-erddap.emodnet-physics.eu/nmdis//erddap/tabledap/EMODPACE_NMDIS_CST");
	for (Variable variable : variables) {
	    List<Attribute> attrs = variable.getAttributes();
	    for (Attribute attr : attrs) {
		System.out.println(attr.getShortName() + " " + attr.getValue(0));
	    }
	    System.out.println();
	}
	assertTrue(variables.size()>3);
    }

    private List<Variable> findVariables(String url) throws IOException {
	List<Variable> ret = new ArrayList<>();
	NetcdfDataset d = NetcdfDataset.openDataset(url);
	List<Variable> variables = d.getVariables();
	for (Variable variable : variables) {
	    if (variable instanceof StructureDS) {
		StructureDS s = (StructureDS) variable;
		List<Variable> svars = s.getVariables();

		for (Variable svar : svars) {
		    Attribute axis = svar.findAttribute("axis");
		    if (axis != null) {
			continue;
		    }
		    Attribute sn = svar.findAttribute("standard_name");
		    if (sn == null) {
			continue;
		    }
		    ret.add(svar);
		}

	    }

	}
	d.close();
	return ret;
    }

}
