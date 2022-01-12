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

import com.fasterxml.jackson.annotation.JsonInclude;

import eu.essi_lab.model.Source;
import eu.essi_lab.model.configuration.GSJSONSerializable;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class VolatileSourceAdd extends GSJSONSerializable {

    private Source source;

    private GSConfiguration context;

    public GSConfiguration getContext() {
	return context;
    }

    public void setContext(GSConfiguration context) {
	this.context = context;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source s) {
	source = s;
    }

}
