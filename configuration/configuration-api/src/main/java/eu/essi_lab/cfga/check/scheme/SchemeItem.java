/**
 *
 */
package eu.essi_lab.cfga.check.scheme;

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

import eu.essi_lab.cfga.setting.*;

import java.util.*;
import java.util.function.*;

/**
 * @author Fabrizio
 */
public interface SchemeItem {

    /**
     * @author Fabrizio
     */
    class Descriptor {

	private final Function<Setting, Boolean> descriptor;
	private final Supplier<Setting> creator;
	private final boolean singleton;

	/**
	 * @param clazz
	 * @return
	 */
	public static Descriptor of(Class<? extends Setting> clazz) {

	    return new Descriptor((s) -> s.getSettingClass().equals(clazz), () -> SettingUtils.create(clazz), true);
	}

	/**
	 * @param descriptor
	 * @param creator
	 * @return
	 */
	public static Descriptor of(Function<Setting, Boolean> descriptor, Supplier<Setting> creator) {

	    return new Descriptor(descriptor, creator, true);
	}

	/**
	 * @return
	 */
	public Function<Setting, Boolean> describe() {

	    return descriptor;
	}

	/**
	 * @return
	 */
	public Supplier<Setting> create() {

	    return creator;
	}

	/**
	 * @param descriptor
	 * @param supplier
	 * @param singleton
	 */
	private Descriptor(Function<Setting, Boolean> descriptor, Supplier<Setting> supplier, boolean singleton) {

	    this.descriptor = descriptor;
	    this.creator = supplier;
	    this.singleton = singleton;
	}

	/**
	 * @return
	 */
	public boolean isSingleton() {

	    return singleton;
	}
    }

    /**
     * Returns a list of {@link Descriptor}s which describe and create the {@link Setting}/s of this item
     *
     * @return
     */
    List<Descriptor> getDescriptors();
}
