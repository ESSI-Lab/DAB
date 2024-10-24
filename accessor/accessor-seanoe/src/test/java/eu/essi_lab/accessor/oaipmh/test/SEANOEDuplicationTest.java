package eu.essi_lab.accessor.oaipmh.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;

import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class SEANOEDuplicationTest {

    

    @Test
    public void test() throws IOException, GSException, Exception {

	String record = IOStreamUtils
		.asUTF8String(SEANOEDuplicationTest.class.getClassLoader().getResourceAsStream("seanoe.xml"));

	XMLDocumentReader xdoc = new XMLDocumentReader(record);
	
	List<Node> entries = xdoc.evaluateOriginalNodesList("//*:entry");
	List<String> titles = new ArrayList<>();
	Set<String> duplicationList = new HashSet<>();
	List<String> duplicatedId = new ArrayList<>();
	for(Node n: entries) {
	    String title = xdoc.evaluateString(n, "*:title");
	    if(titles.contains(title)) {
		duplicationList.add(title);
		String id = xdoc.evaluateString(n, "*:id");
		duplicatedId.add(id);
	    } else {
		titles.add(title);
	    }
	}
	System.out.println("Records found: " + titles.size());
	System.out.println(Arrays.toString(titles.toArray()));
	System.out.println("Duplication records found: " + duplicationList.size());
	
	System.out.println("ID found: " + duplicatedId.size());
	System.out.println(Arrays.toString(duplicatedId.toArray()));
	Assert.assertTrue(duplicationList.size() == 0);
	
//	List<Node> duplicatedNodes;
//	for(String s: duplicationList) {
//	    duplicatedNodes = xdoc.evaluateOriginalNodesList("//*:id/../*:title[contains(text()," + s + ")]");
//	}
//	List<Node> duplicatedNodes = 
//	    for(Node node: duplicatedNodes) {
//		totalDuplicates.add(node.getTextContent());
//	    }
//	
//	System.out.println(Arrays.toString(totalDuplicates.toArray()));
//	Assert.assertTrue(totalDuplicates.size() > 0);
	
	
    }
}