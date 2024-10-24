//package eu.essi_lab.accessor.wof.info;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//
//import javax.ws.rs.core.MediaType;
//
//import eu.essi_lab.jaxb.oaipmh.SetType;
//import eu.essi_lab.lib.utils.GSLoggerFactory;
//import eu.essi_lab.messages.ValidationMessage;
//import eu.essi_lab.messages.web.WebRequest;
//import eu.essi_lab.model.GSSource;
//import eu.essi_lab.model.exceptions.GSException;
//import eu.essi_lab.pdk.handler.DefaultRequestHandler;
//import eu.essi_lab.pdk.wrt.ConfigurationUtils;
//
//public class GetWaterOneFlowServiceInfoHandlerTest extends DefaultRequestHandler {
//
//    @Override
//    public ValidationMessage validate(WebRequest request) throws GSException {
//
//	GetWaterOneFlowServiceInfoValidatorTest validator = new GetWaterOneFlowServiceInfoValidatorTest();
//
//	return validator.validate(request);
//    }
//
//    @Override
//    public String getResponse(WebRequest webRequest) throws GSException {
//	List<GSSource> allSources = new ArrayList<GSSource>();
//
//	try {
//	    allSources = new ConfigurationWrapper().getAllSources();
//	} catch (GSException e) {
//	    GSLoggerFactory.getLogger(getClass()).warn("Unable to get all sources", e);
//	}
//
//	String ret = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //
//		"<ArrayOfServiceInfo xmlns=\"http://hiscentral.cuahsi.org/20100205/\"\n" + //
//		"                    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" + //
//		"                    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
//
//	// sorts all the sources alphabetically by unique identifier
//	allSources.sort(new Comparator<GSSource>() {
//
//	    @Override
//	    public int compare(GSSource o1, GSSource o2) {
//		String s1 = o1.getUniqueIdentifier();
//		String s2 = o2.getUniqueIdentifier();
//		return s1.compareTo(s2);
//	    }
//	});
//
//	for (int i = 0; i < allSources.size(); i++) {
//
//	    GSSource gsSource = allSources.get(i);
//
//	    SetType setType = new SetType();
//	    setType.setSetName(gsSource.getLabel());
//	    setType.setSetSpec(gsSource.getUniqueIdentifier());
//
//	    String servURL = "http://localhost"; //
//	    String title = gsSource.getLabel(); //
//	    String serviceDescriptionURL = "http://localhost"; //
//	    String email = "info@essi-lab.eu"; //
//	    String phone = "055"; //
//	    String organization = "ESSI-Lab"; //
//	    String orgWebSite = "http://localhost"; //
//	    String citation = "Fake citation"; //
//	    String aabstract = "Fake abstract"; //
//	    String valueCount = "100"; //
//	    String variableCount = "1"; //
//	    String siteCount = "1"; //
//	    String serviceID = "" + (i + 1); // the service ID should be an integer for CUAHSI WOF. This is the reason
//					     // why the sources are sorted above.
//	    String networkName = "ESSI"; //
//	    String minx = "-180"; //
//	    String miny = "-90"; //
//	    String maxx = "180"; //
//	    String maxy = "90"; //
//	    ret += "<ServiceInfo>\n" + //
//		    "	<servURL>" + servURL + "</servURL>\n" + // http://icewater.usu.edu/littlebearriverwof/cuahsi_1_1.asmx?WSDL
//		    "	<Title>" + title + "</Title>\n" + // Little Bear River Experimental Watershed, Northern Utah,
//							  // USA\n" + //
//		    "	<ServiceDescriptionURL>" + serviceDescriptionURL + "</ServiceDescriptionURL>\n" + // http://hiscentral.cuahsi.org/pub_network.aspx?n=52\n
//		    "	<Email>" + email + "</Email>\n" + // jeff.horsburgh@usu.edu
//		    "	<phone>" + phone + "</phone>\n" + // 435-797-2946
//		    "	<organization>" + organization + "</organization>\n" + // Utah Water Research Laboratory, Utah
//									       // State University
//		    "	<orgwebsite>" + orgWebSite + "</orgwebsite>\n" + // http://littlebearriver.usu.edu
//		    "	<citation>" + citation + "</citation>\n" + // Horsburgh, J. S., D. K. Stevens, D. G. Tarboton,
//								   // N. O. ..." + //
//		    "	<aabstract>" + aabstract + "</aabstract>\n" + // Utah State University is conducting continuous
//								      // monitoring...
//		    "	<valuecount>" + valueCount + "</valuecount>\n" + // 4546654
//		    "	<variablecount>" + variableCount + "</variablecount>\n" + // 59
//		    "	<sitecount>" + siteCount + "</sitecount>\n" + // 16
//		    "	<ServiceID>" + serviceID + "</ServiceID>\n" + // 52
//		    "	<NetworkName>" + networkName + "</NetworkName>\n" + // LittleBearRiver
//		    "	<minx>" + minx + "</minx>\n" + // -111.9464
//		    "	<miny>" + miny + "</miny>\n" + // 41.49541
//		    "	<maxx>" + maxx + "</maxx>\n" + // -111.7993
//		    "	<maxy> " + maxy + "</maxy>\n" + // 41.71847
//		    "	<serviceStatus />\n" + //
//		    "</ServiceInfo>";
//
//	}
//
//	ret += "</ArrayOfServiceInfo>\n";
//	return ret;
//    }
//
//    @Override
//    public MediaType getMediaType() {
//	return MediaType.valueOf("text/xml; charset=utf-8");
//    }
//
//}
