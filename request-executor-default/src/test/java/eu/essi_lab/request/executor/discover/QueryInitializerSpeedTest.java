package eu.essi_lab.request.executor.discover;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;

/**
 * This test checks QueryInitializer speed, it should be fast to pass
 * 
 * @author boldrini
 */
public class QueryInitializerSpeedTest {

    @Test
    public void speedTest1() throws Exception {
	speedTest(QueryInitializerTestBond.getBond());
    }

    @Test
    public void speedTest2() throws Exception {

	List<Bond> bonds = new ArrayList<Bond>();
	for (int i = 0; i < 50; i++) {
	    bonds.add(BondFactory.createSourceIdentifierBond("s" + i));

	}
	LogicalBond sourcesBond = BondFactory.createOrBond(//
		bonds);

	List<Bond> themeBonds = new ArrayList<Bond>();

	for (int i = 0; i < 50; i++) {

	    Bond themeBond = BondFactory.createAndBond(createKeywordBond("" + i, 2), sourcesBond);
	    themeBonds.add(themeBond);
	}

	LogicalBond bond = BondFactory.createOrBond(BondFactory.createSourceIdentifierBond("chinageosatellite"),
		BondFactory.createAndBond(themeBonds));
	speedTest(bond);

    }

    private Bond createKeywordBond(String prefix, int i) {
	List<String> keywords = new ArrayList<String>();
	for (int j = 0; j < i; j++) {
	    keywords.add("k" + prefix + "-" + j + "-" + i);
	}
	return BondFactory.createKeywordListBond(keywords, LogicalOperator.AND);
    }

    public void speedTest(Bond bond) throws Exception {

	System.out.println("Speed test with bond:");
	System.out.println(bond.toString());

	QueryInitializer qi = new QueryInitializer();
	long start = System.currentTimeMillis();
	System.out.println("Normalization started");
	Bond normal = qi.normalizeBond(bond);
	System.out.println(normal.toString());
	long end = System.currentTimeMillis();
	System.out.println("Normalization ended");
	System.out.println(end - start);
	long s = (end - start) / 1000;
	System.out.println(s);

	assertTrue(s < 2);

    }
}
