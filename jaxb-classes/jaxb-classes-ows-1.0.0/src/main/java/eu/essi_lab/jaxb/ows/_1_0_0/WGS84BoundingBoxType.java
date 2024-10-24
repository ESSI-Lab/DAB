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
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import javax.xml.bind.annotation.XmlType;


/**
 * This type is adapted from the general BoundingBoxType, with modified contents and documentation for use with the 2D WGS 84 coordinate reference system. 
 * 
 * <p>Classe Java per WGS84BoundingBoxType complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="WGS84BoundingBoxType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.opengis.net/ows}BoundingBoxType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="LowerCorner" type="{http://www.opengis.net/ows}PositionType2D"/&gt;
 *         &lt;element name="UpperCorner" type="{http://www.opengis.net/ows}PositionType2D"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="crs" type="{http://www.w3.org/2001/XMLSchema}anyURI" fixed="urn:ogc:def:crs:OGC:2:84" /&gt;
 *       &lt;attribute name="dimensions" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" fixed="2" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WGS84BoundingBoxType")
public class WGS84BoundingBoxType
    extends BoundingBoxType
{


}
