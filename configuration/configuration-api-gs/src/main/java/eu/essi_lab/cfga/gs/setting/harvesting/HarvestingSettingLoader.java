package eu.essi_lab.cfga.gs.setting.harvesting;

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

import java.lang.reflect.Constructor;
import java.util.ServiceLoader;
import java.util.UUID;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * A loader for {@link HarvestingSetting} implementations.<br>
 * <br>
 * {@link HarvestingSetting} is a {@link Setting} template,
 * that is a {@link Setting} by which to select other settings, in this case {@link AccessorSetting}s and
 * {@link AugmenterSetting}s. The list of all the available {@link AccessorSetting}s and {@link AugmenterSetting}s is
 * long
 * and grows in the time. Because of this, this kind of setting when not <i>clean</i>, is quite huge and the loading
 * operation
 * requires some time. In order to improve performances, the {@link HarvestingSetting} implementation is loaded once in
 * a static clause and the {@link JSONObject} content is stored in memory.<br>
 * The {@link #load()} method creates a new instance of the concrete {@link HarvestingSetting} class by reusing the
 * {@link JSONObject} content loaded in the static clause, but with a new random identifier, and using the
 * {@link HarvestingSetting} constructor with the {@link String} parameter. This is much more efficient than always
 * loading
 * a new instance using <code>ServiceLoader.load(HarvestingSetting.class)</code>
 * 
 * @author Fabrizio
 */
public class HarvestingSettingLoader {

    private static final String content;
    private static final Class<? extends HarvestingSetting> clazz;

    static {

	ServiceLoader<HarvestingSetting> loader = ServiceLoader.load(HarvestingSetting.class);

	HarvestingSetting setting = loader.iterator().next();

	clazz = setting.getClass();
	content = setting.getObject().toString();
    }

    /**
     * Creates a new instance of the concrete {@link HarvestingSetting} class by reusing the {@link JSONObject} content
     * loaded in the static clause, but with a new random identifier
     * 
     * @return
     */
    public static HarvestingSetting load() {

	HarvestingSetting setting = null;

	try {

	    Constructor<? extends HarvestingSetting> constructor = clazz.getConstructor(String.class);
	    setting = constructor.newInstance(content);
	    setting.setIdentifier(UUID.randomUUID().toString());

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(HarvestingSettingLoader.class).error(ex.getMessage(), ex);
	}

	return setting;
    }

    /**
     * Creates a new instance of the concrete {@link HarvestingSetting} using the given <code>content</code>.<br>
     * The new instance is created using the
     * {@link HarvestingSetting#HarvestingSetting(JSONObject)}
     * and and empty {@link JSONObject}, which is the fastest way to create settings since all the work is
     * done in the empty constructor. When the instance is ready, a clone of the given <code>content</code> is set as
     * object
     *
     * @param content
     * @return
     */
    public static HarvestingSetting load(JSONObject content) {

	HarvestingSetting setting = null;

	try {

	    Constructor<? extends HarvestingSetting> constructor = clazz.getConstructor(JSONObject.class);
	    setting = constructor.newInstance(new JSONObject());
	    setting.setObject(new JSONObject(content.toString()));

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(HarvestingSettingLoader.class).error(ex.getMessage(), ex);
	}

	return setting;
    }
}
