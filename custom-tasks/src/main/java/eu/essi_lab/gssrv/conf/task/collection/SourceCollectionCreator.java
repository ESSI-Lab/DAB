package eu.essi_lab.gssrv.conf.task.collection;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WHOSOntology;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
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
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import eu.essi_lab.request.executor.IStatisticsExecutor;

public class SourceCollectionCreator {

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
	String country = "unknown";
	String sourceId = source.getUniqueIdentifier();
	String sourceAcronym = sourceId;
	if (sourceId.contains("-")) {
	    int dashIndex = sourceId.indexOf("-");
	    String c = sourceId.substring(0, dashIndex);
	    Country cc = Country.decode(c);
	    if (cc != null) {
		country = cc.getISO2().toLowerCase();
		sourceAcronym = sourceId.substring(dashIndex + 1);
	    }
	}
	// GET Country!
	String topic = "origin/a/wis2/" + //
		"it-cnr-iia"
		// country + "-" + sourceAcronym //
		+ "/metadata"; // only at this level
	// core/hydrology" + getAdditionalLevels(groupBy);

	dataset.getExtensionHandler().setWISTopicHierarchy(topic);

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
		for (TermFrequencyItem item : list) {
		    String org = item.getTerm();
		    ResponsibleParty party = new ResponsibleParty();
		    party.setOrganisationName(org);
		    coreMetadata.getDataIdentification().addPointOfContact(party);
		}
	    }
	}

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

	coreMetadata
		.setAbstract("Data provider: " + source.getLabel() + ". " + time + "Number of platforms: " + platforms.getValue() + ".");

	addAdditionalElements(dataset, source.getUniqueIdentifier(), groupBy);

	return dataset;
    }

    protected String getAdditionalLevels(String groupBy) {
	return "";
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

	for (PropertyResult property : observedProperties) {
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
				    wmoKeywords.addKeyword(concept.getPreferredLabel(""), concept.getURI());
				} else if (concept.getURI().contains("hydro.geodab.eu")) {
				    if (whosKeywords == null) {
					whosKeywords = new Keywords();
					whosKeywords.setThesaurusNameCitationTitle("http://hydro.geodab.eu/hydro-ontology/");
				    }
				    whosKeywords.addKeyword(concept.getPreferredLabel(""), concept.getURI());
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
	coreMetadata.setAbstract((abs + props).trim());

    }

    protected String getMetadataIdentifier(String sourceIdentifier, String groupBy) {
	return "urn:wmo:md:it-cnr-iia:" + sourceIdentifier ;
    }

    protected List<ResponseItem> getStatistics(String sourceId, Queryable queryable) throws GSException {
	StatisticsMessage statisticsMessage = new StatisticsMessage();
	// set the required properties
	statisticsMessage.setSources(new ArrayList<GSSource>(Arrays.asList(ConfigurationWrapper.getSource(sourceId))));
	statisticsMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
	// statisticsMessage.setSharedRepositoryInfo(ConfigurationUtils.getSharedRepositoryInfo());

	ServiceLoader<IStatisticsExecutor> loader = ServiceLoader.load(IStatisticsExecutor.class);
	IStatisticsExecutor executor = loader.iterator().next();

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

	statisticsMessage.computeFrequency(Arrays.asList(MetadataElement.ORGANISATION_NAME));
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

    }

    protected HashSet<PropertyResult> getProperties(String sourceId) throws GSException {
	HashSet<PropertyResult> ret = new HashSet<>();
	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

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
