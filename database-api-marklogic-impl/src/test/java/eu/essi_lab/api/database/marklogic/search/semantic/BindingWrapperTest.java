package eu.essi_lab.api.database.marklogic.search.semantic;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.ontology.JSONBindingWrapper;

/**
 * @author Fabrizio
 */
public class BindingWrapperTest {

    @Test
    public void singleLanBindingTest() throws IOException {

	String binding = IOStreamUtils
		.asUTF8String(BindingWrapperTest.class.getClassLoader().getResourceAsStream("single-lan-binding.json"));

	JSONBindingWrapper wrapper = new JSONBindingWrapper(binding);

	{
	    Optional<String> value = wrapper.readValue("ontology");
	    Optional<String> type = wrapper.readType("ontology");
	    Optional<String> language = wrapper.readLanguage("ontology");

	    Assert.assertEquals(1, wrapper.getElementsCount("ontology"));

	    Assert.assertEquals("http://eu.essi_lab.essi.core/test/unontology", value.get());
	    Assert.assertEquals("uri", type.get());
	    Assert.assertFalse(language.isPresent());
	}

	{
	    Optional<String> value = wrapper.readValue("ontology", 0);
	    Optional<String> type = wrapper.readType("ontology", 0);
	    Optional<String> language = wrapper.readLanguage("ontology", 0);

	    Assert.assertEquals("http://eu.essi_lab.essi.core/test/unontology", value.get());
	    Assert.assertEquals("uri", type.get());
	    Assert.assertFalse(language.isPresent());
	}

	{
	    Optional<String> value = wrapper.readValue("ontology", 1);
	    Optional<String> type = wrapper.readType("ontology", 2);
	    Optional<String> language = wrapper.readLanguage("ontology", 2);

	    Assert.assertFalse(value.isPresent());
	    Assert.assertFalse(type.isPresent());
	    Assert.assertFalse(language.isPresent());
	}

	{
	    Optional<String> value = wrapper.readValue("label");
	    Optional<String> type = wrapper.readType("label");
	    Optional<String> language = wrapper.readLanguage("label");

	    Assert.assertEquals(1, wrapper.getElementsCount("label"));

	    Assert.assertEquals("Target 1.2", value.get());
	    Assert.assertEquals("literal", type.get());
	    Assert.assertEquals("de", language.get());
	}

	{
	    Optional<String> value = wrapper.readValue("label", 0);
	    Optional<String> type = wrapper.readType("label", 0);
	    Optional<String> language = wrapper.readLanguage("label", 0);

	    Assert.assertEquals("Target 1.2", value.get());
	    Assert.assertEquals("literal", type.get());
	    Assert.assertEquals("de", language.get());
	}
    }

    @Test
    public void multiLabelBindingTest() throws IOException {

	String biding = IOStreamUtils.asUTF8String(BindingWrapperTest.class.getClassLoader().getResourceAsStream("multi-lan-binding.json"));

	JSONBindingWrapper wrapper = new JSONBindingWrapper(biding);

	{
	    Optional<String> value = wrapper.readValue("ontology");
	    Optional<String> type = wrapper.readType("ontology");
	    Optional<String> language = wrapper.readLanguage("ontology");

	    Assert.assertEquals(1, wrapper.getElementsCount("ontology"));

	    Assert.assertEquals("http://eu.essi_lab.essi.core/test/unontology", value.get());
	    Assert.assertEquals("uri", type.get());
	    Assert.assertFalse(language.isPresent());
	}

	{
	    Assert.assertEquals(2, wrapper.getElementsCount("label"));

	    Optional<String> value0 = wrapper.readValue("label", 0);
	    Optional<String> value1 = wrapper.readValue("label", 1);

	    Optional<String> type0 = wrapper.readType("label", 0);
	    Optional<String> type1 = wrapper.readType("label", 1);

	    Optional<String> language0 = wrapper.readLanguage("label", 0);
	    Optional<String> language1 = wrapper.readLanguage("label", 1);

	    Assert.assertEquals("German literal", value0.get());
	    Assert.assertEquals("English literal", value1.get());

	    Assert.assertEquals("literal", type0.get());
	    Assert.assertEquals("literal", type1.get());

	    Assert.assertEquals("de", language0.get());
	    Assert.assertEquals("en", language1.get());
	}
    }

    @Test
    public void bindingsReductionTest() throws IOException {

	String bindingsArray = IOStreamUtils.asUTF8String(BindingWrapperTest.class.getClassLoader().getResourceAsStream("bindings.json"));

	reduceByLabelAndAbstract(new JSONArray(bindingsArray));

	reduceByLabel(new JSONArray(bindingsArray));

	reduceByAbstract(new JSONArray(bindingsArray));
    }

    /**
     * The bindings array contains 5 binding elements, 1 distinct, the other are the same binding but with
     * labels and abstract in different languages: "it" or not specified.
     * So the reduction by label must return 2 biding elements, the distinct one and the other 4 reduced to 1 element
     * having 2 labels and 1 abstract. The language of the abstract is not predictable, it can be "it" or not specified
     */
    private void reduceByLabel(JSONArray array) {

	List<JSONBindingWrapper> reducedBindings = JSONBindingWrapper.reduceBindings("object", array, Arrays.asList("label"));

	Assert.assertEquals(2, reducedBindings.size());

	// sorting by object identifier
	reducedBindings.sort((w1, w2) -> w1.readValue("object").get().toString().compareTo(w2.readValue("object").get().toString()));

	{
	    //
	    // this is the distinct binding
	    //
	    JSONBindingWrapper wrapper = reducedBindings.get(0);

	    String value = wrapper.readValue("object").get();
	    Assert.assertEquals("http://eu.essi_lab.essi.core/test/target1", value);

	    Assert.assertEquals(1, wrapper.getElementsCount("label"));
	    Assert.assertEquals(1, wrapper.getElementsCount("abstract"));
	}

	{
	    //
	    // this is the reduced binding
	    //
	    JSONBindingWrapper wrapper = reducedBindings.get(1);

	    String value = wrapper.readValue("object").get();
	    Assert.assertEquals("http://eu.essi_lab.essi.core/test/target2", value);

	    // 2 labels
	    Assert.assertEquals(2, wrapper.getElementsCount("label"));
	    // 1 abstract
	    Assert.assertEquals(1, wrapper.getElementsCount("abstract"));

	    Optional<String> lan0 = wrapper.readLanguage("label", 0);
	    Optional<String> lan1 = wrapper.readLanguage("label", 1);

	    if (lan0.isPresent()) {

		Assert.assertEquals("it", lan0.get());
		Assert.assertFalse(lan1.isPresent());

	    } else if (lan1.isPresent()) {

		Assert.assertEquals("it", lan1.get());
		Assert.assertFalse(lan0.isPresent());
	    }

	    Optional<String> abs = wrapper.readLanguage("abstract");

	    // at the moment there is no way to know which abstract element has been
	    // selected, the one with the "it" language or the other without language.
	    //
	    // maybe a preference should be set ?
	    //
	    if (abs.isPresent()) {

		Assert.assertEquals("it", abs.get());
	    }
	}
    }

    /**
     * The bindings array contains 5 binding elements, 1 distinct, the other are the same binding but with
     * labels and abstract in different languages: "it" or not specified.
     * So the reduction by abstract must return 2 biding elements, the distinct one and the other 4 reduced to 1 element
     * having 1 labels and 2 abstract. The language of the label is not predictable, it can be "it" or not specified
     */
    private void reduceByAbstract(JSONArray array) {

	List<JSONBindingWrapper> reducedBindings = JSONBindingWrapper.reduceBindings("object", array, Arrays.asList("abstract"));

	Assert.assertEquals(2, reducedBindings.size());

	// sorting by object identifier
	reducedBindings.sort((w1, w2) -> w1.readValue("object").get().toString().compareTo(w2.readValue("object").get().toString()));

	{
	    //
	    // this is the distinct binding
	    //
	    JSONBindingWrapper wrapper = reducedBindings.get(0);

	    String value = wrapper.readValue("object").get();
	    Assert.assertEquals("http://eu.essi_lab.essi.core/test/target1", value);

	    Assert.assertEquals(1, wrapper.getElementsCount("label"));
	    Assert.assertEquals(1, wrapper.getElementsCount("abstract"));
	}

	{
	    //
	    // this is the reduced binding
	    //
	    JSONBindingWrapper wrapper = reducedBindings.get(1);

	    String value = wrapper.readValue("object").get();
	    Assert.assertEquals("http://eu.essi_lab.essi.core/test/target2", value);

	    // 1 labels
	    Assert.assertEquals(1, wrapper.getElementsCount("label"));
	    // 2 abstract
	    Assert.assertEquals(2, wrapper.getElementsCount("abstract"));

	    Optional<String> abs0 = wrapper.readLanguage("abstract", 0);
	    Optional<String> abs1 = wrapper.readLanguage("abstract", 1);

	    if (abs0.isPresent()) {

		Assert.assertEquals("it", abs0.get());
		Assert.assertFalse(abs1.isPresent());

	    } else if (abs1.isPresent()) {

		Assert.assertEquals("it", abs1.get());
		Assert.assertFalse(abs0.isPresent());
	    }

	    Optional<String> label = wrapper.readLanguage("label");

	    // at the moment there is no way to know which label element has been
	    // selected, the one with the "it" language or the other without language.
	    //
	    // maybe a preference should be set ?
	    //
	    if (label.isPresent()) {

		Assert.assertEquals("it", label.get());
	    }
	}
    }

    /**
     * The bindings array contains 5 binding elements, 1 distinct, the other are the same binding but with
     * labels and abstract in different languages: "it" or not specified.
     * So the reduction by label and abstract must return 2 biding elements, the distinct one and the other 4 reduced to
     * 1 element
     * having 2 labels and 2 abstract
     */
    private void reduceByLabelAndAbstract(JSONArray array) {

	//
	// reducing by label and abstract
	//
	List<JSONBindingWrapper> reducedBindings = JSONBindingWrapper.reduceBindings("object", array, Arrays.asList("label", "abstract"));

	Assert.assertEquals(2, reducedBindings.size());

	// sorting by object identifier
	reducedBindings.sort((w1, w2) -> w1.readValue("object").get().toString().compareTo(w2.readValue("object").get().toString()));

	{
	    //
	    // this is the distinct binding
	    //
	    JSONBindingWrapper wrapper = reducedBindings.get(0);

	    String value = wrapper.readValue("object").get();
	    Assert.assertEquals("http://eu.essi_lab.essi.core/test/target1", value);

	    Assert.assertEquals(1, wrapper.getElementsCount("label"));
	    Assert.assertEquals(1, wrapper.getElementsCount("abstract"));
	}

	{
	    //
	    // this is the reduced binding
	    //
	    JSONBindingWrapper wrapper = reducedBindings.get(1);

	    String value = wrapper.readValue("object").get();
	    Assert.assertEquals("http://eu.essi_lab.essi.core/test/target2", value);

	    // 2 labels
	    Assert.assertEquals(2, wrapper.getElementsCount("label"));
	    // 2 abstracts
	    Assert.assertEquals(2, wrapper.getElementsCount("abstract"));

	    Optional<String> lan0 = wrapper.readLanguage("label", 0);
	    Optional<String> lan1 = wrapper.readLanguage("label", 1);

	    if (lan0.isPresent()) {

		Assert.assertEquals("it", lan0.get());
		Assert.assertFalse(lan1.isPresent());

	    } else if (lan1.isPresent()) {

		Assert.assertEquals("it", lan1.get());
		Assert.assertFalse(lan0.isPresent());
	    }

	    Optional<String> abs0 = wrapper.readLanguage("abstract", 0);
	    Optional<String> abs1 = wrapper.readLanguage("abstract", 1);

	    if (abs0.isPresent()) {

		Assert.assertEquals("it", abs0.get());
		Assert.assertFalse(abs1.isPresent());

	    } else if (abs1.isPresent()) {

		Assert.assertEquals("it", abs1.get());
		Assert.assertFalse(abs0.isPresent());
	    }
	}

    }
}