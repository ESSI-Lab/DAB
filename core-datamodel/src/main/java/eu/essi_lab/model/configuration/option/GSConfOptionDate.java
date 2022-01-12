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

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.essi_lab.model.exceptions.GSException;
public class GSConfOptionDate extends GSConfOption<Date> {

    public GSConfOptionDate() {
	super(Date.class);
    }

    @Override
    public void validate() throws GSException {

    }

    @Override
    @JsonDeserialize(using = DateDeserializer.class)
    public void setValue(Date v) {

	super.setValue(v);

    }

    @Override
    //    @JsonSerialize(using = GSDateDeserializer)
    @JsonSerialize(using = GSDateSerializer.class)
    public Date getValue() {

	return super.getValue();

    }

    @Override
    public List<Date> getAllowedValues() {
	return null;
    }
}
