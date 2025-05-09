package eu.essi_lab.messages;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.resource.ResourceProperty;

public class SortedFields {
    private List<SimpleEntry<Queryable, SortOrder>> fields = new ArrayList<>();

    public SortedFields(List<SimpleEntry<Queryable, SortOrder>> fields) {
	this.fields = fields;
    }

    public SortedFields(Queryable property, SortOrder order) {
	this(Arrays.asList(new SimpleEntry(property,order)));
    }

    public List<SimpleEntry<Queryable, SortOrder>> getFields() {
        return fields;
    }

    public void setFields(List<SimpleEntry<Queryable, SortOrder>> fields) {
        this.fields = fields;
    }
}
