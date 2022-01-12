package eu.essi_lab.messages;

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

import java.io.Serializable;

import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.auth.GSUser;
public class GSMessage implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7668464862195580878L;
    protected GSPropertyHandler header;
    protected GSPropertyHandler payload;

    public GSMessage() {
	header = new GSPropertyHandler();
	payload = new GSPropertyHandler();
    }

    /**
     * Get the message header
     * 
     * @return
     */
    public GSPropertyHandler getHeader() {

	return header;
    }

    /**
     * Get the message payload
     * 
     * @return
     */
    public GSPropertyHandler getPayload() {

	return payload;
    }

    @Override
    public String toString() {
	return "HEADER: " + header.toString() + "\n\nPAYLOAD: " + payload.toString();
    }

    public static void main(String[] args) {

	GSMessage m = new GSMessage();

	m.getPayload().add(new GSProperty<String>("startPage", "23"));
	m.getPayload().add(new GSProperty<String>("maxRecords", "48"));

	String startPage = m.getPayload().get("startPage", String.class);
	System.out.println(startPage);

	String maxRecords = m.getPayload().get("maxRecords", String.class);
	System.out.println(maxRecords);
    }
}
