package eu.essi_lab.model.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class CountryTest {

    @Test
    public void test() {
	assertEquals(Country.ITALY, Country.decode("italy"));
	assertEquals(Country.ITALY, Country.decode("ItALy"));
	assertEquals(Country.ITALY, Country.decode("ITA"));
	assertEquals(Country.ITALY, Country.decode("IT"));
	assertEquals(Country.SPAIN, Country.decode("ESP"));
	assertEquals(Country.RUSSIAN_FEDERATION, Country.decode("Russia"));
	assertEquals(Country.UNITED_KINGDOM_OF_GREAT_BRITAIN_AND_NORTHERN_IRELAND, Country.decode("United Kingdom"));
	assertNull(Country.decode("Klingon"));
    }

}
