/**
 * 
 */
package eu.essi_lab.pdk.rsm.impl.atom;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Objects;

import eu.essi_lab.lib.xml.atom.CustomEntry;

/**
 * @author Fabrizio
 */
public class LandsatTag extends SatelliteTag {

    public LandsatTag() {
    }

    /**
     * @param row
     */
    public void setRow(String row) {

	if (Objects.nonNull(row)) {

	    CustomEntry.addContentTo(acquisition, "row", row);
	}
    }

    /**
     * @param path
     */
    public void setPath(String path) {

	if (Objects.nonNull(path)) {

	    CustomEntry.addContentTo(acquisition, "path", path);
	}
    }

   
}
