package eu.essi_lab.cfga.gui.components.tabs.descriptor;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.util.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class TabDescriptor {

    private String label;
    private int index;
    private boolean forceReadOnly;
    private List<TabContentDescriptor> list;

    /**
     * @param index
     * @param descriptors
     * @return
     */
    public static TabDescriptor of(int index, String label, TabContentDescriptor... descriptors) {

	TabDescriptor out = new TabDescriptor(index);
	out.setLabel(label);
	Stream.of(descriptors).forEach(out::addContentDescriptor);

	return out;
    }

    /**
     *
     */
    public TabDescriptor() {

	list = new ArrayList<>();
	setForceReadOnly(true);
    }

    /**
     * @param index
     */
    public TabDescriptor(int index) {

	this();
	setIndex(index);
    }

    /**
     * @param index
     * @param label
     */
    public TabDescriptor(int index, String label) {

	this();
	setIndex(index);
	setLabel(label);
    }

    /**
     * @return
     */
    public String getLabel() {

	return label;
    }

    /**
     * @param label
     */
    public void setLabel(String label) {

	this.label = label;
    }

    /**
     * @param descriptor
     */
    public void addContentDescriptors(TabContentDescriptor... descriptors) {

	list.addAll(Arrays.asList(descriptors));
    }

    /**
     * @param descriptor
     */
    public void addContentDescriptor(TabContentDescriptor descriptor) {

	list.add(descriptor);
    }

    /**
     * @return
     */
    public List<TabContentDescriptor> getContentDescriptors() {

	return list;
    }

    /**
     * @param forceReadOnly
     */
    public void setForceReadOnly(boolean forceReadOnly) {

	this.forceReadOnly = forceReadOnly;
    }

    /**
     * @return
     */
    public boolean isForceReadOnlySet() {

	return forceReadOnly;
    }

    /**
     * @return
     */
    public int getIndex() {

	return index;
    }

    /**
     * @param index
     */
    public void setIndex(int index) {

	this.index = index;
    }

    @Override
    public String toString() {

	return "Tab#" + getIndex();
    }

    @Override
    public int hashCode() {

	return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {

	return this.hashCode() == o.hashCode();
    }
}
