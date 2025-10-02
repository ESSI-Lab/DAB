/**
 * 
 */
package eu.essi_lab.cfga.gs.setting;

import java.util.Arrays;
import java.util.Optional;

import org.json.JSONObject;

import com.vaadin.flow.data.provider.SortDirection;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.GSTabIndex;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.cfga.gui.extension.directive.Directive.ConfirmationPolicy;
import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;

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
		withLabel("Name").//
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
		withDescription("The ontology description").//
		cannotBeDisabled().//
		build();

	addOption(description);

	Option<String> model = StringOptionBuilder.get().//
		withKey(DATA_MODEL_OPTION_KEY).//
		withLabel("Data model").//
		withDescription("The ontology data model. At the moment only SKOS is supported").//
		withSingleSelection().//
		withValues(Arrays.asList("SKOS")).//
		withSelectedValue("SKOS").//
		cannotBeDisabled().//
		build();

	addOption(model);

	Option<String> queryLanguage = StringOptionBuilder.get().//
		withKey(QUERY_LANG_OPTION_KEY).//
		withLabel("Ontology query language").//
		withDescription("The query language used to query the ontology. At the moment only SPARQL is supported").//
		withSingleSelection().//
		withValues(Arrays.asList("SPARQL")).//
		withSelectedValue("SPARQL").//
		cannotBeDisabled().//
		build();

	addOption(queryLanguage);

	Option<String> enabled = StringOptionBuilder.get().//
		withKey(AVAILABILITY_OPTION_KEY).//
		withLabel("Ontology availability").//
		withDescription("If enabled the ontology can be queried").//
		withSingleSelection().//
		withValues(Arrays.asList("Enabled", "Disabled")).//
		withSelectedValue("Enabled").//
		cannotBeDisabled().//
		build();

	addOption(enabled);

	//
	//
	//

	setExtension(new OntologySettingComponentInfo());
    }

    /**
     * @param endpoint
     */
    public void setOntolgyEnabled(boolean enabled) {

	getOption(AVAILABILITY_OPTION_KEY, String.class).get().setValue(enabled ? "Enabled" : "Disabled");
    }

    /**
     * @return
     */
    public boolean isOntologyEnabled() {

	return getOption(AVAILABILITY_OPTION_KEY, String.class).get().getValue().equals("Enabled");
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
     * @author Fabrizio
     */
    public static class OntologySettingComponentInfo extends ComponentInfo {

	/**
	 * 
	 */
	public OntologySettingComponentInfo() {

	    setComponentName(AccessorSetting.class.getName());

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(GSTabIndex.ONTOLOGIES.getIndex()).//
		    withShowDirective("Ontologies", SortDirection.ASCENDING).//
		    withAddDirective("Add ontology", OntologySetting.class).//
		    withRemoveDirective("Remove ontology", true, OntologySetting.class).//
		    withEditDirective("Edit ontology", ConfirmationPolicy.ON_WARNINGS).//
		    build();

	    setTabInfo(tabInfo);
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
