package eu.essi_lab.gssrv.conf.task.collection;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Identification;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WHOSOntology;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.whos.WIS2DataPolicy;
import eu.essi_lab.lib.whos.WIS2HydrologyTopic;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.stats.ComputationResult;
import eu.essi_lab.messages.stats.ResponseItem;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.index.jaxb.CardinalValues;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.profiler.wis.WISUtils;
import eu.essi_lab.request.executor.*;

class SourceCollectionCreator {

    /**
     * Max number of terms of the frequency statistics, enough to get all the observed properties of a source
     */
    private static final int MAX_FREQUENCY_ITEMS = 100;

    /**
     * Max number of organisations added as point of contact
     */
    private static final int MAX_ORGANISATIONS = 10;

    protected final String centreId;

    /**
     * Max number of datasets of an organisation read to find its e-mail address
     */
    private static final int MAX_EMAIL_DATASETS = 10;

    /**
     * The e-mail addresses of the organisations of a source, by organisation name, cached since the same organisations
     * appear in all the collections of the source
     */
    private final HashMap<String, Optional<String>> organisationEmails = new HashMap<>();

    public SourceCollectionCreator() {
	this(WISUtils.getCentreId());
    }

    public SourceCollectionCreator(String centreId) {
	this.centreId = centreId;
    }

    public List<DatasetCollection> getCollections(String sourceId, String sourceDeployments) throws GSException {

	GSSource source = ConfigurationWrapper.getSource(sourceId);

	if (source == null) {
	    throw new RuntimeException("Source not found: " + sourceId);
	}

	List<ResponseItem> statisticsResponse = getStatistics(sourceId, getGroupByQueryable());

	List<DatasetCollection> ret = new ArrayList<>();

	for (ResponseItem response : statisticsResponse) {
	    DatasetCollection dataset = mapDataset(source, response, sourceDeployments);
	    ret.add(dataset);
	}

	return ret;
    }

    protected Queryable getGroupByQueryable() {
	return ResourceProperty.SOURCE_ID;
    }

    private DatasetCollection mapDataset(GSSource source, ResponseItem responseItem, String sourceDeployments) throws GSException {

	String groupBy = responseItem.getGroupedBy().get();

	DatasetCollection dataset = new DatasetCollection();
	String[] sd = sourceDeployments.split(",");
	for (String s : sd) {
	    dataset.getPropertyHandler().addSourceDeployment(s.trim());
	}

	dataset.setSource(source);
	String sourceId = source.getUniqueIdentifier();

	dataset.getExtensionHandler().setWISTopicHierarchy(WIS2HydrologyTopic.getMetadataTopic(centreId));

	// the data of core sources are published (by the data harvester task) under the topics of their observed
	// properties, advertised according to the data notifications mode. Topics are sorted to keep the generated record
	// (and its integrity checksum) stable
	if (WISUtils.getDataPolicy(sourceId) == WIS2DataPolicy.CORE) {
	    TreeSet<String> topics = new TreeSet<>();
	    Optional<ComputationResult> properties = responseItem.getFrequency(MetadataElement.OBSERVED_PROPERTY_URI);
	    if (properties.isPresent()) {
		for (TermFrequencyItem item : properties.get().getFrequencyItems()) {
		    WIS2HydrologyTopic.decode(item.getTerm()).flatMap(WISUtils::getAdvertisedTopic).ifPresent(topics::add);
		}
	    }
	    topics.forEach(dataset.getExtensionHandler()::addWISDataTopic);
	}

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	String id = getMetadataIdentifier(source.getUniqueIdentifier(), groupBy);
	dataset.setOriginalId(id);
	dataset.setPrivateId(id);
	coreMetadata.setIdentifier(id);
	MIMetadata miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	coreMetadata.getMIMetadata().setDateStampAsDate(ISO8601DateTimeUtils.getISO8601Date());
	coreMetadata.setTitle(getTitle(source, groupBy));
	miMetadata.setCharacterSetCode("utf8");

	miMetadata.addHierarchyLevelScopeCodeListValue("series");

	Double w = null;
	Double e = null;
	Double s = null;
	Double n = null;

	Optional<CardinalValues> cardinal = responseItem.getBBoxUnion().getCardinalValues();
	if (cardinal.isPresent()) {
	    w = Double.parseDouble(cardinal.get().getWest());
	    e = Double.parseDouble(cardinal.get().getEast());
	    s = Double.parseDouble(cardinal.get().getSouth());
	    n = Double.parseDouble(cardinal.get().getNorth());
	}

	if (n != null && w != null && e != null && s != null) {
	    coreMetadata.addBoundingBox(n, w, s, e);
	} else {
	    GSLoggerFactory.getLogger(getClass()).error("source collection to check");
	}

	ComputationResult union = responseItem.getTempExtentUnion();
	String t = union.getValue();
	String begin = "";
	String end = "";
	if (t != null && t.contains(" ")) {
	    String[] tSplit = t.split(" ");
	    begin = tSplit[0];
	    end = tSplit[1];
	    coreMetadata.addTemporalExtent(begin, end);
	}

	Optional<ComputationResult> frequency = responseItem.getFrequency(MetadataElement.ORGANISATION_NAME);
	if (frequency.isPresent()) {
	    List<TermFrequencyItem> list = frequency.get().getFrequencyItems();
	    if (!list.isEmpty()) {
		for (TermFrequencyItem item : list.subList(0, Math.min(list.size(), MAX_ORGANISATIONS))) {
		    String org = item.getTerm();
		    ResponsibleParty party = new ResponsibleParty();
		    party.setOrganisationName(org);
		    getOrganisationEmail(source.getUniqueIdentifier(), org).ifPresent(email -> {
			Address address = new Address();
			address.addElectronicMailAddress(email);
			Contact contact = new Contact();
			contact.setAddress(address);
			party.setContactInfo(contact);
		    });
		    coreMetadata.getDataIdentification().addPointOfContact(party);
		}
	    }
	}

	// license URIs of the children, sorted to keep the generated record (and its integrity checksum) stable
	Optional<ComputationResult> licenses = responseItem.getFrequency(MetadataElement.USE_LEGAL_CONSTRAINTS_URI);
	if (licenses.isPresent()) {
	    TreeSet<String> uris = new TreeSet<>();
	    for (TermFrequencyItem item : licenses.get().getFrequencyItems()) {
		String uri = item.getTerm();
		if (uri != null && !uri.isBlank()) {
		    uris.add(uri.trim());
		}
	    }
	    if (!uris.isEmpty()) {
		LegalConstraints legalConstraints = new LegalConstraints();
		for (String uri : uris) {
		    legalConstraints.addOtherConstraints(uri, uri);
		}
		coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(legalConstraints);
	    }
	}

	// time resolutions of the children, from the finest: the WCMP2 record provides the finest as time.resolution and
	// the second finest as additionalExtents.temporal.resolution, while the abstract lists all of them
	List<String> resolutions = getTimeResolutions(responseItem);
	resolutions.forEach(dataset.getExtensionHandler()::addWISTimeResolution);

	List<ComputationResult> count = responseItem.getCountDistinct();
	ComputationResult attributes = count.get(0);
	ComputationResult platforms = count.get(1);

	String time = "";

	if (begin != null && end != null && !begin.isEmpty() && !end.isEmpty()) {
	    Date beginDate = ISO8601DateTimeUtils.parseISO8601(begin);
	    Date endDate = ISO8601DateTimeUtils.parseISO8601(end);
	    String beginString = ISO8601DateTimeUtils.getISO8601Date(beginDate);
	    String endString = ISO8601DateTimeUtils.getISO8601Date(endDate);
	    time = "Temporal range: from " + beginString + " to " + endString + ". ";
	}

	String timeResolutions = "";
	if (!resolutions.isEmpty()) {
	    timeResolutions = " Time resolutions: " + String.join(", ", resolutions) + ".";
	}

	coreMetadata.setAbstract("Data provider: " + source.getLabel() + ". " + time + "Number of platforms: " + platforms.getValue() + "."
		+ timeResolutions);

	addAdditionalElements(dataset, source.getUniqueIdentifier(), groupBy);

	return dataset;
    }

    /**
     * @param responseItem
     * @return the distinct time resolutions of the grouped datasets, as normalized ISO 8601 durations sorted from the
     *         finest. Invalid and non positive durations are discarded
     */
    private List<String> getTimeResolutions(ResponseItem responseItem) {

	// calendar based durations (e.g. P1M) have no fixed length: they are compared from a reference date
	Date reference = new Date(0);
	TreeMap<Long, String> sorted = new TreeMap<>();

	Optional<ComputationResult> frequency = responseItem.getFrequency(MetadataElement.TIME_RESOLUTION_DURATION_8601);
	if (frequency.isPresent()) {
	    for (TermFrequencyItem item : frequency.get().getFrequencyItems()) {
		String term = item.getTerm();
		if (term == null || term.isBlank()) {
		    continue;
		}
		try {
		    String resolution = ISO8601DateTimeUtils.normalizeISO8601Duration(term.trim());
		    long millis = ISO8601DateTimeUtils.getDuration(resolution).getTimeInMillis(reference);
		    if (millis > 0) {
			sorted.putIfAbsent(millis, resolution);
		    }
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).warn("Discarded time resolution {}: {}", term, e.getMessage());
		}
	    }
	}

	return new ArrayList<>(sorted.values());
    }

    /**
     * @param sourceId
     * @param organisationName
     * @return the e-mail address of the given organisation, from the first of its datasets in the given source where
     *         the organisation has one
     */
    private Optional<String> getOrganisationEmail(String sourceId, String organisationName) {

	return organisationEmails.computeIfAbsent(organisationName, name -> {

	    try {

		DiscoveryMessage discoveryMessage = new DiscoveryMessage();
		discoveryMessage.setRequestId(UUID.randomUUID().toString());
		discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
		discoveryMessage.getResourceSelector().setSubset(ResourceSubset.FULL);
		discoveryMessage.setPage(new Page(1, MAX_EMAIL_DATASETS));
		discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
		discoveryMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());

		// the datasets of the organisation, excluding the collections
		discoveryMessage.setUserBond(BondFactory.createAndBond(//
			BondFactory.createSourceIdentifierBond(sourceId), //
			BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ORGANISATION_NAME, name), //
			BondFactory.createMissingSimpleValueBond(MetadataElement.WIS_TOPIC_HIERARCHY)));

		ServiceLoader<DiscoveryExecutor> loader = ServiceLoader.load(DiscoveryExecutor.class);
		ResultSet<GSResource> resultSet = loader.iterator().next().retrieve(discoveryMessage);

		for (GSResource resource : resultSet.getResultsList()) {
		    for (ResponsibleParty party : getParties(resource)) {
			if (party != null && name.equalsIgnoreCase(Objects.toString(party.getOrganisationName(), "").trim())) {
			    Optional<String> email = getEmail(party);
			    if (email.isPresent()) {
				return email;
			    }
			}
		    }
		}

	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error("Unable to find the e-mail of {}: {}", name, e.getMessage(), e);
	    }

	    return Optional.empty();
	});
    }

    /**
     * @param resource
     * @return the parties of the given resource indexed as its organisations
     */
    private List<ResponsibleParty> getParties(GSResource resource) {

	List<ResponsibleParty> parties = new ArrayList<>();
	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	Iterator<ResponsibleParty> contacts = miMetadata.getContacts();
	if (contacts != null) {
	    contacts.forEachRemaining(parties::add);
	}
	for (Identification identification : miMetadata.getIdentifications()) {
	    identification.getPointOfContacts().forEachRemaining(parties::add);
	    parties.addAll(identification.getCitedParty());
	}
	if (miMetadata.getDistribution() != null) {
	    parties.addAll(miMetadata.getDistribution().getDistributorParties());
	}
	return parties;
    }

    /**
     * @param party
     * @return the e-mail address of the given party, without the possible mailto: prefix
     */
    private Optional<String> getEmail(ResponsibleParty party) {

	Contact contact = party.getContact();
	if (contact == null || contact.getAddress() == null) {
	    return Optional.empty();
	}
	String email = contact.getAddress().getElectronicMailAddress();
	if (email == null) {
	    return Optional.empty();
	}
	email = email.trim().replaceFirst("(?i)^mailto:", "");
	return email.contains("@") && !email.contains(" ") ? Optional.of(email) : Optional.empty();
    }

    protected String getTitle(GSSource source, String parameter) {
	return "Observations from " + normalize(source.getLabel());
    }

    protected String normalize(String label) {
	String cleaned = label.replaceAll("[^a-zA-Z0-9()\\s]", "");
	return cleaned;
    }

    protected void addAdditionalElements(DatasetCollection dataset, String sourceId, String groupBy) throws GSException {

	HashSet<PropertyResult> observedProperties = getProperties(sourceId);

	addProperties(dataset, observedProperties);

    }

    public void addProperties(DatasetCollection dataset, HashSet<PropertyResult> observedProperties) {
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	MIMetadata miMetadata = coreMetadata.getMIMetadata();
	HashSet<String> propertyNames = new HashSet<>();
	HashSet<String> propertyURIs = new HashSet<>();
	String properties = "";
	Keywords whosKeywords = null;

	Keywords wmoKeywords = null;
	WHOSOntology ontology = new WHOSOntology();
	miMetadata.getDataIdentification().addKeyword("DAB");

	// sorted, so that keywords and abstract (and the integrity checksum of the WIS2 notification) are stable among runs
	List<PropertyResult> sortedProperties = new ArrayList<>(observedProperties);
	sortedProperties.sort(Comparator.comparing(PropertyResult::getName, Comparator.nullsLast(Comparator.naturalOrder()))
		.thenComparing(PropertyResult::getUri, Comparator.nullsLast(Comparator.naturalOrder())));

	for (PropertyResult property : sortedProperties) {
	    if (property.getUri() != null) {
		if (!propertyURIs.contains(property.getUri())) {
		    List<SKOSConcept> concepts = ontology.findConcepts(property.getUri(), true, false);
		    if (!concepts.isEmpty()) {
			for (SKOSConcept concept : concepts) {
			    if (concept != null && concept.getURI() != null) {
				if (concept.getURI().contains("codes.wmo.int/wmdr")) {
				    if (wmoKeywords == null) {
					wmoKeywords = new Keywords();
					wmoKeywords.setThesaurusNameCitationTitle(
						concept.getURI().substring(0, concept.getURI().lastIndexOf("/")));
				    }
				    wmoKeywords.addKeyword(concept.getPreferredLabel("en"), concept.getURI());
				} else if (concept.getURI().contains("hydro.geodab.eu")) {
				    if (whosKeywords == null) {
					whosKeywords = new Keywords();
					whosKeywords.setThesaurusNameCitationTitle("http://hydro.geodab.eu/hydro-ontology/");
				    }
				    whosKeywords.addKeyword(concept.getPreferredLabel("en"), concept.getURI());
				}
				dataset.getExtensionHandler().setObservedPropertyURI(concept.getURI());

			    }
			}
		    }
		    propertyURIs.add(property.getUri());

		}
	    }
	    if (property.getName() != null) {
		if (!propertyNames.contains(property.getName())) {

		    properties += property.getName() + ", ";
		    propertyNames.add(property.getName());
		    CoverageDescription coverageDescription = new CoverageDescription();
		    coverageDescription.setAttributeTitle(property.getName());
		    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
		}
	    }
	}

	if (wmoKeywords != null) {
	    miMetadata.getDataIdentification().addKeywords(wmoKeywords);
	}
	if (whosKeywords != null) {
	    miMetadata.getDataIdentification().addKeywords(whosKeywords);
	}
	if (!properties.isEmpty()) {
	    properties = properties.substring(0, properties.length() - 2);
	}
	String abs = coreMetadata.getAbstract();
	String props = "";
	if (!propertyNames.isEmpty()) {
	    props = "Number of observed properties: " + propertyNames.size() + " (" + properties + ").";
	}
	coreMetadata.setAbstract((abs + " " + props).trim());

    }

    protected String getMetadataIdentifier(String sourceIdentifier, String groupBy) {
	return WISUtils.getMetadataIdentifier(centreId, sourceIdentifier, null);
    }

    protected List<ResponseItem> getStatistics(String sourceId, Queryable queryable) throws GSException {
	StatisticsMessage statisticsMessage = new StatisticsMessage();
	// set the required properties
	statisticsMessage.setSources(new ArrayList<GSSource>(Arrays.asList(ConfigurationWrapper.getSource(sourceId))));
	statisticsMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
	// statisticsMessage.setSharedRepositoryInfo(ConfigurationUtils.getSharedRepositoryInfo());

	ServiceLoader<StatisticsExecutor> loader = ServiceLoader.load(StatisticsExecutor.class);
	StatisticsExecutor executor = loader.iterator().next();

	// pagination works with grouped results. in this case there is one result item for each
	// source/country/etc.
	// in order to be sure to get all the items in the same statistics response,
	// we set the count equals to number of sources
	Page page = new Page();
	page.setStart(1);
	page.setSize(1000);

	statisticsMessage.setPage(page);

	// computes union of bboxes
	statisticsMessage.computeBboxUnion();

	statisticsMessage.computeTempExtentUnion();

	statisticsMessage.computeFrequency(Arrays.asList(MetadataElement.ORGANISATION_NAME, MetadataElement.USE_LEGAL_CONSTRAINTS_URI,
		MetadataElement.OBSERVED_PROPERTY_URI, MetadataElement.TIME_RESOLUTION_DURATION_8601), MAX_FREQUENCY_ITEMS);
	// computes count distinct of 2 queryables
	statisticsMessage.countDistinct(//
		Arrays.asList(//
			MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, //
			MetadataElement.UNIQUE_PLATFORM_IDENTIFIER));

	statisticsMessage.computeSum(Arrays.asList(MetadataElement.DATA_SIZE));

	Bond missing = BondFactory.createMissingSimpleValueBond(MetadataElement.WIS_TOPIC_HIERARCHY);
	statisticsMessage.setUserBond(BondFactory.createAndBond(missing, BondFactory.createSourceIdentifierBond(sourceId)));

	statisticsMessage.groupBy(queryable);

	return executor.compute(statisticsMessage).getItems();
    }

    public class PropertyResult {
	private String name;
	private String uri;

	public String getName() {
	    return name;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public String getUri() {
	    return uri;
	}

	public void setUri(String uri) {
	    this.uri = uri;
	}

	@Override
	public boolean equals(Object obj) {
	    if (obj instanceof PropertyResult) {
		PropertyResult other = (PropertyResult) obj;
		if (Objects.equals(getName(), other.getName())) {
		    if (Objects.equals(getUri(), other.getUri())) {
			return true;
		    }
		}
	    }
	    return false;
	}

	@Override
	public int hashCode() {
	    return Objects.hash(getName(), getUri());
	}

    }

    protected HashSet<PropertyResult> getProperties(String sourceId) throws GSException {
	HashSet<PropertyResult> ret = new HashSet<>();
	ServiceLoader<DiscoveryExecutor> loader = ServiceLoader.load(DiscoveryExecutor.class);
	DiscoveryExecutor executor = loader.iterator().next();

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();

	discoveryMessage.setRequestId(UUID.randomUUID().toString());

	discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	discoveryMessage.getResourceSelector().setSubset(ResourceSubset.FULL);
	discoveryMessage.setPage(new Page(1, 1000));
	discoveryMessage.setIteratedWorkflow(IterationMode.FULL_RESPONSE);
	discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
	StorageInfo uri = ConfigurationWrapper.getStorageInfo();
	discoveryMessage.setDataBaseURI(uri);

	discoveryMessage.setUserBond(BondFactory.createSourceIdentifierBond(sourceId));
	discoveryMessage.setDistinctValuesElement(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER);
	ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);
	List<GSResource> resources = resultSet.getResultsList();
	for (int i = 0; i < resources.size(); i++) {
	    GSResource resource = resources.get(i);
	    PropertyResult r = new PropertyResult();
	    Iterator<CoverageDescription> descriptions = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .getCoverageDescriptions();
	    while (descriptions.hasNext()) {
		CoverageDescription next = descriptions.next();
		String description = next.getAttributeTitle();
		r.setName(description);
	    }
	    Optional<String> prop = resource.getExtensionHandler().getObservedPropertyURI();
	    if (prop.isPresent()) {
		r.setUri(prop.get());
	    }
	    ret.add(r);
	}

	return ret;
    }
}
