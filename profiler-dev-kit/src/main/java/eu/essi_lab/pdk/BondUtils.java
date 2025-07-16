package eu.essi_lab.pdk;

import java.util.Optional;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.model.resource.MetadataElement;

public class BondUtils {
    /**
     * @param value
     * @param element
     * @return
     */
    public static Optional<Bond> createBond(BondOperator operator, String value, MetadataElement element) {

	if (value == null || value.equals("")) {
	    return Optional.empty();
	}

	if (value.contains(" AND ")) {
	    String[] split = value.split(" AND ");
	    LogicalBond bond = BondFactory.createAndBond();
	    for (String s : split) {
		bond.getOperands().add(BondFactory.createSimpleValueBond(operator, element, s.trim()));
	    }
	    return Optional.of(bond);
	}

	if (value.contains(" OR ")) {
	    String[] split = value.split(" OR ");
	    LogicalBond bond = BondFactory.createOrBond();
	    for (String s : split) {
		bond.getOperands().add(BondFactory.createSimpleValueBond(operator, element, s.trim()));
	    }
	    return Optional.of(bond);
	}

	return Optional.of(BondFactory.createSimpleValueBond(operator, element, value.trim()));
    }
}
