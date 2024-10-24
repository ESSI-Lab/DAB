//package eu.essi_lab.accessor.nextgeoss.bond;
//
//import java.util.ArrayList;
//
//import org.junit.Assert;
//import org.junit.Test;
//import org.mockito.Mockito;
//
//import eu.essi_lab.accessor.nextgeoss.bond.NextGEOSSGranulesBondHandler;
//import eu.essi_lab.messages.DiscoveryMessage;
//import eu.essi_lab.messages.bond.Bond;
//import eu.essi_lab.messages.bond.LogicalBond;
//import eu.essi_lab.messages.bond.SimpleValueBond;
//import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
//import eu.essi_lab.model.resource.MetadataElement;
//
///**
// * @author ilsanto
// */
//public class FEDOGranulesBondHandlerTest {
//
//    @Test
//    public void test() {
//
//	// (tmpExtentEnd <= 2018-12-04T00:00:00.000Z
//	// AND TF bond: [sourceId, keyword, format, protocol]
//	// AND parentId = 8a385ca5-22ba-4ec1-ae79-4860c86ca93a
//	// AND tmpExtentBegin >= 2016-12-01T00:00:00.000Z)
//
//	LogicalBond andBond = Mockito.mock(LogicalBond.class);
//
//	LogicalBond.LogicalOperator andOprator = LogicalBond.LogicalOperator.AND;
//
//	Mockito.doReturn(andOprator).when(andBond).getLogicalOperator();
//
//	SimpleValueBond bond3 = Mockito.mock(SimpleValueBond.class);
//
//	MetadataElement parentElement = MetadataElement.PARENT_IDENTIFIER;
//
//	Mockito.doReturn(parentElement).when(bond3).getProperty();
//
//	String parentid = "parentid";
//	Mockito.doReturn(parentid).when(bond3).getPropertyValue();
//
//	SimpleValueBond bond4 = Mockito.mock(SimpleValueBond.class);
//
//	MetadataElement start = MetadataElement.TEMP_EXTENT_BEGIN;
//
//	Mockito.doReturn(start).when(bond4).getProperty();
//
//	String startTime = "2000-01-01T00:00:00Z";
//	Mockito.doReturn(startTime).when(bond4).getPropertyValue();
//
//	SimpleValueBond bond1 = Mockito.mock(SimpleValueBond.class);
//
//	MetadataElement end = MetadataElement.TEMP_EXTENT_END;
//
//	Mockito.doReturn(end).when(bond1).getProperty();
//
//	String endTime = "2018-01-31T00:00:00Z";
//	Mockito.doReturn(endTime).when(bond1).getPropertyValue();
//
//	ArrayList<Bond> ands = Mockito.spy(new ArrayList<>());
//
//	Bond[] arr = new Bond[] { bond1, bond3, bond4 };
//
//	Mockito.doReturn(arr).when(ands).toArray(Mockito.any());
//
//	Mockito.doReturn(ands).when(andBond).getOperands();
//
//	String template = "https://fedeo.esa.int/opensearch/request?httpAccept=application%2Fatom%2Bxml&parentIdentifier=EOP:SSARA&startRecord={startRecord?}&maximumRecords={maximumRecords?}&startDate={time:start}&endDate={time:end}&bbox={geo:box}&clientId=gs-service";
//	// "http://cwic.wgiss.ceos.org/opensearch/granules.atom?datasetId=C1238517253-GES_DISC&startIndex={startIndex?}&count={count?}&timeStart={time:start}&timeEnd={time:end}&geoBox={geo:box}&clientId=gs-service";
//	// https://fedeo.esa.int/opensearch/request?httpAccept=application/atom%2Bxml&parentIdentifier=EOP:SSARA&startDate=2000-01-01T00:00:00Z&endDate=2018-01-31T00:00:00Z
//	// http://fedeo.esa.int/opensearch/request?httpAccept=application%2Fatom%2Bxml&parentIdentifier=EOP:SSARA&startRecord={startRecord?}&maximumRecords={maximumRecords?}&startDate={time:start}&endDate={time:end}&bbox={geo:box}&clientId=gs-service";
//	NextGEOSSGranulesBondHandler handler = new NextGEOSSGranulesBondHandler(template);
//
//	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);
//
//	Mockito.doReturn(andBond).when(message).getPermittedBond();
//
//	DiscoveryBondParser parser = new DiscoveryBondParser(message);
//
//	parser.parse(handler);
//
//	handler.setCount(10);
//	handler.setStart(1);
//
//	String query = handler.getQueryString();
//	Assert.assertEquals(
//		"https://fedeo.esa.int/opensearch/request?httpAccept=application%2Fatom%2Bxml&parentIdentifier=EOP:SSARA&startDate=2000-01-01T00:00:00Z&endDate=2018-01-31T00:00:00Z&maximumRecords=10&startRecord=1&",
//		query);
//	// Assert.assertEquals("http://cwic.wgiss.ceos.org/opensearch/granules.atom?datasetId=C1238517253-GES_DISC&timeStart=2016-12-01T00:00:00Z&timeEnd=2018-12-04T00:00:00Z&count=10&startIndex=1&",
//	// query);
//    }
//}