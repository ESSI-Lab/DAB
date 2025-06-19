/**
 * 
 */
package eu.essi_lab.model.resource.composed;

import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class ComposedElementBuilder {

    private ComposedElement composedElement;

    /**
     * @param elementName
     */
    private ComposedElementBuilder(String elementName) {

	this.composedElement = new ComposedElement(elementName);
    }

    /**
     * @return
     */
    public static ComposedElementBuilder get(String elementName) {

	return new ComposedElementBuilder(elementName);
    }

    /**
     * @param item
     * @return
     */
    public ComposedElementBuilder addItem(ComposedElementItem item) {

	composedElement.addItem(item);

	return this;
    }

    /**
     * @param name
     * @param type
     * @param value
     * @return
     */
    public ComposedElementBuilder addItem(String name, ContentType type) {

	composedElement.addItem(new ComposedElementItem(name, type));

	return this;
    }

    /**
     * @return
     */
    public ComposedElement build() {

	return composedElement;
    }

}
