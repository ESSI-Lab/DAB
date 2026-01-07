/**
 * 
 */
package eu.essi_lab.messages.bond;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;

/**
 * @author Fabrizio
 */
class BondReplacer {

    private final Bond bond;
    private final LogicalBond clone;

    /**
     * @param searchValues
     * @param replaceValues
     */
    public BondReplacer(Bond bond) {
	this.bond = bond;
	this.clone = (LogicalBond) bond.clone();
    }

    /**
     * @param searchValues
     * @param replaceValues
     * @return
     */
    public LogicalBond replaceWithValues(List<String> searchValues, List<String> replaceValues) {

	DiscoveryBondParser parser = new DiscoveryBondParser(bond);

	for (int i = 0; i < searchValues.size(); i++) {

	    ReplaceHandler replaceHandler = new ReplaceHandler();

	    replaceHandler.setSearch(searchValues.get(i));
	    replaceHandler.setReplace(replaceValues.get(i));

	    parser.parse(replaceHandler);
	}

	return clone;
    }

    /**
     * @param searchValues
     * @param replaceValues
     * @return
     */
    public LogicalBond replaceWithBonds(List<String> searchValues, List<Bond> replaceValues) {

	DiscoveryBondParser parser = new DiscoveryBondParser(bond);

	for (int i = 0; i < searchValues.size(); i++) {

	    ReplaceHandler replaceHandler = new ReplaceHandler();

	    replaceHandler.setSearch(searchValues.get(i));
	    replaceHandler.setReplace(replaceValues.get(i));

	    parser.parse(replaceHandler);
	}

	return clone;
    }

    /**
     * @param source
     * @param search
     * @return
     */
    private Optional<LogicalBond> findLogicalBond(Bond source, String search) {

	FindBondHandler findHandler = new FindBondHandler(search);

	DiscoveryBondParser parser = new DiscoveryBondParser(source);

	parser.parse(findHandler);

	return findHandler.getBond();
    }

    private class ReplaceHandler implements DiscoveryBondHandler {

	private String replaceString;
	private Bond replaceBond;
	private String search;

	private LogicalBond currentLogicalBond;

	/**
	 * @param search
	 */
	public void setSearch(String search) {
	    this.search = search;
	}

	/**
	 * @param replaceString
	 */
	public void setReplace(String replaceString) {

	    this.replaceString = replaceString;
	}

	/**
	 * @param replaceString
	 */
	public void setReplace(Bond replaceBond) {

	    this.replaceBond = replaceBond;
	}

	@Override
	public void startLogicalBond(LogicalBond bond) {

	    this.currentLogicalBond = bond;
	}

	@Override
	public void separator() {
	}

	@Override
	public void nonLogicalBond(Bond bond) {
	}

	@Override
	public void endLogicalBond(LogicalBond bond) {
	}

	@Override
	public void viewBond(ViewBond bond) {
	}

	@Override
	public void spatialBond(SpatialBond bond) {
	}

	@Override
	public void simpleValueBond(SimpleValueBond bond) {

	    String value = bond.getPropertyValue();

	    if (value.equals(this.search)) {

		if (currentLogicalBond != null) {

		    Optional<LogicalBond> findBond = findLogicalBond(clone, this.search);

		    if (findBond.isPresent()) {

			List<Bond> operands = Lists.newArrayList(findBond.get().getOperands());

			findBond.get().getOperands().clear();

			for (Bond operand : operands) {

			    if (operand instanceof QueryableBond) {

				@SuppressWarnings("unchecked")
				QueryableBond<Object> q = (QueryableBond<Object>) operand;
				if (q.getPropertyValue().equals(this.search)) {

				    if (replaceString != null) {

					q.setPropertyValue(this.replaceString);
					findBond.get().getOperands().add(q);

				    } else {

					findBond.get().getOperands().add(replaceBond);
				    }
				} else {

				    findBond.get().getOperands().add(operand);
				}
			    } else {

				findBond.get().getOperands().add(operand);
			    }
			}
		    }
		}
	    }
	}

	@Override
	public void resourcePropertyBond(ResourcePropertyBond bond) {
	}

	@Override
	public void customBond(QueryableBond<String> bond) {
	}

	@Override
	public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
	    // TODO Auto-generated method stub
	    
	}
    }

    /**
     * @author Fabrizio
     */
    private static class FindBondHandler implements DiscoveryBondHandler {

	private Optional<LogicalBond> out;
	private final String search;

	/***
	 * @param original
	 */
	public FindBondHandler(String search) {
	    this.search = search;
	    this.out = Optional.empty();
	}

	/**
	 * @return
	 */
	public Optional<LogicalBond> getBond() {

	    return out;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void startLogicalBond(LogicalBond bond) {

	    boolean found = bond.getOperands().//
		    stream().//
		    filter(b -> b instanceof QueryableBond).//
		    map(b -> ((QueryableBond) b).getPropertyValue()).//
		    anyMatch(v -> v.equals(search));

	    if (found) {

		out = Optional.of(bond);
	    }
	}

	@Override
	public void separator() {
	}

	@Override
	public void nonLogicalBond(Bond bond) {
	}

	@Override
	public void endLogicalBond(LogicalBond bond) {
	}

	@Override
	public void viewBond(ViewBond bond) {
	}

	@Override
	public void spatialBond(SpatialBond bond) {
	}

	@Override
	public void simpleValueBond(SimpleValueBond bond) {
	}

	@Override
	public void resourcePropertyBond(ResourcePropertyBond bond) {
	}

	@Override
	public void customBond(QueryableBond<String> bond) {
	}

	@Override
	public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
	    // TODO Auto-generated method stub
	    
	}
    }

}
