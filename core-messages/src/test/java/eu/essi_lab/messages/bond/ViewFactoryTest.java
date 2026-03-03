package eu.essi_lab.messages.bond;

import eu.essi_lab.messages.bond.jaxb.*;
import org.junit.*;
import org.junit.rules.*;

public class ViewFactoryTest {

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @Before
    public void init() {
    }

    @Test
    public void testMarshaller() {
	ViewFactory.createMarshaller();
    }

    @Test
    public void testUnmarshaller() {
	ViewFactory.createUnmarshaller();
    }

}
