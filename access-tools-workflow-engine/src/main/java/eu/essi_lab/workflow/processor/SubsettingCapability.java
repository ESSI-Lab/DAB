package eu.essi_lab.workflow.processor;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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
 * Indicates if it's possible to subset/upset data on a specified axis. E.g.: select a part or a superset of the data.
 */
public class SubsettingCapability {

    BooleanCapabilityElement spatialSubsetting;
    BooleanCapabilityElement temporalSubsetting;
    BooleanCapabilityElement otherSubsetting;

    public SubsettingCapability(BooleanCapabilityElement spatialSubsetting, BooleanCapabilityElement temporalSubsetting,
	    BooleanCapabilityElement otherSubsetting) {
	if (spatialSubsetting == null || temporalSubsetting == null || otherSubsetting == null) {
	    throw new IllegalArgumentException("Null arguments in the constructor");
	}
	this.spatialSubsetting = spatialSubsetting;
	this.temporalSubsetting = temporalSubsetting;
	this.otherSubsetting = otherSubsetting;
    }

    public static SubsettingCapability ANY_SUBSETTING() {
	return new SubsettingCapability(new BooleanCapabilityElement(PresenceType.ANY), new BooleanCapabilityElement(PresenceType.ANY),
		new BooleanCapabilityElement(PresenceType.ANY));
    }

    public static SubsettingCapability SAME_AS_SUBSETTING() {
	return new SubsettingCapability(new BooleanCapabilityElement(PresenceType.SAME_AS),
		new BooleanCapabilityElement(PresenceType.SAME_AS), new BooleanCapabilityElement(PresenceType.SAME_AS));
    }

    public static SubsettingCapability NO_SUBSETTING() {
	return new SubsettingCapability(new BooleanCapabilityElement(false), new BooleanCapabilityElement(false),
		new BooleanCapabilityElement(false));
    }

    public static SubsettingCapability SPATIAL_SUBSETTING() {
	return new SubsettingCapability(new BooleanCapabilityElement(true), new BooleanCapabilityElement(false),
		new BooleanCapabilityElement(false));
    }

    public static SubsettingCapability OTHER_SUBSETTING() {
	return new SubsettingCapability(new BooleanCapabilityElement(false), new BooleanCapabilityElement(false),
		new BooleanCapabilityElement(true));
    }

    public static SubsettingCapability TEMPORAL_SUBSETTING() {
	return new SubsettingCapability(new BooleanCapabilityElement(false), new BooleanCapabilityElement(true),
		new BooleanCapabilityElement(false));
    }

    public static SubsettingCapability SPATIAL_TEMPORAL_SUBSETTING() {
	return new SubsettingCapability(new BooleanCapabilityElement(true), new BooleanCapabilityElement(true),
		new BooleanCapabilityElement(false));
    }

    public static SubsettingCapability SPATIAL_TEMPORAL_OTHER_SUBSETTING() {
	return new SubsettingCapability(new BooleanCapabilityElement(true), new BooleanCapabilityElement(true),
		new BooleanCapabilityElement(true));
    }

    public BooleanCapabilityElement getSpatialSubsetting() {
	return spatialSubsetting;
    }

    public BooleanCapabilityElement getTemporalSubsetting() {
	return temporalSubsetting;
    }

    public BooleanCapabilityElement getOtherSubsetting() {
	return otherSubsetting;
    }

    @Override
    public String toString() {
	return "s[" + spatialSubsetting + "] " + "t[" + temporalSubsetting + "] " + "o[" + otherSubsetting + "]";
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof SubsettingCapability) {
	    SubsettingCapability sub = (SubsettingCapability) obj;
	    return Objects.equals(spatialSubsetting, sub.spatialSubsetting) && //
		    Objects.equals(temporalSubsetting, sub.temporalSubsetting) && //
		    Objects.equals(otherSubsetting, sub.otherSubsetting) //
	    ;
	}
	return super.equals(obj);
    }

    public boolean accept(SubsettingCapability output) {
	SubsettingCapability out = (SubsettingCapability) output;

	return spatialSubsetting.accept(out.spatialSubsetting) && //
		temporalSubsetting.accept(out.temporalSubsetting) && //
		otherSubsetting.accept(out.otherSubsetting); //

    }

}
