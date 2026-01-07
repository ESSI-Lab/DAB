package eu.essi_lab.messages;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;

import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.SortOrder;

/**
 * 
 */
public class SortedFields {

    private List<SimpleEntry<Queryable, SortOrder>> fields;

    /**
     * @param fields
     */
    public static SortedFields of(List<SimpleEntry<Queryable, SortOrder>> fields) {

	return new SortedFields(fields);
    }

    /**
     * @param fields
     */
    @SafeVarargs
    public static SortedFields of(SimpleEntry<Queryable, SortOrder>... fields) {

	return new SortedFields(fields);
    }

    /**
     * @param property
     * @param order
     */
    public static SortedFields of(Queryable property, SortOrder order) {

	return new SortedFields(property, order);
    }

    /**
     * @param fields
     */
    public SortedFields(List<SimpleEntry<Queryable, SortOrder>> fields) {

	this.fields = fields;
    }

    /**
     * @param fields
     */
    @SafeVarargs
    public SortedFields(SimpleEntry<Queryable, SortOrder>... fields) {

	this(Arrays.asList(fields));
    }

    /**
     * @param property
     * @param order
     */
    public SortedFields(Queryable property, SortOrder order) {

	this(Arrays.asList(new SimpleEntry<>(property, order)));
    }

    /**
     * @return
     */
    public List<SimpleEntry<Queryable, SortOrder>> getFields() {

	return fields;
    }

    /**
     * @param fields
     */
    public void setFields(List<SimpleEntry<Queryable, SortOrder>> fields) {

	this.fields = fields;
    }
}
