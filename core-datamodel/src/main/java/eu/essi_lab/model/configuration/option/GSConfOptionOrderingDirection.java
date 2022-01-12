package eu.essi_lab.model.configuration.option;

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

import java.util.Arrays;
import java.util.List;

import eu.essi_lab.model.OrderingDirection;
import eu.essi_lab.model.exceptions.GSException;
public class GSConfOptionOrderingDirection extends GSConfOption<OrderingDirection> {
    /**
     * 
     */
    private static final long serialVersionUID = -1443640338039799756L;
    private static final List<OrderingDirection> ALLOWED_VALUES;

    static {
	ALLOWED_VALUES = Arrays.asList(OrderingDirection.values());
	ALLOWED_VALUES.sort((s1, s2) -> s1.getName().compareTo(s2.getName()));
    }

    public GSConfOptionOrderingDirection() {
	super(OrderingDirection.class);
	setValue(OrderingDirection.ASCENDING);
    }

    @Override
    public void validate() throws GSException {
	// no validation required
    }

    @Override
    public List<OrderingDirection> getAllowedValues() {

	return ALLOWED_VALUES;
    }
}
