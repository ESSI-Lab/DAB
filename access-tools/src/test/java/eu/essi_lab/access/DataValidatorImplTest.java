package eu.essi_lab.access;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;

public class DataValidatorImplTest {

    private DataValidatorImpl validator;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void init() {
	this.validator = new DataValidatorImpl() {

	    @Override
	    public Provider getProvider() {
		return null;
	    }

	    @Override
	    public DataFormat getFormat() {
		return null;
	    }

	    @Override
	    public DataDescriptor readDataAttributes(DataObject dataObject) {
		return null;
	    }

	    @Override
	    public DataType getType() {
		return null;
	    }
	};
    }

    @Test
    public void test1() {
	validator.checkAxisConsistency(0, 3, 4l, 1, null);

    }

    @Test
    public void test2() {
	exception.expect(IllegalArgumentException.class);
	validator.checkAxisConsistency(0, 3, null, 1.1, null);

    }

    @Test
    public void test3() {
	exception.expect(IllegalArgumentException.class);
	validator.checkAxisConsistency(0, 3, 5l, 1, null);

    }

    @Test
    public void test4() {
	exception.expect(IllegalArgumentException.class);
	validator.checkAxisConsistency(0, 3, 5l, 1.1, null);

    }

}
