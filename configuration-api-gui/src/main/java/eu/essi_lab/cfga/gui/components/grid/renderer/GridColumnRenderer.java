/**
 * 
 */
package eu.essi_lab.cfga.gui.components.grid.renderer;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.renderer.ComponentRenderer;

/**
 * @author Fabrizio
 */
public abstract class GridColumnRenderer<C extends Component> extends ComponentRenderer<C, HashMap<String, String>> {

    /**
     * 
     */
    private static final long serialVersionUID = -6816629717419845396L;
    protected Consumer<HashMap<String, String>> consumer;
    protected C component;

    /**
    * 
    */
    public GridColumnRenderer() {

    }

    /**
     * @param consumer
     */
    public GridColumnRenderer(Consumer<HashMap<String, String>> consumer) {

	this.consumer = consumer;
    }

    @Override
    public abstract C createComponent(HashMap<String, String> item);

    /**
     * @return
     */
    public C getComponent() {

	return component;
    }

    /**
     * @return
     */
    public Optional<Consumer<HashMap<String, String>>> getConsumer() {

	return Optional.ofNullable(consumer);
    }

}
