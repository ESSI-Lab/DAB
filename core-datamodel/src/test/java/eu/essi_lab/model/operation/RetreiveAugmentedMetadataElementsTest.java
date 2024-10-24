package eu.essi_lab.model.operation;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.model.resource.AugmentedMetadataElement;
import eu.essi_lab.model.resource.HarmonizedMetadata;

public class RetreiveAugmentedMetadataElementsTest {

    private int expectedSize;
    private String elementName;
    private HarmonizedMetadata harmonizedMetadata;
    private HarmonizedMetadataOperation operation;

    @Before
    public void setUp() {
	elementName = "anyName";
	harmonizedMetadata = Mockito.mock(HarmonizedMetadata.class);
	operation = new RetreiveAugmentedMetadataElements();
    }

    @Test
    public void retreiveElementsByNonExistingName() {
	expectedSize = 0;
	Mockito.when(harmonizedMetadata.getAugmentedMetadataElements()).thenReturn(new ArrayList<>());
	operation.perform(harmonizedMetadata);
	List<AugmentedMetadataElement> augmentedElements = ((RetreiveAugmentedMetadataElements) operation).getElementsByName(elementName);
	int actualSize = augmentedElements.size();
	Assert.assertEquals(expectedSize, actualSize);
    }

    @Test
    public void retreiveElementsByExistingName() {
	expectedSize = 2;
	List<AugmentedMetadataElement> augmentedElementsInHarmonizedMetadata = getAugmentedMetadataElements();
	Mockito.when(harmonizedMetadata.getAugmentedMetadataElements()).thenReturn(augmentedElementsInHarmonizedMetadata);
	operation.perform(harmonizedMetadata);
	List<AugmentedMetadataElement> augmentedElements = ((RetreiveAugmentedMetadataElements) operation).getElementsByName(elementName);
	int actualSize = augmentedElements.size();
	Assert.assertEquals(expectedSize, actualSize);
    }

    private List<AugmentedMetadataElement> getAugmentedMetadataElements() {
	List<AugmentedMetadataElement> elements = new ArrayList<>();
	AugmentedMetadataElement firstElement = new AugmentedMetadataElement();
	firstElement.setName(elementName);
	AugmentedMetadataElement secondElement = new AugmentedMetadataElement();
	secondElement.setName(elementName);
	elements.add(firstElement);
	elements.add(secondElement);
	return elements;
    }
}
