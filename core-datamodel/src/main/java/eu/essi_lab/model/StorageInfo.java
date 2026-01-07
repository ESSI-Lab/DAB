package eu.essi_lab.model;

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

import java.io.Serializable;
import java.net.URI;
import java.util.Optional;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * A POJO which encapsulates information about a generic storage (for example a
 * database)
 *
 * @author Fabrizio
 */
public class StorageInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4119671755722377148L;
    private String uri;
    private String name;
    private String identifier;
    private String password;
    private String user;
    private String type;
    private String path;

    public void setPath(String path) {
	this.path = path;
    }

    public StorageInfo() {
    }

    /**
     * @param uri
     */
    public StorageInfo(String uri) {

	if (uri == null) {

	    GSLoggerFactory.getLogger(getClass()).error("URI parameter cannot be null");
	    throw new IllegalArgumentException("URI parameter cannot be null");
	}

	this.uri = uri;
	parse();
    }

    public Optional<String> getPath() {
	if (path == null || path.isEmpty()) {
	    return Optional.empty();
	}
	return Optional.of(path);
    }

    /**
     * @return
     */
    public String getUri() {

	if (uri != null && uri.startsWith("file://")) {

	    String clone = new String(uri);
	    return clone.replace("file://", "");
	}

	return uri;
    }

    /**
     * @param uri
     */
    public void setUri(String uri) {

	this.uri = uri;
    }

    /**
     * @return
     */
    public String getName() {

	return name;
    }

    /**
     * @param dataBaseName
     */
    public void setName(String dataBaseName) {

	this.name = dataBaseName;
    }

    /**
     * @return
     */
    public String getIdentifier() {

	return identifier;
    }

    /**
     * @param identifier
     */
    public void setIdentifier(String identifier) {

	this.identifier = identifier;
    }

    /**
     * @return
     */
    public String getPassword() {

	return password;
    }

    /**
     * @param password
     */
    public void setPassword(String password) {

	this.password = password;
    }

    /**
     * @param user
     */
    public void setUser(String user) {

	this.user = user;
    }

    /**
     * @return
     */
    public String getUser() {

	return user;
    }

    /**
     * @param type
     */
    public void setType(String type) {

	this.type = type;
    }

    /**
     * @return
     */
    public Optional<String> getType() {

	return Optional.ofNullable(type);
    }

    @Override
    public boolean equals(Object object) {

	if (object == null) {
	    return false;
	}

	if (!(object instanceof StorageInfo)) {
	    return false;
	}

	StorageInfo su = (StorageInfo) object;

	return ((su.password == null && this.password == null) || su.password.equals(this.password)) && //
		((su.user == null && this.user == null) || su.user.equals(this.user)) && //
		((su.uri == null && this.uri == null) || su.uri.equals(this.uri)) && //
		((su.identifier == null && this.identifier == null) || su.identifier.equals(this.identifier)) && //
		((su.type == null && this.type == null) || su.type.equals(this.type)) && //
		((su.name == null && this.name == null) || su.name.equals(this.name));
    }

    @Override
    public String toString() {

	return "- URI: " + this.uri + //
		"\n- Identifier: " + this.identifier + //
		"\n- Name: " + this.name + //
		"\n- User: " + this.user + //
		"\n- Password: " + this.password + //
		"\n- Type: " + this.type;

    }

    @Override
    public int hashCode() {

	return toString().hashCode();
    }

    @Override
    public StorageInfo clone() {

	StorageInfo clone = new StorageInfo(this.getUri());
	clone.setIdentifier(this.getIdentifier());
	clone.setPassword(this.getPassword());
	clone.setName(this.getName());
	clone.setUser(this.getUser());
	clone.setType(this.getType().orElse(null));
	if (this.getPath().isPresent()) {
	    clone.setPath(this.getPath().get());
	}
	return clone;
    }

    /**
     * 
     */
    private void parse() {

	URI url = null;

	try {
	    url = new URI(uri);

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

		uri = uri.replace(userinfo + "@", "");
	    }

	    String path = url.getPath();

	    if (path != null && !"".equalsIgnoreCase(path)) {

		String[] split = path.split("/");

		if (split.length < 2) {
		    return;
		}

		name = split[1];

		if (split.length > 2) {
		    identifier = split[2];
		}
		this.path = path.replace("/", "");
		uri = uri.replace(path, "");
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }
}
