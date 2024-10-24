package eu.essi_lab.model.operation;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.resource.AugmentedMetadataElement;
import eu.essi_lab.model.resource.HarmonizedMetadata;

public class HarmonizedMetadataLastUpdateTimeTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private HarmonizedMetadata harmonizedMetadata;
    private HarmonizedMetadataOperation harmonizedMetadataLastUpdateTime;
    private Date expectedLastUpdateTime;

    @Before
    public void setUp() {
	harmonizedMetadataLastUpdateTime = new HarmonizedMetadataLastUpdateTime();
    }

    @Test
    public void retreiveLastUpdateTimeAboutNullHarmonizedMetadata() {
	harmonizedMetadata = null;
	expectedException.expect(NullPointerException.class);
	harmonizedMetadataLastUpdateTime.perform(harmonizedMetadata);
    }

    @Test
    public void retreiveLastUpdateTimeAboutBrandNewHarmonizedMetadata() throws ParseException {
	harmonizedMetadata = Mockito.mock(HarmonizedMetadata.class);
	Mockito.when(harmonizedMetadata.getAugmentedMetadataElements()).thenReturn(new ArrayList<>());
	harmonizedMetadataLastUpdateTime.perform(harmonizedMetadata);
	Date lastUpdateTime = ((HarmonizedMetadataLastUpdateTime) harmonizedMetadataLastUpdateTime).getLastUpdateTime();
	Assert.assertNull(lastUpdateTime);
    }

    @Test
    public void retreiveLastUpdateTimeAboutAlreadyUpdatedHarmonizedMetadata() throws ParseException {
	expectedLastUpdateTime = ISO8601DateTimeUtils.getISO8601DateTimeWithMillisecondsAsDate(2017, 1, 2, 0, 0, 0, 500);
	harmonizedMetadata = Mockito.mock(HarmonizedMetadata.class);
	List<AugmentedMetadataElement> augmentedElements = getAugmentedElements();
	Mockito.when(harmonizedMetadata.getAugmentedMetadataElements()).thenReturn(augmentedElements);
	harmonizedMetadataLastUpdateTime.perform(harmonizedMetadata);
	Date localLastUpdateTime = ((HarmonizedMetadataLastUpdateTime) harmonizedMetadataLastUpdateTime).getLastUpdateTime();
	Assert.assertEquals(expectedLastUpdateTime.getTime(), localLastUpdateTime.getTime());
    }

    private List<AugmentedMetadataElement> getAugmentedElements() {
	List<AugmentedMetadataElement> elements = new ArrayList<>();
	AugmentedMetadataElement element1 = new AugmentedMetadataElement();
	element1.setUpdateTimeStamp(ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(2017, 1, 1, 0, 0, 0, 0));
	elements.add(element1);
	AugmentedMetadataElement element2 = new AugmentedMetadataElement();
	element2.setUpdateTimeStamp(ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(2017, 1, 1, 0, 0, 0, 180));
	elements.add(element2);
	AugmentedMetadataElement element3 = new AugmentedMetadataElement();
	element3.setUpdateTimeStamp(ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(2017, 1, 2, 0, 0, 0, 0));
	elements.add(element3);
	AugmentedMetadataElement element4 = new AugmentedMetadataElement();
	element4.setUpdateTimeStamp(ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(expectedLastUpdateTime));
	elements.add(element4);
	return elements;
    }
}
