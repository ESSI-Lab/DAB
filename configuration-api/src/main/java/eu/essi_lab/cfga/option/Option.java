package eu.essi_lab.cfga.option;

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

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cfga.Selectable;
import eu.essi_lab.cfga.setting.ConfigurationObject;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.Base64Utils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * 
 */
public class Option<T> extends ConfigurationObject implements Selectable<T> {

    /**
     * 
     */
    public Option(Class<T> clazz) {

	super();

	getObject().put("type", "option");
	getObject().put("valueClass", clazz.getName());

	setPosition(0);
    }

    /**
     * @param object
     */
    public Option(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public Option(String object) {

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

    @Override
    public void select(Predicate<T> predicate) throws UnsetSelectionModeException{

	if (getSelectionMode() == SelectionMode.UNSET) {

	    throw new UnsetSelectionModeException();
	}

	List<Integer> selIndexes = getValues().//
		stream().//
		filter(predicate).//
		map(v -> getValueIndex(v)).//
		collect(Collectors.toList());

	putSelectedIndexes(selIndexes);
    }

    @Override
    public void clean() {

	if (getSelectionMode() == SelectionMode.UNSET) {

	    return;
	}

	setSelectionMode(SelectionMode.UNSET);

	List<T> selectedValues = getSelectedValues();

	setValues(selectedValues);

	List<Integer> selIndexes = getValues().//
		stream().//
		map(v -> getValueIndex(v)).//
		collect(Collectors.toList());

	putSelectedIndexes(selIndexes);
    }

    /**
     * @return
     */
    public List<T> getSelectedValues() {

	return getValues().//
		stream().//
		filter(v -> getSelectedIndexes().contains(getValueIndex(v))).//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    public Optional<T> getOptionalSelectedValue() {

	List<T> values = getSelectedValues();

	return values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }

    /**
     * @return
     */
    public T getSelectedValue() {

	return getOptionalSelectedValue().orElse(null);
    }

    /**
     * @return
     */
    public boolean isBase64EncodedValue() {

	return getObject().has("base64") ? getObject().getBoolean("base64") : false;
    }

    /**
     * 
     */
    public void setBase64EncodedValue() {

	getObject().put("base64", true);
    }

    /**
     * Default: false
     * 
     * @return
     */
    public boolean isRequired() {

	return isPropertySet("required", false);
    }

    /**
     * Default: false
     * 
     * @param required
     */
    public void setRequired(boolean required) {

	setProperty("required", required, false);
    }

    /**
     * Default: false
     * 
     * @return
     */
    public boolean isAdvanced() {

	return isPropertySet("advanced", false);
    }

    /**
     * Default: false
     * 
     * @param advanced
     */
    public void setAdvanced(boolean advanced) {

	setProperty("advanced", advanced, false);
    }

    /**
     * @param pattern
     */
    public void setInputPattern(InputPattern pattern) {

	getObject().put("inputPattern", pattern.getClass().getName() + "_" + pattern.getName());
    }

    public static void main(String[] args) {

	String s = "eu.essi_lab.cfga.gs.GSSourcePattern_gsSourceId";

	int lastUs = s.lastIndexOf("_");

	String className = s.substring(0, lastUs);

	System.out.println(className);

	String patternName = s.substring(lastUs + 1, s.length());

	System.out.println(patternName);

    }

    /**
     * @return
     */
    public Optional<InputPattern> getInputPattern() {

	if (getObject().has("inputPattern")) {

	    String inputPattern = getObject().getString("inputPattern");

	    int lastUs = inputPattern.lastIndexOf("_");

	    String className = inputPattern.substring(0, lastUs);
	    String patternName = inputPattern.substring(lastUs + 1, inputPattern.length());

	    try {
		@SuppressWarnings("unchecked")
		Class<? extends InputPattern> clazz = (Class<? extends InputPattern>) Class.forName(className);

		Method method = clazz.getMethod("fromName", String.class);
		InputPattern out = (InputPattern) method.invoke(null, patternName);

		return Optional.of(out);

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	}

	return Optional.empty();
    }

    /**
     * @param minValue
     */
    public void setMinValue(Number minValue) {

	getObject().put("minValue", minValue);
    }

    /**
     * @return
     */
    public Optional<Number> getMinValue() {

	if (getObject().has("minValue")) {

	    if (getValueClass().equals(Integer.class)) {

		return Optional.of(getObject().getInt("minValue"));
	    }

	    return Optional.of(getObject().getDouble("minValue"));
	}

	return Optional.empty();
    }

    /**
     * @param maxValue
     */
    public void setMaxValue(Number maxValue) {

	getObject().put("maxValue", maxValue);
    }

    /**
     * @return
     */
    public Optional<Number> getMaxValue() {

	if (getObject().has("maxValue")) {

	    if (getValueClass().equals(Integer.class)) {

		return Optional.of(getObject().getInt("maxValue"));
	    }

	    return Optional.of(getObject().getDouble("maxValue"));
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    public String getLabel() {

	return getObject().getString("label");
    }

    /**
     * @param label
     */
    public void setLabel(String label) {

	getObject().put("label", label);
    }

    /**
     * @return
     */
    public String getKey() {

	return getObject().getString("key");
    }

    /**
     * @param key
     */
    public void setKey(String key) {

	getObject().put("key", key);
    }

    /**
     * Default: false
     * If enabled, the option values is shown in a text area. In read-only mode, each new line character
     * corresponds to a text area row.<br>
     * This property is valid only for string content
     * 
     * @param enable
     */
    public void enableTextArea(boolean enable) {

	setProperty("textArea", enable, false);
    }

    /**
     * @return
     */
    public boolean isTextAreaEnabled() {

	return isPropertySet("textArea", false);
    }

    /**
     * @return
     */
    public Optional<T> getOptionalValue() {

	List<T> values = getValues();

	return values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }

    /**
     * @return
     */
    public T getValue() {

	return getOptionalValue().orElse(null);
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<T> getValues() {

	Optional<Stream<Object>> optStream = getValuesStream();

	if (optStream.isPresent()) {

	    Stream<Object> values = optStream.get();

	    Optional<OptionValueMapper<T>> mapper = getOptionMapper();

	    //
	    // Using mapper
	    //
	    if (mapper.isPresent() && mapper.get().getValueClass().isAssignableFrom(getValueClass())) {

		return values.//
			map(v -> mapper.get().fromString((String) v)).//
			collect(Collectors.toList());
	    }

	    Class<?> valueClass = getValueClass();

	    //
	    // Base64 encoded values
	    //
	    if (isBase64EncodedValue()) {

		return values.//
			map(o -> ((T) Base64Utils.decodeToObject((String) o))).//
			collect(Collectors.toList());
	    }

	    //
	    // LabeledEnum values
	    //
	    if (LabeledEnum.class.isAssignableFrom(valueClass)) {

		List<T> enums = values.//
			map(v -> (String) v).//
			map(l -> LabeledEnum.valueOf((Class<? extends LabeledEnum>) valueClass, l).orElseGet(null)).//
			filter(Objects::nonNull).//
			map(e -> (T) e).//
			collect(Collectors.toList());

		return enums;
	    }

	    //
	    // Enum values
	    //
	    if (Enum.class.isAssignableFrom(valueClass)) {

		List<T> enums = values.//
			map(v -> (String) v).//
			map(n -> valueOfOrNull(valueClass, n)).//
			filter(Objects::nonNull).//
			map(e -> (T) e).//
			collect(Collectors.toList());

		return enums;
	    }

	    //
	    // ISODateTime values
	    //
	    if (ISODateTime.class.isAssignableFrom(valueClass)) {

		List<T> isoDateTime = values.//
			map(v -> (String) v).//
			map(v -> new ISODateTime(v)).//
			map(e -> (T) e).//
			collect(Collectors.toList());

		return isoDateTime;
	    }

	    //
	    // Setting values
	    //
	    if (Setting.class.isAssignableFrom(valueClass)) {

		List<T> setting = values.//
			map(v -> (String) v).//
			map(v -> create(valueClass)).//
			map(e -> (T) e).//
			collect(Collectors.toList());

		return setting;
	    }

	    //
	    // Type T values
	    //
	    return values.//
		    map(o -> (T) o).//
		    collect(Collectors.toList());
	}

	//
	// No values
	//
	return new ArrayList<>();
    }

    /**
     * @param value
     */
    public void addValue(T value) throws IllegalArgumentException {

	if (value == null) {
	    throw new IllegalArgumentException("Null value");
	}

	List<T> values = Arrays.asList(value);

	if (getObject().has("values")) {

	    values = getValues();
	    values.add(value);
	}

	setValues(values);
    }

    /**
     * @param values
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("rawtypes")
    public void setValues(List<T> values) throws IllegalArgumentException {

	JSONArray array = new JSONArray();

	Optional<OptionValueMapper<T>> mapper = getOptionMapper();

	//
	// Using mapper
	//
	if (mapper.isPresent() && mapper.get().getValueClass().isAssignableFrom(getValueClass())) {

	    List<String> mappedValues = values.//
		    stream().//
		    map(v -> (mapper.get().asString(v))).//
		    collect(Collectors.toList());

	    mappedValues.forEach(v -> array.put(v));
	}

	//
	// Base 64 encoded value
	//
	else if (isBase64EncodedValue()) {

	    try {

		List<String> encodedValues = values.//
			stream().//
			map(this::encodeOrThrow).//
			collect(Collectors.toList());

		encodedValues.forEach(v -> array.put(v));

	    } catch (ClassCastException ex) {

		throw new IllegalArgumentException("Option class <" + values.get(0).getClass().getName() + "> is not serializable");
	    } catch (RuntimeException e) {
		throw new IllegalArgumentException(e.getMessage(), e);
	    }

	}

	else {

	    values.forEach(v -> {

		//
		// labels of LabeledEnum
		//
		if (v instanceof LabeledEnum) {

		    array.put(((LabeledEnum) v).getLabel());

		    //
		    // constant names of Enum
		    //
		} else if (v instanceof Enum) {

		    array.put(((Enum) v).name());

		    //
		    // ISODateTime value
		    //

		} else if (v instanceof ISODateTime) {

		    array.put(((ISODateTime) v).getValue());

		    //
		    // Setting value
		    //
		} else if (v instanceof Setting) {

		    array.put(((Setting) v).getSettingClass().getName());

		    //
		    // T value
		    //

		} else {

		    array.put(v);
		}
	    });
	}

	// there are no selected values when the values are set
	getObject().remove("selectedIndexes");

	getObject().put("values", array);
    }

    /**
     * @param value
     */
    @SuppressWarnings("unchecked")
    public void setObjectValue(Object value) throws IllegalArgumentException {

	if (value == null) {
	    throw new IllegalArgumentException("Null value");
	}

	setValues(Arrays.asList((T) value));
    }

    /**
     * @param values
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    public void setObjectValues(List<?> values) throws IllegalArgumentException {

	List<T> mappedValues = values.stream().map(o -> (T) o).collect(Collectors.toList());

	setValues(mappedValues);
    }

    /**
     * @param value
     */
    public void setValue(T value) throws IllegalArgumentException {

	if (value == null) {
	    throw new IllegalArgumentException("Null value");
	}

	setValues(Arrays.asList(value));
    }

    /**
     * 
     */
    public void clearValues() {

	getObject().remove("values");
    }

    /**
     * @return
     */
    public Class<?> getValueClass() {

	try {
	    return Class.forName(getObject().getString("valueClass"));
	} catch (Exception e) {
	}

	return null;
    }

    /**
     * @param clazz
     * @return
     */
    public boolean isValueOf(Class<?> clazz) {

	return getValueClass() != null && getValueClass().equals(clazz);
    }

    /**
     * @param option
     * @return
     */
    public void setPosition(int position) {

	getObject().put("position", position);
    }

    /**
     * @param option
     * @return
     */
    public Integer getPosition() {

	return getObject().getInt("position");
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public Optional<OptionValueMapper<T>> getOptionMapper() {

	try {

	    if (getObject().has("optionMapperClass")) {

		return (Optional<OptionValueMapper<T>>) Optional
			.of((T) Class.forName(getObject().getString("optionMapperClass")).getDeclaredConstructor().newInstance());
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param optionMapper
     */
    public void setMapper(OptionValueMapper<? extends T> optionMapper) {

	getObject().put("optionMapperClass", optionMapper.getClass().getName());
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public Optional<ValuesLoader<T>> getLoader() {

	try {

	    if (getObject().has("valuesLoaderClass")) {

		return (Optional<ValuesLoader<T>>) Optional
			.of((T) Class.forName(getObject().getString("valuesLoaderClass")).getDeclaredConstructor().newInstance());
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param loader
     */
    public void setLoader(ValuesLoader<T> loader) {

	getObject().put("valuesLoaderClass", loader.getClass().getName());
    }

    /**
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Optional<Class<? extends ValuesLoader>> getOptionalLoaderClass() {

	if (getObject().has("valuesLoaderClass")) {

	    String extensionClass = getObject().getString("valuesLoaderClass");

	    try {

		Class<? extends ValuesLoader> clazz = (Class<? extends ValuesLoader>) Class.forName(extensionClass);

		return Optional.of(clazz);

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	}

	return Optional.empty();
    }

    /**
     * Verifies if this option is similar to <code>other</code>.<br>
     * Two options are similar if they have the same {@link #getValueClass()}
     * 
     * @param other
     * @return
     */
    public boolean similar(Option<?> other) {

	return this.getValueClass().equals(other.getValueClass());
    }

    /**
     * @param opt1
     * @param opt2
     * @return
     */
    public static int sort(Option<?> opt1, Option<?> opt2) {

	return opt1.getPosition().compareTo(opt2.getPosition());
    }

    @Override
    protected String initObjectType() {

	return "option";
    }

    @SuppressWarnings("unchecked")
    private T create(Class<?> valueClass) {

	try {
	    return (T) valueClass.getDeclaredConstructor().newInstance();
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}
	return null;
    }

    /**
     * @param indexes
     */
    private void putSelectedIndexes(List<Integer> indexes) {

	JSONArray array = new JSONArray();
	indexes.forEach(i -> array.put(i));

	getObject().put("selectedIndexes", array);
    }

    /**
     * @return
     */
    private Optional<Stream<Object>> getValuesStream() {

	if (getObject().has("values")) {

	    return Optional.of(getObject().//
		    getJSONArray("values").//
		    toList().//
		    stream());//
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    private List<Integer> getSelectedIndexes() {

	if (getObject().has("selectedIndexes")) {

	    return getObject().//
		    getJSONArray("selectedIndexes").//
		    toList().//
		    stream().//
		    map(v -> (Integer) v).//
		    collect(Collectors.toList());
	}

	return new ArrayList<>();
    }

    /**
     * @param value
     * @return
     */
    private int getValueIndex(T value) {

	int indexOf = getValues().//

		indexOf(value);

	return indexOf;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Enum valueOfOrNull(Class<?> valueClass, String constantName) {

	try {
	    return Enum.valueOf((Class<? extends Enum>) valueClass, constantName);
	} catch (Exception e) {
	}

	return null;
    }

    /**
     * @param v
     * @return
     */
    private String encodeOrThrow(Object v) {

	try {
	    return Base64Utils.encodeObject((Serializable) v);
	} catch (IOException e) {

	    throw new RuntimeException(e.getMessage(), e);
	}
    }
}
