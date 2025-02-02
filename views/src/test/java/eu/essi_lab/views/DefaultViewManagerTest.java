package eu.essi_lab.views;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.jaxb.ViewFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;

public class DefaultViewManagerTest {

    private DefaultViewManager manager;
    private SimpleValueBond view1;
    private SimpleValueBond view2;
    private LogicalBond view3;
    private LogicalBond view4;

    @Before
    public void init() throws GSException {
	this.manager = Mockito.spy(new DefaultViewManager());
	this.view1 = BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "temperature");
	this.view2 = BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "discharge");
	this.view3 = BondFactory.createAndBond(BondFactory.createViewBond("view1"), BondFactory.createViewBond("view2"));
	this.view4 = BondFactory.createOrBond(view3, view1);
	ViewFactory vf = new ViewFactory();
	Mockito.doReturn(Optional.of(vf.createView("view1", "", view1))).when(manager).getView("view1");
	Mockito.doReturn(Optional.of(vf.createView("view2", "", view2))).when(manager).getView("view2");
	Mockito.doReturn(Optional.of(vf.createView("view3", "", view3))).when(manager).getView("view3");
	Mockito.doReturn(Optional.of(vf.createView("view4", "", view4))).when(manager).getView("view4");
    }

    @Test
    public void testResolve() throws Exception {

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
