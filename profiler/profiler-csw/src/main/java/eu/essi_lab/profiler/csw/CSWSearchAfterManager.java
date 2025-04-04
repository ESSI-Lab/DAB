/**
 * 
 */
package eu.essi_lab.profiler.csw;

import java.io.InputStream;
import java.util.Optional;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class CSWSearchAfterManager {

    /**
     * 
     */
    private static final String SEARCH_AFTER_KEY_OPTION = "searchAfterKey";

    /**
     * 
     */
    private static final Object USE_SEARCH_AFTER_OPTION = "useSearchAfter";

    /**
     * @param request
     * @param setting
     * @param searchAfter
     * @throws Exception
     */
    public static void put(WebRequest request, CSWProfilerSetting setting, SearchAfter searchAfter) throws Exception {

	String key = getKey(setting);

	getFolder().remove(key);

	InputStream serialized = SearchAfter.serialize(searchAfter);

	getFolder().store(key, FolderEntry.of(serialized), EntryType.CACHE_ENTRY);
    }

    /**
     * @param message
     * @param setting
     * @return
     * @throws GSException
     */
    public static Optional<SearchAfter> get(Page page, CSWProfilerSetting setting) throws GSException {

	try {

	    String key = getKey(setting);

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
     * @return
     */
    public static boolean isEnabled(CSWProfilerSetting setting) {

	return setting.getKeyValueOptions().//
		map(opt -> opt.getOrDefault(USE_SEARCH_AFTER_OPTION, "false").equals("true")).//
		orElse(false);
    }

    /**
     * @param setting
     * @return
     */
    private static String getKey(CSWProfilerSetting setting) {

	return setting.getKeyValueOptions().get().getProperty(SEARCH_AFTER_KEY_OPTION);
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
