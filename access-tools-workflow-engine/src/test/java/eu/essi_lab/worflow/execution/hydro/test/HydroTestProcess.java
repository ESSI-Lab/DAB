package eu.essi_lab.worflow.execution.hydro.test;

import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.TargetHandler;

/**
 * This test process do not alter the data, but changes only its data descriptor
 * 
 * @author Fabrizio
 */
public class HydroTestProcess extends DataProcessor {

    public HydroTestProcess() {
    }

    @Override
    public DataObject process(GSResource resource,DataObject dataObject, TargetHandler handler) throws Exception {

	return null;
    }
}
