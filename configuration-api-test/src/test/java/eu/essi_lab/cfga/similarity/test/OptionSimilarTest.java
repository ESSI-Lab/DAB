package eu.essi_lab.cfga.similarity.test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Selectable.SelectionMode;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.OptionValueMapper;
import eu.essi_lab.cfga.option.ValuesLoader;

/**
 * 
 */
public class OptionSimilarTest {

    @Test
    public void optionSimilarTest1() {

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setValue("xxx");

	Option<String> option2 = new Option<String>(option1.toString());
	option2.setValue("yyy");

	boolean similar = option1.similar(option2);

	Assert.assertTrue(similar);
    }

    @Test
    public void optionSimilarTest1_1() {

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setValue("xxx");

	Option<String> option2 = new Option<String>(option1.toString());
	option2.setValue("xxx");

	boolean similar = option1.similar(option2);

	Assert.assertTrue(similar);
    }

    @Test
    public void optionSimilarTest2() {

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setValue("xxx");
	option1.setDescription("desc1");

	Option<String> option2 = new Option<String>(option1.toString());
	option2.setDescription("desc2");

	boolean similar = option1.similar(option2);

	Assert.assertTrue(similar);
    }

    @Test
    public void optionSimilarTest2_1() {

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setValue("xxx");
	option1.setDescription("desc1");

	Option<String> option2 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setValue("xxx");

	boolean similar = option1.similar(option2);

	Assert.assertTrue(similar);
    }

    @Test
    public void optionSimilarTest2_2() {

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setValue("xxx");

	Option<String> option2 = new Option<String>(String.class);
	option2.setKey("key1");

	boolean similar = option1.similar(option2);

	Assert.assertTrue(similar);
    }

    @Test
    public void optionSimilarTest3() {

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setDescription("desc1");

	Option<Integer> option2 = new Option<Integer>(Integer.class);
	option2.setKey("key1");
	option2.setDescription("desc1");

	boolean similar = option1.similar(option2);

	Assert.assertFalse(similar);
    }

    @Test
    public void optionSimilarTest4() {

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setValue("yyy");
	option1.setDescription("desc1");
	option1.setPosition(0);

	Option<String> option2 = new Option<String>(option1.toString());
	option2.setPosition(1);

	boolean similar = option1.similar(option2);

	Assert.assertTrue(similar);
    }

    @Test
    public void optionSimilarTest5() {

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setValue("xxx");
	option1.setDescription("desc1");
	option1.setPosition(1);

	Option<String> option2 = new Option<String>(String.class);
	option2.setKey("key2");
	option2.setValue("xxx");
	option2.setDescription("desc1");
	option2.setPosition(1);

	boolean similar = option1.similar(option2);

	Assert.assertTrue(similar);
    }

    @Test
    public void optionSimilarTest6() {

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setDescription("desc1");
	option1.setPosition(0);
	option1.setSelectionMode(SelectionMode.SINGLE);
	option1.setValues(Arrays.asList("a", "b", "c", "d", "e"));

	Option<String> option2 = new Option<String>(String.class);
	option2.setKey("key2");
	option2.setDescription("desc2");
	option2.setPosition(0);
	option2.setSelectionMode(SelectionMode.MULTI);
	option2.setValues(Arrays.asList("a", "b", "c", "d", "e"));

	boolean similar = option1.similar(option2);

	Assert.assertTrue(similar);
    }

    @Test
    public void optionSimilarTest6_1() {

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setDescription("desc1");
	option1.setPosition(0);
	option1.setSelectionMode(SelectionMode.SINGLE);
	option1.setValues(Arrays.asList("a", "b", "c", "d", "e"));

	Option<String> option2 = new Option<String>(option1.toString());
	option2.setSelectionMode(SelectionMode.MULTI);

	boolean similar = option1.similar(option2);

	Assert.assertTrue(similar);
    }

    @Test
    public void optionSimilarTest7() {

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setDescription("desc1");
	option1.setPosition(0);
	option1.setSelectionMode(SelectionMode.SINGLE);
	option1.setValues(Arrays.asList("a", "b", "c", "d", "e"));
	option1.select(v -> v.equals("a"));

	Option<String> option2 = new Option<String>(option1.toString());

	boolean similar = option1.similar(option2);

	Assert.assertTrue(similar);
    }

    @Test
    public void optionSimilarTest7_1() {

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setDescription("desc1");
	option1.setPosition(0);
	option1.setSelectionMode(SelectionMode.SINGLE);
	option1.setValues(Arrays.asList("a", "b", "c", "d", "e"));
	option1.select(v -> v.equals("a"));

	Option<String> option2 = new Option<String>(option1.toString());
	option2.select(v -> v.equals("b"));

	boolean similar = option1.similar(option2);

	Assert.assertTrue(similar);
    }

    @Test
    public void optionSimilarTest7_2() {

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setDescription("desc1");
	option1.setPosition(0);
	option1.setSelectionMode(SelectionMode.SINGLE);
	option1.setValues(Arrays.asList("a", "b", "c", "d", "e"));
	option1.select(v -> v.equals("a"));

	Option<String> option2 = new Option<String>(option1.toString());
	option2.setValues(Arrays.asList("a", "b"));
	option2.select(v -> v.equals("b"));

	boolean similar = option1.similar(option2);

	Assert.assertTrue(similar);
    }

    @Test
    public void optionSimilarTest7_3() {

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setDescription("desc1");
	option1.setPosition(0);
	option1.setSelectionMode(SelectionMode.SINGLE);
	option1.setValues(Arrays.asList("a", "b", "c", "d", "e"));
	option1.select(v -> v.equals("a"));
	option1.clean();

	Option<String> option2 = new Option<String>(option1.toString());
	option2.setSelectionMode(SelectionMode.SINGLE);
	option2.select(v -> v.equals("b"));
	option2.clean();

	boolean similar = option1.similar(option2);

	Assert.assertTrue(similar);
    }

    @Test
    public void optionSimilarTest8() {

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setValue("xxx");
	option1.setLoader(new ValuesLoader<String>() {
	    @Override
	    protected List<String> loadValues(Optional<String> input) throws Exception {

		return null;
	    }
	});

	Option<String> option2 = new Option<String>(option1.toString());
	option2.setLoader(new ValuesLoader<String>() {
	    @Override
	    protected List<String> loadValues(Optional<String> input) throws Exception {

		return null;
	    }
	});

	boolean similar = option1.similar(option2);

	Assert.assertTrue(similar);
    }

    @Test
    public void optionSimilarTest9() {

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setMapper(new OptionValueMapper<String>(null) {

	    @Override
	    public String asString(String value) {
		return null;
	    }

	    @Override
	    public String fromString(String value) {
		return null;
	    }
	});

	Option<String> option2 = new Option<String>(option1.toString());
	option2.setKey("key1");
	option2.setMapper(new OptionValueMapper<String>(null) {

	    @Override
	    public String asString(String value) {
		return null;
	    }

	    @Override
	    public String fromString(String value) {
		return null;
	    }
	});

	boolean similar = option1.similar(option2);

	Assert.assertTrue(similar);
    }
}
