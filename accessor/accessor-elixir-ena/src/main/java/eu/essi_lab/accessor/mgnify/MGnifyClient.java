package eu.essi_lab.accessor.mgnify;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;

public class MGnifyClient {

    public static final String[] selectedSuperStudies = new String[] { //
	    "tara-oceans", //
	    // "earth-microbiome-project", //
	    // "nasa-genelab-microbiome-mango", //
	    // "holofood", //
	    "malaspina", //
	    "atlanteco", //
	    // "findingpheno", //
	    // "nmgn-microbiome"

    };

    private String endpoint;

    public MGnifyClient(String endpoint) {
	this.endpoint = endpoint;
    }

    public <T extends MGnifyObject> Pages<T> getPages(MGnifyObjectFactory<T> factory, String url) throws Exception {
	return getPages(factory, url, null);
    }

    public <T extends MGnifyObject> Pages<T> getPages(MGnifyObjectFactory<T> factory, String url, String parameters) throws Exception {
	if (url == null) {
	    url = endpoint + factory.getType();
	    if (parameters != null) {
		url += "?" + parameters;
	    }
	}
	return new Pages<T>(factory, url);
    }

    public Pages<Biome> getBiomes() throws Exception {
	return getPages(new BiomeFactory(), null);
    }

    public Pages<SuperStudy> getSelectedSuperStudies() throws Exception {
	Pages<SuperStudy> ret = getPages(new SuperStudyFactory(), null);
	List<SuperStudy> studies = ret.getObjects();
	List<SuperStudy> selected = new ArrayList<SuperStudy>();
	for (SuperStudy study : studies) {
	    String slug = study.getUrlSlug();
	    boolean present = false;
	    for (String s : selectedSuperStudies) {
		if (slug.equals(s)) {
		    present = true;
		}
	    }
	    if (present) {
		selected.add(study);
	    }
	}
	ret.setObjects(selected);
	return ret;
    }

    public Pages<SuperStudy> getSuperStudies() throws Exception {
	return getPages(new SuperStudyFactory(), null);
    }

    public Pages<Study> getStudies(String url) throws Exception {
	return getPages(new StudyFactory(), url);
    }

    public Pages<Geocoordinates> getGeocoordinates(String url) throws Exception {
	return getPages(new GeocoordinateFactory(), url);
    }

    public Pages<Sample> getSamples(String url) throws Exception {
	return getPages(new SampleFactory(), url);
    }

    public Pages<Analysis> getAnalyses(String url) throws Exception {
	return getPages(new AnalysisFactory(), url);
    }

    public Pages<Download> getDownloads(String url) throws Exception {
	return getPages(new DownloadFactory(), url);
    }

    public Pages<TaxonomyLSU> getTaxonomiesLSU(String url) throws Exception {
	return getPages(new TaxonomyLSUFactory(), url);
    }

    static JSONObject retrieveJSONObject(String url) throws Exception {
	Downloader downloader = new Downloader();

	HttpResponse<InputStream> response;

	do {
	    response = downloader.downloadResponse(url);
	    if (response.statusCode() != 200) {
		Thread.sleep(2000);
	    }
	} while (response.statusCode() != 200);

	InputStream stream = response.body();
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	IOUtils.copy(stream, baos);
	stream.close();
	String str = new String(baos.toByteArray());
	return new JSONObject(str);
    }

}
