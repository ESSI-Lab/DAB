package eu.essi_lab.cfga.gs.setting;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationUtils;
import eu.essi_lab.cfga.gs.GSSourcePattern;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.ValidationResponse.ValidationResult;
import eu.essi_lab.cfga.setting.validation.Validator;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.OrderingDirection;
import eu.essi_lab.model.ResultsPriority;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public class GSSourceSetting extends Setting {

    private static final String LABEL_OPTION_KEY = "label";
    private static final String IDENTIFIER_OPTION_KEY = "identifier";
    private static final String ENDPOINT_OPTION_KEY = "endpoint";
    private static final String COMMENT_OPTION_KEY = "sourceComment";
    private static final String BROKERING_STRATEGY_OPTION_KEY = "brokeringStrategy";
    private static final String DISCOVERY_OPTIONS_KEY = "discoveryOptions";

    public GSSourceSetting() {

	super();

	setCanBeDisabled(false);
	setEditable(false);
	enableCompactMode(false);

	setName("Source settings");

	setValidator(new GSSourceSettingValidator());

	{
	    Option<String> option = StringOptionBuilder.get().//
		    withKey(IDENTIFIER_OPTION_KEY).//
		    withLabel("Source identifier").//
		    required().//
		    withInputPattern(GSSourcePattern.GS_SOURCE_ID).//
		    cannotBeDisabled().//
		    build();

	    addOption(option);
	}

	{
	    Option<String> option = StringOptionBuilder.get().//
		    withKey(LABEL_OPTION_KEY).//
		    withLabel("Source label").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(option);
	}

	{
	    Option<String> option = StringOptionBuilder.get().//
		    withKey(ENDPOINT_OPTION_KEY).//
		    withLabel("Source endpoint").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(option);
	}

	{
	    Option<String> option = StringOptionBuilder.get().//
		    withKey(COMMENT_OPTION_KEY).//
		    withLabel("Source comment").//
		    withTextArea().//
		    cannotBeDisabled().//
		    build();

	    addOption(option);
	}

	{
	    String desc = "E.g: 'Collection\\ntitle\\nDescending', 'Collection', 'Collection\\ntitle'";

	    Option<String> option = StringOptionBuilder.get().//
		    withKey(DISCOVERY_OPTIONS_KEY).//
		    withLabel("Discovery options").//
		    withDescription(desc).//
		    withTextArea().//
		    cannotBeDisabled().//
		    build();

	    addOption(option);
	}
    }

    /**
     * @param source
     */
    public GSSourceSetting(GSSource source) {

	this();

	setSource(source);
    }

    /**
     * @param object
     */
    public GSSourceSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public GSSourceSetting(String object) {

	super(object);
    }

    /**
     * @author Fabrizio
     */
    public static class GSSourceSettingValidator implements Validator {

	@Override
	public ValidationResponse validate(Configuration configuration, Setting setting, ValidationContext context) {

	    GSSourceSetting thisSetting = SettingUtils.downCast(setting, GSSourceSetting.class);

	    ValidationResponse validationResponse = new ValidationResponse();

	    List<GSSource> sources = getAllSources(configuration);

	    String identifier = thisSetting.getSourceIdentifier();
	    if (identifier == null) {

		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		validationResponse.getErrors().add("Source identifier missing");
	    } else {

		boolean idExists = sources.stream().//
			filter(s -> s.getUniqueIdentifier() != null).//
			anyMatch(s -> s.getUniqueIdentifier().equals(identifier));

		switch (context.getContext()) {
		case ValidationContext.PUT:

		    if (idExists) {

			validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
			validationResponse.getErrors().add("A source with the given identifier already exists");
		    }

		    break;
		case ValidationContext.EDIT:

		    break;
		}
	    }

	    String label = thisSetting.getSourceLabel();
	    if (label == null) {

		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		validationResponse.getErrors().add("Source label missing");
	    } else {

		boolean labelExists = sources.stream().//
			filter(s -> s.getLabel() != null).//
			anyMatch(s -> s.getLabel().equals(label));

		switch (context.getContext()) {
		case ValidationContext.PUT:

		    if (labelExists) {

			validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
			validationResponse.getErrors().add("A source with the given label already exists");
		    }

		    break;
		case ValidationContext.EDIT:

		    break;
		}
	    }

	    String endpoint = thisSetting.getSourceEndpoint();
	    if (endpoint == null) {

		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		validationResponse.getErrors().add("Source endpoint missing");
	    }

	    return validationResponse;
	}

	/**
	 * @param configuration
	 * @return
	 */
	private List<GSSource> getAllSources(Configuration configuration) {

	    ArrayList<Setting> list = new ArrayList<>();

	    ConfigurationUtils.deepFind(configuration, s -> s.getSettingClass().equals(AccessorSetting.class), list);

	    return list.stream().map(s -> SettingUtils.downCast(s, AccessorSetting.class).getSource()).collect(Collectors.toList());

	}
    }

    /**
     * @param label
     */
    public void setSourceLabel(String label) {

	getOption(LABEL_OPTION_KEY, String.class).get().setValue(label);
    }

    /**
     * @return
     */
    public String getSourceLabel() {

	return getOption(LABEL_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @param comment
     */
    public void setSourceComment(String comment) {

	getOption(COMMENT_OPTION_KEY, String.class).get().setValue(comment);
    }

    /**
     * @return
     */
    public Optional<String> getSourceComment() {

	return getOption(COMMENT_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param id
     */
    public void setSourceIdentifier(String id) {

	getOption(IDENTIFIER_OPTION_KEY, String.class).get().setValue(id);
    }

    /**
     * @return
     */
    public String getSourceIdentifier() {

	return getOption(IDENTIFIER_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @param endpoint
     */
    public void setSourceEndpoint(String endpoint) {

	getOption(ENDPOINT_OPTION_KEY, String.class).get().setValue(endpoint);
    }

    /**
     * @return
     */
    public String getSourceEndpoint() {

	return getOption(ENDPOINT_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @param strategy
     */
    public void setBrokeringStrategy(BrokeringStrategy strategy) {

	getObject().put(BROKERING_STRATEGY_OPTION_KEY, strategy.getLabel());
    }

    /**
     * @return
     */
    public Optional<BrokeringStrategy> getBrokeringStrategy() {

	if (getObject().has(BROKERING_STRATEGY_OPTION_KEY)) {

	    return Optional.of(LabeledEnum.valueOf(//
		    BrokeringStrategy.class, //
		    getObject().getString(BROKERING_STRATEGY_OPTION_KEY)).get());
	}

	return Optional.empty();
    }

    /**
     * @param priority
     * @param orderingProperty
     * @param direction
     */
    public void setDiscoveryOptions(ResultsPriority priority, MetadataElement orderingProperty, OrderingDirection direction) {

	if (priority == null && orderingProperty == null && direction == null || priority == null && orderingProperty == null) {
	    getOption(DISCOVERY_OPTIONS_KEY, String.class).get().clearValues();
	    return;
	}

	String value = "";

	if (priority != null) {
	    value = priority.getLabel() + "\n";
	} else {
	    value = "-\n";
	}

	if (orderingProperty != null) {
	    value += orderingProperty.getName() + "\n";

	} else {
	    value += "-\n";
	}

	if (orderingProperty != null && direction != null) {
	    value += direction.getLabel();
	} else {
	    value += "-";
	}

	getOption(DISCOVERY_OPTIONS_KEY, String.class).get().setValue(value);
    }

    /**
     * * Collection
     * title
     * Ascending
     * 
     * @return
     */
    public ResultsPriority getResultsPriority() {

	List<String> options = getDiscoveryOptions();

	if (options.isEmpty() || options.get(0).equals("-")) {

	    return ResultsPriority.UNSET;
	}

	return LabeledEnum.valueOf(ResultsPriority.class, options.get(0)).get();
    }

    /**
     * @return
     */
    public Optional<MetadataElement> getOrderingProperty() {

	List<String> options = getDiscoveryOptions();

	if (options.isEmpty() || options.get(1).equals("-")) {

	    return Optional.empty();
	}

	return Optional.of(MetadataElement.fromName(options.get(1)));
    }

    /**
     * @return
     */
    public OrderingDirection getOrderingDirection() {

	List<String> options = getDiscoveryOptions();

	if (options.isEmpty() || options.get(2).equals("-")) {

	    return OrderingDirection.ASCENDING;
	}

	return LabeledEnum.valueOf(OrderingDirection.class, options.get(2)).get();
    }

    /**
     * Collection
     * title
     * Ascending
     * 
     * @return
     */
    private List<String> getDiscoveryOptions() {

	Optional<Option<String>> option = getOption(DISCOVERY_OPTIONS_KEY, String.class);

	if (option.isPresent()) {

	    Optional<String> options = option.get().getOptionalValue();

	    if (options.isPresent()) {

		return Arrays.asList(options.get().trim().split("\n"));
	    }
	}

	return new ArrayList<String>();
    }

    /**
     * @param source
     */
    public void setSource(GSSource source) {

	if (source == null) {

	    throw new IllegalArgumentException("Null source");
	}

	if (source.getUniqueIdentifier() != null) {

	    setSourceIdentifier(source.getUniqueIdentifier());
	}

	if (source.getBrokeringStrategy() != null) {
	    setBrokeringStrategy(source.getBrokeringStrategy());
	}

	if (source.getEndpoint() != null) {
	    setSourceEndpoint(source.getEndpoint());
	}

	if (source.getLabel() != null) {
	    setSourceLabel(source.getLabel());
	}

	MetadataElement ordProperty = source.getOrderingProperty() != null ? MetadataElement.fromName(source.getOrderingProperty()) : null;

	setDiscoveryOptions(source.getResultsPriority(), ordProperty, source.getOrderingDirection());
    }

    /***
     * @return
     */
    public GSSource asSource() {

	GSSource source = new GSSource();

	source.setBrokeringStrategy(getBrokeringStrategy().orElse(null));

	source.setEndpoint(getSourceEndpoint());

	source.setLabel(getSourceLabel());

	source.setUniqueIdentifier(getSourceIdentifier());

	Optional<MetadataElement> orderingProperty = getOrderingProperty();

	if (orderingProperty.isPresent()) {

	    source.setOrderingProperty(getOrderingProperty().get().getName());

	} else {

	    source.setOrderingProperty(null);
	}

	source.setOrderingDirection(getOrderingDirection());

	source.setResultsPriority(getResultsPriority());

	return source;
    }
}
