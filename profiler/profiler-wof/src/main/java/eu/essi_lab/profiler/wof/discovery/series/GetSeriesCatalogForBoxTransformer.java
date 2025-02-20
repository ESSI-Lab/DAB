package eu.essi_lab.profiler.wof.discovery.series;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.handler.selector.WebRequestFilter;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.wof.WOFRequest;

/**
 * HIS Central request transformer
 *
 * @author boldrini
 */
public class GetSeriesCatalogForBoxTransformer extends DiscoveryRequestTransformer {

    /**
     * 
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    public static final String HIS_CENTRAL_GET_BOND_ERROR = "HIS_CENTRAL_GET_BOND_ERROR";

    public GetSeriesCatalogForBoxTransformer() {

    }

    @Override
    public DiscoveryMessage transform(WebRequest request) throws GSException {

	DiscoveryMessage message = super.transform(request);
	message.setIteratedWorkflow(IterationMode.FULL_RESPONSE);

	return message;
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(ResourceSubset.CORE_EXTENDED);
	selector.setIndexesPolicy(IndexesPolicy.NONE);
	selector.setIncludeOriginal(false);

	return selector;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	GetSeriesCatalogForBoxValidator validator = new GetSeriesCatalogForBoxValidator();

	return validator.validate(request);

    }

    @Override
    protected Bond getUserBond(WebRequest webRequest) throws GSException {

	try {

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

	    WOFRequest request = new GetSeriesCatalogForBoxRequest(webRequest);

	    Optional<SpatialBond> areaBond = request.getSpatialBond();
	    if (areaBond.isPresent()) {
		operands.add(areaBond.get());
	    }

	    Optional<SimpleValueBond> beginBond = request.getBeginBond();
	    if (beginBond.isPresent()) {
		operands.add(beginBond.get());
	    }

	    Optional<SimpleValueBond> endBond = request.getEndBond();
	    if (endBond.isPresent()) {
		operands.add(endBond.get());
	    }

	    Optional<SimpleValueBond> keywordBond = request.getKeywordBond();
	    if (keywordBond.isPresent()) 
	    {
		String viewCreator = null;
		Optional<String> viewId = webRequest.extractViewId();
		if (viewId.isPresent()) {		    
		    StorageInfo storageUri = ConfigurationWrapper.getStorageInfo();
		    Optional<View> view = WebRequestTransformer.findView(storageUri , viewId.get());
		    if (view.isPresent()) {
			viewCreator = view.get().getCreator();			
		    }
		}
		List<? extends Bond> keywords = getKeywords(viewCreator, keywordBond.get());
		switch (keywords.size()) {
		case 0:		    
		    break;
		case 1:
		    operands.add(keywords.get(0));
		default:
		    LogicalBond orBond = BondFactory.createOrBond(keywords.toArray(new Bond[] {}));
		    operands.add(orBond);
		    break;
		}
	    }

	    Optional<Bond> sourcesBond = request.getSourcesBond();
	    if (sourcesBond.isPresent()) {
		operands.add(sourcesBond.get());
	    }

	    switch (operands.size()) {
	    case 0:
		return null;
	    case 1:
		return operands.iterator().next();
	    default:
		return BondFactory.createAndBond(operands);
	    }

	} catch (Exception e) {

	    throw getGSException(e, e.getMessage());

	}

    }

    public List<SimpleValueBond> getKeywords(String viewCreator, SimpleValueBond simpleValueBond) {
	List<SimpleValueBond> ret = new ArrayList<>();
	ret.add(simpleValueBond);
	return ret;
    }

    private GSException getGSException(Exception e, String message) {
	return GSException.createException(//
		getClass(), //
		message, //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		HIS_CENTRAL_GET_BOND_ERROR, e);
    }

    @Override
    protected Page getPage(WebRequest webRequest) throws GSException {

	return new Page(1, DEFAULT_PAGE_SIZE);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String getProfilerType() {

	return "HIS-CENTRAL";
    }

}
