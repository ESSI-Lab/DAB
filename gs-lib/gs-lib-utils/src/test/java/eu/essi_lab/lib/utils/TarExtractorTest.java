package eu.essi_lab.lib.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.utils.zip.TarExtractor;

/**
 * @author Fabrizio
 */
public class TarExtractorTest {

    /**
     * out-2 contains also a folder
     */
    private static final String TEST_FILE_NAME = "out-2.tar.gz";

    @Test
    public void extractFromRemoteInputStreamWithTimeoutTest() throws IOException {

	File destination = new File(IOStreamUtils.getUserTempDirectory() + File.separator + "tar-extractor-test");
	destination.mkdirs();

	//
	//
	//

	URL url = new URL("https://essi-lab.eu/projects/demo/example.tar.gz");

	InputStream stream = url.openStream();

	TarExtractor extractor = new TarExtractor();

	Assert.assertNotNull(extractor.getFileNameMapper());
	Assert.assertFalse(extractor.getMaxEntries().isPresent());

	//
	//
	//

	extractor.setFileNameMapper(fileName -> fileName.replace(":", "_"));
	extractor.setTimeOut(TimeUnit.SECONDS, 5);

	try {
	    extractor.extract(stream, destination);

	} catch (TimeoutException e) {

	    return;
	}

	Assert.fail("Timeout exception not thrown");

    }

    @Test
    public void extractFromInputStreamTest() throws TimeoutException {

	File destination = new File(IOStreamUtils.getUserTempDirectory() + File.separator + "tar-extractor-test");
	destination.mkdirs();

	//
	//
	//

	InputStream stream = getClass().getClassLoader().getResourceAsStream(TEST_FILE_NAME);

	TarExtractor extractor = new TarExtractor();

	Assert.assertNotNull(extractor.getFileNameMapper());
	Assert.assertFalse(extractor.getMaxEntries().isPresent());

	//
	//
	//

	List<File> list = extractor.extract(stream, destination);

	Assert.assertEquals(6, list.size());

	List<String> namesList = list.stream().map(f -> f.getName()).sorted().collect(Collectors.toList());
	for (int i = 0; i < namesList.size(); i++) {

	    Assert.assertEquals("file" + (i + 1) + ".txt", namesList.get(i));
	}

	//
	//
	//

	Arrays.asList(destination.listFiles()).forEach(f -> f.delete());
    }

    @Test
    public void extractFromInputStreamMaxEntriesTest() throws TimeoutException {

	File destination = new File(IOStreamUtils.getUserTempDirectory() + File.separator + "tar-extractor-test");
	destination.mkdirs();

	//
	//
	//

	InputStream stream = getClass().getClassLoader().getResourceAsStream(TEST_FILE_NAME);

	TarExtractor extractor = new TarExtractor();

	extractor.setMaxEntries(1);

	Assert.assertNotNull(extractor.getFileNameMapper());
	Assert.assertEquals(new Integer(1), extractor.getMaxEntries().get());

	//
	//
	//

	List<File> list = extractor.extract(stream, destination);

	Assert.assertEquals(1, list.size());

	List<String> namesList = list.stream().map(f -> f.getName()).sorted().collect(Collectors.toList());
	for (int i = 0; i < namesList.size(); i++) {

	    Assert.assertEquals("file" + (i + 1) + ".txt", namesList.get(i));
	}

	//
	//
	//

	Arrays.asList(destination.listFiles()).forEach(f -> f.delete());
    }

    @Test
    public void extractFromInputStreamMaxEntriesConstructorTest() throws TimeoutException {

	File destination = new File(IOStreamUtils.getUserTempDirectory() + File.separator + "tar-extractor-test");
	destination.mkdirs();

	//
	//
	//

	InputStream stream = getClass().getClassLoader().getResourceAsStream(TEST_FILE_NAME);

	TarExtractor extractor = new TarExtractor(1);

	Assert.assertNotNull(extractor.getFileNameMapper());
	Assert.assertEquals(new Integer(1), extractor.getMaxEntries().get());

	//
	//
	//

	List<File> list = extractor.extract(stream, destination);

	Assert.assertEquals(1, list.size());

	List<String> namesList = list.stream().map(f -> f.getName()).sorted().collect(Collectors.toList());
	for (int i = 0; i < namesList.size(); i++) {

	    Assert.assertEquals("file" + (i + 1) + ".txt", namesList.get(i));
	}

	//
	//
	//

	Arrays.asList(destination.listFiles()).forEach(f -> f.delete());
    }

    @Test
    public void extractFromInputStreamWithFileNameMapperTest() throws TimeoutException {

	File destination = new File(IOStreamUtils.getUserTempDirectory() + File.separator + "tar-extractor-test");
	destination.mkdirs();

	//
	//
	//

	InputStream stream = getClass().getClassLoader().getResourceAsStream(TEST_FILE_NAME);

	TarExtractor extractor = new TarExtractor();

	Assert.assertNotNull(extractor.getFileNameMapper());
	Assert.assertFalse(extractor.getMaxEntries().isPresent());

	//
	//
	//

	extractor.setFileNameMapper(f -> f.replace("txt", "xxx"));

	List<File> list = extractor.extract(stream, destination);

	Assert.assertEquals(6, list.size());

	List<String> namesList = list.stream().map(f -> f.getName()).sorted().collect(Collectors.toList());
	for (int i = 0; i < namesList.size(); i++) {

	    Assert.assertEquals("file" + (i + 1) + ".xxx", namesList.get(i));
	}

	//
	//
	//

	Arrays.asList(destination.listFiles()).forEach(f -> f.delete());
    }

    @Test
    public void extractFromInputStreamWithFileNameMapperConstructorTest() throws TimeoutException {

	File destination = new File(IOStreamUtils.getUserTempDirectory() + File.separator + "tar-extractor-test");
	destination.mkdirs();

	//
	//
	//

	InputStream stream = getClass().getClassLoader().getResourceAsStream(TEST_FILE_NAME);

	TarExtractor extractor = new TarExtractor(f -> f.replace("txt", "xxx"));

	Assert.assertNotNull(extractor.getFileNameMapper());
	Assert.assertFalse(extractor.getMaxEntries().isPresent());

	//
	//
	//

	List<File> list = extractor.extract(stream, destination);

	Assert.assertEquals(6, list.size());

	List<String> namesList = list.stream().map(f -> f.getName()).sorted().collect(Collectors.toList());
	for (int i = 0; i < namesList.size(); i++) {

	    Assert.assertEquals("file" + (i + 1) + ".xxx", namesList.get(i));
	}

	//
	//
	//

	Arrays.asList(destination.listFiles()).forEach(f -> f.delete());
    }

    @Test
    public void extractFromFileTest() throws IOException, TimeoutException {

	File destination = new File(IOStreamUtils.getUserTempDirectory() + File.separator + "tar-extractor-test");
	destination.mkdirs();

	//
	//
	//

	InputStream stream = getClass().getClassLoader().getResourceAsStream(TEST_FILE_NAME);
	File tempGzFile = File.createTempFile("gz-test", ".gz");
	FileUtils.copyInputStreamToFile(stream, tempGzFile);

	TarExtractor extractor = new TarExtractor();

	Assert.assertNotNull(extractor.getFileNameMapper());
	Assert.assertFalse(extractor.getMaxEntries().isPresent());

	//
	//
	//

	List<File> list = extractor.extract(tempGzFile, destination);

	Assert.assertEquals(6, list.size());

	List<String> namesList = list.stream().map(f -> f.getName()).sorted().collect(Collectors.toList());
	for (int i = 0; i < namesList.size(); i++) {

	    Assert.assertEquals("file" + (i + 1) + ".txt", namesList.get(i));
	}

	//
	//
	//

	tempGzFile.delete();
	Arrays.asList(destination.listFiles()).forEach(f -> f.delete());
    }

    @Test
    public void extractFromFileMaxEntriesTest() throws IOException, TimeoutException {

	File destination = new File(IOStreamUtils.getUserTempDirectory() + File.separator + "tar-extractor-test");
	destination.mkdirs();

	//
	//
	//

	InputStream stream = getClass().getClassLoader().getResourceAsStream(TEST_FILE_NAME);
	File tempGzFile = File.createTempFile("gz-test", ".gz");
	FileUtils.copyInputStreamToFile(stream, tempGzFile);

	TarExtractor extractor = new TarExtractor();

	extractor.setMaxEntries(1);

	Assert.assertNotNull(extractor.getFileNameMapper());
	Assert.assertEquals(new Integer(1), extractor.getMaxEntries().get());

	//
	//
	//
	List<File> list = extractor.extract(tempGzFile, destination);

	Assert.assertEquals(1, list.size());

	List<String> namesList = list.stream().map(f -> f.getName()).sorted().collect(Collectors.toList());
	for (int i = 0; i < namesList.size(); i++) {

	    Assert.assertEquals("file" + (i + 1) + ".txt", namesList.get(i));
	}

	//
	//
	//

	tempGzFile.delete();
	Arrays.asList(destination.listFiles()).forEach(f -> f.delete());
    }

    @Test
    public void extractFromFileMaxEntriesConstructorTest() throws IOException, TimeoutException {

	File destination = new File(IOStreamUtils.getUserTempDirectory() + File.separator + "tar-extractor-test");
	destination.mkdirs();

	//
	//
	//

	InputStream stream = getClass().getClassLoader().getResourceAsStream(TEST_FILE_NAME);
	File tempGzFile = File.createTempFile("gz-test", ".gz");
	FileUtils.copyInputStreamToFile(stream, tempGzFile);

	TarExtractor extractor = new TarExtractor(1);

	Assert.assertNotNull(extractor.getFileNameMapper());
	Assert.assertEquals(new Integer(1), extractor.getMaxEntries().get());

	//
	//
	//

	List<File> list = extractor.extract(tempGzFile, destination);

	Assert.assertEquals(1, list.size());

	List<String> namesList = list.stream().map(f -> f.getName()).sorted().collect(Collectors.toList());
	for (int i = 0; i < namesList.size(); i++) {

	    Assert.assertEquals("file" + (i + 1) + ".txt", namesList.get(i));
	}

	//
	//
	//

	tempGzFile.delete();
	Arrays.asList(destination.listFiles()).forEach(f -> f.delete());
    }

    @Test
    public void extractFromFileWithFileNameMapperTest() throws IOException, TimeoutException {

	File destination = new File(IOStreamUtils.getUserTempDirectory() + File.separator + "tar-extractor-test");
	destination.mkdirs();

	//
	//
	//

	InputStream stream = getClass().getClassLoader().getResourceAsStream(TEST_FILE_NAME);
	File tempGzFile = File.createTempFile("gz-test", ".gz");
	FileUtils.copyInputStreamToFile(stream, tempGzFile);

	TarExtractor extractor = new TarExtractor();

	Assert.assertNotNull(extractor.getFileNameMapper());
	Assert.assertFalse(extractor.getMaxEntries().isPresent());

	//
	//
	//

	extractor.setFileNameMapper(f -> f.replace("txt", "xxx"));

	List<File> list = extractor.extract(tempGzFile, destination);

	Assert.assertEquals(6, list.size());

	List<String> namesList = list.stream().map(f -> f.getName()).sorted().collect(Collectors.toList());
	for (int i = 0; i < namesList.size(); i++) {

	    Assert.assertEquals("file" + (i + 1) + ".xxx", namesList.get(i));
	}

	//
	//
	//

	tempGzFile.delete();
	Arrays.asList(destination.listFiles()).forEach(f -> f.delete());
    }

    @Test
    public void extractFromFileWithFileNameMapperConstructorTest() throws IOException, TimeoutException {

	File destination = new File(IOStreamUtils.getUserTempDirectory() + File.separator + "tar-extractor-test");
	destination.mkdirs();

	//
	//
	//

	InputStream stream = getClass().getClassLoader().getResourceAsStream(TEST_FILE_NAME);
	File tempGzFile = File.createTempFile("gz-test", ".gz");
	FileUtils.copyInputStreamToFile(stream, tempGzFile);

	TarExtractor extractor = new TarExtractor(f -> f.replace("txt", "xxx"));

	Assert.assertNotNull(extractor.getFileNameMapper());
	Assert.assertFalse(extractor.getMaxEntries().isPresent());

	//
	//
	//

	List<File> list = extractor.extract(tempGzFile, destination);

	Assert.assertEquals(6, list.size());

	List<String> namesList = list.stream().map(f -> f.getName()).sorted().collect(Collectors.toList());
	for (int i = 0; i < namesList.size(); i++) {

	    Assert.assertEquals("file" + (i + 1) + ".xxx", namesList.get(i));
	}

	//
	//
	//

	tempGzFile.delete();
	Arrays.asList(destination.listFiles()).forEach(f -> f.delete());
    }

    @Test
    public void extractFromFileWithFileNameMapperAndMaxEntriesConstructorTest() throws IOException, TimeoutException {

	File destination = new File(IOStreamUtils.getUserTempDirectory() + File.separator + "tar-extractor-test");
	destination.mkdirs();

	//
	//
	//

	InputStream stream = getClass().getClassLoader().getResourceAsStream(TEST_FILE_NAME);
	File tempGzFile = File.createTempFile("gz-test", ".gz");
	FileUtils.copyInputStreamToFile(stream, tempGzFile);

	TarExtractor extractor = new TarExtractor(1, f -> f.replace("txt", "xxx"));

	Assert.assertNotNull(extractor.getFileNameMapper());
	Assert.assertEquals(new Integer(1), extractor.getMaxEntries().get());

	//
	//
	//

	List<File> list = extractor.extract(tempGzFile, destination);

	Assert.assertEquals(1, list.size());

	List<String> namesList = list.stream().map(f -> f.getName()).sorted().collect(Collectors.toList());
	for (int i = 0; i < namesList.size(); i++) {

	    Assert.assertEquals("file" + (i + 1) + ".xxx", namesList.get(i));
	}

	//
	//
	//

	tempGzFile.delete();
	Arrays.asList(destination.listFiles()).forEach(f -> f.delete());
    }

    @Test
    public void extractFromFileWithFileNameMapperAndMaxEntriesTest() throws IOException, TimeoutException {

	File destination = new File(IOStreamUtils.getUserTempDirectory() + File.separator + "tar-extractor-test");
	destination.mkdirs();

	//
	//
	//

	InputStream stream = getClass().getClassLoader().getResourceAsStream(TEST_FILE_NAME);
	File tempGzFile = File.createTempFile("gz-test", ".gz");
	FileUtils.copyInputStreamToFile(stream, tempGzFile);

	TarExtractor extractor = new TarExtractor();

	extractor.setFileNameMapper(f -> f.replace("txt", "xxx"));
	extractor.setMaxEntries(1);

	Assert.assertNotNull(extractor.getFileNameMapper());
	Assert.assertEquals(new Integer(1), extractor.getMaxEntries().get());

	//
	//
	//

	List<File> list = extractor.extract(tempGzFile, destination);

	Assert.assertEquals(1, list.size());

	List<String> namesList = list.stream().map(f -> f.getName()).sorted().collect(Collectors.toList());
	for (int i = 0; i < namesList.size(); i++) {

	    Assert.assertEquals("file" + (i + 1) + ".xxx", namesList.get(i));
	}

	//
	//
	//

	tempGzFile.delete();
	Arrays.asList(destination.listFiles()).forEach(f -> f.delete());
    }
}
