//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.11 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2015.06.08 alle 02:21:50 PM CEST 
//


package eu.essi_lab.jaxb.csw._2_0_2.org.purl.dc.elements._1;

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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the eu.essi_lab.ogc.csw._2_0_2.org.purl.dc.elements._1 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _DCElement_QNAME = new QName("http://purl.org/dc/elements/1.1/", "DC-element");
    private final static QName _Title_QNAME = new QName("http://purl.org/dc/elements/1.1/", "title");
    private final static QName _Creator_QNAME = new QName("http://purl.org/dc/elements/1.1/", "creator");
    private final static QName _Subject_QNAME = new QName("http://purl.org/dc/elements/1.1/", "subject");
    private final static QName _Description_QNAME = new QName("http://purl.org/dc/elements/1.1/", "description");
    private final static QName _Publisher_QNAME = new QName("http://purl.org/dc/elements/1.1/", "publisher");
    private final static QName _Contributor_QNAME = new QName("http://purl.org/dc/elements/1.1/", "contributor");
    private final static QName _Date_QNAME = new QName("http://purl.org/dc/elements/1.1/", "date");
    private final static QName _Type_QNAME = new QName("http://purl.org/dc/elements/1.1/", "type");
    private final static QName _Format_QNAME = new QName("http://purl.org/dc/elements/1.1/", "format");
    private final static QName _Identifier_QNAME = new QName("http://purl.org/dc/elements/1.1/", "identifier");
    private final static QName _Source_QNAME = new QName("http://purl.org/dc/elements/1.1/", "source");
    private final static QName _Language_QNAME = new QName("http://purl.org/dc/elements/1.1/", "language");
    private final static QName _Relation_QNAME = new QName("http://purl.org/dc/elements/1.1/", "relation");
    private final static QName _Coverage_QNAME = new QName("http://purl.org/dc/elements/1.1/", "coverage");
    private final static QName _Rights_QNAME = new QName("http://purl.org/dc/elements/1.1/", "rights");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: eu.essi_lab.ogc.csw._2_0_2.org.purl.dc.elements._1
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SimpleLiteral }
     * 
     */
    public SimpleLiteral createSimpleLiteral() {
        return new SimpleLiteral();
    }

    /**
     * Create an instance of {@link ElementContainer }
     * 
     */
    public ElementContainer createElementContainer() {
        return new ElementContainer();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/elements/1.1/", name = "DC-element")
    public JAXBElement<SimpleLiteral> createDCElement(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_DCElement_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/elements/1.1/", name = "title", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createTitle(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Title_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/elements/1.1/", name = "creator", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createCreator(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Creator_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/elements/1.1/", name = "subject", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createSubject(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Subject_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/elements/1.1/", name = "description", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createDescription(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Description_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/elements/1.1/", name = "publisher", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createPublisher(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Publisher_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/elements/1.1/", name = "contributor", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createContributor(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Contributor_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/elements/1.1/", name = "date", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createDate(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Date_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/elements/1.1/", name = "type", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createType(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Type_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/elements/1.1/", name = "format", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createFormat(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Format_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/elements/1.1/", name = "identifier", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createIdentifier(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Identifier_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/elements/1.1/", name = "source", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createSource(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Source_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/elements/1.1/", name = "language", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createLanguage(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Language_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/elements/1.1/", name = "relation", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createRelation(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Relation_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/elements/1.1/", name = "coverage", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createCoverage(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Coverage_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/elements/1.1/", name = "rights", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createRights(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Rights_QNAME, SimpleLiteral.class, null, value);
    }

}
