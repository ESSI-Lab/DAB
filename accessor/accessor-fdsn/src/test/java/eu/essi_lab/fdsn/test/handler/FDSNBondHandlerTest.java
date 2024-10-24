package eu.essi_lab.fdsn.test.handler;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.accessor.fdsn.handler.FDSNBondHandler;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.model.resource.MetadataElement;

public class FDSNBondHandlerTest {

    private final String oldPortalRequest = "&reqID=bITMvV1WCuZfT4PqwV8J18GL1GpI9Hh8&class=&subj=&relt=&si=1&st=&gdc=&loc=&w3w=&radio_group=on&bbox=&rel=&ts=&te=&trel=&ct=30&outputFormat=application%2Fatom%2Bxml&qic=&tccrv=&dcrv=&lipr=&lisr=&rat=&repasp=&q=&usrd=&uselim=&lac=&luc=&other=&evtOrd=Magnitude&minmag=1&maxmag=3&magt=ML&mind=2&maxd=8&sta=&sensor=&inpe-sat-name=&inpe-instr-name=&sources=&parents=&rela=&input-rela-select-name=skos%3Abroader";
    // includearrivals=false&minmag=1.0&maxmag=3.0&mindepth=2.0&maxdepth=8.0&magtype=ML&includeallmagnitudes=false&includeallorigins=false&limit=30&offset=1&orderby=MAGNITUDE
    private final String newPortalRequest = "reqID=9psvvw4ph961cct2rgvsoflxr&si=1&ct=10&st=&kwd=&frmt=&prot=&searchFields=&bbox=&rel=CONTAINS&tf=providerID,keyword,format,protocol&ts=2017-01-01T00:00:00Z&te=2017-03-01T00:00:00Z&targetId=&from=&until=&sources=&subj=&rela=&maxmag=5&minmag=3&evtOrd=magnitude&magt=ML&outputFormat=application/json&callback=jQuery111300031313141300977065_1488376093404&_=1488376093409";
    // includearrivals=false&minmag=3.0&maxmag=5.0&magtype=ML&includeallmagnitudes=false&includeallorigins=false&starttime=2017-01-01T00:00:00&endtime=2017-03-01T00:00:00&limit=10&offset=1&orderby=MAGNITUDE

    private LogicalBond logicalBond;

    // INTERESTING QUERY PARAMETER : maxmagnitude, minmagnitude, eventOrder (timae, magnitude...), magnitudeType
    // maxmag=5&minmag=3&evtOrd=magnitude&magt=ML

    @Before
    public void init() {

	ArrayList<Bond> firstBondList = new ArrayList<Bond>();

	String startTime = "2017-01-01T00:00:00Z";
	SimpleValueBond startTimeBond = BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.TEMP_EXTENT_BEGIN,
		startTime);
	firstBondList.add(startTimeBond);

	String endTime = "2017-03-01T00:00:00Z";
	SimpleValueBond endTimeBond = BondFactory.createSimpleValueBond(BondOperator.LESS_OR_EQUAL, MetadataElement.TEMP_EXTENT_END,
		endTime);
	firstBondList.add(endTimeBond);

	LogicalBond firstAndBond = BondFactory.createAndBond(firstBondList.toArray(new Bond[] {}));

	ArrayList<Bond> secondBondList = new ArrayList<Bond>();

	String minMagn = "3";
	String maxMagn = "5";
	String type = "ML";
	String depth = "10";

	SimpleValueBond maxMagnitude = BondFactory.createSimpleValueBond(BondOperator.LESS_OR_EQUAL, MetadataElement.QML_MAGNITUDE_VALUE,
		Double.valueOf(maxMagn));
	secondBondList.add(maxMagnitude);

	SimpleValueBond minMagnitude = BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.QML_MAGNITUDE_VALUE,
		Double.valueOf(minMagn));
	secondBondList.add(minMagnitude);

	SimpleValueBond magnitudeType = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.QML_MAGNITUDE_TYPE, type);
	secondBondList.add(magnitudeType);

	SimpleValueBond magnitudeDepth = BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.QML_DEPTH_VALUE,
		Double.valueOf(depth));
	secondBondList.add(magnitudeDepth);

	LogicalBond secondAndBond = BondFactory.createAndBond(secondBondList.toArray(new Bond[] {}));

	// ---------------------------------------
	// inserts the two and bonds in an or bond
	//
	ArrayList<Bond> logicalBondsList = new ArrayList<Bond>();
	logicalBondsList.add(firstAndBond);
	logicalBondsList.add(secondAndBond);

	logicalBond = BondFactory.createAndBond(logicalBondsList.toArray(new Bond[] {}));

    }

    @Test
    public void testBondHandler() {

	FDSNBondHandler bhandler = new FDSNBondHandler();

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);
	Bond reducedBond = Mockito.mock(Bond.class);
	ReducedDiscoveryMessage reducedMessage = new ReducedDiscoveryMessage(message, reducedBond);

	DiscoveryBondParser bondParser = new DiscoveryBondParser(logicalBond);
	bondParser.parse(bhandler);

    }

    @Test
    public void testAndBondHandler() {

	// FDSNBondHandler bhandler = new FDSNBondHandler();
	//
	// DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);
	// Bond reducedBond = Mockito.mock(Bond.class);
	// GSReducedDiscoveryMessage reducedMessage = new GSReducedDiscoveryMessage(message, reducedBond);
	//
	// BondParser bondParser = new BondParser(reducedMessage);
	// bondParser.parse(bhandler);

    }

}
