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

import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class GSConfOptionBrokeringStrategy extends GSConfOption<BrokeringStrategy> {

    private static final String ERR_ID_INVALID_STRATEGY = "ERR_ID_INVALID_STRATEGY";
    private List<BrokeringStrategy> supported;

    public GSConfOptionBrokeringStrategy() {
	super(BrokeringStrategy.class);

    }

    public GSConfOptionBrokeringStrategy(List<BrokeringStrategy> supportedBrokeringStrategies) {

	this();
	setSupported(supportedBrokeringStrategies);
    }

    @Override
    public void validate() throws GSException {

	if (getSupported() == null)
	    return;
	if (getSupported().contains(getValue()))
	    return;

	GSException ex = new GSException();

	ErrorInfo ei = new ErrorInfo();

	ei.setContextId(this.getClass().getName());
	ei.setErrorId(ERR_ID_INVALID_STRATEGY);

	ei.setUserErrorDescription("The provided Brokering Strategy is not valid.");
	ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	ei.setSeverity(ErrorInfo.SEVERITY_WARNING);

	ex.addInfo(ei);

	throw ex;

    }

    @Override
    public List<BrokeringStrategy> getAllowedValues() {

	return getSupported();
    }

    public List<BrokeringStrategy> getSupported() {
	return supported;
    }

    public void setSupported(List<BrokeringStrategy> supported) {
	this.supported = supported;
    }

}
