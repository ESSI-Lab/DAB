package eu.essi_lab.accessor.rarcticnet;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class RArcticNetMapperTest {

    @Test
    public void monthlyTest() throws Exception {

	GSResource resource = map(RArcticNetClientTest.createMonthlySeries());
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Kolyma At Orotuk - Discharge (monthly mean)", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("PointID 5855"));
	assertTrue(coreMetadata.getAbstract().contains("State Hydrological Institute (SHI) code 1001"));
	assertTrue(coreMetadata.getAbstract().contains("drainage area 43600 km2"));
	assertTrue(coreMetadata.getAbstract().contains("from 1957-01-01 to 1997-04-01"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("1957-01-01T00:00:00Z", extent.getBeginPosition());
	assertEquals("1997-04-01T00:00:00Z", extent.getEndPosition());
	assertFalse(extent.isEndPositionIndeterminate());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("5855|MONTHLY", online.getName());
	assertEquals(NetProtocolWrapper.R_ARCTICNET.getCommonURN(), online.getProtocol());

	assertEquals("m3/s", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("P1M", resource.getExtensionHandler().getTimeAggregationDuration8601().get());
	assertEquals("P1M", resource.getExtensionHandler().getTimeResolutionDuration8601().get());
	// the country is not implied by the source agency
	assertFalse(resource.getExtensionHandler().getCountry().isPresent());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.R_ARCTICNET.getCommonURN() + ":Discharge",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	assertNotNull(coreMetadata.getBoundingBox());
    }

    @Test
    public void dailyTest() throws Exception {

	RArcticNetEntity series = RArcticNetClientTest.createDailySeries();
	GSResource resource = map(series);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Kolyma At Orotuk - Discharge (daily mean)", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("UCLA/UNH"));
	assertEquals("5855|DAILY", coreMetadata.getOnline().getName());
	assertEquals("P1D", resource.getExtensionHandler().getTimeAggregationDuration8601().get());

	// different records for the monthly and the daily series of the same gauge
	GSResource monthly = map(RArcticNetClientTest.createMonthlySeries());
	assertFalse(monthly.getOriginalId().get().equals(resource.getOriginalId().get()));
	assertEquals(monthly.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getParentIdentifier(),
		coreMetadata.getMIMetadata().getParentIdentifier());
    }

    @Test
    public void sourceAgencyTest() {

	assertEquals("Environment Canada (HYDAT)", RArcticNetMapper.getSourceAgency("Hydat"));
	assertEquals("Canada", RArcticNetMapper.getCountry("Hydat"));
	assertEquals("United States", RArcticNetMapper.getCountry("USGS"));
	assertEquals(null, RArcticNetMapper.getCountry("SHI"));
	assertEquals("", RArcticNetMapper.getSourceAgency(""));
    }

    /**
     * @param series
     * @return
     * @throws Exception
     */
    private GSResource map(RArcticNetEntity series) throws Exception {

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(RArcticNetMapper.R_ARCTICNET_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(RArcticNetClient.DEFAULT_ENDPOINT);

	return new RArcticNetMapper().map(originalMetadata, source);
    }
}
