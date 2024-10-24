package eu.essi_lab.lib.utils;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

import javax.xml.datatype.Duration;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ISO8601DateTimeUtilsTest {

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @Test
    public void testParsingBad() throws NullPointerException {
	exceptions.expect(NullPointerException.class);
	ISO8601DateTimeUtils.parseISO8601(null);
    }

    @Test
    public void testParsingBad2() throws IllegalArgumentException {
	exceptions.expect(IllegalArgumentException.class);
	ISO8601DateTimeUtils.parseISO8601("quella tal'ora");
    }

    @Test
    public void testParsing() throws IllegalArgumentException {
	testParsing("2017-03-15T23:29:48Z","2017-03-15T23:29:48Z");
	
	testParsing("2017-03-15","2017-03-15T00:00:00Z");
	
	testParsing("20170315","20170315-01-01T00:00:00Z");
	
	
    }
    
    private void testParsing(String start, String expected) {
	Date date = ISO8601DateTimeUtils.parseISO8601(start);
	String time2 = ISO8601DateTimeUtils.getISO8601DateTime(date);
	GSLoggerFactory.getLogger(getClass()).info("Checking {} & {}",time2,expected);
	Assert.assertEquals(expected, time2);
	
    }

    @Test
    public void testParsingDuration() throws ParseException {
	Optional<Date> res = ISO8601DateTimeUtils.parseNotStandardToDate("20110309");	
	Date date = res.get();
	assertEquals(1299628800000l, date.getTime());
	System.out.println(date.getTime());
	Duration duration = ISO8601DateTimeUtils.getDuration("P1M");
//	duration = duration.negate();
	Date result = ISO8601DateTimeUtils.subtractDuration(date, duration);
	assertEquals(1297209600000l, result.getTime());
    }
    

    @Test
    public void testParsingMilliseconds() throws IllegalArgumentException {
	String time = "2017-03-15T23:29:48.250Z";
	Date date = ISO8601DateTimeUtils.parseISO8601(time);
	String time2 = ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(date);
	Assert.assertEquals(time, time2);
    }
    


}
