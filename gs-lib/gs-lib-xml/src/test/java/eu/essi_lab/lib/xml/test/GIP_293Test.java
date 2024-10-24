/**
 * 
 */
package eu.essi_lab.lib.xml.test;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;

/**
 * @author Fabrizio
 */
public class GIP_293Test {

    @Test
    public void test() throws SAXException, IOException, XPathExpressionException {

	InputStream stream = GIP_293Test.class.getClassLoader().getResourceAsStream("cr-file.xml");
	XMLDocumentReader reader = new XMLDocumentReader(stream);

	//
	// number of empty text nodes before removal
	//
	int emptyTextNodesCount = reader.evaluateNumber("count(//text()[normalize-space(.) = ''])").intValue();

	XMLDocumentWriter writer = new XMLDocumentWriter(reader);
	writer.remove("//b");

	//
	// number of empty text nodes after removal
	//
	int emptyTextNodesCount2 = reader.evaluateNumber("count(//text()[normalize-space(.) = ''])").intValue();

	//
	// expecting one empty node less
	//
	Assert.assertEquals((emptyTextNodesCount - 1), emptyTextNodesCount2);
	
	System.out.println(reader);
    }
}
