package eu.essi_lab.lib.xml.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;

public class CDITest {

    public static void main(String[] args) throws Exception {
	XMLDocumentReader reader = new XMLDocumentReader(new File("/home/boldrini/cdi-inspire.xml"));
	XMLDocumentWriter writer = new XMLDocumentWriter(reader);
	writer.setText("//*:electronicMailAddress/*:CharacterString", "info@essilab.eu");
	ByteArrayInputStream bais = reader.asStream();
	FileOutputStream fos = new FileOutputStream(new File("/home/boldrini/cdi-inspire-2.xml"));
	IOUtils.copy(bais, fos);
	fos.close();
	bais.close();
    }

}
