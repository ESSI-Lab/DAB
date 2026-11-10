package eu.essi_lab.accessor.esaca;

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
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class ACAMapperTest {

    @Test
    public void mapTest() throws GSException {

	ACAEntity series = ACAClientTest.createDischargeSeries();
	// an old series, to have a determinate end position
	series.getObject().put(ACAClient.BEGIN, "2001-07-27T15:00:00Z");
	series.getObject().put(ACAClient.END, "2001-10-27T14:15:00Z");

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(ACAMapper.ACA_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(ACAClient.DEFAULT_ENDPOINT);

	GSResource resource = new ACAMapper().map(originalMetadata, source);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Arenys de Munt (riera d'Arenys) - River discharge (Cabal riu)", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("river discharge (Cabal riu, m³/s)"));
	assertTrue(coreMetadata.getAbstract().contains("river gauging station"));
	assertTrue(coreMetadata.getAbstract().contains("RIERA ARENYS"));
	assertTrue(coreMetadata.getAbstract().contains("rolling window"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2001-07-27T15:00:00Z", extent.getBeginPosition());
	assertEquals("2001-10-27T14:15:00Z", extent.getEndPosition());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("AFORAMENT-EST|CALC001658", online.getName());
	assertEquals(NetProtocolWrapper.ACA.getCommonURN(), online.getProtocol());

	assertEquals("m3/s", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("cubic metres per second", resource.getExtensionHandler().getAttributeUnits().get());
	assertEquals("Spain", resource.getExtensionHandler().getCountry().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.ACA.getCommonURN() + ":RIVER_DISCHARGE",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	assertEquals(NetProtocolWrapper.ACA.getCommonURN() + "/080060-001",
		coreMetadata.getMIMetadata().getMIPlatform().getMDIdentifierCode());
    }

    @Test
    public void realTimeTest() throws GSException {

	ACAEntity series = ACAClientTest.createDischargeSeries();
	series.getObject().put(ACAClient.END, ISO8601DateTimeUtils.getISO8601DateTime());

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(ACAMapper.ACA_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(ACAClient.DEFAULT_ENDPOINT);

	GSResource resource = new ACAMapper().map(originalMetadata, source);

	assertTrue(resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent().isEndPositionIndeterminate());
    }

    @Test
    public void unitTest() {

	assertEquals("hm3", ACAMapper.getUnitAbbreviation("hm³"));
	assertEquals("metres above sea level", ACAMapper.getUnitName("msnm"));
	assertEquals("°C", ACAMapper.getUnitAbbreviation("ºC"));
	assertEquals("cm", ACAMapper.getUnitAbbreviation("cm"));
    }
}
