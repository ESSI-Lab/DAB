package eu.essi_lab.model.resource.stax;

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

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import eu.essi_lab.lib.xml.stax.StAXDocumentParser;

public class ResponsiblePartyParser extends StAXDocumentParser {
    private String organisation;
    public String getOrganisation() {
        return organisation;
    }

    public String getIndividual() {
        return individual;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getUrl() {
        return url;
    }

    private String individual;
    private String role;
    private String email;
    private String url;

    public ResponsiblePartyParser(String party) throws XMLStreamException, IOException {
	super(party);

	add(//
		new QName("organisationName"), //
		new QName("CharacterString")//
		, v -> organisation = v);

	add(//
		new QName("individualName"), //
		new QName("CharacterString")//
		, v -> individual = v);

	add(//
		new QName("role"), //
		new QName("CI_RoleCode")//
		, v -> role = v);

	add(//
		new QName("electronicMailAddress"), //
		new QName("CharacterString")//
		, v -> email = v);

	add(//
		new QName("linkage"), //
		new QName("URL")//
		, v -> url = v);

	parse();
    }

}
