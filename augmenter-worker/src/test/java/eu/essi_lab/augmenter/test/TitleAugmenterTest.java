package eu.essi_lab.augmenter.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.model.resource.Dataset;

public class TitleAugmenterTest {

    private ExampleTitleAugmenter augmenter1;
    private ExampleTitleAugmenter augmenter2;
    private ExampleTitleAugmenter augmenter3;
    private Dataset resource;

    @Before
    public void init() {
	this.augmenter1 = new ExampleTitleAugmenter("1");
	this.augmenter2 = new ExampleTitleAugmenter("2");
	this.augmenter3 = new ExampleTitleAugmenter("3");

	this.resource = new Dataset();
    }

    @Test
    public void test() throws Exception {
	augmenter1.augment(resource);
	assertTitle("1");
	augmenter2.augment(resource);
	assertTitle("12");
	augmenter3.augment(resource);
	assertTitle("123");
	augmenter2.augment(resource);
	assertTitle("1232");
	
    }

    private void assertTitle(String expectedTitle) {
	String title = resource.getHarmonizedMetadata().getCoreMetadata().getTitle();
	Assert.assertEquals(expectedTitle, title);

    }

}
