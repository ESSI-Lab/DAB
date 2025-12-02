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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.tabs.Tab;
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
import eu.essi_lab.cfga.gui.components.*;
import eu.essi_lab.cfga.gui.components.tabs.*;
import eu.essi_lab.cfga.gui.dialog.EnhancedDialog;
import eu.essi_lab.cfga.gui.dialog.NotificationDialog;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.TabDescriptor;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.web.WebRequest;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public abstract class ConfigurationView extends AppLayout implements ConfigurationChangeListener, DomEventListener {

    private VerticalTabs tabs;
    private final HorizontalLayout navbarContent;
    private final Label headerLabel;
    private final Image headerImage;
    private final DrawerToggle drawerToggle;
    private boolean drawerOpened;

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

	addClassName("dab-configurator-app-layout");

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

	VerticalLayout mainLayout = ComponentFactory.createNoSpacingNoMarginVerticalLayout("dab-configurator");
	mainLayout.setWidthFull();

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

	saveButton = ConfigurationViewFactory.createSaveButton((ComponentEventListener<ClickEvent<Button>>) this::onSaveButtonClicked);

	saveButton.setEnabled(false);
	// hides the save button, shows it again if initialized, authorized and the tab was closed
	saveButton.setVisible(false);

	navbarContent = ConfigurationViewFactory.createNavBarContentLayout();

	navbarContent.add(headerImage, headerLabel, saveButton);

	mainLayout.add(navbarContent);

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

	tabs = ConfigurationViewFactory.createTabs();

	HorizontalLayout contentLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();
	contentLayout.setWidthFull();

	contentLayout.add(tabs);
	contentLayout.add(tabs.getContent());

	mainLayout.add(contentLayout);

	setContent(mainLayout);

	//
	//
	//

	configuration = retrieveConfiguration();

	configuration.addChangeEventListener(tabs);

	init(configuration);

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
	    logoutButton.getStyle().set("margin-right", "-15px");

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
    protected abstract List<TabDescriptor> getDescriptors();

    /**
     * @return
     */
    protected abstract Configuration initConfiguration();

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
	init(configuration);
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
    public VerticalTabs getTabs() {

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
    public Tab addTab(int index, String label, Renderable component) {

	return tabs.addTab(index, label, component);
    }

    /**
     * @param index
     * @param label
     * @param component
     * @return
     */
    public Tab addTab(int index, Renderable component) {

	return tabs.addTab(index, null, component);
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
     * @param setting
     */
    protected void onSettingPut(List<Setting> settings) {

	GSLoggerFactory.getLogger(getClass())
		.debug("Setting {} put", settings.stream().map(Setting::getName).collect(Collectors.joining(",")));
    }

    /**
     * @param setting
     */
    protected void onSettingReplaced(List<Setting> settings) {

	GSLoggerFactory.getLogger(getClass())
		.debug("Setting {} replaced", settings.stream().map(Setting::getName).collect(Collectors.joining(",")));
    }

    /**
     * @param setting
     */
    protected void onSettingRemoved(List<Setting> settings) {

	GSLoggerFactory.getLogger(getClass())
		.debug("Setting {} removed", settings.stream().map(Setting::getName).collect(Collectors.joining(",")));
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

	GSLoggerFactory.getLogger(getClass()).debug("Configuration auto-reloaded");
    }

    /**
     * @param configuration
     */
    protected void init(Configuration configuration) {

	getDescriptors().//
		stream().//
		sorted(Comparator.comparingInt(TabDescriptor::getIndex)).//
		forEach(descriptor -> {

	    Renderable content = ConfigurationViewFactory.createTabContent(//
		    this,//
		    configuration, //
		    descriptor);

	    addTab(descriptor.getIndex(), descriptor.getLabel(), content);
	});
    }

    /**
     * @return
     */
    private Configuration retrieveConfiguration() {

	Configuration configuration = initConfiguration();

	configuration.addChangeEventListener(this);

	return configuration;
    }
}
