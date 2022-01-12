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

import com.google.common.net.MediaType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.AbstractGSconfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.shared.driver.fs.PersistentDriverFSInit;
import eu.essi_lab.shared.driver.fs.PersistentDriverFSReader;
import eu.essi_lab.shared.driver.fs.PersistentDriverFSWriter;
import eu.essi_lab.shared.messages.SharedContentQuery;
import eu.essi_lab.shared.model.SharedContent;
import eu.essi_lab.shared.model.SharedContentType;
import eu.essi_lab.shared.serializer.GSSharedContentSerializers;
import eu.essi_lab.shared.serializer.IGSScharedContentSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
public class LocalPersistentDriver extends AbstractGSconfigurable implements ISharedPersistentRepositoryDriver {

    private Map<String, GSConfOption<?>> options = new HashMap<>();
    private static final String METHOD_NOT_IMPLEMENTED_ERR_ID = "METHOD_NOT_IMPLEMENTED_ERR_ID";
    private transient MediaType media = MediaType.JSON_UTF_8;

    private transient Logger logger = GSLoggerFactory.getLogger(LocalPersistentDriver.class);

    public LocalPersistentDriver() {
	setLabel("Local FS Persistent");
    }

    PersistentDriverFSReader getFSReader(SharedContentType type) throws GSException {
	return new PersistentDriverFSReader(PersistentDriverFSInit.getInstance().getDirectory(type));
    }

    PersistentDriverFSWriter getFSWriter(SharedContentType type) throws GSException {
	return new PersistentDriverFSWriter(PersistentDriverFSInit.getInstance().getDirectory(type));
    }

    InputStream readFile(File file) throws FileNotFoundException {
	return new FileInputStream(file);
    }

    @Override
    public SharedContent readSharedContent(String identifier, SharedContentType type) throws GSException {

	PersistentDriverFSReader reader = getFSReader(type);

	File f = reader.read(identifier);

	if (f == null)
	    return null;

	SharedContent content = new SharedContent();

	Optional.of(findSerializer(type)).ifPresent(serializer -> {

	    try {
		content.setContent(serializer.fromStream(readFile(f)).getContent());
	    } catch (FileNotFoundException e) {
		logger.error("Exception reading {}", f.getAbsolutePath(), e);
	    } catch (GSException e) {
		logger.error("Found GSException reading {}", f.getAbsolutePath());
		DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));
	    }

	});

	return content.getContent() != null ? content : null;
    }

    @Override
    public List<SharedContent> readSharedContent(SharedContentType type, SharedContentQuery query) throws GSException {

	throw GSException.createException(this.getClass(), "Shared content by timestamp not implemented", null, null,
		ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_WARNING, METHOD_NOT_IMPLEMENTED_ERR_ID);
    }

    IGSScharedContentSerializer findSerializer(SharedContentType type) {
	return GSSharedContentSerializers.getSerializer(type, media);
    }

    @Override
    public void store(SharedContent sharedContent) throws GSException {

	PersistentDriverFSWriter writer = getFSWriter(sharedContent.getType());

	writer.write(sharedContent.getIdentifier(), findSerializer(sharedContent.getType()).toStream(sharedContent));

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
