package eu.essi_lab.accessor.nonvemagasin;

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

import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
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
public class NVEMagasinMapperTest {

    @Test
    public void mapTest() throws GSException {

	NVEMagasinEntity series = NVEMagasinClientTest.createSeries("EL").get(0);
	// an end in the past, so that the end position is not indeterminate
	series.getObject().put(NVEMagasinClient.END, "2001-12-30T00:00:00Z");

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(NVEMagasinMapper.NVE_MAGASIN_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(NVEMagasinClient.DEFAULT_ENDPOINT);

	GSResource resource = new NVEMagasinMapper().map(originalMetadata, source);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Electricity price area NO1 (South-East Norway) - Reservoir filling ratio (fyllingsgrad)", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("Elspotområde 1"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("1995-01-08T00:00:00Z", extent.getBeginPosition());
	assertEquals("2001-12-30T00:00:00Z", extent.getEndPosition());

	GeographicBoundingBox box = coreMetadata.getBoundingBox();
	assertEquals(62.7, box.getNorth(), 0.001);
	assertEquals(8.0, box.getWest(), 0.001);
	assertEquals(58.8, box.getSouth(), 0.001);
	assertEquals(12.9, box.getEast(), 0.001);

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("EL|1|fyllingsgrad", online.getName());
	assertEquals(NetProtocolWrapper.NVE_MAGASIN.getCommonURN(), online.getProtocol());

	assertEquals("1", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Norway", resource.getExtensionHandler().getCountry().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.NVE_MAGASIN.getCommonURN() + ":fyllingsgrad",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
    }
}
