package eu.essi_lab.messages;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.HashMap;
import java.util.List;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.resource.data.DataReferences;
public class BulkDownloadMessage extends RequestMessage {


    @Override
    public String getBaseType() {
	return "bulk-download-message";
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = 57092734370735768L;
    private static final String DATA_REFERENCES = "DATA_REFERENCES";

    /**
     * 
     */
    @Override
    public HashMap<String, List<String>> provideInfo() {

	HashMap<String, List<String>> map = super.provideInfo();

	map.put(RuntimeInfoElement.BULK_DOWNLOAD_MESSAGE_TIME_STAMP.getName(), //
		Arrays.asList(ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds()));

	map.put(RuntimeInfoElement.BULK_DOWNLOAD_MESSAGE_TIME_STAMP_MILLIS.getName(), //
		Arrays.asList(String.valueOf(System.currentTimeMillis())));

	getView().ifPresent(v -> map.put(RuntimeInfoElement.BULK_DOWNLOAD_MESSAGE_VIEW_ID.getName(), Arrays.asList(v.getId())));

	return map;
    }

    @Override
    public String getName() {

	return "BULK_DOWNLOAD_MESSAGE";
    }

    /**
     * @param data references
     */
    public void setDataReferences(DataReferences references) {

	getPayload().add(new GSProperty<DataReferences>(DATA_REFERENCES, references));
    }

    /**
     * @return
     */
    public DataReferences getDataReferences() {

	return getPayload().get(DATA_REFERENCES, DataReferences.class);
    }

}
