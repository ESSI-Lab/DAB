/**
 * 
 */
package eu.essi_lab.cfga.scheme;

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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public interface SchemeItem {

    /**
     * @author Fabrizio
     */
    public static class Descriptor {

	private Function<Setting, Boolean> descriptor;
	private Class<? extends Setting> clazz;

	/**
	 * @param descriptor
	 * @return
	 */
	public static Descriptor of(Function<Setting, Boolean> descriptor) {

	    return new Descriptor(descriptor, null);
	}

	/**
	 * @param descriptor
	 * @param clazz
	 * @return
	 */
	public static Descriptor of(Function<Setting, Boolean> descriptor, Class<? extends Setting> clazz) {

	    return new Descriptor(descriptor, clazz);
	}

	/**
	 * @return
	 */
	public Function<Setting, Boolean> getFunction() {

	    return descriptor;
	}

	/**
	 * @return
	 */
	public Optional<Class<? extends Setting>> getSettingClass() {

	    return Optional.ofNullable(clazz);
	}

	/**
	 * @param descriptor
	 * @param clazz
	 */
	private Descriptor(Function<Setting, Boolean> descriptor, Class<? extends Setting> clazz) {

	    this.descriptor = descriptor;
	    this.clazz = clazz;
	}
    };

    /**
     * Returns <code>true</code> if this item has always at least one instance of the described {@link Setting}
     * 
     * @see #getDescriptors()
     * @return
     */
    public boolean required();

    /**
     * Returns a list of {@link Descriptor}s which describe the {@link Setting}/s of this item
     * 
     * @return
     */
    public List<Descriptor> getDescriptors();

}
