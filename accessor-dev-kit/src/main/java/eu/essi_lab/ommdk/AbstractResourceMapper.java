package eu.essi_lab.ommdk;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.utils.whos.WMOOntology;
import eu.essi_lab.lib.net.utils.whos.WMOUnit;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;
public abstract class AbstractResourceMapper implements IResourceMapper {

    private static final String ORIGINAL_METADATA_MISSING_ERROR = "ORIGINAL_METADATA_MISSING_ERROR";
    private static long oneWeekInMilliseconds = 1000 * 60 * 60 * 24 * 7l; // the last week

    @Override
    public GSResource map(OriginalMetadata originalMD, GSSource source) throws GSException {

	GSResource gsResource = execMapping(originalMD, source);

	if (gsResource == null) {

	    GSLoggerFactory.getLogger(getClass()).error("Resource mapping produced a null resource");
	    return null;
	}

	if (gsResource.getSource() == null) {
	    gsResource.setSource(source);
	}

	String originalMetadata = originalMD.getMetadata();
	if (Objects.isNull(originalMetadata) || originalMetadata.isEmpty()) {

	    throw GSException.createException(//
		    getClass(), //
		    "Original metadata missing", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ORIGINAL_METADATA_MISSING_ERROR //
	    );
	}

	gsResource.setOriginalMetadata(originalMD);

	// ------------------------------
	//
	// original identifier
	//
	String originalIdentifier = createOriginalIdentifier(gsResource);

	gsResource.setOriginalId(originalIdentifier);

	handleUniqueIdentifiers(gsResource);

	// organizations
	List<String> organizations = gsResource.getExtensionHandler().getOriginatorOrganisationDescriptions();
	if (organizations.isEmpty()) {
	    ResponsibleParty poc = gsResource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getPointOfContact();
	    if (poc != null) {
		String name = poc.getOrganisationName();
		if (name != null) {
		    gsResource.getExtensionHandler().addOriginatorOrganisationDescription(name);
		}
	    }
	}

	return gsResource;
    }


    
    /**
     * Generates the original identifier according to the supplied <code>resource</code>
     * 
     * @param resource
     */
    protected abstract String createOriginalIdentifier(GSResource resource);

    /**
     * @param originalMD
     * @param source
     * @return
     * @throws GSException
     */
    protected abstract GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException;

    public static void setIndeterminatePosition(GSResource gsResource) {
	setIndeterminatePosition(gsResource, oneWeekInMilliseconds);
    }

    public static void setIndeterminatePosition(GSResource gsResource, long allowedGap) {
	String endPosition = null;
	try {
	    TemporalExtent extent = gsResource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		    .getTemporalExtent();
	    String beginPosition = extent.getBeginPosition();
	    endPosition = extent.getEndPosition();
	    Optional<Date> beginDate = ISO8601DateTimeUtils.parseISO8601ToDate(beginPosition);
	    Optional<Date> endDate = ISO8601DateTimeUtils.parseISO8601ToDate(endPosition);
	    if (!beginDate.isPresent() && !endDate.isPresent()) {
		// no temporal information given.. not possible to assume other: return
		return;
	    }
	    if (endDate.isPresent()) {
		Date endTime = endDate.get();

		Date now = new Date();
		long gap = now.getTime() - endTime.getTime();
		if (gap < allowedGap) {
		    extent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
		}
	    } else {
		// begin is present, end is not present
		extent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(AbstractResourceMapper.class).error("Unable to add indeterminate position. End position is " + endPosition);
	}

    }

    /**
     * @param miMetadata
     * @param gsResource
     */
    private void handleUniqueIdentifiers(GSResource gsResource) {

	MIMetadata miMetadata = gsResource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	//
	// unique platform identifier
	//
	MIPlatform miPlatform = miMetadata.getMIPlatform();

	if (miPlatform != null) {

	    String mdIdentifierCode = miPlatform.getMDIdentifierCode();

	    if (mdIdentifierCode != null && !mdIdentifierCode.isEmpty()) {

		mdIdentifierCode = generateCode(gsResource, mdIdentifierCode);

		gsResource.getExtensionHandler().setUniquePlatformIdentifier(mdIdentifierCode);
	    }
	}

	//
	// unique instrument identifier
	//
	Iterator<MIInstrument> miInstruments = miMetadata.getMIInstruments();

	if (miInstruments.hasNext()) {

	    MIInstrument instrument = miInstruments.next();
	    String code = instrument.getMDIdentifierCode();

	    if (code != null && !code.isEmpty()) {

		code = generateCode(gsResource, code);

		gsResource.getExtensionHandler().setUniqueInstrumentIdentifier(code);
	    }
	}

	//
	// unique attribute identifier
	//
	CoverageDescription coverageDescription = miMetadata.getCoverageDescription();

	if (coverageDescription != null) {

	    String attributeIdentifier = coverageDescription.getAttributeIdentifier();

	    if (attributeIdentifier != null && !attributeIdentifier.isEmpty()) {

		attributeIdentifier = generateCode(gsResource, attributeIdentifier);

		gsResource.getExtensionHandler().setUniqueAttributeIdentifier(attributeIdentifier);
	    }
	}
    }

    /**
     * @param gsResource
     * @param indentifier
     * @return
     */
    private String generateCode(GSResource gsResource, String indentifier) {

	String out = gsResource.getSource().getUniqueIdentifier() + indentifier;

	try {
	    out = StringUtils.hashSHA1messageDigest(indentifier);
	} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
	    e.printStackTrace();
	}

	return out;
    }

    @Override
    @JsonIgnore
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    /**
     * Default implementation returns false.
     */
    @Override
    public Boolean supportsOriginalMetadata(OriginalMetadata originalMD) {
	return false;
    }

    public String normalizeDate(String date) {
	Optional<Date> parsed = ISO8601DateTimeUtils.parseISO8601ToDate(date);
	if (parsed.isPresent()) {
	    return ISO8601DateTimeUtils.getISO8601DateTime(parsed.get());
	} else {
	    return date;
	}
    }
}
