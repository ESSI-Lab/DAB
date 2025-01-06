package eu.essi_lab.cfga.setting;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.lib.utils.StreamUtils;

/**
 * @author Fabrizio
 */
public abstract class ConfigurationObject {

    /**
     * 
     */
    public static Property<Boolean> ENABLED = Property.of("Enabled", "enabled", true, Optional.of(true)); //
    /**
     * 
     */
    public static Property<Boolean> CAN_BE_DISABLED = Property.of("CanBeDisabled", "canBeDisabled", true, Optional.of(true));//
    /**
     * 
     */
    public static Property<Boolean> VISIBLE = Property.of("Visible", "visible", true, Optional.of(true)); //
    /**
     * 
     */
    public static Property<Boolean> EDITABLE = Property.of("Editable", "editable", true, Optional.of(true)); //
    /**
     * 
     */
    public static Property<String> DESCRIPTION = Property.of("Description", "description", false, Optional.empty());
    /**
     * 
     */
    public static Property<String> OBJECT_TYPE = Property.of("ObjectType", "type", true, Optional.empty());

    private JSONObject object;

    /**
     * 
     */
    public ConfigurationObject() {

	this.object = new JSONObject();

	this.object.put("type", initObjectType());
    }

    /**
     * @param object
     */
    public ConfigurationObject(JSONObject object) {

	this.object = object;
    }

    /**
     * @param object
     */
    public ConfigurationObject(String object) {

	this.object = new JSONObject(object);
    }

    /**
     * Default: true
     * 
     * @param enabled
     */
    public final void setEnabled(boolean enabled) {

	if (!canBeDisabled() && !enabled) {
	    return;
	}

	setProperty("enabled", enabled, true);

	if (!enabled) {
	    propagateEnabled(enabled);
	}
    }

    /**
     * @return
     */
    public final boolean isEnabled() {

	return isPropertySet("enabled", true);
    }

    /**
     * Default: true
     * 
     * @param canBeDisabled
     */
    public void setCanBeDisabled(boolean canBeDisabled) {

	setProperty("canBeDisabled", canBeDisabled, true);
    }

    /**
     * @return
     */
    public boolean canBeDisabled() {

	return isPropertySet("canBeDisabled", true);
    }

    /**
     * Default: true
     * 
     * @param label
     */
    public final void setVisible(boolean visible) {

	setProperty("visible", visible, true);

	if (!visible) {
	    propagateVisible(visible);
	}
    }

    /**
     * @return
     */
    public final boolean isVisible() {

	return isPropertySet("visible", true);
    }

    /**
     * Default: true
     * The behaviour is different according to the object type:<br>
     * <ul>
     * <li>{@link Setting}: shows or hide the edit button</li>
     * <li>{@link Option}: renders the option field editable or readonly</li>
     * </ul>
     * 
     * @return
     */
    public boolean isEditable() {

	return isPropertySet("editable", true);
    }

    /**
     * Default: true
     * The behaviour is different according to the object type:<br>
     * <ul>
     * <li>{@link Setting}: shows or hide the edit button</li>
     * <li>{@link Option}: renders the option field editable or readonly</li>
     * </ul>
     * 
     * @param editable
     */
    public void setEditable(boolean editable) {

	setProperty("editable", editable, true);
    }

    /**
     * @return
     */
    public Optional<String> getDescription() {

	return getObject().has("description") ? Optional.of(getObject().getString("description")) : Optional.empty();
    }

    /**
     * @param description
     */
    public void setDescription(String description) {

	getObject().put("description", description);
    }

    /**
    * 
    */
    public void clearDescription() {

	if (getObject().has("description")) {

	    getObject().remove("description");
	}
    }

    /**
     * @return
     */
    public JSONObject getObject() {

	return object;
    }

    @Override
    public String toString() {

	return getObject().toString(3);
    }

    @Override
    public boolean equals(Object object) {

	if (!(object instanceof ConfigurationObject)) {

	    return false;
	}

	ConfigurationObject o = (ConfigurationObject) object;
	return o.getObject().similar(getObject());
    }

    /**
     * @return
     */
    public String getObjectType() {

	return getObject().getString("type");
    }

    /**
     * @return
     */
    public Optional<Property<?>> getProperty(String name) {

	return getProperties().//
		stream().//
		filter(p -> p.getName().equals(name)).//
		findFirst();
    }

    /**
     * @return
     */
    public List<Property<?>> getProperties() {

	ArrayList<Property<?>> properties = new ArrayList<Property<?>>();

	properties.add(ENABLED);
	properties.add(CAN_BE_DISABLED);
	properties.add(VISIBLE);
	properties.add(EDITABLE);
	properties.add(DESCRIPTION);
	properties.add(OBJECT_TYPE);

	return properties;
    }

    /**
     * @return
     */
    protected List<String> keys() {

	return StreamUtils.//
		iteratorToStream(getObject().keys()).//
		collect(Collectors.toList());
    }

    /**
     * @param property
     * @param defaultValue
     * @return
     */
    protected boolean isPropertySet(String property, boolean defaultValue) {

	return getObject().has(property) ? getObject().getBoolean(property) : defaultValue;
    }

    /**
     * @param property
     * @param value
     * @param defaultValue
     */
    protected void setProperty(String property, boolean value, boolean defaultValue) {

	if (!value && !defaultValue) {

	    getObject().remove(property);
	    return;
	}

	if (value && defaultValue) {

	    getObject().remove(property);
	    return;
	}

	getObject().put(property, value);
    }

    /**
     * @param value
     */
    protected void propagateEnabled(boolean value) {
    }

    /**
     * @param value
     */
    protected void propagateVisible(boolean value) {
    }

    /**
     * @param object
     */
    protected void setObject(JSONObject object) {

	this.object = object;
    }

    /**
     * @return
     */
    protected abstract String initObjectType();
}
