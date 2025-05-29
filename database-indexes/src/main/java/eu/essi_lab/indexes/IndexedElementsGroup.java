package eu.essi_lab.indexes;

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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.index.IndexedElementInfo;

/**
 * Abstract class which groups a collection of {@link IndexedElement}s
 * 
 * @author Fabrizio
 */
public abstract class IndexedElementsGroup {

    /**
     * Retrieves all the {@link IndexedElement}s declared declared by the supplied {@link Class}
     *
     * @param clazz
     * @return
     */
    protected static List<IndexedElement> getIndexes(Class<? extends IndexedElementsGroup> clazz) {

	return Arrays.asList(clazz.getFields()).//
		stream().//
		map(IndexedElementsGroup::getIndexedElement).//
		sorted((i1, i2) -> i1.getElementName().compareTo(i2.getElementName())).//
		collect(Collectors.toList());//
    }

    /**
     * Retrieves all the {@link IndexedElementInfo}s owned by the {@link IndexedElement}s
     * declared by the supplied {@link Class}
     * 
     * @param clazz
     * @param impl
     * @return
     */
    protected static List<IndexedElementInfo> getIndexesInfo(Class<? extends IndexedElementsGroup> clazz, DatabaseImpl impl) {

	return getIndexes(clazz).//
		stream().//
		map(el -> el.getInfo(impl.getName())).//
		collect(Collectors.toList());//
    }

    /**
     * @param value
     * @return
     */
    protected static boolean checkStringValue(String value) {

	return value != null && !value.equals("");
    }

    /**
     * @param field
     * @return
     */
    protected static IndexedElement getIndexedElement(Field field) {

	try {
	    return (IndexedElement) field.get(null);
	} catch (Exception e) {
	}

	return null;
    }
}
