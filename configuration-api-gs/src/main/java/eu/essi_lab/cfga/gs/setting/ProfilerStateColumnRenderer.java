/**
 * 
 */
package eu.essi_lab.cfga.gs.setting;

import java.util.HashMap;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import eu.essi_lab.cfga.gui.components.grid.LegendBuilder;
import eu.essi_lab.cfga.gui.components.grid.renderer.IconColumnRenderer;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class ProfilerStateColumnRenderer extends IconColumnRenderer {

    /**
     * @return
     */
    public Optional<Component> getLegend() {

	Component component = LegendBuilder.get().//
		addLegendPart("Online", createIcon("Online"), -1, 35, 0).//
		addLegendPart("Offline", createIcon("Offline"), -1, 35, 0).//
		build("State");

	return Optional.of(component);
    }

    /**
     * 
     */
    @Override
    protected Icon createIcon(HashMap<String, String> item) {

	return createIcon(item.get("State"));
    }

    @Override
    protected Optional<String> getToolTip(HashMap<String, String> item) {

	String status = item.get("State");
	switch (status) {

	case "Online":
	case "Offline":

	    return Optional.of(status);
	}

	return Optional.empty();
    }

    /**
     * @param status
     * @return
     */
    private Icon createIcon(String status) {
    
        return status.equals("Online") ? VaadinIcon.SIGNAL.create() : VaadinIcon.BAN.create();
    }
}
