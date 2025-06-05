package eu.essi_lab.messages.bond;

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

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import eu.essi_lab.model.resource.MetadataElement;

/**
 * A bond applied on a {@link SpatialEntity}
 * 
 * @see SpatialEntity
 * @author Fabrizio
 */
@XmlRootElement
@XmlSeeAlso(SpatialExtent.class)
public class SpatialBond extends MetadataElementBond<SpatialEntity> {

    /**
     * No-arg constructor only to be used by JAXB
     */
    public SpatialBond() {

    }

    /**
     * Supported operators are spatial operators:
     * <ul>
     * <li>{@link BondOperator#BBOX}</li>
     * <li>{@link BondOperator#CONTAINS}</li>
     * <li>{@link BondOperator#INTERSECTS}</li>
     * <li>{@link BondOperator#DISJOINT}</li>
     * </ul>
     * 
     * @param operator
     * @param value
     */
    SpatialBond(BondOperator operator, SpatialEntity value) {
	super(operator, MetadataElement.BOUNDING_BOX, value);
    }

    /**
     * Supported operators are spatial operators:
     * <ul>
     * <li>{@link BondOperator#BBOX}</li>
     * <li>{@link BondOperator#CONTAINS}</li>
     * <li>{@link BondOperator#INTERSECTS}</li>
     * <li>{@link BondOperator#DISJOINT}</li>
     * </ul>
     *
     * @param value
     */
    SpatialBond(SpatialEntity value) {
	super(null, MetadataElement.BOUNDING_BOX, value);
    }

    /**
     * Supported operators are spatial operators:
     * <ul>
     * <li>{@link BondOperator#BBOX}</li>
     * <li>{@link BondOperator#CONTAINS}</li>
     * <li>{@link BondOperator#INTERSECTS}</li>
     * <li>{@link BondOperator#DISJOINT}</li>
     * </ul>
     * 
     * @param operator
     */
    public void setSpatialOperator(BondOperator operator) {

	setOperator(operator);
    }

    @Override
    protected SpatialBond createClone(BondOperator type, MetadataElement element, SpatialEntity value) {
	SpatialEntity spatialEntityClone = getPropertyValue().clone();
	SpatialBond clone = new SpatialBond(getOperator(), spatialEntityClone);
	return clone;
    }
}
