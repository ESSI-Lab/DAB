package eu.essi_lab.demo.extensions.indexes;

import eu.essi_lab.model.Queryable;

/**
 * This enum provides 2 custom queryables related to the indexed elements provided by the {@link CustomIndexes}
 * class
 * 
 * @author Fabrizio
 */
public enum CustomQueryable implements Queryable {

    /**
     * 
     */
    ONLINE_NAME("onlineName"),

    /**
     * 
     */
    CONTACT_CITY("contactCity");

    private String name;

    private CustomQueryable(String name) {

	this.name = name;
    }

    @Override
    public String getName() {

	return name;
    }

    @Override
    public ContentType getContentType() {

	return ContentType.TEXTUAL;
    }

    @Override
    public boolean isVolatile() {

	return false;
    }

    @Override
    public String toString() {

	return getName();
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    @Override
    public boolean isEnabled() {
	return true;
    }
}
