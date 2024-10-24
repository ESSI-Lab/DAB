package eu.essi_lab.lib.utils;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.ByteStreams;

public class ClonableInputStreamTest {

    private static final String DOC_NAME = "testDoc.xml";
    private static final InputStream DOC_STREAM = ClonableInputStreamTest.class.getClassLoader().getResourceAsStream(DOC_NAME);

    @Test
    public void testClone() {

	try {
	    ClonableInputStream cloneInputStream = new ClonableInputStream(DOC_STREAM);

	    Matcher<String> matcher = new Matcher<String>() {

		@Override
		public void describeTo(Description description) {
		}

		@Override
		public boolean matches(Object item) {

		    return item.toString().contains("OpenSearchDescription");
		}

		@Override
		public void describeMismatch(Object item, Description mismatchDescription) {
		}

		@Override
		public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
		}
	    };

	    InputStream clone = cloneInputStream.clone();
	    String string = new String(ByteStreams.toByteArray(clone));

	    Assert.assertThat("String contains OpenSearchDescription",string, matcher);

	    clone = cloneInputStream.clone();
	    string = new String(ByteStreams.toByteArray(clone));
	    Assert.assertThat("String contains OpenSearchDescriptionDocument",string, matcher);

	} catch (IOException e) {
	    fail("Exception thrown");
	}
    }

}
