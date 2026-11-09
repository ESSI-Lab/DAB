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
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.iso.datamodel.classes.Online;

/**
 * Downloads the CanSWE v6 NetCDF file from Zenodo, harvests the first page of sites, maps the records and downloads the
 * maximum and minimum snow water equivalent of two co-located stations
 */
public class CanSWEHarvestExternalTestIT {

    @Test
    public void testHarvestMapAndDownload() throws Exception {

	CanSWEClient client = new CanSWEClient(CanSWEClient.DEFAULT_ENDPOINT);
	assertEquals("https://zenodo.org/records/10835278/files/CanSWE-CanEEN_1928-2023_v6.nc?download=1", client.getFileURL());

	CanSWEDataset dataset = client.getDataset();
	assertEquals(2921, dataset.getStations().size());

	List<CanSWESite> sites = dataset.getSites();
	long colocated = sites.stream().filter(s -> s.getStations().size() > 1).count();
	System.out.println("Sites: " + sites.size() + ", co-located groups: " + colocated);
	assertTrue(colocated > 100);

	// two stations at the same location with values at the same times
	CanSWESite westCastle = dataset.getSite("ALE-05AA801,ALE-05AA805").get();
	assertEquals(CanSWESite.getStatistics(westCastle.aggregate(CanSWEDataset.SNW)).size(), 2);
	assertTrue(sites.stream().anyMatch(s -> s.getId().equals("ALE-05AA801,ALE-05AA805")));

	CanSWEConnector connector = new CanSWEConnector();
	connector.setSourceURL(CanSWEClient.DEFAULT_ENDPOINT);

	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(new ListRecordsRequest());
	assertEquals(String.valueOf(CanSWEConnector.SITES_PER_PAGE), response.getResumptionToken());

	GSSource source = new GSSource();
	source.setEndpoint(CanSWEClient.DEFAULT_ENDPOINT);

	CanSWEMapper mapper = new CanSWEMapper();
	GSResource maximum = null;
	GSResource minimum = null;
	int count = 0;

	Iterator<OriginalMetadata> records = response.getRecords();
	while (records.hasNext()) {
	    GSResource resource = mapper.map(records.next(), source);
	    String name = resource.getHarmonizedMetadata().getCoreMetadata().getOnline().getName();
	    if (name.equals("snw;MAXIMUM;ALE-05AA801,ALE-05AA805")) {
		maximum = resource;
		System.out.println("MAPPED " + resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
		System.out.println(resource.getHarmonizedMetadata().getCoreMetadata().getAbstract());
	    }
	    if (name.equals("snw;MINIMUM;ALE-05AA801,ALE-05AA805")) {
		minimum = resource;
	    }
	    count++;
	}

	System.out.println("Records of the first page: " + count);
	assertTrue(count > CanSWEConnector.SITES_PER_PAGE);
	assertNotNull(maximum);
	assertNotNull(minimum);

	String max = download(maximum);
	String min = download(minimum);

	// 1968-03-06: ALE-05AA805 155.0, ALE-05AA801 277.0
	assertTrue(max.contains("1968-03-06T00:00:00") && max.contains(">277.0<"));
	assertTrue(min.contains("1968-03-06T00:00:00") && min.contains(">155.0<"));
    }

    private String download(GSResource resource) throws Exception {

	Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();
	// during harvesting the identifier is assigned by the identifier decorator
	online.setIdentifier();

	CanSWEDownloader downloader = new CanSWEDownloader();
	downloader.setOnlineResource(resource, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	assertNotNull(descriptor.getTemporalDimension());

	File file = Optional.ofNullable(downloader.download(descriptor)).get();
	return Files.readString(file.toPath());
    }
}
