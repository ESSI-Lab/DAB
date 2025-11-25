package eu.essi_lab.cfga.gs;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.cfga.check.scheme.SchemeItem;
import eu.essi_lab.cfga.gs.DefaultConfiguration.SingletonSettingsId;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.cfga.gs.setting.distribution.DistributionSetting;
import eu.essi_lab.cfga.gs.setting.driver.DriverSetting;
import eu.essi_lab.cfga.gs.setting.ontology.OntologySetting;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.gui.TabIndex;

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

/**
 * @author Fabrizio
 */
public enum GSTabIndex implements TabIndex {
    /**
     *
     */
    BROKERING(0),
    /**
     *
     */
    DATABASE(1, true, Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.DATABASE_SETTING.getLabel()))),
    /**
     *
     */
    SCHEDULER(2, true, Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.SCHEDULER_SETTING.getLabel()))),
    /**
     *
     */
    AUGMENTERS(3, true, Descriptor.of(s -> s.getSettingClass().getSimpleName().equals("AugmenterWorkerSettingImpl"))),
    /**
     *
     */
    REPOSITORY(4, true, Descriptor.of(s -> s.getSettingClass().getSuperclass().equals(DriverSetting.class))),
    /**
     *
     */
    AUTHORIZATION(5, true, Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.OAUTH_SETTING.getLabel()))),
    /**
     *
     */
    CREDENTIALS(6, true, Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.CREDENTIALS_SETTING.getLabel()))),
    /**
     *
     */
    DOWNLOAD(7, true, Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.DOWNLOAD_SETTING.getLabel()))),
    /**
     *
     */
    SOURCE_STORAGE(8, true, Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.SOURCE_STORAGE_SETTING.getLabel()))),
    /**
     *
     */
    IDENTIFIER_MANAGEMENT(9, true, Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.SOURCE_PRIORITY_SETTING.getLabel()))),
    /**
     *
     */
    GDC_SOURCES(10, true, Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.GDC_SOURCES_SETTING.getLabel()))),
    /**
     *
     */
    CUSTOM_TASKS(11, true, Descriptor.of(s -> s.getSettingClass().equals(CustomTaskSetting.class))),
    /**
     *
     */
    DATA_CACHE(12, true, Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.DATA_CACHE_CONNECTOR_SETTING.getLabel()))),
    /**
     *
     */
    RATE_LIMITER(13, true, Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.RATE_LIMITER_SETTING.getLabel()))),

    /**
     *
     */
    ONTOLOGIES(14, false, Descriptor.of(s -> s.getSettingClass().equals(OntologySetting.class))),
    /**
     *
     */
    CONFIGURATION_HANDLER(15),

    /**
     *
     */
    SYSTEM(16, true, Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.SYSTEM_SETTING.getLabel()))),
    /**
     *
     */
    ABOUT(17);

    private final int index;
    private final boolean required;
    private final List<Descriptor> descriptors;

    /**
     * @param index
     * @param required
     * @param descriptors
     */
    GSTabIndex(int index, boolean required, Descriptor... descriptors) {

	this.index = index;
	this.required = required;
	this.descriptors = Arrays.asList(descriptors);
    }

    /**
     * @param index
     */
    GSTabIndex(int index) {

	this.index = index;
	this.required = false;
	this.descriptors = List.of();
    }

    /**
     * @return the index
     */
    public int getIndex() {

	return index;
    }

    @Override
    public boolean required() {

	return this.required;
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
