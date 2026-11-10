package eu.essi_lab.access.datacache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

public class SeriesStatusRecordTest {

    private static final long DAY = 24 * 60 * 60 * 1000l;

    private static SeriesStatusRecord record(boolean current, Date fillBegin, Date filledFrom, Date firstObservation) {

	SeriesStatusRecord ret = new SeriesStatusRecord();
	ret.setInMetadata(true);
	ret.setCurrent(current);
	ret.setFillBegin(fillBegin);
	ret.setFilledFrom(filledFrom);

	SeriesCacheInfo info = new SeriesCacheInfo();
	info.setValuesCount(31);
	info.setFirstObservation(firstObservation);
	info.setLastObservation(new Date(firstObservation.getTime() + 30 * DAY));
	ret.update(info);
	return ret;
    }

    @Test
    public void testIncomplete() {

	Date fillBegin = new Date(0);
	Date firstObservation = new Date(1000 * DAY);

	// cached data start after the fill begin
	SeriesStatusRecord record = record(false, fillBegin, null, firstObservation);
	assertTrue(record.isIncomplete());
	assertEquals(SeriesStatus.INCOMPLETE.name(), record.getStatus());

	// already filled from the fill begin: the source has no older data
	record = record(false, fillBegin, fillBegin, firstObservation);
	assertFalse(record.isIncomplete());
	assertEquals(SeriesStatus.HISTORICAL.name(), record.getStatus());

	// filled from a later begin (e.g. the fill days have been increased)
	record = record(false, fillBegin, new Date(500 * DAY), firstObservation);
	assertTrue(record.isIncomplete());

	// cached data start at the fill begin
	record = record(false, firstObservation, null, firstObservation);
	assertFalse(record.isIncomplete());

	// unknown fill begin
	record = record(false, null, null, firstObservation);
	assertFalse(record.isIncomplete());
	assertEquals(SeriesStatus.HISTORICAL.name(), record.getStatus());
    }

    @Test
    public void testJSON() {

	Date fillBegin = new Date(0);
	Date filledFrom = new Date(DAY);

	SeriesStatusRecord record = SeriesStatusRecord.fromJSON(record(true, fillBegin, filledFrom, new Date(1000 * DAY)).toJSON());
	assertEquals(fillBegin, record.getFillBegin());
	assertEquals(filledFrom, record.getFilledFrom());
	assertEquals(SeriesStatus.INCOMPLETE.name(), record.getStatus());
    }
}
