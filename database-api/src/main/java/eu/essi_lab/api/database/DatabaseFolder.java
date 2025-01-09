package eu.essi_lab.api.database;

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
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Fabrizio
 */
public interface DatabaseFolder {

    /**
     * @author Fabrizio
     */
    public static class FolderEntry {

	private Document document;
	private InputStream stream;

	/**
	 * @param document
	 */
	private FolderEntry(Document document) {

	    this.document = document;
	}

	/**
	 * @param stream
	 */
	private FolderEntry(InputStream stream) {

	    this.stream = stream;
	}

	/**
	 * @return
	 */
	public Optional<InputStream> getInputStream() {

	    return Optional.ofNullable(stream);
	}

	/**
	 * @return
	 */
	public Optional<Document> getDocument() {

	    return Optional.ofNullable(document);
	}

	/**
	 * @param document
	 * @return
	 */
	public static FolderEntry of(Document document) {

	    return new FolderEntry(document);
	}

	/**
	 * @param stream
	 * @return
	 */
	public static FolderEntry of(InputStream stream) {

	    return new FolderEntry(stream);
	}
    }

    /**
     * @author Fabrizio
     */
    public enum EntryType {

	USER("user"), //
	VIEW("view"), //
	META_FOLDER_ENTRY("metaFolderItem"), //
	DATA_FOLDER_ENTRY("dataFolderItem"), //
	AUGMENTER_PROPERTIES("augmenterProperties"), //
	CONFIGURATION("configuration"), //
	MISC("misc");

	private String type;

	/**
	 * @param type
	 */
	private EntryType(String type) {

	    this.type = type;
	}

	/**
	 * @return the type
	 */
	public String getType() {

	    return type;
	}

	/**
	 * @param type
	 * @return
	 * @throws NoSuchElementException
	 */
	public EntryType decode(String type) throws NoSuchElementException {

	    return Arrays.asList(EntryType.values()).//
		    stream().//
		    filter(t -> t.getType().equals(type)).//
		    findFirst().//
		    orElseThrow();
	}
    }

    /**
     * If this folder is a DAB source folder, this method returns the related source identifier by removing from this
     * folder name the database identifier and the other folder prefixes used to generate the folder name.<br>
     * If this folder is not a DAB source folder, it returns this folder name.
     * 
     * @param database
     * @param folder
     * @return
     */
    public static String computeSourceIdentifier(Database database, DatabaseFolder folder) {

	String name = folder.getName();
	name = name.replace(database.getIdentifier() + "_", "");
	name = name.replace(SourceStorageWorker.META_PREFIX, "");
	name = name.replace(SourceStorageWorker.DATA_1_PREFIX, "");
	name = name.replace(SourceStorageWorker.DATA_2_PREFIX, "");

	return name;
    }

    /**
     * Returns the name of this folder
     */
    String getName();

    /**
     * Stores a DOM resource with the specified <code>key</code> in this folder.<br>
     * Key should not contain slashes.<br>
     * Use {@link #exists}
     * to test whether a resource with the specified <code>key</code> already exists
     *
     * @param key the key of the resource
     * @param res the resource to be stored
     * @return false if the key already exists, true if the store was performed with success
     * @throws Exception if the key is already used or problems occur
     */
    default boolean store(String key, FolderEntry entry) throws Exception {

	return store(key, entry, EntryType.MISC);
    }

    /**
     * Stores a DOM resource with the specified <code>key</code> in this folder.<br>
     * Key should not contain slashes.<br>
     * Use {@link #exists}
     * to test whether a resource with the specified <code>key</code> already exists
     *
     * @param key the key of the resource
     * @param res the resource to be stored
     * @return false if the key already exists, true if the store was performed with success
     * @throws Exception if the key is already used or problems occur
     */
    boolean store(String key, FolderEntry entry, EntryType type) throws Exception;

    /**
     * Replace with <code>newDoc</code> the content of the XML resource with the specified <code>key</code>.<br>
     * Key should not contain
     * slashes.<br>
     * Use {@link #exists} to test whether a resource with the specified <code>key</code> exists
     *
     * @return true if the replacement was made, false if the resource does not exist (no insert is done in this case)
     * @throws Exception if problems occur
     */
    default boolean replace(String key, FolderEntry entry) throws Exception {

	return replace(key, entry, EntryType.MISC);
    }

    /**
     * Replace with <code>newDoc</code> the content of the XML resource with the specified <code>key</code>.<br>
     * Key should not contain
     * slashes.<br>
     * Use {@link #exists} to test whether a resource with the specified <code>key</code> exists
     *
     * @return true if the replacement was made, false if the resource does not exist (no insert is done in this case)
     * @throws Exception if problems occur
     */
    boolean replace(String key, FolderEntry entry, EntryType type) throws Exception;

    /**
     * Returns the DOM resource with the specified <code>key</code>.<br>
     * Key should not contain slashes.<br>
     * Use {@link #exists} to test
     * whether a resource with the specified <code>key</code> exists
     *
     * @param key the key of the resource
     * @return the resource with the specified key
     * @throws Exception if the resource does not exist or problems occur
     */
    Node get(String key) throws Exception;

    /**
     * Returns the binary resource with the specified <code>key</code> <i>(optional operation)</i>.<br>
     * Key should not contain slashes.<br>
     * Use {@link #exists} to test whether a resource with the specified <code>key</code> exists
     *
     * @param key the key of the resource.
     * @return the resource as a {@link InputStream}
     * @throws Exception if the resource does not exist or problems occur
     */
    InputStream getBinary(String key) throws Exception;

    /**
     * Removes the resource with the specified <code>key</code>.<br>
     * Key should not contain slashes.<br>
     * Use {@link #exists} to test whether
     * a resource with the specified <code>key</code> exists
     *
     * @return true if remove was made, false if the resource does not exist
     * @throws Exception if problems occurs
     */
    boolean remove(String key) throws Exception;

    /**
     * Tests whether a resource with the specified <code>key</code> already exists in this repository. Key should not
     * contain slashes<br>
     *
     * @param key the key of the resource
     * @return true if the specified resource exists
     * @throws Exception if problems occur
     */
    boolean exists(String key) throws Exception;

    /**
     * Returns the list of keys of the resources stored in this folder
     *
     * @return a String array with the keys of this folder (if not empty), or an empty array
     * @throws Exception if problems occur
     */
    String[] listKeys() throws Exception;

    /**
     * Returns the number or resources stored in this folder
     *
     * @return a non negative integer representing the resources count
     * @throws Exception if error occur
     */
    int size() throws Exception;

    /**
     * Empties the folder, deleting all stored resources
     *
     * @throws Exception if problems occur.
     */
    void clear() throws Exception;

    /**
     * @return
     */
    Database getDatabase();

}
