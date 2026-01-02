package eu.essi_lab.profiler.wof;

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

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.request.executor.IDiscoveryExecutor;

public class WOFQueryUtils {
    private static ExpiringCache<String> onlineCache;

    static {
	onlineCache = new ExpiringCache<>();
	onlineCache.setDuration(60000);
	onlineCache.setMaxSize(100);
    }

    /**
     * @param requestId
     * @param optional
     * @param siteCode
     * @param variableCode
     * @return
     * @throws GSException
     */
    public static String getOnlineId(//
	    String requestId, //
	    Optional<String> viewId, //
	    String siteCode, //
	    String variableCode) throws GSException {

	if (requestId != null) {
	    synchronized (onlineCache) {
		String onlineId = onlineCache.get(requestId);
		if (onlineId != null) {
		    return onlineId;
		}
	    }
	}

	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();

	discoveryMessage.setRequestId(requestId);
	discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	discoveryMessage.getResourceSelector().setSubset(ResourceSubset.EXTENDED);
	discoveryMessage.getResourceSelector().setIncludeOriginal(false);
	discoveryMessage.setPage(new Page(1, 1));

	Optional<View> view = viewId.map(id -> {
	    try {
		return DiscoveryRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), id).get();
	    } catch (Exception e) {

		GSLoggerFactory.getLogger(WOFQueryUtils.class).error(e);
		return null;
	    }
	});
	
	view.ifPresent(v -> discoveryMessage.setView(v));

	discoveryMessage.setSources(view.isPresent() //
		? ConfigurationWrapper.getViewSources(view.get()) //
		: ConfigurationWrapper.getHarvestedSources());

	discoveryMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());

	SimpleValueBond bond1 = BondFactory.createSimpleValueBond(//
		BondOperator.EQUAL, //
		MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, //
		variableCode);
	SimpleValueBond bond2 = BondFactory.createSimpleValueBond(//
		BondOperator.EQUAL, //
		MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, //
		siteCode);
	LogicalBond bond = BondFactory.createAndBond(bond1, bond2);

	discoveryMessage.setPermittedBond(bond);
	discoveryMessage.setUserBond(bond);
	discoveryMessage.setNormalizedBond(bond);

	ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);

	if (resultSet.getResultsList().isEmpty()) {
	    GSLoggerFactory.getLogger(WOFQueryUtils.class).info("Empty platform + attribute combination");
	    return null;
	}
	GSResource result = resultSet.getResultsList().get(0);
	ReportsMetadataHandler handler = new ReportsMetadataHandler(result);

	List<DataComplianceReport> reports = handler.getReports();
	Optional<DataComplianceReport> optReport = reports.stream()
		.filter(r -> r.getExecutionResult().get().getResult().equals(ValidationResult.VALIDATION_SUCCESSFUL)).findFirst();
	if (!optReport.isPresent()) {
	    GSLoggerFactory.getLogger(WOFQueryUtils.class).info("platform + attribute + report not found");
	    return null;
	}

	DataComplianceReport report = optReport.get();

	GSLoggerFactory.getLogger(WOFQueryUtils.class).info("platform + attribute combination found");
	synchronized (onlineCache) {
	    onlineCache.put(requestId, report.getOnlineId());
	}
	GSLoggerFactory.getLogger(WOFQueryUtils.class).info("platform + attribute combination returning");
	return report.getOnlineId();

    }

    public static boolean isSemanticHarmonizationEnabled(WebRequest webRequest) {
	Optional<String> viewId = webRequest.extractViewId();
	if (viewId.isPresent()) {
	    if (viewId.get().contains("plata-original")) {
		return false;
	    }
	}
	return true;
    }

}
