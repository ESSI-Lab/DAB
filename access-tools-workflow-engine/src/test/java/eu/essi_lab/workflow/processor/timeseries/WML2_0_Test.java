package eu.essi_lab.workflow.processor.timeseries;

import java.io.InputStream;

import org.junit.Test;

import eu.essi_lab.wml._2.JAXBWML2;

public class WML2_0_Test {

    @Test
    public void test() throws Exception {
	InputStream stream = WML2_0_Test.class.getClassLoader().getResourceAsStream("time_series_wml2.xml");
	Object collection = JAXBWML2.getInstance().getUnmarshaller().unmarshal(stream);
	System.out.println();
    }

}
