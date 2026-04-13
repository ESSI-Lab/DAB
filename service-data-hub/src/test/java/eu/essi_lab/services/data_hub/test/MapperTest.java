package eu.essi_lab.services.data_hub.test;

import eu.essi_lab.accessor.datahub.*;
import eu.essi_lab.identifierdecorator.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.resource.*;
import eu.essi_lab.services.data_hub.*;

import static eu.essi_lab.accessor.datahub.DatahubMapper.*;

/**
 * @author Fabrizio
 */
public class MapperTest {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

	String user = "";
	String pwd = "";
	String url = "";

	String token = DataHubService.getAccessToken(user, //
		pwd,//
		url);//

	DatahubConnector connector = new DatahubConnector();

	String urn = "urn:li:dataset:(urn:li:dataPlatform:metadata,v4_7_changedetection_comunereggiocalabria_asbestos_20251011,DEV)";

	String jsonEntity = connector.fetch(url, token, urn);

	OriginalMetadata original = new OriginalMetadata();
	original.setMetadata(jsonEntity);
	original.setSchemeURI(DATAHUB_NS_URI);

	DatahubMapper mapper = new DatahubMapper();

	GSSource source = new GSSource();
	source.setEndpoint("http://");
	source.setUniqueIdentifier("datahub");
	source.setLabel("label");

	GSResource resource = mapper.map(original, source);

	String originalId = IdentifierDecorator.generatePersistentIdentifier(resource.getOriginalId().get(), source.getUniqueIdentifier());

	resource.setPrivateId(StringUtils.URLEncodeUTF8(originalId));
	resource.setOriginalId(originalId);
	resource.setPublicId(originalId);

	System.out.println(resource.getPrivateId());
	System.out.println(resource.getPublicId());
	System.out.println(resource.getOriginalId().get());

	System.out.println(resource.asString(false));
    }
}
