package eu.essi_lab.lib.utils;

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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
public class ReplacingInputStream extends FilterInputStream {

    LinkedList<Integer> inQueue = new LinkedList<Integer>();
    LinkedList<Integer> outQueue = new LinkedList<Integer>();
    HashMap<String, byte[]> searchBytes = new HashMap<>();
    HashMap<String, byte[]> replacementBytes = new HashMap<>();
    Integer maxLength = 0;

    protected ReplacingInputStream(InputStream in) {
	super(in);
    }

    public void addSearchString(String search, String replacement) {
	byte[] bytesIn = search.getBytes(StandardCharsets.UTF_8);
	byte[] bytesOut = replacement.getBytes(StandardCharsets.UTF_8);
	if (bytesIn.length > maxLength) {
	    maxLength = bytesIn.length;
	}
	searchBytes.put(search, bytesIn);
	replacementBytes.put(search, bytesOut);
    }

    private String isMatchFound() {

	nextSearch: for (String search : searchBytes.keySet()) {
	    Iterator<Integer> inIter = inQueue.iterator();
	    byte[] bytesIn = searchBytes.get(search);
	    for (int i = 0; i < bytesIn.length; i++) {
		if (!inIter.hasNext() || bytesIn[i] != inIter.next()) {
		    continue nextSearch;
		}
	    }
	    return search;
	}
	return null;
    }

    private void readAhead() throws IOException {
	// Work up some look-ahead.
	while (inQueue.size() < maxLength) {
	    int next = super.read();
	    inQueue.offer(next);
	    if (next == -1)
		break;
	}
    }

    @Override
    public int read() throws IOException {
	// Next byte already determined.
	if (outQueue.isEmpty()) {
	    readAhead();

	    String search = null;
	    if ((search = isMatchFound()) != null) {
		byte[] searchIn = searchBytes.get(search);
		byte[] replacement = replacementBytes.get(search);
		for (int i = 0; i < searchIn.length; i++) {
		    inQueue.remove();
		}
		for (byte b : replacement) {
		    outQueue.offer((int) b);
		}
		// case for replacement with empty string
		if (outQueue.isEmpty()) {
		    return read();
		}
	    } else {
		outQueue.add(inQueue.remove());
	    }
	}

	int ret = outQueue.remove();
	// System.out.print((char)ret);
	return ret;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
	if (b == null) {
	    throw new NullPointerException();
	} else if (off < 0 || len < 0 || len > b.length - off) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return 0;
	}

	int c = read();
	if (c == -1) {
	    return -1;
	}
	b[off] = (byte) c;

	int i = 1;
	try {
	    for (; i < len; i++) {
		c = read();
		if (c == -1) {
		    break;
		}
		b[off + i] = (byte) c;
	    }
	} catch (IOException ee) {
	}
	return i;
    }

    @Override
    public int read(byte[] b) throws IOException {
	return read(b, 0, b.length);
    }

}
