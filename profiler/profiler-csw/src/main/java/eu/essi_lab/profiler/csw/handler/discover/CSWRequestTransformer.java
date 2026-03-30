package eu.essi_lab.profiler.csw.handler.discover;

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

import eu.essi_lab.jaxb.common.*;
import eu.essi_lab.jaxb.csw._2_0_2.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.*;
import eu.essi_lab.messages.RequestMessage.*;
import eu.essi_lab.messages.ResourceSelector.*;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.messages.web.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.model.pluggable.*;
import eu.essi_lab.model.resource.*;
import eu.essi_lab.pdk.wrt.*;
import eu.essi_lab.profiler.csw.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class CSWRequestTransformer extends DiscoveryRequestTransformer {

    private static final String CSW_GET_BOND_ERROR = "CSW_GET_BOND_ERROR";
    private static final String CSW_GET_PAGE_ERROR = "CSW_GET_PAGE_ERROR";

    /**
     * The max size of a page (CSW max records) supported for a single iteration workflow.<br> If more than 10 records are requested, then
     * the workflow is iterated in partial mode
     */
    private static final int MAX_SUPPORTED_PAGE_SIZE = 10;

    /**
     * For test purpose
     */
    public CSWRequestTransformer() {

	super(new CSWProfilerSetting());
    }

    /**
     * @param setting
     */
    public CSWRequestTransformer(CSWProfilerSetting setting) {

	super(setting);
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	CSWRequestValidator validator = new CSWRequestValidator();
	ValidationMessage validate = validator.validate(request);

	return validate;
    }

    @Override
    protected DiscoveryMessage refineMessage(DiscoveryMessage message) throws GSException {

	DiscoveryMessage refinedMessage = super.refineMessage(message);

	Page page = refinedMessage.getPage();

	if (CSWSearchAfterManager.isEnabled(getSetting().get(), message.getWebRequest())) {

	    refinedMessage.setSortedFields(SortedFields.of(ResourceProperty.RESOURCE_TIME_STAMP, SortOrder.ASCENDING));

	    Optional<SearchAfter> searchAfter = CSWSearchAfterManager.get(message.getView().map(v -> v.getId()), page, getSetting().get());

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
		return BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, identifiers.getFirst());
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

	    return new Page(1, identifiers.size() + 1);

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

	    KeyValueParser parser = new KeyValueParser(webRequest.getOptionalQueryString().get());
	    String ids = parser.getValue("id", true);

	    return Stream.of(ids.split(",")).map(StringUtils::URLDecodeUTF8).toList();
	}

	GetRecordById getRecords = CommonContext.unmarshal(//
		webRequest.getBodyStream().clone(), //
		GetRecordById.class);

	return getRecords.getIds();
    }
}
