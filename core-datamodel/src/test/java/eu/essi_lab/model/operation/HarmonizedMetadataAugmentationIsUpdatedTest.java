package eu.essi_lab.model.operation;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import eu.essi_lab.model.resource.AugmentedMetadataElement;
import eu.essi_lab.model.resource.HarmonizedMetadata;

public class HarmonizedMetadataAugmentationIsUpdatedTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private HarmonizedMetadata harmonizedMetadata;
    private HarmonizedMetadataOperation operation;

    @Before
    public void setUp() {
	operation = new HarmonizedMetadataAugmentationIsUpdated();
    }

    @Test
    public void checkNullHarmonizedMetadata() {
	harmonizedMetadata = null;
	expectedException.expect(NullPointerException.class);
	operation.perform(harmonizedMetadata);
	((HarmonizedMetadataAugmentationIsUpdated) operation).hasUpdatedAugmentedMetadata();
    }

    @Test
    public void checkNotAugmentedHarmonizedMetadata() {
	boolean expectedResult = false;
	harmonizedMetadata = Mockito.mock(HarmonizedMetadata.class);
	Mockito.when(harmonizedMetadata.getAugmentedMetadataElements()).thenReturn(new ArrayList<>());
	operation.perform(harmonizedMetadata);
	boolean actualResult = ((HarmonizedMetadataAugmentationIsUpdated) operation).hasUpdatedAugmentedMetadata();
	Assert.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void checkAugmentedHarmonizedMetadata() {
	boolean expectedResult = true;
	harmonizedMetadata = Mockito.mock(HarmonizedMetadata.class);
	List<AugmentedMetadataElement> augmentedElements = getAugmentedElements();
	Mockito.when(harmonizedMetadata.getAugmentedMetadataElements()).thenReturn(augmentedElements);
	operation.perform(harmonizedMetadata);
	boolean actualResult = ((HarmonizedMetadataAugmentationIsUpdated) operation).hasUpdatedAugmentedMetadata();
	Assert.assertEquals(expectedResult, actualResult);
    }

    private List<AugmentedMetadataElement> getAugmentedElements() {
	List<AugmentedMetadataElement> elements = new ArrayList<>();
	AugmentedMetadataElement augmentedElement1 = new AugmentedMetadataElement();
	augmentedElement1.setName("anyName");
	elements.add(augmentedElement1);
	AugmentedMetadataElement augmentedElement2 = new AugmentedMetadataElement();
	augmentedElement2.setName("anyName");
	elements.add(augmentedElement2);
	AugmentedMetadataElement augmentedElement3 = new AugmentedMetadataElement();
	augmentedElement3.setName("aDifferentName");
	elements.add(augmentedElement3);
	return elements;
    }
}
