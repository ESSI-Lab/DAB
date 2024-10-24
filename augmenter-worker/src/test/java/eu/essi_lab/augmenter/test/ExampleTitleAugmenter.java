package eu.essi_lab.augmenter.test;

import java.util.Optional;

import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.AugmentedMetadataElement;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * Augments the resource title with the specified suffix.
 * 
 * @author boldrini
 */
public class ExampleTitleAugmenter extends ResourceAugmenter<AugmenterSetting> {

    private String suffix;

    /**
     * Augments the resource title with the specified suffix.
     */
    public ExampleTitleAugmenter(String suffix) {
	this.suffix = suffix;
    }

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	// read old value
	String oldTitle = resource.getHarmonizedMetadata().getCoreMetadata().getTitle();

	// generate new value
	String newTitle = oldTitle;
	if (newTitle == null) {
	    newTitle = "";
	}
	newTitle += suffix;

	// write new value
	resource.getHarmonizedMetadata().getCoreMetadata().setTitle(newTitle);

	// record augmented metadata element
	AugmentedMetadataElement element = new AugmentedMetadataElement();
	element.setName(MetadataElement.TITLE);
	element.setOldValue(oldTitle);
	element.setNewValue(newTitle);
	element.setUpdateTimeStamp();
	element.setIdentifier(getClass().getCanonicalName());
	resource.getHarmonizedMetadata().getAugmentedMetadataElements().add(element);

	return Optional.of(resource);
    }

    @Override
    public String getType() {

	return "ExampleTitleAugmenter";
    }

    @Override
    protected String initName() {

	return "Example title augmenter";
    }

    @Override
    protected AugmenterSetting initSetting() {

	return new AugmenterSetting();
    }
}
