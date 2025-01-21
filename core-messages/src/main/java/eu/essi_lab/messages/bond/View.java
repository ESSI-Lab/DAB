package eu.essi_lab.messages.bond;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

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

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.io.output.ByteArrayOutputStream;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.jaxb.ViewFactory;

/**
 * The view object describes a view (a set of predefined constraints associated with a label and a description). Views
 * can be managed by a user in the db,
 * 
 * @author boldrini
 */
@XmlRootElement
public class View implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4652948734431078022L;

    /**
     * @author Fabrizio
     */
    public enum ViewVisibility {

	/**
	 * 
	 */
	PUBLIC,
	/**
	 * 
	 */
	PRIVATE;

	/**
	 * @param visibility
	 * @return
	 */
	public static ViewVisibility fromName(String visibility) {

	    if (visibility.equals(PUBLIC.name())) {

		return PUBLIC;
	    }

	    return PRIVATE;
	}
    }

    private String creator;
    private String id;
    private String label;
    private Date creationTime = new Date();
    private Date expirationTime;
    private String visibility;
    private String owner;

    @XmlElements({ @XmlElement(name = "viewBond", type = ViewBond.class), //
	    @XmlElement(name = "resourcePropertyBond", type = ResourcePropertyBond.class), //
	    @XmlElement(name = "simpleValueBond", type = SimpleValueBond.class), //
	    @XmlElement(name = "spatialBond", type = SpatialBond.class), //
	    @XmlElement(name = "logicalBond", type = LogicalBond.class), //
	    @XmlElement(name = "emptyBond", type = EmptyBond.class) })
    protected Bond bond;

    /**
     * 	
     */
    public View() {
	setVisibility(ViewVisibility.PRIVATE);
    }

    /**
     * @param identifier
     */
    public View(String identifier) {
	setId(identifier);
	setVisibility(ViewVisibility.PRIVATE);
    }

    /**
     * @param identifier
     * @param creator
     */
    public View(String identifier, String creator) {
	this(identifier);
	setCreator(creator);
    }

    /**
     * @return
     * @throws JAXBException
     */
    public InputStream toStream() throws JAXBException {

	return toStream(this);
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     */
    public static View fromStream(InputStream stream) throws JAXBException {

	ViewFactory factory = new ViewFactory();

	Unmarshaller unmarshaller = factory.createUnmarshaller();

	return (View) unmarshaller.unmarshal(stream);
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     */
    public static View createOrNull(InputStream stream) {

	try {
	    return fromStream(stream);
	} catch (JAXBException e) {

	    GSLoggerFactory.getLogger(View.class).error(e);
	}

	return null;
    }

    /**
     * @param view
     * @return
     * @throws JAXBException
     */
    public static InputStream toStream(View view) throws JAXBException {

	ViewFactory factory = new ViewFactory();

	Marshaller marshaller = factory.createMarshaller();

	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	marshaller.marshal(view, baos);

	byte[] bytes = baos.toByteArray();

	return new ByteArrayInputStream(bytes);
    }

    public Date getCreationTime() {
	return creationTime;
    }

    public void setCreationTime(Date creationTime) {
	this.creationTime = creationTime;
    }

    public Date getExpirationTime() {
	return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
	this.expirationTime = expirationTime;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getCreator() {
	return creator;
    }

    public void setCreator(String creator) {
	this.creator = creator;
    }

    public String getLabel() {
	return label;
    }

    public void setLabel(String label) {
	this.label = label;
    }

    public String getOwner() {

	return owner;
    }

    public void setOwner(String owner) {

	this.owner = owner;
    }

    public ViewVisibility getVisibility() {

	return ViewVisibility.fromName(this.visibility);
    }

    public void setVisibility(ViewVisibility viewVisibility) {

	if (viewVisibility == null) {
	    throw new IllegalArgumentException();
	}

	this.visibility = viewVisibility.name();
    }

    @XmlTransient
    public Bond getBond() {
	return bond;
    }

    public void setBond(Bond bond) {
	this.bond = bond;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof View) {
	    return Objects.equals(id, ((View) obj).getId()) && //
		    Objects.equals(label, ((View) obj).getLabel()) && //
		    Objects.equals(creationTime, ((View) obj).getCreationTime()) && //
		    Objects.equals(expirationTime, ((View) obj).getExpirationTime()) && //
		    Objects.equals(bond, ((View) obj).getBond());
	}
	return super.equals(obj);
    }

    @Override
    public String toString() {

	return id + "_" + label;
    }
}
