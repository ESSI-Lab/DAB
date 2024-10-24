package eu.essi_lab.accessor.wod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.MIMetadata;

public class WODMetadataCreatorTest {

    @Test
    public void test() throws Exception {
	InputStream stream = WODMetadataCreatorTest.class.getClassLoader().getResourceAsStream("wod_mbt_2003.nc");
	File tmpFile = File.createTempFile("wod_mbt_2003.nc" + getClass().getSimpleName(), ".nc");
	FileOutputStream fos = new FileOutputStream(tmpFile);
	IOUtils.copy(stream, fos);
	stream.close();
	fos.close();
	WODMetadataCreator creator = new WODMetadataCreator(tmpFile, "http://localhost/wod_mbt_2003.nc");
	MIMetadata result = creator.mapMetadata();
	InputStream s = result.asStream();
	IOUtils.copy(s, System.out);
	tmpFile.delete();
    }

}
