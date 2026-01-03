package eu.essi_lab.cfga.setting;

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

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.Validator;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Fabrizio
 */
public abstract class AbstractSetting extends ConfigurationObject {

    /**
     *
     */
    public static final Property<Boolean> COMPACT_MODE = Property.of("CompactMode", "compactMode", true, Optional.of(false)); //
    /**
     *
     */
    public static final Property<Boolean> FOLDED_MODE = Property.of("FoldedMode", "foldedMode", true, Optional.of(false));//
    /**
     *
     */
    public static final Property<Boolean> CAN_BE_REMOVED = Property.of("CanBeRemoved", "canBeRemoved", true, Optional.of(false)); //
    /**
     *
     */
    public static final Property<Boolean> CAN_BE_CLEANED = Property.of("CanBeCleaned", "canBeCleaned", true, Optional.of(true)); //
    /**
     *
     */
    public static final Property<Boolean> SHOW_HEADER = Property.of("ShowHeader", "showHeader", true, Optional.of(true));

    /**
     *
     */
    public static final Property<? extends ObjectExtension> EXTENSION = Property.of("Extension", "extensionClass", false, Optional.empty());

    /**
     *
     */
    public static final Property<? extends Validator> VALIDATOR = Property.of("Validator", "validatorClass", false, Optional.empty());

    public AbstractSetting() {
    }

    /**
     * @param object
     */
    public AbstractSetting(JSONObject object) {
	super(object);
    }

    /**
     * @param object
     */
    public AbstractSetting(String object) {
	super(object);
    }

    /**
     * @param option
     */
    public void addOption(Option<?> option) {

	if (!this.isEnabled()) {
	    option.setEnabled(false);
	}

	if (!this.isVisible()) {
	    option.setVisible(false);
	}

	int size = getOptions().size();
	option.setPosition(size++);

	getObject().put(option.getKey(), option.getObject());
    }

    /**
     * @param optionKey
     * @return
     */
    public boolean removeOption(String optionKey) {

	if (getObject().has(optionKey)) {

	    getObject().remove(optionKey);
	    return true;
	}

	return false;
    }

    /**
     * @return
     */
    public List<Option<?>> getOptions() {

	return keys().//
		stream().//
		filter(k -> { //

	    Object obj = getObject().get(k);

	    if (obj instanceof JSONObject jsonObject) {

		return jsonObject.has(OBJECT_TYPE.getKey()) && jsonObject.getString(OBJECT_TYPE.getKey()).equals("option");
	    }

	    return false;
	}).

		map(k -> new Option<>(getObject().getJSONObject(k))).//

		sorted(Option::sort).//

		collect(Collectors.toList());
    }

    /**
     * @param key
     * @return
     */
    public Optional<Option<?>> getOption(String key) {

	return getOptions().//
		stream().//
		filter(o -> o.getKey().equals(key)).//
		findFirst();
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> List<Option<T>> getOptions(Class<T> type) {

	return getOptions().//
		stream().//
		filter(o -> o.getObject().get("valueClass").equals(type.getName())).//
		map(o -> (Option<T>) o).//
		collect(Collectors.toList());
    }

    /**
     * @param key
     * @param type
     * @return
     */
    public <T> Optional<Option<T>> getOption(String key, Class<T> type) {

	return getOptions(type).//
		stream().//
		filter(o -> o.getKey().equals(key)).//
		findFirst();
    }

    /**
     * Default: true - This property tells the client to compact all the options inside a component such as an accordion or a details pane
     */
    public void enableCompactMode(boolean set) {

	setProperty(COMPACT_MODE.getKey(), set, COMPACT_MODE.getDefaultValue().get());
    }

    /**
     * @return
     */
    public boolean isCompactModeEnabled() {

	return isPropertySet(COMPACT_MODE.getKey(), COMPACT_MODE.getDefaultValue().get());
    }

    /**
     * Default: false - This property tells the client to insert the whole setting component inside a component such as an accordion or a
     * details pane
     */
    public void enableFoldedMode(boolean set) {

	setProperty(FOLDED_MODE.getKey(), set, FOLDED_MODE.getDefaultValue().get());
    }

    /**
     * @return
     */
    public boolean isFoldedModeEnabled() {

	return isPropertySet(FOLDED_MODE.getKey(), FOLDED_MODE.getDefaultValue().get());
    }

    /**
     * Default: false
     *
     * @param canBeRemoved
     */
    public void setCanBeRemoved(boolean canBeRemoved) {

	setProperty(CAN_BE_REMOVED.getKey(), canBeRemoved, CAN_BE_REMOVED.getDefaultValue().get());
    }

    /**
     * @return
     */
    public boolean canBeRemoved() {

	return isPropertySet(CAN_BE_REMOVED.getKey(), CAN_BE_REMOVED.getDefaultValue().get());
    }

    /**
     * Default: true
     */
    public void setCanBeCleaned(boolean canBeCleaned) {

	setProperty(CAN_BE_CLEANED.getKey(), canBeCleaned, CAN_BE_CLEANED.getDefaultValue().get());
    }

    /**
     * @return
     */
    public boolean canBeCleaned() {

	return isPropertySet(CAN_BE_CLEANED.getKey(), CAN_BE_CLEANED.getDefaultValue().get());
    }

    /**
     * Default: true - - This property tells the client to show/hide the header
     */
    public void setShowHeader(boolean showHeader) {

	setProperty(SHOW_HEADER.getKey(), showHeader, SHOW_HEADER.getDefaultValue().get());
    }

    /**
     * @return
     */
    public boolean isShowHeaderSet() {

	return isPropertySet(SHOW_HEADER.getKey(), SHOW_HEADER.getDefaultValue().get());
    }

    /**
     * @param extension
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends ObjectExtension> Optional<T> getExtension(Class<T> extension) {

	try {

	    if (getObject().has(EXTENSION.getKey())) {

		Class<?> clazz = Class.forName(getObject().getString(EXTENSION.getKey()));

		if (extension.isAssignableFrom(clazz)) {

		    return Optional.of((T) Class.forName(getObject().getString(EXTENSION.getKey())).getDeclaredConstructor().newInstance());
		}
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param extension
     */
    public void setExtension(ObjectExtension extension) {

	getObject().put(EXTENSION.getKey(), extension.getClass().getName());
    }

    /**
     * @return
     */
    public Optional<Class<? extends ObjectExtension>> getExtensionClass() {

	if (getObject().has(EXTENSION.getKey())) {

	    String extensionClass = getObject().getString(EXTENSION.getKey());

	    try {

		@SuppressWarnings("unchecked")
		Class<? extends ObjectExtension> clazz = (Class<? extends ObjectExtension>) Class.forName(extensionClass);

		return Optional.of(clazz);

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).warn(e.getMessage(), e);
	    }
	}

	return Optional.empty();
    }

    /**
     * @param validator
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends Validator> Optional<T> getValidator() {

	try {

	    if (getObject().has(VALIDATOR.getKey())) {

		return Optional.of((T) Class.forName(getObject().getString(VALIDATOR.getKey())).getDeclaredConstructor().newInstance());
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param validator
     */
    public void setValidator(Validator validator) {

	getObject().put(VALIDATOR.getKey(), validator.getClass().getName());
    }

    /**
     *
     */
    public void removeValidator() {

	getObject().remove(VALIDATOR.getKey());

    }

    /**
     * @return
     */
    public Optional<Class<? extends Validator>> getOptionalValidatorClass() {

	if (getObject().has(VALIDATOR.getKey())) {

	    String validatorClass = getObject().getString(VALIDATOR.getKey());

	    try {

		@SuppressWarnings("unchecked")
		Class<? extends Validator> clazz = (Class<? extends Validator>) Class.forName(validatorClass);

		return Optional.of(clazz);

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).warn(e.getMessage(), e);
	    }
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    protected List<Property<?>> getProperties() {

	List<Property<?>> properties = super.getProperties();

	properties.addAll(//
		Arrays.asList(//
			COMPACT_MODE, //
			FOLDED_MODE, //
			CAN_BE_REMOVED, //
			CAN_BE_CLEANED, //
			SHOW_HEADER, //
			EXTENSION, //
			VALIDATOR));

	return properties;
    }

    /**
     * @param configuration
     * @param context
     * @return
     */
    public Optional<ValidationResponse> validate(Configuration configuration, ValidationContext context) {

	Optional<Validator> validator = getValidator();

	return validator.map(value -> value.validate(configuration, (Setting) this, context));

    }

    @Override
    protected String initObjectType() {

	return "setting";
    }
}
