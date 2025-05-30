//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.11 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2015.06.08 alle 02:21:50 PM CEST 
//


package eu.essi_lab.jaxb.csw._2_0_2.org.purl.dc.terms;

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

import eu.essi_lab.jaxb.csw._2_0_2.org.purl.dc.elements._1.SimpleLiteral;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the eu.essi_lab.ogc.csw._2_0_2.org.purl.dc.terms package. 
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

    private final static QName _Abstract_QNAME = new QName("http://purl.org/dc/terms/", "abstract");
    private final static QName _AccessRights_QNAME = new QName("http://purl.org/dc/terms/", "accessRights");
    private final static QName _Alternative_QNAME = new QName("http://purl.org/dc/terms/", "alternative");
    private final static QName _Audience_QNAME = new QName("http://purl.org/dc/terms/", "audience");
    private final static QName _Available_QNAME = new QName("http://purl.org/dc/terms/", "available");
    private final static QName _BibliographicCitation_QNAME = new QName("http://purl.org/dc/terms/", "bibliographicCitation");
    private final static QName _ConformsTo_QNAME = new QName("http://purl.org/dc/terms/", "conformsTo");
    private final static QName _Created_QNAME = new QName("http://purl.org/dc/terms/", "created");
    private final static QName _DateAccepted_QNAME = new QName("http://purl.org/dc/terms/", "dateAccepted");
    private final static QName _DateCopyrighted_QNAME = new QName("http://purl.org/dc/terms/", "dateCopyrighted");
    private final static QName _DateSubmitted_QNAME = new QName("http://purl.org/dc/terms/", "dateSubmitted");
    private final static QName _EducationLevel_QNAME = new QName("http://purl.org/dc/terms/", "educationLevel");
    private final static QName _Extent_QNAME = new QName("http://purl.org/dc/terms/", "extent");
    private final static QName _HasFormat_QNAME = new QName("http://purl.org/dc/terms/", "hasFormat");
    private final static QName _HasPart_QNAME = new QName("http://purl.org/dc/terms/", "hasPart");
    private final static QName _HasVersion_QNAME = new QName("http://purl.org/dc/terms/", "hasVersion");
    private final static QName _IsFormatOf_QNAME = new QName("http://purl.org/dc/terms/", "isFormatOf");
    private final static QName _IsPartOf_QNAME = new QName("http://purl.org/dc/terms/", "isPartOf");
    private final static QName _IsReferencedBy_QNAME = new QName("http://purl.org/dc/terms/", "isReferencedBy");
    private final static QName _IsReplacedBy_QNAME = new QName("http://purl.org/dc/terms/", "isReplacedBy");
    private final static QName _IsRequiredBy_QNAME = new QName("http://purl.org/dc/terms/", "isRequiredBy");
    private final static QName _Issued_QNAME = new QName("http://purl.org/dc/terms/", "issued");
    private final static QName _IsVersionOf_QNAME = new QName("http://purl.org/dc/terms/", "isVersionOf");
    private final static QName _License_QNAME = new QName("http://purl.org/dc/terms/", "license");
    private final static QName _Mediator_QNAME = new QName("http://purl.org/dc/terms/", "mediator");
    private final static QName _Medium_QNAME = new QName("http://purl.org/dc/terms/", "medium");
    private final static QName _Modified_QNAME = new QName("http://purl.org/dc/terms/", "modified");
    private final static QName _Provenance_QNAME = new QName("http://purl.org/dc/terms/", "provenance");
    private final static QName _References_QNAME = new QName("http://purl.org/dc/terms/", "references");
    private final static QName _Replaces_QNAME = new QName("http://purl.org/dc/terms/", "replaces");
    private final static QName _Requires_QNAME = new QName("http://purl.org/dc/terms/", "requires");
    private final static QName _RightsHolder_QNAME = new QName("http://purl.org/dc/terms/", "rightsHolder");
    private final static QName _Spatial_QNAME = new QName("http://purl.org/dc/terms/", "spatial");
    private final static QName _TableOfContents_QNAME = new QName("http://purl.org/dc/terms/", "tableOfContents");
    private final static QName _Temporal_QNAME = new QName("http://purl.org/dc/terms/", "temporal");
    private final static QName _Valid_QNAME = new QName("http://purl.org/dc/terms/", "valid");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: eu.essi_lab.ogc.csw._2_0_2.org.purl.dc.terms
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "abstract", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "description")
    public JAXBElement<SimpleLiteral> createAbstract(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Abstract_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "accessRights", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "rights")
    public JAXBElement<SimpleLiteral> createAccessRights(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_AccessRights_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "alternative", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "title")
    public JAXBElement<SimpleLiteral> createAlternative(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Alternative_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "audience", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createAudience(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Audience_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "available", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "date")
    public JAXBElement<SimpleLiteral> createAvailable(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Available_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "bibliographicCitation", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "identifier")
    public JAXBElement<SimpleLiteral> createBibliographicCitation(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_BibliographicCitation_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "conformsTo", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "relation")
    public JAXBElement<SimpleLiteral> createConformsTo(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_ConformsTo_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "created", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "date")
    public JAXBElement<SimpleLiteral> createCreated(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Created_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "dateAccepted", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "date")
    public JAXBElement<SimpleLiteral> createDateAccepted(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_DateAccepted_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "dateCopyrighted", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "date")
    public JAXBElement<SimpleLiteral> createDateCopyrighted(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_DateCopyrighted_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "dateSubmitted", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "date")
    public JAXBElement<SimpleLiteral> createDateSubmitted(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_DateSubmitted_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "educationLevel", substitutionHeadNamespace = "http://purl.org/dc/terms/", substitutionHeadName = "audience")
    public JAXBElement<SimpleLiteral> createEducationLevel(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_EducationLevel_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "extent", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "format")
    public JAXBElement<SimpleLiteral> createExtent(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Extent_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "hasFormat", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "relation")
    public JAXBElement<SimpleLiteral> createHasFormat(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_HasFormat_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "hasPart", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "relation")
    public JAXBElement<SimpleLiteral> createHasPart(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_HasPart_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "hasVersion", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "relation")
    public JAXBElement<SimpleLiteral> createHasVersion(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_HasVersion_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "isFormatOf", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "relation")
    public JAXBElement<SimpleLiteral> createIsFormatOf(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_IsFormatOf_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "isPartOf", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "relation")
    public JAXBElement<SimpleLiteral> createIsPartOf(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_IsPartOf_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "isReferencedBy", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "relation")
    public JAXBElement<SimpleLiteral> createIsReferencedBy(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_IsReferencedBy_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "isReplacedBy", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "relation")
    public JAXBElement<SimpleLiteral> createIsReplacedBy(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_IsReplacedBy_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "isRequiredBy", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "relation")
    public JAXBElement<SimpleLiteral> createIsRequiredBy(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_IsRequiredBy_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "issued", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "date")
    public JAXBElement<SimpleLiteral> createIssued(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Issued_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "isVersionOf", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "relation")
    public JAXBElement<SimpleLiteral> createIsVersionOf(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_IsVersionOf_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "license", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "rights")
    public JAXBElement<SimpleLiteral> createLicense(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_License_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "mediator", substitutionHeadNamespace = "http://purl.org/dc/terms/", substitutionHeadName = "audience")
    public JAXBElement<SimpleLiteral> createMediator(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Mediator_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "medium", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "format")
    public JAXBElement<SimpleLiteral> createMedium(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Medium_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "modified", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "date")
    public JAXBElement<SimpleLiteral> createModified(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Modified_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "provenance", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createProvenance(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Provenance_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "references", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "relation")
    public JAXBElement<SimpleLiteral> createReferences(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_References_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "replaces", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "relation")
    public JAXBElement<SimpleLiteral> createReplaces(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Replaces_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "requires", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "relation")
    public JAXBElement<SimpleLiteral> createRequires(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Requires_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "rightsHolder", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "DC-element")
    public JAXBElement<SimpleLiteral> createRightsHolder(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_RightsHolder_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "spatial", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "coverage")
    public JAXBElement<SimpleLiteral> createSpatial(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Spatial_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "tableOfContents", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "description")
    public JAXBElement<SimpleLiteral> createTableOfContents(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_TableOfContents_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "temporal", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "coverage")
    public JAXBElement<SimpleLiteral> createTemporal(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Temporal_QNAME, SimpleLiteral.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://purl.org/dc/terms/", name = "valid", substitutionHeadNamespace = "http://purl.org/dc/elements/1.1/", substitutionHeadName = "date")
    public JAXBElement<SimpleLiteral> createValid(SimpleLiteral value) {
        return new JAXBElement<SimpleLiteral>(_Valid_QNAME, SimpleLiteral.class, null, value);
    }

}
