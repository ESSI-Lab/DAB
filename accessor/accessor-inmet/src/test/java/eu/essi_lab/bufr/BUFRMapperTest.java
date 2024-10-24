package eu.essi_lab.bufr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

public class BUFRMapperTest {

    @Test
    public void test() throws IOException, GSException, JAXBException {
	BUFRMapper mapper = new BUFRMapper();
	OriginalMetadata originalMD = new OriginalMetadata();
	InputStream stream = BUFRMapperTest.class.getClassLoader().getResourceAsStream("bufr.metadata.xml");
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	IOUtils.copy(stream, baos);
	stream.close();
	baos.close();
	String metadata = new String(baos.toByteArray(), StandardCharsets.UTF_8);
	originalMD.setMetadata(metadata);
	GSResource mapped = mapper.execMapping(originalMD, new GSSource());
	System.out.println(mapped.asString(true));
    }

}
