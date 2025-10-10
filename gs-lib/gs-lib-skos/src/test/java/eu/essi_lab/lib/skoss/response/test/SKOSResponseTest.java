/**
 * 
 */
package eu.essi_lab.lib.skoss.response.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skoss.SKOSConcept;
import eu.essi_lab.lib.skoss.SKOSResponse;

/**
 * @author Fabrizio
 */
public class SKOSResponseTest {

    @Test
    public void initTest() {

	List<SKOSConcept> results = new ArrayList<>();
	SKOSResponse response = SKOSResponse.of(results);

	Assert.assertTrue(response.getResults().isEmpty());
	Assert.assertTrue(response.getAggregatedResults().isEmpty());
	Assert.assertTrue(response.getAltLabels().isEmpty());
	Assert.assertTrue(response.getPrefLabels().isEmpty());
	Assert.assertTrue(response.getLabels().isEmpty());
    }

    @Test
    public void test() {

	List<SKOSConcept> results = new ArrayList<>();

	results.add(SKOSConcept.of("con_1", "pref_A", "exp_11", "exp_from_con_1a", "alt_A"));
	results.add(SKOSConcept.of("con_1", "pref_A", "exp_11", "exp_from_con_1a", "alt_A"));
	results.add(SKOSConcept.of("con_1", "pref_A", "exp_11", "exp_from_con_1b", "alt_A"));
	results.add(SKOSConcept.of("con_1", "pref_A", "exp_14", "exp_from_con_1b", "alt_D"));
	results.add(SKOSConcept.of("con_1", "pref_A", "exp_15", "exp_from_con_1b", "alt_E"));

	results.add(SKOSConcept.of("con_2", "pref_B", "exp_21", "exp_from_con_2", "alt_F"));
	results.add(SKOSConcept.of("con_2", "pref_B", "exp_22", "exp_from_con_2", "alt_G"));

	results.add(SKOSConcept.of("con_3", "pref_C", "exp_31", "exp_from_con_3", "alt_H"));
	results.add(SKOSConcept.of("con_3", "pref_C", "exp_31", "exp_from_con_3", "alt_H"));
	results.add(SKOSConcept.of("con_3", "pref_C", "exp_32", "exp_from_con_3", "alt_J"));

	SKOSResponse response = SKOSResponse.of(results);

	List<SKOSConcept> results2_ = response.getResults();

	Assert.assertEquals(results, results2_);

	Map<String, List<SKOSConcept>> conceptsMap = results.stream().collect(Collectors.groupingBy((c) -> c.getConcept()));

	//
	// pref test
	//

	List<String> prefLabels = response.getPrefLabels();

	List<String> con_1_pref = conceptsMap.get("con_1").stream().flatMap(c -> c.getPref().stream()).distinct().toList();
	List<String> con_2_pref = conceptsMap.get("con_2").stream().flatMap(c -> c.getPref().stream()).distinct().toList();
	List<String> con_3_pref = conceptsMap.get("con_3").stream().flatMap(c -> c.getPref().stream()).distinct().toList();

	Assert.assertEquals(1, con_1_pref.size());
	Assert.assertEquals(1, con_2_pref.size());
	Assert.assertEquals(1, con_3_pref.size());

	List<String> con_pref = Stream.of(con_1_pref, con_2_pref, con_3_pref).//
		flatMap(List::stream).sorted().toList();

	Assert.assertEquals(prefLabels.toString(), con_pref.toString());

	//
	// alt test
	//

	List<String> altLabels = response.getAltLabels();

	List<String> con_1_alt = conceptsMap.get("con_1").stream().flatMap(c -> c.getAlt().stream()).distinct().toList();
	List<String> con_2_alt = conceptsMap.get("con_2").stream().flatMap(c -> c.getAlt().stream()).distinct().toList();
	List<String> con_3_alt = conceptsMap.get("con_3").stream().flatMap(c -> c.getAlt().stream()).distinct().toList();

	Assert.assertEquals(3, con_1_alt.size());
	Assert.assertEquals(2, con_2_alt.size());
	Assert.assertEquals(2, con_3_alt.size());

	List<String> con_alt = Stream.of(con_1_alt, con_2_alt, con_3_alt).//
		flatMap(List::stream).distinct().sorted().toList();

	Assert.assertEquals(altLabels.toString(), con_alt.toString());

	//
	// all labels
	//

	List<String> labels = response.getLabels();

	List<String> labels_ = Stream.of(con_1_alt, con_2_alt, con_3_alt, con_1_pref, con_2_pref, con_3_pref).//
		flatMap(List::stream).distinct().sorted().toList();

	Assert.assertEquals(labels.toString(), labels_.toString());

	//
	// aggregation
	//

	List<SKOSConcept> aggregatedResults = response.getAggregatedResults();

	Assert.assertEquals(3, aggregatedResults.size());

	Assert.assertTrue(response.getAggregatedConcept("con_x").isEmpty());

	//
	// concept 1
	//

	SKOSConcept con_1_agg = response.getAggregatedConcept("con_1").get();

	Assert.assertEquals("con_1", con_1_agg.getConcept());

	Assert.assertEquals(//
		con_1_pref.get(0).toString(), //
		con_1_agg.getPref().get().toString());

	Assert.assertEquals(//
		con_1_alt.toString(), //
		con_1_agg.getAlt().stream().sorted().toList().toString());

	List<String> con_1_expanded = conceptsMap.get("con_1").stream().flatMap(c -> c.getExpanded().stream()).distinct().sorted().toList();

	Assert.assertEquals(con_1_expanded.toString(), con_1_agg.getExpanded().stream().sorted().toList().toString());

	List<String> con_1_expanded_from = conceptsMap.get("con_1").stream().flatMap(c -> c.getExpandedFrom().stream()).distinct().sorted()
		.toList();

	Assert.assertEquals(con_1_expanded_from.toString(), con_1_agg.getExpandedFrom().stream().sorted().toList().toString());

	//
	// concept 2
	//

	SKOSConcept con_2_agg = response.getAggregatedConcept("con_2").get();

	Assert.assertEquals("con_2", con_2_agg.getConcept());

	Assert.assertEquals(//
		con_2_pref.get(0).toString(), //
		con_2_agg.getPref().get().toString());

	Assert.assertEquals(//
		con_2_alt.toString(), //
		con_2_agg.getAlt().stream().sorted().toList().toString());

	List<String> con_2_expanded = conceptsMap.get("con_2").stream().flatMap(c -> c.getExpanded().stream()).distinct().sorted().toList();

	Assert.assertEquals(con_2_expanded.toString(), con_2_agg.getExpanded().stream().sorted().toList().toString());

	List<String> con_2_expanded_from = conceptsMap.get("con_2").stream().flatMap(c -> c.getExpandedFrom().stream()).distinct().sorted()
		.toList();

	Assert.assertEquals(con_2_expanded_from.toString(), con_2_agg.getExpandedFrom().stream().sorted().toList().toString());

	//
	// concept 3
	//

	SKOSConcept con_3_agg = response.getAggregatedConcept("con_3").get();

	Assert.assertEquals("con_3", con_3_agg.getConcept());

	Assert.assertEquals(//
		con_3_pref.get(0).toString(), //
		con_3_agg.getPref().get().toString());

	Assert.assertEquals(//
		con_3_alt.toString(), //
		con_3_agg.getAlt().stream().sorted().toList().toString());

	List<String> con_3_expanded = conceptsMap.get("con_3").stream().flatMap(c -> c.getExpanded().stream()).distinct().sorted().toList();

	Assert.assertEquals(con_3_expanded.toString(), con_3_agg.getExpanded().stream().sorted().toList().toString());

	List<String> con_3_expanded_from = conceptsMap.get("con_3").stream().flatMap(c -> c.getExpandedFrom().stream()).distinct().sorted()
		.toList();

	Assert.assertEquals(con_3_expanded_from.toString(), con_3_agg.getExpandedFrom().stream().sorted().toList().toString());

    }

}
