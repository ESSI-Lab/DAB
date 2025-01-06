package eu.essi_lab.profiler.thredds.catalog;

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

import java.util.HashSet;
import java.util.Set;

import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;

public class THREDDSCatalogRequestTransformer extends DiscoveryRequestTransformer {

    public static final String GWIS_GET_BOND_ERROR = "GWIS_GET_BOND_ERROR";

    public THREDDSCatalogRequestTransformer() {

    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(ResourceSubset.CORE_EXTENDED);
	selector.setIndexesPolicy(IndexesPolicy.ALL);

	return selector;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;

    }

    @Override
    protected Bond getUserBond(WebRequest webRequest) throws GSException {

	Set<Bond> operands = new HashSet<>();

	// we are interested only on downloadable datasets
	ResourcePropertyBond accessBond = BondFactory.createIsExecutableBond(true);
	operands.add(accessBond);

	// we are interested only on downloadable datasets
	ResourcePropertyBond downBond = BondFactory.createIsDownloadableBond(true);
	operands.add(downBond);

	// we are interested only on TIME SERIES datasets
	// ResourcePropertyBond timeSeriesBond = BondFactory.createIsTimeSeriesBond(true);

	// we are interested only on GRID datasets
	ResourcePropertyBond timeSeriesBond = BondFactory.createIsGridBond(true);
	operands.add(timeSeriesBond);

	switch (operands.size()) {
	case 0:
	    return null;
	case 1:
	    return operands.iterator().next();
	default:
	    return BondFactory.createAndBond(operands);
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

	return new Page(1, 2);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String getProfilerType() {

	return "THREDDS";
    }

}
