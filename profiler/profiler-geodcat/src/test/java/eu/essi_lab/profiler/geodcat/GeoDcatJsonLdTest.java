package eu.essi_lab.profiler.geodcat;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.web.WebRequest;

import jakarta.ws.rs.core.UriInfo;

public class GeoDcatJsonLdTest {

    @Test
    public void jsonLdContextContainsDcatAndDct() {

	JSONObject ctx = GeoDcatJsonLd.jsonLdContext();
	assertEquals("http://www.w3.org/ns/dcat#", ctx.getString("dcat"));
	assertEquals("http://purl.org/dc/terms/", ctx.getString("dct"));
	assertTrue(ctx.has("locn"));
	assertEquals(GeoDcatJsonLd.NS_GEODCAT_AP, ctx.getString("geodcatap"));
	assertTrue(ctx.has("gsp"));
	assertTrue(ctx.has("adms"));
	assertTrue(ctx.has("prov"));
    }

    @Test
    public void stripQueryRemovesQueryString() {

	assertEquals("http://example.org/path", GeoDcatUrlHelper.stripQuery("http://example.org/path?a=1"));
    }

    @Test
    public void catalogPageUrlSetsPagingAndKeepsOtherParams() {

	String u = GeoDcatUrlHelper.catalogPageUrl("https://dab.example/geodcat/catalog", "view=v1&foo=bar", 21, 20);
	assertTrue(u.startsWith("https://dab.example/geodcat/catalog?"));
	assertTrue(u.contains("startIndex=21"));
	assertTrue(u.contains("count=20"));
	assertTrue(u.contains("view=v1"));
	assertTrue(u.contains("foo=bar"));
    }

    @Test
    public void catalogDocumentIncludesHydraNextOnFirstPageWhenMoreResults() {

	UriInfo uriInfo = mock(UriInfo.class);
	when(uriInfo.getAbsolutePath()).thenReturn(URI.create("https://dab.example/geodcat/catalog"));

	WebRequest request = new WebRequest();
	request.setUriInfo(uriInfo);
	request.setQueryString("count=10");

	Page page = new Page(1, 10);
	JSONObject doc = GeoDcatJsonLd.catalogDocument(request, new JSONArray(), 100, page, 10);

	assertTrue(doc.getJSONObject("@context").has("hydra"));
	assertEquals(100, doc.getInt("hydra:totalItems"));
	assertTrue(doc.getJSONObject("hydra:next").getString("@id").contains("startIndex=11"));
	assertTrue(doc.getJSONObject("hydra:first").getString("@id").contains("startIndex=1"));
	assertFalse(doc.has("hydra:previous"));
    }
}
