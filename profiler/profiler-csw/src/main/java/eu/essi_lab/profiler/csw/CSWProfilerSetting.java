/**
 *
 */
package eu.essi_lab.profiler.csw;

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

import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.lib.utils.*;

/**
 * @author Fabrizio
 */
public class CSWProfilerSetting extends ProfilerSetting {

    /**
     * @author Fabrizio
     */
    public enum KeyValueOptionKeys implements LabeledEnum {

	/**
	 *
	 */
	USE_SEARCH_AFTER_OPTION("useSearchAfter"),

	/**
	 *
	 */
	SEARCH_AFTER_KEY_OPTION("searchAfterKey");

	private String name;

	/**
	 * @param name
	 */
	private KeyValueOptionKeys(String name) {

	    this.name = name;
	}

	@Override
	public String toString() {

	    return getLabel();
	}

	@Override
	public String getLabel() {

	    return name;
	}
    }

    /**
     *
     */
    public CSWProfilerSetting() {

	setServiceName("Catalog Service for the Web (CSW) ISO 2.0.2 Service");
	setServiceType("CSW");
	setServicePath("csw");
	setServiceVersion("2.0.2");
    }
}
