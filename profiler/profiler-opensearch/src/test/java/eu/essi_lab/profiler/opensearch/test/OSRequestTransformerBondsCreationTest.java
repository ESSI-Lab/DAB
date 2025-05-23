package eu.essi_lab.profiler.opensearch.test;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.SimpleConfiguration;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.os.OSBox;
import eu.essi_lab.profiler.os.OSBox.CardinalPoint;
import eu.essi_lab.profiler.os.OSParameters;
import eu.essi_lab.profiler.os.OSProfilerSetting;
import eu.essi_lab.profiler.os.OSRequestParser;
import eu.essi_lab.profiler.os.handler.discover.OSRequestTransformer;

public class OSRequestTransformerBondsCreationTest {
    
    @Before
    public void before(){
	
	SimpleConfiguration simpleConfiguration = new SimpleConfiguration();
	ConfigurationWrapper.setConfiguration(simpleConfiguration);
    }

    @Test
    public void testBondsCreationStStartTimeEndTimeBbox() {

	String value = "http://opensearch?si=10&ct=50&st=pippo&ts=1900&te=2000-01-01T00:00:00.000Z&bbox=0,0,0,0_1,1,1,1&outputFormat="
		+ NameSpace.GS_DATA_MODEL_XML_MEDIA_TYPE;
	WebRequest webRequest = WebRequest.createGET(value);

	OSRequestTransformer transformer = createTransformer();

	try {
	    // Mockito.doNothing().when(transformer).addAllSources(Mockito.any());

	    DiscoveryMessage message = transformer.transform(webRequest);

	    // get the bond created by the transformer and creates a parser
	    Bond bond = message.getUserBond().get();

	    // creates a bond handler which creates an OS query string from the bonds
	    OSBondHandler osBondHandler = new OSBondHandler();

	    // parses the bond
	    DiscoveryBondParser bondParser = new DiscoveryBondParser(bond);
	    bondParser.parse(osBondHandler);

	    // get the query string
	    String queryString = osBondHandler.getQueryString();

	    // creates a key value parser from the string
	    KeyValueParser keyValueParser = new KeyValueParser(queryString);

	    // creates the OS parser with the given key value parser
	    OSRequestParser osRequestReader = new OSRequestParser(keyValueParser);

	    // String queryString = osRequestReader.getBondHandler().getQueryString();

	    Assert.assertEquals("pippo", osRequestReader.parse(OSParameters.SEARCH_TERMS));

	    String startTime = osRequestReader.parse(OSParameters.TIME_START);

	    // --------------------------------------------------------------------------------------------
	    // testing the contains since the date time parser return this format: 1900-01-01T00:00:00.000Z
	    // see OSRequestReader#parseISO8601DateTime
	    //
	    if (startTime == null || !startTime.contains("1900")) {
		fail("Start time not found");
	    }

	    String endTime = osRequestReader.parse(OSParameters.TIME_END);
	    Assert.assertEquals("2000-01-01T00:00:00.000Z", endTime.toString());

	    String bboxes = osRequestReader.parse(OSParameters.BBOX);
	    String[] split = bboxes.split("_");

	    // ------------------------------------------------------------------------------------------------
	    // original values are 0,0,0,0 and 1,1,1,1 but the OSRequestReader#parseDouble method adds the '.0'
	    // the values order of the split array 1,1,1,1 before 0,0,0,0 is opposite
	    // to the original request order due to the OSBondHandler implementation
	    //
	    for (String bbox : split) {

		Assert.assertThat(new OSBox(bbox), new Matcher<OSBox>() {

		    @Override
		    public void describeTo(Description description) {
		    }

		    @Override
		    public boolean matches(Object item) {

			OSBox box = (OSBox) item;

			String south = box.getString(CardinalPoint.SOUTH);
			String west = box.getString(CardinalPoint.WEST);
			String north = box.getString(CardinalPoint.NORTH);
			String east = box.getString(CardinalPoint.EAST);

			return (south.equals("0.0") || south.equals("1.0")) && //
			(west.equals("0.0") || west.equals("1.0")) && //
			(north.equals("0.0") || north.equals("1.0")) && //
			(east.equals("0.0") || east.equals("1.0")); //
		    }

		    @Override
		    public void describeMismatch(Object item, Description mismatchDescription) {
		    }

		    @Override
		    public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
		    }

		});
	    }
	} catch (GSException e) {

	    fail("Exception thrown");
	}
    }

    @Test
    public void testBondsCreationStExactSearchWithQuotationMarks() {

	String value = "http://opensearch?si=10&ct=50&st=\"pippo ciccio\"&searchFields=title&outputFormat="
		+ NameSpace.GS_DATA_MODEL_XML_MEDIA_TYPE;

	WebRequest webRequest = WebRequest.createGET(value);

	OSRequestTransformer transformer = createTransformer();

	try {

	    DiscoveryMessage message = transformer.transform(webRequest);

	    SimpleValueBond bond = (SimpleValueBond) message.getUserBond().get();

	    Assert.assertTrue(bond.getPropertyValue().equals("pippo ciccio"));

	} catch (GSException e) {

	    fail("Exception thrown");
	}
    }

    @Test
    public void testBondsCreationStExactSearch() {

	String value = "http://opensearch?si=10&ct=50&st=pippo ciccio&searchFields=title&outputFormat="
		+ NameSpace.GS_DATA_MODEL_XML_MEDIA_TYPE;

	WebRequest webRequest = WebRequest.createGET(value);

	OSRequestTransformer transformer = createTransformer();

	try {

	    DiscoveryMessage message = transformer.transform(webRequest);

	    SimpleValueBond bond = (SimpleValueBond) message.getUserBond().get();

	    Assert.assertTrue(bond.getPropertyValue().equals("pippo ciccio"));

	} catch (GSException e) {

	    fail("Exception thrown");
	}
    }

    @Test
    public void testBondsCreationStOr() {

	String value = "http://opensearch?si=10&ct=50&st=pippo OR ciccio&searchFields=title&outputFormat="
		+ NameSpace.GS_DATA_MODEL_XML_MEDIA_TYPE;

	WebRequest webRequest = WebRequest.createGET(value);

	OSRequestTransformer transformer = createTransformer();

	try {

	    DiscoveryMessage message = transformer.transform(webRequest);

	    LogicalBond bond = (LogicalBond) message.getUserBond().get();

	    Assert.assertTrue(bond.getLogicalOperator() == LogicalOperator.OR);

	    List<String> list = bond.getOperands().//
		    stream().//
		    map(op -> ((SimpleValueBond) op).getPropertyValue()).//
		    sorted().//
		    collect(Collectors.toList());

	    Assert.assertEquals(2, list.size());

	    Assert.assertEquals("ciccio", list.get(0));
	    Assert.assertEquals("pippo", list.get(1));

	} catch (GSException e) {

	    fail("Exception thrown");
	}
    }

    @Test
    public void testBondsCreationStAnd() {

	String value = "http://opensearch?si=10&ct=50&st=pippo AND ciccio AND pluto&searchFields=title&outputFormat="
		+ NameSpace.GS_DATA_MODEL_XML_MEDIA_TYPE;

	WebRequest webRequest = WebRequest.createGET(value);

	OSRequestTransformer transformer = createTransformer();

	try {

	    DiscoveryMessage message = transformer.transform(webRequest);

	    LogicalBond bond = (LogicalBond) message.getUserBond().get();

	    Assert.assertTrue(bond.getLogicalOperator() == LogicalOperator.AND);

	    List<String> list = bond.getOperands().//
		    stream().//
		    map(op -> ((SimpleValueBond) op).getPropertyValue()).//
		    sorted().//
		    collect(Collectors.toList());

	    Assert.assertEquals(3, list.size());

	    Assert.assertEquals("ciccio", list.get(0));
	    Assert.assertEquals("pippo", list.get(1));
	    Assert.assertEquals("pluto", list.get(2));

	} catch (GSException e) {

	    fail("Exception thrown");
	}
    }

    @Test
    public void testBondsCreationNoParameter() {

	String value = "http://opensearch?si=10&ct=50&outputFormat=" + NameSpace.GS_DATA_MODEL_XML_MEDIA_TYPE;
	WebRequest webRequest = WebRequest.createGET(value);

	OSRequestTransformer transformer = createTransformer();

	try {

	    DiscoveryMessage message = transformer.transform(webRequest);

	    // get the bond created by the transformer and creates a parser
	    Optional<Bond> opt = message.getUserBond();
	    Bond bond = opt.isPresent() ? opt.get() : null;

	    // creates a bond handler which creates an OS query string from the bonds
	    OSBondHandler osBondHandler = new OSBondHandler();

	    // parses the bond
	    DiscoveryBondParser bondParser = new DiscoveryBondParser(bond);
	    bondParser.parse(osBondHandler);

	    // get the query string
	    String queryString = osBondHandler.getQueryString();

	    // creates a key value parser from the string
	    KeyValueParser keyValueParser = new KeyValueParser(queryString);

	    // creates the OS parser with the given key value parser
	    OSRequestParser osRequestReader = new OSRequestParser(keyValueParser);

	} catch (GSException e) {

	    fail("Exception thrown");
	}
    }

    private OSRequestTransformer createTransformer() {
	OSRequestTransformer transformer = new OSRequestTransformer(new OSProfilerSetting()) {
	    @Override
	    public DiscoveryMessage transform(WebRequest request) throws GSException {

		DiscoveryMessage message = new DiscoveryMessage();

		message.setWebRequest(request);

		message.setPage(getPage(request));
		message.setUserBond(getUserBond(request));

		// ----------------
		// this is skipped since due to the missing DB an error would be throwed
		// ---------------

		// List<GSSource> sources = getSources(request);
		// if (sources.isEmpty()) {
		// sources = getAllSources();
		// }
		// message.setSources(sources);

		return message;
	    }
	};
	return transformer;
    }
}
