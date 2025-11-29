/**
 * 
 */
package eu.essi_lab.cfga.setting;

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

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.json.JSONException;

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class SettingUtils {

    /**
     * Creates a new instance of {@link Setting} with same original object (not cloned) and the class according
     * to the {@link Setting#getSettingClass()} of the provided <code>setting</code>.<br>
     * This is a shortcut for
     * <code>downCast(setting, setting.getSettingClass())</code> to be used when it is not necessary to handle the
     * specific interface of
     * {@link Setting#getSettingClass()}
     * 
     * @param setting
     * @return
     * @throws RuntimeException
     */
    public static Setting downCast(Setting setting) throws RuntimeException {

	try {

	    return downCast(setting, setting.getSettingClass(), false);

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(SettingUtils.class).error(ex.getMessage(), ex);
	    throw new RuntimeException(ex.getMessage(), ex);
	}
    }

    /**
     * Creates a new instance of {@link Setting} with same object (cloned or not according to <code>clone</code>) and
     * the class according to the {@link Setting#getSettingClass()} of the provided <code>setting</code>.<br>
     * This is a shortcut for
     * <code>downCast(setting, setting.getSettingClass())</code> to be used when it is not necessary to handle the
     * specific interface of
     * {@link Setting#getSettingClass()}
     * 
     * @param setting
     * @param clone
     * @return
     * @throws RuntimeException
     */
    public static Setting downCast(Setting setting, boolean clone) throws RuntimeException {

	try {

	    return downCast(setting, setting.getSettingClass(), clone);

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(SettingUtils.class).error(ex.getMessage(), ex);
	    throw new RuntimeException(ex.getMessage(), ex);
	}
    }

    /**
     * Creates a new instance of {@link Setting} having the class provided by <code>settingClass</code> and
     * as {@link Setting#getObject()} the original (<i>not cloned</i>) object provided by <code>setting</code>.<br>
     * The <code>settingClass</code> parameter must be equals to the given <code>setting</code>
     * {@link Setting#getSettingClass()}, it is used as type parameter
     * 
     * @param setting
     * @param settingClass
     * @return
     */
    public static <T extends Setting> T downCast(Setting setting, Class<T> settingClass) {

	return downCast(setting, settingClass, false);
    }

    /**
     * Creates a new instance of {@link Setting} having the class provided by <code>settingClass</code> and
     * as {@link Setting#getObject()} the object (cloned or not according to <code>clone</code>) provided by
     * <code>setting</code>.<br>
     * The <code>settingClass</code> parameter must be equals to the given <code>setting</code>
     * {@link Setting#getSettingClass()}, it is used as type parameter
     * 
     * @param setting
     * @param settingClass
     * @return
     */
    public static <T extends Setting> T downCast(Setting setting, Class<T> settingClass, boolean clone) {

	try {
	    T newInstance = create(settingClass);

	    newInstance.setObject(clone ? setting.clone().getObject() : setting.getObject());

	    return newInstance;

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(SettingUtils.class).error(e.getMessage(), e);
	    return null;
	}
    }

    /**
     * Creates a new {@link Setting} having the class provided by <code>settingClass</code>
     * 
     * @param settingClass
     * @return
     */
    public static <T extends Setting> T create(Class<T> settingClass) {

	try {
	    Constructor<?> constructor = settingClass.getConstructor();

	    @SuppressWarnings("unchecked")
	    T newInstance = (T) constructor.newInstance();

	    return newInstance;

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(SettingUtils.class).error(e.getMessage(), e);
	    return null;
	}
    }

    /**
     * @param list
     * @param settingId
     * @return
     */
    public static Optional<Setting> get(List<Setting> list, String settingId) {

	return list.//
		stream().//
		filter(setting -> setting.getIdentifier().equals(settingId)).//
		findFirst();
    }

    /**
     * @param list
     * @param settingClass
     * @param exactClassMatch
     * @param clone
     * @return
     */
    public static <T extends Setting> List<T> list(//
	    List<Setting> list, //
	    Class<T> settingClass, //
	    final boolean exactClassMatch) {

	return list(list, settingClass, exactClassMatch, false);
    }

    /**
     * @param list
     * @param settingClass
     * @param exactClassMatch
     * @param clone
     * @return
     */
    public static <T extends Setting> List<T> list(//
	    List<Setting> list, //
	    Class<T> settingClass, //
	    final boolean exactClassMatch, //
	    boolean clone) {

	@SuppressWarnings("unchecked")
	List<T> collect = list.//

		parallelStream().//

		map(s -> {

		    if (s.getObject().getString("settingClass").equals(settingClass.getName())) {

			return SettingUtils.downCast(s, settingClass, clone);
		    }

		    if (!exactClassMatch) {

			Class<?> target = null;
			try {
			    target = Class.forName(s.getObject().getString("settingClass"));
			    if (settingClass.isAssignableFrom(target)) {

				return SettingUtils.downCast(s, (Class<T>) target, clone);
			    }
			} catch (ClassNotFoundException | JSONException e) {
			    GSLoggerFactory.getLogger(SettingUtils.class).error(e.getMessage(), e);
			}
		    }

		    return null;
		}).//

		filter(Objects::nonNull).//
		collect(Collectors.toList());

	return collect;
    }

    /**
     * @param setting
     */
    public static void expand(Setting setting) {

	SettingUtils.deepPerform(setting, s -> s.enableCompactMode(false));
    }

    /**
     * @param setting
     */
    public static void collapse(Setting setting) {

	SettingUtils.deepPerform(setting, s -> s.enableCompactMode(true));
    }

    /**
     * @param setting
     * @param predicate
     * @param matches
     */
    public static void deepFind(Setting setting, Predicate<Setting> predicate, List<Setting> matches) {

	if (predicate.test(setting)) {

	    matches.add(setting);
	}

	setting.getSettings().forEach(s -> deepFind(s, predicate, matches));
    }

    /**
     * @param setting
     * @param predicate
     * @param mapped
     * @return
     */
    public static <T> void deepMap(Setting setting, Function<Setting, T> mapper, List<T> mapped) {

	T apply = mapper.apply(setting);

	if (apply != null) {

	    mapped.add(apply);
	}

	setting.getSettings().forEach(s -> deepMap(s, mapper, mapped));
    }

    /**
     * @param setting
     * @param action
     */
    public static void deepPerform(Setting setting, Consumer<Setting> action) {

	action.accept(setting);

	setting.getSettings().forEach(s -> deepPerform(s, action));
    }

    /**
     * @param option
     * @param input
     */
    public static <T> void loadValues(Option<T> option, Optional<String> input) {

	option.getLoader().ifPresent(loader -> {

	    Semaphore semaphore = new Semaphore(0);

	    loader = option.getLoader().get();
	    loader.load((val, exception) -> {

		option.setValues(val);

		semaphore.release();

	    }, input);

	    try {
		semaphore.acquire();
	    } catch (InterruptedException e) {
	    }
	});
    }
}
