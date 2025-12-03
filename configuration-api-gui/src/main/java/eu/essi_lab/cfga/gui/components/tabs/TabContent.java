package eu.essi_lab.cfga.gui.components.tabs;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.details.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.tabs.*;
import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.gui.components.*;
import eu.essi_lab.cfga.gui.components.grid.*;
import eu.essi_lab.cfga.gui.components.setting.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.cfga.gui.directive.*;
import eu.essi_lab.cfga.setting.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class TabContent extends VerticalLayout implements Renderable {

    /**
     *
     */
    public static final String TAB_HEADER_ID_PREFIX = "tabHeader";

    private GridComponent grid;
    private Optional<RemoveDirective> removeDirective;
    private Optional<EditDirective> editDirective;
    private TabDescriptor tabDesc;
    private TabContentDescriptor tabContentDesc;
    private Configuration configuration;
    private boolean rendered;
    private final List<Component> legends;

    /**
     *
     */
    public TabContent() {

	legends = new ArrayList<>();

	getStyle().set("padding", "0px");
	getStyle().set("padding-top", "5px");

	setMargin(false);
	setSpacing(false);
    }

    /**
     * @return
     */
    @Override
    public Component getComponent() {

	return this;
    }

    /**
     * @param configuration
     * @param tabContentDesc
     * @param tabDesc
     */
    public void init(//
	    Configuration configuration, //
	    TabContentDescriptor tabContentDesc, //
	    TabDescriptor tabDesc) {

	this.configuration = configuration;
	this.tabContentDesc = tabContentDesc;
	this.tabDesc = tabDesc;

	DirectiveManager directiveManager = tabContentDesc.getDirectiveManager();

	this.removeDirective = directiveManager.get(RemoveDirective.class);
	this.editDirective = directiveManager.get(EditDirective.class);

	//
	// Header
	//

	HorizontalLayout headerLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout(
		"tab-container-header-layout-for-" + tabContentDesc.getLabel());
	headerLayout.setWidthFull();
	headerLayout.setAlignItems(Alignment.BASELINE);
	headerLayout.setId(TabContent.TAB_HEADER_ID_PREFIX + "_" + tabContentDesc.getLabel());

	add(headerLayout);

	//
	// Description
	//

	Optional<ShowDirective> showDirective = directiveManager.get(ShowDirective.class);

	if (showDirective.flatMap(ShowDirective::getDescription).isPresent()) {

	    VerticalLayout descLayout = ComponentFactory.createNoSpacingNoMarginVerticalLayout();
	    descLayout.getStyle().set("padding", "0px");
	    descLayout.setWidthFull();

	    String desc = showDirective.flatMap(ShowDirective::getDescription).get();

	    Label descLabel = new Label();
	    descLabel.setWidthFull();
	    descLabel.setMaxHeight("130px");
	    descLabel.setText(desc);
	    descLabel.getStyle().set("margin-left", "4px");
	    descLabel.getStyle().set("font-size", "14px");
	    descLabel.getStyle().set("color", "black");

	    descLayout.add(descLabel);

	    if (showDirective.get().withDescriptionSeparator()) {

		Div separator = ComponentFactory.createSeparator();
		separator.getStyle().set("width", "99%");
		separator.getStyle().set("margin-top", "3px");
		separator.getStyle().set("margin-left", "4px");

		descLayout.add(separator);
	    }

	    headerLayout.add(descLayout);

	} else {

	    Div div = ComponentFactory.createDiv();
	    div.setWidthFull();

	    headerLayout.add(div);
	}

	//
	// ADD button
	//

	Optional<AddDirective> addDirective = directiveManager.get(AddDirective.class);

	addDirective.ifPresent(dir -> {

	    Button addButton = SettingComponentFactory.createSettingAddButton(configuration, this, dir);
	    headerLayout.add(addButton);
	});

	//
	// RELOAD button
	//

	if (tabContentDesc.isReloadable()) {

	    if (addReloadButton(headerLayout)) {

		CustomButton reloadButton = createReloadButton();

		headerLayout.add(reloadButton);
	    }
	}
    }

    /**
     * @param refresh
     */
    @Override
    public void render(boolean refresh) {

	setRendered(true);

	removeAllButHeader();

	//
	// rendering according to the content provided by the TabContentDescriptor
	//

	if (tabContentDesc.getContent().isPresent()) {

	    add(tabContentDesc.getContent().get());

	    return;
	}

	//
	// rendering according to the TabContentDescriptor setting class
	//

	List<Setting> settings = configuration.list(tabContentDesc.getSettingClass().get(), false).//
		stream().//
		map(s -> (Setting) s).//
		collect(Collectors.toList());

	DirectiveManager directiveManager = tabContentDesc.getDirectiveManager();

	Optional<ShowDirective> showDirective = directiveManager.get(ShowDirective.class);

	//
	// optional settings sorting
	//

	showDirective.flatMap(ShowDirective::getSortDirection).ifPresent(dir -> {

	    switch (dir) {
	    case ASCENDING -> settings.sort(Comparator.comparing(Setting::getName));
	    case DESCENDING -> settings.sort((s1, s2) -> s2.getName().compareTo(s1.getName()));
	    }
	});

	boolean readOnly = tabDesc.isForceReadOnlySet();

	//
	// grid component
	//

	if (tabContentDesc.getGridInfo().isPresent()) {

	    Optional<GridInfo> gridInfo = tabContentDesc.getGridInfo();

	    GridComponent gridComponent = new GridComponent(//
		    gridInfo.get(), //
		    settings, //
		    configuration, //
		    this, //
		    readOnly, //
		    refresh);

	    TabSheet tabSheet = new TabSheet();
	    tabSheet.getStyle().set("border-bottom", "1px solid #d3d3d39e");
	    boolean addTabSheet = false;

	    if (tabContentDesc.getGridInfo().get().isShowColumnsHider()) {

		tabSheet.add("Columns", gridComponent.createColumnsHider());
		addTabSheet = true;
	    }

	    if (!legends.isEmpty()) {

		tabSheet.add("Legend", gridComponent.createLegendViewer(legends));
		addTabSheet = true;
	    }

	    if (addTabSheet) {

		add(tabSheet);
	    }

	    add(gridComponent);

	    expand(gridComponent);

	} else {

	    //
	    // settings list
	    //

	    for (int i = 0; i < settings.size(); i++) {

		Setting setting = settings.get(i);

		SettingComponent component = SettingComponentFactory.createSettingComponent(//
			configuration, //
			setting.getIdentifier(), //
			readOnly, //
			this); //

		if (component.getDetails().isPresent()) {

		    Details details = component.getDetails().get();

		    if (i == 0) {

			details.getStyle().set("margin-top", "15px");
		    }

		    this.add(details);

		} else {

		    this.add(component);
		}
	    }
	}
    }

    /**
     * @return the rendered
     */
    @Override
    public boolean isRendered() {

	return rendered;
    }

    /**
     * @param rendered
     */
    @Override
    public void setRendered(boolean rendered) {

	this.rendered = rendered;
    }

    /**
     *
     */
    public void add(Component... components) {

	super.add(components);

	if (components.length == 1 && components[0] instanceof GridComponent) {

	    this.grid = (GridComponent) components[0];
	}
    }

    /**
     * @param legend
     * @return
     */
    public void addLegend(Component legend) {

	if (legends.stream().map(lg -> lg.getId().get()).noneMatch(id -> id.equals(legend.getId().get()))) {

	    legends.add(legend);
	}
    }

    /**
     * @param component
     */
    public void addSettingComponent(SettingComponent component) {

	if (getGrid().isPresent()) {

	    GridComponent grid = getGrid().get();

	    grid.addSettingComponent(component);

	} else {

	    if (component.getDetails().isPresent()) {

		addComponentAtIndex(1, component.getDetails().get());

	    } else {

		addComponentAtIndex(1, component);
	    }
	}
    }

    /**
     * @param oldComponent
     * @param newComponent
     */
    public void replaceSettingComponent(SettingComponent oldComponent, SettingComponent newComponent) {

	if (getGrid().isPresent()) {

	    GridComponent grid = getGrid().get();

	    grid.replaceSettingComponent(oldComponent, newComponent);

	} else {

	    Component toRemove = oldComponent.getDetails().isPresent() ? oldComponent.getDetails().get() : oldComponent;
	    Component toAdd = newComponent.getDetails().isPresent() ? newComponent.getDetails().get() : newComponent;

	    int index = indexOf(toRemove);

	    addComponentAtIndex(index, toAdd);

	    toRemove.getElement().getStyle().set("display", "none");

	    remove(toRemove);

	    // int index = indexOf(oldComponent);
	    //
	    // addComponentAtIndex(index, newComponent);
	    //
	    // remove(oldComponent);
	}
    }

    /**
     * @param component
     * @param settingIdentifier
     */
    public void removeSettingComponent(SettingComponent component, String settingIdentifier) {

	if (getGrid().isPresent()) {

	    getGrid().get().removeSettingComponent(component, settingIdentifier);

	} else {

	    if (component.getDetails().isPresent()) {
		component.getDetails().get().getElement().getStyle().set("display", "none");
	    }

	    component.getStyle().set("display", "none");
	}
    }

    /**
     * Working only if a {@link #getGrid()} is present
     *
     * @param settingIdentifiers
     */
    public void removeSettingComponents(List<String> settingIdentifiers) {

	if (getGrid().isPresent()) {

	    getGrid().get().removeSettingComponents(settingIdentifiers);
	}
    }

    /**
     * @return the removeDirective
     */
    public Optional<RemoveDirective> getRemoveDirective() {

	return removeDirective;
    }

    /**
     * @return the editDirective
     */
    public Optional<EditDirective> getEditDirective() {

	return editDirective;
    }

    /**
     *
     */
    void removeAllButHeader() {

	getChildren().forEach(c -> {

	    if (c.getId().isEmpty() || !c.getId().get().startsWith(TAB_HEADER_ID_PREFIX)) {

		remove(c);
	    }
	});
    }

    /**
     * @return
     */
    private CustomButton createReloadButton() {

	CustomButton reloadButton = ComponentFactory.createReloadButton();

	reloadButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {

	    if (tabContentDesc.getTabReloader().isPresent()) {

		tabContentDesc.getTabReloader().get().run();

		removeAllButHeader();

		render(true);

	    } else {

		removeAllButHeader();

		render(true);
	    }
	});

	return reloadButton;
    }

    /**
     * @param headerLayout
     * @return
     */
    private boolean addReloadButton(HorizontalLayout headerLayout) {

	return headerLayout.//
		getChildren().//
		noneMatch(child -> child.getId().isPresent() && child.getId().get().equals("reloadButton"));
    }

    /**
     * @return
     */
    private HorizontalLayout findHeader() {

	HorizontalLayout[] out = new HorizontalLayout[1];

	getChildren().forEach(c -> {

	    if (c.getId().isPresent() && c.getId().get().startsWith(TAB_HEADER_ID_PREFIX)) {

		out[0] = (HorizontalLayout) c;
	    }
	});

	return out[0];
    }

    /**
     * @return
     */
    private Optional<GridComponent> getGrid() {

	return Optional.ofNullable(grid);
    }
}
