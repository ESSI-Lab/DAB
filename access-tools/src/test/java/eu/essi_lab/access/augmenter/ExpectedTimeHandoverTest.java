package eu.essi_lab.access.augmenter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataRecord;

/**
 * @author boldrini
 */
public class ExpectedTimeHandoverTest {

    private static final String SERIES = "series";

    private static DataRecord record(long time, Long expected) {

	DataRecord record = new DataRecord();
	record.setDataIdentifier(SERIES);
	record.setDate(new Date(time));
	record.setNextRecordExpectedTime(expected == null ? null : new Date(expected));
	return record;
    }

    @Test
    public void currentAndStaleTest() {

	DataRecord newest = record(3000, null);
	DataRecord current = record(2000, 9000L);
	DataRecord stale = record(1000, 5000L);

	// the order of the cached records does not matter
	List<DataRecord> cached = Arrays.asList(stale, newest, current);

	Assert.assertSame(current, ExpectedTimeHandover.getCurrent(cached).get());
	Assert.assertEquals(Arrays.asList(stale), ExpectedTimeHandover.getStale(cached));

	// a single expected time is current, nothing is stale
	List<DataRecord> single = Arrays.asList(newest, current);
	Assert.assertSame(current, ExpectedTimeHandover.getCurrent(single).get());
	Assert.assertTrue(ExpectedTimeHandover.getStale(single).isEmpty());

	// no expected time
	List<DataRecord> none = Arrays.asList(newest, record(1000, null));
	Assert.assertTrue(ExpectedTimeHandover.getCurrent(none).isEmpty());
	Assert.assertTrue(ExpectedTimeHandover.getStale(none).isEmpty());
    }

    @Test
    public void clearTest() throws Exception {

	DataCacheConnector connector = mock(DataCacheConnector.class);

	DataRecord record = record(1000, 5000L);

	ExpectedTimeHandover.clear(Arrays.asList(record), connector);

	Assert.assertNull(record.getNextRecordExpectedTime());
	verify(connector).write(Arrays.asList(record));
    }

    @Test
    public void confirmedTest() throws Exception {

	DataCacheConnector connector = mock(DataCacheConnector.class);

	Date newestDate = new Date(4000);
	when(connector.getRecords(eq(newestDate), eq(newestDate), eq(SERIES))).thenReturn(Arrays.asList(record(4000, 8000L)));

	DataRecord current = record(2000, 3000L);

	ExpectedTimeHandover handover = new ExpectedTimeHandover(SERIES, newestDate, Arrays.asList(current));

	Assert.assertTrue(handover.confirmAndClear(connector));

	Assert.assertNull(current.getNextRecordExpectedTime());

	@SuppressWarnings("unchecked")
	ArgumentCaptor<List<DataRecord>> captor = ArgumentCaptor.forClass(List.class);
	verify(connector).write(captor.capture());
	Assert.assertEquals(Arrays.asList(current), captor.getValue());
    }

    @Test
    public void notConfirmedTest() throws Exception {

	DataCacheConnector connector = mock(DataCacheConnector.class);

	// the newest written record is missing (e.g. its bulk failed)
	Date newestDate = new Date(4000);
	when(connector.getRecords(any(), any(), any(String[].class))).thenReturn(Arrays.asList());

	DataRecord current = record(2000, 3000L);

	ExpectedTimeHandover handover = new ExpectedTimeHandover(SERIES, newestDate, Arrays.asList(current));

	Assert.assertFalse(handover.confirmAndClear(connector));

	// the current expected time is kept, so that the series is refreshed again
	Assert.assertEquals(new Date(3000), current.getNextRecordExpectedTime());
	verify(connector, never()).write(anyList());
    }

    @Test
    public void otherDateNotConfirmedTest() throws Exception {

	DataCacheConnector connector = mock(DataCacheConnector.class);

	Date newestDate = new Date(4000);
	when(connector.getRecords(eq(newestDate), eq(newestDate), eq(SERIES))).thenReturn(Arrays.asList(record(3999, null)));

	DataRecord current = record(2000, 3000L);

	Assert.assertFalse(new ExpectedTimeHandover(SERIES, newestDate, Arrays.asList(current)).confirmAndClear(connector));
	Assert.assertNotNull(current.getNextRecordExpectedTime());
    }
}
