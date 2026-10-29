package eu.essi_lab.accessor.coideam;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.util.Optional;

/**
 * Datasets of raw observations of the automatic stations of IDEAM published on datos.gov.co. All of them share the same columns
 * (see {@link IDEAMClient}), one row per observation
 *
 * @author boldrini
 */
public enum IDEAMDataset {

    /**
     * Instantaneous river water level, hourly
     */
    NIVEL("bdmn-sqnh", "Nivel Instantáneo del Rio", "Water level", "PT1H", null), //
    /**
     * Minimum river water level of each hour
     */
    NIVEL_MIN("pt9a-aamx", "Nivel Mínimo del Rio", "Water level (hourly minimum)", "PT1H", "PT1H"), //
    /**
     * Maximum river water level of each hour
     */
    NIVEL_MAX("vfth-yucv", "Nivel Máximo del Rio", "Water level (hourly maximum)", "PT1H", "PT1H"), //
    /**
     * Precipitation accumulated in 10 minutes
     */
    PRECIPITACION("s54a-sgyg", "Precipitación", "Precipitation", "PT10M", "PT10M");

    private final String id;
    private final String name;
    private final String label;
    private final String resolution;
    private final String aggregation;

    /**
     * @param id the Socrata identifier of the dataset
     * @param name the name of the dataset on datos.gov.co
     * @param label the English name of the observed property
     * @param resolution the nominal time resolution (ISO 8601 duration)
     * @param aggregation the aggregation period (ISO 8601 duration), null for instantaneous values
     */
    private IDEAMDataset(String id, String name, String label, String resolution, String aggregation) {

	this.id = id;
	this.name = name;
	this.label = label;
	this.resolution = resolution;
	this.aggregation = aggregation;
    }

    /**
     * @return the Socrata identifier of the dataset, e.g. "bdmn-sqnh"
     */
    public String getId() {

	return id;
    }

    /**
     * @return the name of the dataset on datos.gov.co, e.g. "Nivel Instantáneo del Rio"
     */
    public String getName() {

	return name;
    }

    /**
     * @return the English name of the observed property, e.g. "Water level"
     */
    public String getLabel() {

	return label;
    }

    /**
     * @return the nominal time resolution, e.g. "PT1H"
     */
    public String getResolution() {

	return resolution;
    }

    /**
     * @return the aggregation period, or empty for instantaneous values
     */
    public Optional<String> getAggregation() {

	return Optional.ofNullable(aggregation);
    }

    /**
     * @return true for the water level datasets
     */
    public boolean isWaterLevel() {

	return this != PRECIPITACION;
    }

    /**
     * @param code the name of the dataset (e.g. "NIVEL") or its Socrata identifier (e.g. "bdmn-sqnh")
     * @return
     */
    public static Optional<IDEAMDataset> decode(String code) {

	if (code == null) {
	    return Optional.empty();
	}

	for (IDEAMDataset dataset : values()) {
	    if (dataset.name().equalsIgnoreCase(code.trim()) || dataset.getId().equalsIgnoreCase(code.trim())) {
		return Optional.of(dataset);
	    }
	}

	return Optional.empty();
    }
}
