
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
String label = viewId;
DatabaseExecutor executor = DatabaseProviderFactory.getExecutor(ConfigurationWrapper.getStorageInfo());

DatabaseFinder finder = DatabaseProviderFactory.getFinder(ConfigurationWrapper.getStorageInfo());
DiscoveryMessage message = new DiscoveryMessage();
Optional<View> v = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);
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

String htmlTable = MetadataReport.createHTMLTable(tables, "Blue-Cloud", viewReports, "https://blue-cloud.geodab.eu/");
out.println(htmlTable);
%>