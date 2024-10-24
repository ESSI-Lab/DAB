package eu.essi_lab.request.executor.discover;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.logicng.formulas.And;
import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.formulas.Literal;
import org.logicng.formulas.Not;
import org.logicng.formulas.Or;
import org.logicng.formulas.Variable;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.LogicalBond;

public class BondFormula {

    final FormulaFactory factory = new FormulaFactory();

    Formula formula = null;

    public BondFormula(Bond bond) {

	formula = parseBond(bond);

    }

    private HashMap<String, Bond> basicBonds = new HashMap<String, Bond>();

    private Formula parseBond(Bond bond) {
	if (bond == null) {
	    return null;
	}
	if (bond instanceof LogicalBond) {
	    LogicalBond logicalBond = (LogicalBond) bond;
	    List<Formula> formulas = new ArrayList<Formula>();
	    for (Bond child : logicalBond.getOperands()) {
		Formula childFormula = parseBond(child);
		formulas.add(childFormula);
	    }
	    switch (logicalBond.getLogicalOperator()) {
	    case AND:
		return factory.and(formulas);
	    case OR:
		return factory.or(formulas);
	    case NOT:
		return factory.not(formulas.get(0));
	    }
	    throw new IllegalArgumentException("Unexpected logical operator");
	} else {

	    for (String var : basicBonds.keySet()) {
		Bond next = basicBonds.get(var);
		if (next.equals(bond)) {
		    return factory.variable(var);
		}
	    }
	    String var = "" + basicBonds.size();
	    basicBonds.put(var, bond);
	    return factory.variable(var);
	}

    }

    public Formula getFormula() {
	return formula;
    }

    public Bond getBond(Formula formula) {
	if (formula == null) {
	    return null;
	}
	if (formula instanceof Literal) {
	    Literal literal = (Literal) formula;
	    String str = literal.toString();
	    if (str.startsWith("~")) {
		Bond operand = getBond(literal.negate());
		return BondFactory.createNotBond(operand);
	    }
	    Bond ret = basicBonds.get(str);
	    if (ret == null) {
		throw new IllegalArgumentException("Unexpected variable: " + str);
	    }
	    return ret;
	} else if (formula instanceof Variable) {
	    Variable var = (Variable) formula;
	    Bond ret = basicBonds.get(var.toString());
	    if (ret == null) {
		throw new IllegalArgumentException("Unexpected variable: " + var.toString());
	    }
	    return ret;
	} else if (formula instanceof And) {
	    And and = (And) formula;
	    List<Bond> children = new ArrayList<>();
	    Iterator<Formula> iterator = and.iterator();
	    while (iterator.hasNext()) {
		Formula childFormula = iterator.next();
		Bond childBond = getBond(childFormula);
		children.add(childBond);
	    }
	    return BondFactory.createAndBond(children);
	} else if (formula instanceof Or) {
	    Or or = (Or) formula;
	    List<Bond> children = new ArrayList<>();
	    Iterator<Formula> iterator = or.iterator();
	    while (iterator.hasNext()) {
		Formula childFormula = iterator.next();
		Bond childBond = getBond(childFormula);
		children.add(childBond);
	    }
	    return BondFactory.createOrBond(children);
	} else if (formula instanceof Not) {
	    Not not = (Not) formula;
	    Formula childFormula = not.iterator().next();
	    Bond childBond = getBond(childFormula);
	    return BondFactory.createAndBond(childBond);
	} else {
	    throw new IllegalArgumentException("Unexpected formula");
	}
    }

}
