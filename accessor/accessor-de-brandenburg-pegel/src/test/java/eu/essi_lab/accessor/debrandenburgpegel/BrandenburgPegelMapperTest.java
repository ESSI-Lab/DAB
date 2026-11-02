package eu.essi_lab.accessor.debrandenburgpegel;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class BrandenburgPegelMapperTest {

    @Test
    public void mapTest() throws GSException {

	// 0491200|Q
	BrandenburgPegelEntity series = BrandenburgPegelClientTest.createSeries().get(1);

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(BrandenburgPegelMapper.BRANDENBURG_PEGEL_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(BrandenburgPegelClient.DEFAULT_ENDPOINT);

	GSResource resource = new BrandenburgPegelMapper().map(originalMetadata, source);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Prenzlau, Wehr UP - Discharge", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("Abfluss"));
	assertTrue(coreMetadata.getAbstract().contains("rolling window"));
	assertTrue(coreMetadata.getAbstract().contains("on Ucker, 63,30 km oberhalb der Mündung"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2025-10-26T23:00:00Z", extent.getBeginPosition());
	// recent end, still updated
	assertTrue(extent.isEndPositionIndeterminate());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("0491200|Q|1", online.getName());
	assertEquals(NetProtocolWrapper.BRANDENBURG_PEGEL.getCommonURN(), online.getProtocol());

	assertEquals("m3/s", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Germany", resource.getExtensionHandler().getCountry().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.BRANDENBURG_PEGEL.getCommonURN() + ":Q",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	assertTrue(coreMetadata.getDataIdentification().getLegalConstraintsUseLimitations().next().contains("dl-de/zero-2-0"));
    }
}
