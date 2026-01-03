/**
 * 
 */
package eu.essi_lab.pdk.rsm.impl.atom;

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

import java.util.Objects;

import eu.essi_lab.lib.xml.atom.CustomEntry;

/**
 * @author Fabrizio
 */
public class SentinelTag extends SatelliteTag {

    /**
     * @param instrumentOpMode
     */
    public void setInstrumentOpMode(String instrumentOpMode) {

	if (Objects.nonNull(instrumentOpMode)) {

	    CustomEntry.addContentTo(acquisition, "instrumentOpMode", instrumentOpMode);
	}
    }

    /**
     * @param relativeOrbit
     */
    public void setRelativeOrbit(String relativeOrbit) {

	if (Objects.nonNull(relativeOrbit)) {

	    CustomEntry.addContentTo(acquisition, "relativeOrbit", relativeOrbit);
	}
    }

    /**
     * @param footprint
     */
    public void setFootprint(String footprint) {

	if (Objects.nonNull(footprint)) {

	    CustomEntry.addContentTo(acquisition, "footprint", footprint);
	}
    }

    /**
     * @param processingbaseline
     */
    public void setProcessingbaseline(String processingbaseline) {

	if (Objects.nonNull(processingbaseline)) {

	    CustomEntry.addContentTo(acquisition, "processingbaseline", processingbaseline);
	}
    }

    /**
     * @param processinglevel
     */
    public void setProcessinglevel(String processinglevel) {

	if (Objects.nonNull(processinglevel)) {

	    CustomEntry.addContentTo(acquisition, "processinglevel", processinglevel);
	}
    }

    /**
     * @param s3InstrumentIdx
     */
    public void setS3InstrumentIdx(String s3InstrumentIdx) {

	if (Objects.nonNull(s3InstrumentIdx)) {

	    CustomEntry.addContentTo(acquisition, "s3InstrumentIdx", s3InstrumentIdx);
	}
    }

    /**
     * @param s3Timeliness
     */
    public void setS3Timeliness(String s3Timeliness) {

	if (Objects.nonNull(s3Timeliness)) {

	    CustomEntry.addContentTo(acquisition, "s3Timeliness", s3Timeliness);
	}
    }

    /**
     * @param s3ProductLevel
     */
    public void setS3ProductLevel(String s3ProductLevel) {

	if (Objects.nonNull(s3ProductLevel)) {

	    CustomEntry.addContentTo(acquisition, "s3ProductLevel", s3ProductLevel);
	}
    }

    /**
     * @param startOrbitNumber
     */
    public void setStartOrbitNumber(String startOrbitNumber) {

	if (Objects.nonNull(startOrbitNumber)) {

	    CustomEntry.addContentTo(acquisition, "startOrbitNumber", startOrbitNumber);
	}
    }

    /**
     * @param orbitdirection
     */
    public void setOrbitdirection(String orbitdirection) {

	if (Objects.nonNull(orbitdirection)) {

	    CustomEntry.addContentTo(acquisition, "orbitdirection", orbitdirection);
	}
    }

    /**
     * @param sensorPolarisation
     */
    public void setSensorPolarisation(String sensorPolarisation) {

	if (Objects.nonNull(sensorPolarisation)) {

	    CustomEntry.addContentTo(acquisition, "sensorPolarisation", sensorPolarisation);
	}
    }

    /**
     * @param productconsolidation
     */
    public void setProductConsolidation(String productconsolidation) {

	if (Objects.nonNull(productconsolidation)) {

	    CustomEntry.addContentTo(acquisition, "productconsolidation", productconsolidation);
	}
    }

    /**
     * @param missiondatatakeid
     */
    public void setMissiondatatakeid(String missiondatatakeid) {

	if (Objects.nonNull(missiondatatakeid)) {

	    CustomEntry.addContentTo(acquisition, "missiondatatakeid", missiondatatakeid);

	}
    }

    /**
     * @param productclass
     */
    public void setProductclass(String productclass) {

	if (Objects.nonNull(productclass)) {

	    CustomEntry.addContentTo(acquisition, "productclass", productclass);

	}
    }

    /**
     * @param acquisitiontype
     */
    public void setAcquisitiontype(String acquisitiontype) {
	if (Objects.nonNull(acquisitiontype)) {

	    CustomEntry.addContentTo(acquisition, "acquisitiontype", acquisitiontype);
	}
    }

    /**
     * @param slicenumber
     */
    public void setSlicenumber(String slicenumber) {
	if (Objects.nonNull(slicenumber)) {

	    CustomEntry.addContentTo(acquisition, "slicenumber", slicenumber);
	}
    }

    /**
     * @param stopRelativeOrbitNumber
     */
    public void setStopRelativeOrbitNumber(String stopRelativeOrbitNumber) {
	if (Objects.nonNull(stopRelativeOrbitNumber)) {

	    CustomEntry.addContentTo(acquisition, "stopRelativeOrbitNumber", stopRelativeOrbitNumber);
	}
    }

    /**
     * @param stopOrbitNumber
     */
    public void setStopOrbitNumber(String stopOrbitNumber) {
	if (Objects.nonNull(stopOrbitNumber)) {

	    CustomEntry.addContentTo(acquisition, "stopOrbitNumber", stopOrbitNumber);
	}
    }

    /**
     * @param status
     */
    public void setStatus(String status) {
	if (Objects.nonNull(status)) {

	    CustomEntry.addContentTo(acquisition, "status", status);
	}
    }
}
