package eu.essi_lab.accessor.kisters;

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

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.essi_lab.accessor.kisters.KISTERSEntity.EntityType;
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
public class KISTERSMapperTest {

    private static final String SPW_ENDPOINT = "https://hydrometrie.wallonie.be/services/KiWIS/KiWIS";

    /**
     * @return the harvested SPW series, the time series of the group merged with its station
     */
    private KISTERSEntity createSPWSeries() {

	KISTERSEntity station = KISTERSClientTest.firstEntity(KISTERSClientTest.SPW_STATION_RESPONSE);
	KISTERSEntity series = KISTERSClientTest.firstEntity(KISTERSClientTest.SPW_GROUP_RESPONSE);

	series.setType(EntityType.TIME_SERIES);
	station.getObject().keySet().forEach(key -> {
	    if (!series.getObject().has(key)) {
		series.getObject().put(key, station.getObject().getString(key));
	    }
	});
	KISTERSConnector.resolveParameterLongName(station, series);

	return series;
    }

    /**
     * @param entity
     * @param endpoint
     * @return
     * @throws GSException
     */
    private GSResource map(KISTERSEntity entity, String endpoint) throws GSException {

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(entity.toString());
	originalMetadata.setSchemeURI(KISTERSMapper.KISTERS_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(endpoint);

	return new KISTERSMapper().map(originalMetadata, source);
    }

    @Test
    public void walloniaTest() throws GSException {

	GSResource resource = map(createSPWSeries(), SPW_ENDPOINT);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Seilles - Water Level (05-Hauteur.Complet)", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("Sources des données : Service public de Wallonie"));
	assertTrue(coreMetadata.getAbstract().contains("river Velaine"));

	assertEquals("Service public de Wallonie (SPW)",
		coreMetadata.getDataIdentification().getPointOfContact().getOrganisationName());
	assertTrue(coreMetadata.getDataIdentification().getLegalConstraints().next().getUseLimitation()
		.contains("Service public de Wallonie"));

	assertEquals("Belgium", resource.getExtensionHandler().getCountry().get());
	assertEquals("m", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Water Level", coreMetadata.getMIMetadata().getCoverageDescription().getAttributeTitle());

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2021-04-13T17:20:00.000+02:00", extent.getBeginPosition());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("356958010", online.getName());
	assertEquals(SPW_ENDPOINT, online.getLinkage());
	assertEquals(NetProtocolWrapper.KISTERS.getCommonURN(), online.getProtocol());

	assertEquals(50.5041390070031, coreMetadata.getBoundingBox().getNorth(), 0.000001);
	assertNotNull(resource.getOriginalId());
    }

    @Test
    public void unknownInstanceTest() throws GSException {

	// legacy per station mode of an instance without specific metadata (e.g. SEPA): same title as before
	KISTERSEntity series = createSPWSeries();
	series.getObject().remove(KISTERSClient.STATION_PARAM_LONG_NAME);

	GSResource resource = map(series, "https://timeseries.sepa.org.uk/KiWIS/KiWIS?datasource=0");
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Seilles - 05-Hauteur.Complet", coreMetadata.getTitle());
	assertEquals("Timeseries 05-Hauteur.Complet with data from station Seilles, river Velaine", coreMetadata.getAbstract());
	assertFalse(coreMetadata.getDataIdentification().getLegalConstraints().hasNext());
    }

    @Test
    public void instanceTest() {

	assertEquals(KISTERSInstance.FLANDERS_VMM,
		KISTERSInstance.fromEndpoint("https://download.waterinfo.be/tsmdownload/KiWIS/KiWIS?datasource=1").get());
	assertEquals(KISTERSInstance.FLANDERS_HIC, KISTERSInstance.fromEndpoint("https://hicws.vlaanderen.be/KiWIS/KiWIS").get());
	assertEquals(KISTERSInstance.WALLONIA_SPW, KISTERSInstance.fromEndpoint(SPW_ENDPOINT).get());
	assertFalse(KISTERSInstance.fromEndpoint("https://portal.grdc.bafg.de/KiWIS/KiWIS?").isPresent());
    }
}
