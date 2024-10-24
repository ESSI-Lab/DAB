package eu.essi_lab.accessor.cmr.bond;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.accessor.cmr.distributed.CMRGranulesBondHandler;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author ilsanto
 */
public class CMRGranulesBondHandlerTest {

    @Test
    public void test() {

	// (tmpExtentEnd <= 2018-12-04T00:00:00.000Z
	// AND TF bond: [sourceId, keyword, format, protocol]
	// AND parentId = 8a385ca5-22ba-4ec1-ae79-4860c86ca93a
	// AND tmpExtentBegin >= 2016-12-01T00:00:00.000Z)

	LogicalBond andBond = Mockito.mock(LogicalBond.class);

	LogicalBond.LogicalOperator andOprator = LogicalBond.LogicalOperator.AND;

	Mockito.doReturn(andOprator).when(andBond).getLogicalOperator();

	SimpleValueBond bond3 = Mockito.mock(SimpleValueBond.class);

	MetadataElement parentElement = MetadataElement.PARENT_IDENTIFIER;

	Mockito.doReturn(parentElement).when(bond3).getProperty();

	String parentid = "parentid";
	Mockito.doReturn(parentid).when(bond3).getPropertyValue();

	SimpleValueBond bond4 = Mockito.mock(SimpleValueBond.class);

	MetadataElement start = MetadataElement.TEMP_EXTENT_BEGIN;

	Mockito.doReturn(start).when(bond4).getProperty();

	String startTime = "2016-12-01T00:00:00.000Z";
	Mockito.doReturn(startTime).when(bond4).getPropertyValue();

	SimpleValueBond bond1 = Mockito.mock(SimpleValueBond.class);

	MetadataElement end = MetadataElement.TEMP_EXTENT_END;

	Mockito.doReturn(end).when(bond1).getProperty();

	String endTime = "2018-12-04T00:00:00.000Z";
	Mockito.doReturn(endTime).when(bond1).getPropertyValue();

	List<Bond> ands = Mockito.spy(new ArrayList<Bond>());

	Bond[] arr = new Bond[] { bond1, bond3, bond4 };

	Mockito.doReturn(arr).when(ands).toArray((Bond[])Mockito.any());

	Mockito.doReturn(ands).when(andBond).getOperands();

	String template = "http://cwic.wgiss.ceos.org/opensearch/granules.atom?datasetId=C1238517253-GES_DISC&startIndex={startIndex?}&count={count?}&timeStart={time:start}&timeEnd={time:end}&geoBox={geo:box}&clientId=gs-service";
	CMRGranulesBondHandler handler = new CMRGranulesBondHandler(template);

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	Mockito.doReturn(andBond).when(message).getPermittedBond();

	DiscoveryBondParser parser = new DiscoveryBondParser(message);

	parser.parse(handler);

	handler.setCount(10);
	handler.setStart(1);

	String query = handler.getQueryString();

	Assert.assertEquals(
		"http://cwic.wgiss.ceos.org/opensearch/granules.atom?datasetId=C1238517253-GES_DISC&clientId=gs-service&timeStart=2016-12-01T00:00:00Z&timeEnd=2018-12-04T00:00:00Z&count=10&startIndex=1&",
		query);
    }
}