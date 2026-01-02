package eu.essi_lab.accessor.imo;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import eu.essi_lab.lib.utils.StringUtils;

public class IMOCSVMetadata {

    private HashMap<String, IMOStationMetadata> stationMetadata = new HashMap<String, IMOStationMetadata>();

    public HashMap<String, IMOStationMetadata> getStationMetadata() {
	return stationMetadata;
    }

    public IMOCSVMetadata(File f) throws FileNotFoundException {
	FileInputStream fis = new FileInputStream(f);
	InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8);
		
	BufferedReader br = new BufferedReader(reader);
	try {
	    String header = br.readLine();
	    String[] split = header.split(";");

	    HashMap<String, Integer> indexes = new HashMap<>();
	    for (int i = 0; i < split.length; i++) {
		String title = StringUtils.trimNBSP(split[i]);
		if (indexes.get(title) == null) { // because there are two c
		    indexes.put(title, i);
		}
	    }

	    String line;
	    while ((line = br.readLine()) != null) {
		String[] splitLine = line.split(";");
		IMOStationMetadata metadata = new IMOStationMetadata();
		String q = "Q";
		Integer qIndex = indexes.get(q);
		if (splitLine.length > 5) {
		    for (int i = 0; i < splitLine.length; i++) {
			splitLine[i] = StringUtils.trimNBSP(splitLine[i]);

		    }
		    String qId = splitLine[qIndex];
		    if (qId != null && !qId.isEmpty()) {
			String[] qSplit = qId.split("/");
			for (int i = 0; i < qSplit.length; i++) {
			    qSplit[i] = StringUtils.trimNBSP(qSplit[i]);
			}
			String sid = qSplit[0].replace("vhm", "V");
			metadata.setStationId(sid);
			String latitude = splitLine[indexes.get("Latitude")].replace(",", ".");
			metadata.setLatitude(latitude);
			String longitude = splitLine[indexes.get("Longitude")].replace(",", ".");
			metadata.setLongitude(longitude);
			metadata.setInstitute(splitLine[indexes.get("Institute")]);
			metadata.setStationName(splitLine[indexes.get("StationName")]);
			metadata.setRiver(splitLine[indexes.get("River")]);
			stationMetadata.put(metadata.getStationId(), metadata);
		    }
		}
	    }

	    br.close();

	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

}
