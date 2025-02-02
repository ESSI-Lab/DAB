package eu.essi_lab.request.executor.discover;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import eu.essi_lab.authorization.converter.IRequestAuthorizationConverter;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.EmptyBond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * These tests test the initialization of different queries of increasing complexity. For each test, a first sub test is
 * run disabling the request authorization converter (by mocking it letting it return the original bond as the permitted
 * bond). Then, the other sub tests mock the request authorization converter in order to add some predefined set of
 * source bond constraints. The query initialization result is then checked to be equal to the expected result for each
 * case.
 *
 * @author boldrini
 */
public class QueryInitializerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    Bond property1Bond;
    Bond property2Bond;
    Bond property3Bond;
    Bond source1Bond;
    Bond source2Bond;
    Bond source12Bond;
    Bond source3Bond;
    Bond source4Bond;
    Bond source123Bond;
    Set<String> sourceSet1 = new HashSet<>();
    Set<String> sourceSet12 = new HashSet<>();
    Set<String> sourceSet123 = new HashSet<>();
    LogicalBond s1p1Bond;
    LogicalBond s2p1Bond;
    LogicalBond orBond;
    LogicalBond notBond;
    private QueryInitializer queryInitializer;
    private DiscoveryMessage message;
    private IRequestAuthorizationConverter requestAuthorizationConverter;

    @Before
    public void init() {
	this.queryInitializer = new QueryInitializer();
	this.requestAuthorizationConverter = Mockito.mock(IRequestAuthorizationConverter.class);

	this.queryInitializer.setRequestAuthorizationConverter(requestAuthorizationConverter);
	message = new DiscoveryMessage();
	property1Bond = BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "p1");
	property2Bond = BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "p2");
	property3Bond = BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "p3");
	source1Bond = BondFactory.createSourceIdentifierBond("source1");
	source2Bond = BondFactory.createSourceIdentifierBond("source2");
	source12Bond = BondFactory.createOrBond(source1Bond, source2Bond);
	source3Bond = BondFactory.createSourceIdentifierBond("source3");
	source4Bond = BondFactory.createSourceIdentifierBond("source4");
	source123Bond = BondFactory.createOrBond(source1Bond, source2Bond, source3Bond);
	sourceSet1.add("source1");
	sourceSet12.add("source1");
	sourceSet12.add("source2");
	sourceSet123.add("source1");
	sourceSet123.add("source2");
	sourceSet123.add("source3");
	s1p1Bond = BondFactory.createAndBond(property1Bond, source1Bond);
	s2p1Bond = BondFactory.createAndBond(property1Bond, source2Bond);
	orBond = BondFactory.createOrBond(property1Bond, source1Bond);
	notBond = BondFactory.createNotBond(property1Bond);

    }

    /**
     * Do the test disabling the authorization request converter
     *
     * @throws GSException
     */
    private void doTest() throws GSException {
	Set<String> sourceIdentifiers = new HashSet<>();
	doTestWithAuthorizedSources(sourceIdentifiers);
    }

    /**
     * Do the test by mocking the request authorization converter returning the authorized bond
     * that is a set of source bonds (ORed) generated from the given source identifiers
     *
     * @param sourceIdentifiers
     * @throws GSException
     */
    private void doTestWithAuthorizedSources(Set<String> sourceIdentifiers) throws GSException {
	//

	Bond authorizedBond;
	if (sourceIdentifiers.isEmpty()) {
	    authorizedBond = null;
	} else {
	    Set<Bond> sourceBonds = new HashSet<>();
	    for (String sourceIdentifier : sourceIdentifiers) {
		sourceBonds.add(BondFactory.createSourceIdentifierBond(sourceIdentifier));
	    }
	    if (sourceBonds.size() == 1) {
		authorizedBond = sourceBonds.iterator().next();
	    } else {
		authorizedBond = BondFactory.createOrBond(sourceBonds);
	    }
	}

	Mockito.when(requestAuthorizationConverter.generateAuthorizedBond(ArgumentMatchers.any(DiscoveryMessage.class)))
		.thenReturn(authorizedBond);
	queryInitializer.initializeQuery(message);

    }

    /**
     * Tests with {@link DiscoveryMessage#BOND} = null -> null
     * Tests adding S1 -> S1
     * Tests adding S1, S2 -> S1, S2
     * Tests adding S1, S2, S3 -> S1, S2, S3
     */
    @Test
    public void testNullBond() throws GSException {
	doTest();
	Assert.assertNull(message.getPermittedBond());
	Assert.assertNull(message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet1);
	Assert.assertEquals(source1Bond, message.getPermittedBond());
	Assert.assertEquals(source1Bond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet12);
	Assert.assertEquals(BondFactory.createOrBond(source1Bond, source2Bond), message.getPermittedBond());
	Assert.assertEquals(BondFactory.createOrBond(source1Bond, source2Bond), message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet123);
	Assert.assertEquals(BondFactory.createOrBond(source1Bond, source2Bond, source3Bond), message.getPermittedBond());
	Assert.assertEquals(BondFactory.createOrBond(source1Bond, source2Bond, source3Bond), message.getNormalizedBond());
    }

    /**
     * Tests with {@link DiscoveryMessage#BOND} = P1 -> P1
     * Tests adding S1 -> P1 AND S1
     * Tests adding S1, S2 -> P1 AND S1 OR P1 AND S2
     * Tests adding S1, S2, S3 -> P1 AND S1 OR P1 AND S2 OR P1 AND S3
     */
    @Test
    public void testBond() throws GSException {
	message.setUserBond(property1Bond);

	doTest();
	Assert.assertEquals(property1Bond, message.getPermittedBond());
	Assert.assertEquals(property1Bond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet1);
	Assert.assertEquals(BondFactory.createAndBond(property1Bond, source1Bond), message.getPermittedBond());
	Assert.assertEquals(BondFactory.createAndBond(property1Bond, source1Bond), message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet12);
	Assert.assertEquals(BondFactory.createAndBond( //
		property1Bond, source12Bond), //
		message.getPermittedBond());
	Assert.assertEquals(BondFactory.createOrBond( //
		BondFactory.createAndBond(property1Bond, source1Bond), //
		BondFactory.createAndBond(property1Bond, source2Bond))//
		, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet123);
	Assert.assertEquals(BondFactory.createAndBond( //
		property1Bond, source123Bond), //
		message.getPermittedBond());
	Assert.assertEquals(BondFactory.createOrBond( //
		BondFactory.createAndBond(property1Bond, source1Bond), //
		BondFactory.createAndBond(property1Bond, source2Bond), //
		BondFactory.createAndBond(property1Bond, source3Bond))//
		, message.getNormalizedBond());

    }

    /**
     * Tests with {@link DiscoveryMessage#BOND} = S1 -> S1
     * Tests adding S1 -> S1
     * Tests adding S1, S2 -> S1
     * Tests adding S1, S2, S3 -> S1
     */
    @Test
    public void testSourceBond() throws GSException {
	message.setUserBond(source1Bond);
	doTest();
	Assert.assertEquals(source1Bond, message.getPermittedBond());
	Assert.assertEquals(source1Bond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet1);
	Assert.assertEquals(source1Bond, message.getPermittedBond());
	Assert.assertEquals(source1Bond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet12);
	Assert.assertEquals(source1Bond, // because of optimization
		message.getPermittedBond());
	Assert.assertEquals(source1Bond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet123);
	Assert.assertEquals(source1Bond, // because of optimization
		message.getPermittedBond());
	Assert.assertEquals(source1Bond, message.getNormalizedBond());
    }

    /**
     * Tests with {@link DiscoveryMessage#BOND} = S1 AND P1 -> S1 AND P1
     * Tests adding S1 -> S1 AND P1
     * Tests adding S1, S2 -> S1 AND P1
     * Tests adding S1, S2, S3 -> S1 AND P1
     */
    @Test
    public void testAndBond() throws GSException {
	message.setUserBond(s1p1Bond);
	doTest();
	Assert.assertEquals(s1p1Bond, message.getPermittedBond());
	Assert.assertEquals(s1p1Bond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet1);
	Assert.assertEquals(s1p1Bond, message.getPermittedBond());
	Assert.assertEquals(s1p1Bond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet12);
	Assert.assertEquals(s1p1Bond, //
		message.getPermittedBond());
	Assert.assertEquals(s1p1Bond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet123);
	Assert.assertEquals(s1p1Bond, //
		message.getPermittedBond());
	Assert.assertEquals(s1p1Bond, message.getNormalizedBond());
    }

    /**
     * Tests with {@link DiscoveryMessage#BOND} = S1 OR P1 -> S1 OR P1
     * Tests adding S1 -> S1
     * Tests adding S1, S2 -> S1 OR (S2 AND P1)
     * Tests adding S1, S2, S3 -> S1 OR (S2 AND P1) OR (S3 AND P1)
     */
    @Test
    public void testOrBond() throws GSException {
	message.setUserBond(orBond);
	doTest();
	Assert.assertEquals(orBond, message.getPermittedBond());
	Assert.assertEquals(orBond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet1);
	Assert.assertEquals(BondFactory.createAndBond(orBond, source1Bond), message.getPermittedBond());
	Assert.assertEquals(source1Bond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet12);
	Assert.assertEquals(BondFactory.createAndBond(orBond, source12Bond), message.getPermittedBond());
	Assert.assertEquals(//
		BondFactory.createOrBond(source1Bond, //
			BondFactory.createAndBond(source2Bond, property1Bond)//
		), message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet123);
	Assert.assertEquals(BondFactory.createAndBond(orBond, source123Bond), message.getPermittedBond());
	Assert.assertEquals(//
		BondFactory.createOrBond(source1Bond, //
			BondFactory.createAndBond(source2Bond, property1Bond), //
			BondFactory.createAndBond(source3Bond, property1Bond)//
		), message.getNormalizedBond());
    }

    /**
     * Tests with {@link DiscoveryMessage#BOND} = NOT(P1) -> NOT(P1)
     * Tests adding S1 -> S1 AND NOT(P1)
     * Tests adding S1, S2 -> (S1 AND NOT(P1)) OR (S2 AND NOT(P1))
     * Tests adding S1, S2, S3 -> (S1 AND NOT(P1)) OR (S2 AND NOT(P1)) OR (S3 AND NOT(P1))
     */
    @Test
    public void testNotBond() throws GSException {
	message.setUserBond(notBond);
	doTest();
	Assert.assertEquals(notBond, message.getPermittedBond());
	Assert.assertEquals(notBond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet1);
	Assert.assertEquals(//
		BondFactory.createAndBond(source1Bond, notBond)//
		, message.getPermittedBond());
	Assert.assertEquals(//
		BondFactory.createAndBond(source1Bond, notBond)//
		, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet12);
	Assert.assertEquals(BondFactory.createAndBond( //
		notBond, source12Bond), //
		message.getPermittedBond());
	Assert.assertEquals(//
		BondFactory.createOrBond( //
			BondFactory.createAndBond(source1Bond, notBond), //
			BondFactory.createAndBond(source2Bond, notBond)//
		)//
		, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet123);
	Assert.assertEquals(BondFactory.createAndBond( //
		notBond, source123Bond), //
		message.getPermittedBond());
	Assert.assertEquals(//
		BondFactory.createOrBond( //
			BondFactory.createAndBond(source1Bond, notBond), //
			BondFactory.createAndBond(source2Bond, notBond), //
			BondFactory.createAndBond(source3Bond, notBond)//
		)//
		, message.getNormalizedBond());
    }

    /**
     * Tests with {@link DiscoveryMessage#BOND} = NOT(NOT(P1)) -> P1
     * Tests adding S1 -> S1 AND P1
     * Tests adding S1, S2 -> (S1 AND P1) OR (S2 AND P1)
     * Tests adding S1, S2, S3 -> (S1 AND P1) OR (S2 AND P1) OR (S3 AND P1)
     */
    @Test
    public void testNotNotBond() throws GSException {
	LogicalBond userBond = BondFactory.createNotBond(notBond);
	message.setUserBond( //
		userBond);
	doTest();
	Assert.assertEquals(userBond, message.getPermittedBond());
	Assert.assertEquals(notBond.getFirstOperand(), message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet1);
	Assert.assertEquals(//
		BondFactory.createAndBond(source1Bond, userBond)//
		, message.getPermittedBond());
	Assert.assertEquals(//
		BondFactory.createAndBond(source1Bond, property1Bond)//
		, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet12);
	Assert.assertEquals(//
		BondFactory.createAndBond(source12Bond, userBond)//
		, message.getPermittedBond());
	Assert.assertEquals(//
		BondFactory.createOrBond( //
			BondFactory.createAndBond(source1Bond, property1Bond), //
			BondFactory.createAndBond(source2Bond, property1Bond)//
		)//
		, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet123);
	Assert.assertEquals(//
		BondFactory.createAndBond(source123Bond, userBond)//
		, message.getPermittedBond());
	Assert.assertEquals(//
		BondFactory.createOrBond( //
			BondFactory.createAndBond(source1Bond, property1Bond), //
			BondFactory.createAndBond(source2Bond, property1Bond), //
			BondFactory.createAndBond(source3Bond, property1Bond)//
		)//
		, message.getNormalizedBond());
    }

    /**
     * Tests with {@link DiscoveryMessage#BOND} = NOT(NOT(NOT(P1))) -> NOT(P1)
     * Tests adding S1 -> S1 AND NOT(P1)
     * Tests adding S1, S2 -> (S1 AND NOT(P1)) OR (S2 AND NOT(P1))
     * Tests adding S1, S2, S3 -> (S1 AND NOT(P1)) OR (S2 AND NOT(P1)) OR (S3 AND NOT(P1))
     */
    @Test
    public void testNotNotNotBond() throws GSException {
	LogicalBond userBond = BondFactory.createNotBond( //
		BondFactory.createNotBond(notBond));
	message.setUserBond(userBond);
	doTest();
	Assert.assertEquals(userBond, message.getPermittedBond());
	Assert.assertEquals(notBond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet1);
	Assert.assertEquals(//
		BondFactory.createAndBond(source1Bond, userBond)//
		, message.getPermittedBond());
	Assert.assertEquals(//
		BondFactory.createAndBond(source1Bond, notBond)//
		, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet12);
	Assert.assertEquals(BondFactory.createAndBond( //
		userBond, source12Bond), //
		message.getPermittedBond());
	Assert.assertEquals(//
		BondFactory.createOrBond( //
			BondFactory.createAndBond(source1Bond, notBond), //
			BondFactory.createAndBond(source2Bond, notBond)//
		)//
		, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet123);
	Assert.assertEquals(BondFactory.createAndBond( //
		userBond, source123Bond), //
		message.getPermittedBond());
	Assert.assertEquals(//
		BondFactory.createOrBond( //
			BondFactory.createAndBond(source1Bond, notBond), //
			BondFactory.createAndBond(source2Bond, notBond), //
			BondFactory.createAndBond(source3Bond, notBond)//
		)//
		, message.getNormalizedBond());
    }

    /**
     * Tests with {@link DiscoveryMessage#BOND} = NOT(P1 AND P2) -> NOT(P1) OR NOT(P2)
     * Tests adding S1 -> S1 AND (NOT(P1) OR NOT(P2))
     * Tests adding S1, S2 -> (S1 AND (NOT(P1) OR NOT(P2))) OR (S2 AND (NOT(P1) OR NOT(P2)))
     * Tests adding S1, S2, S3 -> (S1 AND (NOT(P1) OR NOT(P2))) OR (S2 AND (NOT(P1) OR NOT(P2))) OR (S3 AND (NOT(P1) OR
     * NOT(P2)))
     */
    @Test
    public void testDistributedNotAndBond() throws GSException {
	LogicalBond userBond = BondFactory.createNotBond( //
		BondFactory.createAndBond(property1Bond, property2Bond));
	message.setUserBond(userBond);
	LogicalBond composedNotBond = BondFactory.createOrBond( //
		BondFactory.createNotBond(property1Bond), //
		BondFactory.createNotBond(property2Bond)); //
	doTest();
	Assert.assertEquals(userBond, message.getPermittedBond());
	Assert.assertEquals(composedNotBond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet1);
	Assert.assertEquals(//
		BondFactory.createAndBond(source1Bond, userBond)//
		, message.getPermittedBond());
	Assert.assertEquals(//
		BondFactory.createAndBond(source1Bond, composedNotBond)//
		, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet12);
	Assert.assertEquals(BondFactory.createAndBond( //
		userBond, source12Bond), //
		message.getPermittedBond());
	Assert.assertEquals(//
		BondFactory.createOrBond( //
			BondFactory.createAndBond(source1Bond, composedNotBond), //
			BondFactory.createAndBond(source2Bond, composedNotBond)//
		)//
		, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet123);
	Assert.assertEquals(BondFactory.createAndBond( //
		userBond, source123Bond), //
		message.getPermittedBond());
	Assert.assertEquals(//
		BondFactory.createOrBond( //
			BondFactory.createAndBond(source1Bond, composedNotBond), //
			BondFactory.createAndBond(source2Bond, composedNotBond), //
			BondFactory.createAndBond(source3Bond, composedNotBond)//
		)//
		, message.getNormalizedBond());
    }

    /**
     * Tests with {@link DiscoveryMessage#BOND} = NOT(P1 OR P2) -> NOT(P1) AND NOT(P2)
     * Tests adding S1 -> S1 AND NOT(P1) AND NOT(P2)
     * Tests adding S1, S2 -> (S1 AND NOT(P1) AND NOT(P2)) OR (S2 AND NOT(P1) AND NOT(P2))
     * Tests adding S1, S2, S3 -> (S1 AND NOT(P1) AND NOT(P2)) OR (S2 AND NOT(P1) AND NOT(P2)) OR (S3 AND NOT(P1) AND
     * NOT(P2))
     */
    @Test
    public void testDistributedNotOrBond() throws GSException {
	LogicalBond userBond = BondFactory.createNotBond( //
		BondFactory.createOrBond(property1Bond, property2Bond));
	message.setUserBond(userBond);
	LogicalBond composedNotBond = BondFactory.createAndBond( //
		BondFactory.createNotBond(property1Bond), //
		BondFactory.createNotBond(property2Bond)); //
	doTest();
	Assert.assertEquals(userBond, message.getPermittedBond());
	Assert.assertEquals(composedNotBond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet1);
	Assert.assertEquals(//
		BondFactory.createAndBond(source1Bond, //
			userBond)//
		, message.getPermittedBond());
	Assert.assertEquals(//
		BondFactory.createAndBond(source1Bond, //
			BondFactory.createNotBond(property1Bond), //
			BondFactory.createNotBond(property2Bond) //
		)//
		, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet12);
	Assert.assertEquals(//
		BondFactory.createAndBond(source12Bond, //
			userBond)//
		, message.getPermittedBond());
	Assert.assertEquals(//
		BondFactory.createOrBond( //
			BondFactory.createAndBond(source1Bond, //
				BondFactory.createNotBond(property1Bond), //
				BondFactory.createNotBond(property2Bond) //
			), //
			BondFactory.createAndBond(source2Bond, //
				BondFactory.createNotBond(property1Bond), //
				BondFactory.createNotBond(property2Bond) //
			)//
		)//
		, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet123);
	Assert.assertEquals(BondFactory.createAndBond( //
		userBond, source123Bond), //
		message.getPermittedBond());
	Assert.assertEquals(//
		BondFactory.createOrBond( //
			BondFactory.createAndBond(source1Bond, //
				BondFactory.createNotBond(property1Bond), //
				BondFactory.createNotBond(property2Bond) //
			), //
			BondFactory.createAndBond(source2Bond, //
				BondFactory.createNotBond(property1Bond), //
				BondFactory.createNotBond(property2Bond) //
			), //
			BondFactory.createAndBond(source3Bond, //
				BondFactory.createNotBond(property1Bond), //
				BondFactory.createNotBond(property2Bond) //
			)//
		)//
		, message.getNormalizedBond());
    }

    /**
     * Tests with {@link DiscoveryMessage#BOND} = P1 AND (S1 OR S2) -> (S1 AND P1) OR (S2 AND P1)
     * Tests adding S1 -> S1 AND P1
     * Tests adding S1, S2 -> (S1 AND P1) OR (S2 AND P1)
     * Tests adding S1, S2, S3 -> (S1 AND P1) OR (S2 AND P1)
     */
    @Test
    public void testComplexBond() throws GSException {
	LogicalBond userBond = BondFactory.createAndBond( //
		property1Bond, //
		BondFactory.createOrBond(source1Bond, source2Bond));
	message.setUserBond(userBond);
	doTest();
	Assert.assertEquals(userBond, //
		message.getPermittedBond());
	Assert.assertEquals(BondFactory.createOrBond( //
		s1p1Bond, //
		s2p1Bond), //
		message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet1);
	// Assert.assertEquals(BondFactory.createAndBond( //
	// userBond, source1Bond), //
	// message.getPermittedBond());
	Assert.assertEquals(s1p1Bond, //
		message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet12);
	// Assert.assertEquals(userBond, //
	// message.getPermittedBond());
	Assert.assertEquals(BondFactory.createOrBond( //
		s1p1Bond, //
		s2p1Bond), //
		message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet123);
	// Assert.assertEquals(userBond, //
	// message.getPermittedBond());
	Assert.assertEquals(BondFactory.createOrBond( //
		s1p1Bond, //
		s2p1Bond), //
		message.getNormalizedBond());
    }

    /**
     * Tests with {@link DiscoveryMessage#BOND} = (P1 OR P2) AND (S1 OR S2) -> (S1 AND (P1 OR P2)) OR (S2 AND (P1 OR
     * P2))
     * Tests adding S1 -> S1 AND (P1 OR P2)
     * Tests adding S1, S2 -> (S1 AND (P1 OR P2)) OR (S2 AND (P1 OR P2))
     * Tests adding S1, S2, S3 -> (S1 AND (P1 OR P2)) OR (S2 AND (P1 OR P2))
     */
    @Test
    public void testVeryComplexBond() throws GSException {
	LogicalBond userBond = BondFactory.createAndBond( //
		BondFactory.createOrBond(property1Bond, property2Bond), //
		BondFactory.createOrBond(source1Bond, source2Bond));
	message.setUserBond(userBond);
	doTest();
	// Assert.assertEquals(userBond, //
	// message.getPermittedBond());
	Assert.assertEquals(BondFactory.createOrBond( //
		BondFactory.createAndBond(source1Bond, //
			BondFactory.createOrBond(property1Bond, property2Bond)), //
		BondFactory.createAndBond(source2Bond, //
			BondFactory.createOrBond(property1Bond, property2Bond))), //
		message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet1);
	// Assert.assertEquals(BondFactory.createAndBond(userBond, //
	// source1Bond), message.getPermittedBond());
	Assert.assertEquals(BondFactory.createAndBond(source1Bond, //
		BondFactory.createOrBond(property1Bond, property2Bond)) //
		, //
		message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet12);
	// Assert.assertEquals(userBond, message.getPermittedBond());
	Assert.assertEquals(BondFactory.createOrBond( //
		BondFactory.createAndBond(source1Bond, //
			BondFactory.createOrBond(property1Bond, property2Bond)), //
		BondFactory.createAndBond(source2Bond, //
			BondFactory.createOrBond(property1Bond, property2Bond))), //
		message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet123);
	// Assert.assertEquals(userBond, //
	// message.getPermittedBond());
	Assert.assertEquals(BondFactory.createOrBond( //
		BondFactory.createAndBond(source1Bond, //
			BondFactory.createOrBond(property1Bond, property2Bond)), //
		BondFactory.createAndBond(source2Bond, //
			BondFactory.createOrBond(property1Bond, property2Bond))), //
		message.getNormalizedBond());
    }

    /**
     * Tests with {@link DiscoveryMessage#BOND} = ((P1 OR P2) AND (S1 OR S2)) OR P3 -> (S1 AND (P1 OR P2 OR P3)) OR
     * (S2 AND (P1 OR P2 OR P3))
     * Tests adding S1 -> (S1 AND (P1 OR P2 OR P3))
     * Tests adding S1, S2 -> (S1 AND (P1 OR P2 OR P3)) OR (S2 AND (P1 OR P2 OR P3))
     * Tests adding S1, S2, S3 -> (S1 AND (P1 OR P2 OR P3)) OR (S2 AND (P1 OR P2 OR P3)) OR (S3 AND P3)
     */
    @Test
    public void testVeryVeryComplexBond() throws GSException {
	LogicalBond userBond = BondFactory.createOrBond( //
		BondFactory.createAndBond( //
			BondFactory.createOrBond(property1Bond, property2Bond), //
			BondFactory.createOrBond(source1Bond, source2Bond)), //
		property3Bond);
	message.setUserBond(userBond);
	doTest();
	Assert.assertEquals(userBond, //
		message.getPermittedBond());
	Assert.assertEquals(BondFactory.createOrBond( //
		BondFactory.createAndBond(source1Bond, //
			BondFactory.createOrBond(BondFactory.createOrBond(property1Bond, property2Bond), property3Bond)), //
		BondFactory.createAndBond(source2Bond, //
			BondFactory.createOrBond(BondFactory.createOrBond(property1Bond, property2Bond), property3Bond)) //
	), //
		message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet1);
	Assert.assertEquals(BondFactory.createAndBond(userBond, //
		source1Bond), message.getPermittedBond());
	Assert.assertEquals(BondFactory.createAndBond(source1Bond, //
		BondFactory.createOrBond(BondFactory.createOrBond(property1Bond, property2Bond), property3Bond)), //
		message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet12);
	Assert.assertEquals(BondFactory.createAndBond( //
		userBond, source12Bond), //
		message.getPermittedBond());
	Assert.assertEquals(BondFactory.createOrBond( //
		BondFactory.createAndBond(source1Bond, //
			BondFactory.createOrBond(BondFactory.createOrBond(property1Bond, property2Bond), property3Bond)), //
		BondFactory.createAndBond(source2Bond, //
			BondFactory.createOrBond(BondFactory.createOrBond(property1Bond, property2Bond), property3Bond)) //
	), //
		message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet123);
	Assert.assertEquals(BondFactory.createAndBond( //
		userBond, source123Bond), //
		message.getPermittedBond());
	Assert.assertEquals(BondFactory.createOrBond( //
		BondFactory.createAndBond(source1Bond, //
			BondFactory.createOrBond(BondFactory.createOrBond(property1Bond, property2Bond), property3Bond)), //
		BondFactory.createAndBond(source2Bond, //
			BondFactory.createOrBond(BondFactory.createOrBond(property1Bond, property2Bond), property3Bond)), //
		BondFactory.createAndBond(source3Bond, property3Bond)), //
		message.getNormalizedBond());
    }

    /**
     * Tests with {@link DiscoveryMessage#BOND} = S1 OR (S2 AND P1) OR S4 -> S1 OR (S2 AND P1) OR S4
     * Tests adding S1 -> S1 
     * Tests adding S1, S2 -> S1 OR (S2 AND P1)
     * Tests adding S1, S2, S3 -> S1 OR (S2 AND P1)
     */
    @Test
    public void testSourcesSpeedupBond() throws GSException {
	LogicalBond userBond = BondFactory.createOrBond( //
		source1Bond, BondFactory.createAndBond( //
			property1Bond, source2Bond), //
		source4Bond);
	message.setUserBond(userBond);
	doTest();
	// Assert.assertEquals(userBond, //
	// message.getPermittedBond());
	Assert.assertEquals(userBond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet1);
	// Assert.assertEquals(BondFactory.createAndBond(userBond, //
	// source1Bond), message.getPermittedBond());
	Assert.assertEquals(source1Bond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet12);
	// Assert.assertEquals(BondFactory.createAndBond( //
	// userBond, source12Bond), //
	// message.getPermittedBond());
	Assert.assertEquals(BondFactory.createOrBond( //
		source1Bond, BondFactory.createAndBond( //
			property1Bond, source2Bond) //
	), //
		message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet123);
	// Assert.assertEquals(BondFactory.createAndBond( //
	// userBond, source123Bond), //
	// message.getPermittedBond());
	Assert.assertEquals(BondFactory.createOrBond( //
		source1Bond, BondFactory.createAndBond( //
			property1Bond, source2Bond) //
	), //
		message.getNormalizedBond());
    }
    
    /**
     * Tests with {@link DiscoveryMessage#BOND} = S2 AND P1 -> S2 AND P1
     * Tests adding S1 -> []
     * Tests adding S1, S2 -> S2 AND P1
     * Tests adding S1, S2, S3 -> S2 AND P1
     */
    @Test
    public void testConstrainedSourceBond() throws GSException {
	LogicalBond userBond = 
		BondFactory.createAndBond( //
			property1Bond, source2Bond) //
		;
	message.setUserBond(userBond);
	doTest();
	// Assert.assertEquals(userBond, //
	// message.getPermittedBond());
	Assert.assertEquals(userBond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet1);
	// Assert.assertEquals(BondFactory.createAndBond(userBond, //
	// source1Bond), message.getPermittedBond());
	Assert.assertEquals(new EmptyBond(),  message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet12);
	// Assert.assertEquals(BondFactory.createAndBond( //
	// userBond, source12Bond), //
	// message.getPermittedBond());
	Assert.assertEquals(userBond, message.getNormalizedBond());

	doTestWithAuthorizedSources(sourceSet123);
	// Assert.assertEquals(BondFactory.createAndBond( //
	// userBond, source123Bond), //
	// message.getPermittedBond());
	Assert.assertEquals(userBond, message.getNormalizedBond());
    }

}
