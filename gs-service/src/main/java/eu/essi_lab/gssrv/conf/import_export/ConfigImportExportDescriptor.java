package eu.essi_lab.gssrv.conf.import_export;

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

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.tabs.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.lib.net.s3.*;
import eu.essi_lab.model.*;
import org.apache.commons.io.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * @author Fabrizio
 */
public class ConfigImportExportDescriptor extends TabContentDescriptor {

    /**
     *
     */
    static final String DEFAULT_CONFIG_NAME = "gs-configuration.json";

    /**
     *
     */
    public ConfigImportExportDescriptor() {

	Div mainLayout = new Div();
	mainLayout.getStyle().set("padding", "0px");
	mainLayout.getStyle().set("margin-top", "-20px");
	mainLayout.setWidthFull();

	TabSheet tabSheet = new TabSheet();
	tabSheet.getStyle().set("padding", "0px");

	tabSheet.add("Import", new ConfigImporter());

	tabSheet.add("Export", new ConfigExporter());

	mainLayout.add(tabSheet);

	//
	//
	//

	setLabel("Configuration");
	setContent(mainLayout);
    }

    /**
     * @param wrapper
     * @param info
     * @param configStream
     * @param replace
     * @throws IOException
     */
    static void uploadS3Config(S3TransferWrapper wrapper, StorageInfo info, InputStream configStream) throws IOException {

	File tempFile = Files.createTempFile(UUID.randomUUID().toString(), ".json").toFile();

	FileOutputStream fileOutputStream = new FileOutputStream(tempFile);

	IOUtils.copy(configStream, fileOutputStream);

	wrapper.uploadFile(tempFile.getAbsolutePath(), info.getName(), DEFAULT_CONFIG_NAME);

	tempFile.delete();
    }
}
