package eu.essi_lab.accessor.icos;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Test;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class ICOSMapperExternalTestIT {

    @Test
    public void test() throws Exception {
	String url = "https://meta.icos-cp.eu/objects/9BZV4UgQ3EBL7MwT8l_cnh7u/65DK20210130.csv.json";
	Downloader d = new Downloader();
	Optional<InputStream> stream = d.downloadOptionalStream(url);


	OriginalMetadata originalMD = new OriginalMetadata();

	String mmetadata = IOUtils.toString(stream.get());

	originalMD.setMetadata(mmetadata);

	GSSource gsSource = new GSSource();

	ICOSMapper mapper = new ICOSMapper();
	GSResource resource = mapper.map(originalMD, gsSource);

    }

}
