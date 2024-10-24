package eu.essi_lab.accessor.csw;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.AbstractRecordType;
import eu.essi_lab.jaxb.csw._2_0_2.BriefRecordType;
import eu.essi_lab.jaxb.csw._2_0_2.Capabilities;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * CSW service endpoint: https://www.nodc.noaa.gov/archivesearch/csw?
 * This service has big metadata (some reaching 20MB xml files). It will hang with HTTP code 500 when the response
 * become big. E.g.
 * https://www.nodc.noaa.gov/archivesearch/csw?service=CSW&request=GetRecords&version=2.0.2&outputFormat=application/xml&outputSchema=http://www.isotc211.org/2005/gmd&ElementSetName=full&resultType=results&typeNames=gmd:MD_Metadata&startPosition=1821&maxRecords=20
 * In this case record 1830 was about 20MB and requesting it along with other caused HTTP code 500. The workaround here
 * is to reduce page size to 1 when errors due to big records are detected and restoring the user set page size
 * afterwards (the strategy has been implemented at upper levels of the hierarchy ({@link CSWConnector}) as it
 * seems to
 * be useful in general).
 * 
 * @author boldrini
 */
public class CSWNODCConnector extends CSWGetConnector {

    /**
     * 
     */
    private ArrayList<String> globalList;

    /**
     * 
     */
    private String globalFileName;

    /**
     * 
     */
    public CSWNODCConnector() {

	getSetting().setPageSize(50);

	setSelectedSchema(CommonNameSpaceContext.CSW_NS_URI);

	setElementeSetType(ElementSetType.BRIEF);

	globalList = new ArrayList<>();
    }

    /**
     * 
     */
    public static final String TYPE = "CSW NODC Connector";

    @Override
    public String getType() {

	return TYPE;
    }

    /**
     * @param content
     * @param silent
     * @return
     * @throws JAXBException
     */
    @Override
    protected Capabilities doUnmarshallCapabiliesStream(InputStream content, boolean silent) throws Exception {

	String capString = IOStreamUtils.asUTF8String(content);

	capString = capString.replace("<ows:AllowedValues>", "");
	capString = capString.replace("</ows:AllowedValues>", "");

	content = IOStreamUtils.asStream(capString);

	return super.doUnmarshallCapabiliesStream(content, silent);
    }

    /**
     * @param record
     * @return
     */
    @Override
    protected OriginalMetadata recordToOriginalMetadata(AbstractRecordType record) {

	OriginalMetadata out = null;

	int maxTries = 10;

	int tries = 1;

	String identifier = null;

	while (out == null && tries < maxTries) {

	    try {

		BriefRecordType brief = (BriefRecordType) record;

		identifier = URLEncoder.encode(brief.getIdentifiers().get(0).getValue().getContent().get(0), "UTF-8");

		String requestURL = "https://www.ncei.noaa.gov/metadata/geoportal/rest/metadata/item/" + identifier + "/xml";

		if (identifier.contains("GLOB")) {

		    GSLoggerFactory.getLogger(getClass()).info("Global metadata found: {}", requestURL);

		    Optional<S3TransferWrapper> optS3TransferManager = ConfigurationWrapper.getS3TransferManager();

		    if (optS3TransferManager.isPresent()) {

			GSLoggerFactory.getLogger(getClass()).info("Transfer of global list to S3 STARTED");

			if (globalFileName == null) {

			    globalFileName = "globalList_" + ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds();
			    globalFileName = globalFileName.replace(":", "_");
			    globalFileName = globalFileName.replace(".", "_");
			}

			File tempFile = File.createTempFile(globalFileName, ".txt");

			globalList.add(requestURL.toString());

			String text = globalList.toString().replace("[", "").replace("]", "").replace(",", "\n").replace(" ", "");

			Files.copy(IOStreamUtils.asStream(text), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			optS3TransferManager.get().uploadFile(tempFile.getAbsolutePath(), "nodcglobal", globalFileName);

			tempFile.delete();

			GSLoggerFactory.getLogger(getClass()).info("Transfer of global list to S3 ENDED");
		    }

		    return null;
		}

		GSLoggerFactory.getLogger(getClass())
			.debug("Attempt #" + tries + " to retrieve GMI metadata with id " + identifier + " STARTED");

		Downloader downloader = new Downloader();

		InputStream gmiStream = downloader.downloadOptionalStream(requestURL).get();

		DocumentBuilder builder = XMLFactories.newDocumentBuilderFactory().newDocumentBuilder();

		Document node = builder.parse(gmiStream);

		out = createMetadata(node);

		GSLoggerFactory.getLogger(getClass())
			.debug("Attempt #" + tries + " to retrieve GMI metadata with id " + identifier + " SUCCEEDED");

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass())
			.debug("Attempt #" + tries + " to retrieve GMI metadata with id " + identifier + " FAILED");

		tries++;

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage());

		try {
		    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
		} catch (InterruptedException e1) {
		}
	    }
	}

	return out;
    }

    @Override
    protected String getReturnedMetadataSchema() {

	return CommonNameSpaceContext.NODC_NS_URI;
    }

    @Override
    protected String getRequestedMetadataSchema() throws GSException {

	return CommonNameSpaceContext.CSW_NS_URI;
    }

    @Override
    protected String getConstraintLanguageParameter() {
	return "";
    }

    /**
     * The CSW NODC connector applies only to the NODC catalogue
     */
    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	if (endpoint.contains("nodc")) {
	    return super.supports(source);
	} else {
	    return false;
	}

    }

}
