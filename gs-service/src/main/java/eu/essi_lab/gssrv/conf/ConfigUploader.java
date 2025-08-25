/**
 * 
 */
package eu.essi_lab.gssrv.conf;

import java.io.IOException;
import java.io.InputStream;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;

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

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.TabIndex;
import eu.essi_lab.cfga.gui.components.SettingComponentFactory;
import eu.essi_lab.cfga.gui.components.setting.SettingComponent;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.lib.utils.IOStreamUtils;

/**
 * @author Fabrizio
 */
public class ConfigUploader extends ComponentInfo {

    /**
     * 
     */
    public ConfigUploader() {

	setComponentName("Configuration uploader");

	VerticalLayout verticalLayout = new VerticalLayout();
	verticalLayout.getStyle().set("margin-top", "15px");

	verticalLayout.setWidthFull();
	verticalLayout.setHeightFull();

	//
	//
	//

	ConfigurationSourceSetting setting = new ConfigurationSourceSetting();

	SettingComponent settingComponent = SettingComponentFactory.createSettingComponent(//
		ConfigurationWrapper.getConfiguration().get(), setting, false);

	verticalLayout.add(settingComponent);

	//
	//
	//

	FileBuffer buffer = new FileBuffer();

	Upload upload = new Upload(buffer);
	
	upload.addFinishedListener(event -> {

	    InputStream configStream = buffer.getInputStream();
	    
	     

	});

	upload.setMaxFiles(1);
	upload.setDropAllowed(true);
	upload.setId("upload-button");

	Label uploadLabel = new Label("Upload configuration file");
	uploadLabel.getStyle().set("font-weight", "600");
	uploadLabel.setFor(upload.getId().get());

	Div uploadDiv = new Div(uploadLabel, upload);

	verticalLayout.add(uploadDiv);

	//
	//

	TabInfo tabInfo = TabInfoBuilder.get().//
		withIndex(TabIndex.CONFIG_UPLOADER.getIndex()).//
		withShowDirective(getComponentName()).//
		withComponent(verticalLayout).//
		build();

	setTabInfo(tabInfo);
    }

}
