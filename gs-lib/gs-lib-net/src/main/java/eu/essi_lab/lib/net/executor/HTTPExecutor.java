package eu.essi_lab.lib.net.executor;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.List;

public abstract class HTTPExecutor {

    public abstract HTTPExecutorResponse execute(String url, String httpMethod, List<SimpleEntry<String, String>> headers, byte[] body)
	    throws Exception;

    public HTTPExecutorResponse execute(String url) throws Exception {
	return execute(url, "GET");
    }

    public HTTPExecutorResponse execute(String url, String httpMethod) throws Exception {
	return execute(url, httpMethod, null, null);
    }

}
