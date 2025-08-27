package eu.essi_lab.model.auth;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.validator.constraints.Email;
import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.DOMSerializer;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.model.GSProperty;

@XmlRootElement(name = "user", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
public class GSUser extends DOMSerializer implements Serializable {

    /**
     * 
     */
    public static final String ESSI_LAB_DOMAIN = "@essi-lab.eu";

    /**
     * 
     */
    private static final long serialVersionUID = 396389490559553620L;

    /**
     * 
     */
    public static final String IDENTIFIER_TYPE_ELEMENT_NAME = "identifierType";

    @NotNull
    @Email
    @Pattern(regexp = "^(?!.*(\\\\|\\/).*).*")
    @XmlElement(name = "identifier")
    private String identifier;
    @NotNull
    @Pattern(regexp = "^(facebook|google|twitter)$")
    @XmlElement(name = "authProvider")
    private String authProvider;
    @XmlElement
    private boolean enabled = true; // true by default, to be compatible with previous user without this field
    @XmlElement
    private String role;
    @SuppressWarnings("rawtypes")
    @XmlElement(name = "property")
    private List<GSProperty> properties;

    private static JAXBContext context;
    static {
	try {
	    context = JAXBContext.newInstance(GSUser.class);
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     */
    public static GSUser create(InputStream stream) throws JAXBException {

	return new GSUser().fromStream(stream);
    }

    /**
     * @param node
     * @return
     * @throws JAXBException
     */
    public static GSUser create(Node node) throws JAXBException {

	return new GSUser().fromNode(node);
    }

    /**
     * @param node
     * @return
     * @throws JAXBException
     */
    public static GSUser createOrNull(Node node) {

	try {
	    return new GSUser().fromNode(node);
	} catch (JAXBException e) {
	    GSLoggerFactory.getLogger(GSUser.class).error(e);
	}

	return null;
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     */
    public static GSUser createOrNull(InputStream stream) {

	try {
	    return new GSUser().fromStream(stream);
	} catch (JAXBException e) {
	    GSLoggerFactory.getLogger(GSUser.class).error(e);
	}

	return null;
    }

    /**
     * 
     */
    public GSUser() {

	this.properties = new ArrayList<>();
    }

    /**
     * @param userIdentifier
     * @param identifierType
     * @param role
     */
    public GSUser(String userIdentifier, UserIdentifierType identifierType, String role) {

	this.properties = new ArrayList<>();

	setIdentifier(userIdentifier);
	setRole(role);

	getProperties().add(new GSProperty<String>(identifierType.getType(), userIdentifier));

	getProperties().add(new GSProperty<String>(IDENTIFIER_TYPE_ELEMENT_NAME, identifierType.getType()));
    }

    @XmlTransient
    public String getIdentifier() {

	return identifier;
    }

    /**
     * @param identifier
     */
    public void setIdentifier(String identifier) {

	this.identifier = identifier;
    }

    @XmlTransient
    public String getUri() {

	return toURI(identifier);
    }

    @XmlTransient
    public UserIdentifierType getUserIdentifierType() {

	@SuppressWarnings("unchecked")
	GSProperty<String> property = getProperty(IDENTIFIER_TYPE_ELEMENT_NAME);
	if (property != null) {

	    Optional<UserIdentifierType> ret = UserIdentifierType.fromType(property.getValue().toString());
	    if (ret.isEmpty()) {
		return null;
	    }
	    return ret.get();
	}

	return null;
    }

    @XmlTransient
    public String getAuthProvider() {

	return authProvider;
    }

    public void setAuthProvider(String authProvider) {
	this.authProvider = authProvider;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getRole() {
	return role;
    }

    /**
     * @param role
     */
    public void setRole(String role) {
	this.role = role;
    }

    /**
     * @return
     */
    @XmlTransient
    public Boolean isEnabled() {
	return enabled;
    }

    /**
     * @param role
     */
    public void setEnabled(boolean enabled) {
	this.enabled = enabled;
    }

    @SuppressWarnings("rawtypes")
    @XmlTransient
    public List<GSProperty> getProperties() {
	return properties;
    }

    @SuppressWarnings("rawtypes")
    public GSProperty getProperty(String name) {
	for (GSProperty property : properties) {
	    if (property.getName() != null && property.getName().equals(name)) {
		return property;
	    }
	}
	return null;
    }

    public String getStringPropertyValue(String name) {
	for (GSProperty property : properties) {
	    if (property.getName() != null && property.getName().equals(name)) {
		return property.getValue().toString();
	    }
	}
	return null;
    }

    @SuppressWarnings("rawtypes")
    public void setAttributes(List<GSProperty> attributes) {
	this.properties = attributes;
    }

    public void setPropertyValue(String name, String value) {
	GSProperty<String> target = null;
	for (GSProperty property : properties) {
	    if (property.getName().equals(name)) {
		if (property.getValue() instanceof String) {
		    target = property;
		}
	    }
	}
	if (target != null) {
	    target.setValue(value);
	} else {
	    GSProperty<String> prop = new GSProperty<String>(name, value);
	    properties.add(prop);
	}
    }

    @Override
    public int hashCode() {
	return Objects.hash(getAuthProvider(), getIdentifier(), getProperties());
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	GSUser other = (GSUser) obj;
	if (properties == null) {
	    if (other.properties != null) {
		return false;
	    }
	} else if (!properties.equals(other.properties)) {
	    return false;
	}
	if (authProvider == null) {
	    if (other.authProvider != null) {
		return false;
	    }
	} else if (!authProvider.equals(other.authProvider)) {
	    return false;
	}
	if (identifier == null) {
	    if (other.identifier != null) {
		return false;
	    }
	} else if (!identifier.equals(other.identifier)) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {

	return identifier + ":" + role;
    }

    @Override
    public GSUser fromStream(InputStream stream) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (GSUser) unmarshaller.unmarshal(stream);
    }

    @Override
    public GSUser fromNode(Node node) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (GSUser) unmarshaller.unmarshal(node);
    }

    /**
     * @param userIdentifier
     * @return
     */
    public static String toURI(String userIdentifier) {

	try {
	    userIdentifier = URLEncoder.encode(userIdentifier, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	}

	return userIdentifier;
    }

    @Override
    protected Unmarshaller createUnmarshaller() throws JAXBException {

	return context.createUnmarshaller();
    }

    @Override
    protected Marshaller createMarshaller() throws JAXBException {

	Marshaller marshaller = context.createMarshaller();
	marshaller.setProperty("jaxb.formatted.output", true);
	marshaller.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, new CommonNameSpaceContext());
	return marshaller;
    }

    @Override
    protected Object getElement() throws JAXBException {

	return this;
    }

    public boolean hasPermission(String permission) {
	String permissions = getStringPropertyValue("permissions");
	if (permissions != null) {
	    permissions = permissions.trim();
	    String[] perms = permissions.split(",");
	    for (String perm : perms) {
		perm = perm.trim();
		if (perm.equalsIgnoreCase(permission)) {
		    return true;
		}
	    }

	}
	return false;

    }
}
