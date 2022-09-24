package eu.essi_lab.shared.driver.es.connector.aws;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.HttpResponseHandler;
import com.amazonaws.util.IOUtils;

/**
 * @author ilsanto
 */
public class DummyHandler<T> implements HttpResponseHandler<T> {
    private final T preCannedResponse;

    public DummyHandler(T preCannedResponse) {
	this.preCannedResponse = preCannedResponse;
    }

    @Override
    public T handle(HttpResponse response) throws Exception {
	System.out.println(IOUtils.toString(response.getContent()));
	return preCannedResponse;
    }

    @Override
    public boolean needsConnectionLeftOpen() {
	return false;
    }
}
