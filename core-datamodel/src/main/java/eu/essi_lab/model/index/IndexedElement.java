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

import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.QualifiedName;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;

/**
 * In the GI-suite, "indexed elements" are simple XML elements with a textual content. Each indexed element is related
 * to some kind of {@link GSResource} property with a name and a value. For example given a property with name "title"
 * and value "Sea
 * Surface Temperature", the correspondent indexed element is:
 * <code> &lt;title&gt;Sea Surface Temperature&lt;/title&gt;</code>; "title" is the <i>indexed element name</i> and "Sea
 * Surface Temperature" is its <i>value</i>.<br>
 * <br>
 * These elements must be written in {@link GSResource#getIndexesMetadata()}s before to store the resource in to a
 * Database during the harvesting phase. When the {@link GSResource} is stored, according to the indexed elements that
 * have been previously written in it, the Database builds the correspondent "in memory indexes" in order to speed up
 * queries which involves those indexed
 * elements.<br>
 * <br>
 * In the example above, the Database builds an in memory index called "title" with the values of the
 * "title" indexed elements from all the stored {@link GSResource}s. Queries which use the "title" constraint are
 * quickly resolved by looking in the title index instead of looking in to the stored XML documents.<br>
 * <br>
 * Of course in order to build a particular memory index,
 * the Database must be correctly configured; how it can be done depends from the specific Database implementation
 * and it's out of the scope of this documentation.<br>
 * <br>
 * 
 * @see IndexedMetadataElement
 * @see HarmonizedMetadata#getIndexesMetadata()
 * @author Fabrizio
 */
public class IndexedElement {

    private String elementName;
    private List<IndexedElementInfo> indexInfoList;
    private List<String> values;

    /**
     * Creates a new <code>IndexedElement</code> with the supplied <code>elementName</code>
     * 
     * @param elementName a non <code>null</code> string containing only alphabetical characters and "_" character
     */
    public IndexedElement(String elementName) {

	this(elementName, new ArrayList<>());
    }

    /**
     * Creates a new <code>IndexedElement</code> with the supplied <code>elementName</code> and <code>value</code>
     * 
     * @param elementName a non <code>null</code> string containing only alphabetical characters and "_" character
     * @param value a non <code>null</code> string, empty string is admitted
     */
    public IndexedElement(String elementName, String value) {

	this(elementName, new ArrayList<>());
	getValues().add(value);
    }

    /**
     * Creates a new <code>IndexedElement</code> with the supplied <code>elementName</code> and <code>values</code>
     * 
     * @param elementName a non <code>null</code> string containing only alphabetical characters and "_" character
     * @param values a non <code>null</code> list of string, empty string is admitted
     */
    public IndexedElement(String elementName, List<String> values) {

	this.elementName = elementName;
	this.values = values;
	this.indexInfoList = new ArrayList<IndexedElementInfo>();
    }

    /**
     * Get this <code>IndexedElement</code> name
     * 
     * @return
     */
    public String getElementName() {

	return elementName;
    }

    /**
     * Return the list of values of this indexed element
     * 
     * @implNote This is a live list
     * @return
     */
    public List<String> getValues() {

	return values;
    }

    /**
     * @param dataBaseImpl
     * @return
     */
    public IndexedElementInfo getInfo(String dataBaseImpl) {

	List<IndexedElementInfo> infoList = getInfoList();
	for (IndexedElementInfo indexInfo : infoList) {
	    if (indexInfo.getDataBaseImpl().equals(dataBaseImpl)) {
		return indexInfo;
	    }
	}

	return null;
    }

    /**
     * @return
     */
    public List<IndexedElementInfo> getInfoList() {

	return indexInfoList;
    }

    /**
     * @return
     */
    public QualifiedName asQualifiedName() {

	return new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL.getURI(), elementName, NameSpace.GI_SUITE_DATA_MODEL.getPrefix());
    }
}
