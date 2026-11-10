package eu.essi_lab.accessor.canswe;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class CanSWEMapperTest {

    @Test
    public void onlineName() {

	String name = CanSWEMapper.getOnlineName(CanSWEDataset.SND, "ALE-05AA805");
	String[] parts = CanSWEMapper.parseOnlineName(name).get();
	assertEquals(CanSWEDataset.SND, parts[0]);
	assertEquals("ALE-05AA805", parts[1]);

	assertFalse(CanSWEMapper.parseOnlineName("snw").isPresent());
	assertFalse(CanSWEMapper.parseOnlineName("snw;").isPresent());
	assertFalse(CanSWEMapper.parseOnlineName(null).isPresent());
    }

    @Test
    public void measurementCategory() {

	assertEquals("snow course", CanSWEMapper.getMeasurementCategory(0).get());
	assertEquals("manual", CanSWEMapper.getMeasurementCategory(1).get());
	assertEquals("automatic", CanSWEMapper.getMeasurementCategory(2).get());
	assertEquals("automatic", CanSWEMapper.getMeasurementCategory(3).get());
	assertEquals("automatic", CanSWEMapper.getMeasurementCategory(6).get());
	assertFalse(CanSWEMapper.getMeasurementCategory(-1).isPresent());
	assertFalse(CanSWEMapper.getMeasurementCategory(7).isPresent());
    }

    @Test
    public void measurementTypes() {

	assertEquals(new java.util.TreeSet<>(java.util.Arrays.asList(2l, 3l)), new CanSWEConnectorSetting().getMeasurementTypes());
	assertEquals(new java.util.TreeSet<>(java.util.Arrays.asList(0l, 2l)), CanSWEConnectorSetting.parseMeasurementTypes(" 2, 0 ,"));
	assertEquals(null, CanSWEConnectorSetting.parseMeasurementTypes("*"));
	assertEquals(null, CanSWEConnectorSetting.parseMeasurementTypes(""));
	try {
	    CanSWEConnectorSetting.parseMeasurementTypes("2,x");
	    org.junit.Assert.fail();
	} catch (IllegalArgumentException e) {
	}
    }

    @Test
    public void helpers() {

	assertEquals("kg m-2", CanSWEMapper.normalizeUnits("kg m**-2"));
	assertEquals("Snow water equivalent", CanSWEMapper.getVariableLabel(CanSWEDataset.SNW, "Water equivalent of snow cover"));

	assertFalse(CanSWEDataset.isValid(9.96921e+36f, 9.96921e+36f));
	assertFalse(CanSWEDataset.isValid(Float.NaN, Double.NaN));
	assertTrue(CanSWEDataset.isValid(0, 9.96921e+36f));

	assertEquals("1928-01-03T00:00:00Z",
		ISO8601DateTimeUtils.getISO8601DateTime(CanSWEDataset.toDate(2, "days since 1928-01-01 00:00:00").get()));

	assertTrue(CanSWEClient.isCanSWE("https://zenodo.org/records/10835278"));
	assertTrue(CanSWEClient.isCanSWE("https://zenodo.org/records/19075529/files/CanSWE-CanEEN_1928-2025_v8.nc?download=1"));
	assertFalse(CanSWEClient.isCanSWE("https://zenodo.org/records/123"));
    }
}
