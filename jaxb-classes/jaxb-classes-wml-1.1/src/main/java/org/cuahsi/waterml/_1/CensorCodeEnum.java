//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.12.10 at 03:04:33 PM CET 
//


package org.cuahsi.waterml._1;

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

@XmlType(name = "CensorCodeEnum")
@XmlEnum
public enum CensorCodeEnum {

    @XmlEnumValue("lt")
    LT("lt"),
    @XmlEnumValue("gt")
    GT("gt"),
    @XmlEnumValue("nc")
    NC("nc"),
    @XmlEnumValue("nd")
    ND("nd"),
    @XmlEnumValue("pnq")
    PNQ("pnq");
    private final String value;

    CensorCodeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CensorCodeEnum fromValue(String v) {
        for (CensorCodeEnum c: CensorCodeEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
