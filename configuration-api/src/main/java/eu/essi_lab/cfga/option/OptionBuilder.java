/**
 * 
 */
package eu.essi_lab.cfga.option;

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

import org.json.JSONObject;

import eu.essi_lab.cfga.Selectable.SelectionMode;

/**
 * @author Fabrizio
 */
public class OptionBuilder<T> {

    private Option<T> option;
    private Class<T> clazz;

    /**
     * @param clazz
     */
    public OptionBuilder(Class<T> clazz) {

	this.clazz = clazz;
	this.option = new Option<>(clazz);
    }

    /**
     * @param object
     */
    public OptionBuilder(JSONObject object) {

	this.option = new Option<>(object);
    }

    /**
     * @param clazz
     * @return
     */
    public static <T> OptionBuilder<T> get(Class<T> clazz) {

	return new OptionBuilder<T>(clazz);
    }

    /**
     * @param clazz
     * @param object
     * @return
     */
    public static <T> OptionBuilder<T> get(Class<T> clazz, JSONObject object) {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	OptionBuilder<T> builder = new OptionBuilder(clazz);

	builder.option = new Option<>(object);

	return builder;
    }

    /**
     * @return
     */
    public OptionBuilder<T> base64Encoded() {

	option.setBase64EncodedValue();
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> notEncoded() {

	// nothing to do
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> required() {

	option.setRequired(true);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> optional() {

	option.setRequired(false);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> visible() {

	option.setVisible(true);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> hidden() {

	option.setVisible(false);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> editable() {

	option.setEditable(true);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> readOnly() {

	option.setEditable(false);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> advanced() {

	option.setAdvanced(true);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> notAdvanced() {

	option.setAdvanced(false);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> canBeDisabled() {

	option.setCanBeDisabled(true);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> cannotBeDisabled() {

	option.setCanBeDisabled(false);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> enabled() {

	option.setEnabled(true);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> disabled() {

	option.setEnabled(false);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> withTextArea() {

	option.enableTextArea(true);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> withMapper(OptionValueMapper<T> mapper) {

	option.setMapper(mapper);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> withValuesLoader(ValuesLoader<T> loader) {

	option.setLoader(loader);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> withInputPattern(InputPattern pattern) {

	option.setInputPattern(pattern);
	return this;
    }

    /**
     * @param minValue
     * @return
     */
    public OptionBuilder<T> withMinValue(Number minValue) {

	option.setMinValue(minValue);
	return this;
    }

    /**
     * @param maxValue
     * @return
     */
    public OptionBuilder<T> withMaxValue(Number maxValue) {

	option.setMaxValue(maxValue);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> withSingleSelection() {

	option.setSelectionMode(SelectionMode.SINGLE);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> withMultiSelection() {

	option.setSelectionMode(SelectionMode.MULTI);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> withUnsetSelection() {

	option.setSelectionMode(SelectionMode.UNSET);
	return this;
    }

    /**
     * @param key
     * @return
     */
    public OptionBuilder<T> withKey(String key) {

	option.setKey(key);
	return this;
    }

    /**
     * @param label
     * @return
     */
    public OptionBuilder<T> withLabel(String label) {

	option.setLabel(label);
	return this;
    }

    /**
     * @param description
     * @return
     */
    public OptionBuilder<T> withDescription(String description) {

	option.setDescription(description);
	return this;
    }

    /**
     * @param value
     * @return
     */
    public OptionBuilder<T> withValue(T value) {

	option.addValue(value);
	return this;
    }

    /**
     * @param value
     * @return
     */
    public OptionBuilder<T> withSelectedValue(T value) {

	option.select(v -> v.equals(value));
	return this;
    }

    /**
     * @param values
     * @return
     */
    public OptionBuilder<T> withSelectedValues(List<T> values) {

	option.select(v -> values.contains(v));
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> withNoValues() {

	option.clearValues();
	return this;
    }

    /**
     * @param values
     * @return
     */
    public OptionBuilder<T> withValues(List<T> values) {

	option.setValues(values);
	return this;
    }

    /**
     * @return
     */
    public OptionBuilder<T> clear() {

	option = new Option<>(clazz);
	return this;
    }

    /**
     * @return
     */
    public Option<T> build() {

	return option;
    }

}
