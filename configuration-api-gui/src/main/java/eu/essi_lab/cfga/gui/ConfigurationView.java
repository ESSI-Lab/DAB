package eu.essi_lab.cfga.gui;

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

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs.Orientation;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.dom.DomListenerRegistration;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationChangeListener;
import eu.essi_lab.cfga.gui.components.ComponentFactory;
import eu.essi_lab.cfga.gui.components.ConfigurationViewFactory;
import eu.essi_lab.cfga.gui.components.CustomButton;
import eu.essi_lab.cfga.gui.components.TabContainer;
import eu.essi_lab.cfga.gui.components.TabsWithContent;
import eu.essi_lab.cfga.gui.dialog.EnhancedDialog;
import eu.essi_lab.cfga.gui.dialog.NotificationDialog;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabDescriptor;
import eu.essi_lab.cfga.gui.extension.directive.AddDirective;
import eu.essi_lab.cfga.gui.extension.directive.DirectiveManager;
import eu.essi_lab.cfga.gui.extension.directive.EditDirective;
import eu.essi_lab.cfga.gui.extension.directive.RemoveDirective;
import eu.essi_lab.cfga.gui.extension.directive.ShowDirective;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.web.WebRequest;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public abstract class ConfigurationView extends AppLayout implements ConfigurationChangeListener, DomEventListener {

    private TabsWithContent tabs;
    private final HorizontalLayout navbarContent;
    private final Label headerLabel;
    private final Image headerImage;
    private final DrawerToggle drawerToggle;
    private boolean drawerOpened;

    // for test purpose, shows only the selected tab or all if -1
    private final int oneTab = -1;
    private Configuration configuration;

    /**
     * 
     */
    private final Button saveButton;
    protected boolean tabAlreadyOpen;
    final Label infoLabel;

    /**
     * 
     */
    CustomButton logoutButton;
    private final String requestURL;
    private static String ownerBrowserAdress;

    /**
     * 
     */
    public ConfigurationView() {

	GSLoggerFactory.getLogger(getClass()).info("Loading configuration view STARTED");

	//
	//
	//

	drawerOpened = true;

	//
	//
	//

	VaadinRequest request = VaadinService.getCurrentRequest();

	HttpServletRequest httpServletRequest = ((VaadinServletRequest) request).getHttpServletRequest();

	requestURL = httpServletRequest.getRequestURL().toString();

	//
	//
	//

	List<String> xForwardedForHeaders = WebRequest.readXForwardedForHeaders(httpServletRequest);

	String address = VaadinSession.getCurrent().getBrowser().getAddress();

	if (!xForwardedForHeaders.isEmpty()) {

	    address = xForwardedForHeaders.getFirst();
	}

	GSLoggerFactory.getLogger(getClass()).info("Browser address: {}", address);

	//
	//
	//

	VaadinSession.getCurrent().setErrorHandler(new ErrorHandler() {

	    @Override
	    public void error(ErrorEvent event) {

		Throwable thr = event.getThrowable();

		GSLoggerFactory.getLogger(getClass()).error(thr.getMessage(), thr);

		NotificationDialog.getErrorDialog(thr.getMessage(), thr);
	    }
	});

	//
	//
	//

	setPrimarySection(AppLayout.Section.NAVBAR); // default

	//
	// header
	//

	drawerToggle = new DrawerToggle();
	drawerToggle.setIcon(VaadinIcon.ANGLE_DOUBLE_LEFT.create());
	drawerToggle.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
	    drawerOpened = !drawerOpened;
	    if (drawerOpened) {
		drawerToggle.setIcon(VaadinIcon.ANGLE_DOUBLE_LEFT.create());
	    } else {
		drawerToggle.setIcon(VaadinIcon.ANGLE_DOUBLE_RIGHT.create());
	    }
	});

	headerImage = new Image();
	// headerImage.setSrc("https://i.imgur.com/GPpnszs.png");
	// headerImage.setHeight("44px");

	headerLabel = new Label();
	headerLabel.getStyle().set("font-size", "28px");
	headerLabel.setWidthFull();

	UI.getCurrent().getPage().retrieveExtendedClientDetails(receiver -> {

	});

	infoLabel = ComponentFactory.createLabel();
	infoLabel.getStyle().set("font-size", "12px");
	infoLabel.getStyle().set("padding", "5px");

	// infoLabel.getStyle().set("background-color", "white");
	// infoLabel.getStyle().set("color", "green");
	// infoLabel.getStyle().set("border", "1px solid green");

	infoLabel.setWidth(560, Unit.PIXELS);
	infoLabel.setHeight(30, Unit.PIXELS);

	saveButton = ConfigurationViewFactory.createSaveButton(
		(ComponentEventListener<ClickEvent<Button>>) this::onSaveButtonClicked);

	saveButton.setEnabled(false);
	// hides the save button, shows it again if initialized, authorized and the tab was closed
	saveButton.setVisible(false);

	navbarContent = ConfigurationViewFactory.createConfigurationViewNavBarContentLayout();

	navbarContent.add(drawerToggle, headerImage, headerLabel, saveButton);

	addToNavbar(navbarContent);

	//
	//
	//

	if (!isInitialized()) {

	    GSLoggerFactory.getLogger(getClass()).warn("Initialization required");

	    GSLoggerFactory.getLogger(getClass()).info("Loading configuration view ENDED");

	    return;
	}

	// if (ownerBrowserAdress == null || ownerBrowserAdress != null && ownerBrowserAdress.equals(address)) {
	//
	// GSLoggerFactory.getLogger(getClass()).info("First request or request coming from the current session owner");
	//
	// } else

	if (!enableMultipleTabs() && SingleTabManager.getInstance().isTabOpen()) {

	    GSLoggerFactory.getLogger(getClass()).warn("Tab already opened");

	    GSLoggerFactory.getLogger(getClass()).info("Loading configuration view ENDED");

	    tabAlreadyOpen = true;

	    // infoLabel.setVisible(false);

	    String msg = "If the configuration tab has been closed a little while ago,";
	    msg += " the related session will expire in a few seconds and you will";
	    msg += " be able to start a new one by refreshing this tab or by opening a new configuration tab. If the configuration tab is still open, no other configuration tabs can be opened ";
	    msg += " until it is closed or the 'Logout' button is pressed";

	    NotificationDialog dialog = NotificationDialog.getNotificationDialog("Another session is already active", msg, 700);

	    dialog.getCancelButton().getStyle().set("display", "none");
	    dialog.getCloseButton().getStyle().set("display", "none");
	    dialog.open();

	    return;
	}

	if (!isAuthorized()) {

	    GSLoggerFactory.getLogger(getClass()).warn("Authorization failed");

	    GSLoggerFactory.getLogger(getClass()).info("Loading configuration view ENDED");

	    return;
	}

	//
	//
	//

	ownerBrowserAdress = address;
	GSLoggerFactory.getLogger(getClass()).info("Current session owner address: {}", ownerBrowserAdress);

	//
	//
	//

	SingleTabManager.getInstance().setUI(UI.getCurrent());
	SingleTabManager.getInstance().setTabOpen(true);

	//
	//
	//

	saveButton.setVisible(true);

	tabs = ConfigurationViewFactory.createConfigurationViewTabs();

	addToDrawer(tabs);

	setContent(tabs.getContent());

	//
	//
	//

	configuration = retrieveConfiguration();

	configuration.addChangeEventListener(tabs);

	initContent(configuration);

	//
	//
	//

	if (showLogOutButton()) {

	    logoutButton = new CustomButton(VaadinIcon.SIGN_OUT.create());
	    logoutButton.addThemeVariants(ButtonVariant.LUMO_LARGE);
	    logoutButton.addClickListener(getLogOutButtonListener());
	    logoutButton.setTooltip("Logout");

	    logoutButton.getStyle().set("border", "1px solid hsl(0deg 0% 81%");
	    logoutButton.getStyle().set("margin-left", "100px");
	    logoutButton.getStyle().set("background-color", "white");

	    getNavbarContent().add(logoutButton);
	}

	//
	//
	//

	if (logOutAfterInactivity()) {

	    IdleTracker.getInstance().setUI(UI.getCurrent());
	    IdleTracker.getInstance().setView(this);
	    IdleTracker.getInstance().reset();

	    DomListenerRegistration mouseMove = UI.getCurrent().getElement().addEventListener("mousemove", this);
	    mouseMove.throttle((int) TimeUnit.SECONDS.toMillis(15));

	    DomListenerRegistration mouseWheel = UI.getCurrent().getElement().addEventListener("wheel", this);
	    mouseWheel.throttle((int) TimeUnit.SECONDS.toMillis(15));

	    UI.getCurrent().getElement().addEventListener("click", this);

	    UI.getCurrent().addAttachListener((ComponentEventListener<AttachEvent>) event -> {

		IdleTracker.getInstance().reset();

		onAttachEvent(event);
	    });

	    UI.getCurrent().addDetachListener((ComponentEventListener<DetachEvent>) event -> {

		IdleTracker.getInstance().reset();

		onDetachEvent(event);
	    });
	}

	//
	//
	//

	//
	//
	//

	// LockManager.getInstance().setSource(configuration.getSource());
	//
	// LockManager.getInstance().setUI(UI.getCurrent(), this);
	//
	// LockManager.getInstance().checkLock();

	//
	//
	//

	GSLoggerFactory.getLogger(getClass()).info("Loading configuration view ENDED");
    }

    /**
     * @author Fabrizio
     */
    public enum MessageType {

	INFO, //
	ERROR, //
	WARNING
    }

    /**
     * @param message
     * @param type
     */
    public void updateInfoMessage(String message, MessageType type) {

	infoLabel.setText(message);

	switch (type) {
	case INFO:

	    infoLabel.getStyle().set("background-color", "white");
	    infoLabel.getStyle().set("color", "green");
	    infoLabel.getStyle().set("border", "1px solid green");

	    break;

	case ERROR:

	    infoLabel.getStyle().set("background-color", "white");
	    infoLabel.getStyle().set("color", "red");
	    infoLabel.getStyle().set("border", "1px solid red");

	    break;

	case WARNING:

	    infoLabel.getStyle().set("background-color", "white");
	    infoLabel.getStyle().set("color", "lightgray");
	    infoLabel.getStyle().set("border", "1px solid lightgray");
	}
    }

    /**
     * @return
     */
    protected boolean isInitialized() {

	return true;
    }

    /**
     * @return
     */
    protected boolean isAuthorized() {

	return true;
    }

    /**
     * @param event
     */
    protected void onSaveButtonClicked(ClickEvent<Button> event) {

	try {
	    configuration.flush();
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());

	    NotificationDialog.getErrorDialog(e.getMessage(), e).open();
	}
    }

    /**
     * @param event
     */
    protected void onDetachEvent(DetachEvent event) {

    }

    /**
     * @param event
     */
    protected void onAttachEvent(AttachEvent event) {

    }

    /**
     * @return
     */
    protected boolean showLogOutButton() {

	return false;
    }

    /**
     * @return
     */
    protected LogOutButtonListener getLogOutButtonListener() {

	return null;
    }

    /**
     * @return
     */
    protected boolean logOutAfterInactivity() {

	return false;
    }

    /**
     * @return
     */
    protected boolean enableMultipleTabs() {

	return false;
    }

    /**
     * {@link ComponentInfo} instances and the related {@link TabDescriptor}, can be defined to manage one or more
     * {@link Setting}s.<br>
     * If the {@link RemoveDirective} of the {@link TabDescriptor} allows the removal of
     * all settings, when the view is initialized and there is no settings coupled with the {@link ComponentInfo},
     * the {@link ComponentInfo} and its {@link TabDescriptor} cannot be found and as consequence, the related tab cannot be
     * rendered.<br>
     * In this case, it will no longer be possible to add or remove that kind of settings just because there
     * is no tab with the required {@link AddDirective}.<br>
     * To avoid this issue, this method can be used to register a list of {@link ComponentInfo} that must be
     * <i>always rendered</i> (typically tabs with {@link RemoveDirective}s and {@link AddDirective}), even if the
     * related settings are missing.<br>
     * <br>
     * This method can also be used to add {@link ComponentInfo} instances that have no related {@link Setting}
     * 
     *  @see RemoveDirective#isFullRemovalAllowed()
     * @return
     */
    protected List<ComponentInfo> getAdditionalsComponentInfo() {

	return new ArrayList<>();
    }

    @Override
    public void configurationChanged(ConfigurationChangeEvent event) {

	switch (event.getEventType()) {
	case ConfigurationChangeEvent.SETTING_PUT:
	    onSettingPut(event.getSettings());
	    break;
	case ConfigurationChangeEvent.SETTING_REPLACED:
	    onSettingReplaced(event.getSettings());
	    break;
	case ConfigurationChangeEvent.SETTING_REMOVED:
	    onSettingRemoved(event.getSettings());
	    break;
	case ConfigurationChangeEvent.CONFIGURATION_CLEARED:
	    onConfigurationCleared();
	    break;
	case ConfigurationChangeEvent.CONFIGURATION_FLUSHED:
	    onConfigurationFlushed();
	    break;
	case ConfigurationChangeEvent.CONFIGURATION_AUTO_RELOADED:
	    onConfigurationAutoReloaded();
	    break;
	}
    }

    /**
     * @return the requestURL
     */
    public String getRequestURL() {

	return requestURL;
    }

    /**
     * Handles the registered DOM events by resetting the the {@link IdleTracker}
     */
    @Override
    public void handleEvent(DomEvent event) {

	IdleTracker.getInstance().reset();
    }

    /**
     * 
     */
    public void refresh() {

	configuration = retrieveConfiguration();

	tabs.clear();
	try {
	    EnhancedDialog.closeAll();
	} catch (Throwable t) {
	    // a concurrent modification exception is sometimes thrown
	}
	initContent(configuration);
    }

    /**
     * @return the configuration
     */
    public Configuration getConfiguration() {

	return configuration;
    }

    /**
     * @return
     */
    public TabsWithContent getTabs() {

	return tabs;
    }

    /**
     * @return the saveButton
     */
    public Button getSaveButton() {

	return saveButton;
    }

    /**
     * @return
     */
    public DrawerToggle getDrawerToggle() {

	return drawerToggle;
    }

    /**
     * @param index
     * @param label
     * @param component
     * @return
     */
    public Tab addTab(int index, String label, TabContainer component) {

	return tabs.addTab(index, label, component);
    }

    /**
     * @return
     */
    public HorizontalLayout getNavbarContent() {

	return navbarContent;
    }

    /**
     * @param source
     */
    public void setHeaderImageUrl(String source) {

	headerImage.setSrc(source);
    }

    /**
     * @param source
     * @param width
     * @param height
     */
    public void setHeaderImageUrl(String source, int width, int height) {

	headerImage.setSrc(source);

	if (height > 0) {
	    headerImage.setHeight(height + "px");
	}

	if (width > 0) {
	    headerImage.setWidth(width + "px");
	}
    }

    /**
     * @param source
     * @param height
     */
    public void setHeaderImageUrl(String source, int height) {

	setHeaderImageUrl(source, -1, height);
    }

    /**
     * @param source
     * @param width
     * @param height
     */
    public void setHeaderImage(InputStream source, int width, int height) {

	StreamResource resource = new StreamResource("icon", (InputStreamFactory) () -> source);

	headerImage.setSrc(resource);

	if (height > 0) {
	    headerImage.setHeight(height + "px");
	}

	if (width > 0) {
	    headerImage.setWidth(width + "px");
	}
    }

    /**
     * @param source
     * @param height
     */
    public void setHeaderImage(InputStream source, int height) {

	setHeaderImage(source, -1, height);
    }

    /**
     * @param source
     */
    public void setHeaderImage(InputStream source) {

	setHeaderImage(source, -1, -1);
    }

    /**
     * @param headerText
     */
    public void setHeaderText(String headerText) {

	headerLabel.setText(headerText);
    }

    /**
     * @param headerText
     * @param width
     */
    public void setHeaderText(String headerText, int width) {

	headerLabel.setText(headerText);
	headerLabel.getStyle().set("width", width + "px");
    }

    /**
     * @return
     */
    public Image getHeaderImage() {

	return headerImage;
    }

    /**
     * @return the headerLabel
     */
    public Label getHeaderLabel() {

	return headerLabel;
    }

    /**
     * 
     */
    public void removeDrawerToggle() {

	navbarContent.remove(drawerToggle);
    }

    /**
     * @param tabDescriptor
     * @return
     */
    public List<Setting> retrieveTabSettings(TabDescriptor tabDescriptor) {

	List<Setting> allSetting = configuration.list();

	HashMap<TabDescriptor, List<Setting>> tabInfoMap = createTabInfoMap(allSetting);

	return tabInfoMap.getOrDefault(tabDescriptor, new ArrayList<>());
    }

    /**
     * @param setting
     */
    protected void onSettingPut(List<Setting> settings) {

	GSLoggerFactory.getLogger(getClass()).debug("Setting {} put",
		settings.stream().map(Setting::getName).collect(Collectors.joining(",")));
    }

    /**
     * @param setting
     */
    protected void onSettingReplaced(List<Setting> settings) {

	GSLoggerFactory.getLogger(getClass()).debug("Setting {} replaced",
		settings.stream().map(Setting::getName).collect(Collectors.joining(",")));
    }

    /**
     * @param setting
     */
    protected void onSettingRemoved(List<Setting> settings) {

	GSLoggerFactory.getLogger(getClass()).debug("Setting {} removed",
		settings.stream().map(Setting::getName).collect(Collectors.joining(",")));
    }

    /**
     * 
     */
    protected void onConfigurationCleared() {

	GSLoggerFactory.getLogger(getClass()).debug("Configuration cleared");
    }

    /**
     * 
     */
    protected void onConfigurationFlushed() {

	GSLoggerFactory.getLogger(getClass()).debug("Configuration flushed");
    }

    /**
     * 
     */
    protected void onConfigurationAutoReloaded() {

	GSLoggerFactory.getLogger(getClass()).debug("Configuration autoreloaded");
    }

    /**
     * @param configuration
     */
    protected void initContent(Configuration configuration) {

	List<Setting> allSetting = configuration.list();

	HashMap<TabDescriptor, List<Setting>> tabInfoMap = createTabInfoMap(allSetting);

	List<TabDescriptor> tabDescriptorList = new ArrayList<>(tabInfoMap.keySet());

	//
	//
	//

	List<ComponentInfo> additionalComps = getAdditionalsComponentInfo();

	for (ComponentInfo componentInfo : additionalComps) {

	    Optional<TabDescriptor> tabInfo = componentInfo.getTabDescriptor();

	    if (tabInfo.isPresent()) {

		int index = tabInfo.get().getIndex();

		boolean missing = tabDescriptorList.//
			stream().//
			filter(tab -> tab.getIndex() == index).//
			findFirst().//
			isEmpty();

		if (missing) {

		    tabDescriptorList.add(tabInfo.get());
		}
	    }
	}

	//
	//
	//

	tabDescriptorList = tabDescriptorList.//
		stream().//
		sorted(Comparator.comparingInt(TabDescriptor::getIndex)).//
		collect(Collectors.toList());

	//
	//
	//

	for (TabDescriptor tabDescriptor : tabDescriptorList) {

	    if (oneTab < 0 || tabDescriptor.getIndex() == oneTab) {

		List<Setting> settings = tabInfoMap.getOrDefault(tabDescriptor, new ArrayList<>());

		//
		//
		//

		DirectiveManager directiveManager = tabDescriptor.getDirectiveManager();

		Optional<AddDirective> addDirective = directiveManager.get(AddDirective.class);

		Optional<RemoveDirective> removeDirective = directiveManager.get(RemoveDirective.class);

		Optional<EditDirective> editDirective = directiveManager.get(EditDirective.class);

		Optional<ShowDirective> showDirective = directiveManager.get(ShowDirective.class);

		String tabName = "Tab#" + tabDescriptor.getIndex();

		if (showDirective.isPresent()) {

		    tabName = showDirective.get().getName();
		}

		//
		//
		//

		Optional<ComponentInfo> additionalComp = additionalComps.//
			stream().//
			filter(c -> c.getTabDescriptor().isPresent()).//
			filter(c -> c.getTabDescriptor().get().getIndex() == tabDescriptor.getIndex()).//
			findFirst();

		ComponentInfo componentInfo = additionalComp.orElseGet(() -> settings.getFirst().getExtension(ComponentInfo.class).get());

		Orientation orientation = componentInfo.getOrientation();

		//
		//
		//

		TabContainer container = ConfigurationViewFactory.createConfigurationViewTabContainer(//
			configuration, //
			orientation, //
			tabName, //
			showDirective.flatMap(ShowDirective::getDescription),//
			addDirective, //
			removeDirective, //
			editDirective);

		container.getElement().getStyle().set("margin-left", "auto");
		container.getElement().getStyle().set("margin-right", "auto");

		//
		//
		//

		container.init(this, configuration, componentInfo, tabDescriptor);

		if (tabDescriptor.getIndex() == 0) {

		    container.render();
		}

		//
		//
		//

		if (oneTab >= 0 && tabDescriptor.getIndex() == oneTab) {
		    addTab(0, tabName, container);
		    return;

		} else if (oneTab < 0) {

		    addTab(tabDescriptor.getIndex(), tabName, container);
		}
	    }
	}
    }

    /**
     * @return
     */
    protected abstract Configuration initConfiguration();

    /**
     * @return
     */
    private Configuration retrieveConfiguration() {

	Configuration configuration = initConfiguration();

	configuration.addChangeEventListener(this);

	return configuration;
    }

    /**
     * @param settings
     * @return
     */
    private HashMap<TabDescriptor, List<Setting>> createTabInfoMap(List<Setting> settings) {

	HashMap<TabDescriptor, List<Setting>> map = new HashMap<>();

	for (Setting setting : settings) {

	    Optional<ComponentInfo> extension = setting.getExtension(ComponentInfo.class);

	    if (extension.isPresent()) {

		Optional<TabDescriptor> tabInfo = extension.get().getTabDescriptor();

		if (tabInfo.isPresent()) {

		    List<Setting> list = map.computeIfAbsent(tabInfo.get(), k -> new ArrayList<>());

		    list.add(setting);
		}
	    }
	}

	return map;
    }
}
