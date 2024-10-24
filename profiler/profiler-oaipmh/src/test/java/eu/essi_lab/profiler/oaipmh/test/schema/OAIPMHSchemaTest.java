package eu.essi_lab.profiler.oaipmh.test.schema;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.oaipmh.OAIPMHtype;

public class OAIPMHSchemaTest {

     public void test(OAIPMHtype oai, String match) {

	try {

	    GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	    calendar.setTime(new Date());

	    XMLGregorianCalendar ret = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);

	    ret = ret.normalize();
	    ret.setFractionalSecond(null);

	    oai.setResponseDate(ret);

	} catch (DatatypeConfigurationException ex) {
	    // it should not happen, nothing to do...
	}

	try {
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	    CommonContext.createMarshaller(true).marshal(oai, outputStream);

	    // byte[] byteArray = outputStream.toByteArray();
	    // String out = new String(byteArray);

	    Assert.assertThat("Output string contains " + match, outputStream.toString(), new Matcher<String>() {

		@Override
		public void describeTo(Description description) {
		}

		@Override
		public boolean matches(Object item) {

		    return item.toString().contains(match);
		}

		@Override
		public void describeMismatch(Object item, Description mismatchDescription) {
		}

		@Override
		public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
		}
	    });

	} catch (JAXBException e) {

	    fail("JAXB excpetion thrown");
	}
    }

}
