package eu.essi_lab.api.database.vol;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.Database.IdentifierType;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.model.resource.GSResource;

/**
 */
public class VolatileFolder implements DatabaseFolder {

    private String folderName;
    private HashMap<String, Document> documentsMap;
    private HashMap<String, ModifiedInputStream> streamsMap;

    /**
     * @author Fabrizio
     */
    private class ModifiedInputStream {

	private InputStream inputStream;
	private Date dateStamp;

	/**
	 * @param inputStream
	 */
	public ModifiedInputStream(InputStream inputStream) {

	    this.dateStamp = new Date();
	}

	/**
	 * @param inputStream
	 * @param dateStamp
	 */
	public ModifiedInputStream(InputStream inputStream, Date dateStamp) {

	    this.inputStream = inputStream;
	    this.dateStamp = new Date();
	}

	/**
	 * @return the inputStream
	 */
	public InputStream getInputStream() {

	    return inputStream;
	}

	/**
	 * @return the dateStamp
	 */
	public Date getDateStamp() {

	    return dateStamp;
	}

	@Override
	public boolean equals(Object object) {

	    if (object instanceof ModifiedInputStream) {

		ModifiedInputStream o = (ModifiedInputStream) object;
		return o.getDateStamp().equals(this.getDateStamp()) && o.getInputStream().equals(this.getInputStream());
	    }

	    return false;
	}

	@Override
	public int hashCode() {

	    return this.getDateStamp().toString().hashCode();
	}
    }

    /**
     * @param folderName
     */
    public VolatileFolder(String folderName) {

	this.folderName = folderName;
	this.documentsMap = new HashMap<>();
	this.streamsMap = new HashMap<>();
    }

    @Override
    public String getName() {

	return this.folderName;
    }

    @Override
    public boolean store(String key, FolderEntry entry, EntryType type) throws Exception {

	Optional<Document> document = entry.getDocument();

	if (document.isPresent()) {

	    Document put = documentsMap.put(key, document.get());
	    return put == null;
	}

	ModifiedInputStream put = streamsMap.put(key, new ModifiedInputStream(entry.getStream().get()));
	return put == null;
    }

    @Override
    public boolean replace(String key, FolderEntry entry, EntryType type) throws Exception {

	Optional<Document> document = entry.getDocument();

	if (document.isPresent()) {

	    Document put = documentsMap.put(key, document.get());
	    return put != null;
	}

	ModifiedInputStream put = streamsMap.put(key, new ModifiedInputStream(entry.getStream().get()));
	return put != null;
    }

    @Override
    public Node get(String key) throws Exception {

	return documentsMap.get(key);
    }

    @Override
    public InputStream getBinary(String key) throws Exception {

	return streamsMap.get(key).getInputStream();
    }

    @Override
    public boolean remove(String key) throws Exception {

	Document removed = documentsMap.remove(key);
	return removed != null;
    }

    @Override
    public boolean exists(String key) throws Exception {

	return documentsMap.containsKey(key);
    }

    @Override
    public String[] listKeys() throws Exception {

	List<String> asList = new ArrayList<String>(documentsMap.keySet());
	asList.addAll(new ArrayList<String>(streamsMap.keySet()));

	return asList.toArray(new String[] {});
    }

    @Override
    public int size() throws Exception {

	return listKeys().length;
    }

    @Override
    public void clear() throws Exception {

	documentsMap.clear();
	streamsMap.clear();
    }

    @Override
    public Database getDatabase() {

	return null;
    }

    @Override
    public Optional<GSResource> get(IdentifierType type, String identifier) {

	return Optional.empty();
    }
}
