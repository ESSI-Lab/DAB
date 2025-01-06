package eu.essi_lab.profiler.esri.pbf;

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

import java.io.ByteArrayInputStream;

import org.json.JSONObject;

public class PBFEncoder {

    
    public static void main(String[] args) {

    }

    public static ByteArrayInputStream encode(JSONObject input) {
	return null;
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
