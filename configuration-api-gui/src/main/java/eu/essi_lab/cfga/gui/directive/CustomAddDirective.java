package eu.essi_lab.cfga.gui.directive;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.*;

/**
 * @author Fabrizio
 */
public class CustomAddDirective extends Directive {

    /**
     *
     */
    private ComponentEventListener<ClickEvent<Button>> listener;

    /**
     *
     */
    public CustomAddDirective() {

	super("ADD");
    }

    /**
     *
     * @param listener
     */
    public CustomAddDirective(ComponentEventListener<ClickEvent<Button>> listener) {

	super("ADD");

	this.listener = listener;
    }

    /**
     * @param name
     * @param settingClass
     */
    public CustomAddDirective(String name, ComponentEventListener<ClickEvent<Button>> listener) {

	super(name);

	this.listener = listener;
    }

    /**
     * @return
     */
    public ComponentEventListener<ClickEvent<Button>> getListener() {

	return listener;
    }

    /**
     * @param listener
     */
    public void setListener(ComponentEventListener<ClickEvent<Button>> listener) {

	this.listener = listener;
    }
}
