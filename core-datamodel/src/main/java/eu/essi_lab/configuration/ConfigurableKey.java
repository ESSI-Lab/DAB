package eu.essi_lab.configuration;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.List;

import eu.essi_lab.model.configuration.IGSConfigurable;
public class ConfigurableKey {

    public static final String SEPARATOR = "-->";
    boolean isRoot;

    boolean onRoot = true;

    private List<String> path = new ArrayList<>();
    private int alreadyReadLevels = 0;

    public ConfigurableKey() {

    }

    public ConfigurableKey(String keyString) {

	this();

	String[] parsed = keyString.split(SEPARATOR);

	int idx = 0;

	if (parsed.length > 0) {
	    if (parsed[0].equals("root")) {

		isRoot = true;

		setOnRoot();

		idx++;
	    } else {
		isRoot = false;
	    }

	    for (int i = idx; i < parsed.length; i++) {
		path.add(parsed[i]);

		isRoot = false;
	    }
	}

    }

    /**
     * Checks if this key identifies the root node
     *
     * @return
     */
    public boolean isRoot() {

	return isRoot && path.isEmpty();

    }

    public boolean oneLevelDown() {

	onRoot = false;

	if (alreadyReadLevels + 1 > path.size()) {
	    return false;
	}

	alreadyReadLevels++;

	return true;
    }

    /**
     * This compares the provided {@link IGSConfigurable} with the present node this key is on. If the configurable matches and is not the
     * root, the method returns true. To check the root node use the mothd {@link #isRoot()}.
     *
     * @param configurable
     * @return
     */
    public boolean match(IGSConfigurable configurable) {
	if (onRoot)
	    return false;

	return compareTo(configurable, alreadyReadLevels) && isLeaf(alreadyReadLevels);
    }

    private boolean compareTo(IGSConfigurable configurable, int idx) {

	return compareTo(unique(configurable), idx);
    }

    private boolean compareTo(String key, int idx) {

	if (path.size() < idx)
	    return false;

	return path.get(idx - 1).equalsIgnoreCase(key);
    }

    private boolean isLeaf(int idx) {

	return path.size() - idx == 0;

    }

    private String unique(IGSConfigurable configurable) {

	return configurable.getKey();
    }

    public void setOnRoot() {
	onRoot = true;
    }

    public boolean isOnRoot() {
        return onRoot;
    }

    public void addLevel(String key) {

	path.add(key);

    }

    public void oneLevelUp() {

	oneLevelUp(false);

    }

    public void oneLevelUp(boolean removelowerLevels) {

	alreadyReadLevels--;

	if (alreadyReadLevels == 0) {
	    setOnRoot();

	    if (removelowerLevels)
		path = new ArrayList<>();
	}

	if (removelowerLevels) {
	    if (isRoot())
		path = new ArrayList<>();
	    else {

		for (int i = 0; i < path.size(); i++) {

		    if (i + 1 == alreadyReadLevels) {

			path = path.subList(0, i + 1);

		    }
		}

	    }
	}

    }

    @Override
    public String toString() {

	return "Level --> " + alreadyReadLevels + " :: " + keyString();

    }

    public String keyString() {

	StringBuilder builder = new StringBuilder("root");

	for (String p : path) {
	    builder.append(SEPARATOR);
	    builder.append(p);

	}

	return builder.toString();

    }

    public String getCurrentComponentKey() {

	if (alreadyReadLevels == 0)
	    return "root";

	for (int i = 0; i < path.size(); i++) {

	    if (i + 1 == alreadyReadLevels)
		return path.get(i);

	}

	return null;
    }
}
