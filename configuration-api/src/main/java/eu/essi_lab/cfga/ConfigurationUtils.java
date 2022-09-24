package eu.essi_lab.cfga;

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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class ConfigurationUtils {

    /**
     * @param configuration
     * @param mapper
     * @param mapped
     * @return
     */
    public static <T> Object deepMap(Configuration configuration, Function<Setting, T> mapper, List<T> mapped) {

	configuration.list().forEach(s -> SettingUtils.deepMap(s, mapper, mapped));

	return null;
    }

    /**
     * @param configuration
     * @param predicate
     * @param matches
     */
    public static void deepFind(Configuration configuration, Predicate<Setting> predicate, List<Setting> matches) {

	configuration.list().forEach(s -> SettingUtils.deepFind(s, predicate, matches));
    }

    /**
     * @param configuration
     * @param action
     */
    public static void deepPerform(Configuration configuration, Consumer<Setting> action) {

	configuration.list().forEach(s -> SettingUtils.deepPerform(s, action));
    }
}
