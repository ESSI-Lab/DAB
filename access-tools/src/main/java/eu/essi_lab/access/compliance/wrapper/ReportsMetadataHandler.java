package eu.essi_lab.access.compliance.wrapper;

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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.model.resource.ExtendedMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.PropertiesAdapter;

/**
 * @author Fabrizio
 */
public class ReportsMetadataHandler implements PropertiesAdapter<ReportsMetadataHandler> {

    private ExtendedMetadata metadata;
    private String xPath;

    /**
     * @param metadata
     */
    public ReportsMetadataHandler(GSResource resource) {

	this.metadata = resource.getHarmonizedMetadata().getExtendedMetadata();
	this.xPath = "//" + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":accessReports";
    }

    /**
     * @return
     */
    public List<DataComplianceReport> getReports() {

	String xPath = this.xPath + "//" + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":report";

	List<Node> list = null;
	try {
	    list = metadata.get(xPath);

	} catch (XPathExpressionException e) {

	    e.printStackTrace();
	    GSLoggerFactory.getLogger(ReportWrapper.class).error(e.getMessage(), e);

	    throw new RuntimeException(e.getMessage(), e);
	}

	List<DataComplianceReport> out = new ArrayList<>();

	if (!list.isEmpty()) {

	    out = list.stream().map(n -> {

		try {
		    return ReportWrapper.wrap(n);
		} catch (ParseException e) {
		    e.printStackTrace();
		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}

		return null;

	    }).collect(Collectors.toList());

	    if (out.stream().anyMatch(Objects::isNull)) {

		throw new RuntimeException("Parsing error during report wrapping");
	    }
	}

	return out;
    }

    /**
     * 
     */
    public void clearReports() {

	try {
	    metadata.remove(this.xPath + "/*");
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(ReportWrapper.class).error(e.getMessage(), e);

	    throw new RuntimeException(e.getMessage(), e);
	}
    }

    /**
     * @param report
     */
    public void addReport(DataComplianceReport report) {

	ReportWrapper wrapper = new ReportWrapper(report);

	try {
	    List<Node> list = metadata.get(xPath);
	    if (list.isEmpty()) {

		metadata.add("accessReports");
	    }

	    Node node = metadata.get(xPath).get(0);

	    XMLDocumentReader reader = new XMLDocumentReader(node.getOwnerDocument());
	    reader.setNamespaceContext(new CommonNameSpaceContext());

	    XMLDocumentWriter writer = new XMLDocumentWriter(reader);

	    Element reportDoc = wrapper.asDocument(true).getDocumentElement();

	    writer.addNode(xPath, reportDoc);

	} catch (Exception e) {

	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw new RuntimeException(e.getMessage(), e);
	}
    }

    public void adapt(ReportsMetadataHandler targerHandler, AdaptPolicy policy, String... properties) {

	if (!getReports().isEmpty()) {

	    switch (policy) {
	    case ADD:

		getReports().forEach(r -> targerHandler.addReport(r));
		break;

	    case ON_EMPTY:

		if (targerHandler.getReports().isEmpty()) {
		    getReports().forEach(r -> targerHandler.addReport(r));
		}

		break;

	    case OVERRIDE:

		if (!getReports().isEmpty()) {
		    targerHandler.clearReports();
		    getReports().forEach(r -> targerHandler.addReport(r));
		}

		break;
	    }
	}
    }
}
