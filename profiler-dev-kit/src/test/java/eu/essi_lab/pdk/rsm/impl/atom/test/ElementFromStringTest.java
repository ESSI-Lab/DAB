/**
 * 
 */
package eu.essi_lab.pdk.rsm.impl.atom.test;

import java.io.IOException;
import java.io.StringReader;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import eu.essi_lab.lib.utils.IOStreamUtils;

/**
 * @author Fabrizio
 */
public class ElementFromStringTest {

    public static void main(String[] args) throws JDOMException, IOException {

	String inputStr = IOStreamUtils.asUTF8String(
		ElementFromStringTest.class.getClassLoader().getResourceAsStream("distribution.xml"));
	
	Element e = new Element("parentnode");
	StringReader stringReader = new StringReader(inputStr);

	SAXBuilder builder = new SAXBuilder();
	Document doc = builder.build(stringReader);

	Element rootE = doc.getRootElement();
	e.addContent(rootE.detach());
	
	//
	// ----
	//
	
	Format format = Format.getPrettyFormat();

	format.setEncoding("UTF-8");

	final XMLOutputter outputter = new XMLOutputter(format);
	String outputString = outputter.outputString(rootE);
	
	System.out.println(outputString);
    }
}
