package eu.essi_lab.profiler.rest;

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
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;

/**
 * @author Fabrizio
 */
public class RestDiscoveryRequestTransformer extends DiscoveryRequestTransformer {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	return message;
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();

	return selector;
    }

    @Override
    protected Bond getUserBond(WebRequest request) throws GSException {

	return null;
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	return null;
    }

    @Override
    public String getProfilerType() {

	return RestProfiler.REST_PROFILER_TYPE;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

}
