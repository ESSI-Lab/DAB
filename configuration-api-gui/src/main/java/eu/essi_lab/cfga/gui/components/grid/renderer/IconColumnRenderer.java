/**
 * 
 */
package eu.essi_lab.cfga.gui.components.grid.renderer;

import java.util.HashMap;
import java.util.function.Consumer;

import com.vaadin.flow.component.icon.Icon;

/**
 * @author Fabrizio
 */
public abstract class IconColumnRenderer extends GridColumnRenderer<Icon> {

    /**
     * 
     */
    private static final long serialVersionUID = -6824495335399746205L;

    /**
     * 
     */
    public IconColumnRenderer() {

    }

    /**
     * @param consumer
     */
    public IconColumnRenderer(Consumer<HashMap<String, String>> consumer) {

	super(consumer);
    }

    @Override
    public Icon createComponent(HashMap<String, String> item) {

	component = createIcon(item);
	component.setId(item.get("identifier"));

	if (consumer != null) {
	    component.addClickListener(event -> {

		consumer.accept(item);
	    });
	}

	return component;
    }

    /**
     * @param item
     * @return
     */
    protected abstract Icon createIcon(HashMap<String, String> item);

}
