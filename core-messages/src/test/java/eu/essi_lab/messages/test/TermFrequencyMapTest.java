package eu.essi_lab.messages.test;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap.ItemsSortOrder;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap.TermFrequencyTarget;

public class TermFrequencyMapTest {

    @Test
    public void mergingTest() {

	InputStream stream = TermFrequencyMapTest.class.getClassLoader().getResourceAsStream("termFrequency.xml");

	try {

	    ClonableInputStream clone = new ClonableInputStream(stream);

	    // ---------------------------------------------
	    //
	    // merging the map with itself, nothing changes
	    //
	    //
	    TermFrequencyMap tfMap1 = TermFrequencyMap.create(clone.clone());
	    //
	    // keeping the first 3 items with the higher frequencies
	    //
	    tfMap1 = tfMap1.merge(tfMap1, 3);

	    List<TermFrequencyItem> format = tfMap1.getItems(TermFrequencyTarget.FORMAT);
	    List<TermFrequencyItem> keyword = tfMap1.getItems(TermFrequencyTarget.KEYWORD);
	    List<TermFrequencyItem> protocol = tfMap1.getItems(TermFrequencyTarget.PROTOCOL);
	    List<TermFrequencyItem> source = tfMap1.getItems(TermFrequencyTarget.SOURCE);

	    Assert.assertEquals(format.size(), 3);
	    Assert.assertEquals(keyword.size(), 3);
	    Assert.assertEquals(protocol.size(), 3);
	    Assert.assertEquals(source.size(), 3);

	    String formatTerm = format.get(0).getTerm();
	    String keywordTerm = keyword.get(0).getTerm();
	    String protocolTerm = protocol.get(0).getTerm();
	    String sourceTerm = source.get(0).getTerm();

	    Assert.assertEquals("geoTiff", formatTerm);
	    Assert.assertEquals("GeoTIFF", keywordTerm);
	    Assert.assertEquals("OGC%3AWMS-1.1.1-http-get-map", protocolTerm);
	    Assert.assertEquals("WEBSRVENCAT", sourceTerm);

	    // ---------------------------------------------
	    //
	    // merging the map with map 2, nothing changes
	    //
	    //
	    TermFrequencyMap tfMap2 = TermFrequencyMap.create(clone.clone());

	    TermFrequencyItem item = new TermFrequencyItem();
	    item.setFreq(10000);
	    item.setTerm("10000SOURCE");
	    tfMap2.getItems(TermFrequencyTarget.SOURCE).add(item);

	    item = new TermFrequencyItem();
	    item.setFreq(10000);
	    item.setTerm("10000FORMAT");
	    tfMap2.getItems(TermFrequencyTarget.FORMAT).add(item);

	    item = new TermFrequencyItem();
	    item.setFreq(10000);
	    item.setTerm("10000KWD");
	    tfMap2.getItems(TermFrequencyTarget.KEYWORD).add(item);

	    item = new TermFrequencyItem();
	    item.setFreq(10000);
	    item.setTerm("10000PROT");
	    tfMap2.getItems(TermFrequencyTarget.PROTOCOL).add(item);
	    //
	    // now map 1 has only 3 items per target
	    // as consequence of the merging it will have all the original items plus
	    // the additional ones in the map 2
	    // original items count:
	    // sources: 7
	    // keyword: 10
	    // format: 8
	    // protocol: 10
	    // keeping the first 5 items with the higher frequencies
	    //
	    tfMap1 = TermFrequencyMap.create(clone.clone());
	    tfMap1 = tfMap1.merge(tfMap2, 15);

	    format = tfMap1.getItems(TermFrequencyTarget.FORMAT);
	    keyword = tfMap1.getItems(TermFrequencyTarget.KEYWORD);
	    protocol = tfMap1.getItems(TermFrequencyTarget.PROTOCOL);
	    source = tfMap1.getItems(TermFrequencyTarget.SOURCE);

	    Assert.assertEquals(format.size(), 9); // was 8, the map 2 item is added
	    Assert.assertEquals(keyword.size(), 11); // was 10, the map 2 item is added
	    Assert.assertEquals(protocol.size(), 11); // was 10, the map 2 item is added
	    Assert.assertEquals(source.size(), 8); // was 7, the map 2 item is added

	    formatTerm = format.get(0).getTerm();
	    keywordTerm = keyword.get(0).getTerm();
	    protocolTerm = protocol.get(0).getTerm();
	    sourceTerm = source.get(0).getTerm();

	    Assert.assertEquals("10000FORMAT", formatTerm);
	    Assert.assertEquals("10000KWD", keywordTerm);
	    Assert.assertEquals("10000PROT", protocolTerm);
	    Assert.assertEquals("10000SOURCE", sourceTerm);

	} catch (Exception e) {

	    fail("Exception thrown");
	}
    }

    @Test
    public void unmarshallAndMarshallTest() {

	InputStream stream = TermFrequencyMapTest.class.getClassLoader().getResourceAsStream("termFrequency.xml");
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	try {

	    TermFrequencyMap tfMap = TermFrequencyMap.create(stream);

	    int formatSize = tfMap.getItems(TermFrequencyTarget.FORMAT).size();
	    Assert.assertEquals(8, formatSize);

	    int keywordSize = tfMap.getItems(TermFrequencyTarget.KEYWORD).size();
	    Assert.assertEquals(10, keywordSize);

	    int protocolSize = tfMap.getItems(TermFrequencyTarget.PROTOCOL).size();
	    Assert.assertEquals(10, protocolSize);

	    int sourceSize = tfMap.getItems(TermFrequencyTarget.SOURCE).size();
	    Assert.assertEquals(7, sourceSize);

	    int orgNameSize = tfMap.getItems(TermFrequencyTarget.ORGANISATION_NAME).size();
	    Assert.assertEquals(3, orgNameSize);

	    int sscSCoreSize = tfMap.getItems(TermFrequencyTarget.SSC_SCORE).size();
	    Assert.assertEquals(1, sscSCoreSize);

	    TermFrequencyItem item = tfMap.getItems(TermFrequencyTarget.FORMAT).get(0);
	    int freq = item.getFreq();
	    String term = item.getTerm();

	    Assert.assertEquals(2, freq);
	    Assert.assertEquals("MINES+ParisTech", term);

	    tfMap.toStream(outputStream);
	    // tfMap.toStream(System.out);

	    byte[] byteArray = outputStream.toByteArray();
	    String mapString = new String(byteArray);

	    Assert.assertThat(mapString, new Matcher<String>() {

		@Override
		public void describeTo(Description description) {

		}

		@Override
		public boolean matches(Object item) {

		    return item.toString().contains("BACINI");
		}

		@Override
		public void describeMismatch(Object item, Description mismatchDescription) {
		}

		@Override
		public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
		}
	    });

	    int formatsCount = tfMap.getItemsCount(TermFrequencyTarget.FORMAT);
	    Assert.assertEquals(8, formatsCount);

	    int keywordCount = tfMap.getItemsCount(TermFrequencyTarget.KEYWORD);
	    Assert.assertEquals(10, keywordCount);

	    int protocolCount = tfMap.getItemsCount(TermFrequencyTarget.PROTOCOL);
	    Assert.assertEquals(10, protocolCount);

	    int sourceCount = tfMap.getItemsCount(TermFrequencyTarget.SOURCE);
	    Assert.assertEquals(7, sourceCount);
	    
	    int orgsCount = tfMap.getItemsCount(TermFrequencyTarget.ORGANISATION_NAME);
	    Assert.assertEquals(3, orgsCount);

	    int scoresCount = tfMap.getItemsCount(TermFrequencyTarget.SSC_SCORE);
	    Assert.assertEquals(1, scoresCount);

	    List<TermFrequencyItem> format = tfMap.getItems(TermFrequencyTarget.FORMAT);
	    List<TermFrequencyItem> keyword = tfMap.getItems(TermFrequencyTarget.KEYWORD);
	    List<TermFrequencyItem> protocol = tfMap.getItems(TermFrequencyTarget.PROTOCOL);
	    List<TermFrequencyItem> source = tfMap.getItems(TermFrequencyTarget.SOURCE);
	    List<TermFrequencyItem> orgs = tfMap.getItems(TermFrequencyTarget.ORGANISATION_NAME);
	    List<TermFrequencyItem> scores = tfMap.getItems(TermFrequencyTarget.SSC_SCORE);
	    
	    String formatTerm = format.get(0).getTerm();
	    String keywordTerm = keyword.get(0).getTerm();
	    String protocolTerm = protocol.get(0).getTerm();
	    String sourceTerm = source.get(0).getTerm();
	    String orgTerm = orgs.get(0).getTerm();
	    String scoreTerm = scores.get(0).getTerm();

	    Assert.assertEquals("MINES+ParisTech", formatTerm);
	    Assert.assertEquals("WCS", keywordTerm);
	    Assert.assertEquals("WWW%3ALINK-1.0-http--partners", protocolTerm);
	    Assert.assertEquals("NASASVS", sourceTerm);
	    Assert.assertEquals("Utah State University Utah Water Research Laboratory", orgTerm);
	    Assert.assertEquals("1000", scoreTerm);

	    format = tfMap.getItems(TermFrequencyTarget.FORMAT, ItemsSortOrder.BY_FREQUENCY);
	    keyword = tfMap.getItems(TermFrequencyTarget.KEYWORD, ItemsSortOrder.BY_FREQUENCY);
	    protocol = tfMap.getItems(TermFrequencyTarget.PROTOCOL, ItemsSortOrder.BY_FREQUENCY);
	    source = tfMap.getItems(TermFrequencyTarget.SOURCE, ItemsSortOrder.BY_FREQUENCY);
	    orgs = tfMap.getItems(TermFrequencyTarget.ORGANISATION_NAME, ItemsSortOrder.BY_FREQUENCY);
	    scores = tfMap.getItems(TermFrequencyTarget.SSC_SCORE, ItemsSortOrder.BY_FREQUENCY);

	    formatTerm = format.get(0).getTerm();
	    keywordTerm = keyword.get(0).getTerm();
	    protocolTerm = protocol.get(0).getTerm();
	    sourceTerm = source.get(0).getTerm();
	    orgTerm = orgs.get(0).getTerm();
	    scoreTerm = scores.get(0).getTerm();

	    Assert.assertEquals("geoTiff", formatTerm);
	    Assert.assertEquals("GeoTIFF", keywordTerm);
	    Assert.assertEquals("OGC%3AWMS-1.1.1-http-get-map", protocolTerm);
	    Assert.assertEquals("WEBSRVENCAT", sourceTerm);
	    Assert.assertEquals("Utah State University Utah Water Research Laboratory", orgTerm);
	    Assert.assertEquals("1000", scoreTerm);

	} catch (JAXBException e) {

	    fail("Exception thrown");
	}
    }
}
