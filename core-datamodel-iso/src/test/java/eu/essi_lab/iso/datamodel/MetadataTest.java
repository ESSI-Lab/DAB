package eu.essi_lab.iso.datamodel;

import static org.junit.Assert.fail;

import java.io.InputStream;
import java.lang.reflect.Constructor;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * Tests constructors, get and set methods of the {@link ISOMetadata} sub classes.
 * 
 * @author boldrini
 * @param <S> the {@link ISOMetadata} sub class to test
 * @param <T> the generic type for the {@link ISOMetadata}
 */
public abstract class MetadataTest<S extends ISOMetadata<T>, T> {

    private Class<? extends S> clazz;
    private Class<? extends T> innerClazz;

    /**
     * Subclasses may override this method to initialize objects prior to the test.
     * 
     * @throws Exception in case an exception occurred during initialization
     */
    @Before
    public void init() throws Exception {
    }

    /**
     * Sets the {@link ISOMetadata} properties using the {@link ISOMetadata} set methods
     * 
     * @param metadata
     */
    public abstract void setProperties(S metadata);

    /**
     * Checks that the given {@link ISOMetadata} has the properties set by the
     * {@link MetadataTest#setProperties(ISOMetadata)} method using the {@link ISOMetadata} get methods
     * 
     * @param metadata
     */
    public abstract void checkProperties(S metadata);

    /**
     * Resets all the properties of the given {@link ISOMetadata} to null
     * 
     * @param metadata
     */
    public abstract void clearProperties(S metadata);

    /**
     * Initial check that all the properties are null
     * 
     * @param metadata
     */
    public abstract void checkNullProperties(S metadata);

    public MetadataTest(Class<? extends S> clazz, Class<? extends T> innerClazz) {
	this.clazz = clazz;
	this.innerClazz = innerClazz;
    }

    @Test
    public void test() {

	try {

	    T initialInnerObject = innerClazz.newInstance();
	    S originalMetadata = clazz.newInstance();
	    // sets the attributes using the set methods
	    originalMetadata.setElementType(initialInnerObject);

	    // initial check for null properties
	    checkNullProperties(originalMetadata);

	    // sets the properties
	    setProperties(originalMetadata);
	    // tests equality using the get methods
	    checkProperties(originalMetadata);

	    // Marshalles the filled object
	    InputStream stream = originalMetadata.asStream();

	    // checks input stream constructor
	    Constructor<? extends S> streamConstructor = clazz.getConstructor(InputStream.class);
	    S newMetadata = streamConstructor.newInstance(stream);
	    checkEquality(originalMetadata, newMetadata);

	    // checks Java Object constructor
	    Constructor<? extends S> classConstructor = clazz.getConstructor(innerClazz);
	    S newMetadata2 = classConstructor.newInstance(newMetadata.getElementType());
	    checkEquality(originalMetadata, newMetadata2);

	    // clear properties on original object
	    clearProperties(originalMetadata);
	    // final check for null properties on original object
	    checkNullProperties(originalMetadata);

	    // clear properties on new object
	    clearProperties(newMetadata);
	    // final check for null properties on new object
	    checkNullProperties(newMetadata);

	    // check equality on null set objects
	    TestCase.assertEquals(originalMetadata, newMetadata);

	} catch (Exception ex) {

	    ex.printStackTrace();
	    fail("Exception thrown");
	}
    }

    private void checkEquality(ISOMetadata<T> metadata, S finalMetadata) {
	// tests equality using JAXB
	TestCase.assertEquals(metadata, finalMetadata);
	// tests equality using the get methods
	checkProperties(finalMetadata);

    }

}
