package eu.essi_lab.lib.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

/**
 * @author Fabrizio
 */
public class PropertiesUtils {

    /**
     * @param stream
     * @param propertiesClass
     * @return
     * @throws IOException
     */
    public static <P extends Properties> P fromStream(InputStream stream, Class<P> propertiesClass) throws IOException {

	Properties properties = new Properties();
	properties.load(stream);

	Set<String> keys = properties.stringPropertyNames();

	P newInstance = null;
	try {
	    newInstance = propertiesClass.newInstance();
	    for (String key : keys) {
		newInstance.setProperty(key, properties.getProperty(key));
	    }
	} catch (Exception e) {
	}

	return newInstance;
    }

    /**
     * @param properties
     * @return
     * @throws IOException
     */
    public static InputStream asStream(Properties properties) throws IOException {

	ByteArrayOutputStream out = new ByteArrayOutputStream();
	properties.store(out, "");

	return new ByteArrayInputStream(out.toByteArray());
    }

}
