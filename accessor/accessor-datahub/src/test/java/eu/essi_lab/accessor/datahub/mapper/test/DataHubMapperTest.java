package eu.essi_lab.accessor.datahub.mapper.test;

import eu.essi_lab.accessor.datahub.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.model.resource.*;
import jakarta.xml.bind.*;
import org.junit.*;

import java.io.*;
import java.util.*;

/**
 * @author Fabrizio
 */
public class DataHubMapperTest {

    @Test
    public void test() throws IOException, GSException, JAXBException {

	DatahubMapper mapper = new DatahubMapper();

	GSSource source = new GSSource();
	source.setLabel("Label");
	source.setEndpoint("http://");
	source.setUniqueIdentifier(UUID.randomUUID().toString());

	InputStream stream = getClass().getClassLoader().getResourceAsStream("metadata.json");

	OriginalMetadata original = new OriginalMetadata();
	original.setSchemeURI("http://");
	original.setMetadata(IOStreamUtils.asUTF8String(stream));

	GSResource res = mapper.map(original, source);

	System.out.println(res.asString(false));
    }
}
