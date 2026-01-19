package eu.essi_lab.messages;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class JavaOptionsTest {

    @Test
    public void test() {

	Assert.assertTrue(JavaOptions.CHECK_CONFIG.getDefaultBooleanValue().get());

	Assert.assertTrue(JavaOptions.UPDATE_DATA_FOLDER_INDEX.getDefaultBooleanValue().get());

	Assert.assertFalse(JavaOptions.INIT_CACHES.getDefaultBooleanValue().get());

	Assert.assertFalse(JavaOptions.INIT_OPENSEARCH_INDEXES.getDefaultBooleanValue().get());

	Assert.assertFalse(JavaOptions.DEBUG_OPENSEARCH_QUERIES.getDefaultBooleanValue().get());

	Assert.assertFalse(JavaOptions.FORCE_VOLATILE_DB.getDefaultBooleanValue().get());

	Assert.assertFalse(JavaOptions.SKIP_CONFIG_AUTHORIZATION.getDefaultBooleanValue().get());

	Assert.assertFalse(JavaOptions.SKIP_REQUESTS_AUTHORIZATION.getDefaultBooleanValue().get());

	Assert.assertFalse(JavaOptions.SKIP_HEALTH_CHECK.getDefaultBooleanValue().get());

	Assert.assertFalse(JavaOptions.SKIP_GDAL_TEST.getDefaultBooleanValue().get());

	Assert.assertFalse(JavaOptions.CONFIGURATION_URL.getDefaultBooleanValue().isPresent());
	Assert.assertFalse(JavaOptions.S3_ENDPOINT.getDefaultBooleanValue().isPresent());
	Assert.assertFalse(JavaOptions.NUMBER_OF_DATA_FOLDER_INDEX_SHARDS.getDefaultBooleanValue().isPresent());
    }
}
