package eu.essi_lab.profiler.pubsub;

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

import java.util.Date;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.pubsub.PubSubRequestParser.PubSubRequestParam;

/**
 * @author Fabrizio
 */
public class PubSubRequestTransformer extends DiscoveryRequestTransformer {

    private Long from;
    private Long until;

    public PubSubRequestTransformer(Long from, Long until) {
	this.from = from;
	this.until = until;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	return message;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String getProfilerType() {

	return new PubSubProfilerSetting().getServiceType();
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();

	return selector;
    }

    @Override
    protected Bond getUserBond(WebRequest request) throws GSException {

	String query = request.getQueryString();
	PubSubRequestParser reader = new PubSubRequestParser(query);

	String ts = reader.getParamValue(PubSubRequestParam.TIME_START);
	String te = reader.getParamValue(PubSubRequestParam.TIME_END);
	String bbox = reader.getParamValue(PubSubRequestParam.BBOX);
	String kwd = reader.getParamValue(PubSubRequestParam.SEARCH_TERMS);
	String parents = reader.getParamValue(PubSubRequestParam.PARENTS);
	String sources = reader.getParamValue(PubSubRequestParam.SOURCES);

	LogicalBond andBond = BondFactory.createAndBond();

	if (from != null) {

	    Date date = new Date(from);
	    String iso = ISO8601DateTimeUtils.getISO8601DateTime(date);

	    andBond.getOperands().add(BondFactory.createResourceTimeStampBond(BondOperator.GREATER_OR_EQUAL, iso));
	}

	if (until != null) {

	    Date date = new Date(until);
	    String iso = ISO8601DateTimeUtils.getISO8601DateTime(date);

	    andBond.getOperands().add(BondFactory.createResourceTimeStampBond(BondOperator.LESS_OR_EQUAL, iso));
	}

	if (!ts.equals("")) {

	    andBond.getOperands().add(BondFactory.createSimpleValueBond(//
		    BondOperator.GREATER_OR_EQUAL, //
		    MetadataElement.TEMP_EXTENT_BEGIN, //
		    ts));//
	}

	if (!te.equals("")) {

	    andBond.getOperands().add(BondFactory.createSimpleValueBond(//
		    BondOperator.LESS_OR_EQUAL, //
		    MetadataElement.TEMP_EXTENT_END, //
		    te));//
	}

	if (!bbox.equals("")) {

	    String west = bbox.split(",")[0];
	    String south = bbox.split(",")[1];
	    String east = bbox.split(",")[2];
	    String north = bbox.split(",")[3];

	    SpatialExtent extent = new SpatialExtent(//
		    Double.valueOf(south), //
		    Double.valueOf(west), //
		    Double.valueOf(north), //
		    Double.valueOf(east));//

	    andBond.getOperands().add(BondFactory.createSpatialExtentBond(BondOperator.CONTAINS, extent));
	}

	if (!kwd.equals("")) {

	    andBond.getOperands().add(BondFactory.createSimpleValueBond(//
		    BondOperator.EQUAL, //
		    MetadataElement.KEYWORD, //
		    kwd));//
	}

	if (!parents.equals("")) {

	    andBond.getOperands().add(BondFactory.createSimpleValueBond(//
		    BondOperator.EQUAL, //
		    MetadataElement.PARENT_IDENTIFIER, //
		    parents));//
	}

	if (!sources.equals("")) {

	    andBond.getOperands().add(BondFactory.createSourceIdentifierBond(sources));
	}

	if (andBond.getOperands().size() > 1) {

	    return andBond;

	} else if (andBond.getOperands().size() == 1) {

	    return andBond.getOperands().iterator().next();
	}

	return null;
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	return new Page(1, 10);
    }
}
