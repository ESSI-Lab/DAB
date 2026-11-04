package eu.essi_lab.accessor.wof;

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

import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.accessor.wof.client.datamodel.SitesResponseDocument;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;

public class WML_1_1_INAMapper extends WML_1_1Mapper {

    
    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.WML1_INA_NS_URI;
    }

    public String getSourceOrganization(TimeSeries series) {
	return "Instituto Nacional del Agua (INA)";
    }

    /**
     * INA aggregates sites from several agencies (e.g. Prefectura Naval Argentina): besides INA as point of contact, the
     * agency of the site is added as originator
     */
    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {
	GSResource resource = super.execMapping(originalMD, source);
	try {
	    SitesResponseDocument document = new SitesResponseDocument(originalMD.getMetadata());
	    SiteInfo siteInfo = document.getSitesInfo().get(0);
	    String agency = siteInfo.getReader().evaluateString("*:siteCode/@agencyName");
	    if (agency == null || agency.isEmpty()) {
		agency = siteInfo.getSeries().get(0).getSourceDescription();
	    }
	    if (agency != null && !agency.isEmpty()) {
		ResponsibleParty originator = new ResponsibleParty();
		originator.setOrganisationName(agency);
		originator.setRoleCode("originator");
		resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().addPointOfContact(originator);
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Unable to map originator agency: {}", e.getMessage());
	}
	return resource;
    }

    /**
     * INA publishes the source contact with capitalized element names (e.g. ContactInformation/Email) instead of the
     * WaterML 1.1 ones (contactInformation/email), so the base implementation finds nothing
     */
    @Override
    public ResponsibleParty getContactInfo(TimeSeries series) {
	ResponsibleParty datasetContact = super.getContactInfo(series);
	datasetContact.setIndividualName(getINAContactField(series, "ContactName"));
	datasetContact.setPositionName(getINAContactField(series, "TypeOfContact"));
	Contact contactInfo = datasetContact.getContact();
	Address address = new Address();
	address.addElectronicMailAddress(getINAContactField(series, "Email"));
	address.addDeliveryPoint(getINAContactField(series, "Address"));
	contactInfo.setAddress(address);
	datasetContact.setContactInfo(contactInfo);
	return datasetContact;
    }

    private String getINAContactField(TimeSeries series, String field) {
	try {
	    return series.getReader().evaluateString("*:source/*:ContactInformation/*:" + field);
	} catch (Exception e) {
	    return "";
	}
    }

    // private boolean is30DaysSeries(TimeSeries series) {
    // String unit = super.getTimeScaleUnitAbbreviation(series);
    // Number support = super.getTimeScaleTimeSupport(series);
    // return unit.equals("day") && support.longValue() == 30;
    //
    // }
    //
    // @Override
    // public Number getTimeScaleTimeSupport(TimeSeries series) {
    // if (is30DaysSeries(series)) {
    // return 1;
    // }
    // return super.getTimeScaleTimeSupport(series);
    // }
    //
    // @Override
    // public String getTimeScaleUnitName(TimeSeries series) {
    //// if (is30DaysSeries(series)) {
    //// return "month";
    //// }
    // return super.getTimeScaleUnitName(series);
    // }
    //
    // @Override
    // public String getTimeScaleUnitAbbreviation(TimeSeries series) {
    //// if (is30DaysSeries(series)) {
    //// return "mon";
    //// }
    // return super.getTimeScaleUnitAbbreviation(series);
    // }

    public String getUnitName(TimeSeries series) {
	String ret = series.getUnitName();
	if (ret.equals("milímetros por día")) {
	    return "milímetros";
	}
	return ret;
    }

    /**
     * INA misspells the element as unitAbreviation
     */
    @Override
    public String getUnitAbbreviation(TimeSeries series) {
	String ret = super.getUnitAbbreviation(series);
	if (ret == null || ret.isEmpty()) {
	    try {
		ret = series.getReader().evaluateString("*:variable/*:unit/*:unitAbreviation");
	    } catch (Exception e) {
		ret = "";
	    }
	}
	return ret;
    }

    @Override
    public InterpolationType getInterpolationType(TimeSeries series) {
	InterpolationType interpolation = super.getInterpolationType(series);
	InterpolationType proposal = null;
	String variableCode = series.getVariableCode();
	switch (variableCode) {
	case "50":
	case "52":
	    proposal = InterpolationType.MAX_SUCC;
	    break;
	case "49":
	case "51":
	    proposal = InterpolationType.MIN_SUCC;
	    break;
	case "1":
	case "31":
	case "34":
	    proposal = InterpolationType.TOTAL_SUCC;
	    break;
	case "27":
	case "38":
	    proposal = InterpolationType.TOTAL_PREC;
	    break;
	case "48":
	case "39":
	case "40":
	case "67":
	case "33":
	case "61":
	case "56":
	case "59":
	case "54":
	    proposal = InterpolationType.AVERAGE_SUCC;
	    break;
	case "14":
	    proposal = InterpolationType.AVERAGE_PREC;
	    break;
	default:
	    break;
	}
	if (proposal != null && interpolation != null) {
	    switch (proposal) {
	    case MAX_SUCC:
		if (!interpolation.equals(InterpolationType.MAX)) {
		    errorRefine(interpolation, proposal);
		} else {
		    interpolation = proposal;
		}
		break;
	    case MIN_SUCC:
		if (!interpolation.equals(InterpolationType.MIN)) {
		    errorRefine(interpolation, proposal);
		} else {
		    interpolation = proposal;
		}
		break;
	    case TOTAL_PREC:
	    case TOTAL_SUCC:
		if (!interpolation.equals(InterpolationType.TOTAL)) {
		    errorRefine(interpolation, proposal);
		} else {
		    interpolation = proposal;
		}
		break;
	    case AVERAGE_PREC:
	    case AVERAGE_SUCC:
		if (!interpolation.equals(InterpolationType.AVERAGE)) {
		    errorRefine(interpolation, proposal);
		} else {
		    interpolation = proposal;
		}
		break;
	    default:
		break;
	    }
	}
	return interpolation;
    }

    private void errorRefine(InterpolationType interpolation, InterpolationType proposal) {
	GSLoggerFactory.getLogger(getClass()).error("Not possible to refine from: {} to {}", interpolation, proposal);
    }
}
