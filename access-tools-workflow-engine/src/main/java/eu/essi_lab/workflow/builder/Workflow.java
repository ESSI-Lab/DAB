package eu.essi_lab.workflow.builder;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.DescriptorUtils;
import eu.essi_lab.workflow.processor.ProcessorCapabilities;
import eu.essi_lab.workflow.processor.TargetHandler;

/**
 * A workflow has a list of {@link Workblock} with related {@link DataProcessor}s. A workflow execution consists in a
 * chain processing of a
 * {@link DataObject} which is processed in turn by all the {@link DataProcessor}s in the {@link Workblock} list,
 * according to the list
 * order.<br>
 * A workflow can be viewed as a single {@link DataProcessor} defined by the union of all its {@link DataProcessor}s and
 * having
 * as input a {@link ProcessorCapabilities} derived from the initial description of the data to transform, and as output
 * the {@link
 * ProcessorCapabilities} derived from the initial description of the data to transform and from the target
 * {@link DataDescriptor}
 *
 * @author Fabrizio
 * @see ProcessorCapabilities#fromInputDescriptor(DataDescriptor)
 * @see ProcessorCapabilities#fromTargetDescriptor(DataDescriptor, DataDescriptor)
 * @see #execute(DataObject)
 * @see #getWorkblocks()
 */
public class Workflow {

    Logger logger = GSLoggerFactory.getLogger(Workflow.class);

    private LinkedList<Workblock> list;

    /**
     *
     */
    public Workflow() {

	list = new LinkedList<>();
    }

    /**
     * Executes this workflow. A workflow execution consists in a chain processing of the supplied
     * <code>dataObject</code> which is
     * processed in turn by all the {@link DataProcessor}es in the {@link Workblock} list, according to the list order.
     * Each process
     * transform the data according to its {@link DataProcessor#getOutputCapabilities()} and to the
     * <code>targetDescriptor</code>.<br>
     * The last process of this workflow will provide a data compliant with the supplied <code>targetDescriptor</code>
     *
     * @param dataObject the {@link DataObject} to process
     * @param targetDescriptor the target data descriptor
     * @return the processed {@link DataObject}
     * @throws Exception
     * @see #getWorkblocks()
     */
    public DataObject execute(GSResource resource, DataObject dataObject, DataDescriptor targetDescriptor) throws Exception {

	ProcessorCapabilities currentCap = DescriptorUtils.fromInputDescriptor(dataObject.getDataDescriptor());
	ProcessorCapabilities targetCap = DescriptorUtils.fromTargetDescriptor(//
		dataObject.getDataDescriptor(), //
		targetDescriptor);

	boolean debug = false; // if debug is activated, intermediate files are generated for debug purposes
	// e.g. debug_1_RND, debug_2_RND, ... debug_n_RND
	// Note: these debug files will not be deleted, in order to be examined after execution
	if (debug) {
	    // this check is to ensure that debug files are not generated on production! ... sometimes the debug flag
	    // is left as true on production for error resulting in disk filling up with tmp files during harvesting!
	    if (!isLocalEnvironment()) {
		debug = false;
	    }
	}
	String rnd = debug ? UUID.randomUUID().toString() : "";
	int d = 1;
	if (debug) {
	    copyFile(dataObject.getFile(), "debug_" + rnd + "_" + d++ + "_");
	}

	for (int i = 0; i < list.size(); i++) {

	    Workblock workblock = list.get(i);
	    DataProcessor process = workblock.getProcess();

	    ProcessorCapabilities nextInput = null;
	    if (i < list.size() - 1) {

		nextInput = list.get(i + 1).getProcess().getInputCapabilities();
	    }

	    currentCap = TargetHandler.getNextCapabilities(//
		    process.getOutputCapabilities(), //
		    nextInput, //
		    currentCap, //
		    targetCap);

	    TargetHandler target = new TargetHandler(//
		    dataObject.getDataDescriptor(), //
		    targetDescriptor, //
		    currentCap);

	    logger.info("Processing block: " + i + " (" + process.getClass().getSimpleName() + ")");
	    logger.info("Input data: " + dataObject.getFile());

	    File tmpFile = dataObject.getFile();

	    dataObject = process.process(resource, dataObject, target);

	    if (tmpFile.exists() && !tmpFile.getPath().contains("change")&&!tmpFile.getPath().contains("netcdf-connector")) {
		eu.essi_lab.lib.utils.FileTrash.deleteLater(tmpFile);
	    }

	    if (debug) {
		copyFile(dataObject.getFile(), "debug_" + rnd + "_" + d++ + "_");
	    }
	}

	logger.info("Processing ended.");
	logger.info("Output data: " + dataObject.getFile());

	// updates the data descriptor to the target
	dataObject.setDataDescriptor(targetDescriptor);
    if (dataObject.getFile().exists() && !dataObject.getFile().getPath().contains("change")&& !dataObject.getFile().getPath().contains("netcdf-connector")) {

	dataObject.getFile().deleteOnExit();
    }

	return dataObject;
    }

    private boolean isLocalEnvironment() {
	String[] absolutelySureLocalFolders = new String[] { "/home/boldrini", "/home/santoro" }; // add as needed...
	for (String absolutelySureLocalFolder : absolutelySureLocalFolders) {
	    File file = new File(absolutelySureLocalFolder);
	    if (file.exists()) {
		return true;
	    }
	}
	return false;
    }

    private void copyFile(File file, String name) {
	if (file != null && file.exists()) {
	    String path = file.getAbsolutePath();
	    String suffix = ".bin";
	    if (path.contains(".")) {
		suffix = path.substring(path.lastIndexOf("."));
	    }
	    try {
		File dstFile = File.createTempFile(name, suffix);
		FileInputStream fis = new FileInputStream(file);
		FileOutputStream fos = new FileOutputStream(dstFile);
		IOUtils.copy(fis, fos);
		fis.close();
		fos.close();
		GSLoggerFactory.getLogger(getClass()).trace("Created debug file: " + dstFile.getAbsolutePath());
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}

    }

    /**
     * Checks if the {@link DataProcessor}s of this workflow can effectively transform the {@link DataObject} from
     * <code>initialDescriptor</code> to <code>targetDescriptor</code>
     *
     * @param initialDescriptor
     * @param targetDescriptor
     * @return
     */
    public boolean check(DataDescriptor initialDescriptor, DataDescriptor targetDescriptor) {

	ProcessorCapabilities currentCap = DescriptorUtils.fromInputDescriptor(initialDescriptor);
	ProcessorCapabilities targetCap = DescriptorUtils.fromTargetDescriptor(initialDescriptor, targetDescriptor);

	for (int i = 0; i < list.size(); i++) {

	    Workblock workblock = list.get(i);
	    DataProcessor process = workblock.getProcess();

	    ProcessorCapabilities nextInput = null;
	    if (i < list.size() - 1) {

		nextInput = list.get(i + 1).getProcess().getInputCapabilities();
	    }

	    try {
		currentCap = TargetHandler.getNextCapabilities(//
			process.getOutputCapabilities(), //
			nextInput, //
			currentCap, //
			targetCap);

	    } catch (IllegalStateException ex) {

		GSLoggerFactory.getLogger(getClass()).warn("Check failed: {}", ex.getMessage());

		return false;
	    }
	}

	return true;
    }

    /**
     * Returns the sum of the preference of all the blocks in the block list
     *
     * @see Workblock#getPreference()
     */
    public int getPreference() {

	return list.stream().mapToInt(w -> w.getPreference()).sum();
    }

    /**
     * Returns the list of {@link Workblock} used to execute this workflow. This is a live list, not a snapshot, so it
     * can be used to add
     * and remove the blocks
     *
     * @return
     */
    public List<Workblock> getWorkblocks() {

	return list;
    }

    @Override
    public String toString() {

	StringBuilder out = new StringBuilder("--- INIT WORKFLOW ---\n");

	for (Workblock w : getWorkblocks()) {

	    out.append(w.toString() + "\n");
	}

	out.append("--- END WORKFLOW ---\n");

	return out.toString();
    }

    @Override
    public boolean equals(Object o) {

	if (o == null)
	    return false;

	if (!(o instanceof Workflow))
	    return false;

	Workflow w = (Workflow) o;

	return w.getWorkblocks().equals(getWorkblocks());

    }

    @Override
    public int hashCode() {
	return toString().hashCode();
    }
}
