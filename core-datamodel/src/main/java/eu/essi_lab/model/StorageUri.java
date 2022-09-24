package eu.essi_lab.model;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.Serializable;
import java.net.URI;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * A POJO which encapsulates information about a generic storage (for example a database)
 *
 * @author Fabrizio
 */
public class StorageUri implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4119671755722377148L;
    private String storageUri;
    private String storageName;
    private String configFolder;
    private String password;
    private String user;

    public StorageUri() {
    }

    /**
     * @param uri
     */
    public StorageUri(String uri) {

	if (uri == null) {

	    GSLoggerFactory.getLogger(getClass()).error("URI parameter cannot be null");
	    throw new IllegalArgumentException("URI parameter cannot be null");
	}

	storageUri = uri;
	parse();
    }

    private void parse() {

	URI url = null;

	try {
	    url = new URI(storageUri);

	    String scheme = url.getScheme();

	    if (scheme != null && "file".equalsIgnoreCase(scheme)) {

		return;
	    }

	    String userinfo = url.getUserInfo();

	    if (userinfo != null && !userinfo.isEmpty()) {

		String[] split = userinfo.split(":");

		user = split[0];

		if (split.length > 1) {
		    password = split[1];
		}

		storageUri = storageUri.replace(userinfo + "@", "");
	    }

	    String path = url.getPath();

	    if (path != null && !"".equalsIgnoreCase(path)) {

		String[] split = path.split("/");

		if (split.length < 2) {
		    return;
		}

		storageName = split[1];

		if (split.length > 2) {
		    configFolder = split[2];
		}

		storageUri = storageUri.replace(path, "");
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    public String getUri() {

	if (storageUri != null && storageUri.startsWith("file://")) {

	    String uri = new String(storageUri);
	    return uri.replace("file://", "");
	}

	return storageUri;
    }

    public void setUri(String url) {

	this.storageUri = url;
    }

    public String getStorageName() {

	return storageName;
    }

    public void setStorageName(String dataBaseName) {

	this.storageName = dataBaseName;
    }

    public String getConfigFolder() {

	return configFolder;
    }

    public void setConfigFolder(String storageFolder) {

	this.configFolder = storageFolder;
    }

    public String getPassword() {

	return password;
    }

    public void setPassword(String password) {

	this.password = password;
    }

    public void setUser(String user) {

	this.user = user;
    }

    public String getUser() {

	return user;
    }

    @Override
    public boolean equals(Object object) {

	if (object == null) {
	    return false;
	}

	if (!(object instanceof StorageUri)) {
	    return false;
	}

	StorageUri su = (StorageUri) object;

	return ((su.password == null && this.password == null) || su.password.equals(this.password)) && //
		((su.user == null && this.user == null) || su.user.equals(this.user)) && //
		((su.storageUri == null && this.storageUri == null) || su.storageUri.equals(this.storageUri)) && //
		((su.configFolder == null && this.configFolder == null) || su.configFolder.equals(this.configFolder)) && //
		((su.storageName == null && this.storageName == null) || su.storageName.equals(this.storageName));

    }

    @Override
    public String toString() {

	return "URI: " + this.storageUri + //
		", folder: " + this.configFolder + //
		", name: " + this.storageName; //
	// ", user: " + this.user + //
	// ", password: " + this.password;
    }

    @Override
    public int hashCode() {

	return toString().hashCode();
    }
}
