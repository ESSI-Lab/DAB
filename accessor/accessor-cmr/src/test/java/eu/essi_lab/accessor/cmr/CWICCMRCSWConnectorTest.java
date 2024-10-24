// package eu.essi_lab.accessor.cmr;
//
// import java.io.InputStream;
// import java.io.UnsupportedEncodingException;
//
// import javax.xml.bind.JAXBElement;
// import javax.xml.bind.JAXBException;
//
// import org.junit.Assert;
// import org.junit.Test;
// import org.slf4j.Logger;
//
// import eu.essi_lab.accessor.cmr.legacy.CWICCMRCSWConnector;
// import eu.essi_lab.jaxb.common.CommonContext;
// import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
// import eu.essi_lab.jaxb.csw._2_0_2.QueryType;
// import eu.essi_lab.jaxb.filter._1_1_0.PropertyIsLikeType;
// import eu.essi_lab.lib.utils.GSLoggerFactory;
// import eu.essi_lab.model.exceptions.GSException;
//
/// **
// * @author ilsanto
// */
// public class CWICCMRCSWConnectorTest {
//
// private Logger logger = GSLoggerFactory.getLogger(CWICCMRCSWConnectorTest.class);
//
// @Test
// public void enrichGetRecords() throws JAXBException, GSException, UnsupportedEncodingException {
//
// InputStream stream = CWICCMRCSWConnectorTest.class.getClassLoader().getResourceAsStream(
// "eu/essi_lab/accessor/cmr/test/GetRecords1.xml");
//
// GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);
//
// CWICCMRCSWConnector cwiccswConnector = new CWICCMRCSWConnector();
//
// GetRecords cwicGetRecords = cwiccswConnector.createCwicGetRecords(getRecords);
//
// logger.debug("CWIC Harvest GetRecords: \n\r{}", CommonContext.asString(cwicGetRecords, false));
//
// GetRecords unmarshalled = CommonContext.unmarshal(CommonContext.asInputStream(cwicGetRecords, false),
// GetRecords.class);
//
// QueryType query = ((JAXBElement<QueryType>) unmarshalled.getAbstractQuery()).getValue();
//
// Assert.assertNotNull(query);
//
// Assert.assertNotNull(query.getConstraint());
//
// Assert.assertNotNull(query.getConstraint().getFilter());
//
// Assert.assertNotNull(query.getConstraint().getFilter().getComparisonOps());
//
// JAXBElement<PropertyIsLikeType> propertyIsLikeTypeJAXBElement = (JAXBElement<PropertyIsLikeType>)
// query.getConstraint().getFilter()
// .getComparisonOps();
//
// Assert.assertEquals("true", propertyIsLikeTypeJAXBElement.getValue().getLiteral().getContent().get(0));
//
// Assert.assertEquals("IsCwic", propertyIsLikeTypeJAXBElement.getValue().getPropertyName().getContent().get(0));
//
// Assert.assertEquals(getRecords.getStartPosition(), unmarshalled.getStartPosition());
//
// Assert.assertEquals(getRecords.getOutputFormat(), unmarshalled.getOutputFormat());
//
// Assert.assertEquals(getRecords.getOutputSchema(), unmarshalled.getOutputSchema());
//
// }
// }