package eu.essi_lab.jobs.scheduler;

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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonDeserialize(using = GS_JOB_INTERVAL_PERIODDeserializer.class)
public enum GS_JOB_INTERVAL_PERIOD {

    MONTHS("MONTHS"), //
    WEEKS("WEEKS"), //
    DAYS("DAYS"), //
    HOURS("HOURS"), //
    MINUTES("MINUTES"),
    SECONDS("SECONDS");

    @JsonProperty("value")
    private String name;

    private GS_JOB_INTERVAL_PERIOD(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }
}
