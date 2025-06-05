package eu.essi_lab.gssrv.health.db;

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

import java.util.Objects;

import eu.essi_lab.model.StorageInfo;

/**
 * @author Fabrizio
 */
public class HCStorageInfo extends StorageInfo {

    /**
     * 
     */
    private static final long serialVersionUID = -4260530022257536435L;

    /**
     * 
     */
    public HCStorageInfo() {

	setIdentifier("Folder");
	setPassword("Pwd");
	setName("HCBD");
	setUri("http://HCBD");
	setUser("User");
    }

    @Override
    public boolean equals(Object object) {

	return Objects.nonNull(object) && (object instanceof HCStorageInfo);
    }

    @Override
    public String toString() {

	return this.getClass().getSimpleName();
    }

    @Override
    public int hashCode() {

	return toString().hashCode();
    }

}
