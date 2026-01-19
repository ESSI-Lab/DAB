package eu.essi_lab.messages;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class JavaOptionsTest {

    @Test
    public void test() {

	Assert.assertTrue(JavaOptions.CHECK_CONFIG.getDefaultValue().get());

	Assert.assertTrue(JavaOptions.UPDATE_DATA_FOLDER_INDEX.getDefaultValue().get());

	Assert.assertFalse(JavaOptions.INIT_CACHES.getDefaultValue().get());

	Assert.assertFalse(JavaOptions.INIT_OPENSEARCH_INDEXES.getDefaultValue().get());

	Assert.assertFalse(JavaOptions.DEBUG_OPENSEARCH_QUERIES.getDefaultValue().get());

	Assert.assertFalse(JavaOptions.FORCE_VOLATILE_DB.getDefaultValue().get());

	Assert.assertFalse(JavaOptions.SKIP_CONFIG_AUTHORIZATION.getDefaultValue().get());

	Assert.assertFalse(JavaOptions.SKIP_REQUESTS_AUTHORIZATION.getDefaultValue().get());

	Assert.assertFalse(JavaOptions.SKIP_HEALTH_CHECK.getDefaultValue().get());

	Assert.assertFalse(JavaOptions.SKIP_GDAL_TEST.getDefaultValue().get());

	Assert.assertFalse(JavaOptions.CONFIGURATION_URL.getDefaultValue().isPresent());
	Assert.assertFalse(JavaOptions.S3_ENDPOINT.getDefaultValue().isPresent());
	Assert.assertFalse(JavaOptions.NUMBER_OF_DATA_FOLDER_INDEX_SHARDS.getDefaultValue().isPresent());
    }
}
