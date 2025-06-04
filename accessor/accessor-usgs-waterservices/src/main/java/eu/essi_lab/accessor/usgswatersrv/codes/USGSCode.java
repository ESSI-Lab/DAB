package eu.essi_lab.accessor.usgswatersrv.codes;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Optional;

import eu.essi_lab.lib.net.downloader.Downloader;

/**
 * A class to manage USGS codes
 * 
 * @author boldrini
 */
public abstract class USGSCode {
    private HashMap<String, HashMap<String, String>> codeMap = new HashMap<String, HashMap<String, String>>();
    private String[] mainCodes;

    protected USGSCode(String... mainCodes) {
	this.mainCodes = mainCodes;

	Downloader downloader = new Downloader();
	String url = getRetrievalURL();
	InputStream stream;
	if (url == null || url.isEmpty()) {
	    stream = USGSCode.class.getClassLoader().getResourceAsStream(getLocalResource());
	} else {
	    Optional<InputStream> optional = downloader.downloadOptionalStream(url);
	    if (optional.isPresent()) {
		stream = optional.get();
	    }else {
		stream = USGSCode.class.getClassLoader().getResourceAsStream(getLocalResource());
	    }
	}

	decode(stream);

    }

    public abstract String getRetrievalURL();

    public String getSeparator() {
	return "\t";

    }

    public abstract String getLocalResource();

    public boolean hasSizeLine() {
	return true;
    }

    private void decode(InputStream stream) {

	String line = "";

	long lineIndex = -2; // the first two lines are the headers and possibly the sizes

	String headers = "";
	String sizes = "";

	boolean hasSizeLine = hasSizeLine();

	try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {

	    String[] headersSplit = new String[] {};
	    String separator = getSeparator();
	    while ((line = br.readLine()) != null) {

		if (line.startsWith("#")) {
		    continue;
		} else {
		    if (lineIndex == -2) {
			headers = line;
			headersSplit = headers.split(separator);
		    } else if (lineIndex == -1 && hasSizeLine) {
			sizes = line;
		    } else {
			String[] dataSplit = line.split(separator);
			if (headersSplit.length == dataSplit.length) {
			    HashMap<String, String> values = new HashMap<>();
			    for (int i = 0; i < dataSplit.length; i++) {
				String header = headersSplit[i];
				String data = dataSplit[i];
				values.put(header, data);
			    }
			    String mainCode = "";

			    for (String code : mainCodes) {
				mainCode = mainCode += values.get(code);
			    }

			    codeMap.put(mainCode, values);
			}
		    }
		    lineIndex++;
		}

	    }

	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    public HashMap<String, String> getProperties(String code) {
	if (code == null || code.equals("")) {
	    return new HashMap<>();
	}
	HashMap<String, String> ret = codeMap.get(code);
	if (ret == null) {
	    return new HashMap<>();
	}
	return ret;
    }
}
