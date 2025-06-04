package eu.essi_lab.accessor.cdi;

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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class CDI13Validator extends CDIValidator {

    @Override
    public URL getSchemaURL() {
	try {
	    return new URL("https://schemas.seadatanet.org/Standards-Software/Metadata-formats/SDN_CDI_ISO19139_13.0.0.xsd");
	} catch (MalformedURLException e) {
	    // it shouldn't happen
	    e.printStackTrace();
	}
	return null;
    }

    @Override
    public InputStream getSchematronXSLT() {
	return CDI13Validator.class.getClassLoader().getResourceAsStream("xslt/cdi-13.0.0.xslt");
    }

}
