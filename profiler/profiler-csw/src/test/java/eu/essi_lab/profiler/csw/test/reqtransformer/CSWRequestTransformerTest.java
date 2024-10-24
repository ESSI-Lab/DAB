package eu.essi_lab.profiler.csw.test.reqtransformer;

import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.Constraint;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecordById;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory;
import eu.essi_lab.jaxb.csw._2_0_2.QueryType;
import eu.essi_lab.jaxb.csw._2_0_2.ResultType;
import eu.essi_lab.jaxb.filter._1_1_0.BinaryComparisonOpType;
import eu.essi_lab.jaxb.filter._1_1_0.FilterType;
import eu.essi_lab.jaxb.filter._1_1_0.LiteralType;
import eu.essi_lab.jaxb.filter._1_1_0.PropertyNameType;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.profiler.csw.handler.discover.CSWRequestTransformer;

public class CSWRequestTransformerTest {

    @Test
    public void GetRecorsPageStart0Test() {

	try {
	    GetRecords getRecords = createGetRecords();

	    // Marshaller marshaller = CommonContext.createMarshaller(false);
	    // marshaller.marshal(getRecords, System.out);

	    getRecords.setResultType(ResultType.RESULTS);
	    getRecords.setOutputSchema(CommonNameSpaceContext.GMD_NS_URI);

	    getRecords.setStartPosition(new BigInteger("0"));
	    getRecords.setMaxRecords(new BigInteger("10"));

	    InputStream inputStream = asInputStream(getRecords);

	    WebRequest webRequest = WebRequest.createPOST("http://localhost/cwiso", inputStream);

	    CSWRequestTransformer transformer = new CSWRequestTransformer() {
		/**
		 * Overriding to skip getAllSources which causes Exception since to configuration is provided
		 */
		public DiscoveryMessage transform(WebRequest request) throws GSException {

		    DiscoveryMessage message = new DiscoveryMessage();

		    message.setPage(getPage(request));
		    message.setUserBond(getUserBond(request));

		    return message;
		}
	    };

	    DiscoveryMessage message = transformer.transform(webRequest);

	    Page page = message.getPage();

	    Assert.assertEquals(10, page.getSize());
	    Assert.assertEquals(1, page.getStart());

	    SimpleValueBond userBond = (SimpleValueBond) message.getUserBond().get();

	    Assert.assertEquals(MetadataElement.IDENTIFIER, userBond.getProperty());
	    Assert.assertEquals(BondOperator.EQUAL, userBond.getOperator());
	    Assert.assertEquals("X", userBond.getPropertyValue());

	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void GetRecorsPageStart1Test() {

	try {
	    GetRecords getRecords = createGetRecords();
	    getRecords.setResultType(ResultType.RESULTS);
	    getRecords.setOutputSchema(CommonNameSpaceContext.GMD_NS_URI);

	    getRecords.setStartPosition(new BigInteger("1"));
	    getRecords.setMaxRecords(new BigInteger("10"));

	    InputStream inputStream = asInputStream(getRecords);

	    WebRequest webRequest = WebRequest.createPOST("http://localhost/cwiso", inputStream);

	    CSWRequestTransformer transformer = new CSWRequestTransformer() {
		/**
		 * Overriding to skip getAllSources which causes Exception since to configuration is provided
		 */
		public DiscoveryMessage transform(WebRequest request) throws GSException {

		    DiscoveryMessage message = new DiscoveryMessage();

		    message.setPage(getPage(request));
		    message.setUserBond(getUserBond(request));

		    return message;
		}
	    };

	    DiscoveryMessage message = transformer.transform(webRequest);

	    Page page = message.getPage();

	    Assert.assertEquals(10, page.getSize());
	    Assert.assertEquals(1, page.getStart());

	    SimpleValueBond userBond = (SimpleValueBond) message.getUserBond().get();

	    Assert.assertEquals(MetadataElement.IDENTIFIER, userBond.getProperty());
	    Assert.assertEquals(BondOperator.EQUAL, userBond.getOperator());
	    Assert.assertEquals("X", userBond.getPropertyValue());

	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void GetRecordByIdPOSTSingleIdTest() {

	try {
	    GetRecordById getRecordById = new GetRecordById();
	    getRecordById.getIds().add("X");

	    InputStream inputStream = asInputStream(getRecordById);

	    WebRequest webRequest = WebRequest.createPOST("http://localhost/cwiso", inputStream);

	    CSWRequestTransformer transformer = new CSWRequestTransformer() {
		/**
		 * Overriding to skip getAllSources which causes Exception since to configuration is provided
		 */
		public DiscoveryMessage transform(WebRequest request) throws GSException {

		    DiscoveryMessage message = new DiscoveryMessage();

		    message.setPage(getPage(request));
		    message.setUserBond(getUserBond(request));

		    return message;
		}
	    };

	    DiscoveryMessage message = transformer.transform(webRequest);

	    Page page = message.getPage();

	    Assert.assertEquals(1, page.getSize());
	    Assert.assertEquals(1, page.getStart());

	    SimpleValueBond userBond = (SimpleValueBond) message.getUserBond().get();

	    Assert.assertEquals(MetadataElement.IDENTIFIER, userBond.getProperty());
	    Assert.assertEquals(BondOperator.EQUAL, userBond.getOperator());
	    Assert.assertEquals("X", userBond.getPropertyValue());

	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void GetRecordByIdPOSTMultipeIdTest() {

	try {
	    GetRecordById getRecordById = new GetRecordById();
	    getRecordById.getIds().add("X");
	    getRecordById.getIds().add("Y");
	    getRecordById.getIds().add("Z");

	    InputStream inputStream = asInputStream(getRecordById);

	    WebRequest webRequest = WebRequest.createPOST("http://localhost/cwiso", inputStream);

	    CSWRequestTransformer transformer = new CSWRequestTransformer() {
		/**
		 * Overriding to skip getAllSources which causes Exception since to configuration is provided
		 */
		public DiscoveryMessage transform(WebRequest request) throws GSException {

		    DiscoveryMessage message = new DiscoveryMessage();

		    message.setPage(getPage(request));
		    message.setUserBond(getUserBond(request));

		    return message;
		}
	    };

	    DiscoveryMessage message = transformer.transform(webRequest);

	    Page page = message.getPage();

	    Assert.assertEquals(3, page.getSize());
	    Assert.assertEquals(1, page.getStart());

	    LogicalBond lb = (LogicalBond) message.getUserBond().get();
	    Assert.assertEquals(LogicalOperator.OR, lb.getLogicalOperator());

	    SimpleValueBond[] array = lb.getOperands().toArray(new SimpleValueBond[] {});

	    SimpleValueBond bond0 = array[0];
	    Assert.assertEquals(MetadataElement.IDENTIFIER, bond0.getProperty());
	    Assert.assertEquals(BondOperator.EQUAL, bond0.getOperator());
	    Assert.assertEquals("X", bond0.getPropertyValue());

	    SimpleValueBond bond1 = array[1];
	    Assert.assertEquals(MetadataElement.IDENTIFIER, bond1.getProperty());
	    Assert.assertEquals(BondOperator.EQUAL, bond1.getOperator());
	    Assert.assertEquals("Y", bond1.getPropertyValue());

	    SimpleValueBond bond2 = array[2];
	    Assert.assertEquals(MetadataElement.IDENTIFIER, bond2.getProperty());
	    Assert.assertEquals(BondOperator.EQUAL, bond2.getOperator());
	    Assert.assertEquals("Z", bond2.getPropertyValue());

	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void GetRecordByIdGETSingleIdTest() {

	try {

	    WebRequest webRequest = WebRequest.createGET("http://localhost/cwiso?request=GetRecordById&id=X");

	    CSWRequestTransformer transformer = new CSWRequestTransformer() {
		/**
		 * Overriding to skip getAllSources which causes Exception since to configuration is provided
		 */
		public DiscoveryMessage transform(WebRequest request) throws GSException {

		    DiscoveryMessage message = new DiscoveryMessage();

		    message.setPage(getPage(request));
		    message.setUserBond(getUserBond(request));

		    return message;
		}
	    };

	    DiscoveryMessage message = transformer.transform(webRequest);

	    Page page = message.getPage();

	    Assert.assertEquals(1, page.getSize());
	    Assert.assertEquals(1, page.getStart());

	    SimpleValueBond userBond = (SimpleValueBond) message.getUserBond().get();

	    Assert.assertEquals(MetadataElement.IDENTIFIER, userBond.getProperty());
	    Assert.assertEquals(BondOperator.EQUAL, userBond.getOperator());
	    Assert.assertEquals("X", userBond.getPropertyValue());

	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void GetRecordByIdGETMultipleIdTest() {

	try {

	    WebRequest webRequest = WebRequest.createGET("http://localhost/cwiso?request=GetRecordById&id=X,Y,Z");

	    CSWRequestTransformer transformer = new CSWRequestTransformer() {
		/**
		 * Overriding to skip getAllSources which causes Exception since to configuration is provided
		 */
		public DiscoveryMessage transform(WebRequest request) throws GSException {

		    DiscoveryMessage message = new DiscoveryMessage();

		    message.setPage(getPage(request));
		    message.setUserBond(getUserBond(request));

		    return message;
		}
	    };

	    DiscoveryMessage message = transformer.transform(webRequest);

	    Page page = message.getPage();

	    Assert.assertEquals(3, page.getSize());
	    Assert.assertEquals(1, page.getStart());

	    LogicalBond lb = (LogicalBond) message.getUserBond().get();
	    Assert.assertEquals(LogicalOperator.OR, lb.getLogicalOperator());

	    SimpleValueBond[] array = lb.getOperands().toArray(new SimpleValueBond[] {});

	    SimpleValueBond bond0 = array[0];
	    Assert.assertEquals(MetadataElement.IDENTIFIER, bond0.getProperty());
	    Assert.assertEquals(BondOperator.EQUAL, bond0.getOperator());
	    Assert.assertEquals("X", bond0.getPropertyValue());

	    SimpleValueBond bond1 = array[1];
	    Assert.assertEquals(MetadataElement.IDENTIFIER, bond1.getProperty());
	    Assert.assertEquals(BondOperator.EQUAL, bond1.getOperator());
	    Assert.assertEquals("Y", bond1.getPropertyValue());

	    SimpleValueBond bond2 = array[2];
	    Assert.assertEquals(MetadataElement.IDENTIFIER, bond2.getProperty());
	    Assert.assertEquals(BondOperator.EQUAL, bond2.getOperator());
	    Assert.assertEquals("Z", bond2.getPropertyValue());

	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    private GetRecords createGetRecords() {

	ObjectFactory cswFactory = new eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory();
	eu.essi_lab.jaxb.filter._1_1_0.ObjectFactory filterFactory = new eu.essi_lab.jaxb.filter._1_1_0.ObjectFactory();

	GetRecords getRecords = new GetRecords();
	QueryType queryType = new QueryType();
	Constraint constraint = new Constraint();

	JAXBElement<QueryType> query = cswFactory.createQuery(queryType);

	FilterType filterType = new FilterType();

	BinaryComparisonOpType type = new BinaryComparisonOpType();
	JAXBElement<BinaryComparisonOpType> isEqualTo = filterFactory.createPropertyIsEqualTo(type);

	PropertyNameType propertyNameType = new PropertyNameType();
	propertyNameType.getContent().add("apiso:Identifier");
	type.getExpression().add(filterFactory.createPropertyName(propertyNameType));

	LiteralType literalType = new LiteralType();
	literalType.getContent().add("X");
	type.getExpression().add(filterFactory.createLiteral(literalType));

	filterType.setComparisonOps(isEqualTo);

	constraint.setFilter(filterType);
	queryType.setConstraint(constraint);

	getRecords.setAbstractQuery(query);

	return getRecords;
    }

    private InputStream asInputStream(Object getRecords) throws JAXBException, UnsupportedEncodingException {

	return CommonContext.asInputStream(getRecords, true);
    }
}
