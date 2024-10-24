package eu.essi_lab.workflow.processor.timeseries;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.junit.Test;

import eu.essi_lab.access.DataValidatorImpl;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension.LimitType;

public abstract class AbstractSubsetTest {
    private static final long ONE_HOUR_IN_MILLISECONDS = 1000 * 60 * 60;
    private static final long ONE_DAY_IN_MILLISECONDS = ONE_HOUR_IN_MILLISECONDS * 24;
   

    private void testSubset(Date timeStart, Date timeEnd, int expectedSize, ValidationResult expectedResult) throws Exception {
	DataValidatorImpl validator = getValidator();

	File inputFile = getInputData();
	inputFile.deleteOnExit();
	AbstractTimeSubsetProcessor process = getProcess();
	File outputFile = process.subset(inputFile, timeStart, LimitType.ABSOLUTE, timeEnd, LimitType.ABSOLUTE);
	outputFile.deleteOnExit();
	inputFile.delete();
	DataObject outputData = new DataObject();
	outputData.setFile(outputFile);
	DataDescriptor dataDescriptor = new DataDescriptor();
	dataDescriptor.setTemporalDimension(timeStart, timeEnd);
	dataDescriptor.getTemporalDimension().getContinueDimension().setLowerType(LimitType.CONTAINS);
	dataDescriptor.getTemporalDimension().getContinueDimension().setUpperType(LimitType.CONTAINS);
	dataDescriptor.getTemporalDimension().getContinueDimension().setSize((long) expectedSize);
	dataDescriptor.setDataFormat(getDataFormat());
	dataDescriptor.setDataType(getDataType());
	outputData.setDataDescriptor(dataDescriptor);

	ValidationMessage message = validator.validate(outputData);
	outputFile.delete();
	ValidationResult actualResult = message.getResult();

	assertEquals(expectedResult, actualResult);

    }

    protected abstract DataType getDataType();

    public abstract DataFormat getDataFormat();

    public abstract File getInputData() throws IOException;

    public abstract DataValidatorImpl getValidator();

    public abstract AbstractTimeSubsetProcessor getProcess();

    @Test
    public void testSubsetOriginal() throws Exception {

	Date timeStart = new Date(getOriginalStart()); // original time start
	Date timeEnd = new Date(getOriginalEnd()); // original time end -> 49 values

	testSubset(timeStart, timeEnd, getOriginalSize(), ValidationResult.VALIDATION_SUCCESSFUL);
	testSubset(timeStart, timeEnd, 15, ValidationResult.VALIDATION_FAILED);

    }



    protected abstract int getOriginalSize();

    protected abstract long getOriginalStart();
    
    protected abstract long getOriginalEnd();

    @Test
    public void testSubsetEmpty1() throws Exception {

	Date timeStart = new Date(getOriginalStart() - ONE_DAY_IN_MILLISECONDS); // original time start
	Date timeEnd = new Date(getOriginalStart() - ONE_DAY_IN_MILLISECONDS + ONE_HOUR_IN_MILLISECONDS); // 0 values

	testSubset(timeStart, timeEnd, 0, ValidationResult.VALIDATION_SUCCESSFUL);
	testSubset(timeStart, timeEnd, 15, ValidationResult.VALIDATION_FAILED);

    }

    @Test
    public void testSubsetEmpty2() throws Exception {

	Date timeStart = new Date(getOriginalEnd() + ONE_DAY_IN_MILLISECONDS); // original time start
	Date timeEnd = new Date(getOriginalEnd() + ONE_DAY_IN_MILLISECONDS + ONE_HOUR_IN_MILLISECONDS); // 0 values

	testSubset(timeStart, timeEnd, 0, ValidationResult.VALIDATION_SUCCESSFUL);
	testSubset(timeStart, timeEnd, 15, ValidationResult.VALIDATION_FAILED);

    }

    @Test
    public void testSubsetUpset1() throws Exception {

	Date timeStart = new Date(getOriginalStart()); // original time start
	Date timeEnd = new Date(getOriginalEnd() + ONE_DAY_IN_MILLISECONDS); // -> 49 values

	testSubset(timeStart, timeEnd, getOriginalSize(), ValidationResult.VALIDATION_SUCCESSFUL);
	testSubset(timeStart, timeEnd, 15, ValidationResult.VALIDATION_FAILED);

    }

    @Test
    public void testSubsetUpset2() throws Exception {

	Date timeStart = new Date(getOriginalStart() - ONE_DAY_IN_MILLISECONDS); // original time start
	Date timeEnd = new Date(getOriginalEnd()); // -> 49 values

	testSubset(timeStart, timeEnd, getOriginalSize(), ValidationResult.VALIDATION_SUCCESSFUL);
	testSubset(timeStart, timeEnd, 15, ValidationResult.VALIDATION_FAILED);

    }

    @Test
    public void testSubsetUpset3() throws Exception {

	Date timeStart = new Date(getOriginalStart() - ONE_DAY_IN_MILLISECONDS); // original time start
	Date timeEnd = new Date(getOriginalEnd() + ONE_DAY_IN_MILLISECONDS); // -> 49 values

	testSubset(timeStart, timeEnd, getOriginalSize(), ValidationResult.VALIDATION_SUCCESSFUL);
	testSubset(timeStart, timeEnd, 15, ValidationResult.VALIDATION_FAILED);

    }

    @Test
    public void testSubsetPoint1() throws Exception {

	Date timeStart = new Date(getOriginalStart()); // original time start
	Date timeEnd = new Date(getOriginalStart()); // original time start -> 1 value

	testSubset(timeStart, timeEnd, 1, ValidationResult.VALIDATION_SUCCESSFUL);
	testSubset(timeStart, timeEnd, 15, ValidationResult.VALIDATION_FAILED);

    }

    @Test
    public void testSubsetPoint2() throws Exception {

	Date timeStart = new Date(getOriginalEnd()); // original time start
	Date timeEnd = new Date(getOriginalEnd()); // original time start -> 1 value

	testSubset(timeStart, timeEnd, 1, ValidationResult.VALIDATION_SUCCESSFUL);
	testSubset(timeStart, timeEnd, 15, ValidationResult.VALIDATION_FAILED);

    }

    @Test
    public void testSubsetPoint3() throws Exception {

	Date timeStart = new Date(getOriginalStart() + ONE_HOUR_IN_MILLISECONDS); // original time start
	Date timeEnd = new Date(getOriginalStart() + ONE_HOUR_IN_MILLISECONDS); // original time start -> 1 value

	testSubset(timeStart, timeEnd, 1, ValidationResult.VALIDATION_SUCCESSFUL);
	testSubset(timeStart, timeEnd, 15, ValidationResult.VALIDATION_FAILED);

    }

    @Test
    public void testSubset1() throws Exception {

	Date timeStart = new Date(getOriginalStart()); // original time start
	Date timeEnd = new Date(getOriginalStart() + ONE_HOUR_IN_MILLISECONDS); // original time start + an hour -> 3 values

	testSubset(timeStart, timeEnd, 3, ValidationResult.VALIDATION_SUCCESSFUL);
	testSubset(timeStart, timeEnd, 15, ValidationResult.VALIDATION_FAILED);

    }

    @Test
    public void testSubset2() throws Exception {

	Date timeStart = new Date(getOriginalStart() + ONE_HOUR_IN_MILLISECONDS); // original time start
	Date timeEnd = new Date(getOriginalStart() + ONE_HOUR_IN_MILLISECONDS + ONE_HOUR_IN_MILLISECONDS); // original time

	testSubset(timeStart, timeEnd, 3, ValidationResult.VALIDATION_SUCCESSFUL);
	testSubset(timeStart, timeEnd, 15, ValidationResult.VALIDATION_FAILED);

    }

    @Test
    public void testSubset3() throws Exception {

	Date timeStart = new Date(getOriginalEnd() - ONE_HOUR_IN_MILLISECONDS); // original time start
	Date timeEnd = new Date(getOriginalEnd()); // original time start + an hour -> 3 values

	testSubset(timeStart, timeEnd, 3, ValidationResult.VALIDATION_SUCCESSFUL);
	testSubset(timeStart, timeEnd, 15, ValidationResult.VALIDATION_FAILED);

    }

}
