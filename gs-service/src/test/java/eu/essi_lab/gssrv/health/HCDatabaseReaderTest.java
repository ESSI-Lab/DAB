package eu.essi_lab.gssrv.health;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import eu.essi_lab.gssrv.health.db.HCDatabaseReader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author ilsanto
 */
public class HCDatabaseReaderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void test2() throws GSException {
	expectedException.expect(GSException.class);

	HCDatabaseReader reader = new HCDatabaseReader();

	DiscoveryMessage dm = Mockito.mock(DiscoveryMessage.class);

	Mockito.doReturn(new ArrayList<>()).when(dm).getSources();
	reader.count(dm);

    }

    @Test
    public void test3() throws GSException {
	expectedException.expect(GSException.class);

	HCDatabaseReader reader = new HCDatabaseReader();

	DiscoveryMessage dm = Mockito.mock(DiscoveryMessage.class);

	Mockito.doReturn(new ArrayList<>()).when(dm).getSources();
	reader.discover(dm);

    }

    @Test
    public void test4() throws GSException {

	HCDatabaseReader reader = new HCDatabaseReader();

	DiscoveryMessage dm = Mockito.mock(DiscoveryMessage.class);

	List<GSSource> sources = new ArrayList<>();

	GSSource source = Mockito.mock(GSSource.class);

	String sid = "sid";

	Mockito.doReturn(sid).when(source).getUniqueIdentifier();

	sources.add(source);

	Mockito.doReturn(sources).when(dm).getSources();
	ResultSet<GSResource> rs = reader.discover(dm);

	Assert.assertFalse(rs.getResultsList().isEmpty());

	Assert.assertEquals(sid, rs.getResultsList().get(0).getSource().getUniqueIdentifier());

    }

    @Test
    public void test5() throws GSException {

	HCDatabaseReader reader = new HCDatabaseReader();

	DiscoveryMessage dm = Mockito.mock(DiscoveryMessage.class);

	List<GSSource> sources = new ArrayList<>();

	GSSource source = Mockito.mock(GSSource.class);

	sources.add(source);

	Mockito.doReturn(sources).when(dm).getSources();
	DiscoveryCountResponse count = reader.count(dm);

	Assert.assertNotNull(count);

	Assert.assertEquals(1, count.getCount());

    }

}