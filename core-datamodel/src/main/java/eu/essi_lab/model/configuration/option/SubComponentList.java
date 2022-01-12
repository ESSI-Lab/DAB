package eu.essi_lab.model.configuration.option;

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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import eu.essi_lab.model.configuration.GSJSONSerializable;
import eu.essi_lab.model.configuration.Subcomponent;

@JsonTypeInfo(use = Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "concrete")
public class SubComponentList extends GSJSONSerializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5239347459155604936L;

    @JsonProperty("subcomponents")
    private List<Subcomponent> list = new ArrayList<>();

    public SubComponentList() {
    }

    /**
     * @return
     */
    @JsonIgnore
    public List<Subcomponent> getList() {

	return list;
    }

    /**
     * @param option
     */
    public void add(Subcomponent option) {

	list.add(option);
    }
}
