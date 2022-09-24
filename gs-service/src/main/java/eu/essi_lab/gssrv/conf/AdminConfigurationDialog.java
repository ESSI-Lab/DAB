package eu.essi_lab.gssrv.conf;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting;
import eu.essi_lab.cfga.gui.components.ComponentFactory;
import eu.essi_lab.cfga.gui.components.setting.edit_put.SettingEditDialog;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.ValidationResponse.ValidationResult;
import eu.essi_lab.gssrv.starter.GISuiteStarter;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
class AdminConfigurationDialog extends SettingEditDialog {

    private String requestURL;

    /**
     * @param configuration
     * @param setting
     * @param requestURL
     */
    public AdminConfigurationDialog( //
	    Configuration configuration, //
	    Setting setting, //
	    String requestURL) {//

	super(configuration, setting, null, null);

	removeFromCloseAll();

	this.requestURL = requestURL;

	getCancelButton().getStyle().set("display", "none");

	setTitle("Configure administrator role");

	VerticalLayout contentLayout = getContentLayout();

	Label label = ComponentFactory
		.createLabel("In order to proceed, configure the administrator role and click 'Apply changes' button");
	label.getStyle().set("margin-left", "5px");
	label.getStyle().set("margin-top", "10px");
	label.getStyle().set("margin-bottom", "10px");
	label.getStyle().set("font-size", "18px");

	contentLayout.addComponentAtIndex(0, label);

	contentLayout.setHeight(400, Unit.PIXELS);
    }

    /**
     *  
     */
    public void close() {

	super.close();

	OAuthSetting setting = ConfigurationWrapper.getOAuthSetting();
	ValidationResponse response = setting.getValidator().get().validate(configuration, setting, ValidationContext.edit());

	if (response.getResult() == ValidationResult.VALIDATION_SUCCESSFUL) {

	    try {

		GISuiteStarter.configuration.flush();

		LoginDialog dialog = new LoginDialog(requestURL);

		dialog.open();

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	}
    }
}
