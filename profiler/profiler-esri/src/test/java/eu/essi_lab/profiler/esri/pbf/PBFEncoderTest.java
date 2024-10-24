package eu.essi_lab.profiler.esri.pbf;

import java.io.InputStream;

import org.junit.Test;

import com.esri.arcgis.protobuf.FeatureCollection;
import com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer;

public class PBFEncoderTest {

    @Test
    public void testName() throws Exception {
	InputStream stream = PBFEncoderTest.class.getClassLoader().getResourceAsStream("result.pbf");
	FeatureCollectionPBuffer result = FeatureCollection.FeatureCollectionPBuffer.parseFrom(stream);

	//
	System.out.println(result);

//	String objectId = "oid";
//	String uniqueIdFieldName = "ufn";
//	int wkid = 4326;
//	UniqueIdField uniqueIdField = UniqueIdField.newBuilder().setName(uniqueIdFieldName).setIsSystemMaintained(true).build();
//	List<Field> fields = new ArrayList<>();
//	String fieldName = "name";
//	FieldType type = FieldType.esriFieldTypeBlob;
//	String alias = "alias";
//	Field field = Field.newBuilder().setName(fieldName).setFieldType(type).setAlias(alias).build();
//	fields.add(field);
//
//	List<Value> attributes = new ArrayList<>();
//	attributes.add(Value.newBuilder().setStringValue("test").build());
//	
//	List<Feature> features = new ArrayList<>();
//	Feature feature = Feature.newBuilder().setGeometry(Geometry.newBuilder().addCoords(234).addCoords(100).build()).//
//	addAllAttributes(attributes).
//		//
//		build();
//	features.add(feature);
//
//	SpatialReference spatialReference = SpatialReference.newBuilder().setWkid(wkid).setLatestWkid(wkid).build();
//	FeatureResult featureResult = FeatureResult.newBuilder()//
//		.setObjectIdFieldName(objectId)//
//		.setUniqueIdField(uniqueIdField)//
//		.setSpatialReference(spatialReference)//
//		.addAllFields(fields) //
//		.addAllFeatures(features)//
//		.build();
//	QueryResult queryResult = QueryResult.newBuilder().setFeatureResult(featureResult).build();
//	FeatureCollectionPBuffer featureCollection = FeatureCollection.FeatureCollectionPBuffer.newBuilder().setQueryResult(queryResult)
//		.build();
//	System.out.println(featureCollection);
    }
}
