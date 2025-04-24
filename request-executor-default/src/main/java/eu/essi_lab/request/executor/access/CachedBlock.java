package eu.essi_lab.request.executor.access;

import eu.essi_lab.workflow.builder.Workblock;
import eu.essi_lab.workflow.builder.WorkblockBuilder;
import eu.essi_lab.workflow.processor.DataProcessor;

public class CachedBlock extends Workblock {

    public CachedBlock(DataProcessor process, WorkblockBuilder builder) {
	super(process, builder);
    }

}
