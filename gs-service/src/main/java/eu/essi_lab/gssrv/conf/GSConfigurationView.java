package eu.essi_lab.gssrv.conf;

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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.quartz.SchedulerException;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;

import eu.essi_lab.authorization.userfinder.UserFinder;
import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.GDCSourcesSetting;
import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.OntologySetting.OntologySettingComponentInfo;
import eu.essi_lab.cfga.gs.setting.SourcePrioritySetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting.KeyValueOptionKeys;
import eu.essi_lab.cfga.gs.setting.database.SourceStorageSetting;
import eu.essi_lab.cfga.gs.setting.distribution.DistributionSetting.DistributionSettingComponentInfo;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting.HarvestingSettingComponentInfo;
import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting;
import eu.essi_lab.cfga.gui.ConfigurationView;
import eu.essi_lab.cfga.gui.LogOutButtonListener;
import eu.essi_lab.cfga.gui.dialog.NotificationDialog;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.ValidationResponse.ValidationResult;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.gssrv.starter.DABStarter;
import eu.essi_lab.harvester.worker.HarvestingSettingImpl;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.JavaOptions;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
@Route(value = "/")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@CssImport(value = "./styles/vaadin-item-styles.css", themeFor = "vaadin-item")
@CssImport(value = "./styles/vaadin-list-box-styles.css", themeFor = "vaadin-list-box")
@CssImport(value = "./styles/vaadin-vertical-layout-styles.css", themeFor = "vaadin-vertical-layout")
@CssImport(value = "./styles/vaadin-horizontal-layout-styles.css", themeFor = "vaadin-horizontal-layout")
@CssImport(value = "./styles/vaadin-select-styles.css", themeFor = "vaadin-select")
@CssImport(value = "./styles/vaadin-dialog-styles.css", themeFor = "vaadin-dialog-overlay")
@CssImport(value = "./styles/vaadin-app-layout.css", themeFor = "vaadin-app-layout")
@CssImport(value = "./styles/vaadin-grid.css", themeFor = "vaadin-grid")
@CssImport(value = "./styles/vaadin-text-area-no-border.css", themeFor = "vaadin-text-area")
@CssImport(value = "./styles/vaadin-text-area-no-margin-top.css", themeFor = "vaadin-text-area")
@CssImport(value = "./styles/vaadin-text-field-no-border.css", themeFor = "vaadin-text-field")

@SuppressWarnings("serial")
public class GSConfigurationView extends ConfigurationView {

    static SettingLinkedList<SchedulerWorkerSetting> newWorkerSettingList;
    static SettingLinkedList<SchedulerWorkerSetting> pausedWorkerSettingList;
    static SettingLinkedList<SchedulerWorkerSetting> rescheduledWorkerSettingList;
    static SettingLinkedList<SchedulerWorkerSetting> unscheduledWorkerSettingList;
    static SettingLinkedList<Setting> putSettingList;
    static SettingLinkedList<Setting> removedSettingList;
    static SettingLinkedList<Setting> editedSettingList;
    static LinkedList<String> additionalRemovalInfo;

    /**
     * 
     */
    public GSConfigurationView() {

	setHeaderText("Configuration GUI");

	setHeaderImageUrl("https://raw.githubusercontent.com/ESSI-Lab/DAB/main/DAB.png", 44);

	getHeaderImage().getStyle().set("margin-top", "15px");

	// removeDrawerToggle();

	if (!tabAlreadyOpen) {
	    //
	    // logoutButton = new CustomButton(VaadinIcon.SIGN_OUT.create());
	    // logoutButton.addThemeVariants(ButtonVariant.LUMO_LARGE);
	    // logoutButton.addClickListener(new LogOutButtonListener(this));
	    // logoutButton.setTooltip("Logout");
	    //
	    // logoutButton.getStyle().set("border", "1px solid hsl(0deg 0% 81%");
	    // logoutButton.getStyle().set("margin-left", "100px");
	    // logoutButton.getStyle().set("background-color", "white");
	    //
	    // getNavbarContent().add(logoutButton);

	    initLists();
	}
    }

    @Override
    protected boolean isInitialized() {

	if (JavaOptions.isEnabled(JavaOptions.SKIP_AUTHORIZATION)) {

	    return true;
	}

	OAuthSetting setting = ConfigurationWrapper.getOAuthSetting();
	
	ValidationResponse response = setting.getValidator().get().validate(//
		DABStarter.configuration, //
		setting, //
		ValidationContext.edit());

	if (response.getResult() == ValidationResult.VALIDATION_FAILED) {

	    //
	    //
	    //

	    VaadinRequest request = VaadinService.getCurrentRequest();

	    HttpServletRequest httpServletRequest = ((VaadinServletRequest) request).getHttpServletRequest();

	    String requestURL = httpServletRequest.getRequestURL().toString();

	    OAuthSetting oAuthSetting = ConfigurationWrapper.getOAuthSetting();

	    AdminConfigurationDialog editDialog = new AdminConfigurationDialog(//
		    //
		    // at this point we use the original configuration, not the clone
		    //
		    DABStarter.configuration, //
		    oAuthSetting, //
		    requestURL);

	    editDialog.open();

	    return false;
	}

	return true;
    }

    @Override
    protected boolean isAuthorized() {

	if (JavaOptions.isEnabled(JavaOptions.SKIP_AUTHORIZATION)) {

	    return true;
	}

	VaadinRequest request = VaadinService.getCurrentRequest();

	HttpServletRequest httpServletRequest = ((VaadinServletRequest) request).getHttpServletRequest();

	String requestURL = httpServletRequest.getRequestURL().toString();

	GSUser user = null;

	try {
	    user = UserFinder.findCurrentUser(httpServletRequest);

	} catch (GSException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getErrorInfoList().get(0).getErrorDescription(), e.getCause());
	    return false;
	}

	String userId = user.getIdentifier();

	//
	// this is required to allow all ESSI-Lab users with no registered OAuth 2.0 Client IDs
	//
	if (userId.contains(GSUser.ESSI_LAB_DOMAIN)) {

	    return true;
	}

	Optional<String> adminId = ConfigurationWrapper.readAdminIdentifier();

	if (!adminId.isPresent() || !adminId.get().equals(userId)) {

	    LoginDialog dialog = new LoginDialog(requestURL);

	    dialog.open();

	    return false;
	}

	return true;
    }

    /**
     * @return
     */
    protected boolean showLogOutButton() {

	return true;
    }

    /**
     * @return
     */
    protected LogOutButtonListener getLogOutButtonListener() {

	return new GSLogoutButtonListener(this);
    }

    /**
     * @return
     */
    protected boolean logOutAfterInactivity() {

	return ExecutionMode.get() != ExecutionMode.MIXED && ExecutionMode.get() != ExecutionMode.LOCAL_PRODUCTION;
    }

    /**
     * @return
     */
    protected boolean enableMultipleTabs() {

	SystemSetting systemSettings = ConfigurationWrapper.getSystemSettings();

	Optional<Properties> keyValueOption = systemSettings.getKeyValueOptions();

	if (keyValueOption.isPresent()) {

	    String multipleTabs = keyValueOption.get().getOrDefault(KeyValueOptionKeys.MULTIPLE_CONFIGURATION_TABS.getLabel(), "false")
		    .toString();
	    return Boolean.parseBoolean(multipleTabs);
	}

	return false;
    }

    @Override
    protected void onSaveButtonClicked(ClickEvent<Button> event) {

	SaveConfirmationDialog dialog = new SaveConfirmationDialog(this);

	dialog.open();
    }

    /**
     * @param event
     */
    @Override
    protected void onAttachEvent(AttachEvent event) {

	initLists();
    }

    @Override
    public void configurationChanged(ConfigurationChangeEvent event) {

	super.configurationChanged(event);

	switch (event.getEventType()) {
	case ConfigurationChangeEvent.SETTING_PUT:
	case ConfigurationChangeEvent.SETTING_REPLACED:
	case ConfigurationChangeEvent.SETTING_REMOVED:

	    Configuration clone = getConfiguration().clone();

	    SelectionUtils.deepClean(clone);

	    boolean changes = !DABStarter.configuration.equals(clone);

	    getSaveButton().setEnabled(changes);
	}
    }

    /**
     * @param settings
     */
    protected void onSettingPut(List<Setting> settings) {

	super.onSettingPut(settings);

	Setting setting = settings.get(0);

	putSettingList.add(setting);

	if (SchedulerWorkerSetting.class.isAssignableFrom(setting.getSettingClass())) {

	    SchedulerWorkerSetting workerSetting = SettingUtils.downCast(setting, SchedulerWorkerSetting.class);

	    if (!workerSetting.getScheduling().isEnabled()) {

		GSLoggerFactory.getLogger(getClass()).info("Scheduling disabled, nothing to schedule");
		return;
	    }

	    newWorkerSettingList.add(workerSetting);
	}
    }

    /**
     * @param setting
     */
    protected void onSettingReplaced(List<Setting> settings) {

	super.onSettingReplaced(settings);

	Setting setting = settings.get(0);

	editedSettingList.add(setting);

	if (SchedulerWorkerSetting.class.isAssignableFrom(setting.getSettingClass())) {

	    SchedulerWorkerSetting workerSetting = SettingUtils.downCast(setting, SchedulerWorkerSetting.class);

	    if (!workerSetting.getScheduling().isEnabled()) {

		GSLoggerFactory.getLogger(getClass()).info("Scheduling disabled, current scheduled worker will be paused");

		pausedWorkerSettingList.add(workerSetting);

		return;
	    }

	    rescheduledWorkerSettingList.add(workerSetting);
	}
    }

    /**
     * @param setting
     */
    protected void onSettingRemoved(List<Setting> settings) {

	super.onSettingRemoved(settings);

	Setting setting = settings.get(0);

	removedSettingList.add(setting);

	if (HarvestingSetting.class.isAssignableFrom(setting.getSettingClass())) {

	    HarvestingSetting harvSetting = SettingUtils.downCast(setting, HarvestingSettingImpl.class);

	    GSSourceSetting sourceSetting = harvSetting.getSelectedAccessorSetting().getGSSourceSetting();

	    String sourceIdentifier = sourceSetting.getSourceIdentifier();

	    //
	    // GDCSourcesSetting
	    //

	    GDCSourcesSetting gdcSourceSetting = ConfigurationWrapper.getGDCSourceSetting(getConfiguration());

	    if (gdcSourceSetting.isGDCSource(sourceSetting.asSource())) {

		boolean deselected = gdcSourceSetting.deselectSource(sourceIdentifier);
		boolean replaced = getConfiguration().replace(gdcSourceSetting);

		if (deselected && replaced) {

		    additionalRemovalInfo.add("Source " + sourceSetting.asSource().getLabel() + " deselected from GDC list");

		} else {

		    GSLoggerFactory.getLogger(getClass()).error("Unable to deselect source " + sourceIdentifier + " from GDC list");
		}
	    }

	    //
	    // SourcePrioritySetting
	    //

	    SourcePrioritySetting sourcePrioritySetting = ConfigurationWrapper.getSourcePrioritySetting(getConfiguration());

	    if (sourcePrioritySetting.isPrioritySource(sourceSetting.asSource())) {

		boolean deselected = sourcePrioritySetting.deselectSource(sourceIdentifier);
		boolean replaced = getConfiguration().replace(sourcePrioritySetting);

		if (deselected && replaced) {

		    additionalRemovalInfo.add("Source " + sourceSetting.asSource().getLabel() + " deselected from source priority list");

		} else {

		    GSLoggerFactory.getLogger(getClass())
			    .error("Unable to deselect source " + sourceIdentifier + " from source priority list");
		}
	    }

	    //
	    // SourceStorageSetting
	    //

	    SourceStorageSetting sourceStorageSetting = ConfigurationWrapper.getSourceStorageSettings(getConfiguration());

	    boolean sourceStorageSettingChanged = false;

	    if (sourceStorageSetting.isISOComplianceTestSet(sourceIdentifier)) {

		sourceStorageSetting.removeTestISOCompliance(sourceIdentifier);

		additionalRemovalInfo.add("Source " + sourceSetting.asSource().getLabel() + " deselected from ISO compliance test list");

		sourceStorageSettingChanged = true;
	    }

	    if (sourceStorageSetting.isMarkDeletedOption(sourceIdentifier)) {

		sourceStorageSetting.removeMarkDeleted(sourceIdentifier);

		additionalRemovalInfo.add("Source " + sourceSetting.asSource().getLabel() + " deselected from mark deleted list");

		sourceStorageSettingChanged = true;
	    }

	    if (sourceStorageSetting.isRecoverResourceTagsSet(sourceIdentifier)) {

		sourceStorageSetting.removeRecoverResourceTags(sourceIdentifier);

		additionalRemovalInfo.add("Source " + sourceSetting.asSource().getLabel() + " deselected from recovery resource tags list");

		sourceStorageSettingChanged = true;
	    }

	    if (sourceStorageSetting.isSmartStorageDisabledSet(sourceIdentifier)) {

		sourceStorageSetting.removeSmartStorageDisabledSet(sourceIdentifier);

		additionalRemovalInfo.add("Source " + sourceSetting.asSource().getLabel() + " deselected from smart storage list");

		sourceStorageSettingChanged = true;
	    }

	    if (sourceStorageSettingChanged) {

		boolean replaced = getConfiguration().replace(sourceStorageSetting);

		if (!replaced) {

		    GSLoggerFactory.getLogger(getClass()).error("Unable to replace SourceStorageSetting");
		}
	    }
	}

	if (SchedulerWorkerSetting.class.isAssignableFrom(setting.getSettingClass())) {

	    SchedulerWorkerSetting workerSetting = SettingUtils.downCast(setting, SchedulerWorkerSetting.class);

	    unscheduledWorkerSettingList.add(workerSetting);
	}
    }

    /**
     * This method is called every time the client window is loaded.<br>
     * Returns a clone with disabled autoreload of the {@link DABStarter#configuration}. Changes applied to this
     * configuration instance are
     * not applied to the source configuration until this configuration is flushed and the source configuration performs
     * the autoreload.<br>
     * This avoid to apply intermediate configuration changes to the source configuration which is the core
     * of the {@link ConfigurationWrapper}.<br>
     * When this cloned configuration is flushed, the source configuration and all the other instances of the other
     * suite nodes will apply the changes in maximum <i>autoreload-time</i>, that is currently set in 30 seconds.<br>
     * <br>
     * The source event is read-only and has the autoreload enabled, so this is the only event that the configuration
     * can dispatch. Because of this, in order to receive these events, this view should register itself as listener
     * also to the source
     * configuration (the registration to the this returned instance is done by the superclass).
     * At the moment this is not done since not strictly required
     */
    @Override
    protected Configuration initConfiguration() {

	// GIPStarter.configuration.addChangeEventListener(this);

	Configuration clone = DABStarter.configuration.clone();

	return clone;
    }

    @Override
    protected List<ComponentInfo> getAdditionalsComponentInfo() {

	return Arrays.asList(//
		new DistributionSettingComponentInfo(), //
		new HarvestingSettingComponentInfo(), //
		new SourcesInspector(), //
		new ConfigUploader(), //
		new AboutComponentInfo(),
		new OntologySettingComponentInfo()//
	);
    }

    /**
     * 
     */
    void updateScheduler() {

	Scheduler scheduler = SchedulerFactory.getScheduler(ConfigurationWrapper.getSchedulerSetting());

	if (!newWorkerSettingList.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).info("Scheduling new jobs STARTED");

	    for (SchedulerWorkerSetting setting : newWorkerSettingList) {

		try {

		    scheduler.schedule(setting);

		} catch (SchedulerException e) {

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

		    NotificationDialog.getErrorDialog(e.getMessage(), e).open();
		}
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Scheduling new jobs ENDED");
	}

	if (!rescheduledWorkerSettingList.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).info("Rescheduling jobs STARTED");

	    for (SchedulerWorkerSetting setting : rescheduledWorkerSettingList) {

		try {

		    scheduler.reschedule(setting);

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

		    NotificationDialog.getErrorDialog(e.getMessage(), e).open();
		}
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Rescheduling jobs ENDED");
	}

	if (!pausedWorkerSettingList.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).info("Pausing jobs STARTED");

	    for (SchedulerWorkerSetting setting : pausedWorkerSettingList) {

		try {

		    scheduler.pause(setting);

		} catch (SchedulerException e) {

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

		    NotificationDialog.getErrorDialog(e.getMessage(), e).open();
		}
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Pausing jobs ENDED");
	}

	if (!unscheduledWorkerSettingList.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).info("Unscheduling jobs STARTED");

	    for (SchedulerWorkerSetting setting : unscheduledWorkerSettingList) {

		try {

		    scheduler.unschedule(setting);

		} catch (SchedulerException e) {

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

		    NotificationDialog.getErrorDialog(e.getMessage(), e).open();
		}
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Unscheduling jobs ENDED");
	}
    }

    /**
     * 
     */
    void initLists() {

	// GSLoggerFactory.getLogger(getClass()).debug("Initializing settings list");

	newWorkerSettingList = new SettingLinkedList<>();
	pausedWorkerSettingList = new SettingLinkedList<>();
	rescheduledWorkerSettingList = new SettingLinkedList<>();
	unscheduledWorkerSettingList = new SettingLinkedList<>();

	putSettingList = new SettingLinkedList<>();
	removedSettingList = new SettingLinkedList<>();
	editedSettingList = new SettingLinkedList<>();

	additionalRemovalInfo = new LinkedList<String>();
    }

    /**
     * @author Fabrizio
     */
    class SettingLinkedList<S extends Setting> extends LinkedList<S> {

	@Override
	public boolean add(S setting) {

	    if (notIn(setting)) {

		return super.add(setting);
	    }

	    return false;
	}

	/**
	 * @return
	 */
	private boolean notIn(S setting) {

	    return !stream().//
		    filter(s -> s.getIdentifier().equals(setting.getIdentifier())).//
		    findFirst().//
		    isPresent();
	}
    }
}
