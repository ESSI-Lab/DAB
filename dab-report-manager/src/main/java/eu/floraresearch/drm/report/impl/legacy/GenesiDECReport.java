package eu.floraresearch.drm.report.impl.legacy;
//package eu.floraresearch.drm.report;
//
//import java.math.BigInteger;
//
//import eu.essi_lab.lib.utils.GSLoggerFactory;
//import eu.floraresearch.log.CommonLogger;
//import eu.floraresearch.ogc.csw._2_0_2.GetRecords;
//import eu.floraresearch.sdi.accessor.Accessor;
//import eu.floraresearch.sdi.accessor.OpenSearchAccessor;
//import eu.floraresearch.sdi.datamodel._7_0.QueryProperty;
//import eu.floraresearch.sdi.datamodel._7_0.extensions.Catalog;
//import eu.floraresearch.sdi.datamodel._7_0.extensions.IGIResource;
//import eu.floraresearch.sdi.messages_8.OperatorType;
//import eu.floraresearch.sdi.messages_8.bonds.ISOPropertyBond;
//import eu.floraresearch.sdi.messages_8.discovery.QueryRequest;
//import eu.floraresearch.sdi.messages_8.discovery.QueryResponse;
//import eu.floraresearch.sdi.metadata.IOperationMetadata.Binding;
//import eu.floraresearch.sdi.metadata.impl.SV.SV_OperationMetadata;
//
//public class GenesiDECReport extends DefaultReport {
//
//    private int granules;
//    private int estimated;
//
//    private static String endpoint = "http://catalogue.genesi-dec.eu/search/description";
//
//    public GenesiDECReport() {
//
//	granules = -1;
//    }
//
//    @Override
//    public int getCategory_3_Value() throws Exception {
//
//	if (granules == -1) {
//
//	    try {
//
//		GSLoggerFactory.getLogger(getClass()).info( "*** Performing GenesiDEC granules computation ***\n");
//		
//		Catalog c = new Catalog("testid", "title", "GENESI/OPENSEARCH", "1.0", new SV_OperationMetadata(endpoint, Binding.HTTP_GET));
//
//		Accessor osacc = new OpenSearchAccessor().createAccessor(c);
//
//		GSLoggerFactory.getLogger(getClass()).info( "*** Executing GENESI-DEC Total Estimate method ***");
//
//		GSLoggerFactory.getLogger(getClass()).info( "Retrieving First Page");
//
//		int start = 1;
//
//		int added = harvestPage(start, 0, osacc);
//
//		int totalAdded = added;
//
//		GSLoggerFactory.getLogger(getClass()).info( "********* Added " + added + " Fed EO Collections");
//
//		while (added > 0) {
//
//		    start += 10;
//
//		    GSLoggerFactory.getLogger(getClass()).info( "Retrieving Next Page");
//
//		    added = harvestPage(start, totalAdded, osacc);
//
//		    totalAdded += added;
//
//		    GSLoggerFactory.getLogger(getClass()).info( "********* Added " + added + " GENESI-DEC Collections");
//
//		}
//
//		GSLoggerFactory.getLogger(getClass()).info( "GENESI-DEC Collection Harvest DONE - " + totalAdded + " Harvested Collecitons");
//		GSLoggerFactory.getLogger(getClass()).info( "GENESI-DEC Total Granules - " + estimated);
//
//		granules = estimated;
//		
//		GSLoggerFactory.getLogger(getClass()).info( "*** GenesiDEC granules computation DONE ***\n");
//
//	    } catch (Exception ex) {
//		
//		granules = 0;
//
//		GSLoggerFactory.getLogger(getClass()).error( ex.getMessage(), ex);
//	    }
//	}
//
//	return granules;
//    }
//
//    private int harvestPage(int start, int totalAdded, Accessor acc) {
//
//	QueryRequest r = new QueryRequest();
//
//	GetRecords gr = r.getGetRecords().get(0);
//	gr.setStartPosition(new BigInteger(String.valueOf(start)));
//	gr.setMaxRecords(new BigInteger("10"));
//	int foundEntries = 0;
//
//	try {
//	    QueryResponse resp = acc.query(r);
//
//	    foundEntries = resp.getResources().size();
//
//	    for (int i = 0; i < foundEntries; i++) {
//
//		IGIResource collection = resp.getResources().get(i);
//
//		String id = collection.getId();
//
//		GSLoggerFactory.getLogger(getClass()).info( id);
//
//		try {
//
//		    if (id != null) {
//
//			QueryResponse response = queryCollection(id, acc);
//
//			int expRes = response.getExpectedResults();
//			GSLoggerFactory.getLogger(getClass()).info( "Granules " + expRes);
//			if (expRes > -1)
//
//			    estimated += expRes;
//
//		    }
//
//		} catch (Exception e) {
//		    e.printStackTrace();
//		}
//		foundEntries++;
//
//	    }
//
//	} catch (Exception e) {
//	}
//	return foundEntries;
//    }
//
//    private QueryResponse queryCollection(String id, Accessor acc) throws Exception {
//
//	GSLoggerFactory.getLogger(getClass()).info( "Querying GENESI-DEC Collection " + id);
//
//	ISOPropertyBond isoPropertyBond = new ISOPropertyBond(OperatorType.EQUAL, QueryProperty.PARENT_IDENTIFIER, id);
//
//	QueryRequest r = new QueryRequest();
//
//	r.setISOGetRecords(isoPropertyBond);
//
//	GetRecords gr = r.getGetRecords().get(0);
//
//	gr.setMaxRecords(new BigInteger("10"));
//
//	QueryResponse resp = acc.query(r);
//
//	return resp;
//    }
//}
