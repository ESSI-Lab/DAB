/**
 * 
 */
package eu.essi_lab.model.resource.composed;

import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class ComposedElementItem {

    private String name;
    private ContentType type;
    private Object value;

    /**
     * 
     */
    public ComposedElementItem() {

    }

    /**
     * @param name
     * @param type
     * @param value
     */
    public ComposedElementItem(String name, ContentType type) {

	this.name = name;
	this.type = type;
    }

    /**
     * @param name
     */
    public ComposedElementItem(String name) {

	this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {

	return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {

	this.name = name;
    }

    /**
     * @return the type
     */
    public ContentType getType() {

	return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(ContentType type) {

	this.type = type;
    }

    /**
     * @return
     */
    public String getValue() {

	return value.toString();
    }

    /**
     * @return
     */
    public Object getObjectValue() {

	return switch (type) {
	case BOOLEAN -> {
	    yield Boolean.valueOf(getValue());
	}
	case DOUBLE -> {
	    yield Double.valueOf(getValue());
	}
	case INTEGER -> {
	    yield Integer.valueOf(getValue());
	}
	case TEXTUAL, ISO8601_DATE, ISO8601_DATE_TIME -> {
	    yield getValue();
	}
	case LONG -> {
	    yield Long.valueOf(getValue());
	}

	case COMPOSED -> throw new UnsupportedOperationException("Unimplemented case: " + type);
	case SPATIAL -> throw new UnsupportedOperationException("Unimplemented case: " + type);
	};
    }

    /**
     * @param value
     */
    public void setValue(String value) {

	this.value = value;
    }

    /**
     * @param value
     */
    public void setValue(Integer value) {

	this.value = value;
    }

    /**
     * @param value
     */
    public void setValue(Boolean value) {

	this.value = value;
    }

    /**
     * @param value
     */
    public void setValue(Long value) {

	this.value = value;
    }

    /**
     * @param value
     */
    public void setValue(Double value) {

	this.value = value;
    }
}
