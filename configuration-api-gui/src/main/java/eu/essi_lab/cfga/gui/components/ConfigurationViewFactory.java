package eu.essi_lab.cfga.gui.components;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.Optional;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.Tabs.Orientation;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gui.extension.directive.AddDirective;
import eu.essi_lab.cfga.gui.extension.directive.EditDirective;
import eu.essi_lab.cfga.gui.extension.directive.RemoveDirective;

/**
 * @author Fabrizio
 */
public class ConfigurationViewFactory {

    /**
     * 
     */
    private static final float TAB_WIDTH = 1200;
    static final String TAB_HEADER_ID_PREFIX = "tabHeader";

    /**     * @return
     */
    public static HorizontalLayout createConfigurationViewNavBarContentLayout() {

	HorizontalLayout navbarContent = new HorizontalLayout();
	navbarContent.setWidthFull();
	navbarContent.getStyle().set("padding-bottom", "15px");
	navbarContent.getStyle().set("padding-left", "15px");
	navbarContent.getStyle().set("padding-right", "15px");
	navbarContent.getStyle().set("padding-top", "0px");

	return navbarContent;
    }

    /**
     * @return
     */
    public static TabsWithContent createConfigurationViewTabs() {

	TabsWithContent tabs = new TabsWithContent();
	tabs.setOrientation(Tabs.Orientation.VERTICAL);
	tabs.getStyle().set("padding-left", "15px");

	return tabs;
    }

    /**
     * @param orientation
     * @param tabName
     * @param removeDirective 
     * @param editDirective 
     * @param addDirectives
     * @return
     */
    public static TabContainer createConfigurationViewTabContainer(//
	    Configuration configuration, //
	    Orientation orientation, //
	    String tabName, //
	    Optional<AddDirective> addDirective,//
	    Optional<RemoveDirective> removeDirective, Optional<EditDirective> editDirective) {

	TabContainer layout = null;

	switch (orientation) {
	case HORIZONTAL:

	    HorizontalLayout horizontalLayout = new HorizontalLayout();
	    // horizontalLayout.getStyle().set("border", "1px solid black");
	    horizontalLayout.setWidth("100%");
	    // horizontalLayout.setHeight("100%");

	    // layout = horizontalLayout;

	    break;

	case VERTICAL:

	    TabContainer container = ComponentFactory
		    .createNoSpacingNoMarginTabContainer("tab-container-vertical-layout-for-" + tabName);
	  
	    container.setRemoveDirective(removeDirective);
	    container.setEditDirective(editDirective);

//	    container.setWidth(TAB_WIDTH, Unit.PIXELS);
	    container.getStyle().set("margin-bottom", "50px");

	    HorizontalLayout headerLayout = ComponentFactory
		    .createNoSpacingNoMarginHorizontalLayout("tab-container-header-layout-for-" + tabName);
	    headerLayout.setHeight("60px");
	    headerLayout.setWidthFull();
	    headerLayout.setAlignItems(Alignment.BASELINE);
	    headerLayout.setId(TAB_HEADER_ID_PREFIX+"_"+tabName);

	    container.add(headerLayout);

	    //
	    //
	    //

	    Label label = new Label();
	    label.setWidthFull();
	    label.setText(tabName);
	    label.getStyle().set("font-size", "30px");
	    label.getStyle().set("color", "black");

	    headerLayout.add(label);

	    //
	    //
	    //

	    if (addDirective.isPresent()) {

		Button addButton = SettingComponentFactory.createSettingAddButton(configuration, container, addDirective.get());
		headerLayout.add(addButton);
	    }

	    layout = container;
	}

	return layout;
    }

    /**
     * @param listener
     * @return
     */
    public static Button createSaveButton(ComponentEventListener<ClickEvent<Button>> listener) {

	ConfigurationViewButton button = new ConfigurationViewButton("SAVE", VaadinIcon.CLOUD_UPLOAD_O.create());
	button.addThemeVariants(ButtonVariant.LUMO_LARGE);
	button.setHeight(43, Unit.PIXELS);
	button.setWidth(280, Unit.PIXELS);

	button.addEnabledStyle("color", "red");
	button.addEnabledStyle("background-color", "yellow");
	button.addEnabledStyle("border", "1px solid red");

	button.addClickListener(listener);

	EnabledGroupManager.getInstance().add(button);

	return button;
    }

}
