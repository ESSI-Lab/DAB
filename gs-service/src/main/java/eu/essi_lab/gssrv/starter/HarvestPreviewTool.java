package eu.essi_lab.gssrv.starter;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.accessor.eurobis.ld.EurOBISLdConnector;
import eu.essi_lab.accessor.eurobis.ld.EurOBISLdMapper;
import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.IResourceMapper;
import jakarta.xml.bind.JAXBException;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Utility to preview one harmonized ISO record from different connectors.
 * <p>
 * Developers can switch the method called by {@link #main(String[])} to test another connector/mapper pair.
 */
public class HarvestPreviewTool {

    public class PreviewResult {

	private final String originalMetadata;
	private final GSResource mappedResource;

	public PreviewResult(String originalMetadata, GSResource mappedResource) {
	    this.originalMetadata = originalMetadata;
	    this.mappedResource = mappedResource;
	}

	public String getOriginalMetadata() {
	    return originalMetadata;
	}

	public String getHarmonizedIso() throws Exception {
	    String harmonizedIso = mappedResource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().asString(true);
	    return harmonizedIso;
	}

	public GSResource getMappedResource() {
	    return mappedResource;
	}

	public String getIndexesReport() {
	    StringBuilder builder = new StringBuilder();
	    builder.append("=== Index report ===\n");

	    List<String> properties = mappedResource.getIndexesMetadata().getProperties();
	    LinkedHashSet<String> uniqueProperties = new LinkedHashSet<>(properties);

	    for (String property : uniqueProperties) {
		List<String> values = mappedResource.getIndexesMetadata().read(property);
		if (values.isEmpty()) {
		    continue;
		}
		builder.append(property).append(" = ").append(values).append('\n');
	    }

	    return builder.toString();
	}
    }

    public PreviewResult getPreview(IHarvestedQueryConnector<?> connector, IResourceMapper mapper, String endpoint) throws Exception {

	String sourceId = "source-id";
	String sourceLabel = "sourceLabel";

	connector.setSourceURL(endpoint);
	connector.getSetting().setMaxRecords(1);
	connector.getSetting().setPageSize(1);

	ListRecordsRequest request = new ListRecordsRequest();
	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	List<OriginalMetadata> records = response != null ? response.getRecordsAsList() : List.of();

	if (records.isEmpty()) {
	    throw new IllegalStateException("No records returned by connector for endpoint: " + endpoint);
	}

	OriginalMetadata originalMetadata = records.get(0);
	String originalMetadataString = originalMetadata.getMetadata();

	GSSource source = new GSSource(sourceId);
	source.setEndpoint(endpoint);
	source.setLabel(sourceLabel);

	GSResource mapped = mapper.map(originalMetadata, source);

	IndexedElementsWriter.write(mapped);

	return new PreviewResult(originalMetadataString, mapped);
    }


    public static void main(String[] args) throws Exception {

	HarvestPreviewTool tool = new HarvestPreviewTool();

	PreviewResult result = tool.getPreview(new EurOBISLdConnector(), new EurOBISLdMapper(),
		"https://marineinfo.org/id/collection/619.ttl");

	//		PreviewResult result = tool.previewFirstRecord(new CDIConnector(), new CDIMapper(),
	//			"https://cdi.seadatanet.org/report/aggregation/open");

	System.out.println(result.getOriginalMetadata());
	System.out.println(result.getHarmonizedIso());
	System.out.println(result.getIndexesReport());
    }
}
