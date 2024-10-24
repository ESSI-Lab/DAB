package eu.essi_lab.model.index;

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

import eu.essi_lab.lib.xml.NameSpace;

/**
 * @author Fabrizio
 */
public class IndexedElementInfo {

    private String elementName;
    private String dataBaseImpl;
    private String scalarType;
    private String indexType;
    private String nsURI;

    public IndexedElementInfo() {

    }

    /**
     * @param elementName
     * @param dataBaseImpl
     * @param indexType
     * @param scalarType
     */
    public IndexedElementInfo(String elementName, String dataBaseImpl, String indexType, String scalarType) {
	this(elementName, NameSpace.GS_DATA_MODEL_SCHEMA_URI, dataBaseImpl, indexType, scalarType);
    }

    /**
     * @param elementName
     * @param nsURI
     * @param dataBaseImpl
     * @param indexType
     * @param scalarType
     */
    public IndexedElementInfo(String elementName, String nsURI, String dataBaseImpl, String indexType, String scalarType) {
	this.elementName = elementName;
	this.nsURI = nsURI;
	this.dataBaseImpl = dataBaseImpl;
	this.indexType = indexType;
	this.scalarType = scalarType;
    }

    public String getNsURI() {
	return nsURI;
    }

    public void setNsURI(String nsURI) {
	this.nsURI = nsURI;
    }

    public String getDataBaseImpl() {
	return dataBaseImpl;
    }

    public void setDataBaseImpl(String targetDataBase) {
	this.dataBaseImpl = targetDataBase;
    }

    public String getScalarType() {
	return scalarType;
    }

    public String getElementName() {
	return elementName;
    }

    /**
     * E.g: "string"
     *
     * @param scalarType
     */
    public void setScalarType(String scalarType) {
	this.scalarType = scalarType;
    }

    public String getIndexType() {
	return indexType;
    }

    /**
     * E.g: "element range index"
     *
     * @param indexType
     */
    public void setIndexType(String indexType) {
	this.indexType = indexType;
    }

    @Override
    public boolean equals(Object object) {

	if (object == null)
	    return false;

	if (!(object instanceof IndexedElementInfo))
	    return false;

	IndexedElementInfo info = (IndexedElementInfo) object;
	return compare(this.getElementName(), info.getElementName()) //
		&& compare(this.getNsURI(), info.getNsURI()) //
		&& compare(this.getDataBaseImpl(), info.getDataBaseImpl()) //
		&& compare(this.getIndexType(), info.getIndexType()) //
		&& compare(this.getScalarType(), info.getScalarType()); //
    }

    @Override
    public String toString() {

	return getElementName() + ", " + //
		getNsURI() + ", " + //
		getDataBaseImpl() + ", " + //
		getIndexType() + ", " + //
		getScalarType(); //
    }

    private boolean compare(String a, String b) {

	if (a == null) {
	    a = "";
	}

	if (b == null) {
	    b = "";
	}

	return a.equals(b);
    }

}
