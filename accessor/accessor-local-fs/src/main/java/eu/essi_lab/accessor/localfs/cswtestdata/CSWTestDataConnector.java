package eu.essi_lab.accessor.localfs.cswtestdata;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Provides a connector CSW test data
 * 
 * @author boldrini
 */
public class CSWTestDataConnector extends HarvestedQueryConnector<CSWTestDataConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "CSWTestDataConnector";
    /**
     * 
     */
    private static final String OGC_CITE_CSW_TEST_DATA_SOURCE_ID = "ogc-cite-csw-test-data";
    private static final String CSW_TEST_DATA_CONNECTOR_ERROR = "CSW_TEST_DATA_CONNECTOR_ERROR";

    public CSWTestDataConnector() {
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();

	String[] records = new String[] { "Record_6a3de50b-fa66-4b58-a0e6-ca146fdd18d4.xml",
		"Record_94bc9c83-97f6-4b40-9eb8-a8e8787a5c63.xml", "Record_e9330592-0932-474b-be34-c3a3bb67c7db.xml",
		"Record_19887a8a-f6b0-4a63-ae56-7fba0e17801f.xml", "Record_784e2afd-a9fd-44a6-9a92-a3848371c8ec.xml",
		"Record_9a669547-b69b-469f-a11f-2d875366bbdc.xml", "Record_1ef30a8b-876d-4828-9246-c37ab4510bbd.xml",
		"Record_829babb0-b2f1-49e1-8cd5-7b489fe71a1e.xml", "Record_a06af396-3105-442d-8b40-22b57a90d2f2.xml",
		"Record_66ae76b7-54ba-489b-a582-0f0633d96493.xml", "Record_88247b56-4cbc-4df9-9860-db3f8042e357.xml",
		"Record_ab42a8c4-95e8-4630-bf79-33e59241605a.xml" };

	try {
	    for (String record : records) {
		GSLoggerFactory.getLogger(getClass()).info("Loading record: " + record);
		InputStream stream = CSWTestDataConnector.class.getClassLoader().getResourceAsStream("csw-test-data/" + record);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(stream, baos);
		stream.close();
		String str = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		baos.close();
		OriginalMetadata metadata = new OriginalMetadata();
		metadata.setSchemeURI(CommonNameSpaceContext.CSW_NS_URI);
		metadata.setMetadata(str);
		ret.addRecord(metadata);
	    }
	} catch (IOException e) {
  	    
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_TEST_DATA_CONNECTOR_ERROR, //
		    e);
	}

	ret.setResumptionToken(null);

	return ret;
    }

    
    @Override
    public List<String> listMetadataFormats() throws GSException {

	List<String> ret = new ArrayList<>();
	ret.add("OAI-DC");
	ret.add(NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX);

	return ret;
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getUniqueIdentifier().equals(OGC_CITE_CSW_TEST_DATA_SOURCE_ID);
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected CSWTestDataConnectorSetting initSetting() {

	return new CSWTestDataConnectorSetting();
    }
}
