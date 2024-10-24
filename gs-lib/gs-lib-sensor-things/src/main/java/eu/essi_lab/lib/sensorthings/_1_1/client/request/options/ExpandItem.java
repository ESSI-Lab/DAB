package eu.essi_lab.lib.sensorthings._1_1.client.request.options;

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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.AbstractMap.SimpleEntry;

import eu.essi_lab.lib.sensorthings._1_1.client.request.Composable;
import eu.essi_lab.lib.sensorthings._1_1.client.request.EntityRef;

/**
 * @author Fabrizio
 */
public class ExpandItem implements Composable {

    /**
     * @author Fabrizio
     */
    public enum Operation {

	/**
	 * 
	 */
	FILTER,
	/**
	 * 
	 */
	SELECT
    }

    /**
     * 
     */
    private Operation operation;

    /**
     * 
     */
    private String operatorValue;

    /**
     * 
     */
    private SimpleEntry<EntityRef, EntityRef> hierarchy;

    /**
     * 
     */
    private EntityRef entityRef;

    /**
     * @param parentEntity
     * @param childProperty
     * @return
     */
    public static ExpandItem get(EntityRef parentEntity, EntityRef childProperty) {

	return get(parentEntity, childProperty, null, null);
    }

    /**
     * @param parentEntity
     * @param childEntity
     * @param operation
     * @param value
     * @return
     */
    public static ExpandItem get(EntityRef parentEntity, EntityRef childEntity, Operation operation, String value) {

	ExpandItem item = new ExpandItem();

	item.hierarchy = new SimpleEntry<EntityRef, EntityRef>(parentEntity, childEntity);
	item.operation = operation;
	item.operatorValue = value;

	return item;
    }

    /**
     * @param entity
     * @return
     */
    public static ExpandItem get(EntityRef entity) {

	return get(entity, null, null);
    }

    /**
     * @param entity
     * @param operation
     * @param value
     * @return
     */
    public static ExpandItem get(EntityRef entity, Operation operation, String value) {

	ExpandItem item = new ExpandItem();

	item.entityRef = entity;
	item.operation = operation;
	item.operatorValue = value;

	return item;
    }

    @Override
    public String compose() throws IllegalArgumentException {

	StringBuilder builder = new StringBuilder();

	if (hierarchy != null) {

	    builder.append(hierarchy.getKey().getName());
	    builder.append("/");
	    builder.append(hierarchy.getValue().getName());

	} else {

	    builder.append(entityRef.getName());
	}

	if (operation != null) {

	    builder.append("(");

	    switch (operation) {
	    case FILTER:
		builder.append("$filter=");
		try {
		    builder.append(URLEncoder.encode(operatorValue, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
		}
		break;
	    case SELECT:
		builder.append("$select=");
		builder.append(operatorValue);
		break;
	    }

	    builder.append(")");
	}

	return builder.toString();
    }

    @Override
    public String toString() {

	return compose();
    }

}
