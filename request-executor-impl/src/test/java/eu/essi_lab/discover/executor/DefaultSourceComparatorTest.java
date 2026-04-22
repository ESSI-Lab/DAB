package eu.essi_lab.discover.executor;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.request.executor.discover.DefaultSourceComparator;

public class DefaultSourceComparatorTest {

    private DefaultSourceComparator comparator;
    private List<GSSource> sources;

    @Before
    public void init() {
	this.comparator = new DefaultSourceComparator();
	this.sources = new ArrayList<>();
    }

    @Test
    public void testSort1() {
	initSources(getSource(BrokeringStrategy.HARVESTED, "My source 1", "id1"), //
		getSource(BrokeringStrategy.DISTRIBUTED, "My source 2", "id2"));
	sources.sort(comparator);
	assertOrder("id1", "id2");
    }

    @Test
    public void testSort2() {
	initSources(getSource(BrokeringStrategy.DISTRIBUTED, "My source 2", "id2"), //
		getSource(BrokeringStrategy.HARVESTED, "My source 1", "id1"));
	sources.sort(comparator);
	assertOrder("id1", "id2");
    }

    @Test
    public void testSort3() {
	initSources(getSource(BrokeringStrategy.HARVESTED, "My source 2", "id2"), //
		getSource(BrokeringStrategy.DISTRIBUTED, "My source 3", "id3"), //
		getSource(BrokeringStrategy.HARVESTED, "My source 1", "id1"));
	sources.sort(comparator);
	assertOrder("id1", "id2", "id3");
    }

    @Test
    public void testSort4() {
	initSources(getSource(BrokeringStrategy.HARVESTED, "My source 2", "id2"), //
		getSource(BrokeringStrategy.DISTRIBUTED, "My source 3", "id3"), //
		getSource(BrokeringStrategy.HARVESTED, "My source 1", "id1"), //
		getSource(null, "My source 4", "id4"));
	sources.sort(comparator);
	assertOrder("id1", "id2", "id3", "id4");
    }

    @Test
    public void testSort5() {
	initSources(getSource(BrokeringStrategy.HARVESTED, "My source 2", "id2"), //
		getSource(BrokeringStrategy.DISTRIBUTED, "My source 3", "id1"), //
		getSource(BrokeringStrategy.HARVESTED, "My source 1", "id3"));
	sources.sort(comparator);
	assertOrder("id3", "id2", "id1");
    }

    @Test
    public void testSort6() {
	initSources(

		getSource(BrokeringStrategy.DISTRIBUTED, "My source 5", "id5"), //
		getSource(BrokeringStrategy.HARVESTED, "My source 2", "id2"), //
		getSource(BrokeringStrategy.DISTRIBUTED, "My source 3", "id3"), //
		getSource(BrokeringStrategy.HARVESTED, "My source 1", "id1"), //
		getSource(null, "My source 4", "id4"));
	sources.sort(comparator);
	assertOrder("id1", "id2", "id3", "id5", "id4");
    }

    private void assertOrder(String... ids) {
	Assert.assertEquals("Bad defined test!", sources.size(), ids.length);
	for (int i = 0; i < ids.length; i++) {
	    Assert.assertEquals(ids[i], sources.get(i).getUniqueIdentifier());
	}

    }

    private void initSources(GSSource... sources) {
	this.sources.clear();
	for (GSSource source : sources) {
	    this.sources.add(source);
	}
    }

    private GSSource getSource(BrokeringStrategy strategy, String label, String id) {
	GSSource ret = new GSSource();
	ret.setBrokeringStrategy(strategy);
	ret.setLabel(label);
	ret.setUniqueIdentifier(id);
	return ret;
    }

}
