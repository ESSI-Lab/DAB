package eu.essi_lab.cfga.database.test;

import eu.essi_lab.api.database.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.messages.bond.jaxb.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.model.resource.*;
import org.junit.*;
import org.mockito.*;

import java.util.*;

import static org.junit.Assert.*;

public class ViewManagerTest {

    private ViewManager manager;
    private SimpleValueBond view1;
    private SimpleValueBond view2;
    private LogicalBond view3;
    private LogicalBond view4;

    @Before
    public void init() throws GSException {
	this.manager = Mockito.spy(new ViewManager());
	this.view1 = BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "temperature");
	this.view2 = BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "discharge");
	this.view3 = BondFactory.createAndBond(BondFactory.createViewBond("view1"), BondFactory.createViewBond("view2"));
	this.view4 = BondFactory.createOrBond(view3, view1);

	Mockito.doReturn(Optional.of(ViewFactory.createView("view1", "", view1))).when(manager).getView("view1");
	Mockito.doReturn(Optional.of(ViewFactory.createView("view2", "", view2))).when(manager).getView("view2");
	Mockito.doReturn(Optional.of(ViewFactory.createView("view3", "", view3))).when(manager).getView("view3");
	Mockito.doReturn(Optional.of(ViewFactory.createView("view4", "", view4))).when(manager).getView("view4");
    }

    @Test
    public void testResolve() throws Exception {

	ConfigurationWrapper.setConfiguration(new SimpleConfiguration());

	assertEquals(view1, manager.getView("view1").get().getBond());
	assertEquals(view1, manager.getResolvedView("view1").get().getBond());
	assertEquals(view2, manager.getView("view2").get().getBond());
	assertEquals(view2, manager.getResolvedView("view2").get().getBond());
	assertEquals(view3, manager.getView("view3").get().getBond());
	assertEquals(BondFactory.createAndBond(view1, view2), manager.getResolvedView("view3").get().getBond());
	assertEquals(view4, manager.getView("view4").get().getBond());
	assertEquals(BondFactory.createOrBond(BondFactory.createAndBond(view1, view2), view1),
		manager.getResolvedView("view4").get().getBond());

    }
}
