package eu.essi_lab.workflow.processor;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author boldrini
 */
public class BooleanCapabilityElement extends CapabilityElement<Boolean> {

    /**
     * @param value
     */
    public BooleanCapabilityElement(Boolean value) {
	super(value);
    }

    /**
     * @param presence
     */
    public BooleanCapabilityElement(PresenceType presence) {
	super(presence, Arrays.asList(true, false));

    }

    /**
     * @param object
     */
    public BooleanCapabilityElement(CapabilityElement<?> object) {

	super(object.getPresence(), object.getValues().stream().map(v -> (Boolean) v).collect(Collectors.toList()));
    }

}
