package eu.essi_lab.gssrv.conf.task.bluecloud;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class ReportManager {

    public ReportManager() {

    }

    private HashMap<String, DocumentReport> metadatas = new HashMap<>();

    public HashMap<String, DocumentReport> getMetadatas() {
        return metadatas;
    }

    private HashMap<MetadataElement, ReportResult> reportResults = new HashMap<>();

    public HashMap<MetadataElement, ReportResult> getReportResults() {
	return reportResults;
    }

    public void reset() {
	metadatas = new HashMap<>();
	reportResults = new HashMap<>();
    }

    public void addDocumentReport(String id, DocumentReport metadata) {
	metadatas.put(id, metadata);
    }

    public void printStatistics() {
	int total = metadatas.size();
	System.out.println("Metadata documents: " + total);
	Set<Entry<String, DocumentReport>> set = metadatas.entrySet();
	reportResults = new HashMap<>();
	for (Entry<String, DocumentReport> entry : set) {
	    String id = entry.getKey();
	    DocumentReport metadata = entry.getValue();
	    HashMap<MetadataElement, List<String>> innerMap = metadata.getMap();
	    Set<Entry<MetadataElement, List<String>>> innerEntries = innerMap.entrySet();
	    for (Entry<MetadataElement, List<String>> innerEntry : innerEntries) {

		MetadataElement element = innerEntry.getKey();
		List<String> values = innerEntry.getValue();
		if (values != null && !values.isEmpty()) {
		    ReportResult reportResult = reportResults.get(element);
		    if (reportResult == null) {
			reportResult = new ReportResult();
			reportResult.setTotal(total);
		    }
		    reportResults.put(element, reportResult);
		    reportResult.addValue(values);
		}
	    }
	}
	for (Entry<MetadataElement, ReportResult> entry : reportResults.entrySet()) {
	    MetadataElement element = entry.getKey();
	    ReportResult reportResult = entry.getValue();
	    int count = reportResult.getCount();
	    int percent = (int) (((double) count / (double) total) * 100.0);
	    System.out.println(element + ": " + count + " (" + percent + "%)");
	}

    }



}
