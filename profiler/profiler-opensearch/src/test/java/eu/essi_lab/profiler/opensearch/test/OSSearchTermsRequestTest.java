package eu.essi_lab.profiler.opensearch.test;

import static org.junit.Assert.fail;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.profiler.os.handler.discover.OSRequestTransformer;

public class OSSearchTermsRequestTest {

    @Test
    public void testCase1_Title() {

	String queryString = "http://localhost:9090/gs-service/services/essi/opensearch?outputFormat=" + MediaType.APPLICATION_JSON
		+ "&si=1&ct=5&st=WATER&searchFields=title&outputVersion=2.0";

	WebRequest webRequest = WebRequest.createGET(queryString);

	OSRequestTransformer transformer = new OSRequestTransformer();

	try {

	    SimpleValueBond userBond = (SimpleValueBond) transformer.getUserBond(webRequest);

	    Assert.assertEquals(userBond.getOperator(), BondOperator.LIKE);

	    Assert.assertEquals(userBond.getProperty().getName(), "title");

	    Assert.assertEquals(userBond.getPropertyValue(), "WATER");

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testCase1_AnyText() {

	String queryString = "http://localhost:9090/gs-service/services/essi/opensearch?outputFormat=" + MediaType.APPLICATION_JSON
		+ "&si=1&ct=5&st=WATER&searchFields=anyText&outputVersion=2.0";

	WebRequest webRequest = WebRequest.createGET(queryString);

	OSRequestTransformer transformer = new OSRequestTransformer();

	try {

	    SimpleValueBond userBond = (SimpleValueBond) transformer.getUserBond(webRequest);

	    Assert.assertEquals(userBond.getOperator(), BondOperator.LIKE);

	    Assert.assertEquals(userBond.getProperty().getName(), "anyText");

	    Assert.assertEquals(userBond.getPropertyValue(), "WATER");

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testCase2() {

	String queryString = "http://localhost:9090/gs-service/services/essi/opensearch?outputFormat=" + MediaType.APPLICATION_JSON
		+ "&si=1&ct=5&st=WATER OR TEMPERATURE&searchFields=title&outputVersion=2.0";

	WebRequest webRequest = WebRequest.createGET(queryString);

	OSRequestTransformer transformer = new OSRequestTransformer();

	try {

	    LogicalBond userBond = (LogicalBond) transformer.getUserBond(webRequest);

	    Assert.assertEquals(userBond.getLogicalOperator(), LogicalOperator.OR);

	    Bond[] array = userBond.getOperands().toArray(new Bond[] {});

	    SimpleValueBond waterBond = (SimpleValueBond) array[0];
	    SimpleValueBond temperatureBond = (SimpleValueBond) array[1];

	    Assert.assertEquals(waterBond.getProperty().getName(), "title");
	    Assert.assertEquals(waterBond.getPropertyValue(), "WATER");

	    Assert.assertEquals(temperatureBond.getProperty().getName(), "title");
	    Assert.assertEquals(temperatureBond.getPropertyValue(), "TEMPERATURE");

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testCase3() {

	String queryString = "http://localhost:9090/gs-service/services/essi/opensearch?outputFormat=" + MediaType.APPLICATION_JSON
		+ "&si=1&ct=5&st=WATER AND TEMPERATURE&searchFields=title&outputVersion=2.0";

	WebRequest webRequest = WebRequest.createGET(queryString);

	OSRequestTransformer transformer = new OSRequestTransformer();

	try {

	    LogicalBond userBond = (LogicalBond) transformer.getUserBond(webRequest);

	    Assert.assertEquals(userBond.getLogicalOperator(), LogicalOperator.AND);

	    Bond[] array = userBond.getOperands().toArray(new Bond[] {});

	    SimpleValueBond waterBond = (SimpleValueBond) array[0];
	    SimpleValueBond temperatureBond = (SimpleValueBond) array[1];

	    Assert.assertEquals(waterBond.getProperty().getName(), "title");
	    Assert.assertEquals(waterBond.getPropertyValue(), "WATER");

	    Assert.assertEquals(temperatureBond.getProperty().getName(), "title");
	    Assert.assertEquals(temperatureBond.getPropertyValue(), "TEMPERATURE");

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testCase4_DefaultSearchFields() {

	String queryString = "http://localhost:9090/gs-service/services/essi/opensearch?outputFormat=" + MediaType.APPLICATION_JSON
		+ "&si=1&ct=5&st=WATER&outputVersion=2.0";

	WebRequest webRequest = WebRequest.createGET(queryString);

	OSRequestTransformer transformer = new OSRequestTransformer();

	try {

	    LogicalBond userBond = (LogicalBond) transformer.getUserBond(webRequest);

	    Assert.assertEquals(userBond.getLogicalOperator(), LogicalOperator.OR);

	    Bond[] array = userBond.getOperands().toArray(new Bond[] {});

	    SimpleValueBond titleBond = (SimpleValueBond) array[0];
	    SimpleValueBond SimpleElementBond = (SimpleValueBond) array[1];

	    Assert.assertEquals(SimpleElementBond.getProperty().getName(), "subject");
	    Assert.assertEquals(SimpleElementBond.getPropertyValue(), "WATER");

	    Assert.assertEquals(titleBond.getProperty().getName(), "title");
	    Assert.assertEquals(titleBond.getPropertyValue(), "WATER");

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testCase4_TitleKeyword() {

	String queryString = "http://localhost:9090/gs-service/services/essi/opensearch?outputFormat=" + MediaType.APPLICATION_JSON
		+ "&si=1&ct=5&st=WATER&searchFields=title,keyword&outputVersion=2.0";

	WebRequest webRequest = WebRequest.createGET(queryString);

	OSRequestTransformer transformer = new OSRequestTransformer();

	try {

	    LogicalBond userBond = (LogicalBond) transformer.getUserBond(webRequest);

	    Assert.assertEquals(userBond.getLogicalOperator(), LogicalOperator.OR);

	    Bond[] array = userBond.getOperands().toArray(new Bond[] {});

	    SimpleValueBond titleBond = (SimpleValueBond) array[0];
	    SimpleValueBond kwdBond = (SimpleValueBond) array[1];

	    Assert.assertEquals(kwdBond.getProperty().getName(), "keyword");
	    Assert.assertEquals(kwdBond.getPropertyValue(), "WATER");

	    Assert.assertEquals(titleBond.getProperty().getName(), "title");
	    Assert.assertEquals(titleBond.getPropertyValue(), "WATER");

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testCase4_TitleKeywordAbstract() {

	String queryString = "http://localhost:9090/gs-service/services/essi/opensearch?outputFormat=" + MediaType.APPLICATION_JSON
		+ "&si=1&ct=5&st=WATER&searchFields=title,keyword,abstract&outputVersion=2.0";

	WebRequest webRequest = WebRequest.createGET(queryString);

	OSRequestTransformer transformer = new OSRequestTransformer();

	try {

	    LogicalBond userBond = (LogicalBond) transformer.getUserBond(webRequest);

	    Assert.assertEquals(userBond.getLogicalOperator(), LogicalOperator.OR);

	    Bond[] array = userBond.getOperands().toArray(new Bond[] {});

	    SimpleValueBond titleBond = (SimpleValueBond) array[0];
	    SimpleValueBond absBond = (SimpleValueBond) array[1];
	    SimpleValueBond kwdBond = (SimpleValueBond) array[2];

	    Assert.assertEquals(titleBond.getProperty().getName(), "title");
	    Assert.assertEquals(titleBond.getPropertyValue(), "WATER");

	    Assert.assertEquals(kwdBond.getProperty().getName(), "keyword");
	    Assert.assertEquals(kwdBond.getPropertyValue(), "WATER");

	    Assert.assertEquals(absBond.getProperty().getName(), "abstract");
	    Assert.assertEquals(absBond.getPropertyValue(), "WATER");

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testCase5_DefaultSearchFields() {

	String queryString = "http://localhost:9090/gs-service/services/essi/opensearch?outputFormat=" + MediaType.APPLICATION_JSON
		+ "&si=1&ct=5&st=WATER OR TEMPERATURE&outputVersion=2.0";

	WebRequest webRequest = WebRequest.createGET(queryString);

	OSRequestTransformer transformer = new OSRequestTransformer();

	try {

	    LogicalBond userBond = (LogicalBond) transformer.getUserBond(webRequest);

	    Assert.assertEquals(userBond.getLogicalOperator(), LogicalOperator.OR);

	    Bond[] array = userBond.getOperands().toArray(new Bond[] {});

	    LogicalBond b1 = (LogicalBond) array[0];
	    LogicalBond b2 = (LogicalBond) array[1];

	    Assert.assertEquals(b1.getLogicalOperator(), LogicalOperator.OR);
	    Assert.assertEquals(b2.getLogicalOperator(), LogicalOperator.OR);

	    Bond[] logicOp1Array = b1.getOperands().toArray(new Bond[] {});

	    SimpleValueBond titleBond1 = (SimpleValueBond) logicOp1Array[0];
	    SimpleValueBond SimpleElementBond1 = (SimpleValueBond) logicOp1Array[1];

	    Assert.assertEquals(SimpleElementBond1.getProperty().getName(), "subject");
	    Assert.assertEquals(SimpleElementBond1.getPropertyValue(), "WATER");

	    Assert.assertEquals(titleBond1.getProperty().getName(), "title");
	    Assert.assertEquals(titleBond1.getPropertyValue(), "WATER");

	    Bond[] logicOp2Array = b1.getOperands().toArray(new Bond[] {});

	    SimpleValueBond titleBond2 = (SimpleValueBond) logicOp2Array[0];
	    SimpleValueBond SimpleElementBond2 = (SimpleValueBond) logicOp2Array[1];

	    Assert.assertEquals(titleBond2.getProperty().getName(), "title");
	    Assert.assertEquals(titleBond2.getPropertyValue(), "WATER");

	    Assert.assertEquals(SimpleElementBond2.getProperty().getName(), "subject");
	    Assert.assertEquals(SimpleElementBond2.getPropertyValue(), "WATER");

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testCase6_DefaultSearchFields() {

	String queryString = "http://localhost:9090/gs-service/services/essi/opensearch?outputFormat=" + MediaType.APPLICATION_JSON
		+ "&si=1&ct=5&st=WATER AND TEMPERATURE&outputVersion=2.0";

	WebRequest webRequest = WebRequest.createGET(queryString);

	OSRequestTransformer transformer = new OSRequestTransformer();

	try {

	    LogicalBond userBond = (LogicalBond) transformer.getUserBond(webRequest);

	    Assert.assertEquals(userBond.getLogicalOperator(), LogicalOperator.AND);

	    Bond[] array = userBond.getOperands().toArray(new Bond[] {});

	    LogicalBond b1 = (LogicalBond) array[0];
	    LogicalBond b2 = (LogicalBond) array[1];

	    Assert.assertEquals(b1.getLogicalOperator(), LogicalOperator.OR);
	    Assert.assertEquals(b2.getLogicalOperator(), LogicalOperator.OR);

	    Bond[] logicOp1Array = b1.getOperands().toArray(new Bond[] {});

	    SimpleValueBond titleBond1 = (SimpleValueBond) logicOp1Array[0];
	    SimpleValueBond SimpleElementBond1 = (SimpleValueBond) logicOp1Array[1];

	    Assert.assertEquals(SimpleElementBond1.getProperty().getName(), "subject");
	    Assert.assertEquals(SimpleElementBond1.getPropertyValue(), "WATER");

	    Assert.assertEquals(titleBond1.getProperty().getName(), "title");
	    Assert.assertEquals(titleBond1.getPropertyValue(), "WATER");

	    Bond[] logicOp2Array = b1.getOperands().toArray(new Bond[] {});

	    SimpleValueBond titleBond2 = (SimpleValueBond) logicOp2Array[0];
	    SimpleValueBond SimpleElementBond2 = (SimpleValueBond) logicOp2Array[1];

	    Assert.assertEquals(titleBond2.getProperty().getName(), "title");
	    Assert.assertEquals(titleBond2.getPropertyValue(), "WATER");

	    Assert.assertEquals(SimpleElementBond2.getProperty().getName(), "subject");
	    Assert.assertEquals(SimpleElementBond2.getPropertyValue(), "WATER");

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testCase6_TitleAnyText() {

	String queryString = "http://localhost:9090/gs-service/services/essi/opensearch?outputFormat=" + MediaType.APPLICATION_JSON
		+ "&si=1&ct=5&st=WATER AND TEMPERATURE&searchFields=title,anytext&outputVersion=2.0";

	WebRequest webRequest = WebRequest.createGET(queryString);

	OSRequestTransformer transformer = new OSRequestTransformer();

	try {

	    LogicalBond userBond = (LogicalBond) transformer.getUserBond(webRequest);

	    Assert.assertEquals(userBond.getLogicalOperator(), LogicalOperator.AND);

	    Bond[] array = userBond.getOperands().toArray(new Bond[] {});

	    LogicalBond b1 = (LogicalBond) array[0];
	    LogicalBond b2 = (LogicalBond) array[1];

	    Assert.assertEquals(b1.getLogicalOperator(), LogicalOperator.OR);
	    Assert.assertEquals(b2.getLogicalOperator(), LogicalOperator.OR);

	    Bond[] logicOp1Array = b1.getOperands().toArray(new Bond[] {});

	    SimpleValueBond SimpleElementBond1 = (SimpleValueBond) logicOp1Array[0];
	    SimpleValueBond titleBond1 = (SimpleValueBond) logicOp1Array[1];

	    Assert.assertEquals(SimpleElementBond1.getProperty().getName(), "anyText");
	    Assert.assertEquals(SimpleElementBond1.getPropertyValue(), "WATER");

	    Assert.assertEquals(titleBond1.getProperty().getName(), "title");
	    Assert.assertEquals(titleBond1.getPropertyValue(), "WATER");

	    Bond[] logicOp2Array = b1.getOperands().toArray(new Bond[] {});

	    SimpleValueBond SimpleElementBond2 = (SimpleValueBond) logicOp2Array[0];
	    SimpleValueBond titleBond2 = (SimpleValueBond) logicOp2Array[1];

	    Assert.assertEquals(titleBond2.getProperty().getName(), "title");
	    Assert.assertEquals(titleBond2.getPropertyValue(), "WATER");

	    Assert.assertEquals(SimpleElementBond2.getProperty().getName(), "anyText");
	    Assert.assertEquals(SimpleElementBond2.getPropertyValue(), "WATER");

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

}
