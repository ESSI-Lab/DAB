<%@page import="eu.essi_lab.gssrv.validator.Validator"%>
<%@page import="java.net.URI"%>
<%@page import="java.net.http.HttpRequest"%>
<%@page import="java.net.http.HttpClient"%>
<%@page import="java.net.http.HttpResponse"%>
<%@page import="eu.essi_lab.iso.datamodel.classes.Online"%>
<%@page import="java.util.Iterator"%>
<%@page import="tech.units.indriya.AbstractSystemOfUnits"%>
<%@page import="eu.essi_lab.model.resource.GSResource"%>
<%@page import="eu.essi_lab.messages.ResultSet"%>
<%@page import="eu.essi_lab.messages.bond.Bond"%>
<%@page import="eu.essi_lab.messages.bond.SimpleValueBond"%>
<%@page import="eu.essi_lab.messages.bond.BondOperator"%>
<%@page import="java.nio.charset.StandardCharsets"%>
<%@page import="java.net.URLEncoder"%>
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


<html>
<head>
<title>Metadata report</title>
<style>
</style>
<style>
iframe {
	width: 100%;
	height: 500px;
	border: 1px solid #ccc;
}
</style>
</head>
<body>
	<script>
		function loadIframe(id, url) {
			event.preventDefault(); // Prevents the page from jumping to the top
			document.getElementById(id).src = url;
			document.body.style.cursor = "wait";
		}
		function hideLoader() {
			document.body.style.cursor = "default";
		}
	</script>


	<%
	String view = request.getParameter("view");
	if (view == null || view.isEmpty()) {
		out.println("Unexpected: view parameter missing");
		return;
	}

	String protocol = request.getParameter("protocol");
	String linkageParameter = request.getParameter("linkage");
	String sourceParameter = request.getParameter("source");

	if (protocol != null && !protocol.isEmpty() && sourceParameter != null && !sourceParameter.isEmpty()
			&& linkageParameter != null && !linkageParameter.isEmpty()) {
		// report

		String report = Validator.getReport(linkageParameter, protocol);
		report = report.replace("\n", "<br/>");
		out.println(report);

	} else if (protocol != null && !protocol.isEmpty() && sourceParameter != null && !sourceParameter.isEmpty()) {
		GSSource source = ConfigurationWrapper.getSource(sourceParameter);
		DiscoveryMessage message = new DiscoveryMessage();
		ServiceLoader<IDiscoveryExecutor> discovery = ServiceLoader.load(IDiscoveryExecutor.class);
		IDiscoveryExecutor executor = discovery.iterator().next();

		DiscoveryMessage discoveryMessage = new DiscoveryMessage();
		discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.ALL);
		discoveryMessage.getResourceSelector().setSubset(ResourceSubset.FULL);
		List<GSSource> sources = new ArrayList();
		sources.add(ConfigurationWrapper.getSource(sourceParameter));
		discoveryMessage.setSources(sources);
		discoveryMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
		ResourcePropertyBond sBond = BondFactory.createSourceIdentifierBond(sourceParameter);
		SimpleValueBond pBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ONLINE_PROTOCOL,
		protocol);
		Bond bond = BondFactory.createAndBond(sBond, pBond);
		discoveryMessage.setPermittedBond(bond);
		discoveryMessage.setUserBond(bond);
		discoveryMessage.setNormalizedBond(bond);
		discoveryMessage.setPage(new Page(1, 15));
		ResultSet<GSResource> result = executor.retrieve(discoveryMessage);
		List<GSResource> list = result.getResultsList();
		out.println("<table><tr><th>Protocol</th><th>Linkage</th><th>Id</th><th>Live test</th></tr>");
		String linkage = "";
		for (GSResource res : list) {
			Iterator<Online> onlineIterator = res.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
			.getDistribution().getDistributionOnlines();
			while (onlineIterator.hasNext()) {
		Online online = onlineIterator.next();
		String prot = online.getProtocol();
		if (prot != null && prot.equals(protocol)) {
			linkage = online.getLinkage();
		}
			}
			out.println("<tr><td>" + protocol + "</td>"  //
					+ "<td>" + linkage + "</td>"  //
					+"<td><a target='_blank' href='https://seadatanet.geodab.eu/gs-service/services/essi/csw?service=CSW&version=2.0.2&request=GetRecordById&id="
			+ res.getPublicId()
			+ "&outputschema=http://www.isotc211.org/2005/gmi&elementSetName=full'>Open metadata</a></td>" //
			+ "<td><a href='#' onclick=loadIframe('reportFrame-" + source.getUniqueIdentifier().trim()
			+ "','distribution-report.jsp?view=" + view + "&source=" + source.getUniqueIdentifier() + "&protocol="
			+ URLEncoder.encode(protocol, StandardCharsets.UTF_8) + "&linkage=" + linkage
			+ "')>Start test</a></td></tr>");
		}
		out.println("</table>");

		out.println("<p>Report panel</p><iframe onload='hideLoader()' class='iframe-container' id='reportFrame-"
		+ source.getUniqueIdentifier() + "'></iframe>");

	} else {

		StatisticsMessage statisticsMessage = new StatisticsMessage();
		// set the required properties

		statisticsMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
		// statisticsMessage.setSharedRepositoryInfo(ConfigurationUtils.getSharedRepositoryInfo());

		ServiceLoader<IStatisticsExecutor> loader = ServiceLoader.load(IStatisticsExecutor.class);
		IStatisticsExecutor executor = loader.iterator().next();
		statisticsMessage.groupBy(ResourceProperty.SOURCE_ID);
		// set the view
		WebRequestTransformer.setView(//
		view, //
		statisticsMessage.getDataBaseURI(), //
		statisticsMessage);
		statisticsMessage.setSources(ConfigurationWrapper.getViewSources(statisticsMessage.getView().get()));
		// pagination works with grouped results. in this case there is one result item for each
		// source/country/etc.
		// in order to be sure to get all the items in the same statistics response,
		// we set the count equals to number of sources
		Page p = new Page();
		p.setStart(1);
		p.setSize(1000);
		statisticsMessage.setPage(p);

		// computes union of bboxes
		// 	statisticsMessage.computeBboxUnion();
		// 	statisticsMessage.computeTempExtentUnion();
		List<Queryable> queriables = new ArrayList();
		queriables.add(MetadataElement.ONLINE_PROTOCOL);
		queriables.add(ResourceProperty.SOURCE_ID);
		statisticsMessage.computeFrequency(queriables, 100);
		// computes count distinct of 2 queryables
		// 	statisticsMessage.countDistinct(//
		// 		java.util.Arrays.asList(//
		// 			MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, //
		// 			MetadataElement.UNIQUE_PLATFORM_IDENTIFIER));
		// 	statisticsMessage.computeSum(java.util.Arrays.asList(MetadataElement.DATA_SIZE));
		// 	statisticsMessage.setUserBond(BondFactory.createSourceIdentifierBond("seadatanet-open"));
		// 	statisticsMessage.groupBy(queryable);
		List<ResponseItem> items = executor.compute(statisticsMessage).getItems();
		out.println("<h1>Distribution protocol assessment tool</h1>");

		for (ResponseItem item : items) {

			String sourceId = item.getGroupedBy().get();
			GSSource source = ConfigurationWrapper.getSource(sourceId);

			if (source == null) {
		out.println("<br/><br/><b>SOURCE NOT FOUND: " + sourceId + "</b>");
		continue;
			}

			out.println("<h2>" + source.getLabel() + "</h2>");

			Optional<ComputationResult> frequency = item.getFrequency(MetadataElement.ONLINE_PROTOCOL);

			if (frequency.isPresent()) {

		List<TermFrequencyItem> list = frequency.get().getFrequencyItems();

		if (!list.isEmpty()) {
			out.println("<table>");
			out.println("<tr><th>Count</th><th>Value</th></tr>");
			for (TermFrequencyItem it : list) {
				String label = it.getLabel();
				int freq = it.getFreq();
				String term = it.getTerm();
				String encodedTerm = URLEncoder.encode(term, StandardCharsets.UTF_8);
				out.println("<tr><td>" + freq + "</td><td><a href='#' onclick=loadIframe('contentFrame-"
						+ source.getUniqueIdentifier() + "','distribution-report.jsp?view=" + view + "&source="
						+ source.getUniqueIdentifier() + "&protocol=" + encodedTerm + "')>" + term
						+ "</a></td></tr>");
			}
			out.println("</table>");
			out.println(
					"<p>Example values panel</p><iframe onload='hideLoader()' class='iframe-container' id='contentFrame-"
							+ source.getUniqueIdentifier() + "'></iframe>");

		} else {
			out.println("Empty list");
		}
			} else {
		out.println("Frequency not present");
			}

		}
	}
	%>
</body>
</html>