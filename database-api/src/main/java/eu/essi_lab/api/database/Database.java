package eu.essi_lab.api.database;

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

import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;

/**
 * Marker interface for Database instances. A Database instance must be able to establish a single connection with a
 * Database
 * and to interact using different sessions<br>
 * <br>
 * <b>Implementation notes</b><br>
 * <br>
 * Implementation should publish a complete mid-level/low-level API to read and write
 * the underlying system. This low-level API is published by {@link DatabaseConsumer} instances through
 * high-level interfaces
 * 
 * @see DatabaseConsumer
 * @see DatabaseReader
 * @see DatabaseWriter
 * @author Fabrizio
 */
public interface Database<T extends DatabaseSetting> extends Configurable<T> {

    /**
     * @author Fabrizio
     */
    public enum DatabaseImpl {

	/**
	 * 
	 */
	MARK_LOGIC("MarkLogic"),
	/**
	 * 
	 */
	EXIST_EMBEDDED("eXistEmbedded");

	private String name;

	private DatabaseImpl(String name) {

	    this.name = name;
	}

	public String getName() {

	    return name;
	}

	public String toString() {

	    return getName();
	}
    }
}
