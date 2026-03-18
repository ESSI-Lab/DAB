package eu.essi_lab.cfga.gs.setting.service;

import com.vaadin.flow.data.provider.*;
import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gui.components.grid.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.cfga.gui.directive.*;
import eu.essi_lab.cfga.option.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.lib.net.services.*;
import eu.essi_lab.lib.utils.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class ManagedServiceSetting extends Setting implements EditableSetting, KeyValueOptionDecorator {

    private static final String SERVICE_NAME_OPTION_KEY = "serviceNameKey";
    private static final String SERVICE_DESCRIPTION_OPTION_KEY = "serviceDescKey";
    private static final String SERVICE_OPTIONS_OPTION_KEY = "serviceOptionsKey";
    private static final String SERVICE_ID_OPTION_KEY = "serviceIdOptionKey";

    /**
     *
     */
    public ManagedServiceSetting() {

	setCanBeRemoved(true);
	setCanBeDisabled(false);
	enableCompactMode(false);

	setName("Managed service settings");

	setDescription("A customizable service which executes the code provided by an implementation of the 'ManagedService' interface");

	//
	// Options
	//

	Option<String> serviceNameOption = StringOptionBuilder.get().//
		withKey(SERVICE_NAME_OPTION_KEY).//
		withLabel("Service name").//
		cannotBeDisabled().//
		withSingleSelection().//
		withValuesLoader(new ServiceValuesLoader()).//
		withValues(ServiceValuesLoader.getValues()).//
		required().//
		build();

	addOption(serviceNameOption);

	Option<String> serviceIdOption = StringOptionBuilder.get().//
		withKey(SERVICE_ID_OPTION_KEY).//
		withInputPattern(InputPattern.ALPHANUMERIC_AND_UNDERSCORE).//
		withLabel("Service identifier").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(serviceIdOption);

	Option<String> serviceDescriptionOption = StringOptionBuilder.get().//
		withKey(SERVICE_DESCRIPTION_OPTION_KEY).//
		withLabel("Service description").//
		withValue("No description provided").//
		cannotBeDisabled().//
		build();

	addOption(serviceDescriptionOption);

	Option<String> serviceOptionsOption = StringOptionBuilder.get().//
		withKey(SERVICE_OPTIONS_OPTION_KEY).//
		withLabel("Service options").//
		withTextArea().//
		withDescription("A customizable set of options for the service. The content of these options, the way"
		+ " they are provided and parsed, are completely demanded to the specific service implementation").//
		cannotBeDisabled().//
		build();

	addOption(serviceOptionsOption);

	//
	// Key-value options
	//

	addKeyValueOption();
    }

    /**
     * @author Fabrizio
     */
    public static class ServiceValuesLoader extends ValuesLoader<String> {

	public static List<String> getValues() {

	    ServiceLoader<ManagedService> loader = ServiceLoader.load(ManagedService.class);

	    return StreamUtils.iteratorToStream(loader.iterator()).//
		    map(ManagedService::getName).//
		    sorted().//
		    toList();
	}

	@Override
	protected List<String> loadValues(Optional<String> input) {

	    return getValues();
	}
    }

    /**
     * @author Fabrizio
     */
    public static class TabDescriptorProvider extends TabDescriptor {

	/**
	 *
	 */
	public TabDescriptorProvider() {

	    setLabel("Services");

	    TabContentDescriptor descriptor = TabContentDescriptorBuilder.get(ManagedServiceSetting.class).//

		    withShowDirective("Managed services", SortDirection.ASCENDING).//

		    withAddDirective(//
		    "ADD",//
		    "Add managed service", //
		    ManagedServiceSetting.class.getCanonicalName()).//
		    withRemoveDirective("REMOVE", "Remove managed service", true, ManagedServiceSetting.class.getCanonicalName()).//
		    withEditDirective("EDIT", "Edit managed service", Directive.ConfirmationPolicy.ON_WARNINGS).//

		    withGridInfo(Arrays.asList(//

		    ColumnDescriptor.create("Id", true, true, this::getId), //

		    ColumnDescriptor.create("Implementation", true, true, this::getImpl), //

		    ColumnDescriptor.create("Description", true, true, this::getDesc))).//

		    build();

	    setIndex(GSTabIndex.SERVICES.getIndex());
	    addContentDescriptor(descriptor);
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getDesc(Setting setting) {

	    return setting.getObject().getJSONObject(SERVICE_DESCRIPTION_OPTION_KEY).getJSONArray("values").getString(0);
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getImpl(Setting setting) {

	    return setting.getObject().getJSONObject(SERVICE_NAME_OPTION_KEY).getJSONArray("values").getString(0);
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getId(Setting setting) {

	    return setting.getObject().getJSONObject(SERVICE_ID_OPTION_KEY).getJSONArray("values").getString(0);
	}
    }

    /**
     * @return
     */
    public Optional<String> getServiceOptions() {

	return getOption(SERVICE_OPTIONS_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param options
     */
    public void setServiceOptions(String options) {

	getOption(SERVICE_OPTIONS_OPTION_KEY, String.class).get().setValue(options);
    }

    /**
     * @return
     */
    public String getServiceDescription() {

	return getOption(SERVICE_DESCRIPTION_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @param desc
     */
    public void setServiceDescription(String desc) {

	getOption(SERVICE_DESCRIPTION_OPTION_KEY, String.class).get().setValue(desc);
    }

    /**
     * @return
     */
    public void setServiceId(String id) {

	getOption(SERVICE_ID_OPTION_KEY, String.class).get().setValue(id);
    }

    /**
     * @param desc
     */
    public String getServiceId() {

	return getOption(SERVICE_ID_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @return
     */
    public void selectServiceName(String name) {

	getOption(SERVICE_NAME_OPTION_KEY, String.class).get().select(n -> n.equals(name));
    }

    /**
     * @return
     */
    public String getSelectedServiceName() {

	return getOption(SERVICE_NAME_OPTION_KEY, String.class).get().getSelectedValue();
    }

    /**
     * @return
     */
    public Class<? extends ManagedService> getSelectedServiceImpl() {

	ServiceLoader<ManagedService> loader = ServiceLoader.load(ManagedService.class);

	return StreamUtils.iteratorToStream(loader.iterator()).//
		filter(service -> service.getName().equals(getSelectedServiceName())).//
		map(ManagedService::getClass). //
		findFirst().//
		get();//
    }

}
