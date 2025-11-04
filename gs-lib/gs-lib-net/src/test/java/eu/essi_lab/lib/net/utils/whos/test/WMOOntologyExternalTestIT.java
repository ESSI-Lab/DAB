package eu.essi_lab.lib.net.utils.whos.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WMOOntology;
import eu.essi_lab.lib.net.utils.whos.WMOOntology.CommonWMOUnit;
import eu.essi_lab.lib.net.utils.whos.WMOUnit;

public class WMOOntologyExternalTestIT {
    WMOOntology wmo ;
    
    @Before
    public void before() throws Exception {
	wmo = new WMOOntology();
    }
    
    @Test
    public void testUnits() throws Exception {
	WMOUnit cms = wmo.getUnit(CommonWMOUnit.CUBIC_METRES_PER_SECOND.getUri());
	assertEquals(CommonWMOUnit.CUBIC_METRES_PER_SECOND.getUri(), cms.getURI());
    }
    
    @Test
    public void testVariables() throws Exception {
	SKOSConcept variable = wmo.getVariable("http://codes.wmo.int/wmdr/ObservedVariableTerrestrial/171");
	assertEquals("Stream discharge", variable.getPreferredLabel().getKey());
	assertTrue(variable.getDefinition().getKey().contains("Volume of water flowing through a stream"));
    }
    
    @Test
    public void testVariables2() throws Exception {
	SKOSConcept variable = wmo.getVariable("http://codes.wmo.int/wmdr/ObservedVariableAtmosphere/210");
	assertEquals("Amount of precipitation", variable.getPreferredLabel().getKey());

    }
    
    

}
