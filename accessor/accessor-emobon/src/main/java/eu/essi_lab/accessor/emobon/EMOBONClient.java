package eu.essi_lab.accessor.emobon;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
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
public class EMOBONClient {

	private String DCAT_NS = "http://www.w3.org/ns/dcat#";
	private String DATASET_LOCALNAME = "dataset";

	private String endpoint = "";

	private static String DEFAULT_ENDPOINT = "https://data.emobon.embrc.eu/metadata.ttl";

	private Logger logger = GSLoggerFactory.getLogger(getClass());
	
	private Model mainModel = null;

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public EMOBONClient() throws Exception {
		this(DEFAULT_ENDPOINT);
	}

	public EMOBONClient(String endpoint) throws Exception {
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
			mainModel = ModelFactory.createDefaultModel();

			// Load data from a Turtle file into the model
			mainModel = FileManager.get().readModel(mainModel, tempFile.getAbsolutePath());

			// Iterate over the statements in the model and extract dataset URIs
			StmtIterator iter = mainModel.listStatements();
			while (iter.hasNext()) {
				Statement statement = iter.nextStatement();
				Property predicate = statement.getPredicate();
				RDFNode object = statement.getObject();
				if (isDataset(predicate)) {
					Resource datasetResource = object.asResource();
					String datasetURI = datasetResource.getURI();
					// Only add observatory crates, skip profile datasets
					if (datasetURI != null && datasetURI.contains("observatory-") && datasetURI.endsWith("-crate/")) {
						// Store the actual URI as it appears in the model
						datasetURIs.add(datasetURI);
						datasetResources.add(datasetResource);
					}
				}
			}
			tempFile.delete();
		}
	}

	private boolean isDataset(Property predicate) {
		String ns = predicate.getNameSpace();
		String localname = predicate.getLocalName();
		return ns.equals(DCAT_NS) && localname.equals(DATASET_LOCALNAME);
	}

	private List<String> datasetURIs = new ArrayList<>();
	private List<Resource> datasetResources = new ArrayList<>();

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
	 * Returns TTL metadata associated with the given dataset index
	 * Extracts the TTL statements for the dataset from the main model
	 * 
	 * @param index
	 * @return TTL string for the dataset
	 * @throws Exception
	 */
	public String getMetadataTTL(int index) throws Exception {
		if (mainModel == null) {
			throw new Exception("Main model not loaded");
		}
		
		if (index < 0 || index >= datasetURIs.size()) {
			throw new Exception("Invalid dataset index: " + index);
		}
		
		// Get the URI
		String datasetURI = datasetURIs.get(index);
		
		// Create a sub-model containing only statements about this dataset
		Model datasetModel = ModelFactory.createDefaultModel();
		java.util.Set<String> processedResourceURIs = new java.util.HashSet<>();
		
		// Collect all statements where the subject URI matches our dataset URI
		collectStatementsByURI(datasetURI, datasetModel, processedResourceURIs);
		
		// Serialize the model to TTL format
		StringWriter writer = new StringWriter();
		datasetModel.write(writer, "TURTLE");
		datasetModel.close();
		
		return writer.toString();
	}
	
	/**
	 * Recursively collects all statements for a resource URI and its nested blank nodes
	 * Uses URI string matching to find statements, which is more reliable than Resource object matching
	 * 
	 * @param resourceURI
	 * @param targetModel
	 * @param processedResourceURIs
	 */
	private void collectStatementsByURI(String resourceURI, Model targetModel, java.util.Set<String> processedResourceURIs) {
		if (resourceURI == null || processedResourceURIs.contains(resourceURI)) {
			return;
		}
		processedResourceURIs.add(resourceURI);
		
		// Normalize the resource URI for comparison (extract the observatory name)
		String normalizedResourceURI = normalizeURI(resourceURI);
		
		// Iterate through all statements to find ones with matching subject URI
		StmtIterator allStatements = mainModel.listStatements();
		while (allStatements.hasNext()) {
			Statement stmt = allStatements.nextStatement();
			Resource subject = stmt.getSubject();
			if (subject != null) {
				String subjectURI = subject.getURI();
				// Match by normalizing both URIs (handles file:// vs https:// differences)
				if (subjectURI != null) {
					String normalizedSubjectURI = normalizeURI(subjectURI);
					if (normalizedSubjectURI != null && normalizedSubjectURI.equals(normalizedResourceURI)) {
						targetModel.add(stmt);
						
						// Recursively process blank nodes (anonymous resources)
						RDFNode object = stmt.getObject();
						if (object.isResource()) {
							Resource objResource = object.asResource();
							if (objResource.isAnon()) {
								// For blank nodes, use their internal ID
								String blankNodeId = objResource.getId().toString();
								if (!processedResourceURIs.contains(blankNodeId)) {
									collectStatementsByBlankNode(objResource, targetModel, processedResourceURIs);
								}
							}
						}
					}
				}
			}
		}
		allStatements.close();
	}
	
	/**
	 * Recursively collects statements for a blank node
	 * 
	 * @param blankNode
	 * @param targetModel
	 * @param processedResourceURIs
	 */
	private void collectStatementsByBlankNode(Resource blankNode, Model targetModel, java.util.Set<String> processedResourceURIs) {
		if (blankNode == null || !blankNode.isAnon()) {
			return;
		}
		
		String blankNodeId = blankNode.getId().toString();
		if (processedResourceURIs.contains(blankNodeId)) {
			return;
		}
		processedResourceURIs.add(blankNodeId);
		
		// Get all statements where this blank node is the subject
		StmtIterator iter = mainModel.listStatements(blankNode, null, (RDFNode) null);
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();
			targetModel.add(stmt);
			
			// Recursively process nested blank nodes
			RDFNode object = stmt.getObject();
			if (object.isResource()) {
				Resource objResource = object.asResource();
				if (objResource.isAnon()) {
					collectStatementsByBlankNode(objResource, targetModel, processedResourceURIs);
				}
			}
		}
		iter.close();
	}
	
	/**
	 * Normalizes a URI by extracting the observatory name part
	 * Handles both file:// and https:// schemes
	 * 
	 * @param uri
	 * @return normalized URI (just the observatory-crate part) or null
	 */
	private String normalizeURI(String uri) {
		if (uri == null) {
			return null;
		}
		// Extract the observatory-crate part from various URI formats
		// Examples:
		// - file:///observatory-umf-crate/ -> observatory-umf-crate
		// - https://data.emobon.embrc.eu/observatory-umf-crate/ -> observatory-umf-crate
		int observatoryIndex = uri.indexOf("observatory-");
		if (observatoryIndex >= 0) {
			String rest = uri.substring(observatoryIndex);
			// Extract up to the next slash or end
			int endIndex = rest.indexOf('/', "observatory-".length());
			if (endIndex > 0) {
				return rest.substring(0, endIndex);
			} else {
				// Remove trailing slash if present
				return rest.endsWith("/") ? rest.substring(0, rest.length() - 1) : rest;
			}
		}
		return null;
	}

	/**
	 * Returns JSON metadata (ro-crate-metadata.json) associated with the given dataset index
	 * 
	 * @param index
	 * @return
	 * @throws Exception
	 */
	public InputStream getMetadataJSON(int index) throws Exception {
		String datasetURI = getDatasetURIs().get(index);
		// Convert file:// URI to https:// URL if needed
		String baseURL = convertToHTTPSURL(datasetURI);
		String url = baseURL + "ro-crate-metadata.json";
		return getMetadata(url);
	}
	
	/**
	 * Converts a file:// URI to the corresponding https:// URL
	 * 
	 * @param uri
	 * @return https:// URL
	 */
	private String convertToHTTPSURL(String uri) {
		if (uri == null) {
			return null;
		}
		// Extract the observatory name
		String normalized = normalizeURI(uri);
		if (normalized != null) {
			// Reconstruct as https:// URL
			return "https://data.emobon.embrc.eu/" + normalized + "/";
		}
		// If normalization fails, try to replace file:// with https://
		if (uri.startsWith("file:///")) {
			return uri.replace("file:///", "https://data.emobon.embrc.eu/");
		}
		return uri;
	}

	/**
	 * Returns combined metadata (TTL + JSON) as a single string
	 * 
	 * @param index
	 * @return
	 * @throws Exception
	 */
	public String getCombinedMetadata(int index) throws Exception {
		StringBuilder combined = new StringBuilder();
		
		// Get TTL metadata from the main model
		String ttlMetadata = getMetadataTTL(index);
		if (ttlMetadata != null && !ttlMetadata.isEmpty()) {
			combined.append(ttlMetadata);
		}
		
		// Get JSON metadata
		InputStream jsonStream = getMetadataJSON(index);
		if (jsonStream != null) {
			combined.append("\n\n# RO-Crate JSON Metadata\n");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(jsonStream, baos);
			jsonStream.close();
			baos.close();
			combined.append(new String(baos.toByteArray(), "UTF-8"));
		}
		
		return combined.toString();
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

