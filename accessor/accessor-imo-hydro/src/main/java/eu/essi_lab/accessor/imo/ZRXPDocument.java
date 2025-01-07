package eu.essi_lab.accessor.imo;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

public class ZRXPDocument {

    private static final String TOKEN_1 = "\\|\\*\\|";
    private static final String TOKEN_1_UNESCAPED = "|*|";
    private static final String TOKEN_2 = ";\\*;";
    private File file;

    public File getFile() {
	return file;
    }

    public ZRXPDocument(File file) {
	this.file = file;
    }

    public List<ZRXPBlock> getBlocks() throws IOException {

	List<ZRXPBlock> ret = new ArrayList<>();

	FileInputStream fis = new FileInputStream(file);
	InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
	BufferedReader reader = new BufferedReader(inputStreamReader);

	String line = "";

	ZRXPBlock tmpBlock = null;

	int i = 0;
	int d = 0;

	try {
	    while ((line = reader.readLine()) != null) {

		i++;

		if (isCommentLine(line)) {

		    if (line.toLowerCase().contains("end of block")) {

			if (tmpBlock != null) {
			    tmpBlock.setEndDataLine(i);
			    ret.add(tmpBlock);
			    tmpBlock = null;
			}

		    }

		    continue; // comment line

		} else if (isHeaderLine(line)) { // header line

		    if (tmpBlock == null) {

			tmpBlock = new ZRXPBlock(file, i);
		    }
		    // decode header line

		    List<SimpleEntry<ZRXPKeyword, String>> headers = decodeHeaderLine(line);

		    for (SimpleEntry<ZRXPKeyword, String> header : headers) {
			tmpBlock.addHeader(header.getKey(), header.getValue());
		    }

		} else { // data line
		    d = i;
		    if (tmpBlock != null) {
			Integer startDataLine = tmpBlock.getStartDataLine();
			if (startDataLine == null) {
			    tmpBlock.setStartDataLine(i);
			}
			tmpBlock.addDataLine(line);
		    }

		}

	    }

	} finally {
	    reader.close();
	    inputStreamReader.close();
	    fis.close();
	}

	return ret;

    }

    public boolean isHeaderLine(String line) {
	line = line.trim();
	switch (line.length()) {
	case 0:
	case 1:
	    return false;
	default:
	    return (line.charAt(0) == '#' && line.charAt(1) != '#');
	}
    }

    public boolean isCommentLine(String line) {
	line = line.trim();
	switch (line.length()) {
	case 0:
	    return true;
	case 1:
	    return (line.charAt(0) == '#');
	default:
	    return (line.charAt(0) == '#' && line.charAt(1) == '#');
	}
    }

    private List<SimpleEntry<ZRXPKeyword, String>> decodeHeaderLine(String line) {
	line = line.substring(1);
	String[] split;
	split = line.contains(TOKEN_1_UNESCAPED) ? line.split(TOKEN_1) : line.split(TOKEN_2);
	List<SimpleEntry<ZRXPKeyword, String>> ret = new ArrayList<>();
	for (String keyValue : split) {
	    if (keyValue != null) {
		keyValue = keyValue.trim();
		if (keyValue.equals("")) {
		    continue;
		}
		ZRXPKeyword zk = ZRXPKeyword.fromKeyValue(keyValue);
		if (zk == null) {
		    throw new IllegalArgumentException("Unrecognized header name: " + keyValue);
		} else {
		    String value = zk.getValue(keyValue);
		    SimpleEntry<ZRXPKeyword, String> entry = new SimpleEntry<>(zk, value);
		    ret.add(entry);
		}
	    }
	}
	return ret;
    }

}
