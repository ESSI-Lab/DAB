package eu.essi_lab.cfga;

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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cfga.ConfigurationChangeListener.ConfigurationChangeEvent;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.source.FileSource;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class Configuration {

    /**
     * @author Fabrizio
     */
    public enum State {

	/**
	 * The RW configuration is synchronized with the {@link ConfigurationSource}
	 */
	SYNCH,
	/**
	 * The RW configuration is not synchronized with the {@link ConfigurationSource}
	 */
	DIRTY
    }

    protected ConfigurationSource source;
    private List<Setting> list;
    private boolean dirty;
    private TimeUnit unit;
    private Integer interval;
    private boolean writable;
    private List<ConfigurationChangeListener> listenerList;
    private boolean autoreloadPaused;

    /**
     * Creates an empty, in-memory only configuration with no related source.<br>
     * This configuration <i>cannot be flushed</i>, use it only for test purpose
     */
    public Configuration() {

	this.list = new ArrayList<>(500);
	this.listenerList = new LinkedList<>();
    }

    /**
     * Creates an in-memory only configuration with no related source.<br>
     * 
     * @param list
     * @throws Exception
     */
    public Configuration(JSONArray array) throws Exception {

	this.list = new ArrayList<Setting>();
	array.forEach(obj -> this.list.add(new Setting((JSONObject) obj)));
	
 	this.listenerList = new ArrayList<>();
    }

    /**
     * @param configName
     * @throws Exception
     */
    public Configuration(String configName) throws Exception {

	this(new FileSource(configName));
    }

    /**
     * Set the autoreload with the given <code>interval</code> expressed in <code>unit</code> starting with a delay
     * equals to the interval
     * 
     * @param configName
     * @param unit
     * @param interval
     * @throws Exception
     */
    public Configuration(String configName, TimeUnit unit, int interval) throws Exception {

	this(new FileSource(configName), unit, interval);
    }

    /**
     * @param source
     * @throws Exception
     */
    public Configuration(ConfigurationSource source) throws Exception {

	this.source = source;
	this.list = source.list();
	this.listenerList = new ArrayList<>();
    }

    /**
     * Set the autoreload with the given <code>interval</code> expressed in <code>unit</code> starting with a delay
     * equals to the interval
     * 
     * @param source
     * @param unit
     * @param interval
     * @throws Exception
     */
    public Configuration(ConfigurationSource source, TimeUnit unit, int interval) throws Exception {

	this(source);

	autoreload(unit, interval);
    }

    /**
     * Set the autoreload with the given <code>interval</code> and <code>delay</delay> 
     * expressed in <code>unit</code>
     * 
     * @param unit
     * @param interval
     */
    public void autoreload(TimeUnit unit, int interval, int delay) {

	this.unit = unit;
	this.interval = interval;

	TimerTask timerTask = new TimerTask() {
	    @Override
	    public void run() {

		if (autoreloadPaused) {

		    return;
		}

		try {
		    reload();
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(Configuration.class).error(e.getMessage(), e);
		}
	    }
	};

	new Timer().schedule(//
		timerTask, //
		unit.toMillis(delay), //
		unit.toMillis(interval));
    }

    /**
     * Set the autoreload with the given <code>interval</code> expressed in <code>unit</code> starting with a delay
     * equals to the interval
     * 
     * @param unit
     * @param interval
     */
    public void autoreload(TimeUnit unit, int interval) {

	autoreload(unit, interval, interval);
    }

    /**
     * Forces configuration reloading by synchronizing with the source.<br>
     * If the configuration is {@link State#DIRTY} this method
     * fails and returns false
     * 
     * @return
     * @throws Exception
     */
    public synchronized boolean reload() throws Exception {

	if (getState() == State.SYNCH) {

	    list = source.list();

	    dispatchEvent(ConfigurationChangeEvent.CONFIGURATION_AUTO_RELOADED);

	    return true;
	}

	return false;
    }

    /**
     * 
     */
    public void pauseAutoreload() {

	this.autoreloadPaused = true;
    }

    /**
     * 
     */
    public void resumeAutoreload() {

	this.autoreloadPaused = false;
    }

    /**
     * @param listener
     */
    public void addChangeEventListener(ConfigurationChangeListener listener) {

	listenerList.add(listener);
    }

    /**
     * @return the listenerList
     */
    public List<ConfigurationChangeListener> getListenerList() {

	return listenerList;
    }

    /**
     * @param settingId
     * @return
     */
    public synchronized boolean exists(String settingId) {

	return SettingUtils.get(list, settingId).isPresent();
    }

    /**
     * @param setting
     * @return
     */
    public synchronized boolean contains(Setting setting) {

	Optional<Setting> optional = SettingUtils.get(list, setting.getIdentifier());

	return optional.get().equals(setting);
    }

    /**
     * Read-only method. Changes applied to the returned {@link Setting} will not alter
     * the configuration status. Use write methods to change the configuration status
     * 
     * @see #put(Setting)
     * @see #replace(Setting)
     * @see #remove(String)
     * @see #clear()
     * @param settingId
     */
    public synchronized Optional<Setting> get(String settingId) {

	return SettingUtils.get(list, settingId).map(s -> s.clone());
    }

    /**
     * Read-only method. Changes applied to the returned {@link Setting} will not alter
     * the configuration status. Use write methods to change the configuration status
     * 
     * @see #put(Setting)
     * @see #replace(Setting)
     * @see #remove(String)
     * @see #clear()
     * @param settingId
     * @param settingClass
     * @return
     * @throws RuntimeException
     */
    public synchronized <T extends Setting> Optional<T> get(String settingId, Class<T> settingClass) throws RuntimeException {

	return get(settingId, settingClass, true);
    }

    /**
     * Read-only method. Changes applied to the returned {@link Setting} will not alter
     * the configuration status. Use write methods to change the configuration status
     * 
     * @see #put(Setting)
     * @see #replace(Setting)
     * @see #remove(String)
     * @see #clear()
     * @param settingId
     * @param settingClass
     * @return
     * @throws RuntimeException
     */
    public synchronized <T extends Setting> Optional<T> get(String settingId, Class<T> settingClass, boolean exactClassMatch)
	    throws RuntimeException {

	Optional<T> findFirst = list(settingClass, exactClassMatch).//
		stream().//
		filter(s -> s.getIdentifier().equals(settingId)).//
		findFirst();

	return findFirst;
    }

    /**
     * Read-only method. Changes applied to the returned {@link Setting} will not alter
     * the configuration status. Use write methods to change the configuration status
     * 
     * @see #put(Setting)
     * @see #replace(Setting)
     * @see #remove(String)
     * @see #clear()
     * @return
     */
    public synchronized List<Setting> list() {

	if (writable) {

	    return list;
	}

	ArrayList<Setting> out = new ArrayList<Setting>();

	list.forEach(setting -> out.add(setting.clone()));

	return out;
    }

    /**
     * Read-only method. Changes applied to the returned {@link Setting} will not alter
     * the configuration status. Use write methods to change the configuration status.<br>
     * <br>
     * For performance reasons, using this list method instead of {@link #list()} with a possible stream is recommended
     * since the {@link #list()} method returns a clone of the <i>entire</i> list while this method creates a stream of
     * the original list and then clones only the resulting items
     *
     * @see #put(Setting)
     * @see #replace(Setting)
     * @see #remove(String)
     * @see #clear()
     * @param settingClass
     * @param exactClassMatch
     * @return
     */
    public synchronized <T extends Setting> List<T> list(Class<T> settingClass, boolean exactClassMatch) {

	return SettingUtils.list(list, settingClass, exactClassMatch, true);
    }

    /**
     * Read-only method. Changes applied to the returned {@link Setting} will not alter
     * the configuration status. Use write methods to change the configuration status.<br>
     * <br>
     * This is a special list implementation which allows to use a custom type of <code>mapper</code>.<br>
     * The resulting stream is filtered with {@link Objects#nonNull(Object)}, so if a mapping is not available,
     * the <code>mapper</code> should return a <code>null</code> value.<br>
     * <br>
     * For performance reasons, using this list method instead of {@link #list()} with a possible stream is recommended
     * since the {@link #list()} method returns a clone of the <i>entire</i> list while this method creates a stream of
     * the original list. It is responsibility of the given <code>mapper</code> to clone the the resulting items
     * 
     * @param settingClass
     * @param mapper
     * @return
     */
    public synchronized <T extends Setting> List<T> list(Class<T> settingClass, Function<Setting, T> mapper) {

	List<T> result = list.//

		stream().//

		map(mapper).//

		filter(Objects::nonNull).//
		collect(Collectors.toList());//

	return result;
    }

    /**
     * Read-only method. Changes applied to the returned {@link Setting} will not alter
     * the configuration status. Use write methods to change the configuration status.<br>
     * <br>
     * For performance reasons, using this list method instead of {@link #list()} with a possible stream is recommended
     * since the {@link #list()} method returns a clone of the <i>entire</i> list while this method creates a stream of
     * the original list and then clones only the resulting items
     *
     * @see #put(Setting)
     * @see #replace(Setting)
     * @see #remove(String)
     * @see #clear()
     * @param settingClass
     * @return
     */
    public synchronized <T extends Setting> List<T> list(Class<T> settingClass) {

	return list(settingClass, true);
    }

    /**
     * @param settingClass
     * @return
     */
    public synchronized int size(Class<?> settingClass) {

	return (int) list.//
		stream().//
		filter(s -> s.getObject().getString("settingClass").equals(settingClass.getName())).//
		count();
    }

    /**
     * Write method. If successfully invoked put the RW configuration in a {@link State#DIRTY} state.<br>
     * <br>
     * Put the provide <code>setting</code> in the configuration only if anothe {@link Setting} with same id
     * is not already in
     * 
     * @param setting
     */
    public synchronized boolean put(Setting setting) {

	if (exists(setting.getIdentifier())) {

	    return false;
	}

	this.list.add(setting.clone());
	this.dirty = true;

	dispatchEvent(setting, ConfigurationChangeEvent.SETTING_PUT);

	return true;
    }

    /**
     * Write method. If successfully invoked put the RW configuration in a {@link State#DIRTY} state.<br>
     * <br>
     * Replaces an existing setting according to the provided <code>setting</code> identifier,
     * with the provided <code>setting</code>. The method works only if a {@link Setting} with the
     * provided <code>setting</code> identifier and different content exists
     * 
     * @param setting
     * @return
     */
    public synchronized boolean replace(Setting setting) {

	if (!exists(setting.getIdentifier())) {

	    return false;
	}

	if (contains(setting)) {

	    return false;
	}

	remove(setting.getIdentifier(), false);

	this.list.add(setting.clone());
	this.dirty = true;

	dispatchEvent(setting, ConfigurationChangeEvent.SETTING_REPLACED);

	return true;
    }

    /**
     * Write method. If successfully invoked put the RW configuration in a {@link State#DIRTY} state.<br>
     * <br>
     * In order to be removed, a {@link Setting} with the provided <code>settingId</code> must be already in
     *
     * @param settingId
     */
    public synchronized boolean remove(String settingId) {

	return remove(settingId, true);
    }

    /**
     * Write method. If successfully invoked put the RW configuration in a {@link State#DIRTY} state.<br>
     * <br>
     * In order to be removed, a {@link Setting} with the provided <code>settingId</code> must be already in
     *
     * @param settingId
     * @param dispatchEvent
     */
    public synchronized boolean remove(String settingId, boolean dispatchEvent) {

	ArrayList<String> ids = new ArrayList<>();
	ids.add(settingId);

	return remove(ids, dispatchEvent);
    }

    /**
     * Write method. If successfully invoked put the RW configuration in a {@link State#DIRTY} state.<br>
     * <br>
     * In order to be removed, {@link Setting}s with the provided <code>settingIds</code> must be already in
     *
     * @param settingIds
     */
    public synchronized boolean remove(List<String> settingIds) {

	return remove(settingIds, true);
    }

    /**
     * Write method. If successfully invoked put the RW configuration in a {@link State#DIRTY} state.<br>
     * <br>
     * In order to be removed, {@link Setting}s with the provided <code>settingIds</code> must be already in
     *
     * @param settingIds
     * @param dispatchEvent
     */
    public synchronized boolean remove(List<String> settingIds, boolean dispatchEvent) {

	List<Setting> toRemove = settingIds.//
		stream().//
		map(id -> get(id)).//
		filter(opt -> opt.isPresent()).//
		map(opt -> opt.get()).//
		collect(Collectors.toList());

	if (!toRemove.isEmpty()) {

	    this.list.removeAll(toRemove);
	    this.dirty = true;

	    if (dispatchEvent) {
		dispatchEvent(toRemove, ConfigurationChangeEvent.SETTING_REMOVED);
	    }

	    return true;
	}

	return false;
    }

    /**
     * Write method. It put the RW configuration in a {@link State#DIRTY} state
     */
    public synchronized void clear() {

	this.list.clear();
	this.dirty = true;

	dispatchEvent(ConfigurationChangeEvent.CONFIGURATION_CLEARED);
    }

    /**
     * The state of a RW configuration changes from {@link State#SYNCH} to {@link State#DIRTY}
     * when one of the write methods is successfully invoked.<br>
     * The state of a RW configuration can be
     * reset to {@link State#SYNCH} invoking the {@link #flush()} method
     * 
     * @return
     * @throws Exception
     */
    public synchronized State getState() {

	return dirty ? State.DIRTY : State.SYNCH;
    }

    /**
     * Synchronized this configuration with its source and put the RW configuration in a {@link State#SYNCH} state
     * 
     * @throws Exception
     */
    public synchronized void flush() throws Exception {

	this.source.flush(this.list);
	this.dirty = false;

	dispatchEvent(ConfigurationChangeEvent.CONFIGURATION_FLUSHED);
    }

    /**
     * @return the unit
     */
    public Optional<TimeUnit> getAutoreloadTimeUnit() {

	return Optional.ofNullable(unit);
    }

    /**
     * @return the interval
     */
    public Optional<Integer> getAutoreloadInterval() {

	return Optional.ofNullable(interval);
    }

    /**
     * @return
     */
    public synchronized JSONArray toJSONArray() {

	JSONArray array = new JSONArray();
	list.forEach(setting -> array.put(new JSONObject(setting.getObject().toString())));
	return array;
    }

    @Override
    public synchronized String toString() {

	return toJSONArray().toString(3);
    }

    @Override
    public boolean equals(Object o) {

	if (o instanceof Configuration) {

	    Configuration otherConfig = ((Configuration) o);
	    List<Setting> otherList = otherConfig.list();
	    otherList.sort((s1, s2) -> s1.getIdentifier().compareTo(s2.getIdentifier()));

	    List<Setting> clonedList = list();
	    clonedList.sort((s1, s2) -> s1.getIdentifier().compareTo(s2.getIdentifier()));

	    return otherList.equals(clonedList);
	}

	return false;
    }

    /**
     * Returns a clone of this {@link Configuration} sharing the same source, a cloned list
     * of {@link Setting}, autoreload disabled, and no listeners
     */
    @Override
    public Configuration clone() {

	Configuration clone = new Configuration();
	// the list is cloned
	clone.list = list();
	clone.source = this.source;
	clone.dirty = this.dirty;

	//
	// these fields are left blank, autoreload and listeners are ignored
	//
	// clone.listenerList = this.listenerList;
	// clone.autoreloadPaused = this.autoreloadPaused;
	// clone.unit = this.unit;
	// clone.interval = this.interval;

	return clone;
    }

    /**
     * @return
     */
    public ConfigurationSource getSource() {

	return source;
    }

    /**
     * @param writable
     */
    void setWritable(boolean writable) {

	this.writable = writable;
    }

    /**
     * @param setting
     * @param event
     */
    private void dispatchEvent(List<Setting> settings, int event) {

	this.listenerList.forEach(l -> l.configurationChanged(//
		new ConfigurationChangeEvent(//
			this, //
			settings, //
			event)));
    }

    /**
     * @param setting
     * @param event
     */
    private void dispatchEvent(Setting setting, int event) {

	this.listenerList.forEach(l -> l.configurationChanged(//
		new ConfigurationChangeEvent(//
			this, //
			setting, //
			event)));
    }

    /**
     * @param event
     */
    private void dispatchEvent(int event) {

	this.listenerList.forEach(l -> l.configurationChanged(//
		new ConfigurationChangeEvent(//
			this, //
			event)));
    }

}
