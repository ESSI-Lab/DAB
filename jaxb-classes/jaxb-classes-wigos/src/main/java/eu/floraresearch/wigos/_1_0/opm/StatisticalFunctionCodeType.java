//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2022.09.12 at 03:34:17 PM CEST 
//


package eu.floraresearch.wigos._1_0.opm;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import eu.floraresearch.wigos._1_0.gml._3_2_1.ReferenceType;


/**
 * The «CodeList» class 'StatisticalFunctionCode' specifies the type of statistical function
 *             that is applied to the base observable property to define the statistical summary;
 *             e.g. maximum air temperature.  Note that WMO provides two code-tables listing statistical
 *             operators:  - WMO No. 306 Vol I.2 Part B FM 92 GRIB code-table 4.10 'Type of statistical
 *             processing'; and - WMO No. 306 Vol I.2 Part B FM 94 BUFR code-table 0 08 023 'First-order
 *             statistics'  The GRIB2 code-table is defined as the 'recommended' vocabulary for this
 *             «CodeList» class but lacks some of the necessary terms. For example, the GRIB code-table
 *             includes 'Average' but does not include 'Mean', 'Mode' or 'Median' (which can be found
 *             in the BUFR code-table). However, the BUFR code-table is _NOT_ chosen because 'Accumulation'
 *             is entirely missing. Given that 'extensibility' is set to 'any', authors are free
 *             to refer to their preferred 'statistical operator' vocabulary. The GRIB code-table
 *             is only a recommendation. 
 *             
 *             vocabulary: http://codes.wmo.int/grib2/codeflag/4.10
 *             extensibility: any
 *          
 * 
 * <p>Java class for StatisticalFunctionCodeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StatisticalFunctionCodeType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/gml/3.2}ReferenceType"&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatisticalFunctionCodeType")
public class StatisticalFunctionCodeType
    extends ReferenceType
{


}