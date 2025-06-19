
<%@page import="java.util.Comparator"%>
<%@page import="eu.essi_lab.messages.HarvestingProperties"%>
<%@page import="eu.essi_lab.api.database.SourceStorageWorker"%>
<%@page import="eu.essi_lab.api.database.Database"%>
<%@page import="eu.essi_lab.api.database.factory.DatabaseFactory"%>
<%@page import="eu.essi_lab.model.GSSource"%>
<%@page import="java.nio.charset.StandardCharsets"%>
<%@page import="eu.essi_lab.gssrv.conf.task.bluecloud.MetadataReport"%>
<%@page import="eu.essi_lab.messages.termfrequency.TermFrequencyItem"%>
<%@page import="eu.essi_lab.messages.ResultSet"%>
<%@page import="eu.essi_lab.messages.bond.BondFactory"%>
<%@page import="eu.essi_lab.messages.bond.SimpleValueBond"%>
<%@page import="eu.essi_lab.messages.bond.Bond"%>
<%@page import="eu.essi_lab.gssrv.conf.task.bluecloud.ReportResult"%>
<%@page import="eu.essi_lab.gssrv.conf.task.bluecloud.BlueCloudElements"%>
<%@page import="eu.essi_lab.gssrv.conf.task.bluecloud.BLUECloudReport"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.ArrayList"%>
<%@page
	import="eu.essi_lab.gssrv.conf.task.bluecloud.BlueCloudMetadataElement"%>
<%@page import="eu.essi_lab.gssrv.conf.task.bluecloud.DocumentReport"%>
<%@page import="eu.essi_lab.gssrv.conf.task.bluecloud.ViewReport"%>
<%@page import="eu.essi_lab.messages.count.DiscoveryCountResponse"%>
<%@page import="eu.essi_lab.messages.bond.View"%>
<%@page import="java.util.Optional"%>
<%@page import="eu.essi_lab.pdk.wrt.WebRequestTransformer"%>
<%@page import="eu.essi_lab.api.database.DatabaseFinder"%>
<%@page import="eu.essi_lab.messages.DiscoveryMessage"%>
<%@page import="eu.essi_lab.cfga.gs.ConfigurationWrapper"%>
<%@page
	import="eu.essi_lab.api.database.factory.DatabaseProviderFactory"%>
<%@page import="eu.essi_lab.api.database.DatabaseExecutor"%>
<%@page import="eu.essi_lab.gssrv.conf.task.bluecloud.ReportManager"%>
<%@page import="java.util.List"%>
<%@page import="java.util.HashMap"%>
<%
HashMap<String, List<String[]>> tables = new HashMap<>();

ReportManager reportManager = new ReportManager();

String viewId = request.getParameter("view");

if (viewId == null) {
    Optional<View> bcv = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), "blue-cloud");
    List<GSSource> sources = ConfigurationWrapper.getViewSources(bcv.get());
    out.println("<html><head></head><body><h1>Blue-Cloud BDI metadata dashboard</h1>");
    out.println(
    "<p>This dashboard supports BDIs (Blue-Cloud Data Infrastructures) in enhancing the publication and quality of their metadata. It provides access to test portals, tools, and metadata assessment reports for each BDI.</p>");
    out.println(
    "<p><b>Contacts:</b><br/><a href='mailto:enrico.boldrini@cnr.it'>Enrico Boldrini (CNR)</a>, <a href='mailto:roberto.roncella@cnr.it'>Roberto Roncella (CNR)</a>, <a href='mailto:dick@maris.nl'>Dick Schaap (MARIS)</a></p>");
    out.println("<h2>Resources and tools</h2>");
    out.println("<a target='_blank' href='https://blue-cloud.geodab.eu/gs-service/search?view=blue-cloud'>Level 1 test portal</a><br/>");
    out.println("<a target='_blank' href='https://semantics.bodc.ac.uk/'>Semantic analyser (provided by BODC)</a><br/>");
    out.println("<a target='_blank' href='https://data.blue-cloud.org/search'>Official Blue-Cloud portal</a><br/>");

    out.println("<h2>Metadata quality and test portals by BDI</h2>");
    sources.sort(new Comparator<GSSource>() {
	public int compare(GSSource o1, GSSource o2) {
    return o1.getLabel().compareTo(o2.getLabel());
	}
    });
    for (GSSource source : sources) {
	out.println("<h3>" + source.getLabel() + "</h3>");
	out.println("<a target='_blank' href='blue-cloud-report.jsp?view=" + source.getUniqueIdentifier() + "'>Metadata report</a><br/>");
	out.println("<a target='_blank' href='https://blue-cloud.geodab.eu/gs-service/search?view=" + source.getUniqueIdentifier()
		+ "'>Test portal</a><br/>");

    }
    out.println("</body></html>");
    return;
}

Optional<View> v = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);
String label = v.get().getLabel();

GSSource source = ConfigurationWrapper.getViewSources(v.get()).get(0);

Database database = DatabaseFactory.get(ConfigurationWrapper.getStorageInfo());

SourceStorageWorker worker = database.getWorker(source.getUniqueIdentifier());
HarvestingProperties harvestingProperties = worker.getHarvestingProperties();
String endHarvestingTimestamp = harvestingProperties.getEndHarvestingTimestamp();

DatabaseExecutor executor = DatabaseProviderFactory.getExecutor(ConfigurationWrapper.getStorageInfo());

DatabaseFinder finder = DatabaseProviderFactory.getFinder(ConfigurationWrapper.getStorageInfo());
DiscoveryMessage message = new DiscoveryMessage();

message.setView(v.get());
message.setUserBond(v.get().getBond());
message.setPermittedBond(v.get().getBond());
DiscoveryCountResponse total = finder.count(message);

ViewReport viewReport = new ViewReport();
viewReport.setCount(total.getCount());
viewReport.setReturned(total.getCount());
viewReport.setLabel(label);

DocumentReport documentReport = new DocumentReport();

List<BlueCloudMetadataElement> toTest = new ArrayList<>();
toTest.addAll(Arrays.asList(BlueCloudElements.getCoreMetadataElements()));
toTest.addAll(Arrays.asList(BlueCloudElements.getOptionalMetadataElements()));

HashMap<BlueCloudMetadataElement, ReportResult> results = new HashMap<BlueCloudMetadataElement, ReportResult>();

Bond sourceBond = message.getUserBond().get().clone();
for (BlueCloudMetadataElement element : toTest) {
    ReportResult report = new ReportResult();
    report.setTotal(total.getCount());
    SimpleValueBond bond = BondFactory.createExistsSimpleValueBond(element.getQueryable());
    message.setUserBond(BondFactory.createAndBond(bond, sourceBond));
    message.setPermittedBond(BondFactory.createAndBond(bond, sourceBond));
    DiscoveryCountResponse c2 = finder.count(message);

    ResultSet<TermFrequencyItem> valuesResponse = executor.getIndexValues(message, element.getQueryable(), 0, null);
    if (valuesResponse != null) {
	List<TermFrequencyItem> frequencies = valuesResponse.getResultsList();
	List<String> vvs = new ArrayList<String>();
	for (TermFrequencyItem frequency : frequencies) {
    vvs.add(frequency.getTerm());
	}
	report.addValue(vvs);
	report.setCount(c2.getCount());
    }
    results.put(element, report);
}

List<String[]> table = new ArrayList<>();
table.add(new String[] { "Core metadata element", "Path", "Samples", "Vocabularies", "Occurrence (%)" });

for (BlueCloudMetadataElement element : BlueCloudElements.getCoreMetadataElements()) {
    table.add(MetadataReport.getRow(results, element));
}

table.add(new String[] { "Optional metadata element", "Path", "Samples", "Vocabularies", "Occurrence (%)" });

for (BlueCloudMetadataElement element : BlueCloudElements.getOptionalMetadataElements()) {
    table.add(MetadataReport.getRow(results, element));
}

tables.put(viewId, table);
HashMap<String, ViewReport> viewReports = new HashMap<>();
viewReports.put(viewId, viewReport);

String htmlTable = MetadataReport.createHTMLTable(tables, "FAIR-EASE", viewReports, "https://blue-cloud.geodab.eu/",
	endHarvestingTimestamp);
out.println(htmlTable);
%>