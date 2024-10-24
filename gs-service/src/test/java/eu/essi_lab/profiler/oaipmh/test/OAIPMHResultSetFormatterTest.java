package eu.essi_lab.profiler.oaipmh.test;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.oaipmh.HeaderType;
import eu.essi_lab.jaxb.oaipmh.ListRecordsType;
import eu.essi_lab.jaxb.oaipmh.OAIPMHtype;
import eu.essi_lab.jaxb.oaipmh.RecordType;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.profiler.oaipmh.handler.discover.OAIPMHResultSetFormatter;

public class OAIPMHResultSetFormatterTest extends OAIPMHResultSetMapperTest {

    static {

	RuntimeDelegate.setInstance(new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl());
    }

    @Test
    public void noResumptionTokenTest() {

	try {

	    init("http://profiler-oai-pmh?verb=ListRecords&metadataPrefix=ISO19139", 3);

	    ResultSet<String> resultSet = getResultSet();

	    OAIPMHResultSetFormatter formatter = new OAIPMHResultSetFormatter();
	    message.setPage(new Page(1, 50));
	    Response format = formatter.format(message, resultSet);

	    String string = (String) format.getEntity();
	    OAIPMHtype response = CommonContext.unmarshal(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)),
		    OAIPMHtype.class);

	    // OAIPMHContext.createMarshaller(false).marshal(response, System.out);

	    ListRecordsType listRecords = response.getListRecords();
	    List<RecordType> recordList = listRecords.getRecord();
	    {
		HeaderType header = recordList.get(0).getHeader();
		String identifier = header.getIdentifier();
		Assert.assertEquals("core_MI1", identifier);
	    }
	    {
		HeaderType header = recordList.get(1).getHeader();
		String identifier = header.getIdentifier();
		Assert.assertEquals("core_MI2", identifier);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }
}
