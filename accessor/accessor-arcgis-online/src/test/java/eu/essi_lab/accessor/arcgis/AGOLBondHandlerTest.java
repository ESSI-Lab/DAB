package eu.essi_lab.accessor.arcgis;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.accessor.arcgis.handler.AGOLBondHandler;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.bond.spatial.SpatialExtent;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author ilsanto
 */
public class AGOLBondHandlerTest {

    @Test
    public void nobond() {

	AGOLBondHandler handler = new AGOLBondHandler();

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	DiscoveryBondParser parser = new DiscoveryBondParser(message);

	parser.parse(handler);

	String query = handler.getQueryString();

	Assert.assertEquals("f=json&bbox=-180,-90,180,90", query);
    }

    @Test
    public void bboxOnly() {

	AGOLBondHandler handler = new AGOLBondHandler();

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	SpatialBond bond = Mockito.mock(SpatialBond.class);

	SpatialExtent bbox = Mockito.mock(SpatialExtent.class);

	double east = 40;
	double weast = -12;
	double north = 30;
	double south = 1;

	Mockito.doReturn(east).when(bbox).getEast();

	Mockito.doReturn(weast).when(bbox).getWest();

	Mockito.doReturn(north).when(bbox).getNorth();

	Mockito.doReturn(south).when(bbox).getSouth();

	Mockito.doReturn(bbox).when(bond).getPropertyValue();

	Mockito.doReturn(bond).when(message).getPermittedBond();

	DiscoveryBondParser parser = new DiscoveryBondParser(message);

	parser.parse(handler);

	String query = handler.getQueryString();

	Assert.assertEquals("f=json&bbox=" + weast + "%2C" + south + "%2C" + east + "%2C" + north + "&", query);
    }

    @Test
    public void bboxAndTitle() {

	AGOLBondHandler handler = new AGOLBondHandler();

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	SpatialBond spatialBond = Mockito.mock(SpatialBond.class);

	SpatialExtent bbox = Mockito.mock(SpatialExtent.class);

	double east = 40;
	double weast = -12;
	double north = 30;
	double south = 1;

	Mockito.doReturn(east).when(bbox).getEast();

	Mockito.doReturn(weast).when(bbox).getWest();

	Mockito.doReturn(north).when(bbox).getNorth();

	Mockito.doReturn(south).when(bbox).getSouth();

	Mockito.doReturn(bbox).when(spatialBond).getPropertyValue();

	LogicalBond andBond = Mockito.mock(LogicalBond.class);

	LogicalBond.LogicalOperator andOprator = LogicalBond.LogicalOperator.AND;

	Mockito.doReturn(andOprator).when(andBond).getLogicalOperator();

	List<Bond> operands = new ArrayList<Bond>();

	operands.add(spatialBond);

	SimpleValueBond titleBond = Mockito.mock(SimpleValueBond.class);

	String titleValue = "ozone";

	Mockito.doReturn(titleValue).when(titleBond).getPropertyValue();

	MetadataElement titleElement = MetadataElement.TITLE;

	Mockito.doReturn(titleElement).when(titleBond).getProperty();

	operands.add(titleBond);

	Mockito.doReturn(operands).when(andBond).getOperands();

	Mockito.doReturn(andBond).when(message).getPermittedBond();

	DiscoveryBondParser parser = new DiscoveryBondParser(message);

	parser.parse(handler);

	String query = handler.getQueryString();

	Assert.assertEquals("f=json&bbox=" + weast + "%2C" + south + "%2C" + east + "%2C" + north + "&q=(title:ozone%20OR%20tags:ozone)",
		query);
    }

    @Test
    public void bboxAndTitleInvOperands() {

	AGOLBondHandler handler = new AGOLBondHandler();

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	SpatialBond spatialBond = Mockito.mock(SpatialBond.class);

	SpatialExtent bbox = Mockito.mock(SpatialExtent.class);

	double east = 40;
	double weast = -12;
	double north = 30;
	double south = 1;

	Mockito.doReturn(east).when(bbox).getEast();

	Mockito.doReturn(weast).when(bbox).getWest();

	Mockito.doReturn(north).when(bbox).getNorth();

	Mockito.doReturn(south).when(bbox).getSouth();

	Mockito.doReturn(bbox).when(spatialBond).getPropertyValue();

	LogicalBond andBond = Mockito.mock(LogicalBond.class);

	LogicalBond.LogicalOperator andOprator = LogicalBond.LogicalOperator.AND;

	Mockito.doReturn(andOprator).when(andBond).getLogicalOperator();

	ArrayList<Bond> operands = Mockito.spy(new ArrayList<>());

	SimpleValueBond titleBond = Mockito.mock(SimpleValueBond.class);

	String titleValue = "ozone";

	Mockito.doReturn(titleValue).when(titleBond).getPropertyValue();

	MetadataElement titleElement = MetadataElement.TITLE;

	Mockito.doReturn(titleElement).when(titleBond).getProperty();

	Mockito.doReturn(operands).when(andBond).getOperands();

	Bond[] arr = new Bond[] { spatialBond, titleBond };

	Mockito.doReturn(arr).when(operands).toArray((Bond[])Mockito.any());

	Mockito.doReturn(andBond).when(message).getPermittedBond();

	DiscoveryBondParser parser = new DiscoveryBondParser(message);

	parser.parse(handler);

	String query = handler.getQueryString();

	Assert.assertEquals("f=json&bbox=" + weast + "%2C" + south + "%2C" + east + "%2C" + north + "&q=(title:ozone%20OR%20tags:ozone)",
		query);
    }

    @Test
    public void bboxAndOrComplex() {

	AGOLBondHandler handler = new AGOLBondHandler();

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	LogicalBond andBond1 = Mockito.mock(LogicalBond.class);

	LogicalBond.LogicalOperator andOprator1 = LogicalBond.LogicalOperator.AND;

	Mockito.doReturn(andOprator1).when(andBond1).getLogicalOperator();

	LogicalBond andBond2 = Mockito.mock(LogicalBond.class);

	LogicalBond.LogicalOperator andOprator2 = LogicalBond.LogicalOperator.AND;

	Mockito.doReturn(andOprator2).when(andBond2).getLogicalOperator();

	LogicalBond orBond = Mockito.mock(LogicalBond.class);

	LogicalBond.LogicalOperator orOprator = LogicalBond.LogicalOperator.OR;

	Mockito.doReturn(orOprator).when(orBond).getLogicalOperator();

	List<Bond> ands = Mockito.spy(new ArrayList<>());

	Bond[] arr = new Bond[] { andBond1, andBond2 };

	Mockito.doReturn(arr).when(ands).toArray((Bond[])Mockito.any());

	Mockito.doReturn(ands).when(orBond).getOperands();

	List<Bond> operands1 = Mockito.spy(new ArrayList<>());

	SimpleValueBond titleBond = Mockito.mock(SimpleValueBond.class);

	String titleValue = "ozone";

	Mockito.doReturn(titleValue).when(titleBond).getPropertyValue();

	MetadataElement titleElement = MetadataElement.TITLE;

	Mockito.doReturn(titleElement).when(titleBond).getProperty();

	Bond[] arr1 = new Bond[] { titleBond };

	Mockito.doReturn(arr1).when(operands1).toArray((Bond[])Mockito.any());

	Mockito.doReturn(operands1).when(andBond1).getOperands();

	ArrayList<Bond> operands2 = Mockito.spy(new ArrayList<>());

	SimpleValueBond subjBond = Mockito.mock(SimpleValueBond.class);

	String subjValue = "ozone";

	Mockito.doReturn(subjValue).when(subjBond).getPropertyValue();

	MetadataElement subjElement = MetadataElement.SUBJECT;

	Mockito.doReturn(subjElement).when(subjBond).getProperty();

	Bond[] arr2 = new Bond[] { subjBond };

	Mockito.doReturn(arr2).when(operands2).toArray((Bond[])Mockito.any());

	Mockito.doReturn(operands2).when(andBond2).getOperands();

	Mockito.doReturn(orBond).when(message).getPermittedBond();

	DiscoveryBondParser parser = new DiscoveryBondParser(message);

	parser.parse(handler);

	String query = handler.getQueryString();

	Assert.assertEquals("f=json&q=(title:ozone%20OR%20tags:ozone)", query);

    }

}