package eu.essi_lab.request.executor.discover.submitter;

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
import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.adk.distributed.IDistributedAccessor;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.driver.DriverSetting;
import eu.essi_lab.identifierdecorator.IdentifierDecorator;
import eu.essi_lab.lib.servlet.RequestManager;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.parser.IdentifierBondHandler;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;
import eu.essi_lab.request.executor.query.IDistributedQueryExecutor;
import eu.essi_lab.shared.driver.DriverFactory;
import eu.essi_lab.shared.driver.ISharedRepositoryDriver;

public class DistributedQueryExecutor implements IDistributedQueryExecutor {

    /**
     * 
     */
    private static final String IDENTIFIER_DECORATOR_NOT_SET_ERROR = "IDENTIFIER_DECORATOR_NOT_SET";

    private IdentifierDecorator identifierDecorator = null;

    @SuppressWarnings("rawtypes")
    private IDistributedAccessor accessor;
    private String sourceIdentifier;

    @SuppressWarnings("rawtypes")
    public DistributedQueryExecutor(IDistributedAccessor accessor, String id) {
	this.accessor = accessor;
	this.sourceIdentifier = id;
    }

    @Override
    public IdentifierDecorator getIdentifierDecorator() {
	return identifierDecorator;
    }

    @Override
    public void setIdentifierDecorator(IdentifierDecorator identifierDecorator) {
	this.identifierDecorator = identifierDecorator;
    }

    @Override
    public String getSourceIdentifier() {
	return sourceIdentifier;
    }

    @Override
    public SimpleEntry<String, DiscoveryCountResponse> count(ReducedDiscoveryMessage message) throws GSException {

	RequestManager.getInstance().updateThreadName(getClass(), message.getRequestId());

	Bond reducedBond = message.getReducedBond();

	IdentifierBondHandler parser = new IdentifierBondHandler(reducedBond);

	DiscoveryCountResponse countResult;

	if (parser.isCanonicalQueryByIdentifiers()) {
	    ResultSet<GSResource> result = retrieve(message, null);
	    countResult = new DiscoveryCountResponse();
	    countResult.setCount(result.getResultsList().size());

	} else {

	    countResult = accessor.count(message);
	}

	SimpleEntry<String, DiscoveryCountResponse> ret = new SimpleEntry<String, DiscoveryCountResponse>(getSourceIdentifier(),
		countResult);

	return ret;
    }

    @Override
    public ResultSet<Node> retrieveNodes(ReducedDiscoveryMessage message, Page page) throws GSException {
	ResultSet<GSResource> gsResources = retrieve(message, page);
	ResultSet<Node> ret = new ResultSet<>();

	for (GSResource resource : gsResources.getResultsList()) {

	    try {
		Document node = resource.asDocument(true);
		ret.getResultsList().add(node);
	    } catch (ParserConfigurationException | JAXBException | SAXException | IOException e) {
		e.printStackTrace();
	    }
	}

	return ret;
    }

    @Override
    public ResultSet<String> retrieveStrings(ReducedDiscoveryMessage message, Page page) throws GSException {
	ResultSet<GSResource> gsResources = retrieve(message, page);
	ResultSet<String> ret = new ResultSet<>();

	for (GSResource resource : gsResources.getResultsList()) {

	    try {
		String node = resource.asString(true);
		ret.getResultsList().add(node);
	    } catch (JAXBException | IOException e) {
		e.printStackTrace();
	    }
	}

	return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ResultSet<GSResource> retrieve(ReducedDiscoveryMessage message, Page page) throws GSException {

	RequestManager.getInstance().updateThreadName(getClass(), message.getRequestId());

	if (getIdentifierDecorator() == null) {

	    throw GSException.createException(//
		    getClass(), //
		    "Identifier decorator not set in Distributed Query executor", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    IDENTIFIER_DECORATOR_NOT_SET_ERROR);
	}

	ResultSet<GSResource> ret = new ResultSet<>();

	Bond reducedBond = message.getReducedBond();

	IdentifierBondHandler parser = new IdentifierBondHandler(reducedBond);

	if (parser.isCanonicalQueryByIdentifiers()) {

	    return retrieveCached(message);
	}

	ret = accessor.query(message, page);

	List<GSResource> results = ret.getResultsList();

	for (GSResource result : results) {

	    getIdentifierDecorator().decorateDistributedIdentifier(result);

	    DriverSetting setting = ConfigurationWrapper.getSharedCacheDriverSetting();

	    ISharedRepositoryDriver<?> driver = DriverFactory.getConfiguredDriver(setting, true);

	    SharedContent<GSResource> sharedContent = new SharedContent<>();
	    sharedContent.setType(SharedContentType.GS_RESOURCE_TYPE);
	    sharedContent.setIdentifier(result.getPrivateId());
	    sharedContent.setContent(result);

	    driver.store(sharedContent);
	}

	return ret;
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    private ResultSet<GSResource> retrieveCached(ReducedDiscoveryMessage message) throws GSException {

	Bond reducedBond = message.getReducedBond();

	IdentifierBondHandler parser = new IdentifierBondHandler(reducedBond);

	List<String> identifiers = parser.getIdentifiers();

	String idString = "";
	for (String identifier : identifiers) {
	    idString += identifier + ",";
	}

	if (idString.endsWith(",")) {
	    idString = idString.substring(0, idString.length() - 1);
	}

	ResultSet<GSResource> ret = new ResultSet<>();

	for (String identifier : identifiers) {

	    DriverSetting setting = ConfigurationWrapper.getSharedCacheDriverSetting();

	    ISharedRepositoryDriver<?> driver = DriverFactory.getConfiguredDriver(setting, true);

	    @SuppressWarnings("unchecked")
	    SharedContent<GSResource> content = driver.read(identifier, SharedContentType.GS_RESOURCE_TYPE);

	    if (content == null) {

		return ret;
	    }

	    GSResource resource = content.getContent();

	    String sourceId = null;
	    GSSource source = resource.getSource();

	    if (source != null) {

		sourceId = source.getUniqueIdentifier();
	    }

	    if (sourceId == null || (sourceIdentifier.equals(sourceId))) {

		ret.getResultsList().add(resource);
	    }
	}

	return ret;
    }

    @Override
    public Type getType() {

	return Type.DISTRIBUTED;
    }

}
