package eu.essi_lab.lib.sensorthings._1_1.client.request.options;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.lib.sensorthings._1_1.client.request.Composable;
import eu.essi_lab.lib.sensorthings._1_1.client.request.EntityRef;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.ExpandItem.Operation;

/**
 * @author Fabrizio
 */
public class ExpandOption implements Composable {

    private List<ExpandItem> items;

    /**
     * @param parentProperty
     * @param childProperty
     */
    public ExpandOption(EntityRef parentProperty, EntityRef childProperty) {

	this(ExpandItem.get(parentProperty, childProperty));
    }

    /**
     * @param parentProperty
     * @param childProperty
     * @param operation
     * @param value
     */
    public ExpandOption(EntityRef parentProperty, EntityRef childProperty, Operation operation, String value) {

	this(ExpandItem.get(parentProperty, childProperty, operation, value));
    }

    /**
     * @param property
     */
    public ExpandOption(EntityRef property) {

	this(ExpandItem.get(property));
    }

    /**
     * @param property
     * @param operation
     * @param value
     */
    public ExpandOption(EntityRef property, Operation operation, String value) {

	this(ExpandItem.get(property, operation, value));
    }

    /**
     * 
     */
    public ExpandOption(ExpandItem item) {

	this.items = new ArrayList<ExpandItem>();
	this.items.add(item);
    }

    /**
     * @return
     */
    public ExpandOption(ExpandItem... items) {

	this.items = Arrays.asList(items);
    }

    @Override
    public String compose() throws IllegalArgumentException {

	StringBuilder builder = new StringBuilder();

	builder.append("&$expand=");

	builder.append(items.stream().map(i -> i.compose()).collect(Collectors.joining(",")));

	return builder.toString();
    }

    @Override
    public String toString() {

	return compose();
    }
}
