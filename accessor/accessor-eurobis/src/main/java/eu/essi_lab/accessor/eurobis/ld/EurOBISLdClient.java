package eu.essi_lab.accessor.eurobis.ld;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.slf4j.Logger;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author boldrini
 */
public class EurOBISLdClient {

	private String DCAT_NS = "http://www.w3.org/ns/dcat#";
	private String DATASET_LOCALNAME = "dataset";

	private String endpoint = "";

	private static String DEFAULT_ENDPOINT = "https://marineinfo.org/id/collection/619.ttl";

	private Logger logger = GSLoggerFactory.getLogger(getClass());

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public EurOBISLdClient() throws Exception {
		this(DEFAULT_ENDPOINT);

	}

	public EurOBISLdClient(String endpoint) throws Exception {
		setEndpoint(endpoint);
		Downloader downloader = new Downloader();
		Optional<InputStream> stream = downloader.downloadOptionalStream(endpoint);
		if (stream.isPresent()) {
			File tempFile = File.createTempFile(getClass().getSimpleName(), ".ttl");
			FileOutputStream fos = new FileOutputStream(tempFile);
			InputStream s = stream.get();
			IOUtils.copy(s, fos);
			s.close();
			fos.close();
			Model model = ModelFactory.createDefaultModel();

			// Load data from a Turtle file into the model
			model = FileManager.get().readModel(model, tempFile.getAbsolutePath());

			// Iterate over the statements in the model and do something with them
			StmtIterator iter = model.listStatements();
			while (iter.hasNext()) {
				Statement statement = iter.nextStatement();
				Resource subject = statement.getSubject();
				Property predicate = statement.getPredicate();
				RDFNode object = statement.getObject();
				if (isDataset(predicate)) {
					datasetURIs.add(object.asResource().getURI());
				}
				// System.out.println(subject + " " + predicate + " " + object);
			}
			model.close();
			tempFile.delete();
		}

	}

	private boolean isDataset(Property predicate) {
		String ns = predicate.getNameSpace();
		String localname = predicate.getLocalName();
		return ns.equals(DCAT_NS) && localname.equals(DATASET_LOCALNAME);
	}

	private List<String> datasetURIs = new ArrayList<>();

	public List<String> getDatasetURIs() {
		return datasetURIs;

	}

	public void setDatasetURIs(List<String> datasetURIs) {
		this.datasetURIs = datasetURIs;
	}

	public Integer getSize() {
		return datasetURIs.size();
	}

	/**
	 * Returns EML metadata associated with the given dataset index
	 * 
	 * @param index
	 * @return
	 * @throws Exception
	 */
	public InputStream getMetadata(int index, String extension) throws Exception {
		String url = getDatasetURIs().get(index) + extension;
		return getMetadata(url);
	}

	public InputStream getMetadata(String url) throws Exception {
		Downloader downloader = new Downloader();
		Optional<InputStream> optionalStream = downloader.downloadOptionalStream(url);
		if (optionalStream.isPresent()) {
			InputStream stream = optionalStream.get();
			return stream;
		}
		return null;
	}

}
