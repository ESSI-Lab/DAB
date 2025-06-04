package eu.essi_lab.model.resource;

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

/**
 * An implementing class copies its own properties to a target adapter of the same class <code>T</code>, according to a
 * supplied {@link AdaptPolicy}. The properties to handle entirely depend on the implementing class, with no
 * restrictions
 * 
 * @author Fabrizio
 * @param <T>
 */
public interface PropertiesAdapter<T> {

    /**
     * Policy to use when adapting the properties of this adapter to the target adapter
     * 
     * @see PropertiesAdapter#adapt(Object, AdaptPolicy)
     */
    public enum AdaptPolicy {
	/**
	 * Copies the properties of this adapter to the target adapter only if the target adapter is missing such
	 * properties. Use this policy when the properties of the target must be preserved
	 */
	ON_EMPTY,
	/**
	 * Adds the properties of this adapter to the target adapter
	 */
	ADD,
	/**
	 * If these adapter has properties to copy, then clears the properties of the target adapter and copies the
	 * properties of this adapter, otherwise calling the PropertiesAdapter#adapt(Object, AdaptPolicy) method has no
	 * effect. Use this policy when the properties of this adapter must be preserved
	 */
	OVERRIDE;
    }

    /**
     * Compares the <code>properties</code> of this adapter with the supplied <code>target</code> properties, and copies
     * these adapter properties to the supplied <code>target</code> according to the given <code>policy</code>
     * 
     * @param target
     * @param policy
     * @param properties list of properties to adapt, can be omitted in order to adapt all the adapter properties
     */
    public void adapt(T target, AdaptPolicy policy, String... properties);
}
