package eu.essi_lab.messages;

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

import java.io.Serializable;

import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.auth.GSUser;

/**
 * A generic GI-suite message composed by a header and a payload.<br>
 * The header can be leaved empty, and it can be used to store meta information about the message payload, such as the
 * {@link GSUser} or the view identifier in case of a discover query.<br>
 * A message <b>MUST</b> have a payload.<br>
 * Both header and payload are instances of {@link GSPropertyHandler} and so they can have several
 * {@link GSProperty}.<br>
 * <i>Due to its generic nature, it is recommended to implements subclasses which specifies the message content</i>
 * 
 * @author Fabrizio
 */
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
