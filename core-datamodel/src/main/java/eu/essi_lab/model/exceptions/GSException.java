package eu.essi_lab.model.exceptions;

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

public class GSException extends Exception {
    protected final List<ErrorInfo> errorInfoList = new ArrayList<>();    private static final long serialVersionUID = 4205641760399814704L;

    public ErrorInfo addInfo(ErrorInfo info) {
	this.errorInfoList.add(info);
	return info;
    }

    public void addInfoList(List<ErrorInfo> infoList) {
	this.errorInfoList.addAll(infoList);
    }

    public List<ErrorInfo> getErrorInfoList() {
	return errorInfoList;
    }

    /**
     * Creates an exception with a single {@link ErrorInfo} and all the properties set
     *
     * @param clazz the caller class; class name is used for {@link ErrorInfo#setContextId(String)}
     * @param errorDescription see {@link ErrorInfo#setErrorDescription(String)}
     * @param errorCorrection see {@link ErrorInfo#setErrorCorrection(String)}
     * @param userError see {@link ErrorInfo#setUserErrorDescription(String)}
     * @param errorType see {@link ErrorInfo#setErrorType(int)}
     * @param errorSeverity see {@link ErrorInfo#setSeverity(int)}
     * @param errorId see {@link ErrorInfo#setErrorId(String)}
     * @param cause see {@link ErrorInfo#setCause(Throwable)}
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static GSException createException(Class clazz, String errorDescription, String errorCorrection, String userError, int errorType,
	    int errorSeverity, String errorId, Throwable cause) {

	GSException gsException = new GSException();

	ErrorInfo errorInfo = new ErrorInfo();
	errorInfo.setContextId(clazz.getName());
	errorInfo.setErrorId(errorId);
	errorInfo.setCause(cause);
	errorInfo.setErrorDescription(errorDescription);
	errorInfo.setUserErrorDescription(userError);
	errorInfo.setErrorCorrection(errorCorrection);
	errorInfo.setErrorType(errorType);
	errorInfo.setSeverity(errorSeverity);

	gsException.addInfo(errorInfo);

	return gsException;
    }

    /**
     * Creates an exception with a single {@link ErrorInfo} and all the properties set except
     * {@link ErrorInfo#getCause()}
     *
     * @param clazz the caller class; class name is used for {@link ErrorInfo#setContextId(String)}
     * @param errorDescription see {@link ErrorInfo#setErrorDescription(String)}
     * @param errorCorrection see {@link ErrorInfo#setErrorCorrection(String)}
     * @param userError see {@link ErrorInfo#setUserErrorDescription(String)}
     * @param errorType see {@link ErrorInfo#setErrorType(int)}
     * @param errorSeverity see {@link ErrorInfo#setSeverity(int)}
     * @param errorId see {@link ErrorInfo#setErrorId(String)}
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static GSException createException(Class clazz, String errorDescription, String errorCorrection, String userError, int errorType,
	    int errorSeverity, String errorId) {

	return createException(clazz, errorDescription, errorCorrection, userError, errorType, errorSeverity, errorId, null);
    }

    /**
     * Creates an exception with a single {@link ErrorInfo} and all the properties set except
     * {@link ErrorInfo#getErrorCorrection()()}
     *
     * @param clazz the caller class; class name is used for {@link ErrorInfo#setContextId(String)}
     * @param errorDescription see {@link ErrorInfo#setErrorDescription(String)}
     * @param userError see {@link ErrorInfo#setUserErrorDescription(String)}
     * @param errorType see {@link ErrorInfo#setErrorType(int)}
     * @param errorSeverity see {@link ErrorInfo#setSeverity(int)}
     * @param errorId see {@link ErrorInfo#setErrorId(String)}
     * @param cause see {@link ErrorInfo#setCause(Throwable)}
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static GSException createException(Class clazz, String errorDescription, String userError, int errorType, int errorSeverity,
	    String errorId, Throwable cause) {

	return createException(clazz, errorDescription, null, userError, errorType, errorSeverity, errorId, cause);
    }

    /**
     * Creates an exception with a single {@link ErrorInfo} and all the properties set except
     * {@link ErrorInfo#getErrorCorrection()()} and {@link ErrorInfo#getCause()}
     *
     * @param clazz the caller class; class name is used for {@link ErrorInfo#setContextId(String)}
     * @param errorDescription see {@link ErrorInfo#setErrorDescription(String)}
     * @param userError see {@link ErrorInfo#setUserErrorDescription(String)}
     * @param errorType see {@link ErrorInfo#setErrorType(int)}
     * @param errorSeverity see {@link ErrorInfo#setSeverity(int)}
     * @param errorId see {@link ErrorInfo#setErrorId(String)}
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static GSException createException(Class clazz, String errorDescription, String userError, int errorType, int errorSeverity,
	    String errorId) {

	return createException(clazz, errorDescription, null, userError, errorType, errorSeverity, errorId, null);
    }
}
