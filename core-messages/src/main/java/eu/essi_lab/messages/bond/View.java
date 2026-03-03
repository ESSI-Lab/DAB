package eu.essi_lab.messages.bond;

import com.fasterxml.jackson.annotation.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.bond.jaxb.*;
import jakarta.xml.bind.*;
import jakarta.xml.bind.annotation.*;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.*;
import java.util.*;

/**
 * The view object describes a view (a set of predefined constraints associated with a label and a description). Views can be managed by a
 * user in the db,
 *
 * @author boldrini
 */
@XmlRootElement
public class View implements Serializable {

    /**
     *
     */
    @Serial
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
    private String sourceDeployment;

    @XmlElements({ @XmlElement(name = "viewBond", type = ViewBond.class), //
	    @XmlElement(name = "resourcePropertyBond", type = ResourcePropertyBond.class), //
	    @XmlElement(name = "simpleValueBond", type = SimpleValueBond.class), //
	    @XmlElement(name = "spatialBond", type = SpatialBond.class), //
	    @XmlElement(name = "logicalBond", type = LogicalBond.class), //
	    @XmlElement(name = "falseBond", type = FalseBond.class), @XmlElement(name = "trueBond", type = TrueBond.class) })
    @JsonProperty
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

	Unmarshaller unmarshaller = ViewFactory.createUnmarshaller();

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

	Marshaller marshaller = ViewFactory.createMarshaller();

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
    @JsonIgnore
    public Bond getBond() {

	return bond != null ? bond : BondFactory.getTrueBond();
    }

    public void setBond(Bond bond) {
	this.bond = bond;
    }

    /**
     * @return the sourceDeployment
     */
    public String getSourceDeployment() {
	return sourceDeployment;
    }

    /**
     * @param sourceDeployment
     */
    public void setSourceDeployment(String sourceDeployment) {
	this.sourceDeployment = sourceDeployment;
    }

    @Override
    public boolean equals(Object obj) {

	if (obj instanceof View other) {

	    return Objects.equals(id, other.getId()) && //
		    Objects.equals(label, other.getLabel()) && //
		    Objects.equals(creationTime, other.getCreationTime()) && //
		    Objects.equals(expirationTime, other.getExpirationTime()) && //
		    Objects.equals(getVisibility(), other.getVisibility()) && //
		    Objects.equals(owner, other.getOwner()) &&  //
		    Objects.equals(sourceDeployment, other.getSourceDeployment()) &&  //
		    ((bond == null && other.bond == null) || bond.toString().equals(other.bond.toString()));

	}

	return super.equals(obj);
    }

    @Override
    public String toString() {

	return id + "_" + label;
    }
}
