package eu.essi_lab.profiler.wis.metadata.dataset;

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
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.wis.WISRequest;
import eu.essi_lab.profiler.wis.WISRequest.CollectionItems;
import eu.essi_lab.profiler.wis.WISRequest.Parameter;
import eu.essi_lab.profiler.wis.WISRequest.TopRequest;

public class WISDatasetDiscoveryTransformer extends DiscoveryRequestTransformer {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	try {
	    WISRequest wr = new WISRequest(request);
	    TopRequest topRequest = wr.getTopRequest();
	    CollectionItems collectionItem = wr.getCollectionItem();
	    if (topRequest.equals(TopRequest.COLLECTIONS) && collectionItem.equals(CollectionItems.DATASETS)) {
		ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	    } else {
		ret.setResult(ValidationResult.VALIDATION_FAILED);
	    }
	} catch (Exception e) {
	    ret.setResult(ValidationResult.VALIDATION_FAILED);
	}
	return ret;
    }

    @Override
    public Provider getProvider() {
	return new ESSILabProvider();
    }

    @Override
    protected Bond getUserBond(WebRequest request) throws GSException {
	WISRequest wis = new WISRequest(request);
	String topic = wis.getTopic();		
	Bond bond = null;
	if (topic.startsWith(WISRequest.DATASET_DISCOVERY)) {
	    String sourceId = topic.replace(WISRequest.DATASET_DISCOVERY, "");
	    if (!sourceId.isEmpty()) {
		bond = BondFactory.createSourceIdentifierBond(sourceId);		
	    }
	}
	return bond;
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {
	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(ResourceSubset.FULL);
//	selector.setSubset(ResourceSubset.NONE); 
//	selector.setIndexesPolicy(IndexesPolicy.NONE);
//	selector.addIndex(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
//	selector.addIndex(MetadataElement.PLATFORM_TITLE);
//	selector.addIndex(ResourceProperty.SOURCE_ID);
//	selector.addIndex(MetadataElement.BOUNDING_BOX);
//	selector.addIndex(MetadataElement.COUNTRY);
	selector.setIncludeOriginal(false);
	return selector;
    }

    @Override
    public String getProfilerType() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {
	WISRequest wis = new WISRequest(request);
	String limit = wis.getParameterValue(Parameter.LIMIT);
	if (limit == null || limit.equals("")) {
	    limit = "10";
	}
	Integer l = Integer.parseInt(limit);
	String offset = wis.getParameterValue(Parameter.OFFSET);
	if (offset == null || offset.equals("")) {
	    offset = "1";
	}
	Integer o = Integer.parseInt(offset);
	return new Page(o, l);
    }

}
