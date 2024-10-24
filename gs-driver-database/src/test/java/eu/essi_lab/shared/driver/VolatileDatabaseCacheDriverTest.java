/**
 * 
 */
package eu.essi_lab.shared.driver;

import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.model.StorageInfo;

/**
 * @author Fabrizio
 */
public class VolatileDatabaseCacheDriverTest extends DatabaseCacheDriverTest {

    private static final StorageInfo TEST_DB_URI = new DatabaseSetting(true).asStorageUri();

    /**
     * @param uri
     */
    public VolatileDatabaseCacheDriverTest() {

	setUri(TEST_DB_URI);
    }
}
