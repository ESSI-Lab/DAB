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

import com.google.common.base.Objects;

import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
public class GSConfOptionInteger extends GSConfOption<Integer> {

    private static final String ERR_ID_NON_BOOLEAN_VALUE = "ERR_ID_NON_BOOLEAN_VALUE";
    private static final String ERR_ID_NULL_VALUE = "ERR_ID_NULL_VALUE";

    public GSConfOptionInteger() {
	super(Integer.class);
    }

    @Override
    public void validate() throws GSException {
	if (getValue() == null)

	    throw GSException.createException(this.getClass(), "Null value for " + this.getClass().getName(),
		    "Please use a valid integer value for this option (" + this.getLabel() + ")", ErrorInfo.ERRORTYPE_CLIENT,
		    ErrorInfo.SEVERITY_ERROR, ERR_ID_NULL_VALUE);

	if (getAllowedValues() == null)
	    return;

	for (Integer b : getAllowedValues()) {
	    if (b.toString().equalsIgnoreCase(getValue().toString()))
		return;
	}

	throw GSException.createException(this.getClass(), "Invalid value for " + this.getClass().getName(),
		"Please use one of 'true' or 'false' for this option (" + this.getLabel() + ")", ErrorInfo.ERRORTYPE_CLIENT,
		ErrorInfo.SEVERITY_ERROR, ERR_ID_NON_BOOLEAN_VALUE);
    }

    @Override
    public List<Integer> getAllowedValues() {

	return null;

    }

    @Override
    public int hashCode() {
	return Objects.hashCode(this.getKey(), this.getValue(), this.getType(), this.getLabel());
    }

    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof GSConfOptionBoolean))
	    return false;
	GSConfOptionBoolean option = (GSConfOptionBoolean) obj;
	if (!Objects.equal(this.getKey(), option.getKey()))
	    return false;
	if (!Objects.equal(this.getValue(), option.getValue()))
	    return false;
	if (!Objects.equal(this.getType(), option.getType()))
	    return false;
	if (!Objects.equal(this.getLabel(), option.getLabel()))
	    return false;
	return true;
    }
}
