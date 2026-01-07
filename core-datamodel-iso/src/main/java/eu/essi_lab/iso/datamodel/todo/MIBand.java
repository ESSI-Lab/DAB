package eu.essi_lab.iso.datamodel.todo;

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

public class MIBand extends MDBand {

    /**
    *    @XPathDirective(target = "gmi:transmittedPolarisazion/gmi:MI_PolarisationOrientationCode/@codeListValue")
    */
    public void setTransimettedPolarisationOrientationCode(PolarizationOrientationCodes code) {
    }

    /**
    *    @XPathDirective(target = "gmi:detectedPolarisation/gmi:MI_PolarisationOrientationCode/@codeListValue")
    */
    public void setDetectedPolarisationOrientationCode(PolarizationOrientationCodes code) {
    }

    /**
    *    @XPathDirective(target = "gmd:descriptor/gco:CharacterString")
    */
    public void setDescriptor(String val) {
    }

    /**
    *    @XPathDirective(target = "gmd:minValue/gco:Real")
    */
    public void setMinValue(Double val) {
    }

    /**
    *    @XPathDirective(target = "gmd:maxValue/gco:Real")
    */
    public void setMaxValue(Double val) {
    }

    /**
    *    @XPathDirective(target = "gmd:peakResponse/gco:Real")
    */
    public void setPeakResponse(Double val) {
    }

    /**
    *    @XPathDirective(target = "gmd:peakResponse/gco:Real")
    */
    public Double getPeakResponse() {
	return null;
    }

    /**
    *    @XPathDirective(target = "gmd:units/gml:UnitDefinition/gml:identifier")
    */
    public void setUnits(String units) {
    }

    /**
    *    @XPathDirective(target = "gmi:nominalSpatialResolution/gco:Real")
    */
    public void setNominalSpatialResolution(Double val) {
    }

    /**
    *    @XPathDirective(target = "gmi:nominalSpatialResolution/gco:Real")
    */
    public Double getNominalSpatialResolution() {
	return null;
    }

    /**
    *    @XPathDirective(target = "gmi:nominalSpatialResolution/gco:Distance/@uom")
    */
    public void setNominalSpatialResolutionUnits(String uom) {
    }
}
