package eu.essi_lab.pdk.wrt;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Provider;

public class ScheduleRequestTransformer extends WebRequestTransformer<RequestMessage> {

    private WebRequestTransformer transformer;

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	return transformer.validate(request);
    }

    @Override
    public Provider getProvider() {
	return transformer.getProvider();
    }

    @Override
    protected RequestMessage refineMessage(RequestMessage message) throws GSException {
	RequestMessage refinedMessage = transformer.refineMessage(message);
	return refinedMessage;
    }

    @Override
    protected RequestMessage createMessage() {
	RequestMessage ret = transformer.createMessage();
	ret.setScheduled(true);
	return ret;
    }

    @Override
    public String getProfilerType() {
	return transformer.getProfilerType();
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {
	return transformer.getPage(request);
    }

    public WebRequestTransformer getTransformer() {
	return transformer;
    }

    public void setTransformer(WebRequestTransformer transformer) {
	this.transformer = transformer;
    }

}
