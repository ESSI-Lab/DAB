package eu.essi_lab.shared.driver;

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

import eu.essi_lab.model.configuration.AbstractGSconfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.shared.messages.SharedContentQuery;
import eu.essi_lab.shared.messages.SharedContentReadResponse;
import eu.essi_lab.shared.model.SharedContent;
import eu.essi_lab.shared.model.SharedContentType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class LocalCacheDriver extends AbstractGSconfigurable implements ISharedCacheRepositoryDriver {

    private Map<String, GSConfOption<?>> options = new HashMap<>();
    private static final String METHOD_NOT_IMPLEMENTED_ERR_ID = "METHOD_NOT_IMPLEMENTED_ERR_ID";

    public LocalCacheDriver() {
	setLabel("Local Cache");
    }

    @Override
    public SharedContent readSharedContent(String identifier, SharedContentType type) throws GSException {

	SharedContent content = SharedTable.getInstance().getContent(identifier);

	SharedContentReadResponse response = new SharedContentReadResponse();

	response.addContent(content);

	return content;
    }

    @Override
    public List<SharedContent> readSharedContent(SharedContentType type, SharedContentQuery query) throws GSException {

	throw GSException.createException(this.getClass(), "Shared content by timestamp not implemented", null, null,
		ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_WARNING, METHOD_NOT_IMPLEMENTED_ERR_ID);
    }

    @Override
    public void store(SharedContent sharedContent) throws GSException {

	SharedTable.getInstance().storeContent(sharedContent);

    }

    @Override
    public Long count(SharedContentType type) throws GSException {
	return SharedTable.getInstance().size();
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {
	return options;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {
	//nothing to do here
    }

    @Override
    public void onFlush() throws GSException {
	//nothing to do here
    }
}
