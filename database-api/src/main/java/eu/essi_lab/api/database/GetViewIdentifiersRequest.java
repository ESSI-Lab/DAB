/**
 * 
 */
package eu.essi_lab.api.database;

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

import java.util.Optional;

import eu.essi_lab.messages.bond.View.ViewVisibility;

/**
 * @author Fabrizio
 */
public class GetViewIdentifiersRequest {

    private Integer start;
    private Integer count;
    private String creator;
    private String owner;
    private ViewVisibility viewVisibility;
    private String sourceDeployment;

    /**
     * @return
     */
    public static GetViewIdentifiersRequest create() {

	return new GetViewIdentifiersRequest(null, null, null, null, null, null);
    }

    /**
     * @param creator
     * @return
     */
    public static GetViewIdentifiersRequest withCreator(String creator) {

	return new GetViewIdentifiersRequest(null, null, creator, null, null, null);
    }

    /**
     * @param owner
     * @return
     */
    public static GetViewIdentifiersRequest withOwner(String owner) {

	return new GetViewIdentifiersRequest(null, null, null, owner, null, null);
    }

    /**
     * @param start
     * @param count
     * @param creator
     * @return
     */
    public static GetViewIdentifiersRequest withCreator(Integer start, Integer count, String creator) {

	return new GetViewIdentifiersRequest(start, count, creator, null, null, null);
    }

    /**
     * @param start
     * @param count
     * @param owner
     * @return
     */
    public static GetViewIdentifiersRequest withOwner(Integer start, Integer count, String owner) {

	return new GetViewIdentifiersRequest(start, count, null, owner, null, null);
    }

    /**
     * @param start
     * @param count
     * @return
     */
    public static GetViewIdentifiersRequest create(Integer start, Integer count) {

	return new GetViewIdentifiersRequest(start, count, null, null, null, null);
    }

    /**
     * @param start
     * @param count
     * @param creator
     * @param owner
     * @return
     */
    public static GetViewIdentifiersRequest create(Integer start, Integer count, String creator, String owner) {

	return new GetViewIdentifiersRequest(start, count, creator, owner, null, null);
    }

    /**
     * @param start
     * @param count
     * @param creator
     * @param owner
     * @param viewVisibity
     * @return
     */
    public static GetViewIdentifiersRequest create(Integer start, Integer count, String creator, String owner,
	    ViewVisibility viewVisibity) {

	return new GetViewIdentifiersRequest(start, count, creator, owner, viewVisibity, null);
    }

    /**
     * @param creator
     * @param owner
     * @param viewVisibity
     * @return
     */
    public static GetViewIdentifiersRequest create(String creator, String owner, ViewVisibility viewVisibity) {

	return new GetViewIdentifiersRequest(null, null, creator, owner, viewVisibity, null);
    }

    /**
     * @param creator
     * @param viewVisibity
     * @return
     */
    public static GetViewIdentifiersRequest create(String creator, ViewVisibility viewVisibity) {

	return new GetViewIdentifiersRequest(null, null, creator, null, viewVisibity, null);
    }

    /**
     * @param creator
     * @return
     */
    public static GetViewIdentifiersRequest create(String creator) {

	return new GetViewIdentifiersRequest(null, null, creator, null, null, null);
    }
    
    /**
     * @param viewVisibity
     * @return
     */
    public static GetViewIdentifiersRequest create(ViewVisibility viewVisibity) {

	return new GetViewIdentifiersRequest(null, null, null, null, viewVisibity, null);
    }

    /**
     * @param creator
     * @param owner
     * @return
     */
    public static GetViewIdentifiersRequest create(String creator, String owner) {

	return new GetViewIdentifiersRequest(null, null, creator, owner, null, null);
    }

    /**
     * @param start
     * @param count
     * @param creator
     * @param viewVisibity
     * @return
     */
    public static GetViewIdentifiersRequest create(Integer start, Integer count, String creator, ViewVisibility viewVisibity) {

	return new GetViewIdentifiersRequest(start, count, creator, null, viewVisibity, null);
    }

    /**
     * @param start
     * @param count
     * @param creator
     * @param owner
     * @param viewVisibility
     * @param sourceDeployment
     * @return
     */
    public static GetViewIdentifiersRequest create(Integer start, Integer count, String creator, String owner,
	    ViewVisibility viewVisibility, String sourceDeployment) {

	return new GetViewIdentifiersRequest(start, count, creator, owner, viewVisibility, sourceDeployment);
    }

    /**
     * @param start
     * @param count
     * @param creator
     * @param owner
     * @param viewVisibility
     * @param sourceDeployment
     */
    private GetViewIdentifiersRequest(Integer start, Integer count, String creator, String owner, ViewVisibility viewVisibility, String sourceDeployment) {
	this.start = start;
	this.count = count;
	this.creator = creator;
	this.owner = owner;
	this.viewVisibility = viewVisibility;
	this.sourceDeployment = sourceDeployment;
    }

    /**
     * @return
     */
    public int getStart() {

	return start != null ? start : 0;
    }

    /**
     * @return
     */
    public int getCount() {

	return count != null ? count : Integer.MAX_VALUE;
    }

    /**
     * @return
     */
    public Optional<String> getCreator() {

	return Optional.ofNullable(creator);
    }

    /**
     * @return
     */
    public Optional<String> getOwner() {

	return Optional.ofNullable(owner);
    }

    /**
     * @return
     */
    public Optional<ViewVisibility> getVisibility() {

	return Optional.ofNullable(viewVisibility);
    }

    /**
     * @return
     */
    public Optional<String> getSourceDeployment() {

	return Optional.ofNullable(sourceDeployment);
    }
}
