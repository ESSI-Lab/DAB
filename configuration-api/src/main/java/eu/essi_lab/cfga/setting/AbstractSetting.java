package eu.essi_lab.cfga.setting;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.Validator;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public abstract class AbstractSetting extends ConfigurationObject {

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

		    if (obj instanceof JSONObject) {

			JSONObject jsonObject = (JSONObject) obj;

			return jsonObject.has("type") && jsonObject.getString("type").equals("option");
		    }

		    return false;
		}).

		map(k -> new Option<>(getObject().getJSONObject(k))).//

		sorted(Option::sort).//

		collect(Collectors.toList());
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
     * Default: true
     * -
     * This property tells the client to compact all the options inside a component
     * such as an accordion or a details pane
     */
    public void enableCompactMode(boolean set) {

	setProperty("compactMode", set, true);
    }

    /**
     * @return
     */
    public boolean isCompactModeEnabled() {

	return isPropertySet("compactMode", true);
    }

    /**
     * Default: false
     * -
     * This property tells the client to insert the whole setting component inside a component
     * such as an accordion or a details pane
     */
    public void enableFoldedMode(boolean set) {

	setProperty("foldedMode", set, false);
    }

    /**
     * @return
     */
    public boolean isFoldedModeEnabled() {

	return isPropertySet("foldedMode", false);
    }

    /**
     * Default: false
     * 
     * @param canBeRemoved
     */
    public void setCanBeRemoved(boolean canBeRemoved) {

	setProperty("canBeRemoved", canBeRemoved, false);
    }

    /**
     * @return
     */
    public boolean canBeRemoved() {

	return isPropertySet("canBeRemoved", false);
    }

    /**
     * Default: true
     */
    public void setCanBeCleaned(boolean canBeCleaned) {

	setProperty("canBeCleaned", canBeCleaned, true);
    }

    /**
     * @return
     */
    public boolean canBeCleaned() {

	return isPropertySet("canBeCleaned", true);
    }

    /**
     * Default: true
     * -
     * -
     * This property tells the client to show/hide the header
     */
    public void setShowHeader(boolean showHeader) {

	setProperty("showHeader", showHeader, true);
    }

    /**
     * @return
     */
    public boolean isShowHeaderSet() {

	return isPropertySet("showHeader", true);
    }

    /**
     * @param extension
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends ObjectExtension> Optional<T> getExtension(Class<T> extension) {

	try {

	    Class<?> clazz = Class.forName(getObject().getString("extensionClass"));

	    if (extension.isAssignableFrom(clazz)) {

		return Optional.of((T) Class.forName(getObject().getString("extensionClass")).newInstance());
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

	getObject().put("extensionClass", extension.getClass().getName());
    }

    /**
     * @return
     */
    public Optional<Class<? extends ObjectExtension>> getOptionalExtensionClass() {

	if (getObject().has("extensionClass")) {

	    String extensionClass = getObject().getString("extensionClass");

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

	    if (getObject().has("validatorClass")) {

		return Optional.of((T) Class.forName(getObject().getString("validatorClass")).newInstance());
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

	getObject().put("validatorClass", validator.getClass().getName());
    }

    /**
     * @return
     */
    public Optional<Class<? extends Validator>> getOptionalValidatorClass() {

	if (getObject().has("validatorClass")) {

	    String afterCleanFunctionClass = getObject().getString("validatorClass");

	    try {

		@SuppressWarnings("unchecked")
		Class<? extends Validator> clazz = (Class<? extends Validator>) Class.forName(afterCleanFunctionClass);

		return Optional.of(clazz);

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).warn(e.getMessage(), e);
	    }
	}

	return Optional.empty();
    }

    /**
     * @param configuration
     * @param context
     * @return
     */
    public Optional<ValidationResponse> validate(Configuration configuration, ValidationContext context) {

	Optional<Validator> validator = getValidator();

	if (validator.isPresent()) {

	    return Optional.of(validator.get().validate(configuration, (Setting) this, context));
	}

	return Optional.empty();
    }

    @Override
    protected String initObjectType() {

	return "setting";
    }
}
