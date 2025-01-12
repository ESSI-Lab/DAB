/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

import org.opensearch.client.opensearch._types.mapping.FieldType;

import eu.essi_lab.api.database.Database;

/**
 * @author Fabrizio
 */
public class UsersMapping extends IndexMapping {

    /**
     * 
     */
    public static final String USERS_INDEX = Database.USERS_FOLDER + "-index";

    //
    // users-index properties
    //
    public static final String USER = "user";
    public static final String USER_ID = "userId";
    public static final String USER_ID_TYPE = "userIdType";

    public static final String USER_ROLE = "userRole";
    public static final String ENABLED = "enabled";

    /**
     * @return
     */
    public static final UsersMapping get() {

	return new UsersMapping();
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
    }
}
