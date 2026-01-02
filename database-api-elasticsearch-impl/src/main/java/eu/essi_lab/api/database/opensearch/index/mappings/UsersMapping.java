/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

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

import org.opensearch.client.opensearch._types.mapping.FieldType;

import eu.essi_lab.api.database.Database;

/**
 * @author Fabrizio
 */
public class UsersMapping extends IndexMapping {

    /**
     * 
     */
    private static final String USERS_INDEX = Database.USERS_FOLDER + "-index";

    //
    // users-index properties
    //
    public static final String USER = "user";
    public static final String USER_ID = "userId";
    public static final String USER_ID_TYPE = "userIdType";

    public static final String USER_ROLE = "userRole";
    public static final String ENABLED = "enabled";

    private static UsersMapping instance;

    /**
     * @return
     */
    public static final UsersMapping get() {

	if (instance == null) {

	    instance = new UsersMapping();
	}

	return instance;
    }

    /**
     *  
     */
    private UsersMapping() {

	super(USERS_INDEX);

	// mandatory
	addProperty(USER_ID, FieldType.Text.jsonValue());
	addProperty(USER_ROLE, FieldType.Text.jsonValue());
	addProperty(ENABLED, FieldType.Boolean.jsonValue());
	// optional
	addProperty(USER_ID_TYPE, FieldType.Text.jsonValue());

	addProperty(toKeywordField(USER_ID), FieldType.Keyword.jsonValue());
	addProperty(toKeywordField(USER_ROLE), FieldType.Keyword.jsonValue());
	addProperty(toKeywordField(USER_ID_TYPE), FieldType.Keyword.jsonValue());
    }
}
