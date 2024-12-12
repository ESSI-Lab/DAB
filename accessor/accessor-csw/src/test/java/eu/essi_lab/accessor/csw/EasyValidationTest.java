package eu.essi_lab.accessor.csw;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.w3c.dom.Node;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;

public class EasyValidationTest {

    @Test
    public void test() throws Exception {
	InputStream stream = EasyValidationTest.class.getClassLoader().getResourceAsStream("easy.xml");
	XMLDocumentReader reader = new XMLDocumentReader(stream);
	Map<String, String> map = new HashMap<>();
	map.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
	reader.setNamespaces(map);
	XMLDocumentWriter writer = new XMLDocumentWriter(reader);
	writer.remove("//*[@xsi:type]", "http://www.w3.org/2001/XMLSchema-instance", "type");
	// Node[] nodes = reader.evaluateNodes("//*[@xsi:type]");
	// for (Node element : nodes) {
	// Node attribute = reader.evaluateNode(element, "@xsi:type");
	// element.removeChild(attribute);
	// }
	System.out.println(reader.asString());
    }

}
