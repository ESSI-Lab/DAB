package eu.essi_lab.gssrv.rest;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

public class SimpleUriInfo implements UriInfo {

    private final URI uri;

    public SimpleUriInfo(URI uri) {
        this.uri = uri;
    }

    @Override
    public URI getRequestUri() {
        return uri;
    }

    @Override
    public UriBuilder getRequestUriBuilder() {
        return UriBuilder.fromUri(uri);
    }

    @Override
    public String getPath() {
	return uri.getPath();
    }

    @Override
    public String getPath(boolean decode) {
	return getPath();
    }

    @Override
    public List<PathSegment> getPathSegments() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public List<PathSegment> getPathSegments(boolean decode) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public URI getAbsolutePath() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public UriBuilder getAbsolutePathBuilder() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public URI getBaseUri() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public UriBuilder getBaseUriBuilder() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public List<String> getMatchedURIs() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public List<String> getMatchedURIs(boolean decode) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public List<Object> getMatchedResources() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public URI resolve(URI uri) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public URI relativize(URI uri) {
	// TODO Auto-generated method stub
	return null;
    }
}
