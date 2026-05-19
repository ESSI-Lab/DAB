package eu.essi_lab.cfga.gs.setting.database;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.cfga.option.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.cfga.setting.validation.*;
import eu.essi_lab.cfga.setting.validation.ValidationResponse.*;
import org.json.*;

import java.util.*;
import java.util.stream.*;

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
    private final static String SMART_STORAGE_KEY = "smartStorage";

    public SourceStorageSetting() {

	setName("Source storage settings");
	setDescription("Options for the final phase of the harvesting process");
	enableCompactMode(false);
	setCanBeDisabled(false);

	Option<String> smartStorageOption = StringOptionBuilder.get().//
		withLabel("Disable smart storage").//
		withDescription("Per default this feature is enabled for all the sources."
		+ " Enable this option and add source identifiers to disable this feature for the given sources."
		+ " Disable this option to enable the feature for all the sources (default)." + " Single source identifier per row").//
		withKey(SMART_STORAGE_KEY).//
		disabled().//
		withTextArea().//
		build();

	addOption(smartStorageOption);

	Option<String> markDeletedRecordsOption = StringOptionBuilder.get().//
		withLabel("Enable tagging of deleted records").//
		withDescription("Per default this feature is disabled for all the sources. "
		+ " Enable this option and add source identifiers to enable this feature for the given sources."
		+ " Disable this option to disable the feature for all the sources (default)." + " Single source identifier per row").//
		withKey(MARK_DELETED_RECORDS_KEY).//
		disabled().//
		withTextArea().//
		build();

	addOption(markDeletedRecordsOption);

	//
	// set the validator
	//
	setValidator(new SourceStorageValidator());
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
	    if (markOption.isEnabled() && (markOption.getOptionalValue().isEmpty() || markOption.getValue().isEmpty())) {

		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		validationResponse.getErrors().add("One or more source identifier must be set for the 'Mark deleted records' feature");
	    }

	    Option<String> smartOption = srcStrSetting.getOption(SMART_STORAGE_KEY, String.class).get();
	    if (smartOption.isEnabled() && (smartOption.getOptionalValue().isEmpty() || smartOption.getValue().isEmpty())) {

		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		validationResponse.getErrors().add("One or more source identifier must be set for the 'Disable smart storage' feature");
	    }

	    return validationResponse;
	}
    }

    /**
     * @author Fabrizio
     */
    public static class DescriptorProvider {

	private final TabContentDescriptor descriptor;

	/**
	 *
	 */
	public DescriptorProvider() {

	    descriptor = TabContentDescriptorBuilder.get(SourceStorageSetting.class).//
		    withLabel("Source storage").//
		    withEditDirective("EDIT", "Edit source storage settings").//
		    build();
	}

	/**
	 * @return
	 */
	public TabContentDescriptor get() {

	    return descriptor;
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

	option.setValue(option.getOptionalValue().orElse("") + String.join("\n", Arrays.asList(sourceIdentifiers)));
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

	    //
	    String newValue = Arrays.stream(optionalValue.get().split("\n")).//
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
    public void enableSmartStorage() {

	getOption(SMART_STORAGE_KEY, String.class).get().setEnabled(false);
    }

}
