package eu.essi_lab.profiler.esri.feature;

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

import java.util.Date;
import java.util.UUID;

/**
 * @author boldrini
 */
public enum ESRIFieldType {

    OID("esriFieldTypeOID", com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.FieldType.esriFieldTypeOID), //
    DATE("esriFieldTypeDate", com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.FieldType.esriFieldTypeDate), //
    START_DATE("esriFieldTypeDate", com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.FieldType.esriFieldTypeDate), //
    END_DATE("esriFieldTypeDate", com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.FieldType.esriFieldTypeDate), //
    DOUBLE("esriFieldTypeDouble", com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.FieldType.esriFieldTypeDouble), //
    LATITUDE("esriFieldTypeDouble", com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.FieldType.esriFieldTypeDouble), //
    LONGITUDE("esriFieldTypeDouble", com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.FieldType.esriFieldTypeDouble), //
    STRING("esriFieldTypeString", com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.FieldType.esriFieldTypeString), //
    WHOS_PAGE("esriFieldTypeString", com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.FieldType.esriFieldTypeString), // link
																	 // to
																	 // WHOS
																	 // page!
    ISO_TITLE("esriFieldTypeString", com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.FieldType.esriFieldTypeString), //
    BOOLEAN("esriFieldTypeBoolean", com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.FieldType.esriFieldTypeString), // to
																	// check!
    INTEGER("esriFieldTypeInteger", com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.FieldType.esriFieldTypeInteger);

    ESRIFieldType(String id, com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.FieldType type) {
	this.id = id;
	this.type = type;
    }

    private String id;

    public String getId() {
	return id;
    }

    private com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.FieldType type;

    public com.esri.arcgis.protobuf.FeatureCollection.FeatureCollectionPBuffer.FieldType getType() {
	return type;
    }

    public static Class<?> encode(ESRIFieldType type) {
	if (type.equals(OID)) {
	    return Integer.class;
	}
	if (type.equals(DATE)//
		|| type.equals(START_DATE)//
		|| type.equals(END_DATE)//
	) {
	    return Date.class;
	}
	if (type.equals(DOUBLE)//
		|| type.equals(LATITUDE)//
		|| type.equals(LONGITUDE)//
	) {
	    return Double.class;
	}
	if (type.equals(STRING)//
		|| type.equals(WHOS_PAGE)//
	) {
	    return String.class;
	}
	return null;
    }

    public static ESRIFieldType decode(Class<?> type) {
	if (type.equals(Integer.class)) {
	    return INTEGER;
	}
	if (type.equals(UUID.class)) {
	    return OID;
	}
	if (type.equals(Date.class)) {
	    return DATE;
	}
	if (type.equals(Double.class)) {
	    return DOUBLE;
	}
	if (type.equals(String.class)) {
	    return STRING;
	}
	if (type.equals(Boolean.class)) {
	    return BOOLEAN;
	}
	return null;
    }

    public Class<?> encode() {
	return encode(this);
    }

}
