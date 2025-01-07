/**
 * 
 */
package eu.essi_lab.accessor.digitalglobe;

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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author Fabrizio
 */
public class DigitalGlobeResourceMapper extends OriginalIdentifierMapper {

    // https://partner.digitalglobe.com/partners_shpfiles/wvarchive
    public static final String DIGITAL_GLOBE_SCHEME_URI = "https://partner.digitalglobe.com/scheme";
    private static final String DIGITAL_GLOBE_MAPPING_ERROR = "DIGITAL_GLOBE_MAPPING_ERROR";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {
     
	Dataset out = new Dataset();

	out.setSource(source);

	MIMetadata mi_Metadata = out.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	mi_Metadata.setParentIdentifier(source.getUniqueIdentifier());

	DataIdentification identification = mi_Metadata.getDataIdentification();

	try {

	    XMLDocumentReader docReader = new XMLDocumentReader(originalMD.getMetadata());

	    //
	    // title
	    //
	    String name = docReader.evaluateString("//metadata/@name");
	    String title = docReader.evaluateString("//metadata/idinfo/citation/citeinfo/title");
	    title += ", " + name;
	    identification.setCitationTitle(title);

	    //
	    // publication date
	    //
	    String pubDate = docReader.evaluateString("//metadata/idinfo/citation/citeinfo/pubdate");
	    String[] splitPubDate = pubDate.split("/");
	    String isoPubDate = splitPubDate[2] + "-" + splitPubDate[1] + "-" + splitPubDate[0];
	    identification.addCitationDate(isoPubDate, "publication");

	    //
	    // abstract
	    //
	    String abs = docReader.evaluateString("//metadata/idinfo/descript/abstract");
	    String pur = docReader.evaluateString("//metadata/idinfo/descript/purpose");
	    identification.setAbstract(abs + " " + pur);

	    //
	    // distribution info
	    //
	    String link = docReader.evaluateString("//metadata/@link");
	    out.getHarmonizedMetadata().getCoreMetadata().addDistributionOnlineResource(//
		    name, //
		    link, //
		    "HTTP-GET", //
		    "download");
	    
	    out.getHarmonizedMetadata().getCoreMetadata().addDistributionFormat("shapefile");
	    out.getHarmonizedMetadata().getCoreMetadata().addDistributionFormat("application/dbf");	    
	    
	    //
	    // temporal extent
	    //
	    String calDate = docReader.evaluateString("//metadata/idinfo/timeperd/timeinfo/sngdate/caldate");
	    String startTime = calDate.split(" ")[0];
	    String endTime = calDate.split(" ")[1];
	    identification.addTemporalExtent(startTime, endTime);

	    //
	    // bbox
	    //
	    String west = docReader.evaluateString("//metadata/idinfo/spdom/bounding/westbc");
	    String south = docReader.evaluateString("//metadata/idinfo/spdom/bounding/southbc");
	    String east = docReader.evaluateString("//metadata/idinfo/spdom/bounding/eastbc");
	    String north = docReader.evaluateString("//metadata/idinfo/spdom/bounding/northbc");

	    identification.addGeographicBoundingBox(//
		    Double.valueOf(north), //
		    Double.valueOf(west), //
		    Double.valueOf(south), //
		    Double.valueOf(east));

	    //
	    // keywords
	    //
	    List<String> keywords = Arrays.asList(docReader.evaluateNodes("//keywords//*/text()[normalize-space(.)!='']")).stream()
		    .map(n -> n.getNodeValue()).collect(Collectors.toList());

	    keywords.addAll(Arrays.asList(docReader.evaluateNodes("//attrdef")).stream().map(n -> ((Element) n).getTextContent())
		    .collect(Collectors.toList()));

	    keywords.addAll(Arrays.asList(docReader.evaluateNodes("//attrdefs")).stream().map(n -> ((Element) n).getTextContent())
		    .collect(Collectors.toList()));

	    keywords = keywords.stream().distinct().collect(Collectors.toList());

	    keywords.forEach(k -> identification.addKeyword(k));

	    //
	    // constraints
	    //
	    String accconst = docReader.evaluateString("//metadata/idinfo/accconst");
	    String useconst = docReader.evaluateString("//metadata/idinfo/useconst");

	    LegalConstraints legalConstraints = new LegalConstraints();
	    legalConstraints.addUseLimitation(accconst + " " + useconst);

	    identification.addLegalConstraints(legalConstraints);

	    //
	    // point of contact
	    //
	    String org = docReader.evaluateString("//metadata/idinfo/ptcontac/cntinfo/cntorgp/cntorg");
	    String pos = docReader.evaluateString("//metadata/idinfo/ptcontac/cntinfo/cntpos");

	    String address = docReader.evaluateString("//metadata/idinfo/ptcontac/cntinfo/cntaddr/address");
	    String city = docReader.evaluateString("//metadata/idinfo/ptcontac/cntinfo/cntaddr/city");
	    String state = docReader.evaluateString("//metadata/idinfo/ptcontac/cntinfo/cntaddr/state");
	    String postal = docReader.evaluateString("//metadata/idinfo/ptcontac/cntinfo/cntaddr/postal");
	    String country = docReader.evaluateString("//metadata/idinfo/ptcontac/cntinfo/cntaddr/country");

	    String voice = docReader.evaluateString("//metadata/idinfo/ptcontac/cntinfo/cntvoice");
	    String fax = docReader.evaluateString("//metadata/idinfo/ptcontac/cntinfo/cntfax");
	    String cntemail = docReader.evaluateString("//metadata/idinfo/ptcontac/cntinfo/cntemail");

	    ResponsibleParty responsibleParty = new ResponsibleParty();
	    responsibleParty.setOrganisationName(org);
	    responsibleParty.setPositionName(pos);
	    responsibleParty.setRoleCode("author");
	    
	    Contact contact = new Contact();
	    contact.addPhoneVoice(voice);
	    contact.addPhoneFax(fax);
	    
	    Address contactAddress = new Address();
 	    contactAddress.addElectronicMailAddress(cntemail);
	    contactAddress.setCity(city);
	    contactAddress.setCountry(country);
	    contactAddress.setPostalCode(postal);
	    
	    contact.setAddress(contactAddress);	    
	    responsibleParty.setContactInfo(contact);
	    identification.addPointOfContact(responsibleParty);

	} catch (SAXException | IOException | XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DIGITAL_GLOBE_MAPPING_ERROR, //
		    e);

	}

	return out;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return DIGITAL_GLOBE_SCHEME_URI;
    }

}
