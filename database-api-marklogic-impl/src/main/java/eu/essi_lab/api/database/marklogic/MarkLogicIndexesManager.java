package eu.essi_lab.api.database.marklogic;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.marklogic.xcc.ResultItem;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.exceptions.RequestException;

import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.indexes.CustomIndexedElements;
import eu.essi_lab.indexes.IndexedElements;
import eu.essi_lab.indexes.IndexedMetadataElements;
import eu.essi_lab.indexes.IndexedResourceElements;
import eu.essi_lab.indexes.marklogic.MarkLogicIndexTypes;
import eu.essi_lab.indexes.marklogic.MarkLogicScalarType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.index.IndexedElementInfo;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class MarkLogicIndexesManager {

    private MarkLogicDatabase markLogicDB;
    private HashMap<String, String> map;

    /**
     * @param markLogicDB
     * @param init
     * @throws Exception
     */
    public MarkLogicIndexesManager(MarkLogicDatabase markLogicDB, boolean init) throws Exception {

	this.markLogicDB = markLogicDB;

	map = new HashMap<String, String>();
	map.put("ml", "http://marklogic.com/xdmp/database");

	if (init) {

	    List<IndexedElementInfo> supportedIndexes = getSupportedIndexes();

	    // --------------------------
	    //
	    // custom indexes
	    //
	    PluginsLoader<CustomIndexedElements> loader = new PluginsLoader<>();
	    List<CustomIndexedElements> plugins = loader.loadPlugins(CustomIndexedElements.class);

	    ArrayList<IndexedElementInfo> custom = new ArrayList<>();
	    for (CustomIndexedElements p : plugins) {
		custom.addAll(p.getIndexesInfo(DatabaseImpl.MARK_LOGIC));
	    }

	    // excludes indexes with invalid names
	    custom.stream().//
		    filter(index -> CustomIndexedElements.checkName(index.getElementName())).//
		    forEach(index -> supportedIndexes.add(index));

	    // ---------------------------------------------------------
	    //
	    // preliminary settings which do no require a server restart
	    //
	    // ---------------------------------------------------------

	    GSLoggerFactory.getLogger(getClass()).info("Performing preliminary settings STARTED");

	    // 1) disables the auto index detection
	    GSLoggerFactory.getLogger(getClass()).info("Disabling auto index detection STARTED");
	    String indexDetectionQuery = getSetIndexDetectionQuery(false);
	    String saveConfigQuery = getSaveConfigQuery(indexDetectionQuery);
	    markLogicDB.execXQuery(saveConfigQuery);
	    GSLoggerFactory.getLogger(getClass()).info("Disabling auto index detection ENDED");

	    // 2) disables the automatic reindexing
//	    GSLoggerFactory.getLogger(getClass()).info("Disabling automatic reindexing STARTED");
//	    String setReindexerEnabledQuery = getSetReindexerEnabledQuery(false);
//	    saveConfigQuery = getSaveConfigQuery(setReindexerEnabledQuery);
//	    markLogicDB.execXQuery(saveConfigQuery);
//	    GSLoggerFactory.getLogger(getClass()).info("Disabling automatic reindexing ENDED");

	    // 3) set a low reindexer throttle (low CPU work when reindexing)
	    GSLoggerFactory.getLogger(getClass()).info("Setting low reindexer throttle STARTED");
	    String setReindexerThrottleQuery = getSetReindexerThrottleQuery(2);
	    saveConfigQuery = getSaveConfigQuery(setReindexerThrottleQuery);
	    markLogicDB.execXQuery(saveConfigQuery);
	    GSLoggerFactory.getLogger(getClass()).info("Setting low reindexer throttle ENDED");

	    // 4) enables the triple index
	    GSLoggerFactory.getLogger(getClass()).info("Enabling triple index STARTED");
	    String enableTripleIndex = getEnableTripleIndexQuery();
	    saveConfigQuery = getSaveConfigQuery(enableTripleIndex);
	    markLogicDB.execXQuery(saveConfigQuery);
	    GSLoggerFactory.getLogger(getClass()).info("Enabling triple index ENDED");

	    GSLoggerFactory.getLogger(getClass()).info("Performing preliminary settings ENDED");

	    //
	    // manages the range element indexes
	    //
	    GSLoggerFactory.getLogger(getClass()).info("Managing range element indexes STARTED");
	    manageRangeElementIndexes(supportedIndexes);
	    GSLoggerFactory.getLogger(getClass()).info("Managing range element indexes ENDED");

	    //
	    // manages the geo spatial indexes
	    //
	    GSLoggerFactory.getLogger(getClass()).info("Managing geo spatial indexes STARTED");
	    manageGeoSpatialIndexes(supportedIndexes);
	    GSLoggerFactory.getLogger(getClass()).info("Managing geo spatial indexes ENDED");

	    //
	    // manages the element included in the word query
	    //
	    GSLoggerFactory.getLogger(getClass()).info("Managing word query elements STARTED");
	    manageWordQueryElements();
	    GSLoggerFactory.getLogger(getClass()).info("Managing word query elements ENDED");

	    // ---------------------------------------------------------
	    //
	    // enables reindexing
	    //
	    // ---------------------------------------------------------

	    GSLoggerFactory.getLogger(getClass()).info("Finalizing configuration STARTED");

	    // 1) enables the auto index detection
	    GSLoggerFactory.getLogger(getClass()).info("Enabling auto index detection STARTED");
	    indexDetectionQuery = getSetIndexDetectionQuery(true);
	    saveConfigQuery = getSaveConfigQuery(indexDetectionQuery);
	    markLogicDB.execXQuery(saveConfigQuery);
	    GSLoggerFactory.getLogger(getClass()).info("Enabling auto index detection ENDED");

	    // 2) enables the automatic reindexing
//	    GSLoggerFactory.getLogger(getClass()).info("Enabling automatic reindexing STARTED");
//	    setReindexerEnabledQuery = getSetReindexerEnabledQuery(true);
//	    saveConfigQuery = getSaveConfigQuery(setReindexerEnabledQuery);
//	    markLogicDB.execXQuery(saveConfigQuery);
//	    GSLoggerFactory.getLogger(getClass()).info("Enabling automatic reindexing ENDED");

	    GSLoggerFactory.getLogger(getClass()).info("Finalizing configuration ENDED");
	}
    }

    /**
     * @param markLogicDB
     * @throws Exception
     */
    public MarkLogicIndexesManager(MarkLogicDatabase markLogicDB) throws Exception {

	this(markLogicDB, true);
    }

    /**
     * @return
     */
    public List<IndexedElementInfo> getSupportedIndexes() {

	List<IndexedElementInfo> supportedIndexes = IndexedMetadataElements.getIndexesInfo(DatabaseImpl.MARK_LOGIC);
	supportedIndexes.addAll(IndexedElements.getIndexesInfo(DatabaseImpl.MARK_LOGIC));
	supportedIndexes.addAll(IndexedResourceElements.getIndexesInfo(DatabaseImpl.MARK_LOGIC));
//	supportedIndexes.addAll(IndexedRuntimeInfoElements.getIndexesInfo(DatabaseImpl.MARK_LOGIC));

	List<IndexedElementInfo> filteredIndexes = supportedIndexes.stream().filter(i -> {

	    String elementName = i.getElementName();
	    boolean enabled = true;

	    try {
		MetadataElement element = MetadataElement.fromName(elementName);
		if (!element.isEnabled()) {
		    GSLoggerFactory.getLogger(getClass()).info("Index " + elementName + " disabled");
		    enabled = false;
		}
	    } catch (IllegalArgumentException ex) {
	    }

	    try {

		ResourceProperty property = ResourceProperty.fromName(elementName);
		if (!property.isEnabled()) {
		    GSLoggerFactory.getLogger(getClass()).info("Index " + elementName + " disabled");
		    enabled = false;
		}
	    } catch (IllegalArgumentException ex) {
	    }

	    try {

		RuntimeInfoElement element = RuntimeInfoElement.fromName(elementName);
		if (!element.isEnabled()) {
		    GSLoggerFactory.getLogger(getClass()).info("Index " + elementName + " disabled");
		    enabled = false;
		}
	    } catch (IllegalArgumentException ex) {
	    }

	    return enabled;

	}).collect(Collectors.toList());

	return filteredIndexes;
    }

    // ----------------------------------
    //
    // range element queries
    //
    // ----------------------------------

    public List<IndexedElementInfo> getInstalledRangeElementIndexes() throws Exception {

	String query = getGetRangeElementIndexesQuery();
	ResultSequence res = markLogicDB.execXQuery(query);
	Iterator<ResultItem> iterator = res.iterator();

	ArrayList<IndexedElementInfo> installedIndexes = new ArrayList<IndexedElementInfo>();

	while (iterator.hasNext()) {

	    ResultItem resultItem = iterator.next();

	    XMLDocumentReader xmlDocument = new XMLDocumentReader(resultItem.asInputStream());
	    xmlDocument.setNamespaces(map);

	    String localname = xmlDocument.evaluateString("//ml:localname");
	    String scalarType = xmlDocument.evaluateString("//ml:scalar-type");
	    String nsUri = xmlDocument.evaluateString("//ml:namespace-uri");

	    IndexedElementInfo indexInfo = new IndexedElementInfo(//
		    localname, //
		    nsUri, //
		    DatabaseImpl.MARK_LOGIC.getName(), //
		    MarkLogicIndexTypes.RANGE_ELEMENT_INDEX.getType(), //
		    scalarType);

	    installedIndexes.add(indexInfo);
	}

	return installedIndexes;
    }

    // ---------------------------
    //
    // geo spatial element queries
    //
    // ---------------------------

    public List<IndexedElementInfo> getInstalledGeoSpatialIndexes() throws Exception {

	String query = getGetGeospatialElementIndexesQuery();
	ResultSequence res = markLogicDB.execXQuery(query);
	Iterator<ResultItem> iterator = res.iterator();

	ArrayList<IndexedElementInfo> installedIndexes = new ArrayList<IndexedElementInfo>();

	while (iterator.hasNext()) {

	    ResultItem resultItem = iterator.next();

	    XMLDocumentReader xmlDocument = new XMLDocumentReader(resultItem.asInputStream());
	    xmlDocument.setNamespaces(map);

	    String localname = xmlDocument.evaluateString("//ml:localname");
	    String nsUri = xmlDocument.evaluateString("//ml:namespace-uri");

	    IndexedElementInfo indexInfo = new IndexedElementInfo(//
		    localname, //
		    nsUri, //
		    DatabaseImpl.MARK_LOGIC.getName(), //
		    MarkLogicIndexTypes.GEOSPATIAL_ELEMENT_INDEX.getType(), //
		    null);

	    installedIndexes.add(indexInfo);
	}

	return installedIndexes;
    }

    /**
     * @param indexName
     * @param gsDataModelSchemaUri
     * @param string
     * @return
     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     * @throws RequestException
     */
    public boolean rangeIndexExists(String indexName, MarkLogicScalarType mlScalarType) throws Exception {

	String query = getGetRangeElementIndexesQuery();
	ResultSequence res = markLogicDB.execXQuery(query);
	Iterator<ResultItem> indexes = res.iterator();

	while (indexes.hasNext()) {

	    ResultItem reIndex = indexes.next();

	    XMLDocumentReader xmlDocument = new XMLDocumentReader(reIndex.asInputStream());
	    xmlDocument.setNamespaces(map);

	    String localname = xmlDocument.evaluateString("//ml:localname");
	    String nsUri = xmlDocument.evaluateString("//ml:namespace-uri");
	    String scalarType = xmlDocument.evaluateString("//ml:scalar-type");

	    if (localname.equals(indexName)//
		    && nsUri.equals(NameSpace.GS_DATA_MODEL_SCHEMA_URI)//
		    && scalarType.equals(mlScalarType.getType())) {

		return true;
	    }
	}

	return false;
    }

    /**
     * @param indexName
     * @param scalarType
     * @throws RequestException
     */
    public void removeRangeIndex(String indexName, String scalarType) throws RequestException {

	String removeRangeElementeIndexQuery = getRemoveRangeElementeIndexQuery(//
		indexName, //
		NameSpace.GS_DATA_MODEL_SCHEMA_URI, //
		scalarType);
	String saveConfigQuery = getSaveConfigQuery(removeRangeElementeIndexQuery);
	markLogicDB.execXQuery(saveConfigQuery);
    }

    /**
     * @param indexName
     * @param gsDataModelSchemaUri
     * @param scalarType
     * @throws RequestException
     */
    public void addRangeIndex(String indexName, String scalarType) throws RequestException {

	String addRangeElementeIndexQuery = getAddRangeElementIndexQuery(indexName, scalarType);
	String saveConfigQuery = getSaveConfigQuery(addRangeElementeIndexQuery);
	markLogicDB.execXQuery(saveConfigQuery);
    }

    private void manageWordQueryElements() throws Exception {

	// ----------------------------------------------
	//
	// 1) removes all the installed EXcluded elements
	//
	//
	{
	    GSLoggerFactory.getLogger(getClass()).info("Removing all word query excluded elements, if any");

	    String excludedElementsQuery = getGetWordQueryExcludedElementsQuery();
	    ResultSequence res = markLogicDB.execXQuery(excludedElementsQuery);
	    Iterator<ResultItem> installedIncluded = res.iterator();

	    while (installedIncluded.hasNext()) {

		ResultItem resultItem = installedIncluded.next();

		XMLDocumentReader xmlDocument = new XMLDocumentReader(resultItem.asInputStream());
		xmlDocument.setNamespaces(map);

		String localname = xmlDocument.evaluateString("//ml:localname");
		String nsUri = xmlDocument.evaluateString("//ml:namespace-uri");

		String deleteQuery = getDeleteWordQueryExcludedElementsQuery(localname, nsUri);

		String saveConfigQuery = getSaveConfigQuery(deleteQuery);
		markLogicDB.execXQuery(saveConfigQuery);
	    }
	}

	// ----------------------------------------------------------------------------------
	//
	// 2) get all the installed INcluded elements, removes the invalid and keep the valid
	//
	//
	ArrayList<String> installedValidNames = new ArrayList<>();
	ArrayList<String> supportedNames = new ArrayList<String>();

	{
	    GSLoggerFactory.getLogger(getClass()).info("Retrieving all word query included elements, if any");

	    String includedElementsQuery = getGetWordQueryIncludedElementsQuery();
	    ResultSequence res = markLogicDB.execXQuery(includedElementsQuery);
	    Iterator<ResultItem> installedIncluded = res.iterator();

	    MetadataElement[] values = MetadataElement.values();
	    for (MetadataElement metadataElement : values) {
		String name = metadataElement.getName();
		supportedNames.add(name);
	    }

	    while (installedIncluded.hasNext()) {

		ResultItem resultItem = installedIncluded.next();

		XMLDocumentReader xmlDocument = new XMLDocumentReader(resultItem.asInputStream());
		xmlDocument.setNamespaces(map);

		String localname = xmlDocument.evaluateString("//ml:localname");
		String nsUri = xmlDocument.evaluateString("//ml:namespace-uri");

		GSLoggerFactory.getLogger(getClass()).trace("Checking word query included element: {}@{}", localname, nsUri);

		if (!supportedNames.contains(localname) || !nsUri.equals(NameSpace.GS_DATA_MODEL_SCHEMA_URI)) {

		    GSLoggerFactory.getLogger(getClass()).warn("Deleting unsupported word query included element: {}@{}", localname, nsUri);

		    String deleteQuery = getDeleteWordQueryIncludedElementsQuery(localname, nsUri);

		    String saveConfigQuery = getSaveConfigQuery(deleteQuery);
		    markLogicDB.execXQuery(saveConfigQuery);
		} else {

		    GSLoggerFactory.getLogger(getClass()).trace("Found supported word query included element: {}@{}", localname, nsUri);

		    installedValidNames.add(localname);
		}
	    }
	}

	// ----------------------------------------------
	//
	// 3) adds the missing elements
	//
	//
	supportedNames.removeAll(installedValidNames);

	for (String name : supportedNames) {

	    GSLoggerFactory.getLogger(getClass()).warn("Adding missing word query included element: {}", name);

	    String query = getAddWordQueryIncludedElementsQuery(name);

	    String saveConfigQuery = getSaveConfigQuery(query);
	    markLogicDB.execXQuery(saveConfigQuery);
	}
    }

    /**
     * First removes all the redundant (not supported) geo spatial element indexes, then adds all the missing geo
     * spatial element indexes
     * 
     * @param supportedIndexes
     * @param map
     * @throws RequestException
     */
    private void manageGeoSpatialIndexes(List<IndexedElementInfo> supportedIndexes) throws Exception {

	// -----------------------------------------------------------------------------------------
	//
	// removes all the redundant (not supported) geo spatial element indexes. at the end of this cycle
	// the remoteIndexes list will contain only supported indexes, so:
	//
	// remoteIndexes.size() <= supportedIndexes.size()
	//
	// ------------------------------------------------------------------------------------------

	String query = getGetGeospatialElementIndexesQuery();
	ResultSequence res = markLogicDB.execXQuery(query);
	Iterator<ResultItem> installedGeoSpatialIndexes = res.iterator();

	ArrayList<IndexedElementInfo> validInstalledGeoSpatialIndexes = new ArrayList<IndexedElementInfo>();

	while (installedGeoSpatialIndexes.hasNext()) {

	    ResultItem gsIndex = installedGeoSpatialIndexes.next();

	    XMLDocumentReader xmlDocument = new XMLDocumentReader(gsIndex.asInputStream());
	    xmlDocument.setNamespaces(map);

	    String localname = xmlDocument.evaluateString("//ml:localname");
	    String nsUri = xmlDocument.evaluateString("//ml:namespace-uri");
	    String crs = xmlDocument.evaluateString("//ml:coordinate-system");

	    IndexedElementInfo indexInfo = new IndexedElementInfo(//
		    localname, //
		    nsUri, //
		    DatabaseImpl.MARK_LOGIC.getName(), //
		    MarkLogicIndexTypes.GEOSPATIAL_ELEMENT_INDEX.getType(), //
		    null);//

	    GSLoggerFactory.getLogger(getClass()).trace("Checking geo spatial index: {}", indexInfo);

	    // removes not supported indexes
	    if (!supportedIndexes.contains(indexInfo)) {

		GSLoggerFactory.getLogger(getClass()).warn("Removing usupported geo spatial index: " + indexInfo);

		String removeRangeElementeIndexQuery = getRemoveGeoSpatialElementeIndexQuery(localname, nsUri, crs);
		String saveConfigQuery = getSaveConfigQuery(removeRangeElementeIndexQuery);
		markLogicDB.execXQuery(saveConfigQuery);

	    } else {

		GSLoggerFactory.getLogger(getClass()).trace("Found valid installed geo spatial index: {}", indexInfo);
		validInstalledGeoSpatialIndexes.add(indexInfo);
	    }
	}

	// ---------------------------------------------------------
	//
	// adds all the missing geo spatial element indexes
	//
	// ---------------------------------------------------------

	for (IndexedElementInfo indexInfo : supportedIndexes) {

	    String elementName = indexInfo.getElementName();
	    String indexType = indexInfo.getIndexType();

	    if (indexType == MarkLogicIndexTypes.GEOSPATIAL_ELEMENT_INDEX.getType()) {

		// if not already installed, adds the missing index
		if (!validInstalledGeoSpatialIndexes.contains(indexInfo)) {

		    GSLoggerFactory.getLogger(getClass()).warn("Adding missing geo spatial index: {}", indexInfo);

		    String addRangeElementeIndexQuery = getAddGeoSpatialElementIndexQuery(elementName);
		    String saveConfigQuery = getSaveConfigQuery(addRangeElementeIndexQuery);
		    markLogicDB.execXQuery(saveConfigQuery);

		    validInstalledGeoSpatialIndexes.add(indexInfo);
		}
	    }
	}
    }

    /**
     * First removes all the redundant (not supported) range element indexes, then adds all the missing range element
     * indexes
     * 
     * @param supportedIndexes
     * @throws RequestException
     */
    private void manageRangeElementIndexes(List<IndexedElementInfo> supportedIndexes) throws Exception {

	// -----------------------------------------------------------------------------------------
	//
	// removes all the redundant (not supported) range element indexes. at the end of this cycle
	// the remoteIndexes list will contain only supported indexes, so:
	//
	// remoteIndexes.size() <= supportedIndexes.size()
	//
	// ------------------------------------------------------------------------------------------

	String query = getGetRangeElementIndexesQuery();
	ResultSequence res = markLogicDB.execXQuery(query);
	Iterator<ResultItem> installedRangeElementIndexes = res.iterator();

	ArrayList<IndexedElementInfo> validInstalledRangeElementIndexes = new ArrayList<IndexedElementInfo>();

	while (installedRangeElementIndexes.hasNext()) {

	    ResultItem reIndex = installedRangeElementIndexes.next();

	    XMLDocumentReader xmlDocument = new XMLDocumentReader(reIndex.asInputStream());
	    xmlDocument.setNamespaces(map);

	    String localname = xmlDocument.evaluateString("//ml:localname");
	    String nsUri = xmlDocument.evaluateString("//ml:namespace-uri");
	    String scalarType = xmlDocument.evaluateString("//ml:scalar-type");

	    IndexedElementInfo indexInfo = new IndexedElementInfo(//
		    localname, //
		    nsUri, //
		    DatabaseImpl.MARK_LOGIC.getName(), //
		    MarkLogicIndexTypes.RANGE_ELEMENT_INDEX.getType(), //
		    scalarType); //

	    GSLoggerFactory.getLogger(getClass()).trace("Checking range element index: {}", indexInfo);

	    //
	    // removes not supported indexes excluding the end data folder indexes
	    //
	    if (!supportedIndexes.contains(indexInfo) &&

		    !localname.contains(SourceStorageWorker.DATA_FOLDER_POSTFIX)) {

		GSLoggerFactory.getLogger(getClass()).warn("Removing usupported range element index: {}", indexInfo);

		String removeRangeElementeIndexQuery = getRemoveRangeElementeIndexQuery(localname, nsUri, scalarType);
		String saveConfigQuery = getSaveConfigQuery(removeRangeElementeIndexQuery);
		markLogicDB.execXQuery(saveConfigQuery);

	    } else {

		GSLoggerFactory.getLogger(getClass()).trace("Found valid installed range element index: {} ", indexInfo);
		validInstalledRangeElementIndexes.add(indexInfo);
	    }
	}

	// ---------------------------------------------------------
	//
	// adds all the missing range element indexes
	//
	// ---------------------------------------------------------

	for (IndexedElementInfo indexInfo : supportedIndexes) {

	    String elementName = indexInfo.getElementName();
	    String scalarType = indexInfo.getScalarType();
	    String indexType = indexInfo.getIndexType();

	    if (indexType == MarkLogicIndexTypes.RANGE_ELEMENT_INDEX.getType()) {

		// adds the missing index
		if (!validInstalledRangeElementIndexes.contains(indexInfo)) {

		    GSLoggerFactory.getLogger(getClass()).warn("Adding missing range element index: {}", indexInfo);

		    addRangeIndex(elementName, scalarType);

		    validInstalledRangeElementIndexes.add(indexInfo);
		}
	    }
	}
    }

    // -------------------------------------------------------------------------
    //
    // word query queries
    //
    // -------------------------------------------------------------------------

    private String getGetWordQueryIncludedElementsQuery() {

	String query = "import module namespace admin = \"http://marklogic.com/xdmp/admin\" at \"/MarkLogic/admin.xqy\";";
	query += "let $config := admin:get-configuration()";
	query += " return ";//
	query += " admin:database-get-word-query-included-elements($config, xdmp:database(\"" + markLogicDB.getStorageInfo().getName()
		+ "\"))";

	return query;
    }

    private String getGetWordQueryExcludedElementsQuery() {

	String query = "import module namespace admin = \"http://marklogic.com/xdmp/admin\" at \"/MarkLogic/admin.xqy\";";
	query += "let $config := admin:get-configuration()";
	query += " return ";//
	query += " admin:database-get-word-query-excluded-elements($config, xdmp:database(\"" + markLogicDB.getStorageInfo().getName()
		+ "\"))";

	return query;
    }

    private String getAddWordQueryIncludedElementsQuery(String elementName) {

	String query = "let $config := admin:get-configuration()\n";
	query += "	  let $dbid := xdmp:database(\"" + markLogicDB.getStorageInfo().getName() + "\")\n";
	query += " let $wqspec :=   admin:database-included-element(\"" + NameSpace.GI_SUITE_DATA_MODEL.getURI() + "\",";
	query += " \"" + elementName + "\", 1.0,\"\",\"\",\"\") \n";
	query += "return \n";
	query += " admin:database-add-word-query-included-element($config, $dbid, $wqspec) \n";

	return query;
    }

    private String getDeleteWordQueryIncludedElementsQuery(String elementName, String nsURI) {

	String query = "let $config := admin:get-configuration()\n";
	query += "	  let $dbid := xdmp:database(\"" + markLogicDB.getStorageInfo().getName() + "\")\n";
	query += " let $wqspec :=   admin:database-included-element(\"" + nsURI + "\",";
	query += " \"" + elementName + "\", 1.0,\"\",\"\",\"\") \n";
	query += "return \n";
	query += " admin:database-delete-word-query-included-element($config, $dbid, $wqspec) \n";

	return query;
    }

    private String getDeleteWordQueryExcludedElementsQuery(String elementName, String nsURI) {

	String query = "let $config := admin:get-configuration()\n";
	query += "	  let $dbid := xdmp:database(\"" + markLogicDB.getStorageInfo().getName() + "\")\n";
	query += " let $wqspec :=   admin:database-included-element(\"" + nsURI + "\",";
	query += " \"" + elementName + "\", 1.0,\"\",\"\",\"\") \n";
	query += "return \n";
	query += " admin:database-delete-word-query-excluded-element($config, $dbid, $wqspec) \n";

	return query;
    }

    // -------------------------------------------------------------------------
    //
    // indexes queries
    //
    // -------------------------------------------------------------------------

    private String getGetRangeElementIndexesQuery() {

	String query = "import module namespace admin = \"http://marklogic.com/xdmp/admin\" at \"/MarkLogic/admin.xqy\";";
	query += "let $config := admin:get-configuration()";
	query += " return ";//
	query += " admin:database-get-range-element-indexes($config, xdmp:database(\"" + markLogicDB.getStorageInfo().getName()
		+ "\"))";

	return query;
    }

    private String getRemoveRangeElementeIndexQuery(String elName, String nsUri, String scalarType) {

	String query = "let $config := admin:get-configuration()\n";
	query += " let $dbid := xdmp:database(\"" + markLogicDB.getStorageInfo().getName() + "\")\n";
	query += " let $rangespec := admin:database-range-element-index( \"" + scalarType + "\", " + "\"" + nsUri + "\",";
	query += " \"" + elName + "\", \"http://marklogic.com/collation/\"";
	query += " , fn:false() )\n";
	query += " return \n";
	query += " admin:database-delete-range-element-index($config, $dbid, $rangespec)";

	return query;
    }

    private String getAddRangeElementIndexQuery(String elName, String scalarType) {

	String query = "let $config := admin:get-configuration()\n";
	query += " let $dbid := xdmp:database(\"" + markLogicDB.getStorageInfo().getName() + "\")\n";
	query += " let $rangespec := admin:database-range-element-index( \"" + scalarType + "\", " + "\""
		+ NameSpace.GI_SUITE_DATA_MODEL.getURI() + "\",";
	query += " \"" + elName + "\", \"http://marklogic.com/collation/\"";
	query += " , fn:false() )\n";
	query += " return \n";
	query += " admin:database-add-range-element-index($config, $dbid, $rangespec)";

	return query;
    }

    private String getGetGeospatialElementIndexesQuery() {

	String query = "import module namespace admin = \"http://marklogic.com/xdmp/admin\" at \"/MarkLogic/admin.xqy\";";
	query += "let $config := admin:get-configuration()\n";
	query += " return \n";//
	query += "admin:database-get-geospatial-element-indexes($config, xdmp:database(\"" + markLogicDB.getStorageInfo().getName()
		+ "\"))";

	return query;
    }

    private String getRemoveGeoSpatialElementeIndexQuery(String localName, String nsUri, String crs) {

	String query = "let $config := admin:get-configuration()\n";
	query += " let $geospec := admin:database-geospatial-element-index(\"" + nsUri + "\",\"" + localName + "\", \"" + crs
		+ "\", fn:false() ) \n";
	query += " return \n";//
	query += "admin:database-delete-geospatial-element-index($config, xdmp:database(\"" + markLogicDB.getStorageInfo().getName()
		+ "\"),  $geospec )";

	return query;
    }

    private String getAddGeoSpatialElementIndexQuery(String localName) {

	String query = "let $config := admin:get-configuration()\n";
	query += " let $geospec := admin:database-geospatial-element-index(\"" + NameSpace.GI_SUITE_DATA_MODEL.getURI() + "\",\""
		+ localName + "\", \"wgs84\", fn:false() ) \n";
	query += " return \n";//
	query += "admin:database-add-geospatial-element-index($config, xdmp:database(\"" + markLogicDB.getStorageInfo().getName()
		+ "\"),  $geospec )";

	return query;
    }

    // -------------------------------------------------------------------------
    //
    // configuration queries. all these settings do not require a server restart
    //
    // -------------------------------------------------------------------------

    private String getSetIndexDetectionQuery(boolean auto) {

	String value = auto ? "automatic" : "none";

	String query = " admin:database-set-index-detection($config, " + "xdmp:database(\"" + markLogicDB.getStorageInfo().getName()
		+ "\"), " + "\"" + value + "\")";

	return query;
    }

    private String getEnableTripleIndexQuery() {

	return " admin:database-set-triple-index($config, " + "xdmp:database(\"" + markLogicDB.getStorageInfo().getName()
		+ "\"), fn:true() ) ";
    }

    private String getSetReindexerEnabledQuery(boolean enabled) {

	String value = enabled ? "fn:true()" : "fn:false()";

	String query = " admin:database-set-reindexer-enable($config, " + "xdmp:database(\"" + markLogicDB.getStorageInfo().getName()
		+ "\"), " + "" + value + ")";

	return query;
    }

    private String getSetReindexerThrottleQuery(int value) {

	String query = " admin:database-set-reindexer-throttle($config, " + "xdmp:database(\""
		+ markLogicDB.getStorageInfo().getName() + "\"), " + "" + value + ")";

	return query;
    }

    private String getSaveConfigQuery(String config) {

	String query = "import module namespace admin = \"http://marklogic.com/xdmp/admin\" at \"/MarkLogic/admin.xqy\";\n";
	query += " let $config := admin:get-configuration() \n";
	query += " let $spec := " + config + " \n";
	query += " return \n";//
	query += " admin:save-configuration($spec)";

	return query;
    }
}
