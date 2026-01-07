package eu.essi_lab.iso.datamodel.todo;

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

import java.io.InputStream;
import java.util.Iterator;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.iso19139.srv.v_20060504.SVOperationMetadataType;

public class OperationMetadata extends ISOMetadata<SVOperationMetadataType> {

    public OperationMetadata(InputStream stream) throws JAXBException {

	super(stream);
    }

    public OperationMetadata() {

	this(new SVOperationMetadataType());
    }

    public OperationMetadata(SVOperationMetadataType type) {

	super(type);
    }

    public JAXBElement<SVOperationMetadataType> getElement() {

	JAXBElement<SVOperationMetadataType> element = ObjectFactories.SRV().createSVOperationMetadata(type);
	return element;
    }
    @XmlRootElement
    public enum Binding {
	HTTP_GET("HttpGet"), HTTP_POST("HttpPost"), SOAP_RPC("Soap-RPC"), SOAP_DOC("Soap-DOC"), FILE("File"), FTP("FTP"), XML_RPC(
		"XML-RPC"), UNKNOWN("Unknown");
	private String name;

	Binding(String name) {
	    this.name = name;
	}

	@Override
	public String toString() {
	    return name;
	}

	 
	public static Binding getBinding(String binding) {
	    if (binding == null) {
		return UNKNOWN;
	    }

	    String bl = binding.toLowerCase();
	    if (bl.matches("(?i).*get.*")) {
		return Binding.HTTP_GET;
	    }
	    if (bl.matches("(?i).*post.*")) {
		return Binding.HTTP_POST;
	    }
	    if (bl.matches("(?i).*soap.*")) {
		if (bl.matches("(?i).*rpc.*")) {
		    return Binding.SOAP_RPC;
		} else {
		    return Binding.SOAP_DOC;
		}
	    }
	    if (bl.matches("(?i).*file.*")) {
		return Binding.FILE;
	    }
	    // the generic HTTP request is a get
	    if (bl.matches("(?i).*http.*")) {
		return Binding.HTTP_GET;
	    }

	    if (bl.matches("(?i).*file.*")) {
		return Binding.FILE;
	    }
	    if (bl.matches("(?i).*xmlrpc.*")) {
		return Binding.XML_RPC;
	    }

	    if (bl.matches("(?i).*ftp.*")) {
		return Binding.FTP;
	    }

	    return UNKNOWN;
	}
    }

    /**
    *    @XPathDirective(target = "srv:operationName/gco:CharacterString")
    */
    String getOperationName() {
	return null;
    }

    /**
    *    @XPathDirective(target = "srv:operationName/gco:CharacterString")
    */
    void setOperationName(String name) {
    }

    /**
    *    @XPathDirective(target = ".//srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL/text()")
    */
    Iterator<String> getEndpoints() {
	return null;
    }

    String getEndpoint() {
	return null;
    }

    /**
    *    @XPathDirective(target = ".", parent = "srv:DCP", after = "srv:operationName")
    */
    void addBinding(Binding binding) {
    }

    /**
    *    @XPathDirective(clear = ".//srv:DCP")
    */
    void clearBindings() {
    }

    /**
    *    @XPathDirective(target = ".//srv:DCP/srv:DCPList/@codeListValue")
    */
    Iterator<Binding> getBindings() {
	return null;
    }

    /**
    *    @XPathDirective(target = ".//srv:connectPoint/gmd:CI_OnlineResource")
    */
    Iterator<Online> getOnlineResources() {
	return null;
    }

    Online getOnlineResource() {
	return null;
    }

    /**
    *    @XPathDirective(target = ".", parent = "srv:connectPoint", position = Position.LAST)
    */
    void addOnlineResource(Online res) {
    }

    /**
    *    @XPathDirective(target = ".//srv:parameters/srv:SV_Parameter")
    */
    Iterator<OperationParameter> getParameters() {
	return null;
    }
}
