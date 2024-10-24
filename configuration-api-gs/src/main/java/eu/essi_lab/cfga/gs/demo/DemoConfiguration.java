package eu.essi_lab.cfga.gs.demo;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.concurrent.TimeUnit;

import eu.essi_lab.cfga.ConfigurableLoader;
import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationSource;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.DefaultConfiguration.MainSettingsIdentifier;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting.SystemSettingComponentInfo;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting.DatabaseComponentInfo;
import eu.essi_lab.cfga.source.FileSource;

/**
 * @author Fabrizio
 */
public class DemoConfiguration extends Configuration {

    /**
     * Creates an in-memory only configuration with no related source.<br>
     * This configuration <i>cannot be flushed</i>, use it only for test purpose
     */
    public DemoConfiguration() {

	init();
    }

    /**
     * Uses {@link FileSource#ConfigurationFilesSource(String)}
     * 
     * @param name
     * @throws Exception
     */
    public DemoConfiguration(String configName) throws Exception {

	super(configName);
	init();
    }

    /**
     * Uses {@link FileSource#ConfigurationFilesSource(String)}
     * 
     * @param configName
     * @param unit
     * @param interval
     * @throws Exception
     */
    public DemoConfiguration(String configName, TimeUnit unit, int interval) throws Exception {

	super(new FileSource(configName), unit, interval);
	init();
    }

    /**
     * @param source
     * @param unit
     * @param interval
     * @throws Exception
     */
    public DemoConfiguration(ConfigurationSource source) throws Exception {

	super(source);
	init();
    }

    /**
     * @param source
     * @param unit
     * @param interval
     * @throws Exception
     */
    public DemoConfiguration(ConfigurationSource source, TimeUnit unit, int interval) throws Exception {

	super(source, unit, interval);
	init();
    }

    /**
     * 
     */
    public void clean() {

	SelectionUtils.deepClean(this);
    }

    /**
     * 
     */
    private void init() {
	//
	// ---
	//
	DemoSetting1 demo1 = new DemoSetting1();

	put(demo1);

	//
	// ---
	//
	DemoSetting2 demo2_1 = new DemoSetting2();

	demo2_1.set_Radio_Radio_Mode();

	put(demo2_1);

	//
	// ---
	//
	DemoSetting2 demo2_2 = new DemoSetting2();

	demo2_2.set_Radio_Check_Mode();

	put(demo2_2);

	//
	// ---
	//
	DemoSetting2 demo2_3 = new DemoSetting2();

	demo2_3.set_Check_Check_Mode();

	put(demo2_3);

	//
	// ---
	//
	DemoSetting2 demo2_4 = new DemoSetting2();

	demo2_4.set_Check_Radio_Mode();

	put(demo2_4);

	//
	// ---
	//
	DemoSetting3 demo3 = new DemoSetting3();

	put(demo3);

	//
	// ---
	//

	for (int i = 0; i < 10; i++) {

	    DemoSetting4 demo4 = new DemoSetting4(i);

	    put(demo4);
	}

	//
	// ---
	//

	DemoSetting5 demo5 = new DemoSetting5();

	put(demo5);

	//
	// ---
	//

	DemoSetting6 demo6 = new DemoSetting6();

	put(demo6);

	//
	// ---
	//

	DemoSetting7 demo7 = new DemoSetting7();

	put(demo7);

	//
	// ---
	//

	DemoSetting8 demo8 = new DemoSetting8();

	put(demo8);
	// clean();

	//
	// --- Database - Volatile as default ---
	//

	DatabaseSetting databaseSetting = new DatabaseSetting();
	databaseSetting.setVolatile(true);
	databaseSetting.setIdentifier(MainSettingsIdentifier.DATABASE.getLabel());

	put(databaseSetting);

	//
	// --- System settings ----
	//

	SystemSetting systemSetting = ConfigurableLoader.load().//

		filter(c -> c.getSetting() instanceof SystemSetting).//
		map(c -> ((SystemSetting) c.getSetting())).//
		findFirst().//
		get();

	systemSetting.setIdentifier(MainSettingsIdentifier.SYSTEM_SETTINGS.getLabel());

	put(systemSetting);
    }
}
