package eu.essi_lab.lib.odip.rosetta;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class FakeRosettaStone extends RosettaStone {
    private static final String NERC_PRACTICAL_SALINITY = "http://vocab.nerc.ac.uk/collection/P01/current/PSLTZZ01/";
    private static final String NERC_SALINITY = "http://vocab.nerc.ac.uk/collection/P02/current/PSAL/";
    private static final String NODC_SALINITY = "https://www.nodc.noaa.gov/cgi-bin/OAS/prd/datatype/details/307";
    //
    private static final String NERC_WATER_BODY_TEMPERATURE = "http://vocab.nerc.ac.uk/collection/P01/current/TEMPPR01/";
    private static final String NERC_WATER_TEMPERATURE = "http://vocab.nerc.ac.uk/collection/P02/current/TEMP/";
    private static final String NODC_WATER_TEMPERATURE = "https://www.nodc.noaa.gov/cgi-bin/OAS/prd/datatype/details/373";
    //
    private static final String NERC_SEA_LEVEL = "http://vocab.nerc.ac.uk/collection/P02/current/ASLV/";
    private static final String AODN_SEA_LEVEL = "http://vocab.aodn.org.au/def/discovery_parameter/entity/643";
    private static final String NODC_SEA_LEVEL = "https://www.nodc.noaa.gov/cgi-bin/OAS/prd/datatype/details/312";
    //
    private static final String NERC_SALINOMETER = "http://vocab.nerc.ac.uk/collection/L05/current/LAB30/";
    private static final String NODC_SALINOMETER = "https://www.nodc.noaa.gov/cgi-bin/OAS/prd/insttype/details/75";
    //
    private static final String NERC_WATER_PRESSURE_SENSOR = "http://vocab.nerc.ac.uk/collection/L05/current/WPS/";
    private static final String NODC_PRESSURE_SENSORS = "https://www.nodc.noaa.gov/cgi-bin/OAS/prd/insttype/details/207";
    //
    private static final String SDN_ALASKA = "http://www.seadatanet.org/urnurl/SDN:EDMO::3167/";
    private static final String NODC_ALASKA = "https://www.nodc.noaa.gov/cgi-bin/OAS/prd/institution/details/972";
    private static final String NERC_ALASKA = "http://vocab.nerc.ac.uk/collection/C75/current/UAA/";
    //
    private List<Set<String>> translations = new ArrayList<>();
    private HashMap<String, Set<String>> broader = new HashMap<>();
    private HashMap<String, Set<String>> narrower = new HashMap<>();

    public FakeRosettaStone() {

	// translations
	addTranslation(NERC_SALINITY, translateNERCtoSDN(NERC_SALINITY), NODC_SALINITY);
	addTranslation(NERC_WATER_TEMPERATURE, translateNERCtoSDN(NERC_WATER_TEMPERATURE), NODC_WATER_TEMPERATURE);
	addTranslation(NERC_SEA_LEVEL, translateNERCtoSDN(NERC_SEA_LEVEL), NODC_SEA_LEVEL, AODN_SEA_LEVEL);
	addTranslation(NERC_SALINOMETER, translateNERCtoSDN(NERC_SALINOMETER), NODC_SALINOMETER);
	addTranslation(NERC_ALASKA, SDN_ALASKA, NODC_ALASKA);
	// narrower
	addNarrower(new String[] { NERC_SALINITY, translateNERCtoSDN(NERC_SALINITY) }, //
		new String[] { NERC_PRACTICAL_SALINITY, translateNERCtoSDN(NERC_PRACTICAL_SALINITY) });
	addNarrower(new String[] { NERC_WATER_TEMPERATURE, translateNERCtoSDN(NERC_WATER_TEMPERATURE) }, //
		new String[] { NERC_WATER_BODY_TEMPERATURE, translateNERCtoSDN(NERC_WATER_BODY_TEMPERATURE) });
	addNarrower(new String[] { NODC_PRESSURE_SENSORS }, //
		new String[] { NERC_WATER_PRESSURE_SENSOR, translateNERCtoSDN(NERC_WATER_PRESSURE_SENSOR) });
	// broader
	addBroader(new String[] { NERC_PRACTICAL_SALINITY, translateNERCtoSDN(NERC_PRACTICAL_SALINITY) }, //
		new String[] { NERC_SALINITY, translateNERCtoSDN(NERC_SALINITY) });
	addBroader(new String[] { NERC_WATER_BODY_TEMPERATURE, translateNERCtoSDN(NERC_WATER_BODY_TEMPERATURE) }, //
		new String[] { NERC_WATER_TEMPERATURE, translateNERCtoSDN(NERC_WATER_TEMPERATURE) });
	addBroader(new String[] { NERC_WATER_PRESSURE_SENSOR, translateNERCtoSDN(NERC_WATER_PRESSURE_SENSOR) }, //
		new String[] { NODC_PRESSURE_SENSORS });

    }

    private void addTranslation(String... synonyms) {
	Set<String> set = new TreeSet<>();
	for (String syn : synonyms) {
	    set.add(syn);
	}
	this.translations.add(set);

    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.profiler.os.IRosettaStone#getTranslations(java.lang.String)
     */
    @Override
    public Set<String> getTranslations(String term) {
	Set<String> ret = new TreeSet<>();
	if (term == null) {
	    return null;
	}
	for (Set<String> set : translations) {
	    if (set.contains(term)) {
		for (String syn : set) {
		    if (!syn.equals(term)) {
			ret.add(syn);
		    }
		}
	    }
	}
	return ret;

    }

    private void addNarrower(String[] inputs, String[] outputs) {
	Set<String> outSet = new TreeSet<>();
	outSet.addAll(Arrays.asList(outputs));
	for (String input : inputs) {
	    narrower.put(input, outSet);
	}

    }

    private void addBroader(String[] inputs, String[] outputs) {
	Set<String> outSet = new TreeSet<>();
	outSet.addAll(Arrays.asList(outputs));
	for (String input : inputs) {
	    broader.put(input, outSet);
	}

    }

    @Override
    public Set<String> getNarrower(String term) {
	return narrower.get(term);
    }

    @Override
    public Set<String> getBroader(String term) {
	return broader.get(term);
    }

}
