package eu.essi_lab.shared.driver;

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

import java.util.List;

import eu.essi_lab.cfga.gs.setting.driver.SharedCacheDriverSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;
import eu.essi_lab.shared.messages.SharedContentQuery;

/**
 * This implements the driver for using local cache as the shared repository of category cache. Cached objects are
 * stored into a HashMap
 * which is periodically cleared. This implementation of the interface {@link ISharedCacheRepositoryDriver} does not
 * provide the
 * implementation of the method {@link ISharedCacheRepositoryDriver#read(SharedContentType,
 * eu.essi_lab.shared.messages.SharedContentQuery)}
 *
 * @author ilsanto
 */
public class LocalCacheDriver implements ISharedRepositoryDriver<SharedCacheDriverSetting> {

    /**
     * 
     */
    static final String CONFIGURABLE_TYPE = "LocalCacheDriver";

    private static final String METHOD_NOT_IMPLEMENTED_ERR_ID = "LOCAL_CACHE_DRIVER_METHOD_NOT_IMPLEMENTED_ERROR";

    private static SharedCacheDriverSetting setting;
    private static ContentTable contentTable;

    static {
	contentTable = new ContentTable();
	setting = new SharedCacheDriverSetting();
    }

    public LocalCacheDriver() {

    }

    @SuppressWarnings("rawtypes")
    @Override
    public void store(SharedContent sharedContent) throws GSException {

	contentTable.storeContent(sharedContent);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public synchronized SharedContent read(String identifier, SharedContentType type) throws GSException {

	SharedContent content = contentTable.getContent(identifier);

	if (content == null) {

	    return null;
	}

	if (content.getType() != type) {

	    GSLoggerFactory.getLogger(getClass()).warn("Shared content mismatch. Required: " + type + ", found: " + content.getType());

	    return null;
	}

	return content;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public synchronized List<SharedContent> read(SharedContentType type, SharedContentQuery query) throws GSException {

	throw GSException.createException(//
		this.getClass(), //
		"Shared content by timestamp not implemented", //
		null, //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_WARNING, //
		METHOD_NOT_IMPLEMENTED_ERR_ID);
    }

    @Override
    public synchronized Long count(SharedContentType type) throws GSException {

	return contentTable.size(type);
    }

    @Override
    public void configure(SharedCacheDriverSetting setting) {

	LocalCacheDriver.setting = setting;
    }

    @Override
    public SharedCacheDriverSetting getSetting() {

	return setting;
    }

    @Override
    public String getType() {

	return CONFIGURABLE_TYPE;
    }

    @Override
    public SharedContentCategory getCategory() {

	return SharedContentCategory.LOCAL_CACHE;
    }
}
