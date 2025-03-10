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
</head>
<body>
	<%
	String view = request.getParameter("view");
		if (view==null || view.isEmpty()){
	    out.println("Unexpected: view parameter missing");
	    return;
		}

	    List<GSSource> allSources = ConfigurationWrapper.getAllSources();
	    StatisticsMessage statisticsMessage = new StatisticsMessage();
	    // set the required properties
	    statisticsMessage.setSources(allSources);
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
	    statisticsMessage.computeFrequency(queriables,100);
	    // computes count distinct of 2 queryables
	    // 	statisticsMessage.countDistinct(//
	    // 		java.util.Arrays.asList(//
	    // 			MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, //
	    // 			MetadataElement.UNIQUE_PLATFORM_IDENTIFIER));
	    // 	statisticsMessage.computeSum(java.util.Arrays.asList(MetadataElement.DATA_SIZE));
	    // 	statisticsMessage.setUserBond(BondFactory.createSourceIdentifierBond("seadatanet-open"));
	    // 	statisticsMessage.groupBy(queryable);
	    List<ResponseItem> items = executor.compute(statisticsMessage).getItems();
			out.println("<p>This page lists available protocols from distribution information of each source</p>");

	    for (ResponseItem item : items) {

			String sourceId = item.getGroupedBy().get();
			
			GSSource source = ConfigurationWrapper.getSource(sourceId);
			
			
			out.println("<h2>"+source.getLabel()+"</h2>");
			
				Optional<ComputationResult> frequency = item.getFrequency(MetadataElement.ONLINE_PROTOCOL);

				if (frequency.isPresent()) {
				    List<TermFrequencyItem> list = frequency.get().getFrequencyItems();
				    if (!list.isEmpty()) {
					out.println("<table>");
					out.println("<tr><th>Term</th><th>Occurrences</th></tr>");
					for (TermFrequencyItem it : list) {
					    String label = it.getLabel();
					    int freq = it.getFreq();
					    String term = it.getTerm();
					    out.println("<tr><td>"+term + "</td><td>"+ freq + "</td></tr>");
					}
					out.println("</table>");
				    } else {
					out.println("Empty list");
				    }
				} else {
				    out.println("Frequency not present");
				}

	    }
	%>
</body>
</html>