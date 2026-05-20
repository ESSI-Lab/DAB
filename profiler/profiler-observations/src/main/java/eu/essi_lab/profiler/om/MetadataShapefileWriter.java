package eu.essi_lab.profiler.om;

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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.geotools.api.data.FeatureWriter;
import org.geotools.api.data.Transaction;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * Writes observation metadata to an ESRI shapefile (point layer, WGS84).
 * <p>
 * Appends features via {@link ShapefileDataStore#getFeatureWriterAppend} in batches. Attribute names are
 * limited to ten characters; see the {@code *_attributes.csv} file in the zip for a short description of each DBF field.
 * <p>
 * Reorder shapefile attributes by editing {@link #COLUMNS} only (geometry is always first).
 */
public class MetadataShapefileWriter extends AbstractMetadataWriter implements AutoCloseable {

    static final String ATTRIBUTES_FILE_SUFFIX = "_attributes.csv";

    private static final String GEOM_FIELD = "the_geom";

    /**
     * dBase III max record size is 4000 bytes; keep text fields short so the DBF header stays valid.
     */
    private static final int DBF_STRING_LENGTH = 128;

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    /**
     * Shapefile attribute order and DBF names (max 10 characters per name).
     */
    private static final ShapefileColumn[] COLUMNS = { //
	    column("obs_id", "Observation identifier", AbstractMetadataWriter::extractObservationId), //
	    column("country", "Country", AbstractMetadataWriter::extractCountry), //
	    column("prov_id", "Provider identifier", AbstractMetadataWriter::extractProviderId), //
	    column("prov_label", "Provider label", AbstractMetadataWriter::extractProviderLabel), //
	    column("feat_id", "Feature of interest identifier", AbstractMetadataWriter::extractFeatureId), //
	    column("feat_name", "Feature of interest name", AbstractMetadataWriter::extractFeatureName), //
	    column("latitude", "Latitude", AbstractMetadataWriter::extractLatitude), //
	    column("longitude", "Longitude", AbstractMetadataWriter::extractLongitude), //
	    column("altitude", "Altitude", AbstractMetadataWriter::extractAltitude), //
	    column("time_begin", "Phenomenon time begin", AbstractMetadataWriter::extractPhenomenonBegin), //
	    column("time_end", "Phenomenon time end", AbstractMetadataWriter::extractPhenomenonEnd), //
	    column("prop_title", "Observed property title", AbstractMetadataWriter::extractObservedPropertyTitle), //
	    column("prop_uri", "Observed property URI", AbstractMetadataWriter::extractObservedPropertyUri), //
	    column("uom", "Unit of measure", AbstractMetadataWriter::extractUom) , //
	    column("agg_duratn", "Aggregation duration", AbstractMetadataWriter::extractAggregationDuration), //
	    column("interpoltn", "Interpolation type", AbstractMetadataWriter::extractInterpolationType), //
	    column("resolution", "Intended observation spacing", AbstractMetadataWriter::extractIntendedObservationSpacing), //

    };

    private final File directory;

    private final String baseName;

    private final String attributesFileName;

    private final SimpleFeatureType featureType;

    private final SimpleFeatureBuilder featureBuilder;

    private final List<SimpleFeature> buffer = new ArrayList<>();

    private ShapefileDataStore dataStore;

    private String typeName;

    private int totalWritten;

    private int anonymousRow;

    public MetadataShapefileWriter(File directory, String baseName) throws IOException {
	super();
	this.directory = directory;
	this.baseName = baseName;
	this.attributesFileName = baseName + ATTRIBUTES_FILE_SUFFIX;
	this.featureType = buildFeatureType();
	this.featureBuilder = new SimpleFeatureBuilder(featureType);
	writeAttributesFile(directory);
    }

    public void append(JSONObject observation) throws IOException {
	buffer.add(buildFeature(observation));
	if (buffer.size() >= OMRequestUtils.METADATA_PAGE_SIZE) {
	    flushBuffer();
	}
    }

    @Override
    public void close() throws IOException {
	flushBuffer();
	if (totalWritten == 0) {
	    disposeStore();
	    throw new IOException("No observations were written to the shapefile (query returned zero results)");
	}
	disposeStore();
    }

    public int getTotalWritten() {
	return totalWritten;
    }

    private void flushBuffer() throws IOException {
	if (buffer.isEmpty()) {
	    return;
	}
	ensureStore();
	try (FeatureWriter<SimpleFeatureType, SimpleFeature> writer = dataStore.getFeatureWriterAppend(Transaction.AUTO_COMMIT)) {
	    for (SimpleFeature built : buffer) {
		SimpleFeature target = writer.next();
		target.setAttributes(built.getAttributes());
		writer.write();
	    }
	}
	totalWritten += buffer.size();
	buffer.clear();
    }

    private void ensureStore() throws IOException {
	if (dataStore != null) {
	    return;
	}
	File shpFile = new File(directory, baseName + ".shp");
	Map<String, Serializable> params = new HashMap<>();
	params.put(ShapefileDataStoreFactory.URLP.key, shpFile.toURI().toURL());

	ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
	dataStore = (ShapefileDataStore) factory.createNewDataStore(params);
	dataStore.setCharset(StandardCharsets.UTF_8);
	dataStore.createSchema(featureType);
	typeName = dataStore.getTypeNames()[0];
    }

    private void disposeStore() {
	if (dataStore != null) {
	    dataStore.dispose();
	    dataStore = null;
	}
    }

    private SimpleFeature buildFeature(JSONObject observation) {
	featureBuilder.reset();

	Double latitude = parseDouble(extractLatitude(observation));
	Double longitude = parseDouble(extractLongitude(observation));

	Point point = null;
	if (latitude != null && longitude != null) {
	    point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
	}

	featureBuilder.add(point);
	for (ShapefileColumn column : COLUMNS) {
	    featureBuilder.add(sanitizeDbfString(column.extract(observation)));
	}

	String fid = observation.optString("id", null);
	if (fid == null || fid.isEmpty()) {
	    fid = "row-" + (++anonymousRow);
	}
	return featureBuilder.buildFeature(fid);
    }

    private static SimpleFeatureType buildFeatureType() {
	SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
	builder.setName("metadata");
	builder.setCRS(DefaultGeographicCRS.WGS84);
	builder.add(GEOM_FIELD, Point.class);
	for (ShapefileColumn column : COLUMNS) {
	    addStringField(builder, column.shpName);
	}
	return builder.buildFeatureType();
    }

    private static void addStringField(SimpleFeatureTypeBuilder builder, String name) {
	AttributeTypeBuilder typeBuilder = new AttributeTypeBuilder();
	typeBuilder.setBinding(String.class);
	typeBuilder.setLength(DBF_STRING_LENGTH);
	typeBuilder.setNillable(true);
	builder.add(typeBuilder.buildDescriptor(name));
    }

    private static String buildAttributesFileContent() {
	StringBuilder csv = new StringBuilder("field,description\n");
	for (ShapefileColumn column : COLUMNS) {
	    csv.append(column.shpName).append(',').append(column.description).append('\n');
	}
	return csv.toString();
    }

    private static ShapefileColumn column(String shpName, String description, Function<JSONObject, String> extractor) {
	return new ShapefileColumn(shpName, description, extractor);
    }

    private static Double parseDouble(String value) {
	if (value == null || value.isEmpty()) {
	    return null;
	}
	try {
	    return Double.valueOf(value);
	} catch (NumberFormatException ex) {
	    return null;
	}
    }

    private static String sanitizeDbfString(String value) {
	if (value == null) {
	    return "";
	}
	String cleaned = value.replace("\0", "").replace("\r", " ").replace("\n", " ");
	if (cleaned.length() > DBF_STRING_LENGTH) {
	    cleaned = cleaned.substring(0, DBF_STRING_LENGTH);
	}
	return cleaned;
    }

    private void writeAttributesFile(File directory) throws IOException {
	Files.writeString(directory.toPath().resolve(attributesFileName), buildAttributesFileContent(), StandardCharsets.UTF_8);
    }

    private static final class ShapefileColumn {
	private final String shpName;
	private final String description;
	private final Function<JSONObject, String> extractor;

	private ShapefileColumn(String shpName, String description, Function<JSONObject, String> extractor) {
	    this.shpName = shpName;
	    this.description = description;
	    this.extractor = extractor;
	}

	private String extract(JSONObject observation) {
	    return extractor.apply(observation);
	}
    }
}
