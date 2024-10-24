package csw.test;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSettingLoader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.csw.handler.discover.CSWRequestTransformer;

public class CSWRequestTransformerTest {

    /**
     * Tests a filter with a not operator like this which includes all sources but the ID1 one: <ogc:Not>
     * <ogc:PropertyIsEqualTo>
     * <ogc:PropertyName>sourceId</ogc:PropertyName> <ogc:Literal>ID1</ogc:Literal> </ogc:PropertyIsEqualTo> </ogc:Not>
     */
    @Test
    public void excludedSourceTest() {

	{

	    ArrayList<GSSource> sources = new ArrayList<GSSource>();

	    GSSource gsSource = new GSSource();
	    gsSource.setUniqueIdentifier("ID1");

	    GSSource gsSource2 = new GSSource();
	    gsSource2.setUniqueIdentifier("ID2");

	    GSSource gsSource3 = new GSSource();
	    gsSource3.setUniqueIdentifier("ID3");

	    sources.add(gsSource);
	    sources.add(gsSource2);
	    sources.add(gsSource3);

	    //
	    //
	    //
	    DefaultConfiguration configuration = new DefaultConfiguration();

	    ConfigurationWrapper.setConfiguration(configuration);

	    //
	    //
	    //

	    ConfigurationWrapper.getDistributonSettings().forEach(s -> configuration.remove(s.getIdentifier()));

	    ConfigurationWrapper.getHarvestingSettings().forEach(s -> configuration.remove(s.getIdentifier()));

	    {

		HarvestingSetting sourceSetting = HarvestingSettingLoader.load();

		sourceSetting.getAccessorsSetting().//
			select(s -> s.getName().equals("OAIPMH Accessor"));

		sourceSetting.getSelectedAccessorSetting().getGSSourceSetting().setSourceIdentifier(gsSource.getUniqueIdentifier());

		configuration.put(sourceSetting);
	    }

	    {

		HarvestingSetting sourceSetting = HarvestingSettingLoader.load();

		sourceSetting.getAccessorsSetting().//
			select(s -> s.getName().equals("OAIPMH Accessor"));

		sourceSetting.getSelectedAccessorSetting().getGSSourceSetting().setSourceIdentifier(gsSource2.getUniqueIdentifier());

		configuration.put(sourceSetting);
	    }

	    {

		HarvestingSetting sourceSetting = HarvestingSettingLoader.load();

		sourceSetting.getAccessorsSetting().//
			select(s -> s.getName().equals("OAIPMH Accessor"));

		sourceSetting.getSelectedAccessorSetting().getGSSourceSetting().setSourceIdentifier(gsSource3.getUniqueIdentifier());

		configuration.put(sourceSetting);
	    }

	    configuration.clean();

	}

	CSWRequestTransformer transformer = new CSWRequestTransformer() {

	    public DiscoveryMessage transform(WebRequest request) throws GSException {

		DiscoveryMessage message = new DiscoveryMessage();

		message.setUserBond(getUserBond(request));

		List<GSSource> sources = getSources(message.getUserBond().get());
		message.setSources(sources);

		return message;
	    }

	};

	String query = "	<csw:GetRecords xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" " + //
		"	    xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\" " + //
		"	     xmlns:dcterms=\"http://purl.org/dc/terms/\" " + //
		"	    xmlns:ows=\"http://www.opengis.net/ows\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" " + //
		"	    outputFormat=\"application/xml\" service=\"CSW\" version=\"2.0.2\"> " + //
		"	    <csw:Query xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" typeNames=\"gmd:MD_Metadata\"> " + //
		"	        <csw:ElementName>dc:identifier</csw:ElementName> " + //
		"	        <csw:Constraint version=\"1.1.0\"> " + //
		"		 <ogc:Filter>" + //
		"                <ogc:Not>" + //
		"                   <ogc:PropertyIsEqualTo>" + //
		"                       <ogc:PropertyName>sourceId</ogc:PropertyName>" + //
		"                       <ogc:Literal>ID1</ogc:Literal>" + //
		"                   </ogc:PropertyIsEqualTo>" + //
		"               </ogc:Not> " + //
		"           </ogc:Filter>" + //
		"	        </csw:Constraint> " + //
		"	    </csw:Query> " + //
		"	</csw:GetRecords> "; //

	ByteArrayInputStream inputStream = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
	try {

	    WebRequest webRequest = WebRequest.createPOST("http://localhost/cswiso", inputStream);

	    DiscoveryMessage message = transformer.transform(webRequest);
	    List<GSSource> sources = message.getSources();

	    Assert.assertEquals(2, sources.size());

	    for (GSSource gsSource : sources) {
		if (gsSource.getUniqueIdentifier().equals("ID1")) {
		    fail("Source ID1 not exclued");
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }
}
