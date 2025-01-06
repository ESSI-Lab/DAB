package eu.essi_lab.workflow.processor;

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

import java.util.Objects;

import eu.essi_lab.workflow.processor.CapabilityElement.PresenceType;

/**
 * Indicates if it's possible to resample data on a specified axis. E.g.: change resolution from meters to kilometers.
 */
public class ResamplingCapability {

    BooleanCapabilityElement spatialResampling = null;
    BooleanCapabilityElement temporalResampling = null;
    BooleanCapabilityElement otherResampling = null;

    public ResamplingCapability(BooleanCapabilityElement spatialResampling, BooleanCapabilityElement temporalResampling,
	    BooleanCapabilityElement otherResampling) {
	if (spatialResampling == null || temporalResampling == null || otherResampling == null) {
	    throw new IllegalArgumentException("Null arguments in the constructor");
	}
	this.spatialResampling = spatialResampling;
	this.temporalResampling = temporalResampling;
	this.otherResampling = otherResampling;
    }

    public static ResamplingCapability ANY_RESAMPLING() {
	return new ResamplingCapability(new BooleanCapabilityElement(PresenceType.ANY), new BooleanCapabilityElement(PresenceType.ANY),
		new BooleanCapabilityElement(PresenceType.ANY));
    }

    public static ResamplingCapability SAME_AS_RESAMPLING() {
	return new ResamplingCapability(new BooleanCapabilityElement(PresenceType.SAME_AS),
		new BooleanCapabilityElement(PresenceType.SAME_AS), new BooleanCapabilityElement(PresenceType.SAME_AS));
    }

    public static ResamplingCapability NO_RESAMPLING() {
	return new ResamplingCapability(new BooleanCapabilityElement(false), new BooleanCapabilityElement(false),
		new BooleanCapabilityElement(false));
    }

    public static ResamplingCapability OTHER_RESAMPLING() {
	return new ResamplingCapability(new BooleanCapabilityElement(false), new BooleanCapabilityElement(false),
		new BooleanCapabilityElement(true));
    }

    public static ResamplingCapability SPATIAL_RESAMPLING() {
	return new ResamplingCapability(new BooleanCapabilityElement(true), new BooleanCapabilityElement(false),
		new BooleanCapabilityElement(false));
    }

    public static ResamplingCapability TEMPORAL_RESAMPLING() {
	return new ResamplingCapability(new BooleanCapabilityElement(false), new BooleanCapabilityElement(true),
		new BooleanCapabilityElement(false));
    }

    public static ResamplingCapability SPATIAL_TEMPORAL_RESAMPLING() {
	return new ResamplingCapability(new BooleanCapabilityElement(true), new BooleanCapabilityElement(true),
		new BooleanCapabilityElement(false));
    }

    public static ResamplingCapability SPATIAL_TEMPORAL_OTHER_RESAMPLING() {
	return new ResamplingCapability(new BooleanCapabilityElement(true), new BooleanCapabilityElement(true),
		new BooleanCapabilityElement(true));
    }

    public BooleanCapabilityElement getSpatialResampling() {
	return spatialResampling;
    }

    public BooleanCapabilityElement getTemporalResampling() {
	return temporalResampling;
    }

    public BooleanCapabilityElement getOtherResampling() {
	return otherResampling;
    }

    @Override
    public String toString() {
	return "s[" + spatialResampling + "] " + "t[" + temporalResampling + "] " + "o[" + otherResampling + "]";
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof ResamplingCapability) {
	    ResamplingCapability sub = (ResamplingCapability) obj;
	    return Objects.equals(spatialResampling, sub.spatialResampling) && //
		    Objects.equals(temporalResampling, sub.temporalResampling) && //
		    Objects.equals(otherResampling, sub.otherResampling) //
	    ;
	}
	return super.equals(obj);
    }

    public boolean accept(ResamplingCapability output) {
	ResamplingCapability out = (ResamplingCapability) output;

	return spatialResampling.accept(out.spatialResampling) && //
		temporalResampling.accept(out.temporalResampling) && //
		otherResampling.accept(out.otherResampling); //

    }

}
