package eu.essi_lab.accessor.cmr;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.accessor.cmr.harvested.CMROriginalMDWrapper;
import eu.essi_lab.accessor.cmr.legacy.CMRCollectionMapper;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class CMRCollectionMapperTest {

    @Test
    public void test1() throws IOException, GSException {

	OriginalMetadata om = Mockito.mock(OriginalMetadata.class);

	InputStream stream = CMRCollectionMapperTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/cmr/test/cwicOriginal.xml");

	String md = IOStreamUtils.asUTF8String(stream);

	Mockito.doReturn(md).when(om).getMetadata();

	GSSource source = Mockito.mock(GSSource.class);

	String sid = "sourceid";
	Mockito.doReturn(sid).when(source).getUniqueIdentifier();

	CMROriginalMDWrapper wrapper = new CMROriginalMDWrapper();

	String url = "http://test";

	CMRCollectionMapper mapper = Mockito.spy(new CMRCollectionMapper());

	Assert.assertNull(mapper.execMapping(wrapper.wrap(om, url, url), source));

	Mockito.verify(mapper, Mockito.times(0)).enrichWithSecondLevelUrl(Mockito.any(), Mockito.any(), Mockito.any());

    }

    @Test
    public void test2() throws IOException, GSException {

	OriginalMetadata om = Mockito.mock(OriginalMetadata.class);

	InputStream stream = CMRCollectionMapperTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/cmr/test/non-cwicOriginal.xml");

	String md = IOStreamUtils.asUTF8String(stream);

	Mockito.doReturn(md).when(om).getMetadata();

	GSSource source = Mockito.mock(GSSource.class);

	String sid = "sourceid";
	Mockito.doReturn(sid).when(source).getUniqueIdentifier();

	CMROriginalMDWrapper wrapper = new CMROriginalMDWrapper();

	String url = "http://test";

	CMRCollectionMapper mapper = Mockito.spy(new CMRCollectionMapper());

	Assert.assertNotNull(mapper.execMapping(wrapper.wrap(om, url, url), source));

	Mockito.verify(mapper, Mockito.times(1)).enrichWithSecondLevelUrl(Mockito.any(), Mockito.any(), Mockito.any());

    }

    @Test
    public void test3() throws IOException, GSException {

	OriginalMetadata om = Mockito.mock(OriginalMetadata.class);

	InputStream stream = CMRCollectionMapperTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/cmr/test/non-cwicOriginal.xml");

	String md = IOStreamUtils.asUTF8String(stream);

	Mockito.doReturn(md).when(om).getMetadata();

	GSSource source = Mockito.mock(GSSource.class);

	String sid = "sourceid";
	Mockito.doReturn(sid).when(source).getUniqueIdentifier();

	CMROriginalMDWrapper wrapper = new CMROriginalMDWrapper();

	String url = "http://test";

	CMRCollectionMapper mapper = Mockito.spy(new CMRCollectionMapper());
	
	ErrorInfo errorInfo = new ErrorInfo();
	errorInfo.setCaller(getClass());

	Mockito.doThrow(GSException.createException(errorInfo)).when(mapper).enrichWithSecondLevelUrl(Mockito.any(), Mockito.any(), Mockito.any());

	Assert.assertNotNull(mapper.execMapping(wrapper.wrap(om, url, url), source));

	Mockito.verify(mapper, Mockito.times(1)).enrichWithSecondLevelUrl(Mockito.any(), Mockito.any(), Mockito.any());

    }

}