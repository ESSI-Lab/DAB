package eu.essi_lab.cfga.gui.components;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.*;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gui.*;
import eu.essi_lab.cfga.gui.components.tabs.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;

/**
 * @author Fabrizio
 */
public class ConfigurationViewFactory {

    /**
     * @return
     */
    public static HorizontalLayout createHeaderLayout() {

	HorizontalLayout navbarContent = new HorizontalLayout();
	navbarContent.setWidthFull();
	navbarContent.getStyle().set("padding-left", "30px");
	navbarContent.getStyle().set("padding-right", "15px");

	return navbarContent;
    }

    /**
     * @return
     */
    public static VerticalTabs createTabs() {

	VerticalTabs tabs = new VerticalTabs();
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
    public static Renderable createTabContent(//
	    ConfigurationView view,//
	    Configuration configuration, //
	    TabDescriptor tabDescriptor) {

	Renderable content = null;

	List<TabContentDescriptor> descriptors = tabDescriptor.getContentDescriptors();

	if (descriptors.size() == 1) {

	    TabContentDescriptor descriptor = descriptors.getFirst();

	    content = createTabContent(descriptor, configuration, view, tabDescriptor);

	} else {

	    TabSheetContent tabSheet = new TabSheetContent();

	    descriptors.forEach(desc -> tabSheet.add( //
		    desc.getLabel(), //
		    createTabContent(desc, configuration, view, tabDescriptor)));

	    tabSheet.setRendered(true);

	    content = tabSheet;
	}

	return content;
    }

    /**
     * @param descriptor
     * @param configuration
     * @param view
     * @param componentInfo
     * @param tabDescriptor
     * @return
     */
    private static TabContent createTabContent(//
	    TabContentDescriptor descriptor,//
	    Configuration configuration, //
	    ConfigurationView view,//
	    TabDescriptor tabDescriptor) {

	TabContent content = ComponentFactory.createTabContent("tab-content-vertical-layout-for-" + descriptor.getLabel());

	content.init(configuration, descriptor, tabDescriptor);

	content.render();

	return content;
    }

    /**
     * @param listener
     * @return
     */
    public static Button createLogoutButton(LogOutButtonListener listener) {

	CustomButton logoutButton = new CustomButton(VaadinIcon.SIGN_OUT.create());

	logoutButton.addThemeVariants(ButtonVariant.LUMO_LARGE);
	logoutButton.addClickListener(listener);
	logoutButton.setTooltip("Logout");

	logoutButton.getStyle().set("border", "1px solid hsl(0deg 0% 81%");
	logoutButton.getStyle().set("margin-left", "0px");
	logoutButton.getStyle().set("background-color", "white");
	logoutButton.getStyle().set("margin-right", "-15px");

	return logoutButton;
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
