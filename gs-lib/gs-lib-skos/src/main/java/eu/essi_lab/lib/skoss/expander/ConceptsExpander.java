/**
 * 
 */
package eu.essi_lab.lib.skoss.expander;

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
import java.util.Optional;

import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.SKOSSemanticRelation;

/**
 * @author Fabrizio
 */
@FunctionalInterface
public interface ConceptsExpander {

    /**
     * @author Fabrizio
     */
    public enum ExpansionLevel {

	/**
	 * 
	 */
	BASIC(0),
	/**
	 * 
	 */
	LOW(1),
	/**
	 * 
	 */
	MEDIUM(2),
	/**
	 * 
	 */
	HIGH(3);

	private int value;

	/**
	 * @param value
	 */
	private ExpansionLevel(int value) {

	    this.value = value;
	}

	/**
	 * @param level
	 * @return
	 */
	public Optional<ExpansionLevel> next() {

	    return switch (getValue()) {
	    case 0 -> Optional.of(LOW);
	    case 1 -> Optional.of(MEDIUM);
	    case 2 -> Optional.of(HIGH);
	    case 3 -> Optional.empty();
	    default -> Optional.empty();
	    };
	}

	/**
	 * @param value
	 * @return
	 */
	public static Optional<ExpansionLevel> of(int value) {

	    return Arrays.asList(ExpansionLevel.values()).//
		    stream().//
		    filter(v -> v.getValue() == value).//
		    findFirst();
	}

	/**
	 * @return the value
	 */
	public int getValue() {

	    return value;
	}
    }

    /**
     * @param concept
     * @param ontologyUrls
     * @param sourceLangs
     * @param searchLangs
     * @param expansionRelations
     * @param targetLevel
     * @param limit
     * @return
     * @throws Exception
     */
    SKOSResponse expand(//
	    List<String> concepts, //
	    List<String> ontologyUrls, //
	    List<String> sourceLangs, //
	    List<String> searchLangs, //
	    List<SKOSSemanticRelation> expansionRelations, //
	    ExpansionLevel targetLevel, //
	    ExpansionLimit limit) throws Exception;
}
