//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.11 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2017.01.05 alle 09:41:18 AM CET 
//


package eu.essi_lab.jaxb.oaipmh;

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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per OAI-PMHerrorcodeType.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * <p>
 * <pre>
 * &lt;simpleType name="OAI-PMHerrorcodeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="cannotDisseminateFormat"/&gt;
 *     &lt;enumeration value="idDoesNotExist"/&gt;
 *     &lt;enumeration value="badArgument"/&gt;
 *     &lt;enumeration value="badVerb"/&gt;
 *     &lt;enumeration value="noMetadataFormats"/&gt;
 *     &lt;enumeration value="noRecordsMatch"/&gt;
 *     &lt;enumeration value="badResumptionToken"/&gt;
 *     &lt;enumeration value="noSetHierarchy"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "OAI-PMHerrorcodeType")
@XmlEnum
public enum OAIPMHerrorcodeType {

    @XmlEnumValue("cannotDisseminateFormat")
    CANNOT_DISSEMINATE_FORMAT("cannotDisseminateFormat"),
    @XmlEnumValue("idDoesNotExist")
    ID_DOES_NOT_EXIST("idDoesNotExist"),
    @XmlEnumValue("badArgument")
    BAD_ARGUMENT("badArgument"),
    @XmlEnumValue("badVerb")
    BAD_VERB("badVerb"),
    @XmlEnumValue("noMetadataFormats")
    NO_METADATA_FORMATS("noMetadataFormats"),
    @XmlEnumValue("noRecordsMatch")
    NO_RECORDS_MATCH("noRecordsMatch"),
    @XmlEnumValue("badResumptionToken")
    BAD_RESUMPTION_TOKEN("badResumptionToken"),
    @XmlEnumValue("noSetHierarchy")
    NO_SET_HIERARCHY("noSetHierarchy");
    private final String value;

    OAIPMHerrorcodeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OAIPMHerrorcodeType fromValue(String v) {
        for (OAIPMHerrorcodeType c: OAIPMHerrorcodeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
