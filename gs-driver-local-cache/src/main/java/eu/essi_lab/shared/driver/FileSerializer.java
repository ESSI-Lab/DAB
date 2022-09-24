package eu.essi_lab.shared.driver;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.prefs.Preferences;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;
import eu.essi_lab.shared.serializer.SharedContentSerializer;

/**
 * @author Fabrizio
 */
public class FileSerializer implements SharedContentSerializer {

    /**
     * 
     */
    private static final String SERIALIZATION_ERROR_EXCEPTION = "FILE_SERIALIZER_SERIALIZATION_ERROR_EXCEPTION";

    @Override
    public InputStream toStream(@SuppressWarnings("rawtypes") SharedContent content) throws GSException {

	Object payload = content.getContent();

	File file = (File) payload;

	Preferences preferences = Preferences.userNodeForPackage(FileSerializer.class);

	preferences.put(content.getIdentifier(), file.getAbsolutePath());

	try {
	    return new FileInputStream(file);

	} catch (FileNotFoundException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SERIALIZATION_ERROR_EXCEPTION, //
		    e);
	}
    }

    @SuppressWarnings("rawtypes")
    @Override
    public SharedContent fromStream(String contentIdentifier, InputStream stream) throws GSException {

	Preferences preferences = Preferences.userNodeForPackage(FileSerializer.class);

	String path = preferences.get(contentIdentifier, null);

	SharedContent<Object> sharedContent = new SharedContent<>();

	sharedContent.setContent(new File(path));

	sharedContent.setType(SharedContentType.FILE_TYPE);

	return sharedContent;
    }

    @Override
    public boolean supports(SharedContentType type) {

	return type != null && type == SharedContentType.FILE_TYPE;
    }
}
