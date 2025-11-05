<%@page import="java.util.Map"%>
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
<%@page import="java.util.Locale"%>
<%@page import="java.util.Collections"%>
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
<%!
private static String escapeHtml(String value) {
    if (value == null) {
	return "";
    }
    return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
	    .replace("\"", "&quot;").replace("'", "&#39;");
}

private static String trimToNull(String value) {
    if (value == null) {
	return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
}

private static int compareIgnoreCase(String first, String second) {
    String left = first != null ? first : "";
    String right = second != null ? second : "";
    int result = String.CASE_INSENSITIVE_ORDER.compare(left, right);
    if (result != 0) {
	return result;
    }
    return left.compareTo(right);
}

private static String escapeForJsString(String value) {
    if (value == null) {
	return "";
    }
    String escaped = value.replace("\\", "\\\\").replace("'", "\\'");
    return escaped.replace("\r", "\\r").replace("\n", "\\n");
}

private static String joinWithSeparator(List<String> values, String separator) {
    if (values == null || values.isEmpty()) {
	return "";
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < values.size(); i++) {
	if (i > 0) {
	    sb.append(separator);
	}
	sb.append(values.get(i));
    }
    return sb.toString();
}
%>
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

if (language == null || language.isEmpty()) {
    language = "en";
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
freqs.add(MetadataElement.ORGANIZATION);
freqs.add(MetadataElement.ATTRIBUTE_TITLE);
statisticsMessage.computeFrequency(freqs, 1000);
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
    Optional<ComputationResult> orgFreq = responseItem.getFrequency(MetadataElement.ORGANIZATION);
    if (orgFreq.isPresent()) {
	ComputationResult of = orgFreq.get();
	List<TermFrequencyItem> fitems = of.getFrequencyItems();
	stats.addFrequencyResult(MetadataElement.ORGANIZATION, fitems);
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
    out.println("<html><head><title>" + translator.getTranslation("data_provider_information") + "</title>");
    StringBuilder css = new StringBuilder();
    css.append("<style>\n");
    css.append("body {\n");
    css.append("    font-family: 'Segoe UI', Arial, sans-serif;\n");
    css.append("    background: #f8f9fa;\n");
    css.append("    color: #222;\n");
    css.append("    margin: 0;\n");
    css.append("    padding: 0 0 40px 0;\n");
    css.append("}\n");
    css.append("h1, h2 {\n");
    css.append("    color: #005aef;\n");
    css.append("    margin-top: 30px;\n");
    css.append("}\n");
    css.append("ul {\n");
    css.append("    background: #fff;\n");
    css.append("    border-radius: 6px;\n");
    css.append("    box-shadow: 0 2px 8px rgba(0,0,0,0.04);\n");
    css.append("    padding: 18px 28px 18px 28px;\n");
    css.append("    margin-bottom: 30px;\n");
    css.append("}\n");
    css.append("ul li {\n");
    css.append("    margin-bottom: 8px;\n");
    css.append("    font-size: 1.08em;\n");
    css.append("}\n");
    css.append("table {\n");
    css.append("    border-collapse: collapse;\n");
    css.append("    background: #fff;\n");
    css.append("    margin-top: 18px;\n");
    css.append("    margin-bottom: 30px;\n");
    css.append("    box-shadow: 0 2px 8px rgba(0,0,0,0.04);\n");
    css.append("    border-radius: 6px;\n");
    css.append("    overflow: hidden;\n");
    css.append("    min-width: 400px;\n");
    css.append("}\n");
    css.append("th, td {\n");
    css.append("    border: 1px solid #e0e0e0;\n");
    css.append("    padding: 10px 16px;\n");
    css.append("    text-align: left;\n");
    css.append("}\n");
    css.append("th {\n");
    css.append("    background: #005aef;\n");
    css.append("    color: #fff;\n");
    css.append("    font-weight: 600;\n");
    css.append("}\n");
    css.append("tr:nth-child(even) td {\n");
    css.append("    background: #f3f6fa;\n");
    css.append("}\n");
    css.append("a {\n");
    css.append("    color: #005aef;\n");
    css.append("    text-decoration: none;\n");
    css.append("}\n");
    css.append("a:hover {\n");
    css.append("    text-decoration: underline;\n");
    css.append("}\n");
    css.append(".org-list {\n");
    css.append("    margin: 18px 0 0 0;\n");
    css.append("    display: flex;\n");
    css.append("    flex-direction: column;\n");
    css.append("    gap: 12px;\n");
    css.append("}\n");
    css.append(".org-card {\n");
    css.append("    border: 1px solid #e0e0e0;\n");
    css.append("    border-radius: 6px;\n");
    css.append("    box-shadow: 0 2px 6px rgba(0,0,0,0.06);\n");
    css.append("    background: #fff;\n");
    css.append("}\n");
    css.append(".org-summary {\n");
    css.append("    width: 100%;\n");
    css.append("    display: flex;\n");
    css.append("    align-items: center;\n");
    css.append("    gap: 16px;\n");
    css.append("    justify-content: space-between;\n");
    css.append("    padding: 16px 20px;\n");
    css.append("    font-size: 1em;\n");
    css.append("    font-family: inherit;\n");
    css.append("    text-align: left;\n");
    css.append("    flex-wrap: wrap;\n");
    css.append("}\n");
    css.append(".org-summary-toggle {\n");
    css.append("    border: none;\n");
    css.append("    background: transparent;\n");
    css.append("    cursor: pointer;\n");
    css.append("}\n");
    css.append(".org-summary-toggle:hover {\n");
    css.append("    background: #f3f6fa;\n");
    css.append("}\n");
    css.append(".org-summary-static {\n");
    css.append("    border: none;\n");
    css.append("    background: transparent;\n");
    css.append("    cursor: default;\n");
    css.append("}\n");
    css.append(".org-summary .org-meta {\n");
    css.append("    display: flex;\n");
    css.append("    align-items: center;\n");
    css.append("    gap: 12px;\n");
    css.append("    margin-left: auto;\n");
    css.append("    flex-wrap: wrap;\n");
    css.append("    justify-content: flex-end;\n");
    css.append("}\n");
    css.append(".org-summary .org-name {\n");
    css.append("    font-weight: 600;\n");
    css.append("    color: #1f2933;\n");
    css.append("}\n");
    css.append(".org-summary .org-meta {\n");
    css.append("    display: flex;\n");
    css.append("    align-items: center;\n");
    css.append("    gap: 12px;\n");
    css.append("    margin-left: auto;\n");
    css.append("}\n");
    css.append(".org-summary .org-roles {\n");
    css.append("    color: #4b5563;\n");
    css.append("    font-size: 0.95em;\n");
    css.append("}\n");
    css.append(".org-summary .org-count {\n");
    css.append("    background: #005aef;\n");
    css.append("    color: #fff;\n");
    css.append("    border-radius: 999px;\n");
    css.append("    padding: 4px 12px;\n");
    css.append("    font-size: 0.9em;\n");
    css.append("}\n");
    css.append(".org-toggle {\n");
    css.append("    font-size: 0.9em;\n");
    css.append("    color: #005aef;\n");
    css.append("}\n");
    css.append(".org-details {\n");
    css.append("    border-top: 1px solid #e0e0e0;\n");
    css.append("    padding: 16px 20px 18px 20px;\n");
    css.append("}\n");
    css.append(".org-contact {\n");
    css.append("    margin-bottom: 12px;\n");
    css.append("}\n");
    css.append(".org-contact:last-child {\n");
    css.append("    margin-bottom: 0;\n");
    css.append("}\n");
    css.append(".org-contact-name {\n");
    css.append("    font-weight: 600;\n");
    css.append("    margin-bottom: 4px;\n");
    css.append("}\n");
    css.append(".org-contact-meta {\n");
    css.append("    font-size: 0.92em;\n");
    css.append("    color: #444;\n");
    css.append("    display: flex;\n");
    css.append("    flex-wrap: wrap;\n");
    css.append("    gap: 10px;\n");
    css.append("}\n");
    css.append(".org-contact-meta a {\n");
    css.append("    color: #005aef;\n");
    css.append("    text-decoration: none;\n");
    css.append("}\n");
    css.append(".org-contact-meta a:hover {\n");
    css.append("    text-decoration: underline;\n");
    css.append("}\n");
    css.append(".org-role-label {\n");
    css.append("    font-weight: 500;\n");
    css.append("}\n");
    css.append(".org-meta-sep {\n");
    css.append("    color: #9ca3af;\n");
    css.append("    margin: 0 6px;\n");
    css.append("}\n");
    css.append("</style>");
    out.println(css.toString());
    out.println("</head><body>");
    out.println("<div style='max-width: 60%; margin: 0 auto;'>");
    out.println("<h1>" + translator.getTranslation("data_provider_information") + "</h1>");
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
    out.println("<h2>" + translator.getTranslation("provider_statistics") + "</h2>");
    out.println(
		    "<table style='width: 100%; max-width: 100%; margin-bottom: 30px; border-collapse: separate; border-spacing: 0; background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.04); border-radius: 6px; overflow: hidden;'>");
    out.println("<tr>");
    out.println("<td style='vertical-align: top; padding: 18px 28px 18px 28px; width: 50%;'>");
    out.println("<ul>");
    out.println("<li><b># " + translator.getTranslation("platforms") + ":</b> " + stats.getSiteCount() + "</li>");
    out.println("<li><b># " + translator.getTranslation("observed_properties") + ":</b> " + stats.getAttributeCount() + "</li>");
    out.println("<li><b># " + translator.getTranslation("datasets") + ":</b> " + stats.getTimeSeriesCount() + "</li>");
    out.println("<li><b>" + translator.getTranslation("minimum_temporal_extent") + ":</b> " + stats.getBegin() + "</li>");
    out.println("<li><b>" + translator.getTranslation("maximum_temporal_extent") + ":</b> " + stats.getEnd() + "</li>");
    out.println("<li><b>" + translator.getTranslation("bbox") + ":</b> " + stats.getWest() + ", " + stats.getSouth() + ", "
		    + stats.getEast() + ", " + stats.getNorth() + "</li>");
    out.println("<li><b>" + translator.getTranslation("altitude") + ":</b> " + stats.getMinimumAltitude() + " / "
		    + stats.getMaximumAltitude() + "</li>");

    // Add organizations
    List<TermFrequencyItem> orgs = stats.getFrequencyResult(MetadataElement.ORGANIZATION);
    if (orgs != null && !orgs.isEmpty()) {
	class OrgEntry {
	    String name;
	    int totalCount = 0;
	    java.util.Set<String> roles = new java.util.LinkedHashSet<>();
	    List<java.util.Map<String, String>> contacts = new ArrayList<>();
	}

	String unknownOrganization = trimToNull(translator.getTranslation("unknown_organization"));
	if (unknownOrganization == null || "unknown_organization".equalsIgnoreCase(unknownOrganization)) {
	    unknownOrganization = "Unknown organization";
	}

	java.util.Map<String, OrgEntry> orgMap = new java.util.LinkedHashMap<>();
		for (TermFrequencyItem fitem : orgs) {
		    Map<String, String> properties = fitem.getNestedProperties();
	    String orgName = properties != null ? trimToNull(properties.get("orgName")) : null;
	    if (orgName == null) {
		orgName = unknownOrganization;
	    }
	    OrgEntry entry = orgMap.get(orgName);
	    if (entry == null) {
		entry = new OrgEntry();
		entry.name = orgName;
		orgMap.put(orgName, entry);
	    }
	    entry.totalCount += Math.max(fitem.getFreq(), 0);

	    String role = properties != null ? trimToNull(properties.get("role")) : null;
	    if (role != null) {
		entry.roles.add(role);
	    }

	    Map<String, String> contact = new HashMap<>();
	    if (properties != null) {
		String individualName = trimToNull(properties.get("individualName"));
		String email = trimToNull(properties.get("email"));
		String homepage = trimToNull(properties.get("homePageURL"));

		    if (individualName != null) {
		    contact.put("individualName", individualName);
		}
		if (email != null) {
		    contact.put("email", email);
		}
		if (homepage != null) {
		    contact.put("homePageURL", homepage);
		}
		if (role != null) {
		    contact.put("role", role);
		}
	    }
	    contact.put("count", String.valueOf(Math.max(fitem.getFreq(), 0)));
	    entry.contacts.add(contact);
	}

	List<OrgEntry> orderedOrgEntries = new ArrayList<>(orgMap.values());
	Collections.sort(orderedOrgEntries, new Comparator<OrgEntry>() {
	    @Override
	    public int compare(OrgEntry o1, OrgEntry o2) {
		return compareIgnoreCase(o1.name, o2.name);
	    }
	});

	String showContributorsLabel = trimToNull(translator.getTranslation("show_contributors"));
	if (showContributorsLabel == null || "show_contributors".equalsIgnoreCase(showContributorsLabel)) {
	    showContributorsLabel = "Show contributors";
	}
	String hideContributorsLabel = trimToNull(translator.getTranslation("hide_contributors"));
	if (hideContributorsLabel == null || "hide_contributors".equalsIgnoreCase(hideContributorsLabel)) {
	    hideContributorsLabel = "Hide contributors";
	}
	String showLabelForJs = escapeForJsString(showContributorsLabel);
	String hideLabelForJs = escapeForJsString(hideContributorsLabel);

	StringBuilder orgHtml = new StringBuilder();
	orgHtml.append("<div class='org-list'>");
	int orgIdx = 0;
	for (OrgEntry entry : orderedOrgEntries) {
	    List<String> roleList = new ArrayList<>(entry.roles);
	    Collections.sort(roleList, String.CASE_INSENSITIVE_ORDER);

	    String rolesSummary;
	    if (roleList.isEmpty()) {
		rolesSummary = "(unspecified role)";
	    } else if (roleList.size() == 1) {
		rolesSummary = "Role: " + roleList.get(0);
	    } else {
		rolesSummary = "Roles: " + joinWithSeparator(roleList, ", ");
	    }

	    List<Map<String, String>> contactEntries = new ArrayList<>(entry.contacts);
	    Collections.sort(contactEntries, new Comparator<Map<String, String>>() {
		@Override
		public int compare(Map<String, String> c1, Map<String, String> c2) {
		    int cmp = compareIgnoreCase(c1.get("individualName"), c2.get("individualName"));
		    if (cmp != 0) {
			return cmp;
		    }
		    cmp = compareIgnoreCase(c1.get("role"), c2.get("role"));
		    if (cmp != 0) {
			return cmp;
		    }
		    return compareIgnoreCase(c1.get("email"), c2.get("email"));
		}
	    });

	    boolean singleUnnamedNoMeta = false;
	    if (contactEntries.size() == 1) {
		Map<String, String> onlyContact = contactEntries.get(0);
		String singleName = trimToNull(onlyContact.get("individualName"));
		String singleRole = trimToNull(onlyContact.get("role"));
		String singleEmail = trimToNull(onlyContact.get("email"));
		String singleHomepage = trimToNull(onlyContact.get("homePageURL"));
		if (singleName == null && singleRole == null && singleEmail == null && singleHomepage == null) {
		    singleUnnamedNoMeta = true;
		}
	    }

	    boolean showContributorToggle = !singleUnnamedNoMeta && !contactEntries.isEmpty();

	    StringBuilder contactHtml = new StringBuilder();
	    if (!singleUnnamedNoMeta) {
		for (Map<String, String> contact : contactEntries) {
		    String contactName = contact.get("individualName");
		    String contactRole = contact.get("role");
		    String contactEmail = contact.get("email");
		    String contactHomepage = contact.get("homePageURL");
		    String contactCount = contact.get("count");
		    if (contactCount == null) {
			contactCount = "0";
		    }

		    contactHtml.append("<div class='org-contact'>");
		    String displayName = contactName != null ? contactName : "Unnamed contributor";
		    contactHtml.append("<div class='org-contact-name'>").append(escapeHtml(displayName)).append(" (")
			.append(escapeHtml(contactCount)).append(")</div>");

		    List<String> metaParts = new ArrayList<>();
		    if (contactRole != null) {
			metaParts.add("<span class='org-role-label'>" + escapeHtml(contactRole) + "</span>");
		    }
		    if (contactEmail != null) {
			String safeEmail = escapeHtml(contactEmail);
			metaParts.add("<a href='mailto:" + safeEmail + "'>" + safeEmail + "</a>");
		    }
		    if (contactHomepage != null) {
			String safeHomepage = escapeHtml(contactHomepage);
			metaParts.add("<a href='" + safeHomepage + "' target='_blank' rel='noopener'>Website</a>");
		    }
		    if (!metaParts.isEmpty()) {
			contactHtml.append("<div class='org-contact-meta'>")
			    .append(joinWithSeparator(metaParts, "<span class='org-meta-sep'>&#8226;</span>")).append("</div>");
		    }
		    contactHtml.append("</div>");
		}
	    }

	    boolean hasContactDetails = contactHtml.length() > 0;

	    String detailsId = "org-details-" + orgIdx++;
	    orgHtml.append("<div class='org-card'>");
	    if (showContributorToggle && hasContactDetails) {
		orgHtml.append("<button class='org-summary org-summary-toggle' type='button' data-target='").append(detailsId)
		    .append("' aria-controls='").append(detailsId).append("' aria-expanded='false'>");
	    } else {
		orgHtml.append("<div class='org-summary org-summary-static'>");
	    }
	    orgHtml.append("<span class='org-name'>").append(escapeHtml(entry.name)).append("</span>");
	    orgHtml.append("<span class='org-meta'>");
	    orgHtml.append("<span class='org-roles'>").append(escapeHtml(rolesSummary)).append("</span>");
	    orgHtml.append("<span class='org-count'>").append(entry.totalCount).append("</span>");
	    orgHtml.append("</span>");
	    if (showContributorToggle && hasContactDetails) {
		orgHtml.append("<span class='org-toggle'>").append(escapeHtml(showContributorsLabel)).append("</span>");
		orgHtml.append("</button>");
	    } else {
		orgHtml.append("</div>");
	    }

	    if (hasContactDetails) {
		if (showContributorToggle) {
		    orgHtml.append("<div class='org-details' id='").append(detailsId).append("' hidden>");
		} else {
		    orgHtml.append("<div class='org-details org-details-static'>");
		}
		orgHtml.append(contactHtml.toString());
		orgHtml.append("</div>");
	    }
	    orgHtml.append("</div>");
	}
	orgHtml.append("</div>");

	out.println("<li><b>" + translator.getTranslation("involved_organizations") + ":</b>");
	out.println(orgHtml.toString());
	out.println("</li>");
	StringBuilder orgScript = new StringBuilder();
	orgScript.append("<script>\n");
	orgScript.append("document.querySelectorAll('.org-summary-toggle').forEach(function(btn) {\n");
	orgScript.append("  if (btn.dataset.bound === 'true') { return; }\n");
	orgScript.append("  btn.dataset.bound = 'true';\n");
	orgScript.append("  btn.addEventListener('click', function() {\n");
	orgScript.append("    var targetId = btn.getAttribute('data-target');\n");
	orgScript.append("    var details = document.getElementById(targetId);\n");
	orgScript.append("    var expanded = btn.getAttribute('aria-expanded') === 'true';\n");
	orgScript.append("    btn.setAttribute('aria-expanded', String(!expanded));\n");
	orgScript.append("    var toggleLabel = btn.querySelector('.org-toggle');\n");
	orgScript.append("    if (toggleLabel) {\n");
	orgScript.append("      toggleLabel.textContent = expanded ? '" + showLabelForJs + "' : '" + hideLabelForJs + "';\n");
	orgScript.append("    }\n");
	orgScript.append("    if (details) {\n");
	orgScript.append("      if (expanded) {\n");
	orgScript.append("        details.setAttribute('hidden', 'hidden');\n");
	orgScript.append("      } else {\n");
	orgScript.append("        details.removeAttribute('hidden');\n");
	orgScript.append("      }\n");
	orgScript.append("    }\n");
	orgScript.append("  });\n");
	orgScript.append("});\n");
	orgScript.append("</script>");
	out.println(orgScript.toString());
    }

    // Add observed properties
    List<TermFrequencyItem> props = stats.getFrequencyResult(MetadataElement.ATTRIBUTE_TITLE);
    if (props != null && !props.isEmpty()) {
		StringBuilder propList = new StringBuilder();
		propList.append("<ul>");
		for (int i = 0; i < props.size(); i++) {
		    TermFrequencyItem item = props.get(i);
		    propList.append("<li>" + item.getTerm() + " (" + item.getFreq() + ")</li>");
		}
		propList.append("</ul>");
		out.println("<li><b>" + translator.getTranslation("observed_properties") + ":</b> " + propList.toString() + "</li>");
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
    out.println("        source: new ol.source.OSM()");
    out.println("      }),");
    // Add WMS layer for stations (template, please update URL and LAYERS as needed)
    out.println("      new ol.layer.Tile({");
    out.println("        source: new ol.source.TileWMS({");
    out.println("          url: '/gs-service/services/essi/token/" + token + "/view/" + viewId + "/wms-cluster?sources=" + sourceId
		    + "&',");
    out.println("          params: { LAYERS: '" + viewId + "', TILED: true }, ");
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

	out.println("<h1>" + translator.getTranslation("sample_platforms") + "</h1>");
	out.println("<table>");
	out.println("<tr><th>" + translator.getTranslation("monitoring_point") + "</th><th>" + translator.getTranslation("latitude")
		+ "</th><th>" + translator.getTranslation("longitude") + "</th><th>" + translator.getTranslation("elevation")
		+ "</th></tr>");
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
