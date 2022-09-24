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
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONException;
import org.json.JSONObject;

import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.cfga.Selectable;
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

	    getObject().remove("selectionMode");
	    return;
	}

	getObject().put("selectionMode", mode.getLabel());
    }

    /**
     * @return
     */
    @Override
    public SelectionMode getSelectionMode() {

	if (getObject().has("selectionMode")) {

	    return LabeledEnum.valueOf(SelectionMode.class, getObject().getString("selectionMode")).get();
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

	stream.forEach(s -> this.removeSetting(s));
    }

    /**
     * 
     */
    public void afterClean() {

	Optional<AfterCleanFunction> afterCleanFunction = getAfterCleanFunction();

	if (afterCleanFunction.isPresent()) {

	    afterCleanFunction.get().afterClean(this);
	}
    }

    /**
     * @param extension
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T extends AfterCleanFunction> Optional<T> getAfterCleanFunction() {

	if (getObject().has("afterCleanFunction")) {

	    try {

		Class<?> clazz = Class.forName(getObject().getString("afterCleanFunction"));

		return Optional.of((T) clazz.newInstance());

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

	getObject().put("afterCleanFunction", function.getClass().getName());
    }

    /**
     * @return
     */
    public Optional<Class<? extends AfterCleanFunction>> getOptionalAfterCleanFunctionClass() {

	if (getObject().has("afterCleanFunction")) {

	    String afterCleanFunctionClass = getObject().getString("afterCleanFunction");

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

	setProperty("selected", selected, false);
    }

    /**
     * @return
     */
    public boolean isSelected() {

	return isPropertySet("selected", false);
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
		filter(o -> o.getString("type").equals("setting") || o.getString("type").equals(Scheduling.SCHEDULING_OBJECT_TYPE)).//
		map(o -> new Setting(o)).//
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

	return getObject().getString("settingId");
    }

    /**
     * @param id the id to set
     */
    public final void setIdentifier(String id) {

	getObject().put("settingId", id);
    }

    /**
     * @return the name
     */
    public final String getName() {

	return getObject().getString("settingName");
    }

    /**
     * @param name the name to set
     */
    public final void setName(String name) {

	getObject().put("settingName", name);
    }

    /**
     * Creates a configured instance of the {@link Configurable}
     * according to the {@link #getConfigurableType()} method. In order to be instantiated, the concrete
     * {@link Configurable} implementation must provide an empty constructor
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

	if (!optional.isPresent()) {

	    throw new Exception("Configurable of type " + getConfigurableType() + " not found");
	}

	Configurable configurable = optional.get();

	//
	// this setting can be a Setting subclass instance and
	// since the Configurable interface is parameterized,
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
     * Set the type of the {@link Configurable} configured by this setting. <br>
     * Once set, the method {@link #createConfigurable()} can be invoked in order to create a configured instance of the
     * given {@link Configurable}
     * 
     * @see Configurable#getType()
     * @param configurableType
     */
    public void setConfigurableType(String configurableType) {

	getObject().put("configurableType", configurableType);
    }

    /**
     * @return the configurableType
     */
    public String getConfigurableType() {

	if (!getObject().has("configurableType")) {

	    throw new RuntimeException("Configurable type not set");
	}

	return getObject().getString("configurableType");
    }

    /**
     * @return
     * @throws ClassNotFoundException
     * @throws JSONException
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Setting> getSettingClass() {

	try {
	    return (Class<? extends Setting>) Class.forName(getObject().getString("settingClass"));
	} catch (ClassNotFoundException | JSONException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw new RuntimeException(e.toString());
	}
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

	if(!setting.canBeDisabled() && !value){
	    return;
	}
	
	setting.getObject().put("enabled", value);
	
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

	setting.getObject().put("visible", value);
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
