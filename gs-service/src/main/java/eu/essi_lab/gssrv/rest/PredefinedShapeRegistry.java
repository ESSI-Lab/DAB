package eu.essi_lab.gssrv.rest;

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

import java.util.List;
import java.util.Optional;

import eu.essi_lab.accessor.opensearch.shape.OpenSearchShapefileClient;
import eu.essi_lab.api.database.opensearch.index.mappings.ShapeFileMapping;

/**
 * Facade for predefined shape upload registry stored in OpenSearch.
 */
public class PredefinedShapeRegistry {

    public static final String REGISTRY_ENTRY_NAME = ShapeFileMapping.UPLOAD_REGISTRY_ENTRY_NAME;

    private final OpenSearchShapefileClient client;

    /**
     * @throws Exception
     */
    public PredefinedShapeRegistry() throws Exception {

	this.client = new OpenSearchShapefileClient();
    }

    /**
     * @return upload records
     */
    public List<OpenSearchShapefileClient.UploadRecord> readUploads() throws Exception {

	return client.readUploadRegistry();
    }

    /**
     * @param prefix
     * @param fileName
     */
    public void registerUpload(String prefix, String fileName, String owner) throws Exception {

	client.registerUpload(prefix, fileName, owner);
    }

    /**
     * @param prefix upload identifier
     */
    public Optional<OpenSearchShapefileClient.UploadRecord> findUpload(String prefix) throws Exception {

	return client.findUpload(prefix);
    }

    /**
     * @param prefix
     */
    public void unregisterUpload(String prefix) throws Exception {

	client.unregisterUpload(prefix);
    }
}
