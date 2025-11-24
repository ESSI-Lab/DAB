package eu.essi_lab.messages.bond;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.model.resource.ResourceProperty;

public class DynamicViewTest {

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String S1 = "s1";

    // gs-view-and(v1,v2)
    @Test
    public void test1() {
	String id = DynamicView.RESERVED_PREFIX + DynamicViewAnd.AND_PREFIX + DynamicView.ARGUMENT_START + V1
		+ DynamicView.ARGUMENT_SEPARATOR + V2 + DynamicView.ARGUMENT_END;
	DynamicView view = DynamicView.resolveDynamicView(id).get();
	Bond bond = view.getBond();
	if (bond instanceof LogicalBond andBond) {
	    LogicalOperator operator = andBond.getLogicalOperator();
	    if (operator.equals(LogicalOperator.AND)) {
		List<Bond> operands = andBond.getOperands();
		Assert.assertEquals(2, operands.size());
		Bond operand1 = operands.get(0);
		if (operand1 instanceof ViewBond viewBond) {
		    assertEquals(V1, viewBond.getViewIdentifier());
		} else {
		    Assert.fail("Not a view bond");
		}
		Bond operand2 = operands.get(1);
		if (operand2 instanceof ViewBond viewBond) {
		    assertEquals(V2, viewBond.getViewIdentifier());
		} else {
		    Assert.fail("Not a view bond");
		}
	    } else {
		Assert.fail("not a AND bond");
	    }
	} else {
	    Assert.fail("not a logical bond");
	}

    }

    // gs-view-and(v1,gs-view-source(s1))
    @Test
    public void test2() {
	String id = DynamicView.RESERVED_PREFIX + DynamicViewAnd.AND_PREFIX + DynamicView.ARGUMENT_START + V1
		+ DynamicView.ARGUMENT_SEPARATOR + DynamicView.RESERVED_PREFIX + DynamicViewSource.SOURCE_PREFIX
		+ DynamicView.ARGUMENT_START + S1 + DynamicView.ARGUMENT_END + DynamicView.ARGUMENT_END;
	DynamicView view = DynamicView.resolveDynamicView(id).get();
	Bond bond = view.getBond();
	assertTest2Bond(bond);

    }

    private void assertTest2Bond(Bond bond) {
	if (bond instanceof LogicalBond andBond) {
	    LogicalOperator operator = andBond.getLogicalOperator();
	    if (operator.equals(LogicalOperator.AND)) {
		List<Bond> operands = andBond.getOperands();
		Assert.assertEquals(2, operands.size());
		Bond operand1 = operands.get(0);
		if (operand1 instanceof ViewBond viewBond) {
		    assertEquals(V1, viewBond.getViewIdentifier());
		} else {
		    Assert.fail("Not a view bond");
		}
		Bond operand2 = operands.get(1);
		if (operand2 instanceof ResourcePropertyBond sourceBond) {
		    assertEquals(S1, sourceBond.getPropertyValue());
		    assertEquals(ResourceProperty.SOURCE_ID, sourceBond.getProperty());
		    assertEquals(BondOperator.EQUAL, sourceBond.getOperator());
		} else {
		    Assert.fail("Not a view bond");
		}
	    } else {
		Assert.fail("not a AND bond");
	    }
	} else {
	    Assert.fail("not a logical bond");
	}

    }

    // gs-view-and(v2,gs-view-and(v1,gs-view-source(s1)))
    @Test
    public void test3() {
	String id = DynamicView.RESERVED_PREFIX + DynamicViewAnd.AND_PREFIX + DynamicView.ARGUMENT_START + V2
		+ DynamicView.ARGUMENT_SEPARATOR + DynamicView.RESERVED_PREFIX + DynamicViewAnd.AND_PREFIX + DynamicView.ARGUMENT_START + V1
		+ DynamicView.ARGUMENT_SEPARATOR + DynamicView.RESERVED_PREFIX + DynamicViewSource.SOURCE_PREFIX
		+ DynamicView.ARGUMENT_START + S1 + DynamicView.ARGUMENT_END + DynamicView.ARGUMENT_END + DynamicView.ARGUMENT_END;
	DynamicView view = DynamicView.resolveDynamicView(id).get();
	Bond bond = view.getBond();
	if (bond instanceof LogicalBond andBond) {
	    LogicalOperator operator = andBond.getLogicalOperator();
	    if (operator.equals(LogicalOperator.AND)) {
		List<Bond> operands = andBond.getOperands();
		Assert.assertEquals(2, operands.size());
		Bond operand1 = operands.get(0);
		if (operand1 instanceof ViewBond viewBond) {
		    assertEquals(V2, viewBond.getViewIdentifier());
		} else {
		    Assert.fail("Not a view bond");
		}
		Bond operand2 = operands.get(1);
		assertTest2Bond(operand2);
	    } else {
		Assert.fail("not a AND bond");
	    }
	} else {
	    Assert.fail("not a logical bond");
	}

    }

}
