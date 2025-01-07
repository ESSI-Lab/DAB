package eu.essi_lab.odip.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.AbstractMap.SimpleEntry;
import java.util.Set;
import java.util.TreeSet;

public class ODIPUtils {

    private static ODIPUtils instance = null;

    public static ODIPUtils getInstance() {
	if (instance == null) {
	    instance = new ODIPUtils();
	}
	return instance;
    }

    private ODIPUtils() {
    }

    private Set<SimpleEntry<String, String>> parameters = new TreeSet<SimpleEntry<String, String>>();
    private Set<SimpleEntry<String, String>> instruments = new TreeSet<SimpleEntry<String, String>>();
    private Set<SimpleEntry<String, String>> platforms = new TreeSet<SimpleEntry<String, String>>();
    private Set<SimpleEntry<String, String>> originatorOrganizations = new TreeSet<SimpleEntry<String, String>>();
    private boolean enabled = false;

    public boolean isEnabled() {
	return enabled;
    }

    public void setEnabled(boolean enabled) {
	this.enabled = enabled;
    }

    public Set<SimpleEntry<String, String>> getParameters() {
	return parameters;
    }

    public Set<SimpleEntry<String, String>> getInstruments() {
	return instruments;
    }

    public Set<SimpleEntry<String, String>> getPlatforms() {
	return platforms;
    }

    public Set<SimpleEntry<String, String>> getOriginatorOrganizations() {
	return originatorOrganizations;
    }

    public void write(File folder, String prefix) {
	writeSet(new File(folder, prefix + "-parameters.txt"), parameters);
	writeSet(new File(folder, prefix + "-instruments.txt"), instruments);
	writeSet(new File(folder, prefix + "-platforms.txt"), platforms);
	writeSet(new File(folder, prefix + "-originatorOrganizations.txt"), originatorOrganizations);
    }

    private void writeSet(File file, Set<SimpleEntry<String, String>> set) {
	try {
	    PrintWriter out = new PrintWriter(file);
	    for (SimpleEntry<String, String> row : set) {
		out.println(row.getKey()+";"+row.getValue());
	    }
	    out.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}

    }

    public static void main(String[] args) {
	ODIPUtils.getInstance().getParameters().add(new SimpleEntry<String, String>("id1","label 1"));
	ODIPUtils.getInstance().write(new File("/home/boldrini/odip"), "test");
    }

}
