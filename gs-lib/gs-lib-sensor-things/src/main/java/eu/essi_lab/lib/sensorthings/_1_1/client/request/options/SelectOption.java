package eu.essi_lab.lib.sensorthings._1_1.client.request.options;

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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.lib.sensorthings._1_1.client.request.Composable;

/**
 * @see https://docs.ogc.org/is/18-088/18-088.html#select4
 * @author Fabrizio
 */
public class SelectOption implements Composable {

    /**
     * 
     */
    private List<String> properties;

    /**
     * @return
     */
    public SelectOption(String... properties) {

	this.properties = Arrays.asList(properties);
    }

    /**
     * @return
     */
    public SelectOption(List<String> properties) {

	this.properties = properties;
    }

    @Override
    public String compose() {

	StringBuilder builder = new StringBuilder();

	builder.append("&$select=");
	builder.append(properties.stream().collect(Collectors.joining(",")));

	return builder.toString();
    }

    @Override
    public String toString() {

	return compose();
    }
}
