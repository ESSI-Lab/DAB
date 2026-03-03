package eu.essi_lab.cfga.gui.components;

import com.vaadin.flow.component.checkbox.*;

/**
 * @author Fabrizio
 */
public class ToggleButton extends Checkbox {

    /**
     *
     */
    public ToggleButton() {

	getElement().getThemeList().add("switch");
    }
}
