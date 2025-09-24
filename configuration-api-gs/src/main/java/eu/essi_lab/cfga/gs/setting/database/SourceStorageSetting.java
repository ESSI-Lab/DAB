package eu.essi_lab.cfga.gs.setting.database;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.GSTabIndex;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.ValidationResponse.ValidationResult;
import eu.essi_lab.cfga.setting.validation.Validator;

/**
 * @author Fabrizio
 */
public class SourceStorageSetting extends Setting implements EditableSetting {

    /**
     * 
     */
    private final static String MARK_DELETED_RECORDS_KEY = "markDeletedRecords";

    /**
     * 
     */
    private final static String TEST_ISO_COMPLIANCE_KEY = "testIsoCompliance";

    /**
     * 
     */
    private final static String RECOVER_TAGS_KEY = "recoverTags";

    /**
     * 
     */
    private final static String SMART_STORAGE_KEY = "smartStorage";

    public SourceStorageSetting() {

	setName("Source storage settings");
	setDescription("Set of options for the final phases of the harvesting process.\n"
		+ "To enable/disable a feature for a specific source, enable the related option and add its identifier.\n"
		+ "To enable/disable a feature for all the sources, enable/disable the related option");
	enableCompactMode(false);
	setCanBeDisabled(false);

	Option<String> markDeletedRecordsOption = StringOptionBuilder.get().//
		withLabel("Mark deleted records").//
		withDescription("Per default this feature is disabled for all the sources. "
			+ " Enable this option and add source identifiers to enable this feature for the given sources."
			+ " Disable this option to disable the feature for all the sources (default)."
			+ " Single source identifier per row")
		.//
		withKey(MARK_DELETED_RECORDS_KEY).//
		disabled().//
		withTextArea().//
		build();

	addOption(markDeletedRecordsOption);

	Option<String> isoComplianceTestOption = StringOptionBuilder.get().//
		withLabel("Test ISO compliance").//
		withDescription("Per default this feature is disabled for all the sources. "
			+ " Enable this option and add source identifiers to enable this feature for the given sources."
			+ " Disable this option to disable the feature for all the sources (default)."
			+ " Single source identifier per row")
		.//
		withKey(TEST_ISO_COMPLIANCE_KEY).//
		disabled().//
		withTextArea().//
		build();

	addOption(isoComplianceTestOption);

	Option<String> recoverResourceTagsOption = StringOptionBuilder.get().//
		withLabel("Recover resource tags").//
		withDescription("Per default this feature is disabled for all the sources. "
			+ " Enable this option and add source identifiers to enable this feature for the given sources."
			+ " Disable this option to disable the feature for all the sources (default)."
			+ " Single source identifier per row")
		.//
		withKey(RECOVER_TAGS_KEY).//
		disabled().//
		withTextArea().//
		build();

	addOption(recoverResourceTagsOption);

	Option<String> smartStorageDisabledOption = StringOptionBuilder.get().//
		withLabel("Disable smart storage").//
		withDescription("Per default this feature is enabled for all the sources."
			+ " Enable this option and add source identifiers to disable this feature for the given sources."
			+ " Disable this option to enable the feature for all the sources (default)." + " Single source identifier per row")
		.//
		withKey(SMART_STORAGE_KEY).//
		disabled().//
		withTextArea().//
		build();

	addOption(smartStorageDisabledOption);

	//
	// set the validator
	//
	setValidator(new SourceStorageValidator());

	//
	// set the rendering extension
	//
	setExtension(new SourceStorageSettingComponentInfo());
    }

    /**
     * @author Fabrizio
     */
    public static class SourceStorageValidator implements Validator {

	@Override
	public ValidationResponse validate(Configuration configuration, Setting setting, ValidationContext context) {

	    SourceStorageSetting srcStrSetting = (SourceStorageSetting) SettingUtils.downCast(setting, setting.getSettingClass());

	    ValidationResponse validationResponse = new ValidationResponse();

	    Option<String> markOption = srcStrSetting.getOption(MARK_DELETED_RECORDS_KEY, String.class).get();
	    if (markOption.isEnabled() && (!markOption.getOptionalValue().isPresent() || markOption.getValue().isEmpty())) {

		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		validationResponse.getErrors().add("One or more source identifier must be set for the 'Mark deleted records' feature");
	    }

	    Option<String> isoOption = srcStrSetting.getOption(TEST_ISO_COMPLIANCE_KEY, String.class).get();
	    if (isoOption.isEnabled() && (!isoOption.getOptionalValue().isPresent() || isoOption.getValue().isEmpty())) {

		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		validationResponse.getErrors().add("One or more source identifier must be set for the 'Test ISO compliance' feature");
	    }

	    Option<String> tagsOption = srcStrSetting.getOption(RECOVER_TAGS_KEY, String.class).get();
	    if (tagsOption.isEnabled() && (!tagsOption.getOptionalValue().isPresent() || tagsOption.getValue().isEmpty())) {

		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		validationResponse.getErrors().add("One or more source identifier must be set for the 'Recover resource tags' feature");
	    }

	    Option<String> smartOption = srcStrSetting.getOption(SMART_STORAGE_KEY, String.class).get();
	    if (smartOption.isEnabled() && (!smartOption.getOptionalValue().isPresent() || smartOption.getValue().isEmpty())) {

		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		validationResponse.getErrors().add("One or more source identifier must be set for the 'Disable smart storage' feature");
	    }

	    return validationResponse;
	}
    }

    /**
     * @author Fabrizio
     */
    public static class SourceStorageSettingComponentInfo extends ComponentInfo {

	/**
	 * 
	 */
	public SourceStorageSettingComponentInfo() {

	    setComponentName(SystemSetting.class.getName());

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(GSTabIndex.SOURCE_STORAGE.getIndex()).//
		    withShowDirective("Source storage").//
		    build();

	    setTabInfo(tabInfo);
	}
    }

    /**
     * @param object
     */
    public SourceStorageSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public SourceStorageSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public Boolean isMarkDeletedOption(String sourceIdentifier) {

	return test(MARK_DELETED_RECORDS_KEY, sourceIdentifier);
    }

    /**
     * @param set
     */
    public void setMarkDeleted(String... sourceIdentifiers) {

	addIdentifier(MARK_DELETED_RECORDS_KEY, sourceIdentifiers);
    }

    /**
     * @param sourceIdentifiers
     */
    public void removeMarkDeleted(String... sourceIdentifiers) {

	removeIdentifier(MARK_DELETED_RECORDS_KEY, sourceIdentifiers);
    }

    /**
     * @return
     */
    public Boolean isISOComplianceTestSet(String sourceIdentifier) {

	return test(TEST_ISO_COMPLIANCE_KEY, sourceIdentifier);
    }

    /**
     * @param set
     */
    public void setTestISOCompliance(String... sourceIdentifiers) {

	addIdentifier(TEST_ISO_COMPLIANCE_KEY, sourceIdentifiers);
    }

    /**
     * @param sourceIdentifiers
     */
    public void removeTestISOCompliance(String... sourceIdentifiers) {

	removeIdentifier(TEST_ISO_COMPLIANCE_KEY, sourceIdentifiers);
    }

    /**
     * @return
     */
    public Boolean isRecoverResourceTagsSet(String sourceIdentifier) {

	return test(RECOVER_TAGS_KEY, sourceIdentifier);
    }

    /**
     * @param set
     */
    public void setRecoverResourceTags(String... sourceIdentifiers) {

	addIdentifier(RECOVER_TAGS_KEY, sourceIdentifiers);
    }

    /**
     * @param sourceIdentifiers
     */
    public void removeRecoverResourceTags(String... sourceIdentifiers) {

	removeIdentifier(RECOVER_TAGS_KEY, sourceIdentifiers);
    }

    /**
     * @return
     */
    public Boolean isSmartStorageDisabledSet(String sourceIdentifier) {

	return test(SMART_STORAGE_KEY, sourceIdentifier);

    }

    /**
     * @param sourceIdentifiers
     */
    public void removeSmartStorageDisabledSet(String... sourceIdentifiers) {

	removeIdentifier(SMART_STORAGE_KEY, sourceIdentifiers);
    }

    /**
     * @param set
     */
    public void setDisableSmartStorage(String... sourceIdentifiers) {

	addIdentifier(SMART_STORAGE_KEY, sourceIdentifiers);
    }

    /**
     * @param optionKey
     * @param sourceIdentifiers
     */
    private void addIdentifier(String optionKey, String... sourceIdentifiers) {

	Option<String> option = getOption(optionKey, String.class).get();

	option.setEnabled(true);

	option.setValue(option.getOptionalValue().orElse("") + Arrays.asList(sourceIdentifiers).stream().collect(Collectors.joining("\n")));
    }

    /**
     * @param optionKey
     * @param sourceIdentifiers
     */
    private void removeIdentifier(String optionKey, String... sourceIdentifiers) {

	Option<String> option = getOption(optionKey, String.class).get();

	option.setEnabled(true);

	Optional<String> optionalValue = option.getOptionalValue();

	List<String> targetList = Arrays.asList(sourceIdentifiers);

	if (optionalValue.isPresent()) {

	    String newValue = Arrays.asList(optionalValue.get().split("\n")).//
		    stream().//
		    filter(id -> !targetList.contains(id)).//
		    collect(Collectors.joining("\n"));

	    option.setValue(newValue);
	}
    }

    /**
     * @param optionKey
     * @param sourceIdentifier
     * @return
     */
    private boolean test(String optionKey, String sourceIdentifier) {

	Option<String> option = getOption(optionKey, String.class).get();
	if (!option.isEnabled()) {

	    return false;
	}

	return option.getOptionalValue().orElse("").contains(sourceIdentifier);
    }

    /**
     * 
     */
    public void disableMarkDeleted() {

	getOption(MARK_DELETED_RECORDS_KEY, String.class).get().setEnabled(false);
    }

    /**
     * 
     */
    public void disableRecoverResourceTags() {

	getOption(RECOVER_TAGS_KEY, String.class).get().setEnabled(false);
    }

    /**
     * 
     */
    public void disableTestISOCompliance() {

	getOption(TEST_ISO_COMPLIANCE_KEY, String.class).get().setEnabled(false);
    }

    /**
     * 
     */
    public void enableSmartStorage() {

	getOption(SMART_STORAGE_KEY, String.class).get().setEnabled(false);
    }

}
