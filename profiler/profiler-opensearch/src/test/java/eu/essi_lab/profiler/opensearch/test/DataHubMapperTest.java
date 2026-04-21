package eu.essi_lab.profiler.opensearch.test;

import eu.essi_lab.accessor.datahub.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.resource.*;
import eu.essi_lab.profiler.os.handler.discover.datahub.*;
import org.json.*;

import java.io.*;
import java.util.*;

public class DataHubMapperTest {

    public void test() throws Exception {

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

	//	System.out.println(res.asString(false));
	String jsonMetadata = new DatahubJsonMapper().toJson(res);
	System.out.println(new JSONObject(jsonMetadata).toString(2));
    }

    public static void main(String[] args) throws Exception {

	DataHubMapperTest t = new DataHubMapperTest();
	t.test();
    }

}
