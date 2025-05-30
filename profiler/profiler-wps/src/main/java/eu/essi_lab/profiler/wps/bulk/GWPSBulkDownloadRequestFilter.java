package eu.essi_lab.profiler.wps.bulk;

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

import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.pdk.handler.selector.POSTRequestFilter;

public class GWPSBulkDownloadRequestFilter extends POSTRequestFilter {

    public GWPSBulkDownloadRequestFilter() {
	super("*");
    }

    @Override
    protected boolean accept(ClonableInputStream content) {

	try {
	    XMLDocumentReader reader = new XMLDocumentReader(content.clone());
	    String id = reader.evaluateString("//*:Execute/*:Identifier");
	    Boolean storeExecuteResponse = reader.evaluateBoolean("//*:ResponseDocument/@storeExecuteResponse='true'");
	    return storeExecuteResponse && id.equals(GWPSBulkDownloadHandler.PROCESS_IDENTIFIER);

	} catch (Exception e) {
	    return false;
	}
    }
}
