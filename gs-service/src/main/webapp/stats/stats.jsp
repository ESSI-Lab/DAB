<%@page import="eu.essi_lab.gssrv.portal.PortalTranslator"%>
<%@page
	import="ucar.nc2.ft2.coverage.remote.CdmrFeatureProto.CoordSysOrBuilder"%>
<%@page import="java.util.HashMap"%>
<%@page import="eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer"%>
<%@page import="eu.essi_lab.views.DefaultViewManager"%>
<%@page import="eu.essi_lab.model.index.jaxb.CardinalValues"%>
<%@page import="eu.essi_lab.profiler.semantic.Stats"%>
<%@page import="eu.essi_lab.model.SortOrder"%>
<%@page import="java.util.AbstractMap.SimpleEntry"%>
<%@page import="eu.essi_lab.messages.SortedFields"%>
<%@ page language="java" contentType="text/csv; charset=UTF-8"
	pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@page import="eu.essi_lab.iso.datamodel.classes.VerticalExtent"%>
<%@page import="eu.essi_lab.iso.datamodel.classes.MIMetadata"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox"%>
<%@page import="eu.essi_lab.model.resource.GSResource"%>
<%@page import="eu.essi_lab.messages.ResultSet"%>
<%@page import="eu.essi_lab.messages.RequestMessage.IterationMode"%>
<%@page import="java.util.UUID"%>
<%@page import="java.util.Comparator"%>
<%@page import="java.util.Arrays"%>
<%@page import="eu.essi_lab.model.StorageInfo"%>
<%@page import="net.sf.saxon.expr.instruct.ForEach"%>
<%@page import="java.util.HashSet"%>
<%@page import="eu.essi_lab.model.resource.ResourceProperty"%>
<%@page import="eu.essi_lab.messages.bond.View"%>
<%@page import="eu.essi_lab.model.Queryable"%>
<%@page import="java.util.ArrayList"%>
<%@page import="eu.essi_lab.messages.termfrequency.TermFrequencyItem"%>
<%@page import="eu.essi_lab.messages.stats.ComputationResult"%>
<%@page import="java.util.Optional"%>
<%@page import="eu.essi_lab.messages.stats.ResponseItem"%>
<%@page import="eu.essi_lab.messages.stats.StatisticsResponse"%>
<%@page import="eu.essi_lab.messages.Page"%>
<%@page import="eu.essi_lab.request.executor.IStatisticsExecutor"%>
<%@page import="eu.essi_lab.messages.stats.StatisticsMessage"%>
<%@page import="eu.essi_lab.model.GSSource"%>
<%@page import="java.util.List"%>
<%@page import="eu.essi_lab.messages.bond.ResourcePropertyBond"%>
<%@page import="eu.essi_lab.messages.DiscoveryMessage"%>
<%@page import="java.util.ServiceLoader"%>
<%@page import="eu.essi_lab.request.executor.IDiscoveryExecutor"%>
<%@page import="eu.essi_lab.messages.ResourceSelector.IndexesPolicy"%>
<%@page import="eu.essi_lab.messages.ResourceSelector.ResourceSubset"%>
<%@page import="eu.essi_lab.cfga.gs.ConfigurationWrapper"%>
<%@page import="eu.essi_lab.messages.bond.BondFactory"%>
<%@page import="eu.essi_lab.model.resource.MetadataElement"%>
<%@page import="eu.essi_lab.pdk.wrt.WebRequestTransformer"%>
<%
String token = request.getParameter("token");
String viewId = request.getParameter("view");
String sourceId = request.getParameter("source");
if (viewId == null || viewId.isEmpty()) {
    out.println("Unexpected: view parameter missing");
    return;
}
String format = request.getParameter("format");
boolean csv = false;
String language = request.getParameter("language");

if (language==null||language.isEmpty()){
    language="en";
}
PortalTranslator translator = new PortalTranslator(language);
StatisticsMessage statisticsMessage = new StatisticsMessage();
View view = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId).get();

List<GSSource> sources = ConfigurationWrapper.getViewSources(view);

// set the required properties
statisticsMessage.setSources(sources);
statisticsMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());

// set the view

WebRequestTransformer.setView(//
	viewId, //
	statisticsMessage.getDataBaseURI(), //
	statisticsMessage);

// set the user bond
if (sourceId != null && !sourceId.isEmpty()) {
    statisticsMessage.setUserBond(BondFactory.createSourceIdentifierBond(sourceId));
}

// groups by source id
statisticsMessage.groupBy(ResourceProperty.SOURCE_ID);

// pagination works with grouped results. in this case there is one result item for each source.
// in order to be sure to get all the items in the same statistics response,
// we set the count equals to number of sources

statisticsMessage.setPage(new Page(1, 100));

// computes union of bboxes
statisticsMessage.computeBboxUnion();
List<Queryable> minArray = new ArrayList<>();
minArray.add(MetadataElement.ELEVATION_MIN);
statisticsMessage.computeMin(minArray);
List<Queryable> maxArray = new ArrayList<>();
maxArray.add(MetadataElement.ELEVATION_MAX);
statisticsMessage.computeMax(maxArray);
statisticsMessage.computeTempExtentUnion();
List<Queryable> freqs = new ArrayList<>();
freqs.add(MetadataElement.ORGANISATION_NAME);
freqs.add(MetadataElement.ATTRIBUTE_TITLE);
statisticsMessage.computeFrequency(freqs,1000);
List<Queryable> distArray = new ArrayList<>();
distArray.add(MetadataElement.ATTRIBUTE_TITLE);
distArray.add(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER);
distArray.add(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
distArray.add(MetadataElement.IDENTIFIER);
// computes count distinct of 2 queryables
statisticsMessage.countDistinct(distArray);

// statisticsMessage.computeSum(Arrays.asList(MetadataElement.DATA_SIZE));

ServiceLoader<IStatisticsExecutor> loader = ServiceLoader.load(IStatisticsExecutor.class);
IStatisticsExecutor executor = loader.iterator().next();

StatisticsResponse statResponse = executor.compute(statisticsMessage);
List<ResponseItem> items = statResponse.getItems();
HashMap<String, Stats> smap = new HashMap<>();
for (ResponseItem responseItem : items) {
    Stats stats = new Stats();
    Optional<ComputationResult> orgFreq = responseItem.getFrequency(MetadataElement.ORGANISATION_NAME);
    if (orgFreq.isPresent()) {
	ComputationResult of = orgFreq.get();
	List<TermFrequencyItem> fitems = of.getFrequencyItems();
	stats.addFrequencyResult(MetadataElement.ORGANISATION_NAME, fitems);
    }
    Optional<ComputationResult> propFreq = responseItem.getFrequency(MetadataElement.ATTRIBUTE_TITLE);
    if (propFreq.isPresent()) {
	ComputationResult of = propFreq.get();
	List<TermFrequencyItem> fitems = of.getFrequencyItems();
	stats.addFrequencyResult(MetadataElement.ATTRIBUTE_TITLE, fitems);
    }
    stats.setSiteCount(responseItem.getCountDistinct(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER).get().getValue());
    stats.setUniqueAttributeCount(responseItem.getCountDistinct(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER).get().getValue());
    stats.setAttributeCount(responseItem.getCountDistinct(MetadataElement.ATTRIBUTE_TITLE).get().getValue());
    stats.setTimeSeriesCount(responseItem.getCountDistinct(MetadataElement.IDENTIFIER).get().getValue());
    Optional<CardinalValues> cardinalValues = responseItem.getBBoxUnion().getCardinalValues();
    String union = responseItem.getTempExtentUnion().getValue();
    String begin = union.split(" ")[0];
    String end = union.split(" ")[1];
    if (cardinalValues.isPresent()) {
	stats.setEast(Double.parseDouble(cardinalValues.get().getEast()));
	stats.setNorth(Double.parseDouble(cardinalValues.get().getNorth()));
	stats.setWest(Double.parseDouble(cardinalValues.get().getWest()));
	stats.setSouth(Double.parseDouble(cardinalValues.get().getSouth()));
    }
    stats.setBegin(begin);
    stats.setEnd(end);
    stats.setMinimumAltitude(responseItem.getMin(MetadataElement.ELEVATION_MIN).get().getValue());
    stats.setMaximumAltitude(responseItem.getMax(MetadataElement.ELEVATION_MAX).get().getValue());
    String id = responseItem.getGroupedBy().isPresent() ? responseItem.getGroupedBy().get() : null;
    smap.put(id, stats);

}

if (format != null && format.equals("CSV")) {
    csv = true;
    response.setContentType("text/csv");
    response.setHeader("Content-Disposition", "attachment; filename=\"data.csv\"");

}
if (!csv) {
    response.setContentType("text/html");
    out.println("<html><head><title>"+translator.getTranslation("data_provider_information")+"</title>");
    out.println("<style>\n" + "body {\n" + "    font-family: 'Segoe UI', Arial, sans-serif;\n" + "    background: #f8f9fa;\n"
    + "    color: #222;\n" + "    margin: 0;\n" + "    padding: 0 0 40px 0;\n" + "}\n" + "h1, h2 {\n" + "    color: #005aef;\n"
    + "    margin-top: 30px;\n" + "}\n" + "ul {\n" + "    background: #fff;\n" + "    border-radius: 6px;\n"
    + "    box-shadow: 0 2px 8px rgba(0,0,0,0.04);\n" + "    padding: 18px 28px 18px 28px;\n" + "    margin-bottom: 30px;\n"

    + "}\n" + "ul li {\n" + "    margin-bottom: 8px;\n" + "    font-size: 1.08em;\n" + "}\n" + "table {\n"
    + "    border-collapse: collapse;\n" + "    background: #fff;\n" + "    margin-top: 18px;\n" + "    margin-bottom: 30px;\n"
    + "    box-shadow: 0 2px 8px rgba(0,0,0,0.04);\n" + "    border-radius: 6px;\n" + "    overflow: hidden;\n"
    + "    min-width: 400px;\n" + "}\n" + "th, td {\n" + "    border: 1px solid #e0e0e0;\n" + "    padding: 10px 16px;\n"
    + "    text-align: left;\n" + "}\n" + "th {\n" + "    background: #005aef;\n" + "    color: #fff;\n" + "    font-weight: 600;\n"
    + "}\n" + "tr:nth-child(even) td {\n" + "    background: #f3f6fa;\n" + "}\n" + "a {\n" + "    color: #005aef;\n"
    + "    text-decoration: none;\n" + "}\n" + "a:hover {\n" + "    text-decoration: underline;\n" + "}\n" + "</style>");
    out.println("</head><body>");
    out.println("<div style='max-width: 60%; margin: 0 auto;'>");
    out.println("<h1>"+translator.getTranslation("data_provider_information")+"</h1>");
}

if (sourceId == null || sourceId.isEmpty()) {

    sources.sort(new Comparator<GSSource>() {
	public int compare(GSSource o1, GSSource o2) {
    return o1.getLabel().compareTo(o2.getLabel());
	}
    });

    for (GSSource source : sources) {

	out.println("<h2>" + source.getLabel() + "</h2>");

	out.println("<a href=\"stats.jsp?view=" + viewId + "&source=" + source.getUniqueIdentifier() + "\">Station list</a>");

    }

} else {

    ServiceLoader<IDiscoveryExecutor> discLoader = ServiceLoader.load(IDiscoveryExecutor.class);
    IDiscoveryExecutor discExecutor = discLoader.iterator().next();

    DiscoveryMessage discoveryMessage = new DiscoveryMessage();
    List<SimpleEntry<Queryable, SortOrder>> array = new ArrayList<>();
    // 	    array.add(new SimpleEntry(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, SortOrder.ASCENDING));
    array.add(new SimpleEntry(MetadataElement.PLATFORM_TITLE, SortOrder.ASCENDING));

    discoveryMessage.setSortedFields(new SortedFields(array));

    discoveryMessage.setPage(new Page(1, 10));
    discoveryMessage.setRequestId(UUID.randomUUID().toString());
    GSSource source = ConfigurationWrapper.getSource(sourceId);
    sources.add(source);
    discoveryMessage.setSources(sources);
    StorageInfo uri = ConfigurationWrapper.getStorageInfo();
    discoveryMessage.setDataBaseURI(uri);
    WebRequestTransformer.setView(view.getId(), ConfigurationWrapper.getStorageInfo(), discoveryMessage);
    discoveryMessage.setUserBond(BondFactory.createSourceIdentifierBond(sourceId));
    discoveryMessage.setDistinctValuesElement(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
    ResultSet<GSResource> resultSet = discExecutor.retrieve(discoveryMessage);
    List<GSResource> resources = resultSet.getResultsList();

    if (csv) {
	out.println("Monitoring point\tLatitude\tLongitude\tElevation");
    } else {
	out.println("<h1>" + source.getLabel() + "</h1>");
	Stats stats = smap.get(sourceId);

	if (stats != null) {
    // Start a container for stats and map

    out.println("<div >");
    out.println("<h2>"+translator.getTranslation("provider_statistics")+"</h2>");
    out.println(
		    "<table style='width: 100%; max-width: 100%; margin-bottom: 30px; border-collapse: separate; border-spacing: 0; background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.04); border-radius: 6px; overflow: hidden;'>");
    out.println("<tr>");
    out.println("<td style='vertical-align: top; padding: 18px 28px 18px 28px; width: 50%;'>");
    out.println("<ul>");
    out.println("<li><b># "+translator.getTranslation("platforms")+":</b> " + stats.getSiteCount() + "</li>");
    out.println("<li><b># "+translator.getTranslation("observed_properties")+":</b> " + stats.getAttributeCount() + "</li>");
    out.println("<li><b># "+translator.getTranslation("datasets")+":</b> " + stats.getTimeSeriesCount() + "</li>");
    out.println("<li><b>"+translator.getTranslation("minimum_temporal_extent")+":</b> " + stats.getBegin() + "</li>");
    out.println("<li><b>"+translator.getTranslation("maximum_temporal_extent")+":</b> " + stats.getEnd() + "</li>");
    out.println("<li><b>"+translator.getTranslation("bbox")+":</b> " + stats.getWest() + ", " + stats.getSouth() + ", " + stats.getEast() + ", "
		    + stats.getNorth() + "</li>");
    out.println("<li><b>"+translator.getTranslation("altitude")+":</b> " + stats.getMinimumAltitude() + " / " + stats.getMaximumAltitude() + "</li>");
    
    // Add organizations
    List<TermFrequencyItem> orgs = stats.getFrequencyResult(MetadataElement.ORGANISATION_NAME);
    if (orgs != null && !orgs.isEmpty()) {
        StringBuilder orgList = new StringBuilder();
        orgList.append("<ul>");
        for (int i = 0; i < orgs.size(); i++) {
            TermFrequencyItem item = orgs.get(i);
            orgList.append("<li>"+item.getTerm() + " (" + item.getFreq() + ")</li>");
            
        }
        orgList.append("</ul>");
        out.println("<li><b>"+translator.getTranslation("involved_organizations")+":</b> " + orgList.toString() + "</li>");
    }

    // Add observed properties
    List<TermFrequencyItem> props = stats.getFrequencyResult(MetadataElement.ATTRIBUTE_TITLE);
    if (props != null && !props.isEmpty()) {
        StringBuilder propList = new StringBuilder();
        propList.append("<ul>");
        for (int i = 0; i < props.size(); i++) {
            TermFrequencyItem item = props.get(i);
            propList.append("<li>"+item.getTerm() + " (" + item.getFreq() + ")</li>");      
        }
        propList.append("</ul>");
        out.println("<li><b>"+translator.getTranslation("observed_properties")+":</b> " + propList.toString() + "</li>");
    }
    
    out.println("</ul>");
    out.println("</td>");
    out.println("<td style='vertical-align: top; padding: 18px 28px 18px 28px; width: 50%;'>");
    out.println("<link rel='stylesheet' href='../giapi/ol/ol.css'>");
    out.println("<script src='../giapi/ol/ol.js'></script>");
    out.println("<div id='provider-map' style='width: 100%; height: 320px;'></div>");
    out.println(
		    "<div style='font-size: 0.95em; color: #666; margin: 4px 0 16px 4px;'>© <a href='https://www.openstreetmap.org/copyright' target='_blank' style='color: #666; text-decoration: underline;'>OpenStreetMap contributors</a></div>");
    out.println("<script>");
    out.println("document.addEventListener('DOMContentLoaded', function() {");
    out.println("  if (!window.ol) {");
    out.println("    alert('OpenLayers failed to load.');");
    out.println("    return;");
    out.println("  }");
    out.println(
		    "  var bbox = [" + stats.getWest() + ", " + stats.getSouth() + ", " + stats.getEast() + ", " + stats.getNorth() + "];");
    out.println("  var center = [(bbox[0] + bbox[2]) / 2, (bbox[1] + bbox[3]) / 2];");
    out.println("  var map = new ol.Map({");
    out.println("    target: 'provider-map',");
    out.println("    layers: [");
    out.println("      new ol.layer.Tile({");
    out.println("        source: new ol.source.OSM()") ;
    out.println("      }),");
    // Add WMS layer for stations (template, please update URL and LAYERS as needed)
    out.println("      new ol.layer.Tile({");
    out.println("        source: new ol.source.TileWMS({");
    out.println("          url: '/gs-service/services/essi/token/"+token+"/view/"+viewId+"/wms-cluster?sources="+sourceId+"&',");
    out.println("          params: { LAYERS: '"+viewId+"', TILED: true }, ");
    out.println("          transition: 0");
    out.println("        })");
    out.println("      })");
    out.println("    ],");
    out.println("    view: new ol.View({");
    out.println("      center: ol.proj.fromLonLat(center),");
    out.println("      zoom: 6");
    out.println("    }),");
    out.println("    controls: []");
    out.println("  });");
    out.println("  var extent = ol.proj.transformExtent(bbox, 'EPSG:4326', 'EPSG:3857');");
    out.println("  map.getView().fit(extent, { padding: [20, 20, 20, 20], maxZoom: 12 });");

    out.println("});");
    out.println("</script>");
    out.println("</td>");
    out.println("</tr></table>");
	} else {
    out.println("<p>No statistics available for this provider.</p>");
	}

	out.println("<h1>"+translator.getTranslation("sample_platforms")+"</h1>");
	out.println("<table>");
	out.println("<tr><th>"+translator.getTranslation("monitoring_point")+"</th><th>"+translator.getTranslation("latitude")+"</th><th>"+translator.getTranslation("longitude")+"</th><th>"+translator.getTranslation("elevation")+"</th></tr>");
    }
    for (GSResource resource : resources) {
	String title = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform().getCitation().getTitle();
	Optional<String> platId = resource.getExtensionHandler().getUniquePlatformIdentifier();

	GeographicBoundingBox bb = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
	BigDecimal north = bb.getBigDecimalNorth();
	BigDecimal east = bb.getBigDecimalEast();
	String ele = "";
	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	VerticalExtent vertical = miMetadata.getDataIdentification().getVerticalExtent();
	if (vertical != null) {
    Double minimum = vertical.getMinimumValue();
    ele = "" + minimum;
	}

	if (csv) {
    out.println(title + "\t" + north + "\t" + east + "\t" + ele);
	} else {
    String titleString = title;

    if (platId.isPresent()) {
		String link = "../services/view/" + viewId + "/bnhs/station/" + platId.get() + "/";
		titleString = "<a target=\"_blank\" href=\"" + link + "\">" + title + "</a>";
    }
    out.println("<tr><td>" + titleString + "</td><td>" + north + "</td><td>" + east + "</td><td>" + ele + "</td></tr>");
	}
    }
    if (!csv) {
	out.println("</table>");

    }

    if (!csv) {
	out.println("</div>");
	out.println("</body></html>");
    }
    out.flush();

}
%>
