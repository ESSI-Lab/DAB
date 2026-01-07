package eu.essi_lab.lib.sensorthings._1_1.client.request;

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

/**
 * The resource path can be terminated with an {@link EntityProperty} or an {@link AssociationLink}.<br>
 * If the resource path terminates with one of these segments, no system query options can be set
 * 
 * @author Fabrizio
 */
public interface AddressingPathSegment extends Composable {

    /**
     * 
     */
    public static AssociationLink ASSOCIATION_LINK = new AssociationLink();

    /**
     * @return
     */
    public static EntityProperty getProperty() {

	return new EntityProperty();
    }

    /**
     * @param name
     * @return
     */
    public static EntityProperty getProperty(String name) {

	return new EntityProperty().setName(name);
    }

    /**
     * @param name
     * @param getValue
     * @return
     */
    public static EntityProperty getProperty(String name, boolean getValue) {

	return new EntityProperty().setName(name).setGetValue(getValue);
    }
}
