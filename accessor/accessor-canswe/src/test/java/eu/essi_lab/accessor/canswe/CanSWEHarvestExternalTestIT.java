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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;

import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * Downloads the CanSWE v6 NetCDF file from Zenodo; harvests all the stations of the default measurement types (2, 3);
 * harvests the first page of stations of all the measurement types, maps the records and downloads the snow water
 * equivalent of the first station
 */
public class CanSWEHarvestExternalTestIT {

    @Test
    public void testDefaultMeasurementTypes() throws Exception {

	CanSWEConnector connector = new CanSWEConnector();
	connector.setSourceURL(CanSWEClient.DEFAULT_ENDPOINT);

	java.util.Set<String> stations = new java.util.HashSet<>();
	String token = null;
	do {
	    ListRecordsRequest request = new ListRecordsRequest();
	    request.setResumptionToken(token);
	    ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	    Iterator<OriginalMetadata> records = response.getRecords();
	    while (records.hasNext()) {
		org.json.JSONObject station = new org.json.JSONObject(records.next().getMetadata())
			.getJSONObject(CanSWEConnector.RECORD_STATION);
		long type = station.getLong("type_mes");
		assertTrue(type == 2 || type == 3);
		stations.add(station.getString(CanSWEConnector.RECORD_STATION_ID));
	    }
	    token = response.getResumptionToken();
	} while (token != null);

	System.out.println("Harvested stations of measurement types 2, 3: " + stations.size());
	// 149 snow pillows and 120 passive gamma sensors
	assertEquals(269, stations.size());
    }

    @Test
    public void testHarvestMapAndDownload() throws Exception {

	CanSWEClient client = new CanSWEClient(CanSWEClient.DEFAULT_ENDPOINT);
	assertEquals("https://zenodo.org/records/10835278/files/CanSWE-CanEEN_1928-2023_v6.nc?download=1", client.getFileURL());

	CanSWEDataset dataset = client.getDataset();
	assertEquals(2921, dataset.getStations().size());

	CanSWEConnector connector = new CanSWEConnector();
	connector.setSourceURL(CanSWEClient.DEFAULT_ENDPOINT);
	connector.getSetting().getOption(CanSWEConnectorSetting.CANSWE_MEASUREMENT_TYPES, String.class).get()
		.setValue(CanSWEConnectorSetting.ALL_MEASUREMENT_TYPES);

	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(new ListRecordsRequest());
	assertEquals(String.valueOf(CanSWEConnector.STATIONS_PER_PAGE), response.getResumptionToken());

	GSSource source = new GSSource();
	source.setEndpoint(CanSWEClient.DEFAULT_ENDPOINT);

	CanSWEMapper mapper = new CanSWEMapper();
	GSResource swe = null;
	int count = 0;

	Iterator<OriginalMetadata> records = response.getRecords();
	while (records.hasNext()) {
	    GSResource resource = mapper.map(records.next(), source);
	    if (resource.getHarmonizedMetadata().getCoreMetadata().getOnline().getName().equals("snw;ALE-05AA805")) {
		swe = resource;
		System.out.println("MAPPED " + resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
		System.out.println(resource.getHarmonizedMetadata().getCoreMetadata().getAbstract());
	    }
	    count++;
	}

	System.out.println("Records of the first page: " + count);
	assertTrue(count > CanSWEConnector.STATIONS_PER_PAGE);
	assertNotNull(swe);

	Online online = swe.getHarmonizedMetadata().getCoreMetadata().getOnline();
	// during harvesting the identifier is assigned by the identifier decorator
	online.setIdentifier();

	CanSWEDownloader downloader = new CanSWEDownloader();
	downloader.setOnlineResource(swe, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	assertNotNull(descriptor.getTemporalDimension());

	File file = downloader.download(descriptor);
	assertNotNull(file);

	String wml = Files.readString(file.toPath());
	// 1968-03-06: 155.0 kg m-2
	assertTrue(wml.contains("1968-03-06T00:00:00") && wml.contains(">155.0<"));
    }
}
