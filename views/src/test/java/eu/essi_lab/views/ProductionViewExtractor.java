package eu.essi_lab.views;

import java.io.InputStream;

import org.w3c.dom.Node;

import eu.essi_lab.lib.xml.XMLDocumentReader;

/**
 * Used to extract the view definition to be used in the GS-service production. Steps to perform:
 * <ol>
 * <li>Open page at https://confluence.geodab.eu/display/GPD/GS-service+PRODUCTION+sources in a browser</li>
 * <li>Copy the Id column (with CTRL)</li>
 * <li>Paste it in test file geossTable.xml file</li>
 * <li>Copy the generated code to the {@link ViewConnectorWriter} DELETING THE UNWANTED IDS!</li>
 * </ol>
 * In general it is possible to add new identifiers directly to the {@link ViewConnectorWriter} and the mentioned steps
 * to be used as a last resort
 * 
 * @author boldrini
 */
public class ProductionViewExtractor {

    public ProductionViewExtractor() throws Exception {
	InputStream stream = ProductionViewExtractor.class.getClassLoader().getResourceAsStream("geossTable.xml");
	XMLDocumentReader xdoc = new XMLDocumentReader(stream);
	Node[] nodes = xdoc.evaluateNodes("//*:td");
	System.out.println(
		"case \"geoss\":\n" + "		label = \"GEOSS sources\";\n" + "		bond = BondFactory.createOrBond(//");
	for (Node node : nodes) {
	    String id = node.getTextContent().trim();

	    System.out.println("		BondFactory.createSourceIdentifierBond(\"" + id + "\"), //");

	}
	System.out.println("		);\n" + "		break;");
    }

    public static void main(String[] args) throws Exception {
	new ProductionViewExtractor();
    }
}
