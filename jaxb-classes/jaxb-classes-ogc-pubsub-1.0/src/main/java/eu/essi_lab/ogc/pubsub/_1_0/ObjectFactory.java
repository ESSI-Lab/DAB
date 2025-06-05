//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.11 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2015.09.10 alle 11:53:22 AM CEST 
//


package eu.essi_lab.ogc.pubsub._1_0;

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
 * generated in the eu.essi_lab.ogc.pubsub._1_0 package. 
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

    private final static QName _Publication_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "Publication");
    private final static QName _DerivedPublication_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "DerivedPublication");
    private final static QName _Subscription_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "Subscription");
    private final static QName _MessageBatchingCriteria_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "MessageBatchingCriteria");
    private final static QName _HeartbeatCriteria_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "HeartbeatCriteria");
    private final static QName _Heartbeat_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "Heartbeat");
    private final static QName _PublicationIdentifier_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "PublicationIdentifier");
    private final static QName _SubscriptionIdentifier_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "SubscriptionIdentifier");
    private final static QName _Extension_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "Extension");
    private final static QName _CreatePublication_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "CreatePublication");
    private final static QName _CreatePublicationResponse_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "CreatePublicationResponse");
    private final static QName _GetCapabilities_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "GetCapabilities");
    private final static QName _PublisherCapabilities_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "PublisherCapabilities");
    private final static QName _FilterCapabilities_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "FilterCapabilities");
    private final static QName _FilterLanguage_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "FilterLanguage");
    private final static QName _DeliveryCapabilities_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "DeliveryCapabilities");
    private final static QName _DeliveryMethod_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "DeliveryMethod");
    private final static QName _Publications_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "Publications");
    private final static QName _GetSubscription_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "GetSubscription");
    private final static QName _GetSubscriptionResponse_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "GetSubscriptionResponse");
    private final static QName _Pause_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "Pause");
    private final static QName _PauseResponse_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "PauseResponse");
    private final static QName _RemovePublication_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "RemovePublication");
    private final static QName _RemovePublicationResponse_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "RemovePublicationResponse");
    private final static QName _Renew_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "Renew");
    private final static QName _RenewResponse_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "RenewResponse");
    private final static QName _Resume_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "Resume");
    private final static QName _ResumeResponse_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "ResumeResponse");
    private final static QName _Subscribe_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "Subscribe");
    private final static QName _SubscribeResponse_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "SubscribeResponse");
    private final static QName _Unsubscribe_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "Unsubscribe");
    private final static QName _UnsubscribeResponse_QNAME = new QName("http://www.opengis.net/pubsub/1.0", "UnsubscribeResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: eu.essi_lab.ogc.pubsub._1_0
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PublicationType }
     * 
     */
    public PublicationType createPublicationType() {
        return new PublicationType();
    }

    /**
     * Create an instance of {@link DerivedPublicationType }
     * 
     */
    public DerivedPublicationType createDerivedPublicationType() {
        return new DerivedPublicationType();
    }

    /**
     * Create an instance of {@link SubscriptionType }
     * 
     */
    public SubscriptionType createSubscriptionType() {
        return new SubscriptionType();
    }

    /**
     * Create an instance of {@link MessageBatchingCriteriaType }
     * 
     */
    public MessageBatchingCriteriaType createMessageBatchingCriteriaType() {
        return new MessageBatchingCriteriaType();
    }

    /**
     * Create an instance of {@link HeartbeatCriteriaType }
     * 
     */
    public HeartbeatCriteriaType createHeartbeatCriteriaType() {
        return new HeartbeatCriteriaType();
    }

    /**
     * Create an instance of {@link HeartbeatType }
     * 
     */
    public HeartbeatType createHeartbeatType() {
        return new HeartbeatType();
    }

    /**
     * Create an instance of {@link CreatePublicationType }
     * 
     */
    public CreatePublicationType createCreatePublicationType() {
        return new CreatePublicationType();
    }

    /**
     * Create an instance of {@link CreatePublicationResponseType }
     * 
     */
    public CreatePublicationResponseType createCreatePublicationResponseType() {
        return new CreatePublicationResponseType();
    }

    /**
     * Create an instance of {@link GetCapabilitiesType }
     * 
     */
    public GetCapabilitiesType createGetCapabilitiesType() {
        return new GetCapabilitiesType();
    }

    /**
     * Create an instance of {@link PublisherCapabilitiesType }
     * 
     */
    public PublisherCapabilitiesType createPublisherCapabilitiesType() {
        return new PublisherCapabilitiesType();
    }

    /**
     * Create an instance of {@link FilterCapabilitiesType }
     * 
     */
    public FilterCapabilitiesType createFilterCapabilitiesType() {
        return new FilterCapabilitiesType();
    }

    /**
     * Create an instance of {@link FilterLanguageType }
     * 
     */
    public FilterLanguageType createFilterLanguageType() {
        return new FilterLanguageType();
    }

    /**
     * Create an instance of {@link DeliveryCapabilitiesType }
     * 
     */
    public DeliveryCapabilitiesType createDeliveryCapabilitiesType() {
        return new DeliveryCapabilitiesType();
    }

    /**
     * Create an instance of {@link DeliveryMethodType }
     * 
     */
    public DeliveryMethodType createDeliveryMethodType() {
        return new DeliveryMethodType();
    }

    /**
     * Create an instance of {@link PublicationsType }
     * 
     */
    public PublicationsType createPublicationsType() {
        return new PublicationsType();
    }

    /**
     * Create an instance of {@link GetSubscriptionType }
     * 
     */
    public GetSubscriptionType createGetSubscriptionType() {
        return new GetSubscriptionType();
    }

    /**
     * Create an instance of {@link GetSubscriptionResponseType }
     * 
     */
    public GetSubscriptionResponseType createGetSubscriptionResponseType() {
        return new GetSubscriptionResponseType();
    }

    /**
     * Create an instance of {@link PauseType }
     * 
     */
    public PauseType createPauseType() {
        return new PauseType();
    }

    /**
     * Create an instance of {@link PauseResponseType }
     * 
     */
    public PauseResponseType createPauseResponseType() {
        return new PauseResponseType();
    }

    /**
     * Create an instance of {@link RemovePublicationType }
     * 
     */
    public RemovePublicationType createRemovePublicationType() {
        return new RemovePublicationType();
    }

    /**
     * Create an instance of {@link RemovePublicationResponseType }
     * 
     */
    public RemovePublicationResponseType createRemovePublicationResponseType() {
        return new RemovePublicationResponseType();
    }

    /**
     * Create an instance of {@link RenewType }
     * 
     */
    public RenewType createRenewType() {
        return new RenewType();
    }

    /**
     * Create an instance of {@link RenewResponseType }
     * 
     */
    public RenewResponseType createRenewResponseType() {
        return new RenewResponseType();
    }

    /**
     * Create an instance of {@link ResumeType }
     * 
     */
    public ResumeType createResumeType() {
        return new ResumeType();
    }

    /**
     * Create an instance of {@link ResumeResponseType }
     * 
     */
    public ResumeResponseType createResumeResponseType() {
        return new ResumeResponseType();
    }

    /**
     * Create an instance of {@link SubscribeType }
     * 
     */
    public SubscribeType createSubscribeType() {
        return new SubscribeType();
    }

    /**
     * Create an instance of {@link SubscribeResponseType }
     * 
     */
    public SubscribeResponseType createSubscribeResponseType() {
        return new SubscribeResponseType();
    }

    /**
     * Create an instance of {@link UnsubscribeType }
     * 
     */
    public UnsubscribeType createUnsubscribeType() {
        return new UnsubscribeType();
    }

    /**
     * Create an instance of {@link UnsubscribeResponseType }
     * 
     */
    public UnsubscribeResponseType createUnsubscribeResponseType() {
        return new UnsubscribeResponseType();
    }

    /**
     * Create an instance of {@link SubscriptionDeliveryMethodType }
     * 
     */
    public SubscriptionDeliveryMethodType createSubscriptionDeliveryMethodType() {
        return new SubscriptionDeliveryMethodType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PublicationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "Publication")
    public JAXBElement<PublicationType> createPublication(PublicationType value) {
        return new JAXBElement<PublicationType>(_Publication_QNAME, PublicationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DerivedPublicationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "DerivedPublication", substitutionHeadNamespace = "http://www.opengis.net/pubsub/1.0", substitutionHeadName = "Publication")
    public JAXBElement<DerivedPublicationType> createDerivedPublication(DerivedPublicationType value) {
        return new JAXBElement<DerivedPublicationType>(_DerivedPublication_QNAME, DerivedPublicationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubscriptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "Subscription")
    public JAXBElement<SubscriptionType> createSubscription(SubscriptionType value) {
        return new JAXBElement<SubscriptionType>(_Subscription_QNAME, SubscriptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MessageBatchingCriteriaType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "MessageBatchingCriteria")
    public JAXBElement<MessageBatchingCriteriaType> createMessageBatchingCriteria(MessageBatchingCriteriaType value) {
        return new JAXBElement<MessageBatchingCriteriaType>(_MessageBatchingCriteria_QNAME, MessageBatchingCriteriaType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HeartbeatCriteriaType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "HeartbeatCriteria")
    public JAXBElement<HeartbeatCriteriaType> createHeartbeatCriteria(HeartbeatCriteriaType value) {
        return new JAXBElement<HeartbeatCriteriaType>(_HeartbeatCriteria_QNAME, HeartbeatCriteriaType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HeartbeatType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "Heartbeat")
    public JAXBElement<HeartbeatType> createHeartbeat(HeartbeatType value) {
        return new JAXBElement<HeartbeatType>(_Heartbeat_QNAME, HeartbeatType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "PublicationIdentifier")
    public JAXBElement<String> createPublicationIdentifier(String value) {
        return new JAXBElement<String>(_PublicationIdentifier_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "SubscriptionIdentifier")
    public JAXBElement<String> createSubscriptionIdentifier(String value) {
        return new JAXBElement<String>(_SubscriptionIdentifier_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "Extension")
    public JAXBElement<Object> createExtension(Object value) {
        return new JAXBElement<Object>(_Extension_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreatePublicationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "CreatePublication")
    public JAXBElement<CreatePublicationType> createCreatePublication(CreatePublicationType value) {
        return new JAXBElement<CreatePublicationType>(_CreatePublication_QNAME, CreatePublicationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreatePublicationResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "CreatePublicationResponse")
    public JAXBElement<CreatePublicationResponseType> createCreatePublicationResponse(CreatePublicationResponseType value) {
        return new JAXBElement<CreatePublicationResponseType>(_CreatePublicationResponse_QNAME, CreatePublicationResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCapabilitiesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "GetCapabilities")
    public JAXBElement<GetCapabilitiesType> createGetCapabilities(GetCapabilitiesType value) {
        return new JAXBElement<GetCapabilitiesType>(_GetCapabilities_QNAME, GetCapabilitiesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PublisherCapabilitiesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "PublisherCapabilities")
    public JAXBElement<PublisherCapabilitiesType> createPublisherCapabilities(PublisherCapabilitiesType value) {
        return new JAXBElement<PublisherCapabilitiesType>(_PublisherCapabilities_QNAME, PublisherCapabilitiesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FilterCapabilitiesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "FilterCapabilities")
    public JAXBElement<FilterCapabilitiesType> createFilterCapabilities(FilterCapabilitiesType value) {
        return new JAXBElement<FilterCapabilitiesType>(_FilterCapabilities_QNAME, FilterCapabilitiesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FilterLanguageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "FilterLanguage")
    public JAXBElement<FilterLanguageType> createFilterLanguage(FilterLanguageType value) {
        return new JAXBElement<FilterLanguageType>(_FilterLanguage_QNAME, FilterLanguageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeliveryCapabilitiesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "DeliveryCapabilities")
    public JAXBElement<DeliveryCapabilitiesType> createDeliveryCapabilities(DeliveryCapabilitiesType value) {
        return new JAXBElement<DeliveryCapabilitiesType>(_DeliveryCapabilities_QNAME, DeliveryCapabilitiesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeliveryMethodType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "DeliveryMethod")
    public JAXBElement<DeliveryMethodType> createDeliveryMethod(DeliveryMethodType value) {
        return new JAXBElement<DeliveryMethodType>(_DeliveryMethod_QNAME, DeliveryMethodType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PublicationsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "Publications")
    public JAXBElement<PublicationsType> createPublications(PublicationsType value) {
        return new JAXBElement<PublicationsType>(_Publications_QNAME, PublicationsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSubscriptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "GetSubscription")
    public JAXBElement<GetSubscriptionType> createGetSubscription(GetSubscriptionType value) {
        return new JAXBElement<GetSubscriptionType>(_GetSubscription_QNAME, GetSubscriptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSubscriptionResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "GetSubscriptionResponse")
    public JAXBElement<GetSubscriptionResponseType> createGetSubscriptionResponse(GetSubscriptionResponseType value) {
        return new JAXBElement<GetSubscriptionResponseType>(_GetSubscriptionResponse_QNAME, GetSubscriptionResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PauseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "Pause")
    public JAXBElement<PauseType> createPause(PauseType value) {
        return new JAXBElement<PauseType>(_Pause_QNAME, PauseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PauseResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "PauseResponse")
    public JAXBElement<PauseResponseType> createPauseResponse(PauseResponseType value) {
        return new JAXBElement<PauseResponseType>(_PauseResponse_QNAME, PauseResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RemovePublicationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "RemovePublication")
    public JAXBElement<RemovePublicationType> createRemovePublication(RemovePublicationType value) {
        return new JAXBElement<RemovePublicationType>(_RemovePublication_QNAME, RemovePublicationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RemovePublicationResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "RemovePublicationResponse")
    public JAXBElement<RemovePublicationResponseType> createRemovePublicationResponse(RemovePublicationResponseType value) {
        return new JAXBElement<RemovePublicationResponseType>(_RemovePublicationResponse_QNAME, RemovePublicationResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RenewType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "Renew")
    public JAXBElement<RenewType> createRenew(RenewType value) {
        return new JAXBElement<RenewType>(_Renew_QNAME, RenewType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RenewResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "RenewResponse")
    public JAXBElement<RenewResponseType> createRenewResponse(RenewResponseType value) {
        return new JAXBElement<RenewResponseType>(_RenewResponse_QNAME, RenewResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResumeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "Resume")
    public JAXBElement<ResumeType> createResume(ResumeType value) {
        return new JAXBElement<ResumeType>(_Resume_QNAME, ResumeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResumeResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "ResumeResponse")
    public JAXBElement<ResumeResponseType> createResumeResponse(ResumeResponseType value) {
        return new JAXBElement<ResumeResponseType>(_ResumeResponse_QNAME, ResumeResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubscribeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "Subscribe")
    public JAXBElement<SubscribeType> createSubscribe(SubscribeType value) {
        return new JAXBElement<SubscribeType>(_Subscribe_QNAME, SubscribeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubscribeResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "SubscribeResponse")
    public JAXBElement<SubscribeResponseType> createSubscribeResponse(SubscribeResponseType value) {
        return new JAXBElement<SubscribeResponseType>(_SubscribeResponse_QNAME, SubscribeResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnsubscribeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "Unsubscribe")
    public JAXBElement<UnsubscribeType> createUnsubscribe(UnsubscribeType value) {
        return new JAXBElement<UnsubscribeType>(_Unsubscribe_QNAME, UnsubscribeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnsubscribeResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/pubsub/1.0", name = "UnsubscribeResponse")
    public JAXBElement<UnsubscribeResponseType> createUnsubscribeResponse(UnsubscribeResponseType value) {
        return new JAXBElement<UnsubscribeResponseType>(_UnsubscribeResponse_QNAME, UnsubscribeResponseType.class, null, value);
    }

}
