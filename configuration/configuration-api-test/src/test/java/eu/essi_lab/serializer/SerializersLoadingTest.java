/**
 * 
 */
package eu.essi_lab.serializer;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.shared.SharedContent.SharedContentType;
import eu.essi_lab.shared.serializer.GSResourceSerializer;
import eu.essi_lab.shared.serializer.GenericSerializer;
import eu.essi_lab.shared.serializer.JSONSerializer;
import eu.essi_lab.shared.serializer.SharedContentSerializer;
import eu.essi_lab.shared.serializer.SharedContentSerializers;

/**
 * @author Fabrizio
 */
public class SerializersLoadingTest {

    /**
     * This test is here since it can require several dependencies to work
     */
    @Test
    public void test() {

	{

	    SharedContentSerializer serializer = SharedContentSerializers.getSerializer(SharedContentType.GS_RESOURCE_TYPE);

	    Assert.assertEquals(serializer.getClass(), GSResourceSerializer.class);
	}

	{

	    SharedContentSerializer serializer = SharedContentSerializers.getSerializer(SharedContentType.JSON_TYPE);

	    Assert.assertEquals(serializer.getClass(), JSONSerializer.class);
	}

	{

	    SharedContentSerializer serializer = SharedContentSerializers.getSerializer(SharedContentType.GENERIC_TYPE);

	    Assert.assertEquals(serializer.getClass(), GenericSerializer.class);
	}

    }
}
