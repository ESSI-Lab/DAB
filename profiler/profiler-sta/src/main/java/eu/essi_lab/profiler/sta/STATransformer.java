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

package eu.essi_lab.profiler.sta;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.profiler.sta.STARequest.EntitySet;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;

/**
 * Transforms OGC STA WebRequest to DiscoveryMessage.
 */
public abstract class STATransformer extends DiscoveryRequestTransformer {

    private static final int DEFAULT_TOP = 100;

    public STATransformer() {
    }

    @Override
    protected Bond getUserBond(WebRequest request) throws GSException {
	STARequest staRequest = new STARequest(request);
	Set<Bond> operands = new HashSet<>();

	// we are interested only on downloadable datasets
	ResourcePropertyBond accessBond = BondFactory.createIsExecutableBond(true);
	operands.add(accessBond);

	// we are interested only on downloadable datasets
	ResourcePropertyBond downBond = BondFactory.createIsDownloadableBond(true);
	operands.add(downBond);

	// we are interested only on TIME SERIES datasets
	ResourcePropertyBond timeSeriesBond = BondFactory.createIsTimeSeriesBond(true);
	operands.add(timeSeriesBond);


	String filter = staRequest.getFilter();
	if (filter != null && !filter.isEmpty()) {
	    applyFilterBonds(filter, operands);
	}

	String platformCode = request.extractQueryParameter("platformCode").orElse(null);
	if (platformCode != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, platformCode));
	}
	staRequest.getEntityIdNormalized().ifPresent(id -> {
	    EntitySet es = staRequest.getEntitySet().orElse(null);
	    if (es == EntitySet.Observations || es == EntitySet.Datastreams) {
		operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ONLINE_ID, id));
	    } else if (es == EntitySet.ObservedProperties && !id.matches("\\d+")) {
		operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, id));
	    } else if (es != EntitySet.ObservedProperties) {
		operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, id));
	    }
	});
	String platformName = request.extractQueryParameter("platformName").orElse(null);
	if (platformName != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.PLATFORM_TITLE, platformName));
	}
	String observedProperty = request.extractQueryParameter("observedProperty").orElse(null);
	if (observedProperty != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.ATTRIBUTE_TITLE, observedProperty));
	}
	String begin = request.extractQueryParameter("begin").orElse(null);
	if (begin != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.TEMP_EXTENT_END, begin));
	}
	String end = request.extractQueryParameter("end").orElse(null);
	if (end != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.LESS_OR_EQUAL, MetadataElement.TEMP_EXTENT_BEGIN, end));
	}

	switch (operands.size()) {
	case 0:
	    return null;
	case 1:
	    return operands.iterator().next();
	default:
	    return BondFactory.createAndBond(operands);
	}
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {
	STARequest staRequest = new STARequest(request);
	int skip = staRequest.getSkip() != null ? staRequest.getSkip() : 0;
	int top = staRequest.getTop() != null ? staRequest.getTop() : DEFAULT_TOP;
	return new Page(skip + 1, top);
    }

    @Override
    protected DiscoveryMessage refineMessage(DiscoveryMessage message) throws GSException {
	DiscoveryMessage refined = super.refineMessage(message);
	STARequest staRequest = new STARequest(refined.getWebRequest());
	String resumptionToken = staRequest.getResumptionToken();
	if (resumptionToken != null && !resumptionToken.isEmpty()) {
	    refined.setSearchAfter(SearchAfter.of(resumptionToken));
	}
	return refined;
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {
	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(ResourceSubset.CORE_EXTENDED);
	selector.setIndexesPolicy(IndexesPolicy.ALL);
	selector.setIncludeOriginal(false);
	selector.addIndex(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
	selector.addIndex(MetadataElement.PLATFORM_TITLE);
	selector.addIndex(MetadataElement.PLATFORM_IDENTIFIER);
	selector.addIndex(ResourceProperty.SOURCE_ID);
	selector.addIndex(MetadataElement.BOUNDING_BOX);
	selector.addIndex(MetadataElement.COUNTRY);
	selector.addIndex(MetadataElement.ONLINE_ID);
	return selector;
    }

    @Override
    public String getProfilerType() {
	return "STA";
    }

    @Override
    public Provider getProvider() {
	return new ESSILabProvider();
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage vm = new ValidationMessage();
	vm.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return vm;
    }

    /**
     * Parses $filter and adds corresponding bonds. Supports:
     * <ul>
     * <li>location/type eq 'Point' or location/geometry/type eq 'Point' → createIsTimeSeriesBond(true)</li>
     * <li>location/type eq 'X' or location/geometry/type eq 'X' (X != Point) → getFalseBond()</li>
     * </ul>
     * Subclasses add entity-specific filters (Entity/id eq X, name eq, etc.) via {@link #applyEntitySpecificFilterBonds}.
     */
    private void applyFilterBonds(String filter, Set<Bond> operands) {
	String f = decodeFilter(filter);

	Bond filterBond = STAFilterParser.parse(f);
	if (filterBond != null) {
	    operands.add(filterBond);
	}

	applyEntitySpecificFilterBonds(f, operands);
    }

    private static String decodeFilter(String filter) {
	if (filter == null || filter.isEmpty()) {
	    return filter != null ? filter : "";
	}
	try {
	    return URLDecoder.decode(filter.trim(), StandardCharsets.UTF_8);
	} catch (IllegalArgumentException e) {
	    return filter.trim();
	}
    }

    /**
     * Hook for entity-specific $filter bonds. Override in subclasses for Entity/id eq X, name eq, etc.
     */
    protected void applyEntitySpecificFilterBonds(String filter, Set<Bond> operands) {
    }

    /**
     * Helper for subclasses: adds bond when filter contains EntityName/id eq X (X can be number or quoted string).
     */
    protected void addEntityIdFilter(String filter, String entityName, MetadataElement element, Set<Bond> operands) {
	Pattern p = Pattern.compile(Pattern.quote(entityName) + "/id\\s+eq\\s+(?:'([^']*)'|(\\d+))", Pattern.CASE_INSENSITIVE);
	Matcher m = p.matcher(filter);
	if (m.find()) {
	    String id = m.group(1) != null ? m.group(1) : m.group(2);
	    if (id != null) {
		operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, element, id));
	    }
	}
    }

    /**
     * Helper for subclasses: adds bond when filter contains bare id eq X (X quoted or numeric).
     * Used when entity context is inferred from the request (e.g. Locations → id = UNIQUE_PLATFORM_IDENTIFIER).
     */
    protected void addIdFilter(String filter, MetadataElement element, Set<Bond> operands) {
	Pattern p = Pattern.compile("\\bid\\s+eq\\s+(?:'([^']*)'|(\\d+))", Pattern.CASE_INSENSITIVE);
	Matcher m = p.matcher(filter);
	if (m.find()) {
	    String id = m.group(1) != null ? m.group(1) : m.group(2);
	    if (id != null) {
		operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, element, id));
	    }
	}
    }
}
