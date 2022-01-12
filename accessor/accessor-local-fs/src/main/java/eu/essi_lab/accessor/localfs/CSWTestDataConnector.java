package eu.essi_lab.accessor.localfs;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.cdk.harvest.AbstractHarvestedQueryConnector;
import eu.essi_lab.identifierdecorator.SourcePriorityDocument;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.common.NameSpace;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
public class CSWTestDataConnector extends AbstractHarvestedQueryConnector {

    /**
     * 
     */
    private static final long serialVersionUID = -3290735249137667577L;

    public CSWTestDataConnector() {
    }

    @Override
    public boolean enableMaxRecordsOption() {

	return false;
    }

    @Override
    public String getLabel() {

	return "CSW test data connector";
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
		GSLoggerFactory.getLogger(getClass()).info("Loading record: "+record);
		InputStream stream = CSWTestDataConnector.class.getClassLoader().getResourceAsStream("csw-test-data/" + record);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(stream, baos);
		stream.close();
		String str = new String(baos.toByteArray());
		baos.close();
		OriginalMetadata metadata = new OriginalMetadata();
		metadata.setSchemeURI(CommonNameSpaceContext.CSW_NS_URI);
		metadata.setMetadata(str);
		ret.addRecord(metadata);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	    throw new GSException();
	}

	ret.setResumptionToken(null);

	return ret;
    }

    @JsonIgnore
    @Override
    public List<String> listMetadataFormats() throws GSException {

	List<String> ret = new ArrayList<>();
	ret.add("OAI-DC");
	ret.add(NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX);

	return ret;
    }

    @Override
    public boolean supports(Source source) {

	return source.getUniqueIdentifier().equals(SourcePriorityDocument.OGC_CITE_CSW_TEST_DATA_SOURCE_ID);

    }

}
