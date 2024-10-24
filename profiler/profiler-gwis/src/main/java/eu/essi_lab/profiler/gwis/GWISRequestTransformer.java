package eu.essi_lab.profiler.gwis;

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

import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.gwis.request.GWISRequest;
import eu.essi_lab.profiler.gwis.request.GWISRequestValidator;

/**
 * @author boldrini
 */
public class GWISRequestTransformer extends DiscoveryRequestTransformer {

    public static final String GWIS_GET_BOND_ERROR = "GWIS_GET_BOND_ERROR";

    public GWISRequestTransformer() {

    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(ResourceSubset.CORE_EXTENDED);
	selector.setIndexesPolicy(IndexesPolicy.NONE);

	return selector;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	GWISRequestValidator validator = new GWISRequestValidator();

	return validator.validate(request);

    }

    @Override
    protected Bond getUserBond(WebRequest webRequest) throws GSException {

	try {

	    GWISRequest request = new GWISRequest(webRequest);

	    String onlineId = request.getOnlineId();

	    return BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ONLINE_ID, onlineId);

	} catch (Exception e) {

	    throw getGSException(e, e.getMessage());

	}

    }

    private GSException getGSException(Exception e, String message) {
	return GSException.createException(//
		getClass(), //
		message, //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		GWIS_GET_BOND_ERROR, e);
    }

    @Override
    protected Page getPage(WebRequest webRequest) throws GSException {

	return new Page(1, 100);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String getProfilerType() {

	return "GWIS";
    }

}
