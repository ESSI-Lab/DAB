package eu.essi_lab.cfga.option.test;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.GSSourcePattern;
import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class InputPatternTest {

    /**
     * 
     */
    @Test
    public void alphaNumericPatternTest() {

	String text = "";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC));

	Assert.assertFalse(required(text, InputPattern.ALPHANUMERIC));

	text = " ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC));

	text = " a";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC));

	text = " a ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC));

	text = "a ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC));

	text = "    ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC));

	text = "_!%&/__";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC));

	text = "*";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC));

	text = "_";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC));

	text = "a";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC));

	text = "a12";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC));

	text = "12a12";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC));

	text = "12adas12";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC));

	text = "12adas12das";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC));

	text = "12adas12dasASD";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC));

	text = "XX12adas12dasASD";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC));
    }

    /**
     * 
     */
    @Test
    public void gsSourceIdPatternTest() {

	String text = "";
	Assert.assertTrue(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	Assert.assertFalse(required(text, GSSourcePattern.GS_SOURCE_ID));

	text = " ";
	Assert.assertFalse(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = " a";
	Assert.assertFalse(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = " a ";
	Assert.assertFalse(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = " a_ ";
	Assert.assertFalse(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "_ a_ ";
	Assert.assertFalse(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "a ";
	Assert.assertFalse(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "    ";
	Assert.assertFalse(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "!%&/";
	Assert.assertFalse(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "*";
	Assert.assertFalse(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "a";
	Assert.assertTrue(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "a_";
	Assert.assertTrue(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "____";
	Assert.assertTrue(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "___a___";
	Assert.assertTrue(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "_a_12_324_sdF_fsSDFDSF_____";
	Assert.assertTrue(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "a12";
	Assert.assertTrue(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "12a12";
	Assert.assertTrue(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "12adas12";
	Assert.assertTrue(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "___12adas12das____";
	Assert.assertTrue(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "12adas12__dasASD";
	Assert.assertTrue(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "XX12adas12dasASD";
	Assert.assertTrue(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "XX12ada_s1_____2dasASD";
	Assert.assertTrue(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	//
	//
	//

	text = "----___12adas12das____---";
	Assert.assertTrue(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "12ada---s12__dasA---SD";
	Assert.assertTrue(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "-XX1-2adas-12dasA-SD-";
	Assert.assertTrue(notRequired(text, GSSourcePattern.GS_SOURCE_ID));

	text = "-";
	Assert.assertFalse(notRequired(text, GSSourcePattern.GS_SOURCE_ID));
    }

    /**
     * 
     */
    @Test
    public void alphaNumericAndUnderscorePatternTest() {

	String text = "";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	Assert.assertFalse(required(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = " ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = " a";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = " a ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = " a_ ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = "_ a_ ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = "a ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = "    ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = "!%&/";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = "*";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = "a";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = "a_";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = "____";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = "___a___";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = "_a_12_324_sdF_fsSDFDSF_____";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = "a12";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = "12a12";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = "12adas12";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = "___12adas12das____";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = "12adas12__dasASD";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = "XX12adas12dasASD";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));

	text = "XX12ada_s1_____2dasASD";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE));
    }

    /**
     * 
     */
    @Test
    public void alphaNumericAndSpacePatternTest() {

	String text = "";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	Assert.assertFalse(required(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = " ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = " a";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = " a ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = "a ";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = "    ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = "_!%&/__";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = "*";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = "_";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = "a";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = "a ";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = "a12";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = "a12 ";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = "a12 a12 a12 ";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = " 12a12";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = "12adas12 ";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = "1 2 a da s 1 2 ";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = " 12adas12das";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = "12adas12dasASD  ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));

	text = "XX12adas12dasASD  ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_SPACE));
    }

    /**
     * 
     */
    @Test
    public void alphaNumericAndSpaceAndUnderscorePatternTest() {

	String text = "";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	Assert.assertFalse(required(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = " ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = " a";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = " a ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "a ";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "    ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "_!%&/__";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "*";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "_";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "a";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "_a_";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "_a_ ";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "_ a_ ";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "_  a_ ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "_ a_  ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "a ";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "a _ 123 _______ 234 ____44";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "a12";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "a12 ";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "a12 a12 a12 ";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = " 12a12";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "12adas12 ";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = " __12adas12 ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = " __12adas12 _  ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "1 2 a da s 1 2 ";
	Assert.assertTrue(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = " 12adas12das";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "12adas12dasASD  ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));

	text = "XX12adas12dasASD  ";
	Assert.assertFalse(notRequired(text, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE));
    }

    @Test
    public void extensionTest() {
	{

	    InputPattern inputPattern = GSSourcePattern.fromName("alphaNum");
	    
	    GSLoggerFactory.getLogger(getClass()).debug("Input pattern found: "+inputPattern.getPattern());

	    Assert.assertEquals(InputPattern.ALPHANUMERIC, inputPattern);

	    InputPattern extInputPattern = GSSourcePattern.fromName("gsSourceId");
	    
	    GSLoggerFactory.getLogger(getClass()).debug("Input pattern found: "+extInputPattern.getPattern());

	    Assert.assertEquals(GSSourcePattern.GS_SOURCE_ID, extInputPattern);
	}

	//
	//
	//

	{

	    Option<String> option = StringOptionBuilder.get().//

		    withKey("test").//

		    withInputPattern(GSSourcePattern.GS_SOURCE_ID).//

		    build();

	    extensionTest(option);
	    extensionTest(new Option<String>(option.getObject()));
	    extensionTest(new Option<String>(option.getObject().toString()));
	}
    }

    /**
     * @param option
     */
    private void extensionTest(Option<String> option) {

	Optional<InputPattern> extInputPattern = option.getInputPattern();

	Assert.assertEquals(GSSourcePattern.GS_SOURCE_ID, extInputPattern.get());
    }

    /**
     * @param text
     * @param inputPattern
     * @return
     */
    private boolean required(String text, InputPattern inputPattern) {

	return matches(text, true, inputPattern);
    }

    /**
     * @param text
     * @param inputPattern
     * @return
     */
    private boolean notRequired(String text, InputPattern inputPattern) {

	return matches(text, false, inputPattern);
    }

    /**
     * @param text
     * @param required
     * @param inputPattern
     * @return
     */
    private boolean matches(String text, boolean required, InputPattern inputPattern) {

	Pattern pattern = Pattern.compile(required ? inputPattern.getRequiredPattern() : inputPattern.getPattern());
	Matcher matcher = pattern.matcher(text);

	return matcher.matches();
    }
}
