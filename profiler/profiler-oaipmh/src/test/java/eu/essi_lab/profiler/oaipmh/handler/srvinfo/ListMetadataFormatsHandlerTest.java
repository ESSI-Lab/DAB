package eu.essi_lab.profiler.oaipmh.handler.srvinfo;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.schemas.CommonSchemas;
import eu.essi_lab.jaxb.oaipmh.ListMetadataFormatsType;
import eu.essi_lab.jaxb.oaipmh.MetadataFormatType;
import eu.essi_lab.jaxb.oaipmh.OAIPMHtype;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.profiler.oaipmh.profile.OAIPMHProfile;

public class ListMetadataFormatsHandlerTest {

    private static JAXBContext jaxbContext;

    static {

	//
	// the file access is required for local schemas while http is required in particular
	// for the authzforce XACML data model
	//
	System.setProperty("javax.xml.accessExternalSchema", "all");

	// ---------------------------------------------------------------------------------------
	//
	// init the common schemas
	//
	new CommonSchemas();

	try {

	    jaxbContext = JAXBContext.newInstance(//

		    OAIPMHtype.class // OAI-PMH
	    );
	} catch (JAXBException e) {
	    GSLoggerFactory.getLogger(CommonContext.class).error("Fatal initialization error!");
	    GSLoggerFactory.getLogger(CommonContext.class).error(e.getMessage(), e);
	}
    }

    @Test
    public void test() throws UnsupportedEncodingException, JAXBException {
	OAIPMHtype oai = new OAIPMHtype();

	List<MetadataFormatType> formats = OAIPMHProfile.getAllSupportedMetadataFormats();

	ListMetadataFormatsType mft = new ListMetadataFormatsType();
	formats.forEach(mft.getMetadataFormat()::add);

	oai.setListMetadataFormats(mft);

	// NamespacePrefixMapper mapper = new OAIPMHNameSpaceMapper();
	NamespacePrefixMapper mapper = new NamespacePrefixMapper() {

	    @Override
	    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
		// TODO Auto-generated method stub
		return suggestion;
	    }
	};
	Marshaller marshaller = jaxbContext.createMarshaller();
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	marshaller.marshal(oai, outputStream);

	String str = new String(outputStream.toByteArray());

	System.out.println(str);

    }

}
