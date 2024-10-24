package eu.essi_lab.lib.odip;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.lib.odip.rosetta.RosettaStone;

public abstract class RosettaStoneTest {

    private static final String NERC_PRACTICAL_SALINITY = "http://vocab.nerc.ac.uk/collection/P01/current/PSLTZZ01/";
    private static final String NERC_SALINITY = "http://vocab.nerc.ac.uk/collection/P02/current/PSAL/";
    private static final String NODC_SALINITY = "https://www.nodc.noaa.gov/cgi-bin/OAS/prd/datatype/details/307";
    //
    private static final String NERC_WATER_BODY_TEMPERATURE = "http://vocab.nerc.ac.uk/collection/P01/current/TEMPPR01/";
    private static final String NERC_WATER_TEMPERATURE = "http://vocab.nerc.ac.uk/collection/P02/current/TEMP/";
    private static final String NODC_WATER_TEMPERATURE = "https://www.nodc.noaa.gov/cgi-bin/OAS/prd/datatype/details/373";
    //
    private static final String NERC_SEA_LEVEL = "http://vocab.nerc.ac.uk/collection/P02/current/ASLV/";
    private static final String AODN_SEA_LEVEL = "http://vocab.aodn.org.au/def/discovery_parameter/entity/643";
    private static final String NODC_SEA_LEVEL = "https://www.nodc.noaa.gov/cgi-bin/OAS/prd/datatype/details/312";
    //
    private static final String NERC_SALINOMETER = "http://vocab.nerc.ac.uk/collection/L05/current/LAB30/";
    private static final String NODC_SALINOMETER = "https://www.nodc.noaa.gov/cgi-bin/OAS/prd/insttype/details/75";
    //
    private static final String NERC_WATER_PRESSURE_SENSOR = "http://vocab.nerc.ac.uk/collection/L05/current/WPS/";
    private static final String NODC_PRESSURE_SENSORS = "https://www.nodc.noaa.gov/cgi-bin/OAS/prd/insttype/details/207";
    //
    private static final String SDN_ALASKA = "http://www.seadatanet.org/urnurl/SDN:EDMO::3167/";
    private static final String NODC_ALASKA = "https://www.nodc.noaa.gov/cgi-bin/OAS/prd/institution/details/972";
    private static final String NERC_ALASKA = "http://vocab.nerc.ac.uk/collection/C75/current/UAA/";
    private RosettaStone connector;

    @Before
    public void init() {	
	this.connector = getConnector();
    }

    public abstract RosettaStone getConnector();

    // // PARAMETERS
    @Test
    public void testPSAL() {

	System.out.println("Testing PSAL Translation");

	testTranslation(NERC_SALINITY, connector.translateNERCtoSDN(NERC_SALINITY), NODC_SALINITY);

    }

    @Test
    public void testPSALBroader() {

	System.out.println("Testing PSAL Broader");

	testBroader(new String[] { NERC_PRACTICAL_SALINITY, connector.translateNERCtoSDN(NERC_PRACTICAL_SALINITY) }, //
		new String[] { NERC_SALINITY, connector.translateNERCtoSDN(NERC_SALINITY) });

    }

    @Test
    public void testPSALNarrower() {

	System.out.println("Testing PSAL Narrower");

	testNarrower(new String[] { NERC_SALINITY, connector.translateNERCtoSDN(NERC_SALINITY) }, //
		new String[] { NERC_PRACTICAL_SALINITY, connector.translateNERCtoSDN(NERC_PRACTICAL_SALINITY) });

    }

    @Test
    public void testTEMP() {

	System.out.println("Testing TEMP Translation");

	testTranslation(NERC_WATER_TEMPERATURE, connector.translateNERCtoSDN(NERC_WATER_TEMPERATURE), NODC_WATER_TEMPERATURE);

    }

    @Test
    public void testTEMPBroader() {

	System.out.println("Testing TEMP Broader");

	testBroader(new String[] { NERC_WATER_BODY_TEMPERATURE, connector.translateNERCtoSDN(NERC_WATER_BODY_TEMPERATURE) }, //
		new String[] { NERC_WATER_TEMPERATURE, connector.translateNERCtoSDN(NERC_WATER_TEMPERATURE) });

    }

    @Test
    public void testTEMPNarrower() {

	System.out.println("Testing TEMP Narrower");

	testNarrower(new String[] { NERC_WATER_TEMPERATURE, connector.translateNERCtoSDN(NERC_WATER_TEMPERATURE) }, //
		new String[] { NERC_WATER_BODY_TEMPERATURE, connector.translateNERCtoSDN(NERC_WATER_BODY_TEMPERATURE) });

    }

    @Test
    public void testASLV() {

	System.out.println("Testing ASLV Translation");

	testTranslation(NERC_SEA_LEVEL, connector.translateNERCtoSDN(NERC_SEA_LEVEL), NODC_SEA_LEVEL, AODN_SEA_LEVEL);

    }

    @Test
    public void testSalinometers() {

	System.out.println("Testing Salinometers Translation");

	testTranslation(NERC_SALINOMETER, connector.translateNERCtoSDN(NERC_SALINOMETER), NODC_SALINOMETER);

    }

    @Test
    public void testPressureSensorsBroader() {

	System.out.println("Testing Pressure Sensors Broader");

	testBroader(new String[] { NERC_WATER_PRESSURE_SENSOR, connector.translateNERCtoSDN(NERC_WATER_PRESSURE_SENSOR) }, //
		new String[] { NODC_PRESSURE_SENSORS });

    }

    @Test
    public void testPressureSensorsNarrower() {

	System.out.println("Testing Pressure Sensors Narrower");

	testNarrower(new String[] { NODC_PRESSURE_SENSORS }, //
		new String[] { NERC_WATER_PRESSURE_SENSOR, connector.translateNERCtoSDN(NERC_WATER_PRESSURE_SENSOR) });

    }

    @Ignore("The mapping BODC organisations to EDMO is not ready yet. We are waiting input from ODIP partners for this to work. Reference: email of Alexandra on 2017-09-22.")
    @Test
    public void testAlaska() {

	System.out.println("Testing Alaska Translation");

	testTranslation(NERC_ALASKA, SDN_ALASKA, NODC_ALASKA);

    }

    private void testTranslation(String... synonyms) {
	TreeSet<String> set = new TreeSet<>();
	set.addAll(Arrays.asList(synonyms));
	for (String term : set) {
	    Set<String> translations = connector.getTranslations(term);
	    for (String synonym : synonyms) {
		if (synonym.equals(term)) {
		    continue;
		}
		boolean found = translations.contains(synonym);
		if (!found) {
		    printErrorMessage(term, synonym, translations);
		}
		Assert.assertTrue(found);
	    }
	}
    }

    private void printErrorMessage(String term, String expected, Set<String> obtained) {
	System.out.println("Missing translation of term '" + term + "'");
	System.out.println("Expected '" + expected + "'");
	System.out.println("Translation set:");
	if (obtained.isEmpty()) {
	    System.out.println("EMPTY!");
	}
	for (String translation : obtained) {
	    System.out.println("'" + translation + "'");
	}
	System.out.println();
    }

    private void testNarrower(String[] inputs, String[] outputs) {
	for (String input : inputs) {
	    Set<String> narrowerTerms = connector.getNarrower(input);
	    for (String output : outputs) {
		boolean found = narrowerTerms.contains(output);
		if (!found) {
		    printErrorMessage(input, output, narrowerTerms);
		}
		Assert.assertTrue(found);
	    }
	}
    }

    private void testBroader(String[] inputs, String[] outputs) {
	for (String input : inputs) {
	    Set<String> broaderTerms = connector.getBroader(input);
	    for (String output : outputs) {
		boolean found = broaderTerms.contains(output);
		if (!found) {
		    printErrorMessage(input, output, broaderTerms);
		}
		Assert.assertTrue(found);
	    }
	}
    }

    // INSTRUMENTS

    // ORIGINATORS

    // PLATFORMS
}
