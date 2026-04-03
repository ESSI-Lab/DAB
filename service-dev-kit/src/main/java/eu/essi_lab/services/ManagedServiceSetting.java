package eu.essi_lab.services;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.option.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.cfga.setting.validation.*;
import eu.essi_lab.lib.utils.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class ManagedServiceSetting extends Setting implements EditableSetting, KeyValueOptionDecorator {

    public static final String SERVICE_IMPL_OPTION_KEY = "serviceImpl";
    public static final String SERVICE_DESCRIPTION_OPTION_KEY = "serviceDesc";
    public static final String SERVICE_ID_OPTION_KEY = "serviceId";

    private static final String SERVICE_OPTIONS_OPTION_KEY = "serviceOpt";

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
     *
     */
    public void loadServiceImpl() {

	ServiceValuesLoader loader = new ServiceValuesLoader();
	List<String> values = loader.loadValues(Optional.empty());

	getOption(SERVICE_IMPL_OPTION_KEY, String.class).get().setValues(values);
    }

    /**
     * @param id
     * @param clazz
     * @return
     */
    public static ManagedServiceSetting of(String id, Class<? extends ManagedService> clazz) {

	ManagedServiceSetting setting = new ManagedServiceSetting();

	setting.loadServiceImpl();

	setting.setServiceId(id);
	setting.selectImpl(clazz.getName());

	SelectionUtils.deepClean(setting);

	return setting;
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

	    String impl = manSetting.getSelectedImpl();

	    if (impl == null) {

		response.getErrors().add("Service implementation is required");
		response.setResult(ValidationResponse.ValidationResult.VALIDATION_FAILED);
	    }

	    String serviceId = manSetting.getServiceId();

	    if (serviceId == null || serviceId.isEmpty()) {

		response.getErrors().add("Service id is required");
		response.setResult(ValidationResponse.ValidationResult.VALIDATION_FAILED);

	    } else if (Objects.equals(context.getContext(), ValidationContext.PUT) && ids.contains(serviceId)) {

		response.getErrors().add("Provided service id is already in use");
		response.setResult(ValidationResponse.ValidationResult.VALIDATION_FAILED);
	    }

	    return response;
	}
    }

    /**
     * @return
     */
    public ManagedService createService() {

	try {

	    ManagedService service = getSelectedImplClass().getDeclaredConstructor().newInstance();
	    service.configure(this);

	    return service;

	} catch (Exception e) {

	    throw new RuntimeException(e);
	}
    }

    /**
     * @author Fabrizio
     */
    public static class ServiceValuesLoader extends ValuesLoader<String> {

	public static List<String> getValues() {

	    ServiceLoader<ManagedService> loader = ServiceLoader.load(ManagedService.class);

	    return StreamUtils.iteratorToStream(loader.iterator()).//
		    map(ManagedService::getType).//
		    sorted().//
		    toList();
	}

	@Override
	protected List<String> loadValues(Optional<String> input) {

	    return getValues();
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
    public void selectImpl(String impl) {

	getOption(SERVICE_IMPL_OPTION_KEY, String.class).get().select(n -> n.equals(impl));
    }

    /**
     * @return
     */
    public String getSelectedImpl() {

	return getOption(SERVICE_IMPL_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @return
     */
    public Class<? extends ManagedService> getSelectedImplClass() {

	ServiceLoader<ManagedService> loader = ServiceLoader.load(ManagedService.class);

	return StreamUtils.iteratorToStream(loader.iterator()).//
		filter(service -> service.getType().equals(getSelectedImpl())).//
		map(ManagedService::getClass). //
		findFirst().//
		get();//
    }

    @Override
    public boolean equals(Object o) {

	return o instanceof ManagedServiceSetting other && //
		Objects.equals(toString(), other.toString());
    }

}
