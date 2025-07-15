/**
 * 
 */
package eu.essi_lab.messages.bond;

import java.util.Arrays;
import java.util.List;

import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.composed.ComposedElementItem;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class ComposedElementBond extends QueryableBond<List<ComposedElementItem>> {

    private LogicalOperator logicalOp;

    /**
     * 
     */
    public ComposedElementBond() {

    }

    /**
     * @param operator
     * @param element
     * @param value
     */
    ComposedElementBond(BondOperator operator, MetadataElement element, ComposedElementItem... value) {

	this(operator, LogicalOperator.AND, element, value);
    }

    /**
     * @param operator
     * @param element
     * @param value
     */
    ComposedElementBond(BondOperator operator, LogicalOperator logicalOp, MetadataElement element, ComposedElementItem... value) {

	this.logicalOp = logicalOp;

	setOperator(operator);
	setPropertyValue(Arrays.asList(value));
	setProperty(element);
    }

    /**
     * @return
     */
    public LogicalOperator getLogicalOp() {

	return logicalOp;
    }

    @Override
    public MetadataElement getProperty() {

	return (MetadataElement) super.getProperty();
    }

    @Override
    public ComposedElementBond clone() {

	ComposedElementBond clone = new ComposedElementBond();

	clone.setOperator(getOperator());
	clone.setProperty(getProperty());
	clone.setPropertyValue(getPropertyValue());
	clone.logicalOp = getLogicalOp();

	return clone;
    }
}
