package eu.essi_lab.gssrv.conf.task.collection;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

/**
 * @author boldrini
 */
public class WISRecordDeletionTaskTest {

    private static final String GDC_URL = "https://wmo-resource-catalogue.wis2dev.io/collections/wis2-discovery-metadata/items/urn:wmo:md:it-cnr-itiam-whos-dab:can-swe:10";

    private static final String CANONICAL = "https://production.whos-dab.eu/gs-service/services/essi/view/whos/oapi/collections/discovery-metadata/items/urn:wmo:md:it-cnr-itiam-whos-dab:can-swe:10";

    /**
     * A trimmed version of the record in the WIS2 Global Discovery Catalogue
     */
    private static JSONObject record() {

	return new JSONObject("{" //
		+ "\"id\":\"urn:wmo:md:it-cnr-itiam-whos-dab:can-swe:10\"," //
		+ "\"type\":\"Feature\"," //
		+ "\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[-141.45,42.06],[-141.45,82.5],[74.18,82.5],[74.18,42.06],[-141.45,42.06]]]}," //
		+ "\"properties\":{\"id\":\"urn:wmo:md:it-cnr-itiam-whos-dab:can-swe:10\",\"centre-id\":\"it-cnr-itiam-whos-dab\",\"type\":\"dataset\"}," //
		+ "\"links\":[" //
		+ "{\"rel\":\"canonical\",\"href\":\"" + CANONICAL + "\",\"type\":\"application/json\"}," //
		+ "{\"rel\":\"self\",\"href\":\"" + GDC_URL + "?f=json\",\"type\":\"application/geo+json\"}" //
		+ "]}");
    }

    @Test
    public void deletionMessage() {

	JSONObject message = WISRecordDeletionTask.createDeletionMessage(record(), GDC_URL + "?f=json", "DAB").getJSONObject();

	JSONObject properties = message.getJSONObject("properties");
	assertEquals("urn:wmo:md:it-cnr-itiam-whos-dab:can-swe:10", properties.getString("data_id"));
	assertTrue(properties.has("pubtime"));
	assertFalse(properties.has("integrity"));

	JSONArray links = message.getJSONArray("links");
	assertEquals(1, links.length());
	assertEquals("deletion", links.getJSONObject(0).getString("rel"));
	assertEquals(CANONICAL, links.getJSONObject(0).getString("href"));

	assertEquals("Polygon", message.getJSONObject("geometry").getString("type"));
    }

    @Test
    public void withoutCanonicalLink() {

	JSONObject record = record();
	record.remove("links");
	record.remove("geometry");

	JSONObject message = WISRecordDeletionTask.createDeletionMessage(record, GDC_URL + "?f=json", "DAB").getJSONObject();

	assertEquals(GDC_URL, message.getJSONArray("links").getJSONObject(0).getString("href"));
	assertTrue(message.isNull("geometry"));
    }

    @Test
    public void topic() {

	assertEquals("origin/a/wis2/it-cnr-itiam-whos-dab/metadata", WISRecordDeletionTask.getTopic(record()));

	// the centre id from the record identifier
	JSONObject record = record();
	record.getJSONObject("properties").remove("centre-id");
	assertEquals("origin/a/wis2/it-cnr-itiam-whos-dab/metadata", WISRecordDeletionTask.getTopic(record));
    }

    @Test
    public void jsonURL() {

	assertEquals(GDC_URL + "?f=json", WISRecordDeletionTask.getJSONURL(GDC_URL));
	assertEquals(GDC_URL + "?f=json", WISRecordDeletionTask.getJSONURL(GDC_URL + "?f=json"));
	assertEquals(GDC_URL + "?lang=en&f=json", WISRecordDeletionTask.getJSONURL(GDC_URL + "?lang=en"));
    }
}
