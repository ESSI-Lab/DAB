package eu.essi_lab.workflow.processor.timeseries;

import eu.essi_lab.model.resource.data.DataType;

public abstract class AbstractTrajectorySubsetTest extends AbstractSubsetTest {

    @Override
    protected DataType getDataType() {
	return DataType.TRAJECTORY;
    }
}
