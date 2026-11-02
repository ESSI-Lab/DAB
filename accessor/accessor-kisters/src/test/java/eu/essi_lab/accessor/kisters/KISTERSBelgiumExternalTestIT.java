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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.iso.datamodel.classes.Online;

/**
 * Live tests of the KiWIS instances of Flanders (VMM and HIC, published by waterinfo.be) and Wallonia (SPW), harvested by time
 * series group, and of SEPA, harvested by station (the legacy mode). Anonymous calls are limited in volume: only a few requests
 * are made
 * 
 * @author boldrini
 */
public class KISTERSBelgiumExternalTestIT {

    static final String VMM_ENDPOINT = "https://download.waterinfo.be/tsmdownload/KiWIS/KiWIS?datasource=1";
    static final String VMM_GROUPS = "192780,192782,192786,192893,325066";

    static final String HIC_ENDPOINT = "https://hicws.vlaanderen.be/KiWIS/KiWIS";
    static final String HIC_GROUPS = "04156163,04156162,04156170,04156169";

    static final String SPW_ENDPOINT = "https://hydrometrie.wallonie.be/services/KiWIS/KiWIS";
    static final String SPW_GROUPS = "7255523,7255151,7256917,7256919,7256921,7256922";

    static final String SEPA_ENDPOINT = "https://timeseries.sepa.org.uk/KiWIS/KiWIS?datasource=0";

    @Test
    public void walloniaTest() throws Exception {

	List<OriginalMetadata> records = harvest(SPW_ENDPOINT, SPW_GROUPS, null, false);
	assertTrue(records.size() > 1000);

	checkRecordAndDownload(SPW_ENDPOINT, records.get(0), "Service public de Wallonie");
    }

    @Test
    public void flandersHICTest() throws Exception {

	List<OriginalMetadata> records = harvest(HIC_ENDPOINT, HIC_GROUPS, null, false);
	assertTrue(records.size() > 400);

	checkRecordAndDownload(HIC_ENDPOINT, records.get(0), "waterinfo.be");
    }

    @Test
    public void flandersVMMTest() throws Exception {

	List<OriginalMetadata> records = harvest(VMM_ENDPOINT, VMM_GROUPS, null, false);
	assertTrue(records.size() > 1500);

	// the most recent series, likely real time
	OriginalMetadata latest = records.stream().//
		max((r1, r2) -> new KISTERSEntity(r1.getMetadata()).getObject().getString(KISTERSClient.TS_TO).compareTo(
			new KISTERSEntity(r2.getMetadata()).getObject().getString(KISTERSClient.TS_TO)))
		.get();

	checkRecordAndDownload(VMM_ENDPOINT, latest, "waterinfo.be");
    }

    @Test
    public void sepaStationModeTest() throws Exception {

	// first station only
	List<OriginalMetadata> records = harvest(SEPA_ENDPOINT, "", null, true);
	assertFalse(records.isEmpty());

	GSSource source = new GSSource();
	source.setEndpoint(SEPA_ENDPOINT);
	GSResource resource = new KISTERSMapper().map(records.get(0), source);
	System.out.println("SEPA title: " + resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	System.out.println("SEPA abstract: " + resource.getHarmonizedMetadata().getCoreMetadata().getAbstract());
    }

    /**
     * @param endpoint
     * @param groups
     * @param regex
     * @param firstPageOnly
     * @return
     * @throws Exception
     */
    private List<OriginalMetadata> harvest(String endpoint, String groups, String regex, boolean firstPageOnly) throws Exception {

	KISTERSConnector connector = new KISTERSConnector();
	connector.setSourceURL(endpoint);
	connector.getSetting().setTimeSeriesGroupIds(groups);
	if (regex != null) {
	    connector.getSetting().setTimeSeriesNameRegex(regex);
	}

	List<OriginalMetadata> records = new ArrayList<>();

	ListRecordsRequest request = new ListRecordsRequest();
	int requests = 0;

	do {
	    ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	    records.addAll(response.getRecordsAsList());
	    request.setResumptionToken(response.getResumptionToken());
	    requests++;

	} while (request.getResumptionToken() != null && !firstPageOnly);

	System.out.println(endpoint + ": " + records.size() + " records in " + requests + " pages");

	return records;
    }

    /**
     * @param endpoint
     * @param record
     * @param attribution
     * @throws Exception
     */
    private void checkRecordAndDownload(String endpoint, OriginalMetadata record, String attribution) throws Exception {

	GSSource source = new GSSource();
	source.setEndpoint(endpoint);

	GSResource resource = new KISTERSMapper().map(record, source);
	System.out.println("Title: " + resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	System.out.println("Abstract: " + resource.getHarmonizedMetadata().getCoreMetadata().getAbstract());
	System.out.println("Units: " + resource.getExtensionHandler().getAttributeUnitsAbbreviation().orElse(""));

	assertTrue(resource.getHarmonizedMetadata().getCoreMetadata().getAbstract().contains(attribution));

	Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();
	online.setIdentifier();

	KISTERSDownloader downloader = new KISTERSDownloader();
	downloader.setOnlineResource(resource, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	ContinueDimension dimension = descriptor.getTemporalDimension().getContinueDimension();
	long end = dimension.getUpper().longValue();
	System.out.println("Extent: " + new Date(dimension.getLower().longValue()) + " / " + new Date(end));

	descriptor.setTemporalDimension(new Date(end - TimeUnit.DAYS.toMillis(7)), new Date(end));

	File file = downloader.download(descriptor);
	assertNotNull(file);

	String wml = Files.readString(file.toPath());
	System.out.println("WML size (7 days): " + wml.length());
	assertTrue(wml.contains(":value"));
    }
}
