package eu.essi_lab.accessor.gcw;

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

import java.io.File;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * Lists the OPeNDAP records of the GCW catalogue, then harvests, maps and downloads the last 30 days of one series of
 * each family of time series (MET SYNOP stations, MET permafrost profiles, MET AWS, GEUS PROMICE)
 */
public class GCWHarvestExternalTestIT {

    private static final long DAY = 24 * 60 * 60 * 1000l;

    private static List<JSONObject> records;

    @BeforeClass
    public static void listRecords() throws Exception {

	records = new GCWClient(GCWClient.DEFAULT_ENDPOINT).listOPeNDAPRecords();
	System.out.println("OPeNDAP records: " + records.size());

	Set<String> ids = new HashSet<>();
	for (JSONObject record : records) {
	    assertTrue(GCWClient.getOPeNDAPURL(record).isPresent());
	    assertTrue(ids.add(record.getString(GCWClient.ID)));
	}
	assertTrue(records.size() > 450);
    }

    @Test
    public void testSynop() throws Exception {

	check("/arcticdata/obsSynop/", "TA");
    }

    @Test
    public void testPermafrost() throws Exception {

	check("/frost2netcdf-permafrost/", "soil_temperature[0]");
    }

    @Test
    public void testAWS() throws Exception {

	check("/arcticdata/infranor/", null);
    }

    @Test
    public void testGEUS() throws Exception {

	check("thredds.geus.dk", "t_u");
    }

    /**
     * Harvests the first record whose OPeNDAP URL contains the given string, maps it and downloads the last 30 days
     * of the given series (of the first series if null)
     */
    private void check(String urlPart, String seriesName) throws Exception {

	int index = -1;
	for (int i = 0; i < records.size(); i++) {
	    if (records.get(i).getString(GCWClient.OPENDAP).contains(urlPart)) {
		index = i;
		break;
	    }
	}
	assertTrue("No record of " + urlPart, index >= 0);

	GCWConnector connector = new GCWConnector();
	connector.setSourceURL(GCWClient.DEFAULT_ENDPOINT);

	ListRecordsRequest request = new ListRecordsRequest();
	request.setResumptionToken(String.valueOf(index));
	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	assertEquals(String.valueOf(index + 1), response.getResumptionToken());

	GSSource source = new GSSource();
	source.setEndpoint(GCWClient.DEFAULT_ENDPOINT);

	GCWMapper mapper = new GCWMapper();
	GSResource selected = null;
	int count = 0;

	Iterator<OriginalMetadata> iterator = response.getRecords();
	while (iterator.hasNext()) {
	    GSResource resource = mapper.map(iterator.next(), source);
	    String name = resource.getHarmonizedMetadata().getCoreMetadata().getOnline().getName();
	    System.out.println("MAPPED " + name + ": " + resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	    if (selected == null && (seriesName == null || name.equals(seriesName))) {
		selected = resource;
	    }
	    count++;
	}

	assertTrue(count > 0);
	assertNotNull("Series " + seriesName + " not harvested", selected);

	Online online = selected.getHarmonizedMetadata().getCoreMetadata().getOnline();
	// during harvesting the identifier is assigned by the identifier decorator
	online.setIdentifier();

	GCWDownloader downloader = new GCWDownloader();
	downloader.setOnlineResource(selected, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	assertNotNull(descriptor.getTemporalDimension());

	long end = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	descriptor.setTemporalDimension(new Date(end - 30 * DAY), new Date(end));

	File file = downloader.download(descriptor);
	assertNotNull(file);

	String wml = Files.readString(file.toPath());
	assertFalse(wml.isEmpty());
	assertTrue(wml.contains(":value"));
	System.out.println(wml.substring(wml.indexOf(":value") - 4, Math.min(wml.length(), wml.indexOf(":value") + 300)));
    }
}
