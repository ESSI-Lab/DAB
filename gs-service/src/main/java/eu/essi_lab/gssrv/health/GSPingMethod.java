package eu.essi_lab.gssrv.health;

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

import com.indeed.status.core.PingMethod;
import com.indeed.status.core.Urgency;

import eu.essi_lab.configuration.ExecutionMode;

/**
 * @author Fabrizio
 */
public interface GSPingMethod extends PingMethod {

    /**
     * @return
     */
    String getDescription();

    /**
     * @param mode
     * @return
     */
    Boolean applicableTo(ExecutionMode mode);

    /**
     * @return
     */
    public default String getId() {

	return getClass().getName();
    }

    /**
     * @return
     */
    public default Urgency getUrgency() {

	return Urgency.REQUIRED;
    }
}
