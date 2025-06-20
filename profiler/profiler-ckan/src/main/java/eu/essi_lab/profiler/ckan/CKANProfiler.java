package eu.essi_lab.profiler.ckan;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;

/**
 * @author boldrini
 */
public class CKANProfiler extends Profiler<CKANProfilerSetting> {

    private static final String KEY_REQUEST = "request";

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	// Return a list of the names of the site's datasets (packages).\n\n :param limit: if given, the list of
	// datasets will be broken into pages of\n at most ``limit`` datasets per page and only one page will be
	// returned\n at a time (optional)\n :type limit: int\n :param offset: when ``limit`` is given, the offset to
	// start\n returning packages from\n :type offset: int\n\n :rtype: list of strings\n\n

	////////////////////
	// package list
	////////////////////

	// Return a list of the names of the site's datasets (packages).
	//
	// :param limit: if given, the list of datasets will be broken into pages of
	// at most ``limit`` datasets per page and only one page will be returned
	// at a time (optional)
	// :type limit: int
	// :param offset: when ``limit`` is given, the offset to start
	// returning packages from
	// :type offset: int
	//
	// :rtype: list of strings

	selector.register(new GETRequestFilter(CKANOperation.PACKAGE_LIST.getPath()), new PackageListHandler());
	////////////////////
	// group list
	////////////////////

	// Return a list of the names of the site's groups.
	//
	// :param order_by: the field to sort the list by, must be ``'name'`` or
	// ``'packages'`` (optional, default: ``'name'``) Deprecated use sort.
	// :type order_by: string
	// :param sort: sorting of the search results. Optional. Default:
	// "title asc" string of field name and sort-order. The allowed fields are
	// 'name', 'package_count' and 'title'
	// :type sort: string
	// :param limit: the maximum number of groups returned (optional)
	// Default: ``1000`` when all_fields=false unless set in site's
	// configuration ``ckan.group_and_organization_list_max``
	// Default: ``25`` when all_fields=true unless set in site's
	// configuration ``ckan.group_and_organization_list_all_fields_max``
	// :type limit: int
	// :param offset: when ``limit`` is given, the offset to start
	// returning groups from
	// :type offset: int
	// :param groups: a list of names of the groups to return, if given only
	// groups whose names are in this list will be returned (optional)
	// :type groups: list of strings
	// :param all_fields: return group dictionaries instead of just names. Only
	// core fields are returned - get some more using the include_* options.
	// Returning a list of packages is too expensive, so the `packages`
	// property for each group is deprecated, but there is a count of the
	// packages in the `package_count` property.
	// (optional, default: ``False``)
	// :type all_fields: bool
	// :param include_dataset_count: if all_fields, include the full package_count
	// (optional, default: ``True``)
	// :type include_dataset_count: bool
	// :param include_extras: if all_fields, include the group extra fields
	// (optional, default: ``False``)
	// :type include_extras: bool
	// :param include_tags: if all_fields, include the group tags
	// (optional, default: ``False``)
	// :type include_tags: bool
	// :param include_groups: if all_fields, include the groups the groups are in
	// (optional, default: ``False``).
	// :type include_groups: bool
	// :param include_users: if all_fields, include the group users
	// (optional, default: ``False``).
	// :type include_users: bool
	//
	// :rtype: list of strings

	return selector;
    }

    @Override
    public Response createUncaughtError(WebRequest webRequest, Status status, String message) {

	ValidationMessage vm = new ValidationMessage();
	vm.setError(message);
	vm.setErrorCode(ExceptionCode.NO_APPLICABLE_CODE.toString());

	return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_XML).entity("").build();
    }

    @Override
    protected Response onValidationFailed(WebRequest request, ValidationMessage message) {

	return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_XML).entity("").build();
    }

    @Override
    protected Response onHandlerNotFound(WebRequest request) {

	KeyValueParser parser = new KeyValueParser(request.getQueryString());
	String req = parser.getValue(KEY_REQUEST, true);
	ValidationMessage message = new ValidationMessage();

	if (req == null) {

	    message.setError("Missing mandatory request parameter");
	    message.setErrorCode(ExceptionCode.MISSING_PARAMETER.toString());
	    message.setLocator(KEY_REQUEST);
	} else {
	    message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
	    message.setError("Invalid request parameter");
	    message.setLocator(KEY_REQUEST);
	}

	return onValidationFailed(request, message);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    protected CKANProfilerSetting initSetting() {

	return new CKANProfilerSetting();
    }

    public static void main(String[] args) {
	System.out.println(
		"Return a list of the names of the site's datasets (packages).\n\n    :param limit: if given, the list of datasets will be broken into pages of\n        at most ``limit`` datasets per page and only one page will be returned\n        at a time (optional)\n    :type limit: int\n    :param offset: when ``limit`` is given, the offset to start\n        returning packages from\n    :type offset: int\n\n    :rtype: list of strings\n\n ");
    }
}
