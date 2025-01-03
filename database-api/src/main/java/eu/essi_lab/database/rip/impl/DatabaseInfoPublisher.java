/**
 * 
 */
package eu.essi_lab.database.rip.impl;

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

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang3.StringEscapeUtils;

import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.servlet.RequestManager;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.rip.RuntimeInfoProvider;
import eu.essi_lab.rip.RuntimeInfoPublisher;

/**
 * @author Fabrizio
 */
public class DatabaseInfoPublisher extends RuntimeInfoPublisher {

    private static DatabaseWriter writer;

    private static boolean enabled;

    public static final String RUNTIME_FOLDER = "runtime-info";

    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(new ThreadFactory() {

	@Override
	public Thread newThread(Runnable r) {

	    Thread thread = new Thread(r);
	    thread.setPriority(Thread.MIN_PRIORITY);
	    thread.setName(thread.getName() + "_DB_INFO_PUBLISHER");

	    return thread;
	}
    });

    /**
     * @param runtimeId
     * @param context
     * @throws GSException
     */
    public DatabaseInfoPublisher(StorageInfo uri, String runtimeId, String context) throws GSException {

	super(runtimeId, context);

	if (enabled && writer == null) {
	    writer = DatabaseProviderFactory.getWriter(uri);
	}
    }

    /**
     * This publisher must be enabled by the GI-Suite starter
     */
    public static void enable() {

	enabled = true;
    }

    @Override
    public void publish(RuntimeInfoProvider provider) throws GSException {

	if (provider == null || !enabled) {
	    return;
	}

	THREAD_POOL.execute(new Runnable() {

	    @Override
	    public void run() {

		RequestManager.getInstance().updateThreadName(getClass(), DatabaseInfoPublisher.this.getRuntimeId());

		try {

		    StringBuilder content = new StringBuilder();

		    HashMap<String, List<String>> map = provider.provideInfo();

		    if (!map.isEmpty()) {

			content.append("\n");
			content.append("\n");

			map.forEach((key, list) -> {
			    list.forEach(val -> {

				if (StringUtils.isNotEmpty(val)) {

				    content.append(createTextNode(key, val));
				    content.append("\n");
				}
			    });
			});

			String document = createDocument(provider.getName(), "\n" + content.toString(), false);

			XMLDocumentReader reader = new XMLDocumentReader(document);
			reader.setNamespaceContext(new CommonNameSpaceContext());

			writer.store(createURI(provider.getName()), reader.getDocument());
		    }

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}
	    }
	});
    }

    /**
     * @param providerName
     * @return
     */
    private String createURI(String providerName) {

	return "/" + RUNTIME_FOLDER + "/" + getRuntimeId() + "_" + UUID.randomUUID().toString().substring(0, 4) + "_" + providerName;
    }

    /**
     * @param name
     * @param value
     * @return
     */
    private String createDocument(String name, String value, boolean escape) {

	if (escape) {
	    value = StringEscapeUtils.escapeXml11(value);
	}

	String ns = !escape ? " xmlns:gs='" + NameSpace.GS_DATA_MODEL_SCHEMA_URI + "'" : "";

	return "<gs:" + name + ns + ">" + value + "</gs:" + name + ">";
    }

    /**
     * @param name
     * @param value
     * @return
     */
    private String createTextNode(String name, String value) {

	return createDocument(name, value, true);
    }
}
