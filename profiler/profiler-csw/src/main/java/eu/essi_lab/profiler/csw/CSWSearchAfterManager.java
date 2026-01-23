/**
 *
 */
package eu.essi_lab.profiler.csw;

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

import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.DatabaseFolder.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.messages.*;
import eu.essi_lab.messages.web.*;
import eu.essi_lab.model.exceptions.*;

import java.io.*;
import java.util.*;

import static eu.essi_lab.profiler.csw.CSWProfilerSetting.KeyValueOptionKeys.SEARCH_AFTER_KEY_OPTION;
import static eu.essi_lab.profiler.csw.CSWProfilerSetting.KeyValueOptionKeys.USE_SEARCH_AFTER_OPTION;

/**
 * @author Fabrizio
 */
public class CSWSearchAfterManager {

    /**
     * @param viewId
     * @param setting
     * @param searchAfter
     * @throws Exception
     */
    public static void put(Optional<String> viewId, CSWProfilerSetting setting, SearchAfter searchAfter) throws Exception {

	String key = getKey(viewId, setting);

	getFolder().remove(key);

	InputStream serialized = SearchAfter.serialize(searchAfter);

	getFolder().store(key, FolderEntry.of(serialized), EntryType.CACHE_ENTRY);
    }

    /**
     * @param viewId
     * @param page
     * @param setting
     * @return
     * @throws GSException
     */
    public static Optional<SearchAfter> get(Optional<String> viewId, Page page, ProfilerSetting setting) throws GSException {

	try {

	    String key = getKey(viewId, setting);

	    if (page.getStart() == 1) {

		getFolder().remove(key);

		return Optional.empty();
	    }

	    InputStream binary = getFolder().getBinary(key);

	    return binary == null ? Optional.empty() : Optional.of(SearchAfter.deserialize(binary));

	} catch (Exception ex) {

	    throw GSException.createException(CSWSearchAfterManager.class, "CSWSearchAfterManagerGetError", ex);
	}
    }

    /**
     * @param setting
     * @param webRequest
     * @return
     * @throws GSException
     */
    public static boolean isEnabled(ProfilerSetting setting, WebRequest webRequest) throws GSException {

	boolean fromGET = CSWRequestUtils.isGetRecordsFromGET(webRequest);
	boolean fromPOST = CSWRequestUtils.isGetRecordsFromPOST(webRequest);

	return (fromGET || fromPOST) && setting.getKeyValueOptions().//
		map(opt -> opt.getOrDefault(USE_SEARCH_AFTER_OPTION.getLabel(), "false").equals("true")).//
		orElse(false);
    }

    /**
     * @param viewId
     * @param setting
     * @return
     */
    private static String getKey(Optional<String> viewId, ProfilerSetting setting) {

	String key = setting.readKeyValue(SEARCH_AFTER_KEY_OPTION.getLabel()).get();

	return viewId.map(s -> key + "_" + s).orElse(key);
    }

    /**
     * @return
     * @throws GSException
     */
    private static DatabaseFolder getFolder() throws GSException {

	Database database = DatabaseFactory.get(ConfigurationWrapper.getStorageInfo());

	Optional<DatabaseFolder> folder = database.getFolder(Database.CACHE_FOLDER, true);

	if (folder.isEmpty()) {

	    throw GSException.createException(CSWSearchAfterManager.class, //
		    "Unable to get search after cache folder", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "CSWSearchAfterManagerNoCacheFolderError"); //
	}

	return folder.get();
    }
}
