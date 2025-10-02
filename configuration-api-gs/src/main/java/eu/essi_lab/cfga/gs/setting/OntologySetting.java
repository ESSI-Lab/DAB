/**
 * 
 */
package eu.essi_lab.cfga.gs.setting;

import org.joda.time.DateTimeZone;
import org.json.JSONObject;

import com.vaadin.flow.data.provider.SortDirection;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.GSTabIndex;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.cfga.gui.extension.directive.Directive.ConfirmationPolicy;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.Validator;

/**
 * @author Fabrizio
 */
public class OntologySetting extends Setting implements EditableSetting {

    /**
     * 
     */
    public OntologySetting() {

	setCanBeRemoved(true);
	setCanBeDisabled(false);
	setName("Ontology settings");

	setValidator(new OntologySettingValidator());

	//
	// set the component extension
	//
	setExtension(new OntologySettingComponentInfo());
    }

    /**
     * @author Fabrizio
     */
    public static class OntologySettingValidator implements Validator {

	@Override
	public ValidationResponse validate(Configuration configuration, Setting setting, ValidationContext context) {

	    OntologySetting ontSetting = (OntologySetting) SettingUtils.downCast(setting, setting.getSettingClass());

	    ValidationResponse validationResponse = new ValidationResponse();

	    return validationResponse;
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

	    setComponentName(AccessorSetting.class.getName());

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(GSTabIndex.DISTRIBUTION.getIndex()).//
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
