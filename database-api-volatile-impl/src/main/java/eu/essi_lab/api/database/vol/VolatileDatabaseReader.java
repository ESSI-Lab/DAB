/**
 * 
 */
package eu.essi_lab.api.database.vol;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.GetViewIdentifiersRequest;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.UserBondMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.parser.IdentifierBondHandler;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.termfrequency.TermFrequencyMapType;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class VolatileDatabaseReader implements DatabaseReader {

    private VolatileDatabase database;
    private StorageInfo dbUri;

    @Override
    public boolean supports(StorageInfo dbUri) {

	this.dbUri = dbUri;

	return dbUri.getName() != null && //
		dbUri.getName().equals(DatabaseSetting.VOLATILE_DB_STORAGE_NAME);
    }

    @Override
    public void setDatabase(Database db) {

	this.database = (VolatileDatabase) db;
    }

    @Override
    public VolatileDatabase getDatabase() {

	return (VolatileDatabase) this.database;
    }

    @Override
    public Optional<GSUser> getUser(String userName) throws GSException {

	return getDatabase().//
		getUsersList().//
		stream().//
		filter(u -> u.getIdentifier().equals(userName)).//
		findFirst();
    }

    @Override
    public List<GSUser> getUsers() throws GSException {

	return getDatabase().getUsersList();
    }

    @Override
    public Optional<View> getView(String viewId) throws GSException {

	return getDatabase().//
		getViewsList().//
		stream().//
		filter(v -> v.getId().equals(viewId)).//
		findFirst();
    }

    @Override
    public List<String> getViewIdentifiers(GetViewIdentifiersRequest request) throws GSException {

	if (getDatabase().getViewsList().isEmpty()) {

	    return Arrays.asList();
	}

	return getDatabase().//
		getViewsList().//
		subList(request.getStart(), request.getCount()).//
		stream().//
		filter(v -> request.getCreator().isPresent() ? v.getCreator().equals(request.getCreator().get()) : false).//
		map(v -> v.getId()).//
		collect(Collectors.toList());
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    public DiscoveryCountResponse count(DiscoveryMessage message) throws GSException {

	List<GSResource> resourcesList = createResourcesList(message);

	DiscoveryCountResponse response = new DiscoveryCountResponse();

	Map<String, GSSource> sourcesMap = resourcesList.//
		stream().//
		map(r -> r.getSource()).//
		distinct().//
		collect(Collectors.toMap(s -> s.getUniqueIdentifier(), s -> s));

	if (message.isOutputSources()) {

	    response.setCount(sourcesMap.values().size());

	} else {
	    response.setCount(resourcesList.size());
	}

	TermFrequencyMapType mapType = new TermFrequencyMapType();
	TermFrequencyMap termFrequencyMap = new TermFrequencyMap(mapType);

	response.setTermFrequencyMap(termFrequencyMap);

	for (String sourceId : sourcesMap.keySet()) {

	    int count = (int) resourcesList.//
		    stream().//
		    filter(r -> r.getSource().getUniqueIdentifier().equals(sourceId)).//
		    count();

	    TermFrequencyItem termFrequencyItem = new TermFrequencyItem();

	    termFrequencyItem.setTerm(sourceId);
	    termFrequencyItem.setDecodedTerm(sourceId);

	    termFrequencyItem.setLabel(sourceId);
	    termFrequencyItem.setFreq(count);

	    mapType.getSourceId().add(termFrequencyItem);
	}

	return response;
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    public ResultSet<GSResource> discover(DiscoveryMessage message) throws GSException {

	ResultSet<GSResource> resultSet = new ResultSet<>();

	CountSet countSet = new CountSet();

	List<GSResource> resourcesList = createResourcesList(message);

	//
	//
	//

	List<String> sourceIds = resourcesList.//
		stream().//
		map(r -> r.getSource().getUniqueIdentifier()).//
		distinct().//
		collect(Collectors.toList());

	for (String sourceId : sourceIds) {

	    int count = (int) resourcesList.//
		    stream().//
		    filter(r -> r.getSource().getUniqueIdentifier().equals(sourceId)).//
		    count();

	    DiscoveryCountResponse countResponse = new DiscoveryCountResponse();

	    countResponse.setCount(count);

	    SimpleEntry<String, DiscoveryCountResponse> simpleEntry = new SimpleEntry<String, DiscoveryCountResponse>(//
		    sourceId, countResponse);

	    countSet.addCountPair(simpleEntry);
	}

	countSet.setPageCount(message.getPage().getSize());
	countSet.setPageIndex(message.getPage().getStart());

	resultSet.setCountResponse(countSet);

	//
	//
	//

	if (message.isOutputSources()) {

	    List<GSResource> collect = resourcesList.//
		    stream().//
		    filter(StreamUtils.distinctBy(GSResource::getSource)).//
		    collect(Collectors.toList());

	    resultSet.setResultsList(collect);

	} else {

	    int end = (message.getPage().getStart() - 1) + message.getPage().getSize();

	    resultSet.setResultsList(resourcesList.subList(//
		    message.getPage().getStart() - 1, //
		    Math.min(resourcesList.size(), end)));
	}

	return resultSet;
    }

    /**
     * @param message
     * @return
     */
    private List<GSResource> createResourcesList(UserBondMessage message) {

	List<GSResource> resourcesList = getDatabase().//
		getResourcesList().//
		stream().//
		filter(r -> message.getSources().contains(r.getSource())).//
		collect(Collectors.toList());

	Optional<Bond> userBond = message.getUserBond();

	if (userBond.isPresent()) {

	    IdentifierBondHandler parser = new IdentifierBondHandler(userBond.get());

	    if (parser.isCanonicalQueryByIdentifiers()) {

		List<String> identifiers = parser.getIdentifiers();

		resourcesList = resourcesList.//
			stream().//
			filter(r -> identifiers.contains(r.getPublicId())).//
			collect(Collectors.toList());
	    }
	}

	return resourcesList;
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    public ResultSet<Node> discoverNodes(DiscoveryMessage message) throws GSException {

	ResultSet<GSResource> resultSet = discover(message);

	List<Node> docs = resultSet.getResultsList().//
		stream().//
		map(r -> asDocument(r)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	ResultSet<Node> nodesSet = new ResultSet<>();
	nodesSet.setCountResponse(resultSet.getCountResponse());
	nodesSet.setResultsList(docs);

	return nodesSet;
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    public ResultSet<String> discoverStrings(DiscoveryMessage message) throws GSException {

	ResultSet<Node> resultSet = discoverNodes(message);

	List<String> strings = resultSet.getResultsList().//
		stream().//
		map(n -> asString(n)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	ResultSet<String> stringSet = new ResultSet<>();
	stringSet.setCountResponse(resultSet.getCountResponse());
	stringSet.setResultsList(strings);

	return stringSet;
    }

    /**
     * @param node
     * @return
     */
    private String asString(Node node) {

	try {
	    return XMLNodeReader.asString(node);
	} catch (UnsupportedEncodingException | TransformerException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
	}
	return null;
    }

    /**
     * @param resource
     * @return
     */
    private Document asDocument(GSResource resource) {

	try {
	    return resource.asDocument(true);
	} catch (ParserConfigurationException | JAXBException | SAXException | IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
	}
	return null;
    }

    @Override
    public boolean resourceExists(final IdentifierType identifierType, final String identifier) throws GSException {

	return !getResources(identifierType, identifier).isEmpty();
    }

    @Override
    public List<GSResource> getResources(IdentifierType identifierType, String identifier) throws GSException {

	return getDatabase().//
		getResourcesList().//
		stream().//
		filter(r -> {
		    switch (identifierType) {
		    case PUBLIC:
			return r.getPublicId().equals(identifier);
		    case PRIVATE:
			return r.getPrivateId().equals(identifier);
		    case ORIGINAL:
		    default:
			return r.getOriginalId().equals(identifier);
		    }
		}).collect(Collectors.toList());
    }

    @Override
    public boolean resourceExists(String originalIdentifier, GSSource source) throws GSException {

	return getResource(originalIdentifier, source) != null;
    }

    @Override
    public GSResource getResource(String originalIdentifier, GSSource source) throws GSException {

	return getDatabase().//
		getResourcesList().//
		stream().//
		filter(r -> r.getOriginalId().isPresent() //
			&& r.getOriginalId().get().equals(originalIdentifier)
			&& r.getSource().getUniqueIdentifier().equals(source.getUniqueIdentifier()))
		.//
		findFirst().//
		orElse(null);

    }
}
