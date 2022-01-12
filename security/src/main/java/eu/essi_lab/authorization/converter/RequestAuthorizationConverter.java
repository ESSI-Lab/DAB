package eu.essi_lab.authorization.converter;

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
//package eu.essi_lab.authorization;
//
//import eu.essi_lab.messages.GDiscoveryMessage;
//import eu.essi_lab.messages.bond.Bond;
//import eu.essi_lab.model.exceptions.ErrorInfo;
//import eu.essi_lab.model.exceptions.GSException;
//
//public class RequestAuthorizationConverter implements IRequestAuthorizationConverter {
////    @Override
//    public void modifyRequest(DiscoveryMessage message) throws GSException {
//	Bond bond = message.getBond();
//	Bond clone = null;
//	try {
//	    if (bond != null) {
//		clone = bond.clone();
//	    }
//	} catch (Exception e) {
//	    e.printStackTrace();
//	    GSException exception = new GSException();
//	    ErrorInfo info = new ErrorInfo();
//	    info.setContextId(RequestAuthorizationConverter.class.getName());
//	    info.setErrorId(Bond.CLONE_BOND_FAILED);
//	    info.setErrorDescription("Error cloning the bond.");
//	    info.setUserErrorDescription("Modify query failed, you could try to write a simpler query");
//	    info.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
//	    info.setSeverity(ErrorInfo.SEVERITY_ERROR);
//	    exception.addInfo(info);
//	    throw exception;
//	}
//
//	message.setPermittedBond(bond);
//    }
//
//}
