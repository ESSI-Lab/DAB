/**
 * 
 */
package eu.essi_lab.lib.skoss;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
	NONE(0),
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
	    int limit) throws Exception;
}
