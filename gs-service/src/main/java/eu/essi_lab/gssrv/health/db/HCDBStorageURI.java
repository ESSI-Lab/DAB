package eu.essi_lab.gssrv.health.db;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageUri;
public class HCDBStorageURI extends StorageUri {

    /**
     * 
     */
    private static final long serialVersionUID = -4260530022257536435L;
    @JsonIgnore
    private transient Logger logger = GSLoggerFactory.getLogger(HCDBStorageURI.class);
    private static final String CONFIG_FOLDER = "CONFIG_FOLDER";
    private static final String P = "P";
    private static final String NAME = "NAME";
    private static final String URI = "URI";
    private static final String USER = "USER";

    /**
     * 
     */
    public HCDBStorageURI() {

	setConfigFolder(CONFIG_FOLDER);
	setPassword(P);
	setStorageName(NAME);
	setUri(URI);
	setUser(USER);
    }

    @Override
    public boolean equals(Object object) {

	return Objects.nonNull(object) && (object instanceof HCDBStorageURI);
    }

    @Override
    public String toString() {

	return this.getClass().getName();
    }

    @Override
    public int hashCode() {

	return toString().hashCode();
    }

}
