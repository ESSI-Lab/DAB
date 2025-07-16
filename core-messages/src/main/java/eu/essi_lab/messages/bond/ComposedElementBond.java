/**
 * 
 */
package eu.essi_lab.messages.bond;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
