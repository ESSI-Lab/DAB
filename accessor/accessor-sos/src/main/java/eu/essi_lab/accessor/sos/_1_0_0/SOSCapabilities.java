/**
 * 
 */
package eu.essi_lab.accessor.sos._1_0_0;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.geotools.api.geometry.BoundingBox;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import eu.essi_lab.accessor.sos.SOSRequestBuilder;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.stax.StAXDocumentParser;

/**
 * @author Fabrizio
 */
public class SOSCapabilities {

    private String title;
    private String _abstract;
    private String providerName;
    private String providerSite;
    private List<String> keywords;
    private String individualName;
    private String positionName;
    private String phoneNumber;
    private String deliveryPoint;
    private String city;
    private String postalCode;
    private String country;
    private String mailAddress;
    private List<String> offeringNames;
    private File capabilities;
    private String administrativeArea;

    /**
     * 
     */
    private SOSCapabilities() {

	keywords = new ArrayList<String>();
	offeringNames = new ArrayList<String>();
    }

    /**
     * @param file
     * @throws XMLStreamException
     * @throws IOException
     */
    public SOSCapabilities(File file) throws XMLStreamException, IOException {

	this();

	FileInputStream stream = new FileInputStream(file);

	parseStream(stream);
    }

    /**
     * @param url
     * @throws IOException
     * @throws XMLStreamException
     */
    public SOSCapabilities(String url) throws XMLStreamException, IOException {

	this();

	Downloader downloader = new Downloader();
	downloader.setResponseTimeout(TimeUnit.MINUTES, 3);

	Optional<InputStream> optionalStream = downloader.downloadOptionalStream(url);

	if (optionalStream.isPresent()) {

	    capabilities = Files.createTempFile("capabilities", ".xml", new FileAttribute[] {}).toFile();

	    Files.copy(optionalStream.get(), capabilities.toPath(), StandardCopyOption.REPLACE_EXISTING);

	    FileInputStream stream = new FileInputStream(capabilities);

	    parseStream(stream);

	    capabilities.deleteOnExit();
	}
    }

    /**
     * 
     */
    public void release() {

	capabilities.delete();
    }

    /**
     * @return
     */
    public Optional<String> getTitle() {

	return Optional.ofNullable(title);
    }

    /**
     * @return
     */
    public Optional<String> getAbstract() {

	return Optional.ofNullable(_abstract);
    }

    /**
     * @return
     */
    public Optional<String> getProviderName() {

	return Optional.ofNullable(providerName);
    }

    /**
     * @return
     */
    public Optional<String> getProviderSite() {

	return Optional.ofNullable(providerSite);
    }

    /**
     * @return
     */
    public List<String> getKeywords() {

	return keywords;
    }

    /**
     * @return
     */
    public Optional<String> getIndividualName() {

	return Optional.ofNullable(individualName);
    }

    /**
     * @return
     */
    public Optional<String> getPositionName() {

	return Optional.ofNullable(positionName);
    }

    /**
     * @return
     */
    public Optional<String> getPhoneNumber() {

	return Optional.ofNullable(phoneNumber);
    }

    /**
     * @return
     */
    public Optional<String> getDeliveryPoint() {

	return Optional.ofNullable(deliveryPoint);
    }

    /**
     * @return
     */
    public Optional<String> getCity() {

	return Optional.ofNullable(city);
    }

    /**
     * @return
     */
    public Optional<String> getPostalCode() {

	return Optional.ofNullable(postalCode);
    }

    /**
     * @return
     */
    public Optional<String> getCountry() {

	return Optional.ofNullable(country);
    }

    /**
     * @return
     */
    public Optional<String> getMailAddress() {

	return Optional.ofNullable(mailAddress);
    }

    /**
     * @return
     */
    public final Optional<String> getAdministrativeArea() {

	return Optional.ofNullable(administrativeArea);
    }

    /**
     * @return
     */
    public List<String> getOfferingNames() {

	return offeringNames;
    }

    /**
     * @param offeringName
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    public Optional<String> getDescription(String offeringName) throws XMLStreamException, IOException {

	Optional<String> offering = getOffering(offeringName);

	final List<String> description = new ArrayList<String>();

	if (offering.isPresent()) {

	    StAXDocumentParser parser = new StAXDocumentParser(offering.get());
	    parser.add(new QName("description"), v -> description.add(v));
	    parser.parse();
	}

	return description.stream().findFirst();
    }

    /**
     * @param offeringName
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    public List<String> getProcedures(String offeringName) throws XMLStreamException, IOException {

	GSLoggerFactory.getLogger(getClass()).debug("Finding procedures of offering {} STARTED", offeringName);

	Optional<String> offering = getOffering(offeringName);

	List<String> procedures = new ArrayList<String>();

	if (offering.isPresent()) {

	    File file = Files.createTempFile("offering", ".xml", new FileAttribute[] {}).toFile();

	    Files.copy(IOUtils.toInputStream(//
		    offering.get(), //
		    "UTF-8"), //
		    file.toPath(), StandardCopyOption.REPLACE_EXISTING);

	    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {

		procedures = bufferedReader.//
			lines().//
			filter(l -> l.contains("procedure") && l.contains("href")).//
			map(l -> l.substring(l.indexOf("href=") + 6, l.indexOf(">") - 1)).//
			collect(Collectors.toList());
	    }

	    file.delete();

	    // StAXDocumentParser parser = new StAXDocumentParser(offering.get());
	    // parser.add(new QName("procedure"), "href", v -> procedures.add(v));
	    // parser.parse();
	}

	GSLoggerFactory.getLogger(getClass()).debug("Finding procedures of offering {} ENDED", offeringName);

	return procedures;
    }

    /**
     * @param offeringName
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    public Optional<TemporalExtent> getTemporalExtent(String offeringName) throws XMLStreamException, IOException {

	Optional<String> offering = getOffering(offeringName);

	TemporalExtent temporalExtent = null;

	if (offering.isPresent()) {

	    StAXDocumentParser parser = new StAXDocumentParser(offering.get());

	    Optional<String> timePeriod = parser.find(new QName("TimePeriod")).stream().findFirst();

	    if (timePeriod.isPresent()) {

		final List<String> beginPos = new ArrayList<String>();
		final List<String> endPos = new ArrayList<String>();

		parser = new StAXDocumentParser(timePeriod.get());
		parser.add(new QName("beginPosition"), v -> beginPos.add(v));
		parser.add(new QName("endPosition"), v -> endPos.add(v));

		parser.parse();

		if (!beginPos.isEmpty()) {

		    temporalExtent = new TemporalExtent();
		    temporalExtent.setBeginPosition(beginPos.get(0));
		}

		if (!endPos.isEmpty()) {

		    if (temporalExtent == null) {
			temporalExtent = new TemporalExtent();
		    }

		    temporalExtent.setEndPosition(endPos.get(0));
		}
	    }
	}

	return Optional.of(temporalExtent);
    }

    /**
     * @param offeringName
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    public Optional<BoundingBox> getBoundingBox(String offeringName) throws XMLStreamException, IOException {

	Optional<String> offering = getOffering(offeringName);

	ReferencedEnvelope out = null;

	if (offering.isPresent()) {

	    StAXDocumentParser parser = new StAXDocumentParser(offering.get());

	    Optional<String> env = parser.find(new QName("Envelope")).stream().findFirst();

	    if (env.isPresent()) {

		final List<String> coordEl = new ArrayList<String>();

		parser = new StAXDocumentParser(env.get());
		parser.add(new QName("coordinates"), v -> coordEl.add(v));
		parser.parse();

		if (!coordEl.isEmpty()) {

		    String coordValue = coordEl.get(0);

		    List<Double> coordinates = Arrays.asList(coordValue.split(",")).//
			    stream().//
			    map(v -> Double.valueOf(v)).//
			    collect(Collectors.toList());

		    if (coordinates.size() == 2) {

			out = new ReferencedEnvelope(//
				coordinates.get(0), //
				coordinates.get(0), //
				coordinates.get(1), //
				coordinates.get(1), //
				DefaultGeographicCRS.WGS84);

		    } else {

			out = new ReferencedEnvelope(//
				coordinates.get(0), //
				coordinates.get(2), //
				coordinates.get(1), //
				coordinates.get(3), //
				DefaultGeographicCRS.WGS84);
		    }
		}
	    }
	}

	return Optional.of(out);
    }

    /**
     * @param inputStream
     * @throws IOException
     * @throws XMLStreamException
     */
    private void parseStream(InputStream inputStream) throws XMLStreamException, IOException {

	GSLoggerFactory.getLogger(getClass()).debug("Parsing capabilities STARTED");

	FileInputStream stream = new FileInputStream(capabilities);

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	parser.add(new QName("ServiceIdentification"), new QName("Title"), v -> title = v);
	parser.add(new QName("ServiceIdentification"), new QName("Abstract"), v -> _abstract = v);
	parser.add(new QName("Keywords"), new QName("Keyword"), v -> keywords.add(v));

	parser.add(new QName("ServiceProvider"), new QName("ProviderName"), v -> providerName = v);
	parser.add(new QName("ServiceProvider"), new QName("ProviderSite"), v -> providerSite = v);

	parser.add(new QName("ServiceContact"), new QName("IndividualName"), v -> individualName = v);
	parser.add(new QName("ServiceContact"), new QName("PositionName"), v -> positionName = v);

	parser.add(new QName("Phone"), new QName("Voice"), v -> phoneNumber = v);

	parser.add(new QName("Address"), new QName("DeliveryPoint"), v -> deliveryPoint = v);
	parser.add(new QName("Address"), new QName("City"), v -> city = v);
	parser.add(new QName("Address"), new QName("PostalCode"), v -> postalCode = v);
	parser.add(new QName("Address"), new QName("Country"), v -> country = v);
	parser.add(new QName("Address"), new QName("ElectronicMailAddress"), v -> mailAddress = v);
	parser.add(new QName("Address"), new QName("AdministrativeArea"), v -> administrativeArea = v);

	//
	//
	//

	parser.add(new QName("ObservationOffering"), new QName("name"), v -> offeringNames.add(v));

	parser.parse();

	GSLoggerFactory.getLogger(getClass()).debug("Parsing capabilities ENDED");
    }

    /**
     * @param offeringName
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    private Optional<String> getOffering(String offeringName) throws XMLStreamException, IOException {

	GSLoggerFactory.getLogger(getClass()).debug("Finding offering {} STARTED", offeringName);

	FileInputStream stream = new FileInputStream(capabilities);

	StAXDocumentParser parser = new StAXDocumentParser(stream);
	parser.addNewLineOnCloseTags(true);

	Optional<String> offering = parser.find(new QName("ObservationOffering")).//
		stream().//
		filter(v -> v.contains("name>" + offeringName)).//
		findFirst();

	GSLoggerFactory.getLogger(getClass()).debug("Finding offering {} ENDED", offeringName);

	return offering;
    }

    public static void main(String[] args) throws Exception {

	String url = "https://ggis.un-igrac.org/istsos?api-key=724df1163f0a99f81af89f3b446d76281d07d3ac";

	SOSRequestBuilder requestBuilder = new SOSRequestBuilder(url, "1.0.0");

	SOSCapabilities capabilities = new SOSCapabilities(requestBuilder.createCapabilitiesRequest());

	List<String> names = capabilities.getOfferingNames();

	Optional<TemporalExtent> temporalExtent = capabilities.getTemporalExtent(names.get(0));

	System.out.println(temporalExtent.get().asString(false));

	Optional<BoundingBox> boundingBox = capabilities.getBoundingBox(names.get(0));

	System.out.println(boundingBox.get());

	List<String> procedures = capabilities.getProcedures(names.get(0));
	System.out.println(procedures.size());
    }

}
