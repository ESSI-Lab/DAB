<%@page import="org.json.JSONObject"%>
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
String viewId = request.getParameter("view");
String sourceId = request.getParameter("source");
if (viewId == null || viewId.isEmpty()) {
    out.println("Unexpected: view parameter missing");
    return;
}
if (sourceId == null || sourceId.isEmpty()) {
    out.println("Unexpected: sourceId parameter missing");
    return;
}
String format = request.getParameter("format");
boolean csv = false;

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

    statisticsMessage.setUserBond(BondFactory.createSourceIdentifierBond(sourceId));

// groups by source id
statisticsMessage.groupBy(ResourceProperty.SOURCE_ID);


statisticsMessage.setPage(new Page(1, 100));

// computes union of bboxes
statisticsMessage.computeBboxUnion();

ServiceLoader<IStatisticsExecutor> loader = ServiceLoader.load(IStatisticsExecutor.class);
IStatisticsExecutor executor = loader.iterator().next();

StatisticsResponse statResponse = executor.compute(statisticsMessage);
List<ResponseItem> items = statResponse.getItems();
HashMap<String, Stats> smap = new HashMap<>();
for (ResponseItem responseItem : items) {
    Stats stats = new Stats();
    Optional<CardinalValues> cardinalValues = responseItem.getBBoxUnion().getCardinalValues();
    if (cardinalValues.isPresent()) {
	stats.setEast(Double.parseDouble(cardinalValues.get().getEast()));
	stats.setNorth(Double.parseDouble(cardinalValues.get().getNorth()));
	stats.setWest(Double.parseDouble(cardinalValues.get().getWest()));
	stats.setSouth(Double.parseDouble(cardinalValues.get().getSouth()));
    }
    String id = responseItem.getGroupedBy().isPresent() ? responseItem.getGroupedBy().get() : null;
    smap.put(id, stats);

}

    response.setContentType("application/json");

	Stats stats = smap.get(sourceId);
	if (stats != null) {
JSONObject bbox = new JSONObject();
bbox.put("west", stats.getWest());
bbox.put("east", stats.getEast());
bbox.put("south", stats.getSouth());
bbox.put("north", stats.getNorth());

	out.println(bbox.toString());
    
    out.flush();

}
%>
