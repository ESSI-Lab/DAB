package eu.essi_lab.profiler.wof.discovery.sites;

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

import java.util.Optional;

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.profiler.wof.WOFRequest;

/**
 * HYDRO Server GetSitesByBox/GetSitesByBoxObject request transformer
 *
 * @author boldrini
 */
public class GetSitesByBoxTransformerWithSeries extends GetSitesTransformer {

    private boolean isObject = false;

    public GetSitesByBoxTransformerWithSeries(boolean object) {
	this.isObject = true;
    }

    @Override
    public WOFRequest getWOFRequest(WebRequest webRequest) {
	if (isObject) {
	    return new GetSitesByBoxObjectRequest(webRequest);
	} else {
	    return new GetSitesByBoxRequest(webRequest);
	}

    }

    @Override
    public WebRequestValidator getValidator() {
	if (isObject) {
	    return new GetSitesByBoxObjectValidatorWithSeries();
	} else {
	    return new GetSitesByBoxValidatorWithSeries();
	}

    }

    @Override
    protected Optional<Queryable> getDistinctElement(WebRequest request) {
	return Optional.empty();

    }

    @Override
    protected Optional<Queryable> getOrderingProperty() {
	return Optional.of(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
    }

    @Override
    protected Optional<SortOrder> getOrderingDirection() {
	return Optional.of(SortOrder.ASCENDING);
    }

}
