/**
 *
 */
package eu.essi_lab.cfga.gs.setting;

import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.data.provider.*;
import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.gs.setting.menuitems.*;
import eu.essi_lab.cfga.gui.components.grid.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.cfga.gui.directive.Directive.*;
import eu.essi_lab.cfga.option.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.cfga.setting.validation.*;
import eu.essi_lab.cfga.setting.validation.ValidationResponse.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public abstract class ProfilerSetting extends Setting implements KeyValueOptionDecorator {

    private static final String PATH_OPTION_KEY = "pathOption";
    private static final String TYPE_OPTION_KEY = "typeOption";
    private static final String VERSION_OPTION_KEY = "versionOption";
    private static final String STATE_OPTION = "stateOption";
    private static final String RESOURCE_CONSUMER_OPTION_KEY = "resourceConsumer";

    /**
     * @author Fabrizio
     */
    public enum KeyValueOptionKeys implements LabeledEnum {

	/**
	 * Maximum number of threads used by the discovery result set mapper
	 */
	RSM_THREADS_COUNT_PROPERTY("rsmThreadsCount");

	private String name;

	/**
	 * @param name
	 */
	private KeyValueOptionKeys(String name) {

	    this.name = name;
	}

	@Override
	public String toString() {

	    return getLabel();
	}

	@Override
	public String getLabel() {

	    return name;
	}
    }

    /**
     *
     */
    public static final int DEFAULT_RSM_THREADS_COUNT = 10;

    /**
     *
     */
    public ProfilerSetting() {

	setCanBeRemoved(true);
	setEditable(true);
	enableCompactMode(false);
	enableFoldedMode(true);
	setCanBeDisabled(false);

	Option<String> typeOption = StringOptionBuilder.get().//
		withKey(TYPE_OPTION_KEY).//
		withLabel("Type").//
		readOnly().//
		cannotBeDisabled().//
		build();

	addOption(typeOption);

	Option<String> pathOption = StringOptionBuilder.get().//
		withKey(PATH_OPTION_KEY).//
		withLabel("Path").//
		withDescription("Service path must be unique can contains alphanumeric characters and underscores").//
		withInputPattern(InputPattern.ALPHANUMERIC_AND_UNDERSCORE).//
		cannotBeDisabled().//
		build();

	addOption(pathOption);

	Option<String> versionOption = StringOptionBuilder.get().//
		withKey(VERSION_OPTION_KEY).//
		withLabel("Version").//
		readOnly().//
		cannotBeDisabled().//
		build();

	addOption(versionOption);

	//
	//
	//

	Option<String> onlineOption = StringOptionBuilder.get().//
		withKey(STATE_OPTION).//
		withLabel("Service state").//
		withDescription("If the service is offline, requests forwarded on the service path return a 404 'Not Found' error code").//
		withSingleSelection().//
		withValues(Arrays.asList("Online", "Offline")).//
		withSelectedValue("Online").//
		cannotBeDisabled().//
		build();

	addOption(onlineOption);

	//
	//
	//

	Option<String> resourceConsumerOption = StringOptionBuilder.get().//
		withKey(RESOURCE_CONSUMER_OPTION_KEY).//
		withLabel("Resource consumer").//
		withDescription(
		"If selected, the consumer will apply some changes (according to its implementation)" + " to the result set resources").//
		cannotBeDisabled().//
		withSingleSelection().//
		withValuesLoader(new ResourceConsumerLoader()).//
		withValues(ResourceConsumerLoader.getValues()).//
		build();

	addOption(resourceConsumerOption);

	//
	//
	//

	addKeyValueOption();

	//
	// set the validator
	//
	setValidator(new ProfilerSettingValidator());
    }

    /**
     * @author Fabrizio
     */
    public static class ResourceConsumerLoader extends ValuesLoader<String> {

	public static List<String> getValues() {

	    ServiceLoader<ResourceConsumer> loader = ServiceLoader.load(ResourceConsumer.class);

	    return StreamUtils.iteratorToStream(loader.iterator()).//
		    map(c -> c.getClass().getSimpleName()).//
		    sorted().//
		    collect(Collectors.toList());
	}

	/**
	 * @param simpleClass
	 * @return
	 */
	public static ResourceConsumer load(String simpleClass) {

	    ServiceLoader<ResourceConsumer> loader = ServiceLoader.load(ResourceConsumer.class);

	    return StreamUtils.iteratorToStream(loader.iterator()).//
		    filter(c -> c.getClass().getSimpleName().equals(simpleClass)).//
		    map(c -> {
		try {
		    return c.getClass().getDeclaredConstructor().newInstance();
		} catch (Exception e) {
		}
		return null;
	    }).filter(Objects::nonNull).//
		    findFirst().//
		    get();

	}

	@Override
	protected List<String> loadValues(Optional<String> input) {

	    return getValues();
	}
    }

    /**
     * @author Fabrizio
     */
    public static class ProfilerSettingValidator implements Validator {

	@Override
	public ValidationResponse validate(Configuration configuration, Setting setting, ValidationContext context) {

	    ProfilerSetting thisSetting = (ProfilerSetting) SettingUtils.downCast(setting, setting.getSettingClass());

	    ValidationResponse validationResponse = new ValidationResponse();

	    if (context.getContext().equals(ValidationContext.PUT)) {

		String servicePath = thisSetting.getServicePath();

		boolean exists = configuration.list(ProfilerSetting.class, false).//
			stream().//
			anyMatch(s -> s.getServicePath().equals(servicePath));

		if (exists) {

		    validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		    validationResponse.getErrors().add("Another profiler with path '" + servicePath + "' already exists");
		}
	    }

	    return validationResponse;
	}
    }

    /**
     * @author Fabrizio
     */
    public static class DescriptorProvider {

	private final TabContentDescriptor descriptor;

	/**
	 *
	 */
	public DescriptorProvider() {

	    String desc = "Manage DAB profilers. Profilers can be added, "
		    + "and removed; furthermore, their configuration, path and state can be modified. "
		    + "You can also add several profilers of the same type (e.g: OAI-PMH), making sure "
		    + "they have a different path and possibly, a different configuration. "
		    + "Once added, the profiler state is \"Online\"; if set to \"Offline\", "
		    + "its capabilities will no longer be available and each request will return " + "a 404 error code";

	    descriptor = TabContentDescriptorBuilder.get(ProfilerSetting.class).//

		    withLabel("Profilers").//
		    withAddDirective("Add profiler", ProfilerSettingSelector.class). //
		    withEditDirective("Edit profiler", ConfirmationPolicy.ON_WARNINGS).//
		    withRemoveDirective("Remove profiler", false, ProfilerSetting.class).//
		    withShowDirective(desc, SortDirection.ASCENDING).//
		    withGridInfo(Arrays.asList(//

		    ColumnDescriptor.createPositionalDescriptor(), //

		    ColumnDescriptor.create("Name", true, true, Setting::getName), //

		    ColumnDescriptor.create("State", 150, true, true, //

			    this::getServiceState, //

			    Comparator.comparing(item -> item.get("State")), //

			    new ProfilerStateColumnRenderer()), //

		    ColumnDescriptor.create("Path", 200, true, true, this::getServicePath), //

		    ColumnDescriptor.create("Type", 300, true, true, this::getServiceType), //

		    ColumnDescriptor.create("Version", true, true, this::getServiceVersion) //

	    ), getItemsList(), Grid.SelectionMode.MULTI).//

		    build();

	}

	/**
	 * @return
	 */
	public TabContentDescriptor get() {

	    return descriptor;
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getServiceState(Setting setting) {

	    return setting.getOption(STATE_OPTION, String.class).get().getSelectedValue();
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getServicePath(Setting setting) {

	    return setting.getOption(PATH_OPTION_KEY, String.class).get().getValue();
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getServiceType(Setting setting) {

	    return setting.getOption(TYPE_OPTION_KEY, String.class).get().getValue();
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getServiceVersion(Setting setting) {

	    return setting.getOption(VERSION_OPTION_KEY, String.class).get().getValue();
	}

	/**
	 * @return
	 */
	private List<GridMenuItemHandler> getItemsList() {

	    ArrayList<GridMenuItemHandler> list = new ArrayList<>();

	    list.add(new ProfilerStateOnlineItemHandler());
	    list.add(new ProfilerStateOfflineItemHandler(true, false));

	    return list;
	}
    }

    /**
     * @param online
     */
    public void setOnline(boolean online) {

	getOption(STATE_OPTION, String.class).get().select(state -> online ? state.equals("Online") : state.equals("Offline"));
    }

    /**
     * @return
     */
    public boolean isOnline() {

	return getOption(STATE_OPTION, String.class).get().getSelectedValue().equals("Online");
    }

    /**
     * Get the path where the "GI-suite service" is expected to receive the {@link Profiler} requests from the suitable clients
     *
     * @return a non <code>null</code> string which contains only alphabetic characters
     */
    public String getServicePath() {

	return getOption(PATH_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * Set the path where the "GI-suite service" is expected to receive the {@link Profiler} requests from the suitable clients
     *
     * @param path a non <code>null</code> string which contains only alphabetic characters
     */
    public void setServicePath(String path) {

	getOption(PATH_OPTION_KEY, String.class).get().setValue(path);
    }

    /**
     * Returns the {@link Profiler} name
     *
     * @return a non <code>null</code> string
     */
    public String getServiceName() {

	return getName();
    }

    /**
     * Set the {@link Profiler} name
     *
     * @param name a non <code>null</code> string
     */
    public void setServiceName(String name) {

	setName(name);
    }

    /**
     * Returns the type of the {@link Profiler} service
     *
     * @return a non <code>null</code> string
     */
    public String getServiceType() {

	return getOption(TYPE_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * Set the type of the {@link Profiler} service (e.g: "OAI-PMH", "OpenSearch", etc...) by adding the suffix "Profiler" to the type.<br>
     * This method also set the {@link #getConfigurableType()} with the same value
     *
     * @param type
     */
    public void setServiceType(String type) {

	type = type + "Profiler";

	getOption(TYPE_OPTION_KEY, String.class).get().setValue(type);
	setConfigurableType(type);
    }

    /**
     * Get the version of the {@link Profiler} service
     *
     * @return a non <code>null</code> string
     */
    public String getServiceVersion() {

	return getOption(VERSION_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * Set the version of the {@link Profiler} service (e.g: "OAI-PMH", "OpenSearch", etc...)
     *
     * @param version a non <code>null</code> string
     */
    public void setServiceVersion(String version) {

	getOption(VERSION_OPTION_KEY, String.class).get().setValue(version);
    }

    /**
     * @return
     */
    public Optional<ResourceConsumer> getConsumer() {

	return getOption(RESOURCE_CONSUMER_OPTION_KEY, String.class).//
		get().getOptionalSelectedValue().//
		map(simpleClass -> ResourceConsumerLoader.load(simpleClass));
    }

    /**
     * <ol>
     * <li>if the value of the key-value option {@link KeyValueOptionKeys#RSM_THREADS_COUNT_PROPERTY} is >= 0,
     * returns an {@link Optional} with such value</li>
     * <li>if the value of the key-value option {@link KeyValueOptionKeys#RSM_THREADS_COUNT_PROPERTY} is 0 or -1, then returns an
     * empty {@link Optional} and the <i>DiscoveryResultSetMapper</i>
     * will use a thread for each record in the result set</li>
     * <li>if no value is specified, then returns {@link Optional#of(DEFAULT_RSM_THREADS_COUNT)}</li>
     * </ol>
     *
     * @return
     */
    public Optional<Integer> getResultSetMapperThreadsCount() {

	int count = readKeyValue(KeyValueOptionKeys.RSM_THREADS_COUNT_PROPERTY.getLabel()).//
		map(Integer::parseInt).//
		orElse(DEFAULT_RSM_THREADS_COUNT); //

	return Optional.ofNullable(count == -1 || count == 0 ? null : count);
    }
}
