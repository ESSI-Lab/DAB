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
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tabs.Orientation;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gui.*;
import eu.essi_lab.cfga.gui.components.tabs.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.cfga.gui.directive.*;

/**
 * @author Fabrizio
 */
public class ConfigurationViewFactory {

    /**
     *
     */
    private static final float TAB_WIDTH = 1200;

    /**
     * @return
     */
    public static HorizontalLayout createNavBarContentLayout() {

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
    public static TabsWithContent createTabs() {

	TabsWithContent tabs = new TabsWithContent();
	tabs.setOrientation(Orientation.VERTICAL);
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

	    content = createTabContent(descriptor, configuration, view, tabDescriptor, true);

	} else {

	    TabSheetContent tabSheet = new TabSheetContent();

	    descriptors.forEach(desc -> tabSheet.add( //
		    desc.getLabel(), //
		    createTabContent(desc, configuration, view, tabDescriptor, false)));

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
	    TabDescriptor tabDescriptor,//
	    boolean withLabel) {

	DirectiveManager directiveManager = descriptor.getDirectiveManager();

	TabContent container = ComponentFactory.createNoSpacingNoMarginTabContainer(
		"tab-container-vertical-layout-for-" + descriptor.getLabel());

	container.init(configuration, descriptor, tabDescriptor);

	Optional<ShowDirective> showDirective = directiveManager.get(ShowDirective.class);

	Optional<AddDirective> addDirective = directiveManager.get(AddDirective.class);

	Optional<RemoveDirective> removeDirective = directiveManager.get(RemoveDirective.class);

	Optional<EditDirective> editDirective = directiveManager.get(EditDirective.class);

	container.setRemoveDirective(removeDirective);
	container.setEditDirective(editDirective);

	HorizontalLayout headerLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout(
		"tab-container-header-layout-for-" + descriptor.getLabel());
	headerLayout.setWidthFull();
	headerLayout.setAlignItems(Alignment.BASELINE);
	headerLayout.setId(TabContent.TAB_HEADER_ID_PREFIX + "_" + descriptor.getLabel());

	container.add(headerLayout);

	Label tabLabel = new Label();
	tabLabel.setWidthFull();
	tabLabel.setText(tabDescriptor.getLabel());
	tabLabel.getStyle().set("font-size", "30px");
	tabLabel.getStyle().set("color", "black");

	if (showDirective.flatMap(ShowDirective::getDescription).isPresent()) {

	    String desc = showDirective.flatMap(ShowDirective::getDescription).get();

	    VerticalLayout subLayout = ComponentFactory.createNoSpacingNoMarginVerticalLayout();
	    subLayout.setWidthFull();

	    Label descLabel = new Label();
	    descLabel.setWidthFull();
	    descLabel.setMaxHeight("130px");
	    descLabel.setText(desc);
	    descLabel.getStyle().set("font-size", "16px");
	    descLabel.getStyle().set("color", "gray");

	    if (withLabel) {

		subLayout.add(tabLabel);
	    }

	    subLayout.add(descLabel);

	    headerLayout.add(subLayout);

	} else if (withLabel) {

	    headerLayout.setHeight("45px");

	    headerLayout.add(tabLabel);
	}

	addDirective.ifPresent(dir -> {

	    Button addButton = SettingComponentFactory.createSettingAddButton(configuration, container, dir);
	    headerLayout.add(addButton);
	});

	if (tabDescriptor.getIndex() == 0) {

	    container.render();
	}

	return container;
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
