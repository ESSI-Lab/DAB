package eu.essi_lab.messages.listrecords;

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

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author ilsanto
 */
public class ListRecordsResponse<T> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2073523944915364860L;
    private LinkedList<T> records;
    private String resumptionToken;

    public ListRecordsResponse() {

	records = new LinkedList<>();
    }

    /**
     * @param metadataRecord
     */
    public void addRecord(T metadataRecord) {

	records.add(metadataRecord);
    }

    /**
     * @return
     */
    public Iterator<T> getRecords() {

	return records.iterator();
    }

    /**
     * @return
     */
    public List<T> getRecordsAsList() {

	return records;
    }

    /**
     * @return
     */
    public String getResumptionToken() {

	return resumptionToken;

    }

    /**
     * @param resumptionTokenoken
     */
    public void setResumptionToken(String resumptionTokenoken) {

	resumptionToken = resumptionTokenoken;
    }

    /**
     * 
     */
    public void clearRecords() {

	records.clear();
    }
}
