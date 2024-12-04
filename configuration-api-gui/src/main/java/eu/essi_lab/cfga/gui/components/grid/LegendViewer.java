/**
 * 
 */
package eu.essi_lab.cfga.gui.components.grid;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class LegendViewer extends HorizontalLayout {

    /**
     * 
     */
    public LegendViewer(GridComponent grid, List<Component> legends) {

	setMargin(false);
	setSpacing(false);
	setHeight("30px");

	legends.forEach(leg -> add(leg));
    }
}
