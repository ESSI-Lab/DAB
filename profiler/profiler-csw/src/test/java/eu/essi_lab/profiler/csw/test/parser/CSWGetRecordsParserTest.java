package eu.essi_lab.profiler.csw.test.parser;

import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.csw._2_0_2.Constraint;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory;
import eu.essi_lab.jaxb.csw._2_0_2.QueryType;
import eu.essi_lab.jaxb.filter._1_1_0.DistanceBufferType;
import eu.essi_lab.jaxb.filter._1_1_0.DistanceType;
import eu.essi_lab.jaxb.filter._1_1_0.FilterType;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.profiler.csw.CSWGetRecordsParser;

public class CSWGetRecordsParserTest {

    @Test
    public void testLogicalBondGetRecords() {

	InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords1.xml");
	try {
	    GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

	    CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);
	    LogicalBond bond = (LogicalBond) parser.parseFilter();

	    Assert.assertEquals(LogicalOperator.AND, bond.getLogicalOperator());

	    List<Bond> andOps = bond.getOperands();
	    Bond[] array = andOps.toArray(new Bond[] {});

	    SimpleValueBond el = (SimpleValueBond) array[0];
	    Assert.assertEquals(MetadataElement.TITLE, el.getProperty());

	    LogicalBond not = (LogicalBond) array[1];
	    SimpleValueBond notOp = (SimpleValueBond) not.getFirstOperand();

	    Assert.assertEquals(MetadataElement.TITLE, notOp.getProperty());

	    LogicalBond or = (LogicalBond) array[2];
	    Bond[] orOps = or.getOperands().toArray(new Bond[] {});

	    SimpleValueBond el2 = (SimpleValueBond) orOps[0];
	    Assert.assertEquals(MetadataElement.TITLE, el2.getProperty());
	    Assert.assertEquals("C", el2.getPropertyValue());

	    SimpleValueBond el3 = (SimpleValueBond) orOps[1];
	    Assert.assertEquals(MetadataElement.TITLE, el2.getProperty());
	    Assert.assertEquals("D", el3.getPropertyValue());

	    LogicalBond and2 = (LogicalBond) orOps[2];
	    Bond[] and2ops = and2.getOperands().toArray(new Bond[] {});

	    SimpleValueBond el4 = (SimpleValueBond) and2ops[0];
	    Assert.assertEquals(MetadataElement.TITLE, el2.getProperty());
	    Assert.assertEquals("E", el4.getPropertyValue());

	    SimpleValueBond el5 = (SimpleValueBond) and2ops[1];
	    Assert.assertEquals(MetadataElement.TITLE, el2.getProperty());
	    Assert.assertEquals("F", el5.getPropertyValue());

	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testSupportedOperatorsGetRecords() {

	InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords2.xml");
	try {
	    GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

	    CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);
	    LogicalBond bond = (LogicalBond) parser.parseFilter();

	    Assert.assertEquals(LogicalOperator.AND, bond.getLogicalOperator());

	    List<Bond> andOps = bond.getOperands();
	    Bond[] array = andOps.toArray(new Bond[] {});

	    {
		SimpleValueBond el = (SimpleValueBond) array[0];
		Assert.assertEquals(BondOperator.LIKE, el.getOperator());
	    }
	    {
		SimpleValueBond el = (SimpleValueBond) array[1];
		Assert.assertEquals(BondOperator.NULL, el.getOperator());
	    }

	    {
		SimpleValueBond el = (SimpleValueBond) array[2];
		Assert.assertEquals(BondOperator.NOT_EQUAL, el.getOperator());
	    }

	    {
		SimpleValueBond el = (SimpleValueBond) array[3];
		Assert.assertEquals(BondOperator.GREATER, el.getOperator());
	    }
	    {
		SimpleValueBond el = (SimpleValueBond) array[4];
		Assert.assertEquals(BondOperator.GREATER_OR_EQUAL, el.getOperator());
	    }

	    {
		SimpleValueBond el = (SimpleValueBond) array[5];
		Assert.assertEquals(BondOperator.LESS, el.getOperator());
	    }
	    {
		SimpleValueBond el = (SimpleValueBond) array[6];
		Assert.assertEquals(BondOperator.LESS_OR_EQUAL, el.getOperator());
	    }

	    Assert.assertEquals(BondOperator.CONTAINS, ((SpatialBond) array[7]).getOperator());
	    Assert.assertEquals(BondOperator.INTERSECTS, ((SpatialBond) array[8]).getOperator());
	    Assert.assertEquals(BondOperator.BBOX, ((SpatialBond) array[9]).getOperator());
	    Assert.assertEquals(BondOperator.DISJOINT, ((SpatialBond) array[10]).getOperator());

	    {
		LogicalBond el = (LogicalBond) array[11];
		Assert.assertEquals(LogicalOperator.NOT, el.getLogicalOperator());

		SimpleValueBond firstOperand = (SimpleValueBond) el.getFirstOperand();
		Assert.assertEquals(BondOperator.EQUAL, firstOperand.getOperator());
	    }
	    {
		LogicalBond el = (LogicalBond) array[12];
		Assert.assertEquals(LogicalOperator.NOT, el.getLogicalOperator());

		SimpleValueBond firstOperand = (SimpleValueBond) el.getFirstOperand();
		Assert.assertEquals(BondOperator.NOT_EQUAL, firstOperand.getOperator());
	    }

	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testCOREQueryablesGetRecords() {

	InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/core/GetRecords1.xml");
	try {

	    GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

	    CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);
	    LogicalBond bond = (LogicalBond) parser.parseFilter();

	    Assert.assertEquals(LogicalOperator.AND, bond.getLogicalOperator());

	    List<Bond> operands = bond.getOperands();
	    Bond[] array = operands.toArray(new Bond[] {});

	    Assert.assertEquals(MetadataElement.SUBJECT.getName(), ((SimpleValueBond) array[0]).getProperty().getName());

	    Assert.assertEquals(MetadataElement.TITLE, ((SimpleValueBond) array[1]).getProperty());
	    Assert.assertEquals(MetadataElement.ABSTRACT, ((SimpleValueBond) array[2]).getProperty());

	    Assert.assertEquals(MetadataElement.ANY_TEXT.getName(), ((SimpleValueBond) array[3]).getProperty().getName());

	    Assert.assertEquals(MetadataElement.DISTRIBUTION_FORMAT, ((SimpleValueBond) array[4]).getProperty());
	    Assert.assertEquals(MetadataElement.IDENTIFIER, ((SimpleValueBond) array[5]).getProperty());

	    Assert.assertEquals(MetadataElement.DATE_STAMP, ((SimpleValueBond) array[6]).getProperty());
	    Assert.assertEquals(MetadataElement.HIERARCHY_LEVEL_CODE_LIST_VALUE, ((SimpleValueBond) array[7]).getProperty());

	    Assert.assertEquals(BondOperator.CONTAINS, ((SpatialBond) array[8]).getOperator());
	    Assert.assertEquals(BondOperator.INTERSECTS, ((SpatialBond) array[9]).getOperator());
	    Assert.assertEquals(BondOperator.BBOX, ((SpatialBond) array[10]).getOperator());
	    Assert.assertEquals(BondOperator.DISJOINT, ((SpatialBond) array[11]).getOperator());

	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testISOQueryablesGetRecords() {

	InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/iso/GetRecords1.xml");
	try {
	    GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

	    CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);
	    LogicalBond bond = (LogicalBond) parser.parseFilter();

	    Assert.assertEquals(LogicalOperator.AND, bond.getLogicalOperator());

	    List<Bond> operands = bond.getOperands();
	    Bond[] array = operands.toArray(new Bond[] {});

	    Assert.assertEquals(MetadataElement.REVISION_DATE, ((SimpleValueBond) array[0]).getProperty());
	    // it is expected that we have 2 bonds that are equals. the previous implementation with sets was strange 
	    Assert.assertEquals(MetadataElement.REVISION_DATE, ((SimpleValueBond) array[1]).getProperty());
	    Assert.assertEquals(MetadataElement.ALTERNATE_TITLE, ((SimpleValueBond) array[2]).getProperty());
	    Assert.assertEquals(MetadataElement.CREATION_DATE, ((SimpleValueBond) array[3]).getProperty());
	    Assert.assertEquals(MetadataElement.PUBLICATION_DATE, ((SimpleValueBond) array[4]).getProperty());
	    Assert.assertEquals(MetadataElement.ORGANISATION_NAME, ((SimpleValueBond) array[5]).getProperty());

	    Assert.assertEquals(MetadataElement.HAS_SECURITY_CONSTRAINTS, ((SimpleValueBond) array[6]).getProperty());
	    Assert.assertEquals(MetadataElement.LANGUAGE, ((SimpleValueBond) array[7]).getProperty());
	    Assert.assertEquals(MetadataElement.RESOURCE_IDENTIFIER, ((SimpleValueBond) array[8]).getProperty());
	    Assert.assertEquals(MetadataElement.PARENT_IDENTIFIER, ((SimpleValueBond) array[9]).getProperty());
	    Assert.assertEquals(MetadataElement.KEYWORD_TYPE, ((SimpleValueBond) array[10]).getProperty());
	    Assert.assertEquals(MetadataElement.TOPIC_CATEGORY, ((SimpleValueBond) array[11]).getProperty());
	    Assert.assertEquals(MetadataElement.RESOURCE_LANGUAGE, ((SimpleValueBond) array[12]).getProperty());
	    Assert.assertEquals(MetadataElement.GEOGRAPHIC_DESCRIPTION_CODE, ((SimpleValueBond) array[13]).getProperty());

	    Assert.assertEquals(MetadataElement.DENOMINATOR, ((SimpleValueBond) array[14]).getProperty());
	    Assert.assertEquals(MetadataElement.DISTANCE_VALUE, ((SimpleValueBond) array[15]).getProperty());
	    Assert.assertEquals(MetadataElement.DISTANCE_UOM, ((SimpleValueBond) array[16]).getProperty());
	    Assert.assertEquals(MetadataElement.TEMP_EXTENT_BEGIN, ((SimpleValueBond) array[17]).getProperty());
	    Assert.assertEquals(MetadataElement.TEMP_EXTENT_END, ((SimpleValueBond) array[18]).getProperty());

	    Assert.assertEquals(MetadataElement.SERVICE_TYPE, ((SimpleValueBond) array[19]).getProperty());
	    Assert.assertEquals(MetadataElement.SERVICE_TYPE_VERSION, ((SimpleValueBond) array[20]).getProperty());
	    Assert.assertEquals(MetadataElement.OPERATION, ((SimpleValueBond) array[21]).getProperty());
	    Assert.assertEquals(MetadataElement.COUPLING_TYPE, ((SimpleValueBond) array[22]).getProperty());
	    Assert.assertEquals(MetadataElement.OPERATES_ON, ((SimpleValueBond) array[23]).getProperty());
	    Assert.assertEquals(MetadataElement.OPERATES_ON_IDENTIFIER, ((SimpleValueBond) array[24]).getProperty());
	    Assert.assertEquals(MetadataElement.OPERATES_ON_NAME, ((SimpleValueBond) array[25]).getProperty());

	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testESSIQueryablesGetRecords() {

	InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/iso/GetRecords2.xml");
	try {
	    GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

	    CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);
	    LogicalBond bond = (LogicalBond) parser.parseFilter();

	    Assert.assertEquals(LogicalOperator.AND, bond.getLogicalOperator());

	    List<Bond> operands = bond.getOperands();
	    Bond[] array = operands.toArray(new Bond[] {});

	    Assert.assertEquals(ResourceProperty.IS_ISO_COMPLIANT.getName(), ((ResourcePropertyBond) array[0]).getProperty().getName());
	    Assert.assertEquals(ResourceProperty.IS_ISO_COMPLIANT.getName(), ((ResourcePropertyBond) array[1]).getProperty().getName());
	    Assert.assertEquals(ResourceProperty.IS_DELETED.getName(), ((ResourcePropertyBond) array[2]).getProperty().getName());
	    Assert.assertEquals(ResourceProperty.IS_GEOSS_DATA_CORE.getName(), ((ResourcePropertyBond) array[3]).getProperty().getName());

	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testEmptyFilterGetRecords() {

	try {

	    {
		GetRecords getRecords = new GetRecords();

		// Marshaller marshaller = CommonContext.createMarshaller(true);
		// marshaller.marshal(getRecords, System.out);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		Bond bond = parser.parseFilter();
		Assert.assertNull(bond);
	    }

	    {
		GetRecords getRecords = new GetRecords();
		QueryType queryType = new QueryType();

		ObjectFactory factory = new eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory();
		JAXBElement<QueryType> query = factory.createQuery(queryType);

		getRecords.setAbstractQuery(query);

		// Marshaller marshaller = CommonContext.createMarshaller(true);
		// marshaller.marshal(getRecords, System.out);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		Bond bond = parser.parseFilter();
		Assert.assertNull(bond);
	    }

	    {
		GetRecords getRecords = new GetRecords();
		QueryType queryType = new QueryType();
		queryType.setConstraint(new Constraint());

		ObjectFactory factory = new eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory();
		JAXBElement<QueryType> query = factory.createQuery(queryType);

		getRecords.setAbstractQuery(query);

		// Marshaller marshaller = CommonContext.createMarshaller(true);
		// marshaller.marshal(getRecords, System.out);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		Bond bond = parser.parseFilter();
		Assert.assertNull(bond);
	    }

	    {
		GetRecords getRecords = new GetRecords();
		QueryType queryType = new QueryType();
		Constraint constraint = new Constraint();
		constraint.setFilter(new FilterType());
		queryType.setConstraint(constraint);

		ObjectFactory factory = new eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory();
		JAXBElement<QueryType> query = factory.createQuery(queryType);

		getRecords.setAbstractQuery(query);

		// Marshaller marshaller = CommonContext.createMarshaller(true);
		// marshaller.marshal(getRecords, System.out);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		Bond bond = parser.parseFilter();
		Assert.assertNull(bond);
	    }

	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testInvalidFilterGetRecords() {

	try {

	    {

		ObjectFactory cswFactory = new eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory();
		eu.essi_lab.jaxb.filter._1_1_0.ObjectFactory filterFactory = new eu.essi_lab.jaxb.filter._1_1_0.ObjectFactory();

		GetRecords getRecords = new GetRecords();
		QueryType queryType = new QueryType();
		Constraint constraint = new Constraint();

		JAXBElement<QueryType> query = cswFactory.createQuery(queryType);

		FilterType filterType = new FilterType();

		DistanceBufferType distanceBuffer = filterFactory.createBeyond(new DistanceBufferType()).getValue();
		distanceBuffer.setDistance(new DistanceType());

		filterType.setSpatialOps(filterFactory.createSpatialOps(distanceBuffer));

		constraint.setFilter(filterType);
		queryType.setConstraint(constraint);

		getRecords.setAbstractQuery(query);

		// Marshaller marshaller = CommonContext.createMarshaller(true);
		// marshaller.marshal(getRecords, System.out);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		try {
		    parser.parseFilter();
		    fail("Illegal argument exception not thrown");
		} catch (IllegalArgumentException ex) {

		    String message = ex.getMessage();
		    Assert.assertEquals("Distance Spatial Operator not supported", message);

		} catch (GSException e) {
		    e.printStackTrace();
		    fail("Internal exception thrown");
		}
	    }

	    {

		InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
			.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords3.xml");

		GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		try {
		    parser.parseFilter();
		    fail("Illegal argument exception not thrown");

		} catch (IllegalArgumentException ex) {

		    String message = ex.getMessage();
		    Assert.assertEquals("Unsupported filter", message);

		} catch (GSException e) {
		    e.printStackTrace();
		    fail("Internal exception thrown");
		}
	    }

	    {

		InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
			.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords4.xml");

		GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		try {
		    parser.parseFilter();
		    fail("Exception not thrown");

		} catch (IllegalArgumentException ex) {

		    String message = ex.getMessage();
		    Assert.assertEquals("Unsupported property name: X", message);

		} catch (GSException e) {
		    e.printStackTrace();
		    fail("Exception thrown");
		}
	    }

	    {

		InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
			.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords5.xml");

		GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		try {
		    parser.parseFilter();
		    fail("Exception not thrown");

		} catch (IllegalArgumentException ex) {

		    String message = ex.getMessage();
		    Assert.assertEquals("Unsupported operator for the Not Logical operator", message);

		} catch (GSException e) {
		    e.printStackTrace();
		    fail("Exception thrown");
		}
	    }

	    {

		InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
			.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords6.xml");

		GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		try {
		    parser.parseFilter();
		    fail("Exception not thrown");

		} catch (IllegalArgumentException ex) {

		    String message = ex.getMessage();
		    Assert.assertEquals("Distance Spatial Operator not supported", message);

		} catch (GSException e) {
		    e.printStackTrace();
		    fail("Exception thrown");
		}
	    }

	    {

		InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
			.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords7.xml");

		GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		try {
		    parser.parseFilter();
		    fail("Exception not thrown");

		} catch (IllegalArgumentException ex) {

		    String message = ex.getMessage();
		    Assert.assertEquals("Unsupported spatial operator: Touches", message);

		} catch (GSException e) {
		    e.printStackTrace();
		    fail("Exception thrown");
		}
	    }

	    {

		InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
			.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords8.xml");

		GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);
		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		try {
		    parser.parseFilter();
		    fail("Exception not thrown");

		} catch (IllegalArgumentException ex) {

		    String message = ex.getMessage();
		    Assert.assertEquals("Unsupported spatial operator: Crosses", message);

		} catch (GSException e) {
		    e.printStackTrace();
		    fail("Exception thrown");
		}
	    }

	    {

		InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
			.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords9.xml");

		GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		try {
		    parser.parseFilter();
		    fail("Exception not thrown");

		} catch (IllegalArgumentException ex) {

		    String message = ex.getMessage();
		    Assert.assertEquals("Unsupported spatial operator: Overlaps", message);

		} catch (GSException e) {
		    e.printStackTrace();
		    fail("Exception thrown");
		}
	    }

	    {

		InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
			.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords10.xml");

		GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		try {
		    parser.parseFilter();
		    fail("Exception not thrown");

		} catch (IllegalArgumentException ex) {

		    String message = ex.getMessage();
		    Assert.assertEquals("Distance Spatial Operator not supported", message);

		} catch (GSException e) {
		    e.printStackTrace();
		    fail("Exception thrown");
		}
	    }

	    {

		InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
			.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords12.xml");

		GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		try {
		    parser.parseFilter();
		    fail("Exception not thrown");

		} catch (IllegalArgumentException ex) {

		    String message = ex.getMessage();
		    Assert.assertEquals("Unsupported spatial operator: Equals", message);

		} catch (GSException e) {
		    e.printStackTrace();
		    fail("Exception thrown");
		}
	    }

	    {

		InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
			.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords13.xml");

		GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		try {
		    parser.parseFilter();
		    fail("Exception not thrown");

		} catch (IllegalArgumentException ex) {

		    String message = ex.getMessage();
		    Assert.assertEquals("Unsupported geometry: Point", message);

		} catch (GSException e) {
		    e.printStackTrace();
		    fail("Exception thrown");
		}
	    }

	    {

		InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
			.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords14.xml");

		GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		try {
		    parser.parseFilter();
		    fail("Exception not thrown");

		} catch (IllegalArgumentException ex) {

		    String message = ex.getMessage();
		    Assert.assertEquals("Uncomplete expression of Binary Comparison operator", message);

		} catch (GSException e) {
		    e.printStackTrace();
		    fail("Exception thrown");
		}
	    }

	    {

		InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
			.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords15.xml");

		GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		try {
		    parser.parseFilter();
		    fail("Exception not thrown");

		} catch (IllegalArgumentException ex) {

		    String message = ex.getMessage();
		    Assert.assertEquals("Literal, Literal Comparison operators not supported", message);

		} catch (GSException e) {
		    e.printStackTrace();
		    fail("Exception thrown");
		}
	    }

	    {

		InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
			.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords16.xml");

		GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		try {
		    parser.parseFilter();
		    fail("Exception not thrown");

		} catch (IllegalArgumentException ex) {

		    String message = ex.getMessage();
		    Assert.assertEquals("Missing mandatory geometry-valued property", message);

		} catch (GSException e) {
		    e.printStackTrace();
		    fail("Exception thrown");
		}
	    }

	    {

		InputStream stream = CSWGetRecordsParserTest.class.getClassLoader()
			.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords17.xml");

		GetRecords getRecords = CommonContext.unmarshal(stream, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);

		try {
		    parser.parseFilter();
		    fail("Exception not thrown");

		} catch (IllegalArgumentException ex) {

		    String message = ex.getMessage();
		    Assert.assertEquals("Missing Envelope upper corner values", message);

		} catch (GSException e) {
		    e.printStackTrace();
		    fail("Exception thrown");
		}
	    }

	} catch (JAXBException e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testWrongEnvelopeValuesGetRecords() {

	InputStream stream18 = CSWGetRecordsParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords18.xml");

	InputStream stream19 = CSWGetRecordsParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords19.xml");

	InputStream stream20 = CSWGetRecordsParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords20.xml");
	InputStream stream21 = CSWGetRecordsParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords21.xml");
	InputStream stream22 = CSWGetRecordsParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords22.xml");
	InputStream stream23 = CSWGetRecordsParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/profiler/csw/test/parser/GetRecords23.xml");
	try {

	    GetRecords getRecords = CommonContext.unmarshal(stream18, GetRecords.class);

	    CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);
	    parser.parseFilter();

	    fail("Illegal Argument Exception not thrown");

	} catch (IllegalArgumentException e) {

	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}

	try {
	    {
		GetRecords getRecords = CommonContext.unmarshal(stream19, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);
		parser.parseFilter();
	    }

	    fail("Illegal Argument Exception not thrown");

	} catch (IllegalArgumentException e) {

	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}

	try {
	    {
		GetRecords getRecords = CommonContext.unmarshal(stream20, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);
		parser.parseFilter();
	    }

	    fail("Illegal Argument Exception not thrown");

	} catch (IllegalArgumentException e) {
	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}

	try {
	    {
		GetRecords getRecords = CommonContext.unmarshal(stream21, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);
		parser.parseFilter();
	    }

	    fail("Illegal Argument Exception not thrown");

	} catch (IllegalArgumentException e) {
	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}

	try {
	    {
		GetRecords getRecords = CommonContext.unmarshal(stream22, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);
		parser.parseFilter();
	    }

	    fail("Illegal Argument Exception not thrown");

	} catch (IllegalArgumentException e) {
	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}

	try {
	    {
		GetRecords getRecords = CommonContext.unmarshal(stream23, GetRecords.class);

		CSWGetRecordsParser parser = new CSWGetRecordsParser(getRecords);
		parser.parseFilter();
	    }

	    fail("Illegal Argument Exception not thrown");

	} catch (IllegalArgumentException e) {
	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }
}
