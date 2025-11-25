/**
 *
 */
package eu.essi_lab.cfga.gs.setting.ontology;

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

import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.data.provider.SortDirection;
import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.GSTabIndex;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gui.components.grid.ColumnDescriptor;
import eu.essi_lab.cfga.gui.components.grid.GridMenuItemHandler;
import eu.essi_lab.cfga.gui.components.grid.menuitem.SettingsRemoveItemHandler;
import eu.essi_lab.cfga.gui.extension.*;
import eu.essi_lab.cfga.gui.extension.directive.Directive.ConfirmationPolicy;
import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.OptionBuilder;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.AfterCleanFunction;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.Validator;
import eu.essi_lab.lib.utils.LabeledEnum;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Fabrizio
 */
public class OntologySetting extends Setting implements EditableSetting {

    private static final String DATA_MODEL_OPTION_KEY = "ontologyDataModel";
    private static final String QUERY_LANG_OPTION_KEY = "ontologyQueryLanguage";
    private static final String ENDPOINT_OPTION_KEY = "ontologyEndpoint";
    private static final String NAME_OPTION_KEY = "ontologyName";
    private static final String ID_OPTION_KEY = "ontologyId";
    private static final String DESCRIPTION_OPTION_KEY = "ontologyDescription";
    private static final String AVAILABILITY_OPTION_KEY = "ontologyAvailability";

    /**
     * @author Fabrizio
     */
    public enum QueryLanguage implements LabeledEnum {

	/**
	 *
	 */
	SPARQL("SPARQL");

	private final String label;

	/**
	 * @param label
	 */
	QueryLanguage(String label) {

	    this.label = label;
	}

	@Override
	public String getLabel() {

	    return label;
	}

	@Override
	public String toString() {

	    return getLabel();
	}
    }

    /**
     * @author Fabrizio
     */
    public enum DataModel implements LabeledEnum {

	/**
	 *
	 */
	SKOS("SKOS");

	private final String label;

	/**
	 * @param label
	 */
	DataModel(String label) {

	    this.label = label;
	}

	@Override
	public String getLabel() {

	    return label;
	}

	@Override
	public String toString() {

	    return getLabel();
	}
    }

    /**
     * @author Fabrizio
     */
    public enum Availability implements LabeledEnum {

	/**
	 *
	 */
	ENABLED("Enabled"),
	/**
	 *
	 */
	DISABLED("Disabled");

	private final String label;

	/**
	 * @param label
	 */
	Availability(String label) {

	    this.label = label;
	}

	@Override
	public String getLabel() {

	    return label;
	}

	@Override
	public String toString() {

	    return getLabel();
	}
    }

    /**
     *
     */
    public OntologySetting() {

	setCanBeRemoved(true);
	setCanBeDisabled(false);
	setName("Ontology settings");
	enableFoldedMode(false);
	enableCompactMode(false);

	//
	//
	//

	Option<String> endpoint = StringOptionBuilder.get().//
		withKey(ENDPOINT_OPTION_KEY).//
		withLabel("Endpoint").//
		withDescription("The ontology endpoint").//
		cannotBeDisabled().//
		required().//
		build();

	addOption(endpoint);

	Option<String> id = StringOptionBuilder.get().//
		withKey(ID_OPTION_KEY).//
		withLabel("Id").//
		withInputPattern(InputPattern.ALPHANUMERIC_AND_UNDERSCORE).//
		withDescription("The ontology identifier. Only alphanumeric characters and underscore are accepted").//
		cannotBeDisabled().//
		required().//
		build();

	addOption(id);

	Option<String> name = StringOptionBuilder.get().//
		withKey(NAME_OPTION_KEY).//
		withLabel("Name").//
		withDescription("The ontology name").//
		cannotBeDisabled().//
		required().//
		build();

	addOption(name);

	Option<String> description = StringOptionBuilder.get().//
		withKey(DESCRIPTION_OPTION_KEY).//
		withLabel("Description").//
		withDescription("Optional ontology description").//
		cannotBeDisabled().//
		build();

	addOption(description);

	Option<DataModel> model = OptionBuilder.get(DataModel.class).//
		withKey(DATA_MODEL_OPTION_KEY).//
		withLabel("Data model").//
		withDescription("The ontology data model. At the moment only 'SKOS' is supported").//
		withSingleSelection().//
		withValues(LabeledEnum.values(DataModel.class)).//
		withSelectedValue(DataModel.SKOS).//
		cannotBeDisabled().//
		build();

	addOption(model);

	Option<QueryLanguage> queryLanguage = OptionBuilder.get(QueryLanguage.class).//
		withKey(QUERY_LANG_OPTION_KEY).//
		withLabel("Ontology query language").//
		withDescription("The query language used to query the ontology. At the moment only 'SPARQL' is supported").//
		withSingleSelection().//
		withValues(LabeledEnum.values(QueryLanguage.class)).//
		withSelectedValue(QueryLanguage.SPARQL).//
		cannotBeDisabled().//
		build();

	addOption(queryLanguage);

	Option<Availability> enabled = OptionBuilder.get(Availability.class).//
		withKey(AVAILABILITY_OPTION_KEY).//
		withLabel("Ontology availability").//
		withDescription("If enabled the ontology can be queried").//
		withSingleSelection().//
		withValues(LabeledEnum.values(Availability.class)).//
		withSelectedValue(Availability.ENABLED).//
		cannotBeDisabled().//
		build();

	addOption(enabled);

	//
	//
	//

	setExtension(new OntologySettingComponentInfo());
	setAfterCleanFunction(new OntologySettingAfterCleanFunction());

	//
	//
	//

	setValidator(new OntologySettingValidator());
    }

    /**
     * @author Fabrizio
     */
    public static class OntologySettingValidator implements Validator {

	@Override
	public ValidationResponse validate(Configuration configuration, Setting setting, ValidationContext context) {

	    ValidationResponse response = new ValidationResponse();

	    if (check(setting)) {

		response.getErrors().add("Please provide all the required fields");
		response.setResult(ValidationResponse.ValidationResult.VALIDATION_FAILED);
	    }

	    return response;
	}

	/**
	 * @param setting
	 * @return
	 */
	private boolean check(Setting setting) {

	    return setting.getOptions().//
		    stream().//
		    anyMatch(o -> o.isRequired() && o.getOptionalValue().isEmpty());
	}
    }

    /**
     * @param endpoint
     */
    public void setOntologyAvailability(Availability av) {

	getOption(AVAILABILITY_OPTION_KEY, Availability.class).get().select(v -> v.equals(av));
    }

    /**
     * @return
     */
    public Availability getOntologyAvailability() {

	return getOption(AVAILABILITY_OPTION_KEY, Availability.class).get().getSelectedValue();
    }

    /**
     * @param desc
     */
    public void setOntologyDescription(String desc) {

	getOption(DESCRIPTION_OPTION_KEY, String.class).get().setValue(desc);
    }

    /**
     * @return
     */
    public Optional<String> getOntologyDescription() {

	return getOption(DESCRIPTION_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param endpoint
     */
    public void setOntologyEndpoint(String endpoint) {

	getOption(ENDPOINT_OPTION_KEY, String.class).get().setValue(endpoint);
    }

    /**
     * @return
     */
    public String getOntologyEndpoint() {

	return getOption(ENDPOINT_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @param id
     */
    public void setOntologyId(String id) {

	getOption(ID_OPTION_KEY, String.class).get().setValue(id);
    }

    /**
     * @return
     */
    public String getOntologyId() {

	return getOption(ID_OPTION_KEY, String.class).get().getValue();
    }

    /**
     *
     */
    public void setOntologyName(String name) {

	getOption(NAME_OPTION_KEY, String.class).get().setValue(name);
    }

    /**
     * @return
     */
    public String getOntologyName() {

	return getOption(NAME_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @param queryLanguage
     */
    public void setQueryLanguage(QueryLanguage queryLanguage) {

	getOption(QUERY_LANG_OPTION_KEY, QueryLanguage.class).get().select(v -> v.equals(queryLanguage));
    }

    /**
     * @return
     */
    public QueryLanguage getQueryLanguage() {

	return getOption(QUERY_LANG_OPTION_KEY, QueryLanguage.class).get().getSelectedValue();
    }

    /**
     * @param dataModel
     */
    public void setDataModel(DataModel dataModel) {

	getOption(DATA_MODEL_OPTION_KEY, DataModel.class).get().select(v -> v.equals(dataModel));
    }

    /**
     * @return
     */
    public DataModel getDataModel() {

	return getOption(DATA_MODEL_OPTION_KEY, DataModel.class).get().getSelectedValue();
    }

    /**
     * @author Fabrizio
     */
    public static class OntologySettingAfterCleanFunction implements AfterCleanFunction {

	@Override
	public void afterClean(Setting setting) {

	    OntologySetting thisSetting = SettingUtils.downCast(setting, OntologySetting.class);

	    thisSetting.setName(thisSetting.getOntologyName());
	}
    }

    /**
     * @author Fabrizio
     */
    public static class OntologySettingComponentInfo extends ComponentInfo {

	/**
	 *
	 */
	public OntologySettingComponentInfo() {

	    setName(AccessorSetting.class.getName());

	    TabDescriptor descriptor = TabDescriptorBuilder.get(OntologySetting.class).//
		    withLabel("Ontologies").//
 		    withShowDirective(SortDirection.ASCENDING).//
		    withAddDirective("Add ontology", OntologySetting.class).//
		    withRemoveDirective("Remove ontology", true, OntologySetting.class).//
		    withEditDirective("Edit ontology", ConfirmationPolicy.ON_WARNINGS).//
		    withGridInfo(Arrays.asList(//

		    ColumnDescriptor.createPositionalDescriptor(), //

		    ColumnDescriptor.create("Id", 300, true, true, this::getOntologyId), //

		    ColumnDescriptor.create("Endpoint", 500, true, true, this::getOntologyEndpoint), //

		    ColumnDescriptor.create("Name", 500, true, true, this::getOntologyName), //

		    ColumnDescriptor.create("Description", true, true, this::getOntologyDescription), //

		    ColumnDescriptor.create("Query language", 150, true, true, this::getQueryLanguage), //

		    ColumnDescriptor.create("Data model", 100, true, true, this::getDataModel), //

		    ColumnDescriptor.create("Availability", 100, true, true, this::getOntologyAvailability) //

	    ), getItemsList(), Grid.SelectionMode.MULTI).

		    build();

	    setPlaceholder(TabPlaceholder.of(GSTabIndex.ONTOLOGIES.getIndex(), descriptor));
	}

	/**
	 * @return
	 */
	private List<GridMenuItemHandler> getItemsList() {

	    ArrayList<GridMenuItemHandler> list = new ArrayList<>();

	    list.add(new SettingsRemoveItemHandler(true, true));
	    list.add(new OntologyDisableItemHandler());
	    list.add(new OntologyEnableItemHandler(true, false));

	    return list;
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getOntologyId(Setting setting) {

	    return setting.getOption(ID_OPTION_KEY, String.class).get().getValue();
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getOntologyName(Setting setting) {

	    return setting.getOption(NAME_OPTION_KEY, String.class).get().getValue();
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getOntologyEndpoint(Setting setting) {

	    return setting.getOption(ENDPOINT_OPTION_KEY, String.class).get().getValue();
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getOntologyDescription(Setting setting) {

	    return setting.getOption(DESCRIPTION_OPTION_KEY, String.class).get().getOptionalValue().orElse("");
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getOntologyAvailability(Setting setting) {

	    return setting.getOption(AVAILABILITY_OPTION_KEY, Availability.class).get().getSelectedValue().getLabel();
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getDataModel(Setting setting) {

	    return setting.getOption(DATA_MODEL_OPTION_KEY, DataModel.class).get().getSelectedValue().getLabel();
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getQueryLanguage(Setting setting) {

	    return setting.getOption(QUERY_LANG_OPTION_KEY, QueryLanguage.class).get().getSelectedValue().getLabel();
	}

    }

    /**
     * @param object
     */
    public OntologySetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public OntologySetting(String object) {

	super(object);
    }
}
