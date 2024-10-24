package eu.essi_lab.lib.net.time.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.net.time.TimeZoneInfo;
import eu.essi_lab.lib.net.time.TimeZoneUtils;

public class TimeZoneUtilsExternalTestIT {

    @Test
    public void test() throws SAXException, IOException, XPathExpressionException {
	TimeZoneUtils utils = new TimeZoneUtils();
	TimeZoneInfo timeZone = utils.getTimeZoneInfo(new BigDecimal("43.7696"), new BigDecimal("11.2558"));
	String id = timeZone.getTimezoneId();
	assertEquals("Europe/Rome", id);
    }

}
