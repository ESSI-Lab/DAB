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

/**
 * Request body for updating a predefined shape entry.
 */
public class PredefinedShapeUpdateRequest {

    private String email;
    private String apiKey;
    private String identifier;
    private String newIdentifier;
    private String name;
    private String group;
    private Integer groupOrder;
    private String owner;
    private String shapeView;

    public PredefinedShapeUpdateRequest() {
    }

    public String getEmail() {
	return email;
    }

    public void setEmail(String email) {
	this.email = email;
    }

    public String getApiKey() {
	return apiKey;
    }

    public void setApiKey(String apiKey) {
	this.apiKey = apiKey;
    }

    public String getIdentifier() {
	return identifier;
    }

    public void setIdentifier(String identifier) {
	this.identifier = identifier;
    }

    public String getNewIdentifier() {
	return newIdentifier;
    }

    public void setNewIdentifier(String newIdentifier) {
	this.newIdentifier = newIdentifier;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getGroup() {
	return group;
    }

    public void setGroup(String group) {
	this.group = group;
    }

    public Integer getGroupOrder() {
	return groupOrder;
    }

    public void setGroupOrder(Integer groupOrder) {
	this.groupOrder = groupOrder;
    }

    public String getOwner() {
	return owner;
    }

    public void setOwner(String owner) {
	this.owner = owner;
    }

    public String getShapeView() {
	return shapeView;
    }

    public void setShapeView(String shapeView) {
	this.shapeView = shapeView;
    }
}
