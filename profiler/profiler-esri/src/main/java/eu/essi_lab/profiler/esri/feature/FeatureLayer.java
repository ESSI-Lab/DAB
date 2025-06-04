package eu.essi_lab.profiler.esri.feature;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.stats.ComputationResult;
import eu.essi_lab.messages.stats.ResponseItem;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.index.jaxb.CardinalValues;
import eu.essi_lab.model.pluggable.Pluggable;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.request.executor.IStatisticsExecutor;

/**
 * @author boldrini
 */
public abstract class FeatureLayer implements Pluggable {

    public Long getResourceIdentifier(String view, XMLDocumentReader reader) {
	return null;
    }

    /**
     * Returns all the available layers
     * 
     * @return
     */
    public static List<FeatureLayer> getAvailableLayers() {

	PluginsLoader<FeatureLayer> pluginsLoader = new PluginsLoader<>();
	List<FeatureLayer> layers = pluginsLoader.loadPlugins(FeatureLayer.class);

	GSLoggerFactory.getLogger(FeatureLayer.class).info("Loaded feature layers:");

	layers.forEach(layer -> GSLoggerFactory.getLogger(FeatureLayer.class).info(layer.getClass().getCanonicalName()));//

	return layers;
    }

    public abstract String getId();

    public abstract String getName();

    public abstract String getDescription();

    public abstract List<Field> getFields();

    @Override
    public Provider getProvider() {
	return Provider.essiLabProvider();
    }

    public JSONObject getJSON(WebRequest request) throws GSException {
	JSONObject ret = new JSONObject();
	InputStream stream = FeatureServerHandler.class.getClassLoader().getResourceAsStream("esri/feature-server-layer-template.json");
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	try {
	    IOUtils.copy(stream, baos);
	    String str = new String(baos.toByteArray());
	    String time = ""+System.currentTimeMillis();
	    str = str.replace("$EDITMS", time);
	    stream.close();
	    baos.close();
	    ret = new JSONObject(str);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	ret.put("id", getId());
	ret.put("name", getName());
	ret.put("displayField", getObjectIdField().getName());
	ret.put("description", getDescription());

	//
	// creates the bonds
	// //
	// Set<Bond> operands = new HashSet<>();
	//
	// // we are interested only on downloadable datasets
	// ResourcePropertyBond accessBond = BondFactory.createIsExecutableBond(true);
	// operands.add(accessBond);
	//
	// // we are interested only on downloadable datasets
	// ResourcePropertyBond downBond = BondFactory.createIsDownloadableBond(true);
	// operands.add(downBond);
	//
	// // we are interested only on TIME SERIES datasets
	// ResourcePropertyBond timeSeriesBond = BondFactory.createIsTimeSeriesBond(true);
	// operands.add(timeSeriesBond);
	//
	// LogicalBond andBond = BondFactory.createAndBond(operands);

	//
	// creates the message
	//
	StatisticsMessage statisticsMessage = getStatisticsMessage(request);

	ServiceLoader<IStatisticsExecutor> loader = ServiceLoader.load(IStatisticsExecutor.class);
	IStatisticsExecutor executor = loader.iterator().next();

	StatisticsResponse response = executor.compute(statisticsMessage);

	List<ResponseItem> items = response.getItems();

	ResponseItem responseItem = items.get(0);

	CardinalValues cardinalValues = responseItem.getBBoxUnion().getCardinalValues().get();

//	JSONObject extent = new JSONObject();
//	extent.put("xmin", cardinalValues.getWest());
//	extent.put("ymin", cardinalValues.getSouth());
//	extent.put("xmax", cardinalValues.getEast());
//	extent.put("ymax", cardinalValues.getNorth());
//	JSONObject wkidObject = new JSONObject();
//	wkidObject.put("wkid", 4326);
//	extent.put("spatialReference", wkidObject);
//	ret.put("extent", extent);

	ComputationResult tempExtentUnion = responseItem.getTempExtentUnion();

//	String sourceId = responseItem.getGroupedBy().get();

	// STATISTICS TODO

	ret.put("objectIdField", getObjectIdField().getName());

	// JSONObject uniqueObject = new JSONObject();
	// uniqueObject.put("name", getObjectIdField());
	// uniqueObject.put("isSystemMaintained", true);
	// ret.put("uniqueIdField",uniqueObject);
	JSONArray fieldArray = new JSONArray();
	for (Field field : getFields()) {
	    JSONObject fieldObject = field.getJSON();
	    fieldArray.put(fieldObject);
	}
	ret.put("fields", fieldArray);

	JSONArray templateArray = new JSONArray();
	ret.put("templates", templateArray);
	return ret;
    }

    protected abstract StatisticsMessage getStatisticsMessage(WebRequest webRequest) throws GSException;

    public Field getObjectIdField() {
	List<Field> fields = getFields();
	for (Field field : fields) {
	    if (field.isObjectId()) {
		return field;
	    }
	}
	return null;
    }
    public Field getField(MetadataElement element) {
	List<Field> fields = getFields();
	for (Field field : fields) {
	    if (field.getMetadataElement()!=null &&field.getMetadataElement().equals(element)) {
		return field;
	    }
	}
	return null;
	
    }
    public Field getField(ESRIFieldType type) {
	List<Field> fields = getFields();
	for (Field field : fields) {
	    if (field.getType().equals(type)) {
		return field;
	    }
	}
	return null;
    }

    public static FeatureLayer getLayer(String id) {
	List<FeatureLayer> layers = getAvailableLayers();
	for (FeatureLayer layer : layers) {
	    if (layer.getId().equals(id)) {
		return layer;
	    }
	}
	return null;
    }

    public List<Field> getOutFields(String outFields) {
	List<Field> ret = new ArrayList<>();
	HashSet<String> outputFields = new HashSet<>();
	if (outFields == null || outFields.equals("*") || outFields.isEmpty()) {
	    for (Field field : getFields()) {
		outputFields.add(field.getName().toLowerCase());
	    }
	} else {
	    if (outFields.contains(",")) {
		String[] split = outFields.split(",");
		for (String s : split) {
		    outputFields.add(s.toLowerCase());
		}
	    } else {
		outputFields.add(outFields.toLowerCase());
	    }
	}
	for (Field field : getFields()) {

	    if (outputFields.contains(field.getName().toLowerCase())) {
		ret.add(field);
	    }
	}

	return ret ;
    }

    public Integer getPosition(Field field) {
	List<Field> fields = getFields();
	for (int i = 0; i < fields.size(); i++) {
	    Field f = fields.get(i);
	    if (field.getName().equals(f.getName())) {
		return i;
	    }
	}
	return -1;
    }


}
