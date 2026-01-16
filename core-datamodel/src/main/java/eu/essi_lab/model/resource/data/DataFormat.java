package eu.essi_lab.model.resource.data;

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

import java.io.Serializable;

/**
 * Extensible class of data formats
 * 
 * @author boldrini
 */
public class DataFormat implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2063659270084375094L;

    public static DataFormat WATERML_1_1() {
	return new DataFormat(FormatType.WATERML_1_1);
    }

    public static DataFormat WATERML_2_0() {
	return new DataFormat(FormatType.WATERML_2_0);
    }

    public static DataFormat NETCDF() {
	return new DataFormat(FormatType.NETCDF);
    }

    public static DataFormat NETCDF_3() {
	return new DataFormat(FormatType.NETCDF_3);
    }

    public static DataFormat NETCDF_4() {
	return new DataFormat(FormatType.NETCDF_4);
    }

    public static DataFormat IMAGE_JPG() {
	return new DataFormat(FormatType.IMAGE_JPG);
    }

    public static DataFormat IMAGE_GEOTIFF() {
	return new DataFormat(FormatType.IMAGE_GEOTIFF);
    }

    public static DataFormat IMAGE_TIFF() {
	return new DataFormat(FormatType.IMAGE_TIFF);
    }

    public static DataFormat IMAGE_PNG() {
	return new DataFormat(FormatType.IMAGE_PNG);
    }

    public static DataFormat GML_3_2() {
	return new DataFormat(FormatType.GML_3_2);
    }

    public static DataFormat GML_3_1() {
	return new DataFormat(FormatType.GML_3_1);
    }

    public static DataFormat O_M() {
	return new DataFormat(FormatType.O_M);
    }

    public static DataFormat RDB() {
	return new DataFormat(FormatType.RDB);
    }

    public static DataFormat DDS() {
	return new DataFormat(FormatType.DDS);
    }

    public static DataFormat DAS() {
	return new DataFormat(FormatType.DAS);
    }
    
    public static DataFormat WKT() {
	return new DataFormat(FormatType.WKT);
    }

    public static DataFormat CSV() {
	return new DataFormat(FormatType.CSV);
    }

    public enum FormatType {
	WATERML, //
	WATERML_1_1(WATERML), //
	WATERML_2_0(WATERML), //
	NETCDF, //
	NETCDF_3(NETCDF), //
	NETCDF_4(NETCDF), //
	IMAGE_JPG, //
	IMAGE_TIFF, //
	IMAGE_GEOTIFF(IMAGE_TIFF), //
	IMAGE_PNG, //
	GML, //
	GML_3_2(GML), //
	GML_3_1(GML), //
	RDB, //
	DDS, //
	DAS, //
	WKT, //
	CSV, //
	O_M;

	private FormatType parent = null;

	public FormatType getParent() {
	    return parent;
	}

	FormatType() {

	}

	FormatType(FormatType parent) {
	    this.parent = parent;
	}

	public boolean isSubTypeOf(FormatType parent) {
	    if (this.parent != null && this.parent.equals(parent)) {
		return true;
	    }
	    return false;
	}
    }

    private String identifier;
    private FormatType type = null;

    /**
     * A type, from the enum
     * 
     * @return
     */
    public FormatType getType() {
	return type;
    }

    /**
     * @param identifier
     */
    private DataFormat(String identifier) {

	this.identifier = identifier;
    }

    public DataFormat(FormatType type) {

	this.type = type;
	this.identifier = type.toString();
    }

    /**
     * An identifier, such as "image/jpg" or "IMAGE_JPG"
     * 
     * @return
     */
    public String getIdentifier() {

	return identifier;
    }

    /**
     * @param identifier
     * @return
     */
    public static DataFormat fromIdentifier(String identifier) {

	DataFormat ret = new DataFormat(identifier);

	if (identifier.toLowerCase().contains("o_m")) {
	    ret.setType(FormatType.O_M);
	} else if (identifier.toLowerCase().contains("gml")) {
	    if (identifier.toLowerCase().contains("3_2") || identifier.toLowerCase().contains("3.2")) {
		ret.setType(FormatType.GML_3_2);
	    } else if (identifier.toLowerCase().contains("3_1") || identifier.toLowerCase().contains("3.1")) {
		ret.setType(FormatType.GML_3_1);
	    } else {
		ret.setType(FormatType.GML);
	    }
	} else if (identifier.toLowerCase().contains("cdf")) {
	    if (identifier.toLowerCase().contains("3")) {
		ret.setType(FormatType.NETCDF_3);
	    } else if (identifier.toLowerCase().contains("4")) {
		ret.setType(FormatType.NETCDF_4);
	    } else {
		ret.setType(FormatType.NETCDF);
	    }
	} else if (identifier.toLowerCase().contains("wml") || identifier.toLowerCase().contains("waterml")) {
	    if (identifier.toLowerCase().contains("1_1") || identifier.toLowerCase().contains("1.1")) {
		ret.setType(FormatType.WATERML_1_1);
	    } else if (identifier.toLowerCase().contains("2_0") || identifier.toLowerCase().contains("2.0")) {
		ret.setType(FormatType.WATERML_2_0);
	    } else {
		ret.setType(FormatType.WATERML);
	    }
	} else if (identifier.toLowerCase().contains("png")) {
	    ret.setType(FormatType.IMAGE_PNG);
	} else if (identifier.toLowerCase().contains("jpg") || identifier.toLowerCase().contains("jpeg")) {
	    ret.setType(FormatType.IMAGE_JPG);
	} else if (identifier.toLowerCase().contains("geotiff") || identifier.toLowerCase().contains("gtiff")) {
	    ret.setType(FormatType.IMAGE_GEOTIFF);
	} else if (identifier.toLowerCase().contains("tiff")) {
	    ret.setType(FormatType.IMAGE_TIFF);
	} else if (identifier.toLowerCase().contains("dds")) {
	    ret.setType(FormatType.DDS);
	} else if (identifier.toLowerCase().contains("wkt")) {
	    ret.setType(FormatType.WKT);
	} else if (identifier.toLowerCase().contains("das")) {
	    ret.setType(FormatType.DAS);
	} else if (identifier.toLowerCase().contains("csv")) {
	    ret.setType(FormatType.CSV);
	}

	return ret;
    }

    public void setType(FormatType type) {
	this.type = type;

    }

    public boolean isSubTypeOf(DataFormat format) {

	FormatType parentType = format.getType();
	if (parentType != null && getType() != null) {
	    if (getType().isSubTypeOf(parentType)) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof DataFormat) {
	    DataFormat format = (DataFormat) obj;

	    if (format.getType() != null && getType() != null //
		    && format.getType().equals(getType())) {
		return true;
	    }

	    String id1 = format.getIdentifier().toLowerCase().replace("image_", "").replace("image/", "");
	    String id2 = getIdentifier().toLowerCase().replace("image_", "").replace("image/", "");

	    return id1.equals(id2);
	}
	return super.equals(obj);
    }

    @Override
    public int hashCode() {

	if (type != null) {
	    return type.toString().hashCode();
	}
	return getIdentifier().hashCode();
    }

    @Override
    public String toString() {

	return getIdentifier();
    }


}
