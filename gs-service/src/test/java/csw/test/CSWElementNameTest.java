package csw.test;

import static org.junit.Assert.fail;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.pdk.rsm.impl.xml.iso19139.GMD_ElementName;

public class CSWElementNameTest {

    @Test
    public void test() {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("cswelsetname/metadata.xml");

	try {
	    MIMetadata miMetadata = new MIMetadata(stream);

	    Document full = GMD_ElementName.subset(miMetadata.asDocument(true), ElementSetType.FULL);
	    XMLDocumentReader reader = new XMLDocumentReader(full);
	    int fullCount = reader.evaluateNumber("count(//*)").intValue();
	    // System.out.println(fullCount);

	    Document summary = GMD_ElementName.subset(miMetadata.asDocument(true), ElementSetType.SUMMARY);
	    reader = new XMLDocumentReader(summary);
	    // System.out.println(reader.asString());
	    int summaryCount = reader.evaluateNumber("count(//*)").intValue();
	    // System.out.println(summaryCount);

	    Document brief = GMD_ElementName.subset(miMetadata.asDocument(true), ElementSetType.BRIEF);
	    reader = new XMLDocumentReader(brief);
	    int briefCount = reader.evaluateNumber("count(//*)").intValue();
	    // System.out.println(briefCount);

	    Assert.assertTrue(fullCount > summaryCount && summaryCount > briefCount);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

    }
}
