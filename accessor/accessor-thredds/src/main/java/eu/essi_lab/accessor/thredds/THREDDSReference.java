package eu.essi_lab.accessor.thredds;

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

import java.util.ArrayList;
import java.util.List;

/**
 * @author boldrini
 */
public class THREDDSReference {

    private List<Integer> address = new ArrayList<>();

    public THREDDSReference() {
    }

    public THREDDSReference(int[] reference) {
	for (int i = 0; i < reference.length; i++) {
	    address.add(reference[i]);
	}
    }

    public THREDDSReference(String token) {
	if (token != null) {
	    if (token.contains(".")) {
		String[] split = token.split("\\.");
		for (String string : split) {
		    address.add(Integer.parseInt(string));
		}
	    } else {
		address.add(Integer.parseInt(token));
	    }
	}
    }

    @Override
    public String toString() {
	String ret = "";
	for (Integer integer : address) {
	    ret += integer + ".";
	}
	if (ret.endsWith(".")) {
	    ret = ret.substring(0, ret.length() - 1);
	}
	return ret;
    }

    public Integer getFirstTarget() {
	if (address.isEmpty()) {
	    return null;
	}
	return address.get(0);
    }

    public THREDDSReference getRestTarget() {
	THREDDSReference ret = new THREDDSReference();
	for (int i = 1; i < address.size(); i++) {
	    ret.address.add(address.get(i));
	}
	return ret;
    }

    @Override
    protected THREDDSReference clone() throws CloneNotSupportedException {
	THREDDSReference ret = new THREDDSReference();
	for (Integer integer : address) {
	    ret.address.add(integer);
	}
	return ret;
    }

    public void addTarget(int i) {
	address.add(i);

    }

    public void removeLastTarget() {
	address.remove(address.size() - 1);

    }

    @Override
    public boolean equals(Object arg0) {
	if (arg0 instanceof THREDDSReference) {
	    THREDDSReference ref = (THREDDSReference) arg0;
	    if (ref.address.size() == address.size()) {
		for (int i = 0; i < address.size(); i++) {
		    if (!address.get(i).equals(ref.address.get(i))) {
			return false;
		    }
		}
		return true;
	    }
	}
	return false;
    }

}
