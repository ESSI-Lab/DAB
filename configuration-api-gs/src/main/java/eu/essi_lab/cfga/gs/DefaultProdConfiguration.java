package eu.essi_lab.cfga.gs;

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
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationSource;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.source.FileSource;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class DefaultProdConfiguration extends Configuration {

    /**
     * @throws IOException
     * @throws Exception
     */
    public DefaultProdConfiguration() throws IOException, Exception {

	init();
    }
    
    /**
     * @param unit
     * @param interval
     * @throws IOException
     * @throws Exception
     */
    public DefaultProdConfiguration(ConfigurationSource source) throws IOException, Exception {

	super(source);

	init();
    }

    /**
     * @param unit
     * @param interval
     * @throws IOException
     * @throws Exception
     */
    public DefaultProdConfiguration(ConfigurationSource source, TimeUnit unit, int interval) throws IOException, Exception {

	super(source, unit, interval);

	init();
    }

    /**
     * @return
     */
    protected String getConfigName() {

	return "default-prod-config.json";
    }

    /**
     * @throws URISyntaxException
     */
    private void init() {

	try {

	    InputStream in = getClass().getClassLoader().getResourceAsStream("/" + getConfigName());
	    
	    if(in == null){
		
		in = getClass().getClassLoader().getResourceAsStream(getConfigName());
	    }

	    File tempFile = File.createTempFile(getConfigName(), ".json");

	    FileUtils.copyInputStreamToFile(in, tempFile);

	    FileSource fileSource = new FileSource(tempFile);

	    List<Setting> settings = fileSource.list();

	    settings.forEach(s -> this.put(s));

	    this.source = fileSource;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * 
     */
    public void clean() {

	SelectionUtils.deepClean(this);

	SelectionUtils.deepAfterClean(this);
    }
}
