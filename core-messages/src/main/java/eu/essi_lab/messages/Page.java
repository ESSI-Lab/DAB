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
public class Page implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8396125519488916683L;

    protected int start;
    protected int size;

    /**
     * Creates a new page with <code>start</code> set to 1 and <code>size</code> set to 0
     */
    public Page() {

	this(1, 0);
    }

    /**
     * Creates a new page with <code>start</code> set to 1 and the supplied <code>size</code>
     * 
     * @param size an integer >= 0
     */
    public Page(int size) throws IllegalArgumentException {
	this(1, size);
    }

    /**
     * Creates a new page with the given <code>start</code> and <code>size</code>
     * 
     * @param start an integer >= 1
     * @param size an integer >= 0
     */
    public Page(int start, int size) throws IllegalArgumentException {

	if (start < 1 || size < 0) {
	    throw new IllegalArgumentException("start < 1 or size < 0");
	}

	this.start = start;
	this.size = size;
    }

    /**
     * Get the start value
     * 
     * @return
     */
    public int getStart() {

	return start;
    }

    /**
     * Set the start value
     *
     * @param start an integer >= 1
     */
    public void setStart(int start) throws IllegalArgumentException {

	if (start < 1) {
	    throw new IllegalArgumentException("start < 1");
	}

	this.start = start;
    }

    /**
     * Get the size value
     * 
     * @return
     */
    public int getSize() {

	return size;
    }

    /**
     * Set the size value
     *
     * @param size an integer >= 0
     */
    public void setSize(int size) throws IllegalArgumentException {

	if (size < 0) {
	    throw new IllegalArgumentException("size < 0");
	}

	this.size = size;
    }
}
