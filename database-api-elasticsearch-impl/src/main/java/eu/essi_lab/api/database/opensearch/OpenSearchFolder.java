/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.SourceStorageWorker;

/**
 * @author Fabrizio
 */
public class OpenSearchFolder implements DatabaseFolder {

    private String uri;
    private OpenSearchDatabase openSearchDb;

    /**
     * @param openSearchDb
     * @param uri
     */
    public OpenSearchFolder(OpenSearchDatabase openSearchDb, String uri) {

	this.openSearchDb = openSearchDb;
	this.uri = uri;
    }

    @Override
    public String getURI() {

	return uri;
    }

    @Override
    public String getCompleteName() {

	String name = new String(uri);
	if (uri.startsWith("/")) {
	    name = name.substring(1, uri.length());
	}

	if (uri.endsWith("/")) {
	    name = name.substring(0, uri.length() - 2);
	}

	return name;
    }

    @Override
    public String getSimpleName() {

	String simpleName = getCompleteName();
	simpleName = simpleName.replace(openSearchDb.getIdentifier() + "_", "");
	simpleName = simpleName.replace(SourceStorageWorker.META_PREFIX, "");
	simpleName = simpleName.replace(SourceStorageWorker.DATA_1_PREFIX, "");
	simpleName = simpleName.replace(SourceStorageWorker.DATA_2_PREFIX, "");

	return simpleName;
    }

    @Override
    public boolean store(String key, Document doc) throws Exception {

	return false;
    }

    @Override
    public boolean storeBinary(String key, InputStream res) throws Exception, UnsupportedOperationException {

	return false;
    }

    @Override
    public Node get(String key) throws Exception {

	return null;
    }

    @Override
    public InputStream getBinary(String key) throws Exception {

	return null;
    }

    @Override
    public boolean replace(String key, Document newDoc) throws Exception {

	return false;
    }

    @Override
    public boolean replaceBinary(String key, InputStream res) throws Exception, UnsupportedOperationException {

	return false;
    }

    @Override
    public boolean remove(String key) throws Exception {

	return false;
    }

    @Override
    public boolean exists(String key) throws Exception {

	return false;
    }

    @Override
    public String[] listKeys() throws Exception {

	return null;
    }

    @Override
    public int size() throws Exception {

	return 0;
    }

    @Override
    public void clear() throws Exception {

    }

}
