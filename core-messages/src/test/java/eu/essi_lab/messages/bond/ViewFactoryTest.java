package eu.essi_lab.messages.bond;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.messages.bond.jaxb.ViewFactory;

public class ViewFactoryTest {

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @Before
    public void init() {
	new ViewFactory();
    }

    @Test
    public void testMarshaller() throws Exception {
	ViewFactory.createMarshaller();
    }

    @Test
    public void testUnmarshaller() throws Exception {
	ViewFactory.createUnmarshaller();
    }

}
