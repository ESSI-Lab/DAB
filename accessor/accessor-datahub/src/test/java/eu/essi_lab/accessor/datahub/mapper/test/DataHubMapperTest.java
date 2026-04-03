package eu.essi_lab.accessor.datahub.mapper.test;

import eu.essi_lab.accessor.datahub.DatahubMapper;
import eu.essi_lab.accessor.datahub.DatahubToJsonMapper;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.UUID;

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
            String jsonMetadata = DatahubToJsonMapper.toJson(res);
            System.out.println(new JSONObject(jsonMetadata).toString(2));
        }

        public static void main(String[] args) throws Exception {
            DataHubMapperTest t = new DataHubMapperTest();
            t.test();
        }


}
