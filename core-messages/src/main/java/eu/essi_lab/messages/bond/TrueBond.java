package eu.essi_lab.messages.bond;

import eu.essi_lab.model.resource.*;

import javax.xml.bind.annotation.*;

/**
 * @author Fabrizio
 */
@XmlRootElement
public class TrueBond extends ResourcePropertyBond {

    /**
     *
     */
    public TrueBond() {

	setProperty(ResourceProperty.PRIVATE_ID);
	setOperator(BondOperator.EXISTS);
    }

    @Override
    public TrueBond clone() {

	return new TrueBond();
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof TrueBond) {
	    return true;
	}
	return super.equals(obj);
    }

    @Override
    public String toString() {

	return "true-bond";
    }
}
