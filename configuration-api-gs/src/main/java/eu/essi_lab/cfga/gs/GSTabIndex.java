package eu.essi_lab.cfga.gs;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.cfga.check.scheme.SchemeItem;
import eu.essi_lab.cfga.gs.DefaultConfiguration.SingletonSettingsId;
import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.cfga.gs.setting.database.*;
import eu.essi_lab.cfga.gs.setting.dc_connector.*;
import eu.essi_lab.cfga.gs.setting.distribution.DistributionSetting;
import eu.essi_lab.cfga.gs.setting.driver.*;
import eu.essi_lab.cfga.gs.setting.oauth.*;
import eu.essi_lab.cfga.gs.setting.ontology.*;
import eu.essi_lab.cfga.gs.setting.ratelimiter.*;
import eu.essi_lab.cfga.gs.setting.sessioncoordinator.*;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.gui.TabIndex;

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

/**
 * @author Fabrizio
 */
public enum GSTabIndex implements TabIndex {

    /**
     *
     */
    BROKERING(0, Descriptor.of(SourcePrioritySetting.class)),

    /**
     *
     */
    SOURCES(1, Descriptor.of(SourceStorageSetting.class), Descriptor.of(GDCSourcesSetting.class)),

    /**
     *
     */
    AUGMENTERS(2),

    /**
     *
     */
    CUSTOM_TASKS(3),

    /**
     *
     */
    AUTHORIZATION(4,
	    Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.OAUTH_SETTING.getLabel()), KeycloakProviderSetting::new)),
    /**
     *
     */
    CREDENTIALS(5, Descriptor.of(CredentialsSetting.class)),
    /**
     *
     */
    DATA_CACHE(6, Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.DATA_CACHE_CONNECTOR_SETTING.getLabel()),
	    DataCacheConnectorSettingLoader::load)),

    /**
     *
     */
    ASYNC_DOWNLOADS(7),

    /**
     *
     */
    VIEWS(8),
    /**
     *
     */
    SEMANTICS(9, Descriptor.of(DefaultSemanticSearchSetting.class)), //

    /**
     *
     */
    SYSTEM(10,//
	    Descriptor.of(SystemSetting.class),//
	    Descriptor.of(DatabaseSetting.class),//
	    Descriptor.of(SchedulerViewSetting.class),//
	    Descriptor.of(SharedCacheDriverSetting.class),//
	    Descriptor.of(SharedPersistentDriverSetting.class),//
	    Descriptor.of(DownloadSetting.class),//
	    Descriptor.of(RateLimiterSetting.class), //
	    Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.SESSION_COORDINATOR_SETTING.getLabel()),
		    () -> {
			SessionCoordinatorSetting setting = new SessionCoordinatorSetting();
			setting.setIdentifier(SingletonSettingsId.SESSION_COORDINATOR_SETTING.getLabel());
			return setting;
		    }) //
    ),

    /**
     *
     */
    ABOUT(11);

    private final int index;
    private final List<Descriptor> descriptors;

    /**
     * @param index
     * @param required
     * @param descriptors
     */
    GSTabIndex(int index, Descriptor... descriptors) {

	this.index = index;
	this.descriptors = Arrays.asList(descriptors);
    }

    /**
     * @param index
     */
    GSTabIndex(int index) {

	this.index = index;
	this.descriptors = List.of();
    }

    /**
     * @return the index
     */
    public int getIndex() {

	return index;
    }

    @Override
    public List<Descriptor> getDescriptors() {

	return descriptors;
    }

    /**
     * @return
     */
    public static List<SchemeItem> getItems() {

	return Arrays.stream(values()).collect(Collectors.toList());
    }

    /**
     * @return
     */
    public static List<TabIndex> getValues() {

	return Arrays.stream(values()).collect(Collectors.toList());
    }
}
