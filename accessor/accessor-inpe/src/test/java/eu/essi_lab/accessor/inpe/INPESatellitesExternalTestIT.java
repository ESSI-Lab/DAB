package eu.essi_lab.accessor.inpe;

import java.io.InputStream;
import java.util.Optional;

import org.junit.Test;

import eu.essi_lab.lib.net.downloader.Downloader;

public class INPESatellitesExternalTestIT extends INPESatellitesTest {

    @Test
    public void test() throws Exception {
	super.test();

    }
    
    /**
     * 
     * @return
     */
    @Override
    protected int getExpectedSatellitesCount(){
	
	return 12;
    }
    
    @Override
    public InputStream getStream() {
	String url = "http://www.dgi.inpe.br/CDSR/panel.php";
	Downloader downloader = new Downloader();
	Optional<InputStream> stream = downloader.downloadOptionalStream(url);
	return stream.get();
    }
}
