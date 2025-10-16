/**
 * 
 */
package eu.essi_lab.gssrv.conf;

import java.io.InputStream;
import java.util.Properties;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

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

import eu.essi_lab.cfga.gs.GSTabIndex;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * 
 * @author Fabrizio
 */
public class AboutComponentInfo extends ComponentInfo {

    /**
     * 
     */
    public AboutComponentInfo() {

	setComponentName("About");

	VerticalLayout verticalLayout = new VerticalLayout();
	verticalLayout.getStyle().set("margin-top", "15px");

	verticalLayout.setWidthFull();
	verticalLayout.setHeightFull();

	Properties buildProps = new Properties();
	try (InputStream is = getClass().getClassLoader().getResourceAsStream("META-INF/build.properties")) {
	    if (is != null) {
		buildProps.load(is);
	    }
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	String desc = buildProps.getProperty("project.description", "N/A");
	String name = buildProps.getProperty("project.name", "N/A");
	String groupId = buildProps.getProperty("project.groupId", "N/A");
	String artifactId = buildProps.getProperty("project.artifactId", "N/A");
	String version = buildProps.getProperty("project.version", "N/A");
	String timeStamp = buildProps.getProperty("build.timestamp", "N/A");
	String commit = buildProps.getProperty("git.commit.id", "N/A");

	verticalLayout.add(build("Description", desc));
	verticalLayout.add(build("Project Name", name));
	verticalLayout.add(build("Group Id", groupId));
	verticalLayout.add(build("Artifact Id", artifactId));
	verticalLayout.add(build("Version", version));
	verticalLayout.add(build("Build Timestamp", timeStamp));
	verticalLayout.add(build("Commit", commit));

	TabInfo tabInfo = TabInfoBuilder.get().//
		withIndex(GSTabIndex.ABOUT.getIndex()).//
		withShowDirective(getComponentName()).//
		withComponent(verticalLayout).//
		build();

	setTabInfo(tabInfo);
    }

    /**
     * @param title
     * @param content
     * @return
     */
    private HorizontalLayout build(String title, String content) {

	HorizontalLayout layout = new HorizontalLayout();
	layout.setWidthFull();

	Label titleField = new Label();
	titleField.setText("• " + title + ": ");
	titleField.setWidth("165px");
	titleField.getStyle().set("font-weight", "bold");

	layout.add(titleField);

	Label contentField = new Label();
	contentField.setText(content);
	contentField.setWidthFull();
	layout.add(contentField);

	return layout;
    }
}
