package eu.essi_lab.gssrv.rest;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

/**
 * Request body for deleting predefined shape entries.
 */
public class PredefinedShapeDeleteRequest {

    private String email;
    private String apiKey;
    private String prefix;
    private List<String> identifiers;
    private String shapeView;

    public PredefinedShapeDeleteRequest() {
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

    public String getPrefix() {
	return prefix;
    }

    public void setPrefix(String prefix) {
	this.prefix = prefix;
    }

    public List<String> getIdentifiers() {
	return identifiers;
    }

    public void setIdentifiers(List<String> identifiers) {
	this.identifiers = identifiers;
    }

    public String getShapeView() {
	return shapeView;
    }

    public void setShapeView(String shapeView) {
	this.shapeView = shapeView;
    }
}
