package eu.essi_lab.gssrv.portal;

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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public class PortalTranslator {
    private String language;

    private static HashMap<String, HashMap<String, String>> translations = new HashMap<String, HashMap<String, String>>();

    public PortalTranslator(String language) {

	initializeDictionary("it", "giportal/lang/it.json");
	initializeDictionary("en", "giportal/lang/en.json");

	this.language = language;
    }

    private void initializeDictionary(String language, String resource) {
	HashMap<String, String> dictionary = translations.get(language);
	if (dictionary == null) {
	    dictionary = new HashMap<String, String>();
	    translations.put(language, dictionary);
	}
	InputStream stream = PortalTranslator.class.getClassLoader().getResourceAsStream(resource);
	try {
	    String str = IOUtils.toString(stream, StandardCharsets.UTF_8);
	    JSONObject json = new JSONObject(str);
	    Iterator<String> keys = json.keys();
	    while (keys.hasNext()) {
		String id = keys.next();
		String label = json.getString(id);
		dictionary.put(id, label);		
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}	
    }

    public String getTranslation(String id) {
	HashMap<String, String> dictionary = translations.get(language);
	if (dictionary != null) {
	    return dictionary.get(id);
	}
	return id;
    }
}
