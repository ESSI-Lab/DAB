package eu.essi_lab.lib.odip;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.IOStreamUtils;

/**
 * @author Fabrizio
 */
public class ODIPVocabularyHandler {

    private OutputFormat outputFormat;
    private static final String PROFILE = "profile";
    private static final String TARGET = "target";

    public ODIPVocabularyHandler() {

	outputFormat = OutputFormat.JSON;
    }

    public enum Profile {
	NODC, //
	MCP, //
	CDI, //
    }

    public enum Target {
	INSTRUMENTS, //
	ORIG_ORGANIZATIONS, //
	PARAMETERS, //
	PLATFORMS//
    }

    public enum OutputFormat {
	JSON, //
	XML//
    }

    /**
     * @param outputFormat
     */
    public void setOutputFormat(OutputFormat outputFormat) {

	this.outputFormat = outputFormat;
    }

    /**
     * @param profile
     * @param target
     * @param suggestion
     * @return
     * @throws IOException
     */
    public String listLabels(Profile profile, Target target, String suggestion) throws IOException {

	HashMap<String, String> map = getMap(profile, target);
	String output = null;
	switch (outputFormat) {
	case JSON:
	    JSONObject object = new JSONObject();
	    object.put(PROFILE, profile.toString());
	    object.put(TARGET, target.toString());

	    JSONArray labels = new JSONArray();
	    Set<String> keySet = map.keySet();
	    String[] keys = keySet.toArray(new String[] {});
	    Arrays.sort(keys);

	    for (String key : keys) {
		
		key = key.trim();
		
		if (suggestion == null) {
		    labels.put(key);
		} else if (suggestion != null && key.toLowerCase().contains(suggestion.toLowerCase())) {
		    labels.put(key);
		}
	    }

	    object.put("labels", labels);
	    output = object.toString();
	    break;
	case XML:
	    break;
	}

	return output;
    }

    /**
     * @param profile
     * @param target
     * @return
     * @throws IOException
     */
    public String listTerms(Profile profile, Target target) throws IOException {

	HashMap<String, String> map = getMap(profile, target);
	String output = null;
	switch (outputFormat) {
	case JSON:
	    JSONObject object = new JSONObject();
	    object.put(PROFILE, profile.toString());
	    object.put(TARGET, target.toString());

	    JSONArray terms = new JSONArray();
	    Set<String> keySet = map.keySet();
	    for (String key : keySet) {
		terms.put(key.trim());
	    }

	    object.put("terms", terms);
	    output = object.toString();
	    break;
	case XML:
	    break;
	}
	return output;

    }

    /**
     * @param term
     * @return
     * @throws IOException
     */
    public String getLabel(String term) throws IOException {

	term = URLDecoder.decode(term, "UTF-8");

	HashMap<String, String> map = getUnionMap();
	String output = null;
	switch (outputFormat) {
	case JSON:
	    JSONObject object = new JSONObject();
	    object.put("label", map.get(term).trim());
	    output = object.toString();
	    break;
	case XML:
	    break;
	}

	return output;
    }

    /**
     * @param profile
     * @param target
     * @param label
     * @return
     * @throws IOException
     */
    public String getTerm(Profile profile, Target target, String label) throws IOException {

	label = URLDecoder.decode(label, "UTF-8").trim();

	HashMap<String, String> map = getMap(profile, target);
	String output = null;
	switch (outputFormat) {
	case JSON:
	    JSONObject object = new JSONObject();
	    object.put(PROFILE, profile.toString());
	    object.put(TARGET, target.toString());

	    Set<String> keySet = map.keySet();
	    for (String key : keySet) {
		if (key.trim().equals(label)) {
		    object.put("label", key);
		    object.put("term", map.get(key).trim());
		    break;
		}
	    }

	    output = object.toString();
	    break;
	case XML:
	    break;
	}

	return output;
    }

    private HashMap<String, String> getUnionMap() throws IOException {

	HashMap<String, String> map = new HashMap<>();

	for (int i = 0; i < Profile.values().length; i++) {
	    for (int j = 0; j < Target.values().length; j++) {

		Profile profile = Profile.values()[i];
		Target target = Target.values()[j];

		String prefix = getPrefix(profile);
		String postfix = getPostfix(target);

		InputStream stream = getClass().getClassLoader().getResourceAsStream(prefix + postfix);
		String doc = IOStreamUtils.asUTF8String(stream);
		if (!doc.equals("")) {
		    String[] rows = doc.split("\n");
		    for (String row : rows) {
			String[] split = row.split(";");
			map.put(split[0], split[1]);
		    }
		}
	    }
	}

	return map;
    }

    private HashMap<String, String> getMap(Profile profile, Target target) throws IOException {

	String prefix = getPrefix(profile);
	String postfix = getPostfix(target);

	HashMap<String, String> map = new HashMap<>();
	InputStream stream = getClass().getClassLoader().getResourceAsStream(prefix + postfix);
	String doc = IOStreamUtils.asUTF8String(stream);
	if (!doc.equals("")) {
	    String[] rows = doc.split("\n");
	    for (String row : rows) {
		String[] split = row.split(";");
		map.put(split[1], split[0]);
	    }
	}

	return map;
    }

    private String getPrefix(Profile profile) {

	String prefix = null;

	switch (profile) {
	case CDI:
	    prefix = "cdi";
	    break;
	case MCP:
	    prefix = "mcp";
	    break;
	case NODC:
	    prefix = "nodc";
	    break;
	}

	return prefix;
    }

    private String getPostfix(Target target) {

	String postfix = null;

	switch (target) {
	case INSTRUMENTS:
	    postfix = "-instruments.txt";
	    break;
	case ORIG_ORGANIZATIONS:
	    postfix = "-originatorOrganizations.txt";
	    break;
	case PARAMETERS:
	    postfix = "-parameters.txt";
	    break;
	case PLATFORMS:
	    postfix = "-platforms.txt";
	    break;
	}

	return postfix;
    }
}
