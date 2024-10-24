package eu.essi_lab.accessor.stac;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.accessor.stac.distributed.STACGranulesBondHandler;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author ilsanto
 */
public class STACGranulesBondHandlerTest {

    @Test
    public void test() {

	// (TF bond: [sourceId, keyword, format, protocol]
	// AND parentId = 06156594-c45a-4adf-a5b6-acd3257f6886)

	LogicalBond andBond = Mockito.mock(LogicalBond.class);

	LogicalBond.LogicalOperator andOprator = LogicalBond.LogicalOperator.AND;

	Mockito.doReturn(andOprator).when(andBond).getLogicalOperator();

	SimpleValueBond bond2 = Mockito.mock(SimpleValueBond.class);

	MetadataElement parentElement = MetadataElement.PARENT_IDENTIFIER;

	Mockito.doReturn(parentElement).when(bond2).getProperty();

	String parentid = "parentid";
	Mockito.doReturn(parentid).when(bond2).getPropertyValue();

	List<Bond> ands = Mockito.spy(new ArrayList<Bond>());

	Bond[] arr = new Bond[] { bond2 };

	Mockito.doReturn(arr).when(ands).toArray((Bond[])Mockito.any());

	Mockito.doReturn(ands).when(andBond).getOperands();

	String node = "ndvi_climatology_ls";
	STACGranulesBondHandler handler = new STACGranulesBondHandler(node);

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	Mockito.doReturn(andBond).when(message).getPermittedBond();

	DiscoveryBondParser parser = new DiscoveryBondParser(message);

	parser.parse(handler);

	handler.setCount(10);
	handler.setStart(1);

	String query = handler.getQueryString();

	Assert.assertEquals("collections=ndvi_climatology_ls&_o=0&limit=10&", query);
    }

    @Test
    public void test2() {

	// https://explorer.digitalearth.africa/stac/search?collections=ndvi_climatology_ls&_o=0&limit=10&bbox=-5.411,53.105,-0.489,58.203&
	// (TF bond: [sourceId, keyword, format, protocol]
	// AND parentId = 06156594-c45a-4adf-a5b6-acd3257f6886)
	// AND bbox = west=-5.411 south=53.105 east=-0.489 north=58.203


	LogicalBond andBond = Mockito.mock(LogicalBond.class);

	LogicalBond.LogicalOperator andOprator = LogicalBond.LogicalOperator.AND;

	Mockito.doReturn(andOprator).when(andBond).getLogicalOperator();

	SimpleValueBond bond2 = Mockito.mock(SimpleValueBond.class);

	MetadataElement parentElement = MetadataElement.PARENT_IDENTIFIER;

	Mockito.doReturn(parentElement).when(bond2).getProperty();

	String parentid = "parentid";
	Mockito.doReturn(parentid).when(bond2).getPropertyValue();

	SpatialBond bond3 = Mockito.mock(SpatialBond.class);

	SpatialExtent bbox = Mockito.mock(SpatialExtent.class);

	double east = -0.489;
	double weast = -5.411;
	double north = 58.203;
	double south = 53.105;

	Mockito.doReturn(east).when(bbox).getEast();

	Mockito.doReturn(weast).when(bbox).getWest();

	Mockito.doReturn(north).when(bbox).getNorth();

	Mockito.doReturn(south).when(bbox).getSouth();

	Mockito.doReturn(bbox).when(bond3).getPropertyValue();

	List<Bond> ands = Mockito.spy(new ArrayList<Bond>());

	Bond[] arr = new Bond[] { bond2, bond3 };

	Mockito.doReturn(arr).when(ands).toArray((Bond[])Mockito.any());

	Mockito.doReturn(ands).when(andBond).getOperands();

	String node = "ndvi_climatology_ls";
	STACGranulesBondHandler handler = new STACGranulesBondHandler(node);

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	Mockito.doReturn(andBond).when(message).getPermittedBond();

	DiscoveryBondParser parser = new DiscoveryBondParser(message);

	parser.parse(handler);

	handler.setCount(10);
	handler.setStart(1);

	String query = handler.getQueryString();

	Assert.assertEquals(
		"collections=ndvi_climatology_ls&_o=0&limit=10&bbox=-5.411,53.105,-0.489,58.203&",
		query);
    }

    @Test
    public void test3() {

	// https://explorer.digitalearth.africa/stac/search?collections=ndvi_climatology_ls&_o=0&limit=10&datetime=2016-12-01T00:00:00.000Z&

	// (TF bond: [sourceId, keyword, format, protocol]
	// AND parentId = 06156594-c45a-4adf-a5b6-acd3257f6886)
	// AND bbox = west=-5.411 south=53.105 east=-0.489 north=58.203

	LogicalBond andBond = Mockito.mock(LogicalBond.class);

	LogicalBond.LogicalOperator andOprator = LogicalBond.LogicalOperator.AND;

	Mockito.doReturn(andOprator).when(andBond).getLogicalOperator();

	SimpleValueBond bond2 = Mockito.mock(SimpleValueBond.class);

	MetadataElement parentElement = MetadataElement.PARENT_IDENTIFIER;

	Mockito.doReturn(parentElement).when(bond2).getProperty();

	String parentid = "parentid";
	Mockito.doReturn(parentid).when(bond2).getPropertyValue();

	SimpleValueBond bondTimeStart = Mockito.mock(SimpleValueBond.class);

	MetadataElement start = MetadataElement.TEMP_EXTENT_BEGIN;

	Mockito.doReturn(start).when(bondTimeStart).getProperty();

	String startDay = "2016-12-01";

	String startTime = startDay + "T00:00:00.000Z";
	Mockito.doReturn(startTime).when(bondTimeStart).getPropertyValue();

	List<Bond> ands = Mockito.spy(new ArrayList<Bond>());

	Bond[] arr = new Bond[] { bond2, bondTimeStart };

	Mockito.doReturn(arr).when(ands).toArray((Bond[])Mockito.any());

	Mockito.doReturn(ands).when(andBond).getOperands();

	String node = "ndvi_climatology_ls";
	STACGranulesBondHandler handler = new STACGranulesBondHandler(node);

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	Mockito.doReturn(andBond).when(message).getPermittedBond();

	DiscoveryBondParser parser = new DiscoveryBondParser(message);

	parser.parse(handler);

	handler.setCount(10);
	handler.setStart(1);

	String query = handler.getQueryString();

	Assert.assertEquals("collections=ndvi_climatology_ls&_o=0&limit=10&datetime=" + startDay + "/..&", query);
    }

    @Test
    public void test4() {

	//https://explorer.digitalearth.africa/stac/search?collections=ndvi_climatology_ls&_o=0&limit=10&datetime=2016-12-01T00:00:00.000Z&

	// (TF bond: [sourceId, keyword, format, protocol]
	// AND parentId = 06156594-c45a-4adf-a5b6-acd3257f6886)
	// AND bbox = west=-5.411 south=53.105 east=-0.489 north=58.203

	LogicalBond andBond = Mockito.mock(LogicalBond.class);

	LogicalBond.LogicalOperator andOprator = LogicalBond.LogicalOperator.AND;

	Mockito.doReturn(andOprator).when(andBond).getLogicalOperator();

	SimpleValueBond bond2 = Mockito.mock(SimpleValueBond.class);

	MetadataElement parentElement = MetadataElement.PARENT_IDENTIFIER;

	Mockito.doReturn(parentElement).when(bond2).getProperty();

	String parentid = "parentid";
	Mockito.doReturn(parentid).when(bond2).getPropertyValue();

	SimpleValueBond bondTimeStart = Mockito.mock(SimpleValueBond.class);

	MetadataElement start = MetadataElement.TEMP_EXTENT_END;

	Mockito.doReturn(start).when(bondTimeStart).getProperty();

	String endDay = "2016-12-01";

	String startTime = endDay + "T00:00:00.000Z";
	Mockito.doReturn(startTime).when(bondTimeStart).getPropertyValue();

	List<Bond> ands = Mockito.spy(new ArrayList<Bond>());

	Bond[] arr = new Bond[] { bond2, bondTimeStart };

	Mockito.doReturn(arr).when(ands).toArray((Bond[])Mockito.any());

	Mockito.doReturn(ands).when(andBond).getOperands();

	String node = "ndvi_climatology_ls";
	STACGranulesBondHandler handler = new STACGranulesBondHandler(node);

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	Mockito.doReturn(andBond).when(message).getPermittedBond();

	DiscoveryBondParser parser = new DiscoveryBondParser(message);

	parser.parse(handler);

	handler.setCount(10);
	handler.setStart(1);

	String query = handler.getQueryString();

	Assert.assertEquals("collections=ndvi_climatology_ls&_o=0&limit=10&datetime=../2016-12-01&", query);
    }

    @Test
    public void test5() {

	LogicalBond andBond = Mockito.mock(LogicalBond.class);

	LogicalBond.LogicalOperator andOprator = LogicalBond.LogicalOperator.AND;

	Mockito.doReturn(andOprator).when(andBond).getLogicalOperator();

	SimpleValueBond bond2 = Mockito.mock(SimpleValueBond.class);

	MetadataElement parentElement = MetadataElement.PARENT_IDENTIFIER;

	Mockito.doReturn(parentElement).when(bond2).getProperty();

	String parentid = "parentid";
	Mockito.doReturn(parentid).when(bond2).getPropertyValue();

	SimpleValueBond endBond = Mockito.mock(SimpleValueBond.class);

	MetadataElement endelem = MetadataElement.TEMP_EXTENT_END;

	Mockito.doReturn(endelem).when(endBond).getProperty();

	String endDay = "2017-12-01";

	String endTime = endDay + "T00:00:00.000Z";
	Mockito.doReturn(endTime).when(endBond).getPropertyValue();

	SimpleValueBond bondTimeStart = Mockito.mock(SimpleValueBond.class);

	MetadataElement start = MetadataElement.TEMP_EXTENT_BEGIN;

	Mockito.doReturn(start).when(bondTimeStart).getProperty();

	String startDay = "2016-12-01";

	String startTime = startDay + "T00:00:00.000Z";
	Mockito.doReturn(startTime).when(bondTimeStart).getPropertyValue();

	List<Bond> ands = Mockito.spy(new ArrayList<Bond>());

	Bond[] arr = new Bond[] { bond2, endBond, bondTimeStart };

	Mockito.doReturn(arr).when(ands).toArray((Bond[])Mockito.any());

	Mockito.doReturn(ands).when(andBond).getOperands();

	String node = "ndvi_climatology_ls";
	STACGranulesBondHandler handler = new STACGranulesBondHandler(node);

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	Mockito.doReturn(andBond).when(message).getPermittedBond();

	DiscoveryBondParser parser = new DiscoveryBondParser(message);

	parser.parse(handler);

	handler.setCount(10);
	handler.setStart(1);

	String query = handler.getQueryString();

	Assert.assertEquals("collections=ndvi_climatology_ls&_o=0&limit=10&datetime=" + startDay + "/" + endDay + "&", query);
    }

    @Test
    public void test6() {

	LogicalBond andBond = Mockito.mock(LogicalBond.class);

	LogicalBond.LogicalOperator andOprator = LogicalBond.LogicalOperator.AND;

	Mockito.doReturn(andOprator).when(andBond).getLogicalOperator();

	SimpleValueBond bond2 = Mockito.mock(SimpleValueBond.class);

	MetadataElement parentElement = MetadataElement.PARENT_IDENTIFIER;

	Mockito.doReturn(parentElement).when(bond2).getProperty();

	String parentid = "parentid";
	Mockito.doReturn(parentid).when(bond2).getPropertyValue();

	SimpleValueBond endBond = Mockito.mock(SimpleValueBond.class);

	MetadataElement endelem = MetadataElement.TEMP_EXTENT_END;

	Mockito.doReturn(endelem).when(endBond).getProperty();

	String endDay = "2017-12-01";

	String endTime = endDay + "T00:00:00.000Z";
	Mockito.doReturn(endTime).when(endBond).getPropertyValue();

	SimpleValueBond bondTimeStart = Mockito.mock(SimpleValueBond.class);

	MetadataElement start = MetadataElement.TEMP_EXTENT_BEGIN;

	Mockito.doReturn(start).when(bondTimeStart).getProperty();

	String startDay = "2016-12-01";

	String startTime = startDay + "T00:00:00.000Z";
	Mockito.doReturn(startTime).when(bondTimeStart).getPropertyValue();

	SpatialBond bond3 = Mockito.mock(SpatialBond.class);

	SpatialExtent bbox = Mockito.mock(SpatialExtent.class);

	double east = -0.489;
	double weast = -5.411;
	double north = 58.203;
	double south = 53.105;

	Mockito.doReturn(east).when(bbox).getEast();

	Mockito.doReturn(weast).when(bbox).getWest();

	Mockito.doReturn(north).when(bbox).getNorth();

	Mockito.doReturn(south).when(bbox).getSouth();

	Mockito.doReturn(bbox).when(bond3).getPropertyValue();

	List<Bond> ands = Mockito.spy(new ArrayList<Bond>());

	Bond[] arr = new Bond[] { bond2, endBond, bondTimeStart, bond3 };

	Mockito.doReturn(arr).when(ands).toArray((Bond[])Mockito.any());

	Mockito.doReturn(ands).when(andBond).getOperands();

	String node = "ndvi_climatology_ls";
	STACGranulesBondHandler handler = new STACGranulesBondHandler(node);

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	Mockito.doReturn(andBond).when(message).getPermittedBond();

	DiscoveryBondParser parser = new DiscoveryBondParser(message);

	parser.parse(handler);

	handler.setCount(10);
	handler.setStart(1);

	String query = handler.getQueryString();

	Assert.assertEquals(
		"collections=ndvi_climatology_ls&_o=0&limit=10&bbox=-5.411,53.105,-0.489,58.203&datetime="
			+ startDay + "/" + endDay + "&",
		query);
    }

}