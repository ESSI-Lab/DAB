package eu.essi_lab.lib.net.utils.s3.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * @author Fabrizio
 */
public class S3TransferManagerExternalTestIT {

    /**
     * 
     */
    private static final String TEST_BUCKET_NAME = "s3-transfer-manager-test";

    @Before
    public void deleteAll() {

	S3TransferWrapper manager = createManager();

	List<S3Object> objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	objectsSummaries.stream().map(s -> s.key()).forEach(key -> manager.deleteObject(TEST_BUCKET_NAME, key));
    }

    /**
     * @throws IOException
     */
    @Test
    public void uploadAndDeleteFileTest() throws Exception {

	uploadAndDeleteFileTest(true);
	
	uploadAndDeleteFileTest(false);

	
    }

    /**
     * @throws IOException
     */
    private void uploadAndDeleteFileTest(boolean publicACLRead) throws Exception {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("s3tman/file1.txt");
	File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".txt");
	FileUtils.copyInputStreamToFile(stream, tempFile);

	S3TransferWrapper manager = createManager();

	if (publicACLRead) {
	    manager.setACLPublicRead(true);
	}

	//
	//
	//

	List<S3Object> objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(0, objectsSummaries.size());

	Assert.assertEquals(0, manager.countObjects(TEST_BUCKET_NAME, null));

	//
	//
	//

	manager.uploadFile(tempFile.getPath(), TEST_BUCKET_NAME, null);

	objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(1, objectsSummaries.size());

	String key = objectsSummaries.get(0).key();
	Assert.assertEquals(tempFile.getName(), key);

	//
	//
	//

	URL objectURL = manager.getObjectURL(TEST_BUCKET_NAME, key);

	boolean connectivity = HttpConnectionUtils.checkConnectivity(objectURL.toString());

	Assert.assertEquals(connectivity, publicACLRead);

	//
	//
	//

	manager.deleteObject(TEST_BUCKET_NAME, tempFile.getName());

	objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(0, objectsSummaries.size());

	//
	//
	//

	stream.close();
	tempFile.delete();
    }

    /**
     * @throws IOException
     */
    @Test
    public void uploadAndDeleteFileWithGivenNameTest() throws IOException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("s3tman/file1.txt");
	File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".txt");
	FileUtils.copyInputStreamToFile(stream, tempFile);

	String objectName = "testObjectName.txt";

	S3TransferWrapper manager = createManager();

	//
	//
	//

	List<S3Object> objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(0, objectsSummaries.size());

	Assert.assertEquals(0, manager.countObjects(TEST_BUCKET_NAME, null));

	manager.uploadFile(tempFile.getPath(), TEST_BUCKET_NAME, objectName);

	objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(1, objectsSummaries.size());

	Assert.assertEquals(1, manager.countObjects(TEST_BUCKET_NAME, null));

	//
	//
	//

	String key = objectsSummaries.get(0).key();
	Assert.assertEquals(objectName, key);

	//
	//
	//

	manager.deleteObject(TEST_BUCKET_NAME, objectName);

	objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(0, objectsSummaries.size());

	Assert.assertEquals(0, manager.countObjects(TEST_BUCKET_NAME, null));

	//
	//
	//

	stream.close();
	tempFile.delete();
    }

    @Test
    public void uploadFileListAndDeleteAllTest() throws Exception {

	uploadFileListAndDeleteAllTest(false);

	uploadFileListAndDeleteAllTest(true);
    }

    /**
     * @param publicACLRead
     * @throws IOException
     */
    private void uploadFileListAndDeleteAllTest(boolean publicACLRead) throws Exception {

	List<File> filesList = createFileList();

	S3TransferWrapper manager = createManager();

	if (publicACLRead) {

	    manager.setACLPublicRead(publicACLRead);
	}

	//
	//
	//

	List<S3Object> objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(0, objectsSummaries.size());

	Assert.assertEquals(0, manager.countObjects(TEST_BUCKET_NAME, null));

	//
	//
	//

	manager.uploadFileList(filesList, TEST_BUCKET_NAME);

	objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(filesList.size(), objectsSummaries.size());

	Assert.assertEquals(filesList.size(), manager.countObjects(TEST_BUCKET_NAME, null));

	//
	//
	//

	objectsSummaries.stream().map(s -> s.key()).forEach(key -> {

	    URL objectURL = manager.getObjectURL(TEST_BUCKET_NAME, key);

	    try {
		boolean connectivity = HttpConnectionUtils.checkConnectivity(objectURL.toString());
		Assert.assertEquals(connectivity, publicACLRead);

	    } catch (URISyntaxException e) {
		e.printStackTrace();
	    }
	});

	//
	//
	//

	objectsSummaries.stream().map(s -> s.key()).forEach(key -> manager.deleteObject(TEST_BUCKET_NAME, key));

	objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(0, objectsSummaries.size());

	Assert.assertEquals(0, manager.countObjects(TEST_BUCKET_NAME, null));

	//
	//
	//

	filesList.forEach(f -> f.delete());
    }

    @Test
    public void uploadFileListWithPrefixAndDeleteAllTest() throws IOException {

	List<File> filesList = createFileList();

	S3TransferWrapper manager = createManager();

	String prefixName = "temp-folder";

	//
	//
	//

	List<S3Object> objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(0, objectsSummaries.size());

	Assert.assertEquals(0, manager.countObjects(TEST_BUCKET_NAME, prefixName));

	//
	//
	//

	manager.uploadFileList(filesList, TEST_BUCKET_NAME, prefixName);

	objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(filesList.size(), objectsSummaries.size());

	Assert.assertEquals(filesList.size(), manager.countObjects(TEST_BUCKET_NAME, prefixName));

	objectsSummaries.stream().map(s -> s.key()).forEach(key -> Assert.assertTrue(key.startsWith(prefixName)));

	//
	//
	//

	objectsSummaries.stream().map(s -> s.key()).forEach(key -> manager.deleteObject(TEST_BUCKET_NAME, key));

	objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(0, objectsSummaries.size());

	Assert.assertEquals(0, manager.countObjects(TEST_BUCKET_NAME, prefixName));

	//
	//
	//

	filesList.forEach(f -> f.delete());
    }

    @Test
    public void uploadDirAndDeleteAllTest() throws IOException {

	uploadDirAndDeleteAllTest(false);

	uploadDirAndDeleteAllTest(true);
    }

    /**
     * @throws IOException
     */
    private void uploadDirAndDeleteAllTest(boolean publicACLRead) throws IOException {

	List<File> filesList = createFileList();

	File tempFolder = copyToTempFolder(filesList);

	S3TransferWrapper manager = createManager();

	if (publicACLRead) {

	    manager.setACLPublicRead(publicACLRead);
	}

	//
	//
	//

	List<S3Object> objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(0, objectsSummaries.size());

	//
	//
	//

	manager.uploadDir(tempFolder.getAbsolutePath(), TEST_BUCKET_NAME, false);

	objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(filesList.size(), objectsSummaries.size());

	//
	//
	//

	objectsSummaries.stream().map(s -> s.key()).forEach(key -> {

	    URL objectURL = manager.getObjectURL(TEST_BUCKET_NAME, key);

	    try {
		boolean connectivity = HttpConnectionUtils.checkConnectivity(objectURL.toString());
		if (connectivity!=publicACLRead) {
		    System.out.println();
		}
		Assert.assertEquals(connectivity, publicACLRead);

	    } catch (URISyntaxException e) {
		e.printStackTrace();
	    }
	});

	//
	//
	//

	objectsSummaries.stream().map(s -> s.key()).forEach(key -> manager.deleteObject(TEST_BUCKET_NAME, key));

	objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(0, objectsSummaries.size());

	//
	//
	//

	filesList.forEach(f -> f.delete());
	Arrays.asList(tempFolder.listFiles()).forEach(f -> f.delete());
    }

    @Test
    public void uploadDirRecursiveAndDeleteAllTest() throws IOException {

	// TODO
    }

    @Test
    public void uploadDirRecursiveWithPrefixAndDeleteAllTest() throws IOException {

	// TODO
    }

    @Test
    public void uploadDirWithPrefixAndDeleteAllTest() throws IOException {

	String prefixName = "temp-folder";

	List<File> filesList = createFileList();

	File tempFolder = copyToTempFolder(filesList);

	//
	//
	//

	S3TransferWrapper manager = createManager();

	//
	//
	//

	List<S3Object> objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(0, objectsSummaries.size());

	//
	//
	//

	manager.uploadDir(tempFolder.getAbsolutePath(), TEST_BUCKET_NAME, prefixName, false);

	objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(filesList.size(), objectsSummaries.size());

	//
	//
	//

	objectsSummaries.stream().map(s -> s.key()).forEach(key -> Assert.assertTrue(key.startsWith(prefixName)));

	//
	//
	//

	objectsSummaries.stream().map(s -> s.key()).forEach(key -> manager.deleteObject(TEST_BUCKET_NAME, key));

	objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	Assert.assertEquals(0, objectsSummaries.size());

	//
	//
	//

	filesList.forEach(f -> f.delete());
	Arrays.asList(tempFolder.listFiles()).forEach(f -> f.delete());
    }

    @Test
    public void listBucketsTest() {

	S3TransferWrapper manager = createManager();

	List<software.amazon.awssdk.services.s3.model.Bucket> listBuckets = manager.listBuckets();

	Assert.assertFalse(listBuckets.isEmpty());

	Assert.assertTrue(listBuckets.stream().anyMatch(b -> b.name().equals(TEST_BUCKET_NAME)));
    }

    /**
     * @param filesList
     */
    private File copyToTempFolder(List<File> filesList) {

	File tempDirectory = IOStreamUtils.getUserTempDirectory();
	File s3tmanTestFolder = new File(tempDirectory + File.separator + "s3tman");
	s3tmanTestFolder.mkdirs();
	File[] files = s3tmanTestFolder.listFiles();
	for (File file : files) {
	    file.delete();
	}

	filesList.forEach(file -> {

	    try {
		FileUtils.copyFileToDirectory(file, s3tmanTestFolder);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	});

	return s3tmanTestFolder;
    }

    /**
     * @return
     */
    private List<File> createFileList() {

	return Arrays.asList(//
		getClass().getClassLoader().getResourceAsStream("s3tman/file1.txt"), //
		getClass().getClassLoader().getResourceAsStream("s3tman/file2.txt"), //
		getClass().getClassLoader().getResourceAsStream("s3tman/file3.txt"), //
		getClass().getClassLoader().getResourceAsStream("s3tman/file4.txt"), //
		getClass().getClassLoader().getResourceAsStream("s3tman/file5.txt")//

	).stream().map(s -> {

	    try {

		File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".txt");
		FileUtils.copyInputStreamToFile(s, tempFile);
		s.close();

		return tempFile;

	    } catch (IOException e) {
		e.printStackTrace();
	    }

	    return null;
	}).collect(Collectors.toList());
    }

    /**
     * @return
     */
    private S3TransferWrapper createManager() {

	String accessKey = System.getProperty("accessKey");
	String secretKey = System.getProperty("secretKey");
	String endpoint = System.getProperty("endpoint");

	GSLoggerFactory.getLogger(getClass()).info("Access key: "+accessKey);
	GSLoggerFactory.getLogger(getClass()).info("Secret key: "+secretKey);
	GSLoggerFactory.getLogger(getClass()).info("Endpoint: "+endpoint);

	S3TransferWrapper manager = new S3TransferWrapper();
	manager.setAccessKey(accessKey);
	manager.setSecretKey(secretKey);
	if (endpoint!=null && !endpoint.isEmpty()) {
	    manager.setEndpoint(endpoint);
	}
	
	Assert.assertEquals(accessKey, manager.getAccessKey());
	Assert.assertEquals(secretKey, manager.getSecretKey());

	return manager;
    }
}
