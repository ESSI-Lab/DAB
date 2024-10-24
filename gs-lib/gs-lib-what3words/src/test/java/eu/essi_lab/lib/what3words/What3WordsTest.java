package eu.essi_lab.lib.what3words;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.MockedDownloader;

public class What3WordsTest {

    public class MockedWhat3Words extends What3Words {

	public MockedWhat3Words(String firstWord, String secondWord, String thirdWord) throws Exception {
	    super(firstWord, secondWord, thirdWord);
	}

	public MockedWhat3Words(String latitude, String longitude) throws Exception {
	    super(latitude, longitude);

	}

	@Override
	protected Downloader createDownloader() {
	    return downloader;
	}

    }

    private Downloader downloader = null;

    @Before
    public void init() {
	String url1 = "https://api.what3words.com/v2/reverse?key=CHQR93EQ&coords=43.819251%2C11.200691&lang=en&format=json&display=full";
	String content1 = "{\"thanks\":\"Thanks from all of us at index.home.raft for using a what3words API\",\"crs\":{\"type\":\"link\",\"properties\":{\"href\":\"http:\\/\\/spatialreference.org\\/ref\\/epsg\\/4326\\/ogcwkt\\/\",\"type\":\"ogcwkt\"}},\"words\":\"firm.airless.camp\",\"bounds\":{\"southwest\":{\"lng\":11.200673,\"lat\":43.819238},\"northeast\":{\"lng\":11.20071,\"lat\":43.819265}},\"geometry\":{\"lng\":11.200691,\"lat\":43.819251},\"language\":\"en\",\"map\":\"http:\\/\\/w3w.co\\/firm.airless.camp\",\"status\":{\"reason\":\"OK\",\"status\":200}}";
	String url2 = "https://api.what3words.com/v2/forward?key=CHQR93EQ&addr=firm.airless.camp&lang=en&format=json&display=full";
	String content2 = "{\"thanks\":\"Thanks from all of us at index.home.raft for using a what3words API\",\"crs\":{\"type\":\"link\",\"properties\":{\"href\":\"http:\\/\\/spatialreference.org\\/ref\\/epsg\\/4326\\/ogcwkt\\/\",\"type\":\"ogcwkt\"}},\"words\":\"firm.airless.camp\",\"bounds\":{\"southwest\":{\"lng\":11.200673,\"lat\":43.819238},\"northeast\":{\"lng\":11.20071,\"lat\":43.819265}},\"geometry\":{\"lng\":11.200691,\"lat\":43.819251},\"language\":\"en\",\"map\":\"http:\\/\\/w3w.co\\/firm.airless.camp\",\"status\":{\"reason\":\"OK\",\"status\":200}}";

	this.downloader = new MockedDownloader(url1, content1, url2, content2);

    }

    @Test
    public void testWordsToPosition() throws Exception {
	What3Words w3w = new MockedWhat3Words("firm", "airless", "camp");
	assertEquals("43.819251", w3w.getLatitude());
	assertEquals("11.200691", w3w.getLongitude());
    }

    @Test
    public void testPositionToWords() throws Exception {
	What3Words w3w = new MockedWhat3Words("43.819251", "11.200691");
	assertEquals("firm", w3w.getWord1());
	assertEquals("airless", w3w.getWord2());
	assertEquals("camp", w3w.getWord3());
    }

}
