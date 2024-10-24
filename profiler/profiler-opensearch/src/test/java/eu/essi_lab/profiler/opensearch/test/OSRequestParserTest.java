package eu.essi_lab.profiler.opensearch.test;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.profiler.os.OSParameters;
import eu.essi_lab.profiler.os.OSRequestParser;

public class OSRequestParserTest {

    private KeyValueParser parser;
    private OSRequestParser reader;

    @Before
    public void init() {

	parser = Mockito.mock(KeyValueParser.class);
	reader = new OSRequestParser(parser);
    }

    @Test
    public void testNonIntegerStartIndex() {

	Mockito.when(parser.getValue(OSParameters.START_INDEX.getName())).thenReturn("a");
	try {
	    reader.parse(OSParameters.START_INDEX);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	}
    }

    @Test
    public void testDefaultStartIndex() {

	Mockito.when(parser.getValue(OSParameters.START_INDEX.getName())).thenReturn(null);
	try {
	    String startIndex = reader.parse(OSParameters.START_INDEX);
	    Assert.assertEquals("1", startIndex);

	} catch (IllegalArgumentException ex) {
	    fail("Exception not thrown");
	}
    }

    @Test
    public void testDeclaredStartIndex() {

	Mockito.when(parser.getValue(OSParameters.START_INDEX.getName())).thenReturn("5");
	try {
	    String startIndex = reader.parse(OSParameters.START_INDEX);
	    Assert.assertEquals("5", startIndex);

	} catch (IllegalArgumentException ex) {
	    fail("Exception not thrown");
	}
    }

    @Test
    public void testNonIntegerCount() {

	Mockito.when(parser.getValue(OSParameters.COUNT.getName())).thenReturn("a");
	try {
	    reader.parse(OSParameters.COUNT);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	}
    }

    @Test
    public void testDefaultCount() {

	Mockito.when(parser.getValue(OSParameters.COUNT.getName())).thenReturn(null);
	try {
	    String count = reader.parse(OSParameters.COUNT);
	    Assert.assertEquals("10", count);

	} catch (IllegalArgumentException ex) {
	    fail("Exception not thrown");
	}
    }

    @Test
    public void testDeclaredCount() {

	Mockito.when(parser.getValue(OSParameters.COUNT.getName())).thenReturn("5");
	try {
	    String count = reader.parse(OSParameters.COUNT);
	    Assert.assertEquals("5", count);

	} catch (IllegalArgumentException ex) {
	    fail("Exception not thrown");
	}
    }

    @Test
    public void testNullSearchTerms() {

	Mockito.when(parser.getValue(OSParameters.SEARCH_TERMS.getName())).thenReturn(null);
	try {
	    String searchTerms = reader.parse(OSParameters.SEARCH_TERMS);
	    Assert.assertNull(searchTerms);

	} catch (IllegalArgumentException ex) {
	    fail("Exception not thrown");
	}
    }

    @Test
    public void testDeclaredSearchTerms() {

	Mockito.when(parser.getValue(OSParameters.SEARCH_TERMS.getName())).thenReturn("abcd");
	try {
	    String searchTerms = reader.parse(OSParameters.SEARCH_TERMS);
	    Assert.assertEquals("abcd", searchTerms);

	} catch (IllegalArgumentException ex) {
	    fail("Exception not thrown");
	}
    }

    @Test
    public void testNullTime() {

	Mockito.when(parser.getValue(OSParameters.TIME_START.getName())).thenReturn(null);
	try {
	    String startTime = reader.parse(OSParameters.TIME_START);
	    Assert.assertNull(startTime);

	} catch (IllegalArgumentException ex) {
	    fail("Exception not thrown");
	}
    }

    @Test
    public void testInvalidTime() {

	Mockito.when(parser.getValue(OSParameters.TIME_START.getName())).thenReturn("abcd");
	try {
	    reader.parse(OSParameters.TIME_START);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	}

	Mockito.when(parser.getValue(OSParameters.TIME_END.getName())).thenReturn("abcd");
	try {
	    reader.parse(OSParameters.TIME_END);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	}
    }

    @Test
    public void testInvalidTime2() {

	Mockito.when(parser.getValue(OSParameters.TIME_START.getName())).thenReturn("1900-w");
	try {
	    reader.parse(OSParameters.TIME_START);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex);
	}

	Mockito.when(parser.getValue(OSParameters.TIME_END.getName())).thenReturn("1900-w");
	try {
	    reader.parse(OSParameters.TIME_END);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex);
	}
    }

    @Test
    public void testInvalidTime3() {

	Mockito.when(parser.getValue(OSParameters.TIME_START.getName())).thenReturn("1900-");
	try {
	    reader.parse(OSParameters.TIME_START);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex);
	}

	Mockito.when(parser.getValue(OSParameters.TIME_END.getName())).thenReturn("1900-");
	try {
	    reader.parse(OSParameters.TIME_END);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex);
	}
    }

    @Test
    public void testInvalidTime4() {

	Mockito.when(parser.getValue(OSParameters.TIME_START.getName())).thenReturn("1900-12-");
	try {
	    reader.parse(OSParameters.TIME_START);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex);
	}

	Mockito.when(parser.getValue(OSParameters.TIME_END.getName())).thenReturn("1900-12-");
	try {
	    reader.parse(OSParameters.TIME_END);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex);
	}
    }

    @Test
    public void testInvalidTime5() {

	Mockito.when(parser.getValue(OSParameters.TIME_START.getName())).thenReturn("1900-12-A");
	try {
	    reader.parse(OSParameters.TIME_START);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex);
	}

	Mockito.when(parser.getValue(OSParameters.TIME_END.getName())).thenReturn("1900-12-A");
	try {
	    reader.parse(OSParameters.TIME_END);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex);
	}
    }

    @Test
    public void testInvalidTime6() {

	Mockito.when(parser.getValue(OSParameters.TIME_START.getName())).thenReturn("1900-12/12");
	try {
	    reader.parse(OSParameters.TIME_START);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex);
	}

	Mockito.when(parser.getValue(OSParameters.TIME_END.getName())).thenReturn("1900-12/12");
	try {
	    reader.parse(OSParameters.TIME_END);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex);
	}
    }

    @Test
    public void testInvalidTime7() {

	Mockito.when(parser.getValue(OSParameters.TIME_START.getName())).thenReturn("1900-12-50");
	try {
	    reader.parse(OSParameters.TIME_START);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex);
	}

	Mockito.when(parser.getValue(OSParameters.TIME_END.getName())).thenReturn("1900-12-50");
	try {
	    reader.parse(OSParameters.TIME_END);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex);
	}
    }

    @Test
    public void testInvalidTime8() {

	Mockito.when(parser.getValue(OSParameters.TIME_START.getName())).thenReturn("1900-13-01");
	try {
	    reader.parse(OSParameters.TIME_START);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex);
	}

	Mockito.when(parser.getValue(OSParameters.TIME_END.getName())).thenReturn("1900-13-01");
	try {
	    reader.parse(OSParameters.TIME_END);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex);
	}
    }

    @Test
    public void testInvalidTime9() {

	Mockito.when(parser.getValue(OSParameters.TIME_START.getName())).thenReturn("-1900-13-01");
	try {
	    reader.parse(OSParameters.TIME_START);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex);
	}

	Mockito.when(parser.getValue(OSParameters.TIME_END.getName())).thenReturn("-1900-13-01");
	try {
	    reader.parse(OSParameters.TIME_END);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex);
	}
    }

    @Test
    public void testValidTimeWithFinalT() {

	Mockito.when(parser.getValue(OSParameters.TIME_START.getName())).thenReturn("1900-12-01T");
	try {
	    reader.parse(OSParameters.TIME_START);

	} catch (IllegalArgumentException ex) {
	    fail("Exception thrown");
	}

	Mockito.when(parser.getValue(OSParameters.TIME_END.getName())).thenReturn("1900-12-01T");
	try {
	    reader.parse(OSParameters.TIME_END);

	} catch (IllegalArgumentException ex) {
	    fail("Exception thrown");
	}
    }

    @Test
    public void testValidTimeWithHour() {

	Mockito.when(parser.getValue(OSParameters.TIME_START.getName())).thenReturn("1900-12-01T01");
	try {
	    reader.parse(OSParameters.TIME_START);

	} catch (IllegalArgumentException ex) {
	    fail("Exception thrown");
	}

	Mockito.when(parser.getValue(OSParameters.TIME_END.getName())).thenReturn("1900-12-01T01");
	try {
	    reader.parse(OSParameters.TIME_END);

	} catch (IllegalArgumentException ex) {
	    fail("Exception thrown");
	}
    }

    @Test
    public void testValidTimeWithHourMinutesSeconds() {

	Mockito.when(parser.getValue(OSParameters.TIME_START.getName())).thenReturn("1900-12-01T01:01:01");
	try {
	    reader.parse(OSParameters.TIME_START);

	} catch (IllegalArgumentException ex) {
	    fail("Exception thrown");
	}

	Mockito.when(parser.getValue(OSParameters.TIME_END.getName())).thenReturn("1900-12-01T01:01:01");
	try {
	    reader.parse(OSParameters.TIME_END);

	} catch (IllegalArgumentException ex) {
	    fail("Exception thrown");
	}
    }

    @Test
    public void testValidTimeWithHourMinutesSecondsFinalZ() {

	Mockito.when(parser.getValue(OSParameters.TIME_START.getName())).thenReturn("1900-12-01T01:01:01Z");
	try {
	    reader.parse(OSParameters.TIME_START);

	} catch (IllegalArgumentException ex) {
	    fail("Exception thrown");
	}

	Mockito.when(parser.getValue(OSParameters.TIME_END.getName())).thenReturn("1900-12-01T01:01:01Z");
	try {
	    reader.parse(OSParameters.TIME_END);

	} catch (IllegalArgumentException ex) {
	    fail("Exception thrown");
	}
    }

    @Test
    public void testSingleInvalidABCBox() {

	Mockito.when(parser.getValue(OSParameters.BBOX.getName())).thenReturn("abc");
	try {
	    reader.parse(OSParameters.BBOX);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex.getMessage());
	}
    }

    @Test
    public void testSingleInvalidBox2() {

	Mockito.when(parser.getValue(OSParameters.BBOX.getName())).thenReturn("-180,");
	try {
	    reader.parse(OSParameters.BBOX);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex.getMessage());
	}
    }

    @Test
    public void testSingleInvalidBox3() {

	Mockito.when(parser.getValue(OSParameters.BBOX.getName())).thenReturn("-180,-90,180,");
	try {
	    reader.parse(OSParameters.BBOX);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex.getMessage());
	}
    }

    @Test
    public void testSingleInvalidBox4() {

	Mockito.when(parser.getValue(OSParameters.BBOX.getName())).thenReturn("-180,-90,180,r");
	try {
	    reader.parse(OSParameters.BBOX);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex.getMessage());
	}
    }

    @Test
    public void testSingleInvalidBox5() {

	Mockito.when(parser.getValue(OSParameters.BBOX.getName())).thenReturn("181,-90,180,90");
	try {
	    reader.parse(OSParameters.BBOX);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex.getMessage());
	}
    }

    @Test
    public void testSingleInvalidBox6() {

	Mockito.when(parser.getValue(OSParameters.BBOX.getName())).thenReturn("180,-91,180,90");
	try {
	    reader.parse(OSParameters.BBOX);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex.getMessage());
	}
    }

    @Test
    public void testSingleInvalidBox7() {

	Mockito.when(parser.getValue(OSParameters.BBOX.getName())).thenReturn("180,-90,181,90");
	try {
	    reader.parse(OSParameters.BBOX);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex.getMessage());
	}
    }

    @Test
    public void testSingleInvalidBox8() {

	Mockito.when(parser.getValue(OSParameters.BBOX.getName())).thenReturn("180,-90,180,91");
	try {
	    reader.parse(OSParameters.BBOX);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex.getMessage());
	}
    }

    @Test
    public void testInvalidBoxSouthGTNorth() {

	Mockito.when(parser.getValue(OSParameters.BBOX.getName())).thenReturn("180,90,180,-90");
	try {
	    reader.parse(OSParameters.BBOX);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex.getMessage());
	}
    }

    @Test
    public void testSingleValidCrossOriginBox() {

	Mockito.when(parser.getValue(OSParameters.BBOX.getName())).thenReturn("80,-90,-80,90");
	try {
	    reader.parse(OSParameters.BBOX);

	} catch (IllegalArgumentException ex) {
	    fail("Exception thrown");
	}
    }

    @Test
    public void testInvalidMultipleBox1() {

	Mockito.when(parser.getValue(OSParameters.BBOX.getName())).thenReturn("80,-90,-80,90_d");
	try {
	    reader.parse(OSParameters.BBOX);
	    fail("Exception not thrown");
	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex.getMessage());
	}
    }

    @Test
    public void testInvalidMultipleBox2() {

	Mockito.when(parser.getValue(OSParameters.BBOX.getName())).thenReturn("80,-90,-80,90_70,90-80,-90");
	try {
	    reader.parse(OSParameters.BBOX);
	    fail("Exception not thrown");
	} catch (IllegalArgumentException ex) {
	    //	    System.out.println(ex.getMessage());
	}
    }

    @Test
    public void testOutputFormat() {

	Mockito.when(parser.getValue(OSParameters.OUTPUT_FORMAT.getName())).thenReturn("format");
	try {
	    reader.parse(OSParameters.OUTPUT_FORMAT);
	} catch (IllegalArgumentException ex) {
	    fail("Exception thrown");
	}
    }
}
