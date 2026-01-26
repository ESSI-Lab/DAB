package eu.essi_lab.profiler.oaipmh.handler.discover;

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

import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.*;
import eu.essi_lab.messages.ResourceSelector.*;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.messages.web.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.model.pluggable.*;
import eu.essi_lab.model.resource.*;
import eu.essi_lab.pdk.wrt.*;
import eu.essi_lab.profiler.oaipmh.*;
import eu.essi_lab.profiler.oaipmh.OAIPMHProfilerSetting.*;
import eu.essi_lab.profiler.oaipmh.token.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class OAIPMHRequestTransformer extends DiscoveryRequestTransformer {

    /**
     *
     */
    private static final String OAI_PMH_POST_QUERY_EXTRACTION_ERROR = "OAI_PMH_POST_QUERY_EXTRACTION_ERROR";

    /**
     *
     */
    public OAIPMHRequestTransformer() {
    }

    /**
     * @param setting
     */
    public OAIPMHRequestTransformer(OAIPMHProfilerSetting setting) {

	super(setting);
    }

    @Override
    protected DiscoveryMessage refineMessage(DiscoveryMessage message) throws GSException {

	DiscoveryMessage refinedMessage = super.refineMessage(message);

	refinedMessage.setSortedFields(SortedFields.of(ResourceProperty.RESOURCE_TIME_STAMP, SortOrder.ASCENDING));

	OAIPMHRequestReader reader = createReader(message.getWebRequest());
	String tokenValue = reader.getResumptionToken();

	if (tokenValue != null) {

	    ResumptionToken resumptionToken = ResumptionToken.of(tokenValue);
	    Optional<String> searchAfter = resumptionToken.getSearchAfter();

	    searchAfter.ifPresent(s -> refinedMessage.setSearchAfter(SearchAfter.of(s)));
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

		GSResource resource = resultSet.getResultsList().getFirst();
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

	return getSetting().get(). //
		readKeyValue(KeyValueOptionKeys.PAGE_SIZE.getLabel()).//
		map(Integer::parseInt).//
		orElse(getDefaultPageSize());
    }

    /**
     * If the "set" parameter in the request is set, the returned bond is a single {@link SourceIdentifierBond} built with the
     * <code>setSpec</code> value. If the "set" parameter in the request is not set, the returned bond is an OR logical bond with
     * {@link SourceIdentifierBond} bonds built with the identifiers of the harvested sources. If there are no harvested sources, the
     * returned bond is a bond which forces the request to return an empty result set.
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
		setBond = BondFactory.createSourceIdentifierBond(ids.getFirst());
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
		map(GSSource::getUniqueIdentifier).//
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
