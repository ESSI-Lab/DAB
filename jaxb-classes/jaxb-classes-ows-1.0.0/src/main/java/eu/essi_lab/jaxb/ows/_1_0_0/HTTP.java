//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.11 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2015.06.08 alle 02:24:35 PM CEST 
//


package eu.essi_lab.jaxb.ows._1_0_0;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getOrPost"
})
@XmlRootElement(name = "HTTP")
public class HTTP {

    @XmlElementRefs({
        @XmlElementRef(name = "Post", namespace = "http://www.opengis.net/ows", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "Get", namespace = "http://www.opengis.net/ows", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<RequestMethodType>> getOrPost;

    /**
     * Gets the value of the getOrPost property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the getOrPost property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGetOrPost().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link RequestMethodType }{@code >}
     * {@link JAXBElement }{@code <}{@link RequestMethodType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<RequestMethodType>> getGetOrPost() {
        if (getOrPost == null) {
            getOrPost = new ArrayList<JAXBElement<RequestMethodType>>();
        }
        return this.getOrPost;
    }

}
