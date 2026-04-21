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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONException;
import org.json.JSONObject;

import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.cfga.Selectable;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.lib.utils.StreamUtils;

/**
 * @author Fabrizio
 */
public class Setting extends AbstractSetting implements Selectable<Setting> {

    /**
     *
     */
    public static final Property<String> IDENTIFIER = Property.of("Identifier", "settingId", true, Optional.empty()); //

    /**
     *
     */
    public static final Property<String> NAME = Property.of("Name", "settingName", true, Optional.empty());//

    /**
     *
     */
    public static final Property<SelectionMode> SELECTION_MODE = Property.of("SelectionMode", "selectionMode", true,
	    Optional.of(SelectionMode.UNSET));//

    /**
     *
     */
    public static final Property<AfterCleanFunction> AFTER_CLEAN_FUNCTION = Property.of("AfterCleanFunction", "afterCleanFunction", false,
	    Optional.empty());//

    /**
     *
     */
    public static final Property<Boolean> SELECTED = Property.of("Selected", "selected", true, Optional.of(false)); //

    /**
     *
     */
    public static final Property<String> CONFIGURABLE_TYPE = Property.of("ConfigurableType", "configurableType", false, Optional.empty());//

    /**
     *
     */
    public static final Property<Class<? extends Setting>> SETTING_CLASS = Property.of("SettingClass", "settingClass", true,
	    Optional.empty());//

    /**
     *
     */
    private boolean forceHideHeader;

    /**
     *
     */
    public Setting() {

	setIdentifier(UUID.randomUUID().toString());

	setName(getIdentifier());

	getObject().put("settingClass", getClass().getName());

	enableCompactMode(true);
    }

    /**
     * @param object
     */
    public Setting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public Setting(String object) {

	super(object);
    }

    /**
     * @param mode
     */
    @Override
    public void setSelectionMode(SelectionMode mode) {

	if (mode == SelectionMode.UNSET) {

	    getObject().remove(SELECTION_MODE.getKey());
	    return;
	}

	getObject().put(SELECTION_MODE.getKey(), mode.getLabel());
    }

    /**
     * @return
     */
    @Override
    public SelectionMode getSelectionMode() {

	if (getObject().has(SELECTION_MODE.getKey())) {

	    return LabeledEnum.valueOf(SelectionMode.class, getObject().getString(SELECTION_MODE.getKey())).get();
	}

	return SelectionMode.UNSET;
    }

    /**
     * {@link Scheduling} cannot be selected
     *
     * @param predicate
     */
    @Override
    public void select(Predicate<Setting> predicate) {

	getSettings().//
		stream().//
		filter(s -> !s.getObjectType().equals(Scheduling.SCHEDULING_OBJECT_TYPE)).//
		forEach(s -> s.setSelected(predicate.test(s)));
    }

    /**
     * {@link Scheduling} cannot be removed
     */
    @Override
    public void clean() {

	if (getSelectionMode() == SelectionMode.UNSET || !canBeCleaned()) {

	    return;
	}

	setSelectionMode(SelectionMode.UNSET);

	Stream<Setting> stream = getSettings().//
		stream().//
		filter(s -> !s.isSelected() && !s.getObjectType().equals(Scheduling.SCHEDULING_OBJECT_TYPE));

	stream.forEach(this::removeSetting);
    }

    /**
     *
     */
    public void afterClean() {

	getAfterCleanFunction().ifPresent(f -> f.afterClean(this));
    }

    /**
     * @param extension
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T extends AfterCleanFunction> Optional<T> getAfterCleanFunction() {

	if (getObject().has(AFTER_CLEAN_FUNCTION.getKey())) {

	    try {

		Class<?> clazz = Class.forName(getObject().getString(AFTER_CLEAN_FUNCTION.getKey()));

		return Optional.of((T) clazz.getDeclaredConstructor().newInstance());

	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).warn(e.getMessage(), e);
	    }
	}

	return Optional.empty();
    }

    /**
     * @param function
     */
    public void setAfterCleanFunction(AfterCleanFunction function) {

	getObject().put(AFTER_CLEAN_FUNCTION.getKey(), function.getClass().getName());
    }

    /**
     * @return
     */
    public Optional<Class<? extends AfterCleanFunction>> getOptionalAfterCleanFunctionClass() {

	if (getObject().has(AFTER_CLEAN_FUNCTION.getKey())) {

	    String afterCleanFunctionClass = getObject().getString(AFTER_CLEAN_FUNCTION.getKey());

	    try {

		@SuppressWarnings("unchecked")
		Class<? extends AfterCleanFunction> clazz = (Class<? extends AfterCleanFunction>) Class.forName(afterCleanFunctionClass);

		return Optional.of(clazz);

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).warn(e.getMessage(), e);
	    }
	}

	return Optional.empty();
    }

    /**
     * @param selected
     */
    public void setSelected(boolean selected) {

	setProperty(SELECTED.getKey(), selected, SELECTED.getDefaultValue().get());
    }

    /**
     * Default: false
     *
     * @return
     */
    public boolean isSelected() {

	return isPropertySet(SELECTED.getKey(), SELECTED.getDefaultValue().get());
    }

    /**
     * Reset this setting to its original state according to its no-args constructor
     */
    public void reset() {

	Setting newSetting = SettingUtils.create(getSettingClass());
	this.setObject(newSetting.getObject());
    }

    /**
     * @param settingIdentifier
     */
    public boolean removeSetting(String settingIdentifier) {

	Optional<Setting> optional = getSettings().//
		stream().//
		filter(s -> s.getIdentifier().equals(settingIdentifier)).//
		findFirst();

	if (optional.isPresent()) {

	    getObject().remove(settingIdentifier);
	    return true;
	}

	return false;
    }

    /**
     * @param setting
     */
    public boolean removeSetting(Setting setting) {

	return removeSetting(setting.getIdentifier());
    }

    /**
     * @param setting
     */
    public void addSetting(Setting setting) {

	if (setting.equals(this)) {
	    throw new IllegalArgumentException("Attempting to add this setting");
	}

	if (!this.isEnabled()) {
	    setting.setEnabled(false);
	}

	if (!this.isVisible()) {
	    setting.setVisible(false);
	}

	getObject().put(setting.getIdentifier(), setting.getObject());
    }

    /**
     * @return
     */
    public List<Setting> getSettings() {

	return keys().//
		stream().//
		map(k -> getObject().get(k)).//
		filter(o -> o instanceof JSONObject).//
		map(o -> (JSONObject) o). //
		filter(o -> o.getString(OBJECT_TYPE.getKey()).equals("setting") || o.getString(OBJECT_TYPE.getKey())
		.equals(Scheduling.SCHEDULING_OBJECT_TYPE)).//
		map(Setting::new).//
		collect(Collectors.toList());
    }

    /**
     * @param settingId
     */
    public synchronized Optional<Setting> getSetting(String settingId) {

	return SettingUtils.get(getSettings(), settingId);
    }

    /**
     * @param settingClass
     * @param exactClassMatch
     * @return
     */
    public <T extends Setting> List<T> getSettings(Class<T> settingClass, boolean exactClassMatch) {

	return SettingUtils.list(getSettings(), settingClass, exactClassMatch);
    }

    /**
     * @param settingClass
     * @return
     */
    public <T extends Setting> List<T> getSettings(Class<T> settingClass) {

	return getSettings(settingClass, true);
    }

    /**
     * @param settingId
     * @param settingClass
     * @param exactClassMatch
     * @return
     */
    public synchronized <T extends Setting> Optional<T> getSetting(String settingId, Class<T> settingClass, boolean exactClassMatch) {

	return getSettings(settingClass, exactClassMatch).//
		stream().//
		filter(s -> s.getIdentifier().equals(settingId)).//
		findFirst();

    }

    /**
     * @param settingId
     * @param settingClass
     * @return
     */
    public synchronized <T extends Setting> Optional<T> getSetting(String settingId, Class<T> settingClass) {

	return getSetting(settingId, settingClass, true);
    }

    /**
     * @return the id
     */
    public final String getIdentifier() {

	return getObject().getString(IDENTIFIER.getKey());
    }

    /**
     * @param id the id to set
     */
    public final void setIdentifier(String id) {

	getObject().put(IDENTIFIER.getKey(), id);
    }

    /**
     * @return the name
     */
    public final String getName() {

	return getObject().getString(NAME.getKey());
    }

    /**
     * @param name the name to set
     */
    public final void setName(String name) {

	getObject().put(NAME.getKey(), name);
    }

    /**
     * Creates a configured instance of the {@link Configurable} according to the {@link #getConfigurableType()} method. In order to be
     * instantiated, the concrete {@link Configurable} implementation must provide an empty constructor
     *
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends Configurable> T createConfigurable() throws Exception {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Optional<Configurable> optional = StreamUtils.//
		iteratorToStream(loader.iterator()).//
		filter(c -> c.getType() != null).// this should never happen! there is a specific test
		filter(c -> c.getType().equals(getConfigurableType())).//
		findFirst();

	if (optional.isEmpty()) {

	    throw new Exception("Configurable of type " + getConfigurableType() + " not found");
	}

	Configurable configurable = optional.get();

	//
	// this setting can be a Setting subclass instance and
	// since the Configurable interface is parameterised,
	// the Configurable.configure method must take as argument
	// an instance of the correct generic type
	//
	Setting settingInstance = SettingUtils.downCast(this);

	configurable.configure(settingInstance);

	return (T) configurable;
    }

    @SuppressWarnings({ "rawtypes" })
    public <T extends Configurable> T createConfigurableOrNull() {

	try {
	    return createConfigurable();
	} catch (Exception ex) {
	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	}

	return null;
    }

    /**
     * Set the type of the {@link Configurable} configured by this setting. <br> Once set, the method {@link #createConfigurable()} can be
     * invoked in order to create a configured instance of the given {@link Configurable}
     *
     * @param configurableType
     * @see Configurable#getType()
     */
    public void setConfigurableType(String configurableType) {

	getObject().put(CONFIGURABLE_TYPE.getKey(), configurableType);
    }

    /**
     * @return the configurableType
     */
    public String getConfigurableType() {

	if (!getObject().has(CONFIGURABLE_TYPE.getKey())) {

	    throw new RuntimeException("Configurable type not set");
	}

	return getObject().getString(CONFIGURABLE_TYPE.getKey());
    }

    /**
     * @return
     * @throws ClassNotFoundException
     * @throws JSONException
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Setting> getSettingClass() {

	try {
	    return (Class<? extends Setting>) Class.forName(getObject().getString(SETTING_CLASS.getKey()));
	} catch (ClassNotFoundException | JSONException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw new RuntimeException(e.toString());
	}
    }

    /**
     * Verifies if this setting is similar to <code>other</code>.<br> Two settings are similar if:
     * <ol>
     * <li>they have the same {@link #getSettingClass()}</li>
     * <li>the related {@link #getObject()}s have exactly the same keys, except the keys included in
     * <code>exclusions</code> and for each key, they must have the same type of value (e.g: Integer, Double, String,
     * JSONObject, ...) )</li>
     * <li>all the options of this setting are similar to the options of <code>other</code> setting, according to the
     * {@link Option#similar(Option)} method</li>
     * <li>the above statements are recursively valid for the sub-settings of this and <code>other</code> setting</li>
     * </ol>
     *
     * @param other
     * @param exclusions the {@link #getObject()} keys to exclude from the check.<br> The following properties cannot be excluded, and if
     * the related identifiers are provided, they are ignored:
     * <ul>
     * <li>{@link #IDENTIFIER}</li>
     * <li>{@link Setting#SETTING_CLASS}</li>
     * <li>{@link Setting#OBJECT_TYPE}</li>
     * </ul>
     * @return
     */
    public boolean similar(Setting other, List<String> exclusions) {

	// they must have the same setting class
	if (!this.getSettingClass().equals(other.getSettingClass())) {

	    return false;
	}

	List<String> thisKeys = keys().//
		stream().//
		// excludes the keys which are identifiers of settings and options
		// they are check later, and excludes the selection key
			filter(k -> getSetting(k).isEmpty() && getOption(k).isEmpty()).//
		// excludes the exclusions keys
			filter(k -> !exclusions.contains(k)).//
			sorted().//
			toList();

	List<String> otherKeys = other.keys().//
		stream().//
		// excludes the keys which are identifiers of settings and options
		// they are check later, and excludes the selection key
			filter(k -> other.getSetting(k).isEmpty() && other.getOption(k).isEmpty()).//
		// excludes the exclusions properties
			filter(k -> !exclusions.contains(k)).//
			sorted().//
			toList();

	// they must have the same keys
	if (thisKeys.equals(otherKeys)) {

	    // each key value class must be exactly the same
	    for (String key : thisKeys) {

		Class<?> thisValueClass = this.getObject().get(key).getClass();
		Class<?> otherValueClass = other.getObject().get(key).getClass();

		if (!thisValueClass.equals(otherValueClass)) {

		    return false;
		}
	    }

	    List<String> thisOptionsKeys = this.getOptions().//
		    stream().//
		    map(Option::getKey).//
		    sorted().//
		    toList();

	    List<String> otherOptionsKeys = other.getOptions().//
		    stream().//
		    map(Option::getKey).//
		    sorted().//
		    toList();

	    // they must have the same options keys
	    if (thisOptionsKeys.equals(otherOptionsKeys)) {

		// all the options must be similar
		for (String key : thisOptionsKeys) {

		    if (!this.getOption(key).get().similar(other.getOption(key).get())) {

			return false;
		    }
		}
	    } else {
		return false;
	    }
	} else {
	    return false;
	}

	List<String> thisSettingsKeys = getSettings().//
		stream().//
		map(Setting::getIdentifier).//
		sorted().//
		toList();

	List<String> otherSettingsKeys = other.getSettings().//
		stream().//
		map(Setting::getIdentifier).//
		sorted().//
		toList();

	// they must have the same settings keys
	if (thisSettingsKeys.equals(otherSettingsKeys)) {

	    // recursive call
	    for (String settingKey : thisSettingsKeys) {

		if (!this.getSetting(settingKey).get().similar(other.getSetting(settingKey).get(), exclusions)) {

		    return false;
		}
	    }
	} else {
	    return false;
	}

	return true;
    }

    /**
     * Verifies if this setting is similar to <code>other</code>.<br> Two settings are similar if:
     * <ol>
     * <li>they have the same {@link #getSettingClass()}</li>
     * <li>the related {@link #getObject()}s have exactly the same keys</li>
     * <li>all the options of this setting are similar to the options of <code>other</code> setting, according to the
     * {@link Option#similar(Option)} method</li>
     * <li>the above statements are recursively valid for the sub-settings of this and <code>other</code> setting</li>
     * </ol>
     *
     * @param setting
     * @return
     */
    public boolean similar(Setting other) {

	return similar(other, List.of());
    }

    /**
     * @return
     */
    public static List<Property<?>> getDeclaredProperties() {

	return new Setting().getProperties();
    }

    /**
     *
     */
    protected final List<Property<?>> getProperties() {

	List<Property<?>> properties = super.getProperties();

	properties.addAll(//

		Arrays.asList(//
			IDENTIFIER, //
			NAME, //
			SELECTION_MODE, //
			AFTER_CLEAN_FUNCTION, //
			SELECTED, //
			CONFIGURABLE_TYPE, //
			SETTING_CLASS//
		));

	return properties;
    }

    @Override
    protected void propagateEnabled(boolean value) {

	propagateEnabled(this, value);
    }

    /**
     * @param setting
     * @param value
     */
    private void propagateEnabled(Setting setting, boolean value) {

	if (!setting.canBeDisabled() && !value) {
	    return;
	}

	setting.getObject().put(ENABLED.getKey(), value);

	setting.getOptions().forEach(o -> {

	    if (!o.canBeDisabled() && !value) {
		return;
	    }

	    o.setEnabled(value);
	});

	setting.getSettings().forEach(s -> propagateEnabled(s, value));
    }

    @Override
    protected void propagateVisible(boolean value) {

	propagateVisible(this, value);
    }

    /**
     * @param setting
     * @param value
     */
    private void propagateVisible(Setting setting, boolean value) {

	setting.getObject().put(VISIBLE.getKey(), value);
	setting.getOptions().forEach(o -> o.setVisible(value));

	setting.getSettings().forEach(s -> propagateVisible(s, value));
    }

    /**
     *
     */
    @Override
    public Setting clone() {

	return new Setting(getObject().toString());
    }
}
