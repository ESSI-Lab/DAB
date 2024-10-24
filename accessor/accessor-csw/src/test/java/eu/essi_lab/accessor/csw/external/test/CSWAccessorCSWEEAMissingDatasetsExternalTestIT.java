package eu.essi_lab.accessor.csw.external.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Roberto
 */
public class CSWAccessorCSWEEAMissingDatasetsExternalTestIT {

    private String endpoint = "https://sdi.eea.europa.eu/catalogue/geoss/eng/csw?";

    private int missing = 0;

    /**
     * @throws GSException
     * @throws JAXBException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void checkMissingDataset() throws Exception {

	InputStream content = getClass().getClassLoader().getResourceAsStream("missing_identifiers.txt");

	String res = IOStreamUtils.asUTF8String(content);

	String[] splittedIds = res.split("\r\n");

	for (int i = 0; i < splittedIds.length; i++) {
	    Downloader d = new Downloader();
	    String metadataURL = endpoint
		    + "service=CSW&request=GetRecordById&version=2.0.2&typeNames=gmd:MD_Metadata&outputSchema=http://www.isotc211.org/2005/gmd&id="
		    + splittedIds[i];

	    HttpResponse<InputStream> response = d.downloadResponse(metadataURL);
	    String responseString = IOStreamUtils.asUTF8String(response.body());
	    if (responseString.toLowerCase().contains("exceptionreport") || !responseString.toLowerCase().contains("md_metadata")) {
		GSLoggerFactory.getLogger(getClass()).info("Missing datasets identifier: {}", splittedIds[i]);
		missing++;
	    }
	    Thread.sleep(3000);
	}

	GSLoggerFactory.getLogger(getClass()).info("Number of Missing datasets: {}", missing);
	Assert.assertTrue(missing == 5);

    }
}
