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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the eu.essi_lab.ogc.csw._2_0_2 package. 
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

    private final static QName _AbstractRecord_QNAME = new QName("http://www.opengis.net/cat/csw/2.0.2", "AbstractRecord");
    private final static QName _DCMIRecord_QNAME = new QName("http://www.opengis.net/cat/csw/2.0.2", "DCMIRecord");
    private final static QName _BriefRecord_QNAME = new QName("http://www.opengis.net/cat/csw/2.0.2", "BriefRecord");
    private final static QName _SummaryRecord_QNAME = new QName("http://www.opengis.net/cat/csw/2.0.2", "SummaryRecord");
    private final static QName _Record_QNAME = new QName("http://www.opengis.net/cat/csw/2.0.2", "Record");
    private final static QName _AbstractQuery_QNAME = new QName("http://www.opengis.net/cat/csw/2.0.2", "AbstractQuery");
    private final static QName _Query_QNAME = new QName("http://www.opengis.net/cat/csw/2.0.2", "Query");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: eu.essi_lab.ogc.csw._2_0_2
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DCMIRecordType }
     * 
     */
    public DCMIRecordType createDCMIRecordType() {
        return new DCMIRecordType();
    }

    /**
     * Create an instance of {@link BriefRecordType }
     * 
     */
    public BriefRecordType createBriefRecordType() {
        return new BriefRecordType();
    }

    /**
     * Create an instance of {@link SummaryRecordType }
     * 
     */
    public SummaryRecordType createSummaryRecordType() {
        return new SummaryRecordType();
    }

    /**
     * Create an instance of {@link RecordType }
     * 
     */
    public RecordType createRecordType() {
        return new RecordType();
    }

    /**
     * Create an instance of {@link GetCapabilities }
     * 
     */
    public GetCapabilities createGetCapabilities() {
        return new GetCapabilities();
    }

    /**
     * Create an instance of {@link Capabilities }
     * 
     */
    public Capabilities createCapabilities() {
        return new Capabilities();
    }

    /**
     * Create an instance of {@link DescribeRecord }
     * 
     */
    public DescribeRecord createDescribeRecord() {
        return new DescribeRecord();
    }

    /**
     * Create an instance of {@link DescribeRecordResponse }
     * 
     */
    public DescribeRecordResponse createDescribeRecordResponse() {
        return new DescribeRecordResponse();
    }

    /**
     * Create an instance of {@link SchemaComponentType }
     * 
     */
    public SchemaComponentType createSchemaComponentType() {
        return new SchemaComponentType();
    }

    /**
     * Create an instance of {@link GetRecords }
     * 
     */
    public GetRecords createGetRecords() {
        return new GetRecords();
    }

    /**
     * Create an instance of {@link DistributedSearchType }
     * 
     */
    public DistributedSearchType createDistributedSearchType() {
        return new DistributedSearchType();
    }

    /**
     * Create an instance of {@link QueryType }
     * 
     */
    public QueryType createQueryType() {
        return new QueryType();
    }

    /**
     * Create an instance of {@link Constraint }
     * 
     */
    public Constraint createConstraint() {
        return new Constraint();
    }

    /**
     * Create an instance of {@link ElementSetName }
     * 
     */
    public ElementSetName createElementSetName() {
        return new ElementSetName();
    }

    /**
     * Create an instance of {@link GetRecordsResponse }
     * 
     */
    public GetRecordsResponse createGetRecordsResponse() {
        return new GetRecordsResponse();
    }

    /**
     * Create an instance of {@link RequestStatusType }
     * 
     */
    public RequestStatusType createRequestStatusType() {
        return new RequestStatusType();
    }

    /**
     * Create an instance of {@link SearchResultsType }
     * 
     */
    public SearchResultsType createSearchResultsType() {
        return new SearchResultsType();
    }

    /**
     * Create an instance of {@link GetRecordById }
     * 
     */
    public GetRecordById createGetRecordById() {
        return new GetRecordById();
    }

    /**
     * Create an instance of {@link GetRecordByIdResponse }
     * 
     */
    public GetRecordByIdResponse createGetRecordByIdResponse() {
        return new GetRecordByIdResponse();
    }

    /**
     * Create an instance of {@link GetDomain }
     * 
     */
    public GetDomain createGetDomain() {
        return new GetDomain();
    }

    /**
     * Create an instance of {@link GetDomainResponse }
     * 
     */
    public GetDomainResponse createGetDomainResponse() {
        return new GetDomainResponse();
    }

    /**
     * Create an instance of {@link DomainValuesType }
     * 
     */
    public DomainValuesType createDomainValuesType() {
        return new DomainValuesType();
    }

    /**
     * Create an instance of {@link Acknowledgement }
     * 
     */
    public Acknowledgement createAcknowledgement() {
        return new Acknowledgement();
    }

    /**
     * Create an instance of {@link EchoedRequestType }
     * 
     */
    public EchoedRequestType createEchoedRequestType() {
        return new EchoedRequestType();
    }

    /**
     * Create an instance of {@link Transaction }
     * 
     */
    public Transaction createTransaction() {
        return new Transaction();
    }

    /**
     * Create an instance of {@link InsertType }
     * 
     */
    public InsertType createInsertType() {
        return new InsertType();
    }

    /**
     * Create an instance of {@link UpdateType }
     * 
     */
    public UpdateType createUpdateType() {
        return new UpdateType();
    }

    /**
     * Create an instance of {@link DeleteType }
     * 
     */
    public DeleteType createDeleteType() {
        return new DeleteType();
    }

    /**
     * Create an instance of {@link RecordProperty }
     * 
     */
    public RecordProperty createRecordProperty() {
        return new RecordProperty();
    }

    /**
     * Create an instance of {@link TransactionResponse }
     * 
     */
    public TransactionResponse createTransactionResponse() {
        return new TransactionResponse();
    }

    /**
     * Create an instance of {@link TransactionSummaryType }
     * 
     */
    public TransactionSummaryType createTransactionSummaryType() {
        return new TransactionSummaryType();
    }

    /**
     * Create an instance of {@link InsertResultType }
     * 
     */
    public InsertResultType createInsertResultType() {
        return new InsertResultType();
    }

    /**
     * Create an instance of {@link Harvest }
     * 
     */
    public Harvest createHarvest() {
        return new Harvest();
    }

    /**
     * Create an instance of {@link HarvestResponse }
     * 
     */
    public HarvestResponse createHarvestResponse() {
        return new HarvestResponse();
    }

    /**
     * Create an instance of {@link EmptyType }
     * 
     */
    public EmptyType createEmptyType() {
        return new EmptyType();
    }

    /**
     * Create an instance of {@link ListOfValuesType }
     * 
     */
    public ListOfValuesType createListOfValuesType() {
        return new ListOfValuesType();
    }

    /**
     * Create an instance of {@link ConceptualSchemeType }
     * 
     */
    public ConceptualSchemeType createConceptualSchemeType() {
        return new ConceptualSchemeType();
    }

    /**
     * Create an instance of {@link RangeOfValuesType }
     * 
     */
    public RangeOfValuesType createRangeOfValuesType() {
        return new RangeOfValuesType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractRecordType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/cat/csw/2.0.2", name = "AbstractRecord")
    public JAXBElement<AbstractRecordType> createAbstractRecord(AbstractRecordType value) {
        return new JAXBElement<AbstractRecordType>(_AbstractRecord_QNAME, AbstractRecordType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DCMIRecordType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/cat/csw/2.0.2", name = "DCMIRecord", substitutionHeadNamespace = "http://www.opengis.net/cat/csw/2.0.2", substitutionHeadName = "AbstractRecord")
    public JAXBElement<DCMIRecordType> createDCMIRecord(DCMIRecordType value) {
        return new JAXBElement<DCMIRecordType>(_DCMIRecord_QNAME, DCMIRecordType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BriefRecordType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/cat/csw/2.0.2", name = "BriefRecord", substitutionHeadNamespace = "http://www.opengis.net/cat/csw/2.0.2", substitutionHeadName = "AbstractRecord")
    public JAXBElement<BriefRecordType> createBriefRecord(BriefRecordType value) {
        return new JAXBElement<BriefRecordType>(_BriefRecord_QNAME, BriefRecordType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SummaryRecordType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/cat/csw/2.0.2", name = "SummaryRecord", substitutionHeadNamespace = "http://www.opengis.net/cat/csw/2.0.2", substitutionHeadName = "AbstractRecord")
    public JAXBElement<SummaryRecordType> createSummaryRecord(SummaryRecordType value) {
        return new JAXBElement<SummaryRecordType>(_SummaryRecord_QNAME, SummaryRecordType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RecordType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/cat/csw/2.0.2", name = "Record", substitutionHeadNamespace = "http://www.opengis.net/cat/csw/2.0.2", substitutionHeadName = "AbstractRecord")
    public JAXBElement<RecordType> createRecord(RecordType value) {
        return new JAXBElement<RecordType>(_Record_QNAME, RecordType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractQueryType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/cat/csw/2.0.2", name = "AbstractQuery")
    public JAXBElement<AbstractQueryType> createAbstractQuery(AbstractQueryType value) {
        return new JAXBElement<AbstractQueryType>(_AbstractQuery_QNAME, AbstractQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/cat/csw/2.0.2", name = "Query", substitutionHeadNamespace = "http://www.opengis.net/cat/csw/2.0.2", substitutionHeadName = "AbstractQuery")
    public JAXBElement<QueryType> createQuery(QueryType value) {
        return new JAXBElement<QueryType>(_Query_QNAME, QueryType.class, null, value);
    }

}
