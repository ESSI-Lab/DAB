package eu.essi_lab.lib.whos;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author boldrini
 */
public class WIS2HydrologyTopicTest {

    private static final String CONCEPT = "http://hydro.geodab.eu/hydro-ontology/concept/";

    @Test
    public void decodeTest() {

	Assert.assertEquals(Optional.of(WIS2HydrologyTopic.STREAM), WIS2HydrologyTopic.decode(CONCEPT + "11"));
	Assert.assertEquals(Optional.of(WIS2HydrologyTopic.STREAM), WIS2HydrologyTopic.decode(CONCEPT + "76"));
	Assert.assertEquals(Optional.of(WIS2HydrologyTopic.STREAM), WIS2HydrologyTopic.decode(CONCEPT + "78"));
	Assert.assertEquals(Optional.of(WIS2HydrologyTopic.PRECIPITATION), WIS2HydrologyTopic.decode(CONCEPT + "65"));

	// observed properties without a topic in the official hierarchy
	Assert.assertTrue(WIS2HydrologyTopic.decode(CONCEPT + "49").isEmpty()); // air temperature
	Assert.assertTrue(WIS2HydrologyTopic.decode(CONCEPT + "51").isEmpty()); // water temperature
	Assert.assertTrue(WIS2HydrologyTopic.decode(CONCEPT + "110").isEmpty());

	// other ontologies, missing values
	Assert.assertTrue(WIS2HydrologyTopic.decode("http://his-central-ontology.geodab.eu/hydro-ontology/concept/11").isEmpty());
	Assert.assertTrue(WIS2HydrologyTopic.decode("").isEmpty());
	Assert.assertTrue(WIS2HydrologyTopic.decode(null).isEmpty());
    }

    @Test
    public void topicTest() {

	Assert.assertEquals(
		"origin/a/wis2/it-cnr-itiam-whos-dab/data/core/hydrology/surface-based-observations/water-quantity/stream",
		WIS2HydrologyTopic.STREAM.getTopic("it-cnr-itiam-whos-dab", WIS2DataPolicy.CORE));

	Assert.assertEquals(
		"origin/a/wis2/it-cnr-itiam-whos-dab/data/recommended/hydrology/surface-based-observations/atmospheric/precipitation",
		WIS2HydrologyTopic.PRECIPITATION.getTopic("it-cnr-itiam-whos-dab", WIS2DataPolicy.RECOMMENDED));

	Assert.assertEquals("origin/a/wis2/it-cnr-itiam-whos-dab/metadata", WIS2HydrologyTopic.getMetadataTopic("it-cnr-itiam-whos-dab"));
    }

    @Test
    public void experimentalAndPrefixTest() {

	String experimental = WIS2HydrologyTopic.STREAM.getExperimentalTopic("it-cnr-itiam-whos-dab", WIS2DataPolicy.CORE);

	Assert.assertEquals(
		"origin/a/wis2/it-cnr-itiam-whos-dab/data/core/hydrology/experimental/surface-based-observations/water-quantity/stream",
		experimental);

	Assert.assertEquals("it-cnr-itiam-whos-dab/data/core/hydrology/experimental/surface-based-observations/water-quantity/stream",
		WIS2HydrologyTopic.withoutPrefix(experimental));

	Assert.assertEquals("test/a/wis2/it-cnr-itiam-whos-dab/data/core/hydrology/surface-based-observations/water-quantity/stream",
		WIS2HydrologyTopic.withPrefix(WIS2HydrologyTopic.STREAM.getTopic("it-cnr-itiam-whos-dab", WIS2DataPolicy.CORE), "test/a/wis2"));
    }
}
