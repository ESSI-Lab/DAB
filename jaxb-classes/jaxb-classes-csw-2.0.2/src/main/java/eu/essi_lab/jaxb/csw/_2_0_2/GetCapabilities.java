//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.11
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine.
// Generato il: 2015.06.08 alle 02:33:24 PM CEST
//

package eu.essi_lab.jaxb.csw._2_0_2;

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.essi_lab.jaxb.ows._1_0_0.GetCapabilitiesType;

/**
 * Request for a description of service capabilities. See OGC 05-008
 * for more information.
 * <p>
 * Classe Java per GetCapabilitiesType complex type.
 * <p>
 * Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="GetCapabilitiesType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/ows}GetCapabilitiesType"&gt;
 *       &lt;attribute name="service" type="{http://www.opengis.net/ows}ServiceType" default="http://www.opengis.net/cat/csw" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetCapabilitiesType")
@XmlRootElement(name = "GetCapabilities")
public class GetCapabilities extends GetCapabilitiesType {

    @XmlAttribute(name = "service")
    protected String service;

    /**
     * Recupera il valore della proprietà service.
     * 
     * @return
     * 	possible object is
     *         {@link String }
     */
    public String getService() {

	return service;
    }

    /**
     * Imposta il valore della proprietà service.
     * 
     * @param value
     *        allowed object is
     *        {@link String }
     */
    public void setService(String value) {
	this.service = value;
    }

}
