package eu.essi_lab.iso.datamodel.todo;

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

import eu.essi_lab.iso.datamodel.classes.CoverageDescription;

public class ImageDescription extends CoverageDescription {

    /**
    *    @XPathDirective(target = "gmd:illuminationAzimuthAngle/gco:Real")
    */
    String getIlluminationAzimuthAngle() {
	return null;
    }

    /**
    *    @XPathDirective(target = "gmd:illuminationAzimuthAngle/gco:Real")
    */
    void setIlluminationAzimuthAngle(String value) {
    }

    /**
    *    @XPathDirective(target = "gmd:illuminationZenithAngle/gco:Real")
    */
    String getIlluminationZenithAngle() {
	return null;
    }

    /**
    *    @XPathDirective(target = "gmd:illuminationZenithAngle/gco:Real")
    */
    void setIlluminationZenithAngle(String value) {
    }

    /**
    *    @XPathDirective(target = "gmd:illuminationElevationAngle/gco:Real")
    */
    String getIlluminationElevationAngle() {
	return null;
    }

    /**
    *    @XPathDirective(target = "gmd:illuminationElevationAngle/gco:Real")
    */
    void setIlluminationElevationAngle(String value) {
    }

    /**
    *    @XPathDirective(target = "gmd:imageQualityCode/gmd:MD_Identifier/gmd:code/gco:CharacterString")
    */
    String getImageQualityCode() {
	return null;
    }

    /**
    *    @XPathDirective(target = "gmd:imageQualityCode/gmd:MD_Identifier/gmd:code/gco:CharacterString")
    */
    void setImageQualityCode(String value) {
    }

    /**
    *    @XPathDirective(target = "gmd:cloudCoverPercentage/gco:Real")
    */
    String getCloudCoverPercentage() {
	return null;
    }

    /**
    *    @XPathDirective(target = "gmd:cloudCoverPercentage/gco:Real")
    */
    void setCloudCoverPercentage(String percentage) {
    }

    /**
    *    @XPathDirective(target = "gmd:processingLevelCode/gmd:MD_Identifier/gmd:code/gco:CharacterString")
    */
    String getProcessingLevelCode() {
	return null;
    }

    /**
    *    @XPathDirective(target = "gmd:processingLevelCode/gmd:MD_Identifier/gmd:code/gco:CharacterString")
    */
    void setProcessingLevelCode(String name) {
    }

    // @XPathDirective(target = "gmd:dimension/gmd:MD_RangeDimension/gmd:descriptor/gco:CharacterString")
    // void setRangeDimension(String descriptor){}
    //
    // @XPathDirective(target = "gmd:dimension/gmd:MD_RangeDimension/gmd:descriptor/gco:CharacterString")
    // String getRangeDimension(){}
    //
    // @XPathDirective(target = "gmd:dimension/gmd:MD_Band/gmd:maxValue/gco:Real")
    // void setRangeMax(Double value){}
    //
    // @XPathDirective(target = "gmd:dimension/gmd:MD_Band/gmd:maxValue/gco:Real")
    // Double getRangeMax(){}
    //
    // @XPathDirective(target = "gmd:dimension/gmd:MD_Band/gmd:minValue/gco:Real")
    // void setRangeMin(Double value){}
    //
    // @XPathDirective(target = "gmd:dimension/gmd:MD_Band/gmd:minValue/gco:Real")
    // Double getRangeMin(){}
    //
    // @XPathDirective(target = "gmd:dimension/gmd:MD_Band/gmd:bitsPerValue/gco:Integer")
    // void setBitsPerValue(Integer value){}
    //
    // @XPathDirective(target = "gmd:dimension/gmd:MD_Band/gmd:bitsPerValue/gco:Integer")
    // Integer getBitsPerValue(){}
    //
    // @XPathDirective(target = "gmd:dimension/gmd:MD_Band/gmd:descriptor/gco:CharacterString")
    // void setBandDimensionDescription(String descriptor){}
    //
    // @XPathDirective(target = "gmd:dimension/gmd:MD_Band/gmd:descriptor/gco:CharacterString")
    // String getBandDimensionDescription(){}
    //
    // @XPathDirective(target = ".", parent = "gmd:dimension", position = Position.LAST)
    // void addBand(IMD_Band band){}
    //
    // @XPathDirective(target = "gmd:dimension/gmd:MD_Band | gmd:dimension/gmi:MI_Band")
    // Iterator<IMD_Band> getBands(){}

}
