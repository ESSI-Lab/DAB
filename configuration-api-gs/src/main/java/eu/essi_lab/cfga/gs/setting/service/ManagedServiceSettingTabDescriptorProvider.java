package eu.essi_lab.cfga.gs.setting.service;

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
public class ManagedServiceSettingTabDescriptorProvider extends TabDescriptor {

    /**
     *
     */
    public ManagedServiceSettingTabDescriptorProvider() {

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

		ColumnDescriptor.create("Implementation", true, true, this::getImpl), //

		ColumnDescriptor.create("Description", true, true, this::getDesc),//

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
