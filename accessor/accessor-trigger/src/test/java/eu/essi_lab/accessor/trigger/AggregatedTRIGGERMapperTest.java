package eu.essi_lab.accessor.trigger;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

/**
 * Verifies that {@link AggregatedTRIGGERMapper} maps real <code>*_daily</code> API payloads using the actual
 * JSON field names (e.g. <code>pm1_mean</code>, <code>heartrate_mean</code>) rather than the enum constant
 * names, and that records with a missing/null value for the requested variable are not mapped.
 */
public class AggregatedTRIGGERMapperTest {

    private AggregatedTRIGGERMapper mapper = new AggregatedTRIGGERMapper();

    private JSONObject readFixture(String name) throws Exception {

	InputStream stream = AggregatedTRIGGERMapperTest.class.getClassLoader().getResourceAsStream(name);
	TestCase.assertNotNull(stream);

	JSONObject object = new JSONObject(IOStreamUtils.asUTF8String(stream));
	stream.close();
	return object;
    }

    private List<TRIGGERTimePosition> positions() {

	return Arrays.asList(//
		new TRIGGERTimePosition(11.35, 44.49, LocalDateTime.of(2025, 10, 16, 0, 0, 0)), //
		new TRIGGERTimePosition(11.36, 44.50, LocalDateTime.of(2025, 10, 17, 0, 0, 0)));
    }

    @Test
    public void testMyairVariable() throws Exception {

	JSONObject object = readFixture("aggregatedTriggerMyairDaily.json");

	OriginalMetadata originalMD = AggregatedTRIGGERMapper.create(//
		object, //
		AggregatedTRIGGERConnector.AGGREGATED_TRIGGER_VARIABLES.PM1MEAN.name(), //
		"myair_daily?", //
		positions());

	Dataset dataset = (Dataset) mapper.execMapping(originalMD, new GSSource());

	CoreMetadata core = dataset.getHarmonizedMetadata().getCoreMetadata();

	TestCase.assertNotNull(core.getTitle());
	TestCase.assertTrue(core.getTitle().contains("124"));

	GeographicBoundingBox bbox = core.getBoundingBox();
	TestCase.assertNotNull(bbox);
	TestCase.assertEquals(44.50, bbox.getNorth());
	TestCase.assertEquals(44.49, bbox.getSouth());

	TemporalExtent extent = core.getMIMetadata().getDataIdentification().getTemporalExtent();
	TestCase.assertNotNull(extent);

	String linkage = core.getMIMetadata().getDistribution().getDistributionOnline().getLinkage();
	TestCase.assertEquals(AggregatedTRIGGERConnector.BASE_URL + "myair_daily?where=userId=124", linkage);
    }

    @Test
    public void testSmartwatchlowVariable() throws Exception {

	JSONObject object = readFixture("aggregatedTriggerSmartwatchlowDaily.json");

	OriginalMetadata originalMD = AggregatedTRIGGERMapper.create(//
		object, //
		AggregatedTRIGGERConnector.AGGREGATED_TRIGGER_VARIABLES.BPHIGHMEAN.name(), //
		"smartwatchlow_daily?", //
		positions());

	Dataset dataset = (Dataset) mapper.execMapping(originalMD, new GSSource());

	CoreMetadata core = dataset.getHarmonizedMetadata().getCoreMetadata();

	TestCase.assertNotNull(core.getTitle());
	TestCase.assertTrue(core.getTitle().contains("173"));

	String linkage = core.getMIMetadata().getDistribution().getDistributionOnline().getLinkage();
	TestCase.assertEquals(AggregatedTRIGGERConnector.BASE_URL + "smartwatchlow_daily?where=userId=173", linkage);
    }

    @Test
    public void testSmartwatchhighVariable() throws Exception {

	JSONObject object = readFixture("aggregatedTriggerSmartwatchhighDaily.json");

	OriginalMetadata originalMD = AggregatedTRIGGERMapper.create(//
		object, //
		AggregatedTRIGGERConnector.AGGREGATED_TRIGGER_VARIABLES.HEARTRATEMEAN.name(), //
		"smartwatchhigh_daily?", //
		positions());

	Dataset dataset = (Dataset) mapper.execMapping(originalMD, new GSSource());

	CoreMetadata core = dataset.getHarmonizedMetadata().getCoreMetadata();

	TestCase.assertNotNull(core.getTitle());
	TestCase.assertTrue(core.getTitle().contains("Heart Rate"));
    }

    /**
     * <code>oxygens_mean/_min/_max</code> are <code>null</code> in the fixture (no oxygen readings that day):
     * the mapper must skip building core metadata for that variable instead of mapping a null value.
     */
    @Test
    public void testNullVariableIsSkipped() throws Exception {

	JSONObject object = readFixture("aggregatedTriggerSmartwatchhighDaily.json");

	OriginalMetadata originalMD = AggregatedTRIGGERMapper.create(//
		object, //
		AggregatedTRIGGERConnector.AGGREGATED_TRIGGER_VARIABLES.OXYGENSMEAN.name(), //
		"smartwatchhigh_daily?", //
		positions());

	Dataset dataset = (Dataset) mapper.execMapping(originalMD, new GSSource());

	CoreMetadata core = dataset.getHarmonizedMetadata().getCoreMetadata();

	TestCase.assertNull(core.getTitle());
    }
}
