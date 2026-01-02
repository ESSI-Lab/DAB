package eu.essi_lab.profiler.wof.discovery.sites;

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
import java.util.List;
import java.util.Optional;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.wof.HydroServerProfiler;
import eu.essi_lab.profiler.wof.HydroServerProfilerSetting;
import eu.essi_lab.profiler.wof.WOFRequest;
import eu.essi_lab.profiler.wof.WOFRequest.Parameter;

/**
 * HYDRO Server GetSiteInfo/GetSitesInfoObject request transformer
 *
 * @author boldrini
 */
public class GetSiteInfoTransformer extends DiscoveryRequestTransformer {

    /**
     * 
     */
    private static final String GET_SITE_INFO_TRANSFORMER_ERROR = "GET_SITE_INFO_TRANSFORMER_ERROR";

    public GetSiteInfoTransformer() {
	// empty constructor for service loader
    }

    @Override
    public DiscoveryMessage transform(WebRequest request) throws GSException {

	DiscoveryMessage message = super.transform(request);

	message.getResourceSelector().setSubset(ResourceSubset.FULL);
	message.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	message.setRequestTimeout(12000); // 200 minutes
	message.getResourceSelector().setIncludeOriginal(false);
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

	GetSiteInfoValidator validator = new GetSiteInfoValidator();

	return validator.validate(request);

    }

    @Override
    protected Bond getUserBond(WebRequest webRequest) throws GSException {

	try {
	    WOFRequest gsi = getWOFRequest(webRequest);
	    String siteCode = gsi.getParameterValue(Parameter.SITE_CODE);
	    if (siteCode.contains(":")) {
		siteCode = siteCode.substring(siteCode.indexOf(':') + 1);
	    }

	    List<Bond> operands = new ArrayList<>();

//	    // we are interested only on downloadable datasets
//	    ResourcePropertyBond accessBond = BondFactory.createIsExecutableBond(true);
//	    operands.add(accessBond);
//
//	    // we are interested only on downloadable datasets
//	    ResourcePropertyBond downBond = BondFactory.createIsDownloadableBond(true);
//	    operands.add(downBond);

	    // we are interested only on TIME SERIES datasets
	    ResourcePropertyBond timeSeriesBond = BondFactory.createIsTimeSeriesBond(true);
	    operands.add(timeSeriesBond);

	    // platform constraint
	    SimpleValueBond platformBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.UNIQUE_PLATFORM_IDENTIFIER,
		    siteCode);
	    operands.add(platformBond);

	    return BondFactory.createAndBond(operands);

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    GET_SITE_INFO_TRANSFORMER_ERROR);
	}

    }

    public WOFRequest getWOFRequest(WebRequest webRequest) {
	return new GetSiteInfoRequest(webRequest);
    }

    @Override
    protected Optional<Queryable> getDistinctElement(WebRequest request) {
	return Optional.of(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER);

    }

    @Override
    protected Page getPage(WebRequest webRequest) throws GSException {

	return new Page(1, 1000000000);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String getProfilerType() {

	return new HydroServerProfilerSetting().getServiceType();
    }

}
