package eu.essi_lab.accessor.inpe;

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

import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.model.resource.Dataset;

public class SatelliteSceneMD {

    private String sceneIdentifier;
    private Double south;
    private Double north;
    private Double east;
    private Double west;
    private Double cloudCover;

    private String timeStart;
    private String timeEnd;

    private String sensor;
    private String sensorName;
    private String platform;

    private String row;
    private String path;

    private String thumbnailURL;
    private String relativeOrbit;

    private String prodType;

    private String ingDate;

    private String sensorOpMode;

    private String platid;
    private String sensorSwath;
    private String dusId;
    private String footprint;
    private String size;
    private List<String[]> polChannels;
    private Map<String, String> extensions;

    public SatelliteSceneMD() {
    }

    public XMLDocumentReader toNode() throws Exception {

	if (sceneIdentifier == null || "".equalsIgnoreCase(sceneIdentifier))
	    throw new Exception("Scene Identifier cannot be null");

	String xmlString = "<dm:scene xmlns:dm=\"http://floraresearch.eu/sdi/services/7.0/dataModel/schema\">" + "<dm:id>" + sceneIdentifier
		+ "</dm:id>" + //
		(south != null ? "<dm:south>" + south + "</dm:south>" : "") + //
		(north != null ? "<dm:north>" + north + "</dm:north>" : "") + //
		(west != null ? "<dm:west>" + west + "</dm:west>" : "") + //
		(east != null ? "<dm:east>" + east + "</dm:east>" : "") + //

		bboxElems() + //
		(timeStart != null ? "<dm:tmpExtentBegin>" + timeStart + "</dm:tmpExtentBegin>" : "") + //
		(timeEnd != null ? "<dm:tmpExtentEnd>" + timeEnd + "</dm:tmpExtentEnd>" : "") + //
		(sensor != null ? "<dm:sensor>" + sensor + "</dm:sensor>" : "") + //
		(sensorName != null ? "<dm:sensorName>" + sensorName + "</dm:sensorName>" : "") + //
		(platform != null ? "<dm:platformDesc>" + platform + "</dm:platformDesc>" : "") + //
		(platid != null ? "<dm:platformId>" + platid + "</dm:platformId>" : "") + //
		(thumbnailURL != null ? "<dm:thumb>" + thumbnailURL + "</dm:thumb>" : "") + //
		(row != null ? "<dm:eop_row>" + row + "</dm:eop_row>" : "") + //
		(path != null ? "<dm:eop_path>" + path + "</dm:eop_path>" : "") + //
		(relativeOrbit != null ? "<dm:relativeOrbit>" + relativeOrbit + "</dm:relativeOrbit>" : "") + //
		(cloudCover != null ? "<dm:cloud_cover_perc>" + cloudCover + "</dm:cloud_cover_perc>" : "") + //

		(prodType != null ? "<dm:prodType>" + prodType + "</dm:prodType>" : "") + //

		(ingDate != null ? "<dm:publication_DateTime>" + ingDate + "</dm:publication_DateTime>" : "") + //
		(ingDate != null ? "<dm:publication_Date>" + ingDate.split("T")[0] + "</dm:publication_Date>" : "") + //
		(sensorOpMode != null ? "<dm:sensorOpMode>" + sensorOpMode + "</dm:sensorOpMode>" : "") + //

		(sensorSwath != null ? "<dm:sensorSwath>" + sensorSwath + "</dm:sensorSwath>" : "") + //

		(dusId != null ? "<dm:dusId>" + dusId + "</dm:dusId>" : "") + //

		(footprint != null ? "<dm:footprint>" + footprint + "</dm:footprint>" : "") + //
		(size != null ? "<dm:size>" + size + "</dm:size>" : "") + //

		(east != null && west != null && east < west ? "<dm:isCrossed>true</dm:isCrossed>" : "<dm:isCrossed>false</dm:isCrossed>") + //

		extensionsNode() + //
		"<dm:area>0</dm:area>";

	if (polChannels != null) {

	    String ch = toIndexedElement(polChannels, false);

	    if (ch != null)
		xmlString += ch;

	}

	xmlString += "</dm:scene>";

	XMLDocumentReader scenexml = new XMLDocumentReader(xmlString);

	return scenexml;
    }

    private String extensionsNode() {

	String ret = "<dm:satExtensions xmlns:dm=\"http://floraresearch.eu/sdi/services/7.0/dataModel/schema\">";

	if (extensions != null) {

	    Iterator<String> it = extensions.keySet().iterator();

	    while (it.hasNext()) {
		String key = it.next();

		String val = extensions.get(key);

		ret += "<dm:" + key + ">" + val + "</dm:" + key + ">";
	    }

	}

	ret += "</dm:satExtensions>";
	return ret;
    }

    private String toIndexedElement(List<String[]> chlist, boolean addSeparator) {

	if (chlist == null || chlist.size() == 0)
	    return null;

	String el = "<dm:sarPolCh xmlns:dm=\"http://floraresearch.eu/sdi/services/7.0/dataModel/schema\">";

	String val = "";

	for (String[] ch : chlist) {

	    val += encodeChannel(ch);
	}

	if (addSeparator) {

	    if (val.length() == 4) {

		val = val.substring(0, 2) + "+" + val.substring(2, 4);

	    }

	}

	el += val + "</dm:sarPolCh>";

	return el;
    }

    public void setFootprint(String fp) {

	this.footprint = fp;

    }

    public String getFootprint() {

	return this.footprint;

    }

    public void setSize(String s) {

	this.size = s;

    }

    public String getSize() {

	return this.size;

    }

    private String bboxElems() {

	if (west != null && east != null && north != null && south != null) {
	    String out = "";
	    out += "<dm:sw>" + south + " " + west + "</dm:sw>";
	    out += "<dm:se>" + south + " " + east + "</dm:se>";
	    out += "<dm:nw>" + north + " " + west + "</dm:nw>";
	    out += "<dm:ne>" + north + " " + east + "</dm:ne>";

	    return out;
	}

	return "";
    }

    public SatelliteSceneMD(Node n) {

	XMLNodeReader doc = new XMLNodeReader(n);

	String nid = checkElement(doc, "//*[local-name()='id']");
	if (hasValue(nid)) {
	    setId(nid);
	}

	nid = checkElement(doc, "//*[local-name()='south']");
	if (hasValue(nid)) {
	    setSouth(Double.valueOf(nid));
	}

	nid = checkElement(doc, "//*[local-name()='north']");
	if (hasValue(nid)) {
	    setNorth(Double.valueOf(nid));
	}

	nid = checkElement(doc, "//*[local-name()='west']");
	if (hasValue(nid)) {
	    setWest(Double.valueOf(nid));
	}

	nid = checkElement(doc, "//*[local-name()='east']");
	if (hasValue(nid)) {
	    setEast(Double.valueOf(nid));
	}

	nid = checkElement(doc, "//*[local-name()='tmpExtentBegin']");
	if (hasValue(nid)) {
	    setTimeStart(nid);
	}

	nid = checkElement(doc, "//*[local-name()='tmpExtentEnd']");
	if (hasValue(nid)) {
	    setTimeEnd(nid);
	}

	nid = checkElement(doc, "//*[local-name()='sensor']");
	if (hasValue(nid)) {
	    setSensor(nid);
	}

	nid = checkElement(doc, "//*[local-name()='sensorName']");
	if (hasValue(nid)) {
	    setSensorName(nid);
	}

	nid = checkElement(doc, "//*[local-name()='platformDesc']");
	if (hasValue(nid)) {
	    setPlatform(nid);
	}

	nid = checkElement(doc, "//*[local-name()='platformId']");
	if (hasValue(nid)) {
	    setPlatformId(nid);
	}

	nid = checkElement(doc, "//*[local-name()='thumb']");
	if (hasValue(nid)) {
	    setThumbnailURL(nid);
	}

	nid = checkElement(doc, "//*[local-name()='eop_row']");
	if (hasValue(nid)) {
	    setRow(nid);
	}

	nid = checkElement(doc, "//*[local-name()='eop_path']");
	if (hasValue(nid)) {
	    setPath(nid);
	}

	nid = checkElement(doc, "//*[local-name()='relativeOrbit']");
	if (hasValue(nid)) {
	    setRelativeOrbit(nid);
	}

	nid = checkElement(doc, "//*[local-name()='cloud_cover_perc']");
	if (hasValue(nid)) {
	    setCloudCover(Double.valueOf(nid));
	}

	nid = checkElement(doc, "//*[local-name()='sensorOpMode']");
	if (hasValue(nid)) {
	    setSensorOperationalMode(nid);
	}

	nid = checkElement(doc, "//*[local-name()='sensorSwath']");
	if (hasValue(nid)) {
	    setSensorSwath(nid);
	}

	nid = checkElement(doc, "//*[local-name()='publication_DateTime']");
	if (hasValue(nid)) {
	    setIngestionDate(nid);
	}

	nid = checkElement(doc, "//*[local-name()='prodType']");
	if (hasValue(nid)) {
	    setProductType(nid);
	}

	nid = checkElement(doc, "//*[local-name()='dusId']");
	if (hasValue(nid)) {
	    setDusId(nid);
	}

	nid = checkElement(doc, "//*[local-name()='footprint']");
	if (hasValue(nid)) {
	    setFootprint(nid);
	}

	nid = checkElement(doc, "//*[local-name()='size']");
	if (hasValue(nid)) {
	    setSize(nid);
	}

	try {

	    Node[] satExt = doc.evaluateNodes("//*[local-name()='satExtensions']");

	    readSatExtensions(satExt);

	} catch (Throwable e) {

	    e.printStackTrace();
	}

	try {

	    String channels = doc.evaluateString("//*[local-name()='sarPolCh']");
	    List<String[]> decoded = null;
	    if (channels != null) {
		if (channels.length() == 2) {
		    decoded = decodeChannelsList(channels);
		} else if (channels.length() == 4) {

		    decoded = decodeChannelsList(channels.substring(0, 2));
		    decoded.add(decodeChannelsList(channels.substring(2, 4)).get(0));

		}

		polChannels = decoded;
	    }

	} catch (Throwable e) {

	    e.printStackTrace();
	}

    }

    private void readSatExtensions(Node[] satExt) {

	if (satExt == null || satExt.length == 0)
	    return;

	extensions = new HashMap<>();

	Node node = satExt[0];

	NodeList children = node.getChildNodes();

	int l = children.getLength();

	for (int i = 0; i < l; i++) {

	    Node item = children.item(i);

	    String name = item.getLocalName();

	    String value = item.getNodeValue();

	    if (value == null || "".equalsIgnoreCase(value) || "null".equalsIgnoreCase(value)) {

		value = item.getTextContent();
	    }
	    if (value != null && !"".equalsIgnoreCase(value) && !"null".equalsIgnoreCase(value)) {
		extensions.put(name, value);
	    }

	}

    }

    public static List<String[]> decodeChannelsList(String list) {

	if (list.equalsIgnoreCase("undefined"))
	    return null;

	String[] splitted = list.split(",");

	if (splitted.length == 1 && splitted[0].length() != 2)
	    splitted = list.split(" ");

	if (splitted.length == 1 && splitted[0].length() == 4)
	    splitted = new String[] { list.substring(0, 2), list.substring(2, 4) };

	List<String[]> ret = new ArrayList<>();

	for (String couple : splitted) {

	    couple = couple.trim();

	    if (couple.length() != 2)
		continue;

	    String tr = couple.toLowerCase().substring(0, 1);

	    String de = couple.toLowerCase().substring(1, 2);

	    if (!tr.equalsIgnoreCase("h") && !tr.equalsIgnoreCase("v"))
		continue;

	    if (!de.equalsIgnoreCase("h") && !de.equalsIgnoreCase("v"))
		continue;

	    ret.add(new String[] { getPolarizationOrientationCode(tr), getPolarizationOrientationCode(de) });
	}

	return ret;

    }

    private static String getPolarizationOrientationCode(String v) {

	if (v.equalsIgnoreCase("h"))
	    return HORIZONTAL;

	if (v.equalsIgnoreCase("v"))
	    return VERTICAL;

	return null;

    }

    public void setDusId(String nid) {

	this.dusId = nid;

    }

    public String getDusId() {

	return this.dusId;

    }

    public void setCloudCover(Double d) {
	DecimalFormat df = new DecimalFormat("#.##########");
	DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();

	dfs.setDecimalSeparator(".".charAt(0));
	df.setDecimalFormatSymbols(dfs);

	this.cloudCover = Double.valueOf(df.format(d));

    }

    public Double getCloudCover() {

	return this.cloudCover;

    }

    private boolean hasValue(String s) {
	return s != null && !s.equalsIgnoreCase("");
    }

    private String checkElement(XMLNodeReader doc, String xpath) {

	String s = null;
	try {
	    s = doc.evaluateString(xpath);
	} catch (Throwable e) {

	    e.printStackTrace();
	}
	return s;

    }

    private void addTime(MIMetadata md_Metadata) {

	if (timeStart != null && !timeStart.equalsIgnoreCase("") && timeEnd != null && !timeEnd.equalsIgnoreCase("")) {

	    try {
		if (ISO8601DateTimeUtils.parseISO8601ToDate(timeStart).get().getTime() <=

		ISO8601DateTimeUtils.parseISO8601ToDate(timeEnd).get().getTime())

		    md_Metadata.getDataIdentification().addTemporalExtent(UUID.randomUUID().toString(), timeStart, timeEnd);
		else {
		    md_Metadata.getDataIdentification().addTemporalExtent(UUID.randomUUID().toString(), timeEnd, timeStart);
		}
	    } catch (Throwable e) {

		e.printStackTrace();

		md_Metadata.getDataIdentification().addTemporalExtent(UUID.randomUUID().toString(), timeStart, timeEnd);
	    }
	}

    }

    private void addBBox(MIMetadata md_Metadata) {

	if (north != null && west != null && east != null && south != null)

	    md_Metadata.getDataIdentification().addGeographicBoundingBox(north, west, south, east);

    }

    public Dataset toDataset(String datasetid, boolean reducedMD) {

	Dataset d = new Dataset();

	MIMetadata md = d.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	md.setParentIdentifier("satellitescene_collection_prefix_INPE_" + platid);

	String title = platform + " " + //
		(row != null ? "- Row " + row + " " : "") + //
		(path != null ? "- Path " + path + " " : "") + //
		(timeStart != null ? "- Day " + timeStart.substring(0, 10) + " " : "") + //
		"(" + sceneIdentifier + ")";

	// try {
	// if (ingDate != null && !"".equalsIgnoreCase(ingDate)) {
	// Optional<Date> date = ISO8601DateTimeUtils.parseISO8601ToDate(ingDate);
	md.getDataIdentification().setCitationTitle(title);
	// } else {
	// md.getDataIdentification().setCitationTitle(title);
	// }
	// } catch (ParseException e) {
	//
	// e.printStackTrace();
	// md.getDataIdentification().setCitationTitle(title);
	// }

	addBBox(md);
	addTime(md);

	if (reducedMD)
	    return d;

	if (size != null)
	    md.getDistribution().getDistributionTransferOptions().next().setTransferSize(Double.parseDouble(size));

	/****************************************************************************************/
	/*************************************** PLATFORM ***************************************/
	/****************************************************************************************/

	String platformName = platform;

	// String ot = ncfile.getPlatformOrbitType(); //TODO

	String pid = platid;

	MIPlatform imiplatform = new MIPlatform();

	if (platformName != null && !platformName.equalsIgnoreCase("")) {

	    imiplatform.setDescription(platformName);

	}

	if (pid != null && !pid.equalsIgnoreCase("")) {

	    imiplatform.setMDIdentifierCode(pid);// TODO

	}

	md.addMIPlatform(imiplatform);

	/****************************************************************************************/
	/**************************************** SENSOR ****************************************/
	/****************************************************************************************/

	String sensorType = sensor;

	String sensorTitle = sensorName;

	if (sensorType != null && !sensorType.equalsIgnoreCase("")) {

	    MIInstrument instrument = new MIInstrument();

	    String sensorDesc = "";

	    if (sensorOpMode != null && !"".equalsIgnoreCase(sensorOpMode)) {

		sensorDesc += "Instrument Operational Mode: " + sensorOpMode;

	    }

	    if (sensorSwath != null && !"".equalsIgnoreCase(sensorSwath)) {

		sensorDesc += (sensorDesc.equalsIgnoreCase("") ? "" : " -- ") + "Instrument Swath: " + sensorSwath;

	    }

	    instrument.setDescription(sensorDesc);

	    instrument.setTitle(sensorTitle);

	    instrument.setMDIdentifierTypeCode(sensorType);
	    instrument.setMDIdentifierTypeIdentifier(sensorType);
	    // imiplatform.addMI_Instrument(instrument);//TODO
	    md.addMIInstrument(instrument);

	}

	// addOtherExtensions(d);

	if (thumbnailURL != null && !"".equalsIgnoreCase(thumbnailURL)) {
	    thumbnailURL = URLDecoder.decode(thumbnailURL);
	    BrowseGraphic browseGraphic = new BrowseGraphic();
	    browseGraphic.setFileName(thumbnailURL);

	    browseGraphic.setFileDescription(VALIDATORDESCRITPTION);
	    md.getDataIdentification().addGraphicOverview(browseGraphic);

	}

	String url = "http://www.dgi.inpe.br/CDSR/manage.php?INDICE=" + sceneIdentifier.replace("INPE", "") + "&DONTSHOW=0";
	Online onLine = new Online();
	onLine.setFunctionCode("information");
	onLine.setLinkage(url);
	onLine.setProtocol("HTTP");
	md.getDistribution().addDistributionOnline(onLine);
	return d;

    }

    // private void addImageDescription(Dataset d) {
    // if (polChannels != null && polChannels.size() > 0) {
    //
    // String el = toIndexedElement(polChannels, true);
    //
    // d.addExtension(el);
    //
    // String mode = "<dm:polarizationMode
    // xmlns:dm=\"http://floraresearch.eu/sdi/services/7.0/dataModel/schema\"><dm:value
    // xmlns:dm=\"http://floraresearch.eu/sdi/services/7.0/dataModel/schema\">"
    // + (polChannels.size() == 1 ? "S" : "D") + "</dm:value></dm:polarizationMode>";
    //
    // d.addExtension(mode);
    // }
    // }

    // private void addOtherExtensions(Dataset d) {
    //
    // String el = "";
    // if (relativeOrbit != null) {
    // String elementName = DM7_IndexedElement.EOP_RELATIVE_ORBIT.getLocalName();
    // String value = relativeOrbit.toString();
    //
    // el += "<dm:" + elementName + " xmlns:dm=\"http://floraresearch.eu/sdi/services/7.0/dataModel/schema\">" + value +
    // "</dm:"
    // + elementName + ">";
    //
    // // d.addExtension(DM7_IndexedElement.EOP_RELATIVE_ORBIT.getLocalName(), relativeOrbit.toString());
    // }
    //
    // if (sensorSwath != null) {
    //
    // String elementName = DM7_IndexedElement.EOP_SENSOR_SWAT.getLocalName();
    // String value = sensorSwath;
    //
    // el += "<dm:" + elementName + " xmlns:dm=\"http://floraresearch.eu/sdi/services/7.0/dataModel/schema\">" + value +
    // "</dm:"
    // + elementName + ">";
    //
    // // d.addExtension(DM7_IndexedElement.EOP_SENSOR_SWAT.getLocalName(), sensorSwath);
    // }
    //
    // if (sensorOpMode != null) {
    //
    // String elementName = DM7_IndexedElement.EOP_SENSOR_OPERATIONAL_MODE.getLocalName();
    // String value = sensorOpMode;
    //
    // el += "<dm:" + elementName + " xmlns:dm=\"http://floraresearch.eu/sdi/services/7.0/dataModel/schema\">" + value +
    // "</dm:"
    // + elementName + ">";
    //
    // // d.addExtension(DM7_IndexedElement.EOP_SENSOR_OPERATIONAL_MODE.getLocalName(), sensorOpMode);
    // }
    //
    // if (cloudCover != null) {
    // String elementName = DM7_IndexedElement.CLOUD_COVER_PERC.getLocalName();
    // String value = cloudCover.toString();
    //
    // el += "<dm:" + elementName + " xmlns:dm=\"http://floraresearch.eu/sdi/services/7.0/dataModel/schema\">" + value +
    // "</dm:"
    // + elementName + ">";
    //
    // // d.addExtension(DM7_IndexedElement.CLOUD_COVER_PERC.getLocalName(), cloudCover.toString());
    // }
    //
    // if (row != null) {
    // String elementName = DM7_IndexedElement.EOP_ROW.getLocalName();
    // String value = row.toString();
    //
    // el += "<dm:" + elementName + " xmlns:dm=\"http://floraresearch.eu/sdi/services/7.0/dataModel/schema\">" + value +
    // "</dm:"
    // + elementName + ">";
    //
    // // d.addExtension(DM7_IndexedElement.EOP_ROW.getLocalName(), row.toString());
    // }
    //
    // if (path != null) {
    // String elementName = DM7_IndexedElement.EOP_PATH.getLocalName();
    // String value = path.toString();
    //
    // el += "<dm:" + elementName + " xmlns:dm=\"http://floraresearch.eu/sdi/services/7.0/dataModel/schema\">" + value +
    // "</dm:"
    // + elementName + ">";
    //
    // // d.addExtension(DM7_IndexedElement.EOP_PATH.getLocalName(), path.toString());
    // }
    //
    // if (prodType != null) {
    // String elementName = DM7_IndexedElement.EOP_PRODUCT_TYPE.getLocalName();
    // String value = prodType.toString();
    //
    // el += "<dm:" + elementName + " xmlns:dm=\"http://floraresearch.eu/sdi/services/7.0/dataModel/schema\">" + value +
    // "</dm:"
    // + elementName + ">";
    //
    // // d.addExtension(DM7_IndexedElement.EOP_PRODUCT_TYPE.getLocalName(), prodType.toString());
    // }
    //
    // if (ingDate != null) {//I add also this in extension because in publicztion date I lose the time
    //
    // String elementName = DM7_IndexedElement.PUBLICATION_DATE_TIME.getLocalName();
    // String value = ingDate.toString();
    //
    // el += "<dm:" + elementName + " xmlns:dm=\"http://floraresearch.eu/sdi/services/7.0/dataModel/schema\">" + value +
    // "</dm:"
    // + elementName + ">";
    //
    // // d.addExtension(DM7_IndexedElement.PUBLICATION_DATE_TIME.getLocalName(), ingDate.toString());
    // }
    //
    // if (dusId != null) {
    // String elementName = "dusId";
    // String value = dusId;
    //
    // el += "<dm:" + elementName + " xmlns:dm=\"http://floraresearch.eu/sdi/services/7.0/dataModel/schema\">" + value +
    // "</dm:"
    // + elementName + ">";
    //
    // // d.addExtension("dusId", dusId);
    // }
    //
    // if (footprint != null) {
    // String elementName = "sat_scene_footprint";
    // String value = footprint;
    //
    // el += "<dm:" + elementName + " xmlns:dm=\"http://floraresearch.eu/sdi/services/7.0/dataModel/schema\">" + value +
    // "</dm:"
    // + elementName + ">";
    //
    // // d.addExtension("sat_scene_footprint", footprint);
    // }
    //
    // if (size != null) {
    //
    // String elementName = "sat_product_size";
    // String value = size;
    //
    // el += "<dm:" + elementName + " xmlns:dm=\"http://floraresearch.eu/sdi/services/7.0/dataModel/schema\">" + value +
    // "</dm:"
    // + elementName + ">";
    //
    // // d.addExtension("sat_product_size", size);
    // }
    // try {
    //
    // List<Node> nodes = new XMLDocument(StreamUtils.convertStringToStream("<rrr>" + el + "</rrr>",
    // StandardCharsets.UTF_8))
    // .evaluateXPath("//*[local-name()='rrr']/*").asNodesList();
    //
    // for (Node n : nodes) {
    //
    // d.addExtension(n);
    //
    // }
    //
    // } catch (Throwable e) {
    //
    // e.printStackTrace();
    // }
    //
    // addImageDescription(d);
    //
    // if (extensions != null) {
    // String satex = extensionsNode();
    //
    // try {
    // d.addExtension(XMLUtils.createDocument(satex));
    // } catch (Throwable e) {
    //
    // e.printStackTrace();
    // }
    // }
    //
    // }

    public static final String VALIDATORDESCRITPTION = "Pictorial preview of the dataset. Generated by Access Validator";

    public Map<String, String> getSatExtensions() {

	return extensions;

    }

    public String getId() {
	return sceneIdentifier;
    }

    public void setId(String id) {
	this.sceneIdentifier = id;
    }

    public Double getSouth() {
	return south;
    }

    public void setSouth(Double s) {
	DecimalFormat df = new DecimalFormat("#.####");
	DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();

	dfs.setDecimalSeparator(".".charAt(0));
	df.setDecimalFormatSymbols(dfs);

	this.south = Double.valueOf(df.format(s));

    }

    public Double getNorth() {
	return north;
    }

    public void setNorth(Double n) {
	DecimalFormat df = new DecimalFormat("#.####");
	DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();

	dfs.setDecimalSeparator(".".charAt(0));
	df.setDecimalFormatSymbols(dfs);

	this.north = Double.valueOf(df.format(n));

    }

    public Double getEast() {
	return east;
    }

    public void setEast(Double e) {
	DecimalFormat df = new DecimalFormat("#.####");
	DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();

	dfs.setDecimalSeparator(".".charAt(0));
	df.setDecimalFormatSymbols(dfs);

	this.east = Double.valueOf(df.format(e));

    }

    public String getTimeStart() {
	return timeStart;
    }

    public void setTimeStart(String timeStart) {
	this.timeStart = timeStart;
    }

    public Double getWest() {
	return west;
    }

    public void setWest(Double w) {
	DecimalFormat df = new DecimalFormat("#.####");
	DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();

	dfs.setDecimalSeparator(".".charAt(0));
	df.setDecimalFormatSymbols(dfs);

	this.west = Double.valueOf(df.format(w));

    }

    public String getTimeEnd() {
	return timeEnd;
    }

    public void setTimeEnd(String timeEnd) {
	this.timeEnd = timeEnd;
    }

    public String getSensor() {
	return sensor;
    }

    public void setSensor(String sensor) {
	this.sensor = sensor;
    }

    public String getSensorName() {
	return sensorName;
    }

    public void setSensorName(String sensor) {
	this.sensorName = sensor;
    }

    public String getPlatform() {
	return platform;
    }

    public void setPlatform(String platform) {
	this.platform = platform;
    }

    public String getPlatformId() {
	return platid;
    }

    public void setPlatformId(String pid) {
	this.platid = pid;
    }

    public String getThumbnailURL() {
	return thumbnailURL;
    }

    public void setThumbnailURL(String t) {
	this.thumbnailURL = t;
    }

    public String getRow() {
	return row;
    }

    public void setRow(String row) {
	this.row = row;
    }

    public String getPath() {
	return path;
    }

    public void setPath(String path) {
	this.path = path;
    }

    public void setRelativeOrbit(String rOrbit) {

	this.relativeOrbit = rOrbit;

    }

    public String getRelativeOrbit() {

	return this.relativeOrbit;

    }

    public void setProductType(String productTytpe) {

	prodType = productTytpe;

    }

    public String getProductType() {

	return this.prodType;

    }

    public void setIngestionDate(String date) {

	ingDate = date;

    }

    public String getIngestionDate() {

	return this.ingDate;

    }

    public void setSensorOperationalMode(String mode) {

	sensorOpMode = mode;

    }

    public String getSensorOperationalMode() {

	return this.sensorOpMode;

    }

    public void setSensorSwath(String sw) {

	sensorSwath = sw;

    }

    public String getSensorSwath() {

	return this.sensorSwath;

    }

    public void setPolarization(List<String[]> chList) {

	this.polChannels = chList;

    }

    public List<String[]> getPolarization() {

	return this.polChannels;

    }

    public void addOtherMD(Map<String, String> otherMd) {

	extensions = otherMd;

    }

    public static String encodeChannel(String[] ch) {

	if (ch == null || ch.length != 2)
	    return null;

	String ret = "";
	String first = ch[0];
	String second = ch[1];

	if (first.equalsIgnoreCase(HORIZONTAL))
	    ret += "H";
	else if (first.equalsIgnoreCase(VERTICAL))
	    ret += "V";
	else
	    return null;

	if (second.equalsIgnoreCase(HORIZONTAL))
	    ret += "H";
	else if (second.equalsIgnoreCase(VERTICAL))
	    ret += "V";
	else
	    return null;

	return ret;

    }

    private static final String HORIZONTAL = "horizontal";
    private static final String VERTICAL = "vertical";

}
