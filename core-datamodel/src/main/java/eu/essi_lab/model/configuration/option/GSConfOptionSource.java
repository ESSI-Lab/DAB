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

import java.util.List;

import eu.essi_lab.model.Source;
import eu.essi_lab.model.exceptions.GSException;

public class GSConfOptionSource extends GSConfOption<Source> {

    private static final String ERR_ID_INVALID_SOURCE = "ERR_ID_INVALID_SOURCE";
    private Source source;

    public GSConfOptionSource() {
	super(Source.class);
    }

    public Source getSource() {
	return source;
    }

    public void setSource(Source s) {
	source = s;
    }

    @Override
    public void validate() throws GSException {


    }

    @Override
    public List<Source> getAllowedValues() {
	// TODO Auto-generated method stub
	return null;
    }

}
