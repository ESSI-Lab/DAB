package eu.essi_lab.messages.bond;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement
public class LogicalBond implements Bond {

    public enum LogicalOperator {
	AND, //
	OR, // ,
	NOT,//
    }

    @XmlElement
    private LogicalOperator logicalOperator;
    @XmlElements({ @XmlElement(name = "viewBond", type = ViewBond.class), //
	    @XmlElement(name = "resourcePropertyBond", type = ResourcePropertyBond.class), //
	    @XmlElement(name = "simpleValueBond", type = SimpleValueBond.class), //
	    @XmlElement(name = "spatialBond", type = SpatialBond.class), //
	    @XmlElement(name = "logicalBond", type = LogicalBond.class) })
    private List<Bond> operands = new ArrayList<Bond>();

    /**
     * No-arg constructor only to be used by JAXB
     */
    public LogicalBond() {

    }

    /**
     * Creates a new logical bond with the supplied <code>logicalOperator</code> and without operators
     * 
     * @param logicalOperator
     */
    LogicalBond(LogicalOperator logicalOperator) {
	this.logicalOperator = logicalOperator;
    }

    /**
     * Creates a new logical bond with the supplied <code>logicalOperator</code> and <code>operands</code>
     * 
     * @param logicalOperator
     * @param operands
     */
    LogicalBond(LogicalOperator logicalOperator, Bond... operands) {
	this(logicalOperator);
	for (Bond bond : operands) {
	    if (bond != null) {
		this.operands.add(bond);
	    }
	}
    }

    /**
     * Creates a new logical bond with the supplied <code>logicalOperator</code> and <code>operands</code>
     *
     * @param logicalOperator
     * @param operands
     */
    LogicalBond(LogicalOperator logicalOperator, Collection<Bond> operands) {
	this(logicalOperator);
	for (Bond bond : operands) {
	    if (bond != null) {
		this.operands.add(bond);
	    }
	}
    }

    /**
     * Creates a clone of this bond by replacing all the {@link QueryableBond} with the provided
     * <code>searchValues</code>, with {@link QueryableBond}s having the same
     * properties and the values provided by <code>replaceValues</code>. In particular the
     * value of
     * <code>searchValues</code> at index <code>i</code> is replaced by the value of <code>replaceValues</code> at the
     * same index (thus the lists
     * must have the same size)
     * 
     * @param searchValues
     * @param replaceValues
     * @return
     */
    public LogicalBond replaceWithValues(List<String> searchValues, List<String> replaceValues) {

	if (searchValues.size() != replaceValues.size()) {

	    throw new IllegalArgumentException("Lists have different size");
	}

	BondReplacer bondReplacer = new BondReplacer(this);
	return bondReplacer.replaceWithValues(searchValues, replaceValues);
    }

    /**
     * Creates a clone of this bond by replacing all the {@link QueryableBond} with the provided
     * <code>searchValues</code>, with {@link QueryableBond}s provided by <code>replaceValues</code>. In particular the
     * value of <code>searchValues</code> at index <code>i</code> is replaced by the bond of <code>replaceValues</code>
     * at the same index (thus the lists must have the same size)
     * 
     * @param searchValues
     * @param replaceValues
     * @return
     */
    public LogicalBond replaceWithBonds(List<String> searchValues, List<Bond> replaceValues) {

	if (searchValues.size() != replaceValues.size()) {

	    throw new IllegalArgumentException("Lists have different size");
	}

	BondReplacer bondReplacer = new BondReplacer(this);
	return bondReplacer.replaceWithBonds(searchValues, replaceValues);
    }

    /**
     * @return
     */
    public LogicalOperator getLogicalOperator() {
	return logicalOperator;
    }

    /**
     * Returns the ordered set of operands of this logical bond
     * 
     * @return
     */
    public List<Bond> getOperands() {
	return this.operands;
    }

    /**
     * @return
     */
    public Bond getFirstOperand() {

	return getOperands().iterator().next();
    }

    @Override
    public String toString() {
	String bonds = "";
	if (operands == null || operands.isEmpty()) {
	    return "EMPTY LOGICAL BOND!";
	}
	if (operands.size() == 1) {
	    return getLogicalOperator() + "(" + operands.iterator().next().toString() + ")";
	}
	for (Iterator<Bond> iterator = getOperands().iterator(); iterator.hasNext();) {
	    Bond bond = iterator.next();
	    bonds += bond.toString();
	    if (iterator.hasNext()) {
		bonds += " " + getLogicalOperator() + "\n";
	    }
	}
	return "(" + bonds.trim() + ")";
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof LogicalBond) {
	    LogicalBond logicalBond = (LogicalBond) obj;
	    if (!Objects.equals(logicalBond.getLogicalOperator(), getLogicalOperator())) {
		return false;
	    }
	    // the following is hashset and not list by purpose of testing equality!
	    return Objects.equals(new HashSet<Bond>(getOperands()), new HashSet<Bond>(logicalBond.getOperands()));
	}
	return super.equals(obj);
    }

    @Override
    public LogicalBond clone() {
	ArrayList<Bond> cloneOperands = new ArrayList<>();
	for (Bond bond : getOperands()) {
	    cloneOperands.add(bond.clone());
	}
	return new LogicalBond(getLogicalOperator(), cloneOperands);
    }

    @Override
    public int hashCode() {
	int hash = logicalOperator == null ? "null".hashCode() : logicalOperator.hashCode();
	for (Bond bond : operands) {
	    // hash is summed, because order of bonds should not create different hashes, for the sake of equality
	    // method in sets
	    hash += bond.hashCode();
	}
	return hash;
    }
}
