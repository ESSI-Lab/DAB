package eu.essi_lab.profiler.oaipmh.test.schema;

import org.junit.Test;

import eu.essi_lab.jaxb.oaipmh.DeletedRecordType;
import eu.essi_lab.jaxb.oaipmh.GranularityType;
import eu.essi_lab.jaxb.oaipmh.IdentifyType;
import eu.essi_lab.jaxb.oaipmh.OAIPMHtype;
import eu.essi_lab.jaxb.oaipmh.RequestType;
import eu.essi_lab.jaxb.oaipmh.VerbType;

public class IdentifyRequestSchemaTest extends OAIPMHSchemaTest {

    @Test
    public void test() {

	IdentifyType type = new IdentifyType();
	type.setRepositoryName("DAB OAI-PMH 2.0 interface");
	type.setBaseURL("BASE_URL");
	type.setDeletedRecord(DeletedRecordType.TRANSIENT);
	type.setEarliestDatestamp("EARLIEST_DATE_STAMP");
	type.setGranularity(GranularityType.YYYY_MM_DD_THH_MM_SS_Z);
	type.setProtocolVersion("2.0");
	type.getAdminEmail().add("test@my-org.com");

	OAIPMHtype oai = new OAIPMHtype();
	oai.setIdentify(type);

	RequestType requestType = new RequestType();
	requestType.setVerb(VerbType.IDENTIFY);
	requestType.setValue("BASE_URL");

	oai.setRequest(requestType);

	super.test(oai, "OAI-PMH");
    }
}
