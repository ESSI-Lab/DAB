package eu.essi_lab.messages;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class JVMOptionTest {

    @Test
    public void test() {

	Assert.assertTrue(JVMOption.CHECK_CONFIG.getDefaultBooleanValue().get());

	Assert.assertEquals(JVMOption.CHECK_CONFIG.getDefaultBooleanValue(), JVMOption.isEnabled(JVMOption.CHECK_CONFIG));

	//
	//
	//

	Assert.assertTrue(JVMOption.UPDATE_DATA_FOLDER_INDEX.getDefaultBooleanValue().get());

	Assert.assertEquals(JVMOption.UPDATE_DATA_FOLDER_INDEX.getDefaultBooleanValue(),
		JVMOption.isEnabled(JVMOption.UPDATE_DATA_FOLDER_INDEX));

	//
	//
	//

	Assert.assertFalse(JVMOption.INIT_CACHES.getDefaultBooleanValue().get());

	Assert.assertEquals(JVMOption.INIT_CACHES.getDefaultBooleanValue(), JVMOption.isEnabled(JVMOption.INIT_CACHES));

	//
	//
	//

	Assert.assertFalse(JVMOption.INIT_OPENSEARCH_INDEXES.getDefaultBooleanValue().get());

	Assert.assertEquals(JVMOption.INIT_OPENSEARCH_INDEXES.getDefaultBooleanValue(),
		JVMOption.isEnabled(JVMOption.INIT_OPENSEARCH_INDEXES));

	//
	//
	//

	Assert.assertFalse(JVMOption.DEBUG_OPENSEARCH_QUERIES.getDefaultBooleanValue().get());

	Assert.assertEquals(JVMOption.DEBUG_OPENSEARCH_QUERIES.getDefaultBooleanValue(),
		JVMOption.isEnabled(JVMOption.DEBUG_OPENSEARCH_QUERIES));

	//
	//
	//

	Assert.assertFalse(JVMOption.FORCE_VOLATILE_DB.getDefaultBooleanValue().get());

	Assert.assertEquals(JVMOption.FORCE_VOLATILE_DB.getDefaultBooleanValue(), JVMOption.isEnabled(JVMOption.FORCE_VOLATILE_DB));

	//
	//
	//

	Assert.assertFalse(JVMOption.SKIP_CONFIG_AUTHORIZATION.getDefaultBooleanValue().get());

	Assert.assertEquals(JVMOption.SKIP_CONFIG_AUTHORIZATION.getDefaultBooleanValue(),
		JVMOption.isEnabled(JVMOption.SKIP_CONFIG_AUTHORIZATION));

	//
	//
	//

	Assert.assertFalse(JVMOption.SKIP_REQUESTS_AUTHORIZATION.getDefaultBooleanValue().get());

	Assert.assertEquals(JVMOption.SKIP_REQUESTS_AUTHORIZATION.getDefaultBooleanValue(),
		JVMOption.isEnabled(JVMOption.SKIP_REQUESTS_AUTHORIZATION));

	//
	//
	//

	Assert.assertFalse(JVMOption.SKIP_HEALTH_CHECK.getDefaultBooleanValue().get());

	Assert.assertEquals(JVMOption.SKIP_HEALTH_CHECK.getDefaultBooleanValue(), JVMOption.isEnabled(JVMOption.SKIP_HEALTH_CHECK));

	//
	//
	//

	Assert.assertFalse(JVMOption.SKIP_GDAL_TEST.getDefaultBooleanValue().get());

	Assert.assertEquals(JVMOption.SKIP_GDAL_TEST.getDefaultBooleanValue(), JVMOption.isEnabled(JVMOption.SKIP_GDAL_TEST));

	//
	//
	//

	Assert.assertFalse(JVMOption.CONFIGURATION_URL.getDefaultStringValue().isPresent());

	Assert.assertFalse(JVMOption.getStringValue(JVMOption.CONFIGURATION_URL).isPresent());

	//
	//
	//

	Assert.assertFalse(JVMOption.S3_ENDPOINT.getDefaultStringValue().isPresent());

	Assert.assertFalse(JVMOption.getStringValue(JVMOption.S3_ENDPOINT).isPresent());

	//
	//
	//

	Assert.assertFalse(JVMOption.NUMBER_OF_DATA_FOLDER_INDEX_SHARDS.getDefaultStringValue().isPresent());

	Assert.assertFalse(JVMOption.getIntValue(JVMOption.NUMBER_OF_DATA_FOLDER_INDEX_SHARDS).isPresent());

	//
	//
	//

	Assert.assertEquals(Integer.MAX_VALUE, (int) JVMOption.ANONYMOUS_OFFSET_LIMIT.getDefaultIntValue().get());

	// since the option is not declared, we expect its default value
	Assert.assertEquals((int) JVMOption.ANONYMOUS_OFFSET_LIMIT.getDefaultIntValue().get(),
		(int) JVMOption.getIntValue(JVMOption.ANONYMOUS_OFFSET_LIMIT).get());

	//
	//
	//

	Assert.assertEquals(200, (int) JVMOption.ANONYMOUS_PAGE_SIZE_LIMIT.getDefaultIntValue().get());

	// since the option is not declared, we expect its default value
	Assert.assertEquals((int) JVMOption.ANONYMOUS_PAGE_SIZE_LIMIT.getDefaultIntValue().get(),
		(int) JVMOption.getIntValue(JVMOption.ANONYMOUS_PAGE_SIZE_LIMIT).get());
    }
}
