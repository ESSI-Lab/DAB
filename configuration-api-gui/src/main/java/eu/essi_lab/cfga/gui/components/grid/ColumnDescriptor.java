package eu.essi_lab.cfga.gui.components.grid;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;

import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.ValueProvider;

import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class ColumnDescriptor {

    private String columnName;
    private boolean sortable;
    private boolean filtered;
    private int columnWidth;
    private boolean visible;
    private ValueProvider<Setting, String> valueProvider;
    private Comparator<HashMap<String, String>> comparator;
    private Renderer<HashMap<String, String>> renderer;
    private boolean hasCheckBox;

    static final String POSITIONAL_COLUMN_NAME = "#";

    /**
     * 
     */
    public ColumnDescriptor() {
    }

    /**
     * @return
     */
    public static ColumnDescriptor createPositionalDescriptor() {

	return create(POSITIONAL_COLUMN_NAME, 30, false, false, null); //
    }

    /**
     * @return
     */
    public static ColumnDescriptor createCheckBoxDescriptor(String columnName) {

	return createCheckBoxDescriptor(columnName, null);
    }

    /**
     * @return
     */
    public static ColumnDescriptor createCheckBoxDescriptor(Consumer<HashMap<String, String>> consumer) {

	return createCheckBoxDescriptor("", consumer);
    }

    /**
     * @return
     */
    public static ColumnDescriptor createCheckBoxDescriptor(String columnName, Consumer<HashMap<String, String>> consumer) {

	ColumnDescriptor descriptor = create(columnName, 45, false, false, null, null, new CheckBoxColumnRenderer(consumer)); //
	descriptor.setHasCheckbox(true);

	return descriptor;
    }

    /**
     * @param columnName
     * @param valueProvider
     * @return
     */
    public static ColumnDescriptor create(String columnName, ValueProvider<Setting, String> valueProvider) {

	return create(columnName, -1, false, false, true, valueProvider, null);
    }

    /**
     * @param columnName
     * @param valueProvider
     * @param renderer
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    ValueProvider<Setting, String> valueProvider, //
	    Renderer<HashMap<String, String>> renderer) {

	return create(columnName, -1, false, false, true, valueProvider, null, renderer);
    }

    /**
     * @param columnName
     * @param valueProvider
     * @param comparator
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    ValueProvider<Setting, String> valueProvider, //
	    Comparator<HashMap<String, String>> comparator) {

	return create(columnName, -1, false, false, true, valueProvider, comparator);
    }

    /**
     * @param columnName
     * @param valueProvider
     * @param comparator
     * @param renderer
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    ValueProvider<Setting, String> valueProvider, //
	    Comparator<HashMap<String, String>> comparator, //
	    Renderer<HashMap<String, String>> renderer) {

	return create(columnName, -1, false, false, true, valueProvider, comparator, renderer);
    }

    /**
     * @param columnName
     * @param sortable
     * @param valueProvider
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    boolean sortable, //
	    ValueProvider<Setting, String> valueProvider) {

	return create(columnName, -1, sortable, false, true, valueProvider, null);
    }

    /**
     * @param columnName
     * @param sortable
     * @param valueProvider
     * @param renderer
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    boolean sortable, //
	    ValueProvider<Setting, String> valueProvider, //
	    Renderer<HashMap<String, String>> renderer) {

	return create(columnName, -1, sortable, false, true, valueProvider, null, renderer);
    }

    /**
     * @param columnName
     * @param sortable
     * @param valueProvider
     * @param comparator
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    boolean sortable, //
	    ValueProvider<Setting, String> valueProvider, //
	    Comparator<HashMap<String, String>> comparator) {

	return create(columnName, -1, sortable, false, true, valueProvider, comparator);
    }

    /**
     * @param columnName
     * @param sortable
     * @param valueProvider
     * @param comparator
     * @param renderer
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    boolean sortable, //
	    ValueProvider<Setting, String> valueProvider, //
	    Comparator<HashMap<String, String>> comparator, //
	    Renderer<HashMap<String, String>> renderer) {

	return create(columnName, -1, sortable, false, true, valueProvider, comparator, renderer);
    }

    /**
     * @param columnName
     * @param sortable
     * @param filtered
     * @param valueProvider
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    boolean sortable, //
	    boolean filtered, //
	    ValueProvider<Setting, String> valueProvider) {

	return create(columnName, -1, sortable, filtered, true, valueProvider, null);
    }

    /**
     * @param columnName
     * @param sortable
     * @param filtered
     * @param valueProvider
     * @param renderer
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    boolean sortable, //
	    boolean filtered, //
	    ValueProvider<Setting, String> valueProvider, //
	    Renderer<HashMap<String, String>> renderer) {

	return create(columnName, -1, sortable, filtered, true, valueProvider, null, renderer);
    }

    /**
     * @param columnName
     * @param sortable
     * @param filtered
     * @param valueProvider
     * @param comparator
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    boolean sortable, //
	    boolean filtered, //
	    ValueProvider<Setting, String> valueProvider, //
	    Comparator<HashMap<String, String>> comparator) {

	return create(columnName, -1, sortable, filtered, true, valueProvider, comparator);
    }

    /**
     * @param columnName
     * @param sortable
     * @param filtered
     * @param valueProvider
     * @param comparator
     * @param renderer
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    boolean sortable, //
	    boolean filtered, //
	    ValueProvider<Setting, String> valueProvider, //
	    Comparator<HashMap<String, String>> comparator, //
	    Renderer<HashMap<String, String>> renderer) {

	return create(columnName, -1, sortable, filtered, true, valueProvider, comparator, renderer);
    }

    /**
     * @param columnName
     * @param sortable
     * @param filtered
     * @param valueProvider
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    int columnWidth, //
	    boolean sortable, //
	    boolean filtered, //
	    ValueProvider<Setting, String> valueProvider) {

	return create(columnName, columnWidth, sortable, filtered, true, valueProvider, null);
    }

    /**
     * @param columnName
     * @param columnWidth
     * @param sortable
     * @param filtered
     * @param valueProvider
     * @param renderer
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    int columnWidth, //
	    boolean sortable, //
	    boolean filtered, //
	    ValueProvider<Setting, String> valueProvider, //
	    Renderer<HashMap<String, String>> renderer) {

	return create(columnName, columnWidth, sortable, filtered, true, valueProvider, null, renderer);
    }

    /**
     * @param columnName
     * @param columnWidth
     * @param sortable
     * @param filtered
     * @param valueProvider
     * @param comparator
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    int columnWidth, //
	    boolean sortable, //
	    boolean filtered, //
	    ValueProvider<Setting, String> valueProvider, //
	    Comparator<HashMap<String, String>> comparator) {

	return create(columnName, columnWidth, sortable, filtered, true, valueProvider, comparator);
    }

    /**
     * @param columnName
     * @param columnWidth
     * @param sortable
     * @param filtered
     * @param valueProvider
     * @param comparator
     * @param renderer
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    int columnWidth, //
	    boolean sortable, //
	    boolean filtered, //
	    ValueProvider<Setting, String> valueProvider, //
	    Comparator<HashMap<String, String>> comparator, //
	    Renderer<HashMap<String, String>> renderer) {

	return create(columnName, columnWidth, sortable, filtered, true, valueProvider, comparator, renderer);
    }

    /**
     * @param columnName
     * @param sortable
     * @param filtered
     * @param visible
     * @param valueProvider
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    boolean sortable, //
	    boolean filtered, //
	    boolean visible, //
	    ValueProvider<Setting, String> valueProvider) {

	return create(columnName, -1, sortable, filtered, visible, valueProvider, null);
    }

    /**
     * @param columnName
     * @param sortable
     * @param filtered
     * @param visible
     * @param valueProvider
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    boolean sortable, //
	    boolean filtered, //
	    boolean visible, //
	    ValueProvider<Setting, String> valueProvider, //
	    Renderer<HashMap<String, String>> renderer) {

	return create(columnName, -1, sortable, filtered, visible, valueProvider, null, renderer);
    }

    /**
     * @param columnName
     * @param columnWidth
     * @param sortable
     * @param filtered
     * @param visible
     * @param valueProvider
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    int columnWidth, //
	    boolean sortable, //
	    boolean filtered, //
	    boolean visible, //
	    ValueProvider<Setting, String> valueProvider, //
	    Comparator<HashMap<String, String>> comparator) {

	return create(columnName, columnWidth, sortable, filtered, visible, valueProvider, comparator, null);
    }

    /**
     * @param columnName
     * @param columnWidth
     * @param sortable
     * @param filtered
     * @param visible
     * @param valueProvider
     * @param comparator
     * @param renderer
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    int columnWidth, //
	    boolean sortable, //
	    boolean filtered, //
	    boolean visible, //
	    ValueProvider<Setting, String> valueProvider, //
	    Comparator<HashMap<String, String>> comparator, //
	    Renderer<HashMap<String, String>> renderer) {

	ColumnDescriptor descriptor = new ColumnDescriptor();
	descriptor.setColumnName(columnName);
	descriptor.setColumnWidth(columnWidth);
	descriptor.setSortable(sortable);
	descriptor.setFiltered(filtered);
	descriptor.setValueProvider(valueProvider);
	descriptor.setColumnVisible(visible);
	descriptor.setComparator(comparator);
	descriptor.setRenderer(renderer);

	return descriptor;
    }

    /**
     * @param columnName
     * @param columnWidth
     * @param sortable
     * @param filtered
     * @param visible
     * @param valueProvider
     * @return
     */
    public static ColumnDescriptor create(//
	    String columnName, //
	    int columnWidth, //
	    boolean sortable, //
	    boolean filtered, //
	    boolean visible, //
	    Renderer<HashMap<String, String>> renderer) {

	ColumnDescriptor descriptor = new ColumnDescriptor();
	descriptor.setColumnName(columnName);
	descriptor.setColumnWidth(columnWidth);
	descriptor.setSortable(sortable);
	descriptor.setFiltered(filtered);
	descriptor.setColumnVisible(visible);
	descriptor.setRenderer(renderer);

	return descriptor;
    }

    /**
     * @param visible
     */
    public void setColumnVisible(boolean visible) {

	this.visible = visible;
    }

    /**
     * @return the sortable
     */
    public boolean isColumnVisible() {

	return visible;
    }

    /**
     * @return
     */
    public int getColumnWidth() {

	return columnWidth;
    }

    /**
     * @param columnWidth
     */
    public void setColumnWidth(int columnWidth) {

	this.columnWidth = columnWidth;
    }

    /**
     * @return the sortable
     */
    public boolean isSortable() {

	return sortable;
    }

    /**
     * @param sortable the sortable to set
     */
    public void setSortable(boolean sortable) {

	this.sortable = sortable;
    }

    /**
     * @return the filtered
     */
    public boolean isFiltered() {

	return filtered;
    }

    /**
     * @param sortable the filtered to set
     */
    public void setFiltered(boolean filtered) {

	this.filtered = filtered;
    }

    /**
     * @return
     */
    public String getColumnName() {

	return columnName;
    }

    /**
     * @param columnName
     */
    public void setColumnName(String columnName) {

	this.columnName = columnName;
    }

    /**
     * @return
     */
    public ValueProvider<Setting, String> getValueProvider() {

	return valueProvider;
    }

    /**
     * @param valueProvider
     */
    public void setValueProvider(ValueProvider<Setting, String> valueProvider) {

	this.valueProvider = valueProvider;
    }

    /**
     * @return the comparator
     */
    public Optional<Comparator<HashMap<String, String>>> getComparator() {

	return Optional.ofNullable(comparator);
    }

    /**
     * @param comparator the comparator to set
     */
    public void setComparator(Comparator<HashMap<String, String>> comparator) {

	this.comparator = comparator;
    }

    /**
     * @return
     */
    public Optional<Renderer<HashMap<String, String>>> getRenderer() {

	return Optional.ofNullable(renderer);
    }

    /**
     * @param renderer
     */
    public void setRenderer(Renderer<HashMap<String, String>> renderer) {

	this.renderer = renderer;
    }

    /**
     * @param hasCheckBox
     */
    void setHasCheckbox(boolean hasCheckBox) {

	this.hasCheckBox = hasCheckBox;
    }

    /**
     * @return
     */
    boolean hasCheckBox() {

	return hasCheckBox;
    }
}
