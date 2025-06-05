package eu.essi_lab.accessor.waf.onamet_stations;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.accessor.waf.onamet_stations.ONAMETParameter.ONAMETParameterId;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;

/**
 * @author Fabrizio
 */
public class OMAMETStationCVSFile {

    private String content;

    /**
     *     
     */
    public OMAMETStationCVSFile(String content) {

	this.content = content;

    }

    public String getStationId() {

	return null;

    }

    /**
     * @return
     */
    public String getDate() {

	return Arrays.asList(content.split("/n")).get(0).split(",")[1].trim();
    }

    /**
     * @param oNAMETParameterId
     * @return
     */
    public List<SimpleEntry<String, Double>> getParameterValues(ONAMETParameterId oNAMETParameterId) {

	int varIndex = 0;
	int dateIndex = 1;
	int timeIndex = 2;

	switch (oNAMETParameterId) {
	case PE:
	    varIndex = 3;
	    break;
	case VV:
	    varIndex = 4;
	    break;

	case DV:
	    varIndex = 5;
	    break;
	case TA:
	    varIndex = 6;
	    break;

	case HR:
	    varIndex = 7;
	    break;
	case PR:
	    varIndex = 8;
	    break;
	}

	List<SimpleEntry<String, Double>> out = new ArrayList<>();

	try {

	    List<String> lines = Arrays.asList(content.split("\n")).stream().filter(l -> !l.isEmpty()).collect(Collectors.toList());

	    String date = lines.get(0).split(",")[dateIndex].trim();

	    for (String line : lines) {

		String time = line.split(",")[timeIndex].trim();

		String isoDateTime = date + "T" + time;

		String value = line.split(",")[varIndex].trim();

		Double optValue = Double.NaN;
		if (!value.equals("nd") && !value.isEmpty()) {
		    optValue = Double.valueOf(value);
		}

		SimpleEntry<String, Double> entry = new SimpleEntry<String, Double>(isoDateTime, optValue);

		out.add(entry);
	    }
	} catch (Throwable ex) {

	    GSLoggerFactory.getLogger(getClass()).error("Error occurred during CVS file parsing");
	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage());
	}

	return out;
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

	InputStream resourceAsStream = OMAMETStationCVSFile.class.getClassLoader().getResourceAsStream("test.txt");
	String string = IOStreamUtils.asUTF8String(resourceAsStream);

	OMAMETStationCVSFile file = new OMAMETStationCVSFile(string);

	String date = file.getDate();

	List<SimpleEntry<String, Double>> dv = file.getParameterValues(ONAMETParameterId.DV);
	List<SimpleEntry<String, Double>> hr = file.getParameterValues(ONAMETParameterId.HR);
	List<SimpleEntry<String, Double>> pe = file.getParameterValues(ONAMETParameterId.PE);
	List<SimpleEntry<String, Double>> pr = file.getParameterValues(ONAMETParameterId.PR);
	List<SimpleEntry<String, Double>> ta = file.getParameterValues(ONAMETParameterId.TA);
	List<SimpleEntry<String, Double>> vv = file.getParameterValues(ONAMETParameterId.VV);

	System.out.println(date);

	System.out.println(dv);
	System.out.println(hr);
	System.out.println(pe);
	System.out.println(pr);
	System.out.println(ta);
	System.out.println(vv);

    }
}
