package eu.essi_lab.cdk.harvest;

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

import eu.essi_lab.cfga.gs.setting.accessor.*;
import eu.essi_lab.cfga.gs.setting.harvesting.*;
import eu.essi_lab.indexes.*;
import eu.essi_lab.messages.listrecords.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.resource.*;
import eu.essi_lab.ommdk.*;
import jakarta.xml.bind.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

/**
 * Utility to preview one harmonized ISO record from different connectors
 *
 * @author boldrini
 */
public class HarvestPreviewTool {

    /**
     *
     */
    public static class Preview {

	private final String originalMetadata;
	private final GSResource mappedResource;

	/**
	 * @param originalMetadata
	 * @param mappedResource
	 */
	private Preview(String originalMetadata, GSResource mappedResource) {

	    this.originalMetadata = originalMetadata;
	    this.mappedResource = mappedResource;
	}

	/**
	 * @return
	 */
	public String getOriginalMetadata() {

	    return originalMetadata;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String getHarmonizedMetadata() {

	    try {
		return mappedResource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().asString(true);
	    } catch (Exception e) {

		throw new RuntimeException(e);
	    }
	}

	/**
	 * @return
	 */
	public GSResource getMappedResource() {

	    return mappedResource;
	}

	/**
	 * @return
	 */
	public String getMappedResourceString() {

	    try {
		return mappedResource.asString(false);
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	}

	/**
	 * @return
	 */
	public String getIndexesReport() {

	    return mappedResource.getIndexesMetadata().//
		    getProperties().//
		    stream().//
		    distinct().//
		    filter(p -> !mappedResource.getIndexesMetadata().read(p).isEmpty()).//
		    map(p -> p + " = " + mappedResource.getIndexesMetadata().read(p)).//
		    collect(Collectors.joining("\n"));//
	}
    }

    /**
     * @param setting
     * @return
     * @throws Exception
     */
    public static Preview get(HarvestingSetting setting) throws Exception {

	AccessorSetting accSetting = setting.getSelectedAccessorSetting();

	IHarvestedQueryConnector<?> connector = accSetting.getHarvestedConnectorSetting().createConfigurable();

	String endpoint = accSetting.getSource().getEndpoint();

	return get(connector,  //
		endpoint, //
		accSetting.getGSSourceSetting().getSourceIdentifier(), //
		accSetting.getGSSourceSetting().getSourceLabel());
    }

    /**
     * @param connector
     * @param endpoint
     * @return
     * @throws Exception
     */
    public static Preview get(IHarvestedQueryConnector<?> connector, String endpoint) throws Exception {

	return get(connector, endpoint, "sourceId", "sourceLabel");
    }

    /**
     * @param connector
     * @param endpoint
     * @return
     * @throws Exception
     */
    private static Preview get(//
	    IHarvestedQueryConnector<?> connector, //
	    String endpoint, //
	    String sourceId, //
	    String sourceLabel)//
	    throws Exception {

	if (!connector.supportsPreview()) {

	    throw new UnsupportedOperationException("Preview not supported by the given connector");
	}

	connector.setSourceURL(endpoint);
	connector.getSetting().setMaxRecords(1);
	connector.getSetting().setPageSize(1);

	ListRecordsRequest request = new ListRecordsRequest();
	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	List<OriginalMetadata> records = response != null ? response.getRecordsAsList() : List.of();

	if (records.isEmpty()) {

	    throw new IllegalStateException("No records found");
	}

	OriginalMetadata originalMetadata = records.getFirst();
	String originalMetadataString = originalMetadata.getMetadata();

	GSSource source = new GSSource(sourceId);
	source.setEndpoint(endpoint);
	source.setLabel(sourceLabel);

	IResourceMapper mapper = ResourceMapperFactory.getResourceMapper(originalMetadata.getSchemeURI());

	GSResource mapped = mapper.map(originalMetadata, source);

	IndexedElementsWriter.write(mapped);

	return new Preview(originalMetadataString, mapped);
    }

}
