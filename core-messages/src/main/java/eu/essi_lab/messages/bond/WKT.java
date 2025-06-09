/**
 * 
 */
package eu.essi_lab.messages.bond;

/**
 * @author Fabrizio
 */
public class WKT implements SpatialEntity {

    private String value;

    /**
     * 
     */
    public WKT() {
    }

    /**
     * @param value
     */
    public WKT(String value) {

	this.value = value;
    }

    /**
     * @return
     */
    public String getValue() {

	return value;
    }

    /**
     * @param value
     */
    public void setValue(String value) {

	this.value = value;
    }

    @Override
    public String toString() {

	return value;
    }
    
    @Override
    public boolean equals(Object obj) {
	
	if (obj instanceof WKT) {
	    
	    return this.toString().equals(obj.toString());
	}
	
	return false;
    }

    @Override
    public WKT clone() {

	return new WKT(getValue());
    }
}
