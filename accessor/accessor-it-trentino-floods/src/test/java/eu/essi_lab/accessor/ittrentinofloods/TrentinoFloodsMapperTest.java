package eu.essi_lab.accessor.ittrentinofloods;

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
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class TrentinoFloodsMapperTest {

    @Test
    public void mapTest() throws GSException {

	TrentinoFloodsEntity series = TrentinoFloodsClientTest.createSeries().get(2);
	series.getObject().put(TrentinoFloodsClient.BEGIN, "2024-01-03T23:00:00Z");
	series.getObject().put(TrentinoFloodsClient.END, "2024-01-10T23:00:00Z");

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(TrentinoFloodsMapper.TRENTINO_FLOODS_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(TrentinoFloodsClient.DEFAULT_ENDPOINT);

	GSResource resource = new TrentinoFloodsMapper().map(originalMetadata, source);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Trento-Fersina - Discharge", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("Portata (calcolata)"));
	assertTrue(coreMetadata.getAbstract().contains("torr. Fersina"));
	assertTrue(coreMetadata.getAbstract().contains("last 7 days"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2024-01-03T23:00:00Z", extent.getBeginPosition());
	assertEquals("2024-01-10T23:00:00Z", extent.getEndPosition());
	// not updated since a long time
	assertFalse(extent.isEndPositionIndeterminate());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("2|12", online.getName());
	assertEquals(NetProtocolWrapper.TRENTINO_FLOODS.getCommonURN(), online.getProtocol());

	assertEquals("m3/s", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Italy", resource.getExtensionHandler().getCountry().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.TRENTINO_FLOODS.getCommonURN() + ":DISCHARGE",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());

	// level and discharge of the same station share the parent identifier
	TrentinoFloodsEntity level = TrentinoFloodsClientTest.createSeries().get(0);
	originalMetadata.setMetadata(level.toString());
	GSResource levelResource = new TrentinoFloodsMapper().map(originalMetadata, source);
	assertEquals(coreMetadata.getMIMetadata().getParentIdentifier(),
		levelResource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getParentIdentifier());
	assertEquals("Trento-Fersina - Water level", levelResource.getHarmonizedMetadata().getCoreMetadata().getTitle());
    }
}
