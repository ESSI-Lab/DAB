package eu.essi_lab.cfga.gs.setting.service;

import com.vaadin.flow.data.provider.*;
import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gui.components.grid.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.cfga.gui.directive.*;
import eu.essi_lab.cfga.option.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.cfga.setting.validation.*;
import eu.essi_lab.lib.net.service.*;
import eu.essi_lab.lib.utils.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class ManagedServiceSetting extends Setting implements EditableSetting, KeyValueOptionDecorator {

    private static final String SERVICE_IMPL_OPTION_KEY = "serviceImpl";
    private static final String SERVICE_DESCRIPTION_OPTION_KEY = "serviceDesc";
    private static final String SERVICE_OPTIONS_OPTION_KEY = "service";
    static final String SERVICE_ID_OPTION_KEY = "serviceId";

    /**
     *
     */
    public ManagedServiceSetting() {

	setCanBeRemoved(true);
	setCanBeDisabled(true);
	enableCompactMode(false);

	setName("Managed service settings");

	setDescription("A customizable service provided by an implementation of the 'ManagedService' interface");

	//
	// Options
	//

	Option<String> serviceImplOption = StringOptionBuilder.get().//
		withKey(SERVICE_IMPL_OPTION_KEY).//
		withLabel("Service implementation").//
		cannotBeDisabled().//
		withSingleSelection().//
		withValuesLoader(new ServiceValuesLoader()).//
		withValues(ServiceValuesLoader.getValues()).//
		withSelectedValue(ServiceValuesLoader.getValues().getFirst()).//
		required().//
		build();

	addOption(serviceImplOption);

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

	//
	// set the validator
	//
	setValidator(new ManagedServiceValidator());
    }

    /**
     * @author Fabrizio
     */
    public static class ManagedServiceValidator implements Validator {

	@Override
	public ValidationResponse validate(Configuration configuration, Setting setting, ValidationContext context) {

	    ManagedServiceSetting manSetting = SettingUtils.downCast(setting, ManagedServiceSetting.class);

	    List<Setting> list = new ArrayList<>();

	    ConfigurationUtils.deepFind(configuration, s -> s.getSettingClass().equals(ManagedServiceSetting.class), list);

	    List<String> ids = list.stream()
		    .map(s -> s.getOption(ManagedServiceSetting.SERVICE_ID_OPTION_KEY, String.class).get().getValue()).toList();

	    ValidationResponse response = new ValidationResponse();

	    String impl = manSetting.getSelectedImplementation();

	    if (impl == null) {

		response.getErrors().add("Service implementation is required");
		response.setResult(ValidationResponse.ValidationResult.VALIDATION_FAILED);
	    }

	    if (ids.contains(manSetting.getServiceId())) {

		response.getErrors().add("Provided service id is already in use");
		response.setResult(ValidationResponse.ValidationResult.VALIDATION_FAILED);
	    }

	    return response;
	}
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

		    withShowDirective("Customizable services provided by an implementation of the 'ManagedService' interface. "
		    + "\n\nServices can run only on the local node, or they can be distributed on a multi-node cluster."
		    + "according to the 'Session coordinator' setting in the 'System' tab", SortDirection.ASCENDING).//

		    withAddDirective(//
		    "ADD",//
		    "Add managed service", //
		    ManagedServiceSetting.class.getCanonicalName()).//
		    withRemoveDirective("REMOVE", "Remove managed service", true, ManagedServiceSetting.class.getCanonicalName()).//
		    withEditDirective("EDIT", "Edit managed service", Directive.ConfirmationPolicy.ON_WARNINGS).//

		    withGridInfo(Arrays.asList(//

		    ColumnDescriptor.create("Id", true, true, this::getId), //

		    ColumnDescriptor.create("Implementation", true, true, this::getImpl), //

		    ColumnDescriptor.create("Description", true, true, this::getDesc),//

		    ColumnDescriptor.create("Status", true, true, s -> ManagedServiceSupport.getInstance().getServiceStatus(s)),

		    ColumnDescriptor.create("Host", true, true, s -> ManagedServiceSupport.getInstance().getServiceHost(s))

	    )).//
		    reloadable(() -> ManagedServiceSupport.getInstance().update()).//

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

	    return setting.getObject().getJSONObject(SERVICE_IMPL_OPTION_KEY).getJSONArray("values").getString(0);
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
    public void selectImplementation(String impl) {

	getOption(SERVICE_IMPL_OPTION_KEY, String.class).get().select(n -> n.equals(impl));
    }

    /**
     * @return
     */
    public String getSelectedImplementation() {

	return getOption(SERVICE_IMPL_OPTION_KEY, String.class).get().getSelectedValue();
    }

    /**
     * @return
     */
    public Class<? extends ManagedService> getSelectedImplementationClass() {

	ServiceLoader<ManagedService> loader = ServiceLoader.load(ManagedService.class);

	return StreamUtils.iteratorToStream(loader.iterator()).//
		filter(service -> service.getName().equals(getSelectedImplementation())).//
		map(ManagedService::getClass). //
		findFirst().//
		get();//
    }

}
