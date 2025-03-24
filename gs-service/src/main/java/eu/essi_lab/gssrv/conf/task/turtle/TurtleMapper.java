package eu.essi_lab.gssrv.conf.task.turtle;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;

public class TurtleMapper extends DiscoveryResultSetMapper<String> {

    @Override
    public MappingSchema getMappingSchema() {
	MappingSchema ret = new MappingSchema();
	ret.setEncodingMediaType(new MediaType("text", "turtle"));
	ret.setDescription("Terse RDF Triple Language");
	ret.setEncoding("RDF Turtle");
	ret.setEncodingVersion("1.1");
	ret.setName("RDF Turtle");
	return ret;
    }

    @Override
    public Provider getProvider() {
	return new ESSILabProvider();
    }

    private boolean isURI(String id) {
	if (id == null) {
	    return false;
	}
	if (id.startsWith("http")) {
	    return true;
	}
	if (id.startsWith("https")) {
	    return true;
	}
	return false;
    }

    @Override
    public String map(DiscoveryMessage message, GSResource resource) throws GSException {
	try {
	    String ret = getPrefixes();

	    Document doc = resource.asDocument(true);
	    XMLDocumentReader reader = new XMLDocumentReader(doc);

	    HarmonizedMetadata harmo = resource.getHarmonizedMetadata();
	    MIMetadata meta = harmo.getCoreMetadata().getMIMetadata();
	    ExtensionHandler handler = resource.getExtensionHandler();
	    DataIdentification info = meta.getDataIdentification();

	    HashSet<String> keywordTypes = getKeywordTypes(reader);

	    // platform parameter instrument cruise project

	    // PARAMETERs
	    List<SimpleEntry<String, String>> parvalueURIs = getKeywordsURIs(reader, "parameter");
	    List<SimpleEntry<String, String>> instrumentvalueURIs = getKeywordsURIs(reader, "instrument");

	    for (SimpleEntry<String, String> valueURI : parvalueURIs) {
		String id = valueURI.getValue();
		String title = valueURI.getKey();

		if (isURI(id)) {
		    ret += "<" + id + "> a skos:Concept, sosa:ObservedProperty, iop:Variable;\n";

		    if (title != null && !title.isEmpty()) {
			ret += "skos:prefLabel \"" + title + "\" .\n";
		    }

		}

	    }

	    // TODO QUALITY FLAGS
	    // <http://vocab.nerc.ac.uk/collection/L27/current/ARGO_QC/> a skos:Concept;
	    // skos:prefLabel "ARGO quality flags".

	    // ORGANIZATIONs
	    Node[] organizations = reader.evaluateNodes("//*:CI_ResponsibleParty");
	    for (Node organization : organizations) {
		String uri = reader.evaluateString(organization, "*:organisationName/*[1]/@*:href");
		if (isURI(uri)) {
		    ret += "<" + uri + "> a prov:Organization ;\n";
		    String name = reader.evaluateString(organization, "*:organisationName/*[1]");
		    if (name != null && !name.isEmpty()) {
			ret += "rdfs:label \"" + normalize(name) + "\".\n";
		    }

		}
	    }

	    // Keywords
	    Node[] keywords = reader.evaluateNodes(
		    "//*:MD_Keywords[not(*:type) or not(contains('platform platform_class parameter instrument cruise project',*:type/*:MD_KeywordTypeCode/@codeListValue))]/*:keyword");
	    for (Node keyword : keywords) {
		String uri = reader.evaluateString(keyword, "*[1]/@*:href");
		if (isURI(uri)) {
		    ret += "<" + uri + "> a skos:Concept;\n";
		    String value = reader.evaluateString(keyword, "*[1]");
		    ret += "skos:prefLabel \"" + normalize(value) + "\" .\n";
		}
	    }

	    // ex:FAIREASECatalog a dcat:Catalog ;
	    // dct:title "FAIREASECatalog Catalog"@en ;
	    // rdfs:label "FAIREASECatalog Catalog"@en ;
	    // foaf:homepage <http://example.org/catalog> ;
	    // dct:publisher <https://edmo.seadatanet.org/report/43> ;
	    // dct:language <http://id.loc.gov/vocabulary/iso639-1/en> ;
	    // #Contains other catalogues
	    // dcat:catalog ex:SDNCatalog, ex:CopernicusCatalog .

	    // #Describe each Catalogue with as much detail as FAIRE-EASE has and list their
	    // datasets. here I only
	    // include
	    // one dataset for SDN ex:MyDataset
	    Distribution distribution = meta.getDistribution();
	    List<Online> onlines = new ArrayList<>();
	    if (distribution != null) {
		Iterator<Online> onlinesIterator = distribution.getDistributionOnlines();
		while (onlinesIterator.hasNext()) {
		    Online online = (Online) onlinesIterator.next();
		    onlines.add(online);
		}
	    }

	    // CATALOG
	    String catId = "<https://dataset.geodab.eu/source/" + urlEncode(resource.getSource().getUniqueIdentifier()) + ">";
	    ret += catId + " a dcat:Catalog ;\n";
	    ret += "rdfs:label \"" + resource.getSource().getLabel() + "\";\n";
	    String filename = null;
	    if (resource.getOriginalId().isPresent()) {
		filename = resource.getOriginalId().get();
	    }
	    if (filename == null) {
		filename = resource.getPrivateId();
	    }
	    if (filename == null) {
		filename = resource.getPublicId();
	    }
	    String myDataset = "<https://dataset.geodab.eu/dataset/" + urlEncode(resource.getSource().getUniqueIdentifier()) + "/"
		    + urlEncode(filename + ".ttl") + ">";

	    ret += "dcat:dataset " + myDataset + ".\n";

	    ret += myDataset + " a dcat:Dataset, sdo:Dataset;\n";
	    ret += "dct:title \"" + normalize(info.getCitationTitle()) + "\";\n";
	    if (info.getAbstract() != null) {
		ret += "dct:description \"" + normalize(info.getAbstract()) + "\";\n";
	    }

	    Iterator<LegalConstraints> lci = info.getLegalConstraints();
	    if (lci != null) {
		while (lci.hasNext()) {
		    String license = null;
		    LegalConstraints legalConstraints = (LegalConstraints) lci.next();
		    String code = legalConstraints.getAccessConstraintCode();
		    if (code != null && !code.equals("otherRestrictions")) {
			license = code;
		    } else {
			String other = legalConstraints.getOtherConstraint();
			if (other != null) {
			    license = other;
			}
		    }
		    if (license != null) {
			String uri = null;
			if (license.contains("http")) {
			    uri = license.substring(license.indexOf("http"));
			    if (uri.contains(" ")) {
				uri = uri.substring(0, uri.indexOf(" "));
			    }
			    if (uri.contains(")")) {
				uri = uri.substring(0, uri.indexOf(")"));
			    }
			    ret += "dct:license \"" + license + "\" ;\n";
			    if (!license.equals(uri)) {
				ret += "dct:rights \"" + license + "\" ;\n";
			    }
			} else {
			    ret += "dct:rights \"" + license + "\" ;\n";
			}

		    }
		}
	    }

	    String resourceId = info.getResourceIdentifier();
	    if (resourceId != null) {
		if (isURI(resourceId)) {
		    ret += "dct:identifier <" + resourceId + "> ;\n";
		} else {
		    ret += "dct:identifier \"" + resourceId + "\" ;\n";
		}

	    }

	    for (int i = 0; i < onlines.size(); i++) {
		String distributionId = myDataset.replace(">", "/distribution/" + i + ">");
		ret += "dcat:distribution " + distributionId + " ;\n";
	    }

	    for (int i = 0; i < instrumentvalueURIs.size(); i++) {
		SimpleEntry<String, String> valueURI = instrumentvalueURIs.get(i);
		String id = valueURI.getValue();
		String title = valueURI.getKey();
		String idd = meta.getFileIdentifier() + "-" + i;

		if (isURI(id)) {
		    ret += "prov:wasGeneratedBy exdata:Activity" + idd + ";\n";
		}

	    }

	    // DOI
	    // adms:identifier [
	    // rdf:type adms:Identifier;
	    // rdf:parseType "Resource";
	    // skos:notation "10.1000/182" ;
	    // adms:schemaAgency <https://registry.identifiers.org/registry/doi> ;
	    // ];

	    for (SimpleEntry<String, String> valueURI : parvalueURIs) {
		String id = valueURI.getValue();
		String title = valueURI.getKey();

		ret += "sdo:variableMeasured [\n";
		ret += "rdf:type sdo:PropertyValue;\n";
		ret += "sdo:name \"" + title + "\";\n";
		if (isURI(id)) {
		    ret += "sdo:propertyID <" + id + "> ;\n";
		}
		// sdo:alternateName "WC_temp68";

		ret += "] ;\n";

	    }

	    for (Node organization : organizations) {

		String dctOrganizationType = null;
		String role = reader.evaluateString(organization, "*:role/*:CI_RoleCode/@codeListValue");
		boolean roleToBeSpecified = false;
		if (role != null && !role.isEmpty()) {
		    switch (role) {
		    case "distributor":
		    case "publisher":
			dctOrganizationType = "dct:publisher";
			break;
		    case "originator":
		    case "author":
			dctOrganizationType = "dct:creator";
			break;
		    case "custodian":
		    case "pointOfContact":
		    default:
			dctOrganizationType = "dct:contributor";
			roleToBeSpecified = true;
			break;
		    }
		}
		String uri = reader.evaluateString(organization, "*:organisationName/*[1]/@*:href");

		if (isURI(uri)) {
		    ret += dctOrganizationType + " ";

		    ret += "<" + uri + ">;\n";
		} else {
		    String name = reader.evaluateString(organization, "*:organisationName/*[1]");
		    if (name != null && !name.isEmpty()) {
			ret += dctOrganizationType + " ";

			ret += "[\n"//
				+ "        a dct:Agent ;\n"//
				+ "        foaf:name \"" + name + "\" ;\n";//

			if (roleToBeSpecified) {

			    ret += "        dcat:hadRole [\n"//
				    + "            a skos:Concept ;\n"//
				    + "            skos:prefLabel \"" + role + "\"@en\n"//
				    + "        ]\n";//
			}

			ret += "    ] .";
		    }
		}
	    }

	    // THEMEs
	    List<SimpleEntry<String, String>> themevalueURIs = getKeywordsURIs(reader, "theme");
	    for (SimpleEntry<String, String> valueURI : themevalueURIs) {
		String id = valueURI.getValue();
		String value = valueURI.getKey();
		if (isURI(id)) {
		    ret += "dcat:theme <" + id + "> ;\n";
		} else {
		    ret += "dcat:theme \"" + value.replace("\n", "") + "\" ;\n";
		}
	    }

	    String revisionDate = reader.evaluateString(
		    "//*:MD_DataIdentification/*:citation/*:CI_Citation/*:date/*:CI_Date[*:dateType/*:CI_DateTypeCode/@codeListValue='revision']/*:date/*:Date");
	    if (revisionDate != null) {
		ret += "dct:issued " + formatTime(revisionDate) + ";\n";
	    }

	    // #Temporal Info
	    TemporalExtent temporalExtent = info.getTemporalExtent();

	    if (temporalExtent != null) {
		String begin = temporalExtent.getBeginPosition();
		String end = temporalExtent.getEndPosition();
		if (begin != null && end != null) {
		    String timeBegin = formatTime(begin) + ";\n";
		    String timeEnd = formatTime(end) + ";\n";
		    if (timeBegin != null && timeEnd != null) {
			ret += "dct:temporal [\n";
			ret += "a dct:PeriodOfTime ;\n";
			ret += "dcat:startDate " + timeBegin;
			ret += "dcat:endDate   " + timeEnd;
			ret += "];\n";
		    }
		}
	    }

	    GeographicBoundingBox bbox = info.getGeographicBoundingBox();
	    if (bbox != null) {
		Double w = bbox.getWest();
		Double e = bbox.getEast();
		Double s = bbox.getSouth();
		Double n = bbox.getNorth();
		if (w != null && e != null && s != null && n != null) {
		    // #Spatial Info
		    ret += "dct:spatial [\n";
		    ret += "a dct:Location ;\n";
		    ret += "dcat:bbox \"\"\"POLYGON((\n";
		    ret += w + " " + s + ", " + e + " " + s + ", " + e + " " + n + ", " + w + " " + n + ", " + w + " " + s;
		    ret += "))\"\"\"^^geosparql:wktLiteral ;\n";
		    // ret += "geosparql:hasSerialization \"\"\"POLYGON((\n";
		    // ret += w + " " + n + ", " + e + " " + n + ", " + e + " " + s + ", " + w + " "
		    // + s + ", " + e + "
		    // "
		    // + n;
		    // ret += "))\"\"\"^^geosparql:wktLiteral ;\n";
		    ret += "] .\n";
		}
	    }

	    if (ret.endsWith(";\n")) {
		ret = ret.substring(0, ret.length() - 2) + ".\n";
	    }

	    for (int i = 0; i < instrumentvalueURIs.size(); i++) {
		SimpleEntry<String, String> valueURI = instrumentvalueURIs.get(i);
		String id = valueURI.getValue();

		String title = valueURI.getKey();
		String idd = meta.getFileIdentifier() + "-" + i;

		if (isURI(id)) {
		    ret += "exdata:Activity" + idd + " a prov:Activity;\n";
		    ret += "prov:used <" + id + "> .\n";
		    ret += "<" + id + ">  a sosa:Sensor, prov:Entity, skos:Concept;\n";
		    ret += "skos:prefLabel \"" + title + "\".\n";
		}

	    }

	    // sosa:observes <http://vocab.nerc.ac.uk/collection/P01/current/TEMPP681/> ;
	    // sosa:isHostedBy <http://vocab.nerc.ac.uk/collection/B76/current/B7600031/> .
	    List<SimpleEntry<String, String>> platformvalueURIs = getKeywordsURIs(reader, "platform");
	    if (platformvalueURIs.isEmpty()) {
		platformvalueURIs = getKeywordsURIs(reader, "platform_class");
	    }
	    for (SimpleEntry<String, String> valueURI : platformvalueURIs) {
		String id = valueURI.getValue();
		String title = valueURI.getKey();
		if (isURI(id)) {
		    if (title != null) {
			ret += "<" + id + "> a sosa:Platform;\n";
			ret += "skos:prefLabel \"" + title + "\".\n";
		    } else {
			ret += "<" + id + "> a sosa:Platform.\n";
		    }

		}
		// add platform class
	    }

	    // sosa:hosts <http://vocab.nerc.ac.uk/collection/L05/current/134/> .

	    // DISTRIBUTION
	    for (int i = 0; i < onlines.size(); i++) {
		Online online = onlines.get(i);
		String distributionId = myDataset.replace(">", "/distribution/" + i + ">");
		ret += distributionId + " a dcat:Distribution ;\n";

		String linkage = online.getLinkage();
		String protocol = online.getProtocol();

		FAIREaseMapping mapping = FAIREaseMapper.map(protocol, linkage, resource.getSource().getUniqueIdentifier());
		protocol = mapping.getProtocol();
		boolean inAccessService = mapping.isInAccessService();
		String mediaType = mapping.getMediaType();
		boolean download = false;

		if (isURI(linkage)) {
		    if (protocol != null && protocol.toLowerCase().contains("download")) {
			ret += "dcat:downloadURL <" + encodeURL(linkage) + "> ;\n";
			download = true;
		    } else {
			ret += "dcat:accessURL <" + encodeURL(linkage) + "> ;\n";
		    }
		}
		if (protocol != null && !protocol.isEmpty()) {
		    ret += "dcat:conformsTo <" + encodeURL(protocol) + "> ;\n";
		}
		String name = online.getName();
		String description = online.getDescription();
		if (name != null) {
		    ret += "dct:title \"" + normalize(name) + "\"@en ;\n";
		}

		if (mediaType != null && mediaType.startsWith("http")) {
		    ret += "dcat:mediaType <" + mediaType + "> ;\n";
		}
		//
		// dcat:byteSize "5120"^^xsd:nonNegativeInteger .
		if (!download) {
		    URL host;
		    try {
			if (!linkage.contains("://")) {
			    // default
			    linkage = "http://" + linkage;
			}
			host = new URL(linkage);
			if (inAccessService) {
			    String serverId = "exdata:subset-service-" + host.getHost().hashCode();
			    ret += "dcat:accessService " + serverId + ".\n";

			    ret += serverId + "\n";
			    ret += "rdf:type dcat:DataService ;\n";
			    if (protocol != null) {
				ret += "dct:conformsTo \"" + protocol + "\" ;\n";
			    }
			    ret += "dct:type <https://inspire.ec.europa.eu/metadata-codelist/SpatialDataServiceType/invoke> ;\n";
			    if (description != null) {
				ret += "dcat:endpointDescription \"" + description.replace("\n", "").replace("\"", "\\\"") + "\" ;\n";
			    }
			    ret += "dcat:endpointURL <" + encodeURL(linkage)
			    // + linkage.replace(" ", "%20").replace("[", "%5B").replace("]", "%5D").replace("{",
			    // "%7B").replace("}", "%7D") +
				    + "> ;\n";
			    ret += "dcat:servesDataset " + myDataset + " .\n";
			}

		    } catch (Exception e) {
			GSLoggerFactory.getLogger(getClass()).error(e);
		    }

		}
		if (ret.endsWith(";\n")) {
		    ret = ret.substring(0, ret.length() - 2) + ".\n";
		}
	    }

	    // #The quality info here
	    // dqv:hasQualityMeasurement exdata:measurement1;
	    // ex:measurement1 a dqv:QualityMeasurement ;
	    // dqv:isMeasurementOf <http://vocab.nerc.ac.uk/collection/L27/current/ARGO_QC/>
	    // ;
	    // dqv:value "good"^^xsd:boolean .

	    return ret;
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e);
	    return null;
	}
    }

    public static String encodeURL(String linkage) {
	if (!linkage.contains("?")) {
	    return linkage.replace(" ", "%20");
	} else {
	    String pre = linkage.substring(0, linkage.indexOf("?"));
	    String post = linkage.substring(linkage.indexOf("?") + 1);
	    String[] split = post.split("&");
	    String ret = pre.replace(" ", "%20") + "?";
	    for (String par : split) {
		if (par.contains("=")) {
		    String[] split2 = par.split("=");
		    if (split2.length > 1) {
			ret += URLEncoder.encode(split2[0], StandardCharsets.UTF_8) + "="
				+ URLEncoder.encode(split2[1], StandardCharsets.UTF_8);
		    } else {
			ret += URLEncoder.encode(split2[0], StandardCharsets.UTF_8);
		    }
		} else {
		    ret += URLEncoder.encode(par, StandardCharsets.UTF_8);
		}
		ret += "&";
	    }
	    if (split.length > 0) {
		ret = ret.substring(0, ret.length() - 1);
	    }
	    return ret;
	}
    }

    public static void main(String[] args) throws Exception {
	String originalPath = "http://example.com/my folder/file.txt";
	// Create URI without encoding the slash
	URI uri = new URI(originalPath);
	System.out.println("Encoded URI: " + uri.toString());
    }

    private String normalize(String text) {
	return text.replace("\\", "\\\\").replace("\r", "").replace("\n", "").replace("\"", "\\\"");
    }

    private String formatTime(String time) {
	if (time.length() < 11) {
	    // return time + "\"^^xsd:date;\n";
	    try {
		Date d = ISO8601DateTimeUtils.parseISO8601(time);
		return "\"" + ISO8601DateTimeUtils.getISO8601DateTime(d) + "\"^^xsd:dateTime";
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error("Error parsing time: {}", time);
		return null;
	    }
	} else {
	    return "\"" + time + "\"^^xsd:dateTime";
	}

    }

    private String urlEncode(String uniqueIdentifier) {
	return URLEncoder.encode(uniqueIdentifier, StandardCharsets.UTF_8);
    }

    private HashSet<String> getKeywordTypes(XMLDocumentReader reader) throws XPathExpressionException {
	HashSet<String> ret = new HashSet<>();
	Node[] types = reader.evaluateNodes("//*:MD_KeywordTypeCode/@codeListValue");
	for (Node type : types) {
	    ret.add(reader.evaluateString(type, "."));
	}
	return ret;
    }

    private List<SimpleEntry<String, String>> getKeywordsURIs(XMLDocumentReader reader, String type) throws XPathExpressionException {
	List<SimpleEntry<String, String>> ret = new ArrayList<>();
	Node[] keywords = reader.evaluateNodes("//*:MD_Keywords[*:type/*:MD_KeywordTypeCode/@codeListValue='" + type + "']/*:keyword");
	for (Node keyword : keywords) {
	    String title = reader.evaluateString(keyword, "*[1]/.");
	    if (title != null) {
		title = title.trim();
	    }
	    String uri = reader.evaluateString(keyword, "*[1]/@*:href");
	    if (uri != null) {
		uri = uri.trim();
	    }
	    ret.add(new SimpleEntry<>(title, uri));
	}
	return ret;
    }

    private String getPrefixes() {
	return "@prefix dap: <http://example.org/dap-example/> .\n"//
		+ "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"//
		+ "@prefix exdata: <https://essi-lab.eu/dab/fair-ease> .\n"//
		+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"//
		+ "@prefix sosa: <http://www.w3.org/ns/sosa/> .\n"//
		// + "@prefix ssn: <http://www.w3.org/ns/ssn/> .\n"//
		+ "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n"//
		+ "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n"//
		+ "@prefix dcat: <http://www.w3.org/ns/dcat#> .\n"//
		+ "@prefix dct: <http://purl.org/dc/terms/>.\n"//
		+ "@prefix geosparql: <http://www.opengis.net/ont/geosparql#> .\n"//
		+ "@prefix prov: <http://www.w3.org/ns/prov#> .\n"//
		+ "@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n"//
		+ "@prefix dqv: <http://www.w3.org/ns/dqv#> .\n"//
		+ "@prefix adms: <http://www.w3.org/ns/adms#> .\n"//
		+ "@prefix sdo: <https://schema.org/> .\n"//
		+ "@prefix iop: <https://w3id.org/iadopt/ont/>.\n"//
		+ "@prefix skos: <http://www.w3.org/2004/02/skos/core#>.\n";
    }

}
