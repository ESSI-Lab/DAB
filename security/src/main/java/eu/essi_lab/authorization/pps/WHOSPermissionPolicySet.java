/**
 * 
 */
package eu.essi_lab.authorization.pps;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
 * Users having the "whos" role, are allowed to discovery if and only if:<br>
 * <br>
 * 1) the view creator is "whos"<br>
 *
 * 2) the discovery path is supported<br>
 * <br>
 * Users having this policy role, are allowed to access if and only if:<br>
 * <br>
 * 1) the view creator is "whos"<br>
 * 
 * 2) the access path is is supported<br>
 * <br>
 * Users having this policy role are also allowed to perform other actions if and only if:<br>
 * <br>
 * 1) the view creator is "whos"<br>
 *
 * 2) the discovery path is supported OR the access path is is supported<br>
 * 
 * @author Fabrizio
 */
public class WHOSPermissionPolicySet extends CreatorPermissionPolicySet {

    public WHOSPermissionPolicySet() {

	super("whos");
    }

    @Override
    protected String getCreator() {

	return "whos";
    }
}
