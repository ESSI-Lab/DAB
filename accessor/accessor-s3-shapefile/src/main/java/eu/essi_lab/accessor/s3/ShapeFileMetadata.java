package eu.essi_lab.accessor.s3;

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

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.FeatureSource;
import org.geotools.api.feature.GeometryAttribute;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.geometry.BoundingBox;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;

@XmlRootElement
public class ShapeFileMetadata {

    @XmlElements(@XmlElement)
    private List<FeatureMetadata> features = new ArrayList<FeatureMetadata>();
    @XmlElement
    private String url;

    public ShapeFileMetadata(File shpFile) throws Exception {
	// Prepare a map to point to the .shp file
	Map<String, Object> map = new HashMap<>();
	map.put("url", shpFile.toURI().toURL());

	// Open the Shapefile using DataStore
	DataStore dataStore = DataStoreFinder.getDataStore(map);
	String typeName = dataStore.getTypeNames()[0];

	FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(typeName);
	SimpleFeatureCollection featureCollection = (SimpleFeatureCollection) featureSource.getFeatures();

	try (SimpleFeatureIterator features = featureCollection.features()) {
	    // Iterate through features and print attributes and bounding box
	    while (features.hasNext()) {
		SimpleFeature feature = features.next();

		FeatureMetadata f = new FeatureMetadata();
		// Print feature attributes
		// System.out.println("Feature ID: " + feature.getID());
		f.setId(feature.getID());
		GeometryAttribute geomAttr = feature.getDefaultGeometryProperty();
		for (AttributeDescriptor attr : feature.getFeatureType().getAttributeDescriptors()) {
		    if (attr.getName().equals(geomAttr.getName())) {
			continue;
		    }
		    Object a = feature.getAttribute(attr.getName());
		    f.getAttributes().put(attr.getName().getLocalPart(), a.toString());
		    // System.out.println("X" + attr.getName() + ": " + a);

		}

		// Get and print the bounding box (geometry extent)
		BoundingBox boundingBox = feature.getBounds();
		// System.out.println("Bounding Box: " + boundingBox);
		// System.out.println("---------------------------------");

		f.setWest(new BigDecimal(boundingBox.getMinX()));
		f.setEast(new BigDecimal(boundingBox.getMaxX()));
		f.setNorth(new BigDecimal(boundingBox.getMaxY()));
		f.setSouth(new BigDecimal(boundingBox.getMinY()));
		getFeatures().add(f);

	    }
	}

	// Close the data store
	dataStore.dispose();

    }

    @XmlTransient
    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    @XmlTransient
    public List<FeatureMetadata> getFeatures() {
	return features;
    }

    public void setFeatures(List<FeatureMetadata> features) {
	this.features = features;
    }

}
