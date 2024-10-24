package eu.essi_lab.workflow.processor.timeseries;

import eu.essi_lab.model.resource.data.DataType;

public abstract class AbstractTimeSeriesSubsetTest extends AbstractSubsetTest {

    @Override
    protected DataType getDataType() {
	return DataType.TIME_SERIES;
    }
}
