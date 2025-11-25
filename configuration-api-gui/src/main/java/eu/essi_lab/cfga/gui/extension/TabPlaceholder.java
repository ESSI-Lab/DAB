package eu.essi_lab.cfga.gui.extension;

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
public class TabPlaceholder {

    private int index;
    private List<TabDescriptor> list;

    /**
     *
     * @param index
     * @param descriptors
     * @return
     */
    public static TabPlaceholder of(int index, TabDescriptor ... descriptors){

	TabPlaceholder placeholder = new TabPlaceholder(index);
	Stream.of(descriptors).forEach(placeholder::addDescriptor);

	return placeholder;
    }

    /**
     *
     * @param index
     */
    private TabPlaceholder(int index) {

	list = new ArrayList<>();
	setIndex(index);
    }

    /**
     *
     * @param descriptor
     */
    public void addDescriptor(TabDescriptor descriptor){

	list.add(descriptor);
    }

    /**
     *
     * @return
     */
    public List<TabDescriptor> getDescriptors(){

	return list;
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
