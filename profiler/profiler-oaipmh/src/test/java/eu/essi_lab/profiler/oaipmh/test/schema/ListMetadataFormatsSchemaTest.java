package eu.essi_lab.profiler.oaipmh.test.schema;

import java.util.ArrayList;

import org.junit.Test;

import eu.essi_lab.jaxb.oaipmh.ListMetadataFormatsType;
import eu.essi_lab.jaxb.oaipmh.MetadataFormatType;
import eu.essi_lab.jaxb.oaipmh.OAIPMHtype;
import eu.essi_lab.jaxb.oaipmh.RequestType;
import eu.essi_lab.jaxb.oaipmh.VerbType;

public class ListMetadataFormatsSchemaTest extends OAIPMHSchemaTest {

    @Test
    public void test() {


	ArrayList<MetadataFormatType> supportedFormats = new ArrayList<MetadataFormatType>();

	MetadataFormatType isoMetadata = new MetadataFormatType();
	isoMetadata.setMetadataPrefix("ISO19139");
	isoMetadata.setSchema("http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd");
	isoMetadata.setMetadataNamespace("http://www.isotc211.org/2005/gmd");

	supportedFormats.add(isoMetadata);

	MetadataFormatType coreMetadata = new MetadataFormatType();
	coreMetadata.setMetadataPrefix("oai_dc");
	coreMetadata.setSchema("http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
	coreMetadata.setMetadataNamespace("http://www.openarchives.org/OAI/2.0/oai_dc/");

	supportedFormats.add(coreMetadata);

	ListMetadataFormatsType mft = new ListMetadataFormatsType();
	for (MetadataFormatType format : supportedFormats) {
	    mft.getMetadataFormat().add(format);
	}
	
	OAIPMHtype oai = new OAIPMHtype();
	oai.setListMetadataFormats(mft);
	
	RequestType requestType = new RequestType();
	requestType.setVerb(VerbType.LIST_METADATA_FORMATS);
	requestType.setValue("BASE_URL");

	oai.setRequest(requestType);

	super.test(oai, "ListMetadataFormats");
    }

}
