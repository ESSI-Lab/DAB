/**
 *
 */
package eu.essi_lab.lib.utils;

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

/**
 * @author Fabrizio
 */

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;


/**
 * @author Fabrizio
 */
public class ShapeReader {

    private DataStore dataStore;

    private void init(Map<String, URL> connect) throws IOException {
	dataStore = DataStoreFinder.getDataStore(connect);
	if (dataStore == null) {
	    dataStore = new ShapefileDataStore(connect.get("url"));
	}
    }

    /**
     * @param uri
     * @throws IOException
     */
    public ShapeReader(URI uri) throws IOException {

	this(uri.toURL());
    }

    /**
     * @param url
     * @throws IOException
     */
    public ShapeReader(URL url) throws IOException {

	Map<String, URL> connect = new HashMap<>();
	connect.put("url", url);
	init(connect);
    }

    /**
     * @param path
     * @throws IOException
     */
    public ShapeReader(String path) throws IOException {

	this(new File(path).toURI().toURL());
    }

    /**
     * @param file
     * @throws IOException
     * @throws Exception
     */
    public ShapeReader(File file) throws IOException {

	if (file.getName().toLowerCase().endsWith(".zip")) {

	    Unzipper unzipper = new Unzipper(file);
	    unzipper.unzip();

	    unzipper.getOutputFolder().deleteOnExit();
	    File[] files = unzipper.getOutputFolder().listFiles();

	    for (File entry : files) {
		entry.deleteOnExit();
		if (entry.getName().toLowerCase().endsWith(".shp")) {
		    file = entry;
		}
	    }
	}

	Map<String, URL> connect = new HashMap<>();

	connect.put("url", file.toURI().toURL());

	init(connect);
    }

    /**
     * @param typeName
     * @return
     * @throws IOException
     * @throws Throwable
     */
    public BoundingBox getBBox(String typeName) throws IOException {

	return getBBox(dataStore.getFeatureSource(typeName).getFeatures());
    }

    /**
     * @return
     * @throws IOException
     */
    public List<String> getTypeNames() throws IOException {

	return Arrays.asList(dataStore.getTypeNames());
    }

    /**
     * @param imageBiggestSideSize
     * @return
     * @throws IOException
     */
    public BufferedImage getShapePreview(int imageBiggestSideSize) throws IOException {

	FeatureSource<SimpleFeatureType, SimpleFeature> features = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);

	Style style = SLD.createSimpleStyle(features.getSchema());

	Layer layer = new FeatureLayer(features, style);
	MapContent content = new MapContent();
	content.addLayer(layer);

	Double diffx = features.getBounds().getMaxX() - features.getBounds().getMinX();

	Double diffy = features.getBounds().getMaxY() - features.getBounds().getMinY();

	Size size = new Size(diffx.intValue(), diffy.intValue(), imageBiggestSideSize);

	BufferedImage image = new BufferedImage(size.getWidth(), size.getHeight(), BufferedImage.TYPE_INT_ARGB);
	Graphics2D graphics = image.createGraphics();
	Rectangle screenArea = new Rectangle(0, 0, size.getWidth(), size.getHeight());
	ReferencedEnvelope mapArea = features.getBounds();

	StreamingRenderer renderer = new StreamingRenderer();
	renderer.setMapContent(content);
	renderer.paint(graphics, screenArea, mapArea);

	return image;
    }

    /**
     * @param typeName
     * @return
     * @throws IOException
     * @throws Throwable
     */
    public SimpleFeatureCollection getFeatures(String typeName) throws IOException {

	return dataStore.getFeatureSource(typeName).getFeatures();
    }

    /**
     * @param typeName
     * @param timePropertyName
     * @return a list of two strings: the first one is the time start, the second one is the time end
     * @throws IOException
     * @throws Throwable
     */
    public List<String> getTimeExtent(String typeName, String timePropertyName) throws IOException {

	return getTimeExtent(dataStore.getFeatureSource(typeName).getFeatures(), timePropertyName);
    }



    private BoundingBox getBBox(FeatureCollection<?, ?> collection) {

	FeatureIterator<?> iterator = collection.features();
	BoundingBox envelope = null;

	try {

	    while (iterator.hasNext()) {

		Feature feature = iterator.next();
		BoundingBox sourceGeometry = feature.getDefaultGeometryProperty().getBounds();

		if (envelope == null) {
		    
		    envelope = sourceGeometry;

		} else {

		    envelope.include(sourceGeometry);

		}
	    }
	} finally {

	    iterator.close();
	}

	return envelope;
    }

    private List<String> getTimeExtent(FeatureCollection<?, ?> collection, String timePropertyName) {
	FeatureIterator<?> iterator = collection.features();

	Date dateMin = null;
	Date dateMax = null;

	Boolean foundDate = false;

	try {

	    while (iterator.hasNext()) {
		Feature feature = iterator.next();
		Property acq = feature.getProperty(timePropertyName);

		Date date = null;
		try {

		    date = ISO8601DateTimeUtils.parseISO8601(acq.getValue().toString());

		} catch (IllegalArgumentException ex) {

		    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
		}

		if (date == null) {
		    continue;
		}

		foundDate = true;

		long millis = date.getTime();

		if (dateMax == null)
		    dateMax = date;
		else {
		    if (dateMax.getTime() < millis) {
			dateMax = date;
		    }
		}

		if (dateMin == null) {
		    dateMin = date;
		} else {
		    if (dateMin.getTime() > millis) {
			dateMin = date;
		    }
		}
	    }
	} finally {
	    iterator.close();
	}

	List<String> ret = new ArrayList<>();

	if (!foundDate) {
	    return ret;
	}

	ret.add(ISO8601DateTimeUtils.getISO8601DateTime(dateMin));
	ret.add(ISO8601DateTimeUtils.getISO8601DateTime(dateMax));

	return ret;
    }
}

class Size {
    private int width;
    private int height;
    private int maxSize;

    public Size(int width, int height, int maxSize) {
	if (width > height) {
	    this.width = maxSize;
	    this.height = (height * maxSize) / width;
	} else {
	    this.height = maxSize;
	    this.width = (width * maxSize) / height;
	}
    }

    public int getWidth() {
	return width;
    }

    public int getHeight() {
	return height;
    }

    public int getMaxSize() {
	return maxSize;
    }
}
