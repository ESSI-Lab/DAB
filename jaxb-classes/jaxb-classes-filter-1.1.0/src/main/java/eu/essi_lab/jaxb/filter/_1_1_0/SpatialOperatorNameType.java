//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.11 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2015.06.08 alle 02:33:20 PM CEST 
//


package eu.essi_lab.jaxb.filter._1_1_0;

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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "SpatialOperatorNameType")
@XmlEnum
public enum SpatialOperatorNameType {

    BBOX("BBOX"),
    @XmlEnumValue("Equals")
    EQUALS("Equals"),
    @XmlEnumValue("Disjoint")
    DISJOINT("Disjoint"),
    @XmlEnumValue("Intersects")
    INTERSECTS("Intersects"),
    @XmlEnumValue("Touches")
    TOUCHES("Touches"),
    @XmlEnumValue("Crosses")
    CROSSES("Crosses"),
    @XmlEnumValue("Within")
    WITHIN("Within"),
    @XmlEnumValue("Contains")
    CONTAINS("Contains"),
    @XmlEnumValue("Overlaps")
    OVERLAPS("Overlaps"),
    @XmlEnumValue("Beyond")
    BEYOND("Beyond"),
    @XmlEnumValue("DWithin")
    D_WITHIN("DWithin");
    private final String value;

    SpatialOperatorNameType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SpatialOperatorNameType fromValue(String v) {
        for (SpatialOperatorNameType c: SpatialOperatorNameType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
