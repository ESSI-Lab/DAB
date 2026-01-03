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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public interface SchemeItem {

    /**
     * @author Fabrizio
     */
    public static class Descriptor {

	private final Function<Setting, Boolean> descriptor;
	private final Supplier<Setting> creator;

	/**
	 * @param descriptor
	 * @return
	 */
	public static Descriptor of(Function<Setting, Boolean> descriptor) {

	    return new Descriptor(descriptor, null);
	}

	/**
	 * @param descriptor
	 * @param creator
	 * @return
	 */
	public static Descriptor of(Function<Setting, Boolean> descriptor, Supplier<Setting> creator) {

	    return new Descriptor(descriptor, creator);
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
	public Optional<Supplier<Setting>> create() {

	    return Optional.ofNullable(creator);
	}

	/**
	 * @param descriptor
	 * @param clazz
	 */
	private Descriptor(Function<Setting, Boolean> descriptor, Supplier<Setting> supplier) {

	    this.descriptor = descriptor;
	    this.creator = supplier;

	}

    }

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
