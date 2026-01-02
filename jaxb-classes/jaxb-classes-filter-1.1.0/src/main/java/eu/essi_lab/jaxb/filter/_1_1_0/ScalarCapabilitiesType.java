//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.11 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2015.06.08 alle 02:33:20 PM CEST 
//


package eu.essi_lab.jaxb.filter._1_1_0;

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per Scalar_CapabilitiesType complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="Scalar_CapabilitiesType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/ogc}LogicalOperators" minOccurs="0"/&gt;
 *         &lt;element name="ComparisonOperators" type="{http://www.opengis.net/ogc}ComparisonOperatorsType" minOccurs="0"/&gt;
 *         &lt;element name="ArithmeticOperators" type="{http://www.opengis.net/ogc}ArithmeticOperatorsType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Scalar_CapabilitiesType", propOrder = {
    "logicalOperators",
    "comparisonOperators",
    "arithmeticOperators"
})
public class ScalarCapabilitiesType {

    @XmlElement(name = "LogicalOperators")
    protected LogicalOperators logicalOperators;
    @XmlElement(name = "ComparisonOperators")
    protected ComparisonOperatorsType comparisonOperators;
    @XmlElement(name = "ArithmeticOperators")
    protected ArithmeticOperatorsType arithmeticOperators;

    /**
     * Recupera il valore della proprietà logicalOperators.
     * 
     * @return
     *     possible object is
     *     {@link LogicalOperators }
     *     
     */
    public LogicalOperators getLogicalOperators() {
        return logicalOperators;
    }

    /**
     * Imposta il valore della proprietà logicalOperators.
     * 
     * @param value
     *     allowed object is
     *     {@link LogicalOperators }
     *     
     */
    public void setLogicalOperators(LogicalOperators value) {
        this.logicalOperators = value;
    }

    public boolean isSetLogicalOperators() {
        return (this.logicalOperators!= null);
    }

    /**
     * Recupera il valore della proprietà comparisonOperators.
     * 
     * @return
     *     possible object is
     *     {@link ComparisonOperatorsType }
     *     
     */
    public ComparisonOperatorsType getComparisonOperators() {
        return comparisonOperators;
    }

    /**
     * Imposta il valore della proprietà comparisonOperators.
     * 
     * @param value
     *     allowed object is
     *     {@link ComparisonOperatorsType }
     *     
     */
    public void setComparisonOperators(ComparisonOperatorsType value) {
        this.comparisonOperators = value;
    }

    public boolean isSetComparisonOperators() {
        return (this.comparisonOperators!= null);
    }

    /**
     * Recupera il valore della proprietà arithmeticOperators.
     * 
     * @return
     *     possible object is
     *     {@link ArithmeticOperatorsType }
     *     
     */
    public ArithmeticOperatorsType getArithmeticOperators() {
        return arithmeticOperators;
    }

    /**
     * Imposta il valore della proprietà arithmeticOperators.
     * 
     * @param value
     *     allowed object is
     *     {@link ArithmeticOperatorsType }
     *     
     */
    public void setArithmeticOperators(ArithmeticOperatorsType value) {
        this.arithmeticOperators = value;
    }

    public boolean isSetArithmeticOperators() {
        return (this.arithmeticOperators!= null);
    }

}
