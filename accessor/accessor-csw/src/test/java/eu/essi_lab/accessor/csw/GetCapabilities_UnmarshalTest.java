/**
 * 
 */
package eu.essi_lab.accessor.csw;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.csw._2_0_2.Capabilities;
import eu.essi_lab.jaxb.ows._1_0_0.CodeType;
import eu.essi_lab.jaxb.ows._1_0_0.KeywordsType;
import eu.essi_lab.jaxb.ows._1_0_0.ServiceIdentification;

/**
 * @author Fabrizio
 */
public class GetCapabilities_UnmarshalTest {

    @Test
    public void test() throws JAXBException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("GetCapabilities2.xml");

	Capabilities capabilities = CommonContext.unmarshal(stream, Capabilities.class);

	ServiceIdentification serviceIdentification = capabilities.getServiceIdentification();

	String abstract_ = serviceIdentification.getAbstract();
	Assert.assertEquals("pycsw is an OGC CSW server implementation written in Python", abstract_);

	String title = serviceIdentification.getTitle();
	Assert.assertEquals("pycsw Geospatial Catalogue", title);

	List<KeywordsType> keywordType = serviceIdentification.getKeywords();
	Assert.assertEquals(1, keywordType.size());

	List<String> keywords = keywordType.get(0).getKeyword();
	Assert.assertEquals(3, keywords.size());

	Assert.assertTrue(keywords.stream().anyMatch(k -> k.equals("catalogue")));
	Assert.assertTrue(keywords.stream().anyMatch(k -> k.equals("discovery")));
	Assert.assertTrue(keywords.stream().anyMatch(k -> k.equals("metadata")));

	CodeType serviceType = serviceIdentification.getServiceType();
	Assert.assertEquals("CSW", serviceType.getValue());

	List<String> serviceTypeVersion = serviceIdentification.getServiceTypeVersion();
	Assert.assertEquals(2, serviceTypeVersion.size());

	Assert.assertTrue(serviceTypeVersion.stream().anyMatch(k -> k.equals("2.0.2")));
	Assert.assertTrue(serviceTypeVersion.stream().anyMatch(k -> k.equals("3.0.0")));
    }
}
