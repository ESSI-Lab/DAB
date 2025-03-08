package eu.essi_lab.profiler.oaipmh.handler.discover;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.OrderingDirection;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.oaipmh.OAIPMHProfiler;
import eu.essi_lab.profiler.oaipmh.OAIPMHProfilerSetting;
import eu.essi_lab.profiler.oaipmh.OAIPMHRequestReader;
import eu.essi_lab.profiler.oaipmh.OAIPMHRequestValidator;
import eu.essi_lab.profiler.oaipmh.OAIPMRequestFilter;
import eu.essi_lab.profiler.oaipmh.token.ResumptionToken;

/**
 * @author Fabrizio
 */
public class OAIPMHRequestTransformer extends DiscoveryRequestTransformer {

    /**
     * 
     */
    private static final String OAI_PMH_POST_QUERY_EXTRACTION_ERROR = "OAI_PMH_POST_QUERY_EXTRACTION_ERROR";
    private OAIPMHProfilerSetting setting;

    /**
     * @param setting
     */
    public OAIPMHRequestTransformer(OAIPMHProfilerSetting setting) {

	this.setting = setting;
    }

    @Override
    protected DiscoveryMessage refineMessage(DiscoveryMessage message) throws GSException {

	DiscoveryMessage refinedMessage = super.refineMessage(message);

	if (sortResults()) {
	    refinedMessage.setOrderingDirection(OrderingDirection.ASCENDING);
	    refinedMessage.setOrderingProperty(ResourceProperty.PRIVATE_ID);
	}

	OAIPMHRequestReader reader = createReader(message.getWebRequest());
	String tokenValue = reader.getResumptionToken();

	if (tokenValue != null) {

	    ResumptionToken resumptionToken = ResumptionToken.of(tokenValue);
	    Optional<String> searchAfter = resumptionToken.getSearchAfter();

	    if (searchAfter.isPresent()) {
		refinedMessage.setSearchAfter(SearchAfter.of(searchAfter.get()));
	    }
	}

	return refinedMessage;
    }

    /**
     * @return
     */
    protected int getDefaultPageSize() {

	return 50;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	OAIPMHRequestValidator validator = new OAIPMHRequestValidator();
	return validator.validate(request);

    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(ResourceSubset.FULL);
	selector.addIndex(ResourceProperty.IS_DELETED);
	selector.addIndex(ResourceProperty.OAI_PMH_HEADER_ID);
	selector.addIndex(ResourceProperty.RESOURCE_TIME_STAMP);

	return selector;
    }

    @Override
    protected Bond getUserBond(WebRequest request) throws GSException {

	OAIPMHRequestReader reader = createReader(request);

	String setSpec = null;
	String from = null;
	String until = null;

	String tokenValue = reader.getResumptionToken();
	if (tokenValue != null) {

	    ResumptionToken rt = ResumptionToken.of(tokenValue);
	    from = rt.getFrom();
	    until = rt.getUntil();
	    setSpec = rt.getSet();

	} else {
	    setSpec = reader.getSet();
	    from = reader.getFrom();
	    until = reader.getUntil();
	}

	// ------------------------------------------------------------------
	//
	// this avoids potential (but very unlikely!) never ending harvesting
	//
	//
	if (until == null) {
	    until = ISO8601DateTimeUtils.getISO8601DateTime();
	}

	String identifier = reader.getIdentifier();

	Bond bond = null;

	if (identifier == null) {

	    Bond fBond = null;
	    if (from != null) {
		fBond = BondFactory.createResourceTimeStampBond(BondOperator.GREATER_OR_EQUAL, String.valueOf(from));
	    }

	    Bond uBond = BondFactory.createResourceTimeStampBond(BondOperator.LESS_OR_EQUAL, String.valueOf(until));
	    Bond setBond = createSetBond(setSpec);

	    if (setBond != null) {

		// set + until
		if (fBond == null) {

		    bond = BondFactory.createAndBond(setBond, uBond);
		}
		// set + from + until
		else {

		    bond = BondFactory.createAndBond(setBond, fBond, uBond);
		}
	    } else {

		// from + until
		if (fBond != null) {

		    bond = BondFactory.createAndBond(fBond, uBond);
		}
		// until
		else {

		    bond = uBond;
		}
	    }
	} else {

	    LogicalBond orBond = BondFactory.createOrBond();

	    SimpleValueBond idBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, identifier);
	    ResourcePropertyBond oaiBond = BondFactory.createOAIPMHHeaderIdentifierBond(identifier);

	    orBond.getOperands().add(idBond);
	    orBond.getOperands().add(oaiBond);

	    bond = orBond;
	}

	return bond;
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	OAIPMHRequestReader reader = createReader(request);
	String tokenValue = reader.getResumptionToken();
	if (tokenValue == null) {
	    return new Page(getPageSize());
	}

	ResumptionToken rt = ResumptionToken.of(tokenValue);
	int adv = rt.getAdvancement();

	return new Page(adv, getPageSize());
    }

    static OAIPMHRequestReader createReader(WebRequest request) throws GSException {

	String queryString = null;
	if (request.isPostRequest()) {

	    InputStream stream = request.getBodyStream().clone();
	    try {
		queryString = OAIPMRequestFilter.extractQueryString(stream);
	    } catch (IOException e) {

		throw GSException.createException(OAIPMHProfiler.class, e.getMessage(), null, ErrorInfo.ERRORTYPE_INTERNAL,
			ErrorInfo.SEVERITY_ERROR, OAI_PMH_POST_QUERY_EXTRACTION_ERROR, e);
	    }
	} else {
	    queryString = request.getQueryString();
	}

	KeyValueParser keyValueParser = new KeyValueParser(queryString, true);
	return new OAIPMHRequestReader(keyValueParser);
    }

    public static String getMinMaxDateStamp(String requestId, BondOperator operator, String setIdentifier) throws GSException {

	Bond bond = null;
	if (setIdentifier != null) {
	    bond = BondFactory.createMinMaxResourceTimeStampBond(operator, setIdentifier);
	} else {
	    bond = BondFactory.createMinMaxResourceTimeStampBond(operator);
	}

	DiscoveryMessage message = new DiscoveryMessage();
	message.setPage(new Page(1));
	message.setPermittedBond(bond);
	message.setIncludeDeleted(true);

	ResourceSelector selector = new ResourceSelector();
	selector.setIncludeOriginal(false);
	selector.setSubset(ResourceSubset.NO_CORE);
	selector.addIndex(ResourceProperty.RESOURCE_TIME_STAMP);
	message.setResourceSelector(selector);

	StorageInfo uri = ConfigurationWrapper.getStorageInfo();
	GSLoggerFactory.getLogger(OAIPMHRequestTransformer.class).debug("Storage uri: {}", uri);

	DatabaseFinder finder = DatabaseProviderFactory.getFinder(uri);

	try {

	    GSLoggerFactory.getLogger(OAIPMHRequestTransformer.class).debug("Discovering of " + operator + " resource time stamp STARTED");

	    ResultSet<GSResource> resultSet = finder.discover(message);

	    GSLoggerFactory.getLogger(OAIPMHRequestTransformer.class).debug("Discovering of " + operator + " resource time stamp ENDED");

	    if (!resultSet.getResultsList().isEmpty()) {

		GSResource resource = resultSet.getResultsList().get(0);
		Optional<String> ts = resource.getPropertyHandler().getResourceTimeStamp();

		if (ts.isPresent()) {

		    GSLoggerFactory.getLogger(OAIPMHRequestTransformer.class).debug(operator + " resource time stamp: " + ts.get());

		    return ts.get();
		}

		GSLoggerFactory.getLogger(OAIPMHRequestTransformer.class).warn("Can't read min max date stamp from DB");

	    } else {

		GSLoggerFactory.getLogger(OAIPMHRequestTransformer.class).warn("{} datestamp not found, using the current one", operator);
	    }
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(OAIPMHRequestTransformer.class).warn(e.getMessage(), e);
	}

	return ISO8601DateTimeUtils.getISO8601DateTime();
    }

    public static String getMinMaxDateStamp(String requestId, BondOperator operator) throws GSException {

	return getMinMaxDateStamp(requestId, operator, null);
    }

    /**
     * @return
     */
    private int getPageSize() {
    
        Optional<Properties> properties = setting.getKeyValueOptions();
        int pageSize = getDefaultPageSize();
        if (properties.isPresent()) {
    
            pageSize = Integer.valueOf(properties.get().getProperty("pageSize", String.valueOf(getDefaultPageSize())));
        }
    
        return pageSize;
    }

    /**
     * @return
     */
    private boolean sortResults() {

	Optional<Properties> properties = setting.getKeyValueOptions();
	boolean sortResults = false;
	if (properties.isPresent()) {

	    sortResults = Boolean.valueOf(properties.get().getProperty("sortResults", "false"));
	}

	return sortResults;
    }

    /**
     * If the "set" parameter in the request is set, the returned bond is a single {@link SourceIdentifierBond} built
     * with the <code>setSpec</code> value. If the "set" parameter in the request is not set, the returned bond is an OR
     * logical bond with {@link SourceIdentifierBond} bonds built with the identifiers of the harvested sources. If
     * there are no harvested sources, the returned bond is a bond which forces the request to return an empty result
     * set.
     *
     * @param setSpec
     * @return
     * @throws GSException
     */
    private Bond createSetBond(String setSpec) throws GSException {

	Bond setBond = null;

	if (setSpec != null) {

	    setBond = BondFactory.createSourceIdentifierBond(setSpec);

	} else {

	    List<String> ids = getHarvestedSourcesIds();

	    if (ids.size() == 1) {
		setBond = BondFactory.createSourceIdentifierBond(ids.get(0));
	    } else {
		setBond = BondFactory.createOrBond();
		for (String sourceId : ids) {
		    ((LogicalBond) setBond).getOperands().add(BondFactory.createSourceIdentifierBond(sourceId));
		}
	    }
	}

	return setBond;
    }

    private List<String> getHarvestedSourcesIds() throws GSException {

	return ConfigurationWrapper.getHarvestedSources().//
		stream().//
		map(s -> s.getUniqueIdentifier()).//
		collect(Collectors.toList());
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String getProfilerType() {

	return new OAIPMHProfilerSetting().getServiceType();
    }
}
