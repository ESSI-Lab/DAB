package eu.essi_lab.shared.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.UUID;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;
import eu.essi_lab.shared.serializer.GenericSerializer;

@Ignore
public class GenericSerializerFileTest {

    /**
     * @throws Exception
     */
    @Test
    public void test() throws Exception {

	SharedContent<File> sharedContent = new SharedContent<>();

	{

	    sharedContent.setIdentifier(UUID.randomUUID().toString());

	    sharedContent.setType(SharedContentType.GENERIC_TYPE);

	    File file = File.createTempFile("temp", null);

	    JSONObject object = new JSONObject();
	    object.put("key", "value");

	    FileOutputStream stream = new FileOutputStream(file);
	    stream.write(object.toString().getBytes(Charset.forName("UTF-8")));
	    stream.flush();
	    stream.close();

	    sharedContent.setContent(file);
	}

	//
	//
	//

	{

	    GenericSerializer serializer = new GenericSerializer();
	    InputStream toStream = serializer.toStream(sharedContent);

	    @SuppressWarnings("rawtypes")
	    SharedContent fromStream = serializer.fromStream(null, toStream);

	    File content = (File) fromStream.getContent();

	    FileInputStream fileInputStream = new FileInputStream(content);

	    String asUTF8String = IOStreamUtils.asUTF8String(fileInputStream);

	    JSONObject jsonObject = new JSONObject(asUTF8String);
	    Assert.assertEquals("value", jsonObject.get("key"));
	}
    }
}
