package eu.essi_lab.cfga.gs.setting.service;

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

import com.vaadin.flow.data.provider.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gui.components.grid.*;
import eu.essi_lab.cfga.gui.components.grid.renderer.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.cfga.gui.directive.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.services.*;

import java.util.*;

/**
 *
 * @author Fabrizio
 *
 */
public class ServicesTabDescriptor extends TabDescriptor {

    /**
     *
     */
    public ServicesTabDescriptor() {

	setLabel("Services");

	TabContentDescriptor descriptor = TabContentDescriptorBuilder.get(ManagedServiceSetting.class).//

		withShowDirective("Customizable services provided by an implementation of the 'ManagedService' interface. "
		+ "\n\nServices can run only on the local node, or they can be distributed on a multi-node cluster."
		+ "according to the 'Session coordinator' setting in the 'System' tab", SortDirection.ASCENDING).//

		withAddDirective(//
		"ADD",//
		"Add managed service", //
		ManagedServiceSetting.class.getCanonicalName()).//
		withRemoveDirective("REMOVE", "Remove managed service", true, ManagedServiceSetting.class.getCanonicalName()).//
		withEditDirective("EDIT", "Edit managed service", Directive.ConfirmationPolicy.ON_WARNINGS).//

		withGridInfo(Arrays.asList(//

		ColumnDescriptor.create("Id", true, true, this::getId), //

		ColumnDescriptor.create("Implementation", 400, true, true, this::getImpl), //

		ColumnDescriptor.create("Description", 400, true, true, this::getDesc),//

		ColumnDescriptor.create("Status", true, true, s -> ManagedServiceSupport.getInstance().getServiceStatus(s)),

		ColumnDescriptor.create("Host", true, true, s -> ManagedServiceSupport.getInstance().getServiceHost(s)),

		ColumnDescriptor.create("Messages", 70, s -> ManagedServiceSupport.getInstance().getServiceMessages(s), //
			ViewerColumnRenderer.create("Messages", "Messages", "View service messages"))

	)).//
		reloadable(() -> ManagedServiceSupport.getInstance().update()).//

		build();

	setIndex(GSTabIndex.SERVICES.getIndex());
	addContentDescriptor(descriptor);
    }

    /**
     * @param setting
     * @return
     */
    private String getDesc(Setting setting) {

	return setting.getObject().getJSONObject(ManagedServiceSetting.SERVICE_DESCRIPTION_OPTION_KEY).getJSONArray("values").getString(0);
    }

    /**
     * @param setting
     * @return
     */
    private String getImpl(Setting setting) {

	return setting.getObject().getJSONObject(ManagedServiceSetting.SERVICE_IMPL_OPTION_KEY).getJSONArray("values").getString(0);
    }

    /**
     * @param setting
     * @return
     */
    private String getId(Setting setting) {

	return setting.getObject().getJSONObject(ManagedServiceSetting.SERVICE_ID_OPTION_KEY).getJSONArray("values").getString(0);
    }
}
