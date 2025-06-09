package eu.essi_lab.profiler.esri.feature.query;

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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.ExtendedElementsPolicy;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.data.CRSUtils;
import eu.essi_lab.model.resource.data.EPSGCRS;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.esri.feature.ESRIFieldType;
import eu.essi_lab.profiler.esri.feature.FeatureLayer;
import eu.essi_lab.profiler.esri.feature.Field;
import eu.essi_lab.profiler.esri.feature.query.parser.ESRIParser;

/**
 * @author boldrini
 */
public class FeatureQueryRequestTransformer extends DiscoveryRequestTransformer {

    public static final String FEATURE_QUERY_ERROR = "FEATURE_QUERY_ERROR";

    /**
     * 
     */
    private static final int DEFAULT_PAGE_SIZE = 1000;

    public FeatureQueryRequestTransformer() {

    }

    @Override
    public DiscoveryMessage transform(WebRequest request) throws GSException {

	DiscoveryMessage message = super.transform(request);

	message.setIteratedWorkflow(IterationMode.FULL_RESPONSE);

	return message;
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();

	String path = request.getServletRequest().getPathInfo();
	String id = path.substring(path.indexOf("FeatureServer/"));
	id = id.substring(id.indexOf("/") + 1);
	id = id.substring(0, id.indexOf("/"));
	FeatureLayer layer = FeatureLayer.getLayer(id);

	ESRIRequest esriRequest = new ESRIRequest(request);
	String value = esriRequest.getParameter("outfields");
	List<Field> returnFields = layer.getOutFields(value);

	String returnGeometryValue = esriRequest.getParameter("returnGeometry");
	Boolean returnGeometry = true;
	if (returnGeometryValue != null && returnGeometryValue.toLowerCase().equals("false")) {
	    returnGeometry = false;
	}
	boolean extendedOnly = true;
	if (returnGeometry) {
	    extendedOnly = false;
	}
	for (Field field : returnFields) {
	    if (field.getType().equals(ESRIFieldType.WHOS_PAGE)) {
		// reports metadata handler
		selector.addExtendedElement("accessReports");
	    } else if (field.getType().equals(ESRIFieldType.LATITUDE)) {
		// resource core
		extendedOnly = false;
	    } else if (field.getType().equals(ESRIFieldType.LONGITUDE)) {
		// resource core
		extendedOnly = false;
	    } else if (field.getType().equals(ESRIFieldType.OID)) {
		// extension platform
		selector.addExtendedElement(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
		selector.addExtendedElement(MetadataElement.BNHS_INFO);
	    } else if (field.getType().equals(ESRIFieldType.START_DATE)) {
		// resource core
		extendedOnly = false;
	    } else if (field.getType().equals(ESRIFieldType.END_DATE)) {
		// resource core
		extendedOnly = false;
	    } else if (field.getType().equals(ESRIFieldType.ISO_TITLE)) {
		// resource core
		extendedOnly = false;
	    } else if (field.getBNHSProperty() != null) {
		// extension BNHS
		selector.addExtendedElement(MetadataElement.BNHS_INFO);
	    }
	}

	if (extendedOnly) {
	    selector.setSubset(ResourceSubset.EXTENDED);
	    selector.setExtendedElementsPolicy(ExtendedElementsPolicy.ALL);
	} else {
	    selector.setSubset(ResourceSubset.CORE_EXTENDED);
	}

	selector.setIndexesPolicy(IndexesPolicy.NONE);

	selector.setIncludeOriginal(false);

	return selector;
    }

    private ResourceSubset getResourceSubset(WebRequest request) {
	String path = request.getServletRequest().getPathInfo();
	String id = path.substring(path.indexOf("FeatureServer/"));
	id = id.substring(id.indexOf("/") + 1);
	id = id.substring(0, id.indexOf("/"));
	FeatureLayer layer = FeatureLayer.getLayer(id);

	ESRIRequest esriRequest = new ESRIRequest(request);
	String value = esriRequest.getParameter("outfields");
	List<Field> returnFields = layer.getOutFields(value);

	boolean extendedOnly = true;
	for (Field field : returnFields) {
	    if (field.getType().equals(ESRIFieldType.WHOS_PAGE)) {
		// reports metadata handler
	    } else if (field.getType().equals(ESRIFieldType.LATITUDE)) {
		// resource core
		extendedOnly = false;
	    } else if (field.getType().equals(ESRIFieldType.LONGITUDE)) {
		// resource core
		extendedOnly = false;
	    } else if (field.getType().equals(ESRIFieldType.OID)) {
		// extension platform
	    } else if (field.getType().equals(ESRIFieldType.START_DATE)) {
		// resource core
		extendedOnly = false;
	    } else if (field.getType().equals(ESRIFieldType.END_DATE)) {
		// resource core
		extendedOnly = false;
	    } else if (field.getType().equals(ESRIFieldType.ISO_TITLE)) {
		// resource core
		extendedOnly = false;
	    } else if (field.getBNHSProperty() != null) {
		// extension BNHS
	    }
	}

	if (extendedOnly) {
	    return ResourceSubset.EXTENDED;
	} else {
	    return ResourceSubset.CORE_EXTENDED;
	}
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;

    }

    @Override
    protected Optional<Queryable> getDistinctElement(WebRequest request) {
	return Optional.of(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);

    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	ResourceSubset subset = getResourceSubset(request);

	if (subset.equals(ResourceSubset.EXTENDED)) {
	    return new Page(1, 100);
	}

	return new Page(1, DEFAULT_PAGE_SIZE);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String getProfilerType() {

	return "ESRI_FEATURE_SERVER";
    }

    @Override
    protected Bond getUserBond(WebRequest request) throws GSException {

	if (request==null) {
	    return null;
	}
	
	String path = request.getServletRequest().getPathInfo();
	String id = path.substring(path.indexOf("FeatureServer/"));
	id = id.substring(id.indexOf("/") + 1);
	id = id.substring(0, id.indexOf("/"));
	FeatureLayer layer = FeatureLayer.getLayer(id);

	List<Bond> operands = new ArrayList<>();

	ESRIRequest esriRequest = new ESRIRequest(request);

	// IDENTIFIERS CLAUSE
	String identifiers = esriRequest.getParameter("objectIds");
	if (identifiers != null && !identifiers.isEmpty()) {
	    List<Bond> orOperands = new ArrayList<>();
	    String[] ids;
	    if (identifiers.contains(",")) {
		ids = identifiers.split(",");
	    } else {
		ids = new String[] { identifiers };
	    }
	    for (String identifier : ids) {
		MetadataElement element = layer.getObjectIdField().getMetadataElement();
		orOperands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, element, identifier));
	    }
	    if (orOperands.size() == 1) {
		operands.add(orOperands.get(0));
	    } else {
		operands.add(BondFactory.createOrBond(orOperands));
	    }
	}

	// WHERE CLAUSE
	String where = esriRequest.getParameter("where");
	if (where != null && !where.isEmpty()) {
	    try {
		where = URLDecoder.decode(where, "UTF-8");

		ESRIParser parser = new ESRIParser();
		Bond parsed = parser.parse(where, layer.getFields());
		if (parsed != null) {
		    if (parsed instanceof LogicalBond) {
			LogicalBond lb = (LogicalBond) parsed;
			if (lb.getLogicalOperator().equals(LogicalOperator.AND)) {
			    // and bond
			    operands.addAll(lb.getOperands());
			} else {
			    // or bond
			    operands.add(parsed);
			}
		    } else {
			// not a logical bond
			operands.add(parsed);
		    }
		}

	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	// GEOMETRY CLAUSE
	String geometry = esriRequest.getParameter("geometry");
	if (geometry != null && !geometry.isEmpty()) {
	    try {
		geometry = URLDecoder.decode(geometry, "UTF-8");
		JSONObject geo = new JSONObject(geometry);
		Double xmin = null;
		Double xmax = null;
		Double ymin = null;
		Double ymax = null;
		if (geo.has("xmin")) {
		    xmin = geo.getDouble("xmin");
		    xmax = geo.getDouble("xmax");
		    ymin = geo.getDouble("ymin");
		    ymax = geo.getDouble("ymax");
		} else if (geo.has("rings")) {
		    JSONArray rings = geo.getJSONArray("rings");
		    for (int i = 0; i < rings.length(); i++) {
			JSONArray ring = rings.getJSONArray(i);
			for (int j = 0; j < ring.length(); j++) {
			    JSONArray point = ring.getJSONArray(j);
			    double x = point.getDouble(0);
			    double y = point.getDouble(1);
			    if (xmax == null || xmax < x) {
				xmax = x;
			    }
			    if (xmin == null || xmin > x) {
				xmin = x;
			    }
			    if (ymax == null || ymax < y) {
				ymax = y;
			    }
			    if (ymin == null || ymin > y) {
				ymin = y;
			    }
			}
		    }
		} else {
		    xmin = -20037508.342789244;
		    xmax = 20037508.342789244;
		    ymin = -242528680.94374272;
		    ymax = 242528680.94374272;
		}

		Integer wkid = null;
		JSONObject spatialReference = null;
		if (geo.has("spatialReference")) {
		    spatialReference = geo.getJSONObject("spatialReference");
		    if (spatialReference != null) {
			wkid = spatialReference.getInt("wkid");
		    }
		}
		SpatialExtent extent = null;
		if (wkid == null) {
		    String inSR = esriRequest.getParameter("inSR");
		    if (inSR != null && !inSR.isEmpty()) {
			wkid = Integer.parseInt(inSR);
		    }
		} else if (wkid != null) {

		    if (wkid == 4326) {

			extent = new SpatialExtent(ymin, xmin, ymax, xmax);

		    } else {
			// ESRI CRS ID
			if (wkid == 102100) {
			    wkid = 3857;
			}
			SimpleEntry<Double, Double> lowerCorner = new SimpleEntry<>(xmin, ymin);
			SimpleEntry<Double, Double> upperCorner = new SimpleEntry<>(xmax, ymax);
			SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> sourceCorners = new SimpleEntry<>(lowerCorner,
				upperCorner);
			try {
			    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox4326 = CRSUtils
				    .translateBBOX(sourceCorners, new EPSGCRS(wkid), EPSGCRS.EPSG_4326());
			    extent = new SpatialExtent(bbox4326.getKey().getKey(), bbox4326.getKey().getValue(),
				    bbox4326.getValue().getKey(), bbox4326.getValue().getValue());
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    }
		}
		if (extent != null) {
		    operands.add(BondFactory.createSpatialEntityBond(BondOperator.CONTAINS, extent));
		}

	    } catch (UnsupportedEncodingException e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	}

	switch (operands.size()) {
	case 0:
	    return null;
	case 1:
	    return operands.get(0);
	default:
	    return BondFactory.createAndBond(operands);
	}

    }

}
