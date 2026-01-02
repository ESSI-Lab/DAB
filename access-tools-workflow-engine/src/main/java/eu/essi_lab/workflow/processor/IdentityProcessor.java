package eu.essi_lab.workflow.processor;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import com.google.common.io.Files;

import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataObject;

/**
 * It does nothing!
 * 
 * @author boldrini
 */
public class IdentityProcessor extends DataProcessor {

    private String caller = null;

    public IdentityProcessor(String caller) {
	this.caller = caller;
    }

    public IdentityProcessor() {
    }

    public IdentityProcessor(ProcessorCapabilities input, ProcessorCapabilities output) {

	setInputCapabilities(input);
	setOutputCapabilities(output);
    }

    @Override
    public DataObject process(GSResource resource, DataObject dataObject, TargetHandler handler) throws Exception {

	File file = dataObject.getFile();
	File temp = File.createTempFile("identity", null);

	Files.copy(file, temp);

	dataObject.setFile(temp);

	return dataObject;
    }

    @Override
    public String toString() {
	return caller == null ? "IdentityProcessor" : "IdentityProcessor(" + caller + ")";
    }
}
