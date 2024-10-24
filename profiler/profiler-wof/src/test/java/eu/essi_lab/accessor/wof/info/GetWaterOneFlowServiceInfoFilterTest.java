//package eu.essi_lab.accessor.wof.info;
//
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import eu.essi_lab.messages.web.WebRequest;
//import eu.essi_lab.model.exceptions.GSException;
//
///**
// * @author boldrini
// */
//public class GetWaterOneFlowServiceInfoFilterTest {
//
//    private static final String PATH = "http://localhost:9090/gs-service/services/essi";
//    private GetWaterOneFlowServiceInfoFilter filter;
//
//    @Before
//    public void init() {
//	this.filter = new GetWaterOneFlowServiceInfoFilter();
//    }
//
//    @Test
//    public void testGood() throws GSException {
//
//	assertTrue(filter.accept(WebRequest.create(PATH + "/hiscentral.asmx/GetWaterOneFlowServiceInfo?")));
//	
//	assertTrue(filter.accept(WebRequest.create(PATH + "/hiscentral.asmx/GetWaterOneFlowServiceInfo")));
//
//	assertTrue(filter
//		.accept(WebRequest.create(PATH + "http://localhost:9090/gs-service/services/essi/cuahsi/getwateroneflowserviceinfo")));
//
//    }
//
//    @Test
//    public void testBad() throws GSException {
//
//	assertFalse(filter.accept(WebRequest.create(PATH, null)));
//
//	assertFalse(filter.accept(WebRequest.create(PATH + "/hiscentral.asmx/GetWaterOneFlowServiceInfos?")));
//	
//	assertFalse(filter.accept(WebRequest.create(PATH + "/hiscentral.asmx/GetWaterOneFlowService?")));
//
//    }
//}