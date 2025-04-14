package eu.essi_lab.profiler.csw.handler.discover;

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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecordById;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.csw.CSWGetRecordsParser;
import eu.essi_lab.profiler.csw.CSWProfilerSetting;
import eu.essi_lab.profiler.csw.CSWRequestConverter;
import eu.essi_lab.profiler.csw.CSWRequestUtils;
import eu.essi_lab.profiler.csw.CSWSearchAfterManager;

/**
 * @author Fabrizio
 */
public class CSWRequestTransformer extends DiscoveryRequestTransformer {

    private static final String CSW_GET_BOND_ERROR = "CSW_GET_BOND_ERROR";
    private static final String CSW_GET_PAGE_ERROR = "CSW_GET_PAGE_ERROR";

    /**
     * The max size of a page (CSW max records) supported for a single iteration workflow.<br>
     * If more than 10 records are requested, then the workflow is iterated in partial mode
     */
    private static final int MAX_SUPPORTED_PAGE_SIZE = 10;

    /**
     * 
     */
    private CSWProfilerSetting setting;

    /**
     * For test purpose
     */
    public CSWRequestTransformer() {

	this(new CSWProfilerSetting());
    }

    /**
     * @param setting
     */
    public CSWRequestTransformer(CSWProfilerSetting setting) {

	this.setting = setting;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	CSWRequestValidator validator = new CSWRequestValidator();
	ValidationMessage validate = validator.validate(request);

	return validate;
    }

    protected DiscoveryMessage refineMessage(DiscoveryMessage message) throws GSException {

	DiscoveryMessage refinedMessage = super.refineMessage(message);

	Page page = refinedMessage.getPage();

	if (CSWSearchAfterManager.isEnabled(setting, message.getWebRequest())) {

	    refinedMessage.setSortOrder(SortOrder.ASCENDING);
	    refinedMessage.setSortProperty(ResourceProperty.RESOURCE_TIME_STAMP);

	    Optional<SearchAfter> searchAfter = CSWSearchAfterManager.get(message.getView().map(v -> v.getId()), page, setting);

	    if (searchAfter.isPresent()) {

		refinedMessage.setSearchAfter(searchAfter.get());
	    }
	} else if (page.getSize() > MAX_SUPPORTED_PAGE_SIZE) {

	    refinedMessage.setIteratedWorkflow(IterationMode.PARTIAL_RESPONSE);
	}

	return refinedMessage;
    }

    @Override
    protected Bond getUserBond(WebRequest webRequest) throws GSException {

	try {

	    boolean getRecordsFromPOST = CSWRequestUtils.isGetRecordsFromPOST(webRequest);
	    boolean getRecordsFromGET = CSWRequestUtils.isGetRecordsFromGET(webRequest);

	    // -----------------------
	    //
	    // GetRecords
	    //

	    if (getRecordsFromPOST || getRecordsFromGET) {

		CSWGetRecordsParser parser = null;

		if (getRecordsFromGET) {

		    CSWRequestConverter converter = new CSWRequestConverter();
		    GetRecords getRecords = converter.convert(webRequest);

		    parser = new CSWGetRecordsParser(getRecords);

		} else {

		    parser = new CSWGetRecordsParser(webRequest.getBodyStream().clone());
		}

		return parser.parseFilter();
	    }

	    // -----------------------
	    //
	    // GetRecordById
	    //

	    List<String> identifiers = getIdentifiersFromGetRecordById(webRequest);
	    LogicalBond orBond = BondFactory.createOrBond();

	    if (identifiers.size() == 1) {
		return BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, identifiers.get(0));
	    }

	    for (String id : identifiers) {

		SimpleValueBond bond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, id);
		orBond.getOperands().add(bond);
	    }

	    return orBond;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_GET_BOND_ERROR, e);
	}
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(ResourceSubset.FULL);
	selector.setIndexesPolicy(IndexesPolicy.NONE);

	return selector;
    }

    @Override
    protected Page getPage(WebRequest webRequest) throws GSException {

	try {

	    boolean getRecordsFromPOST = CSWRequestUtils.isGetRecordsFromPOST(webRequest);
	    boolean getRecordsFromGET = CSWRequestUtils.isGetRecordsFromGET(webRequest);

	    // -----------------------
	    //
	    // GetRecords
	    //

	    if (getRecordsFromPOST || getRecordsFromGET) {

		GetRecords getRecords = null;

		if (getRecordsFromGET) {

		    CSWRequestConverter converter = new CSWRequestConverter();
		    getRecords = converter.convert(webRequest);

		} else {

		    getRecords = CommonContext.unmarshal(webRequest.getBodyStream().clone(), GetRecords.class);
		}

		int maxRecords = getRecords.getMaxRecords().intValue();
		int startPosition = getRecords.getStartPosition().intValue();
		if (startPosition == 0) {
		    startPosition = 1;
		}

		return new Page(startPosition, maxRecords);
	    }

	    // -----------------------
	    //
	    // GetRecordById
	    //

	    List<String> identifiers = getIdentifiersFromGetRecordById(webRequest);

	    return new Page(1, identifiers.size());

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_GET_PAGE_ERROR, e);
	}
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String getProfilerType() {

	return "CSW";
    }

    /**
     * @param webRequest
     * @return
     * @throws Exception
     */
    private List<String> getIdentifiersFromGetRecordById(WebRequest webRequest) throws Exception {

	if (webRequest.isGetRequest()) {

	    KeyValueParser parser = new KeyValueParser(webRequest.getURLDecodedQueryString());
	    String ids = parser.getValue("id", true);
	    return Arrays.asList(ids.split(","));
	}

	GetRecordById getRecords = CommonContext.unmarshal(//
		webRequest.getBodyStream().clone(), //
		GetRecordById.class);

	return getRecords.getIds();
    }
}
