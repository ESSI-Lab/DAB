package eu.essi_lab.cfga.option.test;

import static org.junit.Assert.fail;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting.JobStoreType;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class Base64Test {

    @SuppressWarnings("serial")
    private class PartiallySerializableClass implements Serializable {

	@SuppressWarnings("unused")
	private String string = "";
	@SuppressWarnings("unused")
	private GSResource dataset = new Dataset();
    }

    /**
     * 
     */
    @Test
    public void test() {

	//
	// Base 64 encoding/decoding test 1
	//
	StorageInfo storageUri = new StorageInfo();
	storageUri.setIdentifier("storageFolder");
	storageUri.setPassword("password");
	storageUri.setName("dataBaseName");
	storageUri.setUri("url");
	storageUri.setUser("user");

	Option<StorageInfo> option = new Option<>(StorageInfo.class);
	option.setBase64EncodedValue();
	option.setValue(storageUri);

	testBase64(option, storageUri);
	testBase64(new Option<StorageInfo>(option.getObject()), storageUri);
	testBase64(new Option<StorageInfo>(option.getObject().toString()), storageUri);

	option = new Option<>(StorageInfo.class);
	option.setBase64EncodedValue();
	option.addValue(storageUri);

	testBase64(option, storageUri);
	testBase64(new Option<StorageInfo>(option.getObject()), storageUri);
	testBase64(new Option<StorageInfo>(option.getObject().toString()), storageUri);

	option.addValue(storageUri);
	option.addValue(storageUri);

	testBase64(option, storageUri);
	testBase64(new Option<StorageInfo>(option.getObject()), storageUri);
	testBase64(new Option<StorageInfo>(option.getObject().toString()), storageUri);

	//
	// Base 64 test with partially serializable class
	//

	PartiallySerializableClass partiallySerializableClass = new PartiallySerializableClass();

	Option<PartiallySerializableClass> partiallySerializableOption = new Option<>(PartiallySerializableClass.class);
	partiallySerializableOption.setBase64EncodedValue();

	try {
	    partiallySerializableOption.setValue(partiallySerializableClass);
	    fail("Exception not thrown");

	} catch (Exception ex) {
	    // OK
	}

	//
	// Base 64 test with non serializable class
	//

	Dataset dataset = new Dataset();
	Option<Dataset> datasetOption = new Option<>(Dataset.class);

	datasetOption.setBase64EncodedValue();

	try {
	    datasetOption.setValue(dataset);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    // OK
	}

	try {
	    datasetOption.addValue(dataset);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    // OK
	}

	//
	// ENUM Base 64 encoding/decoding test
	//

	Option<JobStoreType> jobStoreOption = new Option<>(JobStoreType.class);
	jobStoreOption.setBase64EncodedValue();

	jobStoreOption.setValue(JobStoreType.PERSISTENT);

	testJobStoreEnum(jobStoreOption, JobStoreType.PERSISTENT);
	testJobStoreEnum(new Option<JobStoreType>(jobStoreOption.getObject()), JobStoreType.PERSISTENT);
	testJobStoreEnum(new Option<JobStoreType>(jobStoreOption.getObject().toString()), JobStoreType.PERSISTENT);

	jobStoreOption.setValue(JobStoreType.VOLATILE);

	testJobStoreEnum(jobStoreOption, JobStoreType.VOLATILE);
	testJobStoreEnum(new Option<JobStoreType>(jobStoreOption.getObject()), JobStoreType.VOLATILE);
	testJobStoreEnum(new Option<JobStoreType>(jobStoreOption.getObject().toString()), JobStoreType.VOLATILE);

	//
	// ENUM option not Base 64 encoded
	//

	jobStoreOption = new Option<>(JobStoreType.class);
	jobStoreOption.setValue(JobStoreType.PERSISTENT);

	testJobStoreEnum(jobStoreOption, JobStoreType.PERSISTENT);

	testJobStoreEnum(new Option<JobStoreType>(jobStoreOption.getObject()), JobStoreType.PERSISTENT);

	testJobStoreEnum(new Option<JobStoreType>(jobStoreOption.getObject().toString()), JobStoreType.PERSISTENT);

    }

    private void testJobStoreEnum(Option<JobStoreType> option, JobStoreType type) {

	Assert.assertEquals(type, option.getValue());
    }

    private void testBase64(Option<StorageInfo> option, StorageInfo storageUri) {

	StorageInfo value = option.getValue();

	Assert.assertEquals(value, storageUri);
    }

}
