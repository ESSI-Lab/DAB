package eu.essi_lab.accessor.hiscentral.lombardia.opendata;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.model.resource.InterpolationType;

/**
 * The time series published by ARPA Lombardia on the Regione Lombardia open data portal, one per sensor type
 * ({@code tipologia} in the sensor registry) and aggregation operator ({@code idoperatore} in the real time dataset).
 * <p>
 * Each series is served by the real time dataset (rolling window, all types together, filtered by operator) and by
 * the validated archive datasets, one per type and period, which have no operator column.
 *
 * @author boldrini
 */
public enum LombardiaOpenDataProperty {

    LIVELLO_IDROMETRICO("Livello Idrometrico", Operator.AVERAGE, "Livello idrometrico", InterpolationType.AVERAGE, //
	    archive("xubc-puka", null, 2011), archive("gsyu-uxt3", 2011, 2021), archive("3e8b-w7ay", 2021, null)), //

    PORTATA("Portata fiume", Operator.AVERAGE, "Portata", InterpolationType.AVERAGE, //
	    archive("3iu8-z7xf", null, 2011), archive("rdbp-83r6", 2011, 2021), archive("ujnt-2rtf", 2021, null)), //

    PRECIPITAZIONE("Precipitazione", Operator.TOTAL, "Precipitazione", InterpolationType.TOTAL, //
	    archive("e7r2-7m84", null, 2011), archive("2kar-pnuk", 2011, 2021), archive("pstb-pga6", 2021, null)), //

    ALTEZZA_NEVE("Altezza Neve", Operator.AVERAGE, "Altezza neve", InterpolationType.AVERAGE, //
	    archive("5kig-ayub", null, 2011), archive("id9e-7wf3", 2011, 2021), archive("uqbu-tt6m", 2021, null)), //

    TEMPERATURA("Temperatura", Operator.AVERAGE, "Temperatura", InterpolationType.AVERAGE, //
	    archive("6eu4-4tja", null, 2011), archive("d4kj-kbpj", 2011, 2021), archive("w9wd-u6jh", 2021, null)), //

    UMIDITA_RELATIVA("Umidità Relativa", Operator.AVERAGE, "Umidità relativa", InterpolationType.AVERAGE, //
	    archive("nure-mhrg", null, 2011), archive("xpun-8722", 2011, 2021), archive("823w-fh4c", 2021, null)), //

    RADIAZIONE_GLOBALE("Radiazione Globale", Operator.AVERAGE, "Radiazione globale", InterpolationType.AVERAGE, //
	    archive("63ns-e4tv", null, 2021), archive("cxym-eps2", 2021, null)), //

    VELOCITA_VENTO("Velocità Vento", Operator.AVERAGE, "Velocità vento", InterpolationType.AVERAGE, //
	    archive("ed5f-uim8", null, 2011), archive("x9gp-z9xx", 2011, 2021), archive("hu5q-68e3", 2021, null)), //

    VELOCITA_VENTO_RAFFICA("Velocità Vento", Operator.MAX, "Velocità vento raffica", InterpolationType.MAX, //
	    archive("ppwf-2tzh", null, 2021), archive("gi9u-2aez", 2021, null)), //

    DIREZIONE_VENTO("Direzione Vento", Operator.AVERAGE, "Direzione vento", InterpolationType.AVERAGE, //
	    archive("cptm-adt5", null, 2011), archive("ypty-e75m", 2011, 2021), archive("purm-rsjf", 2021, null)), //

    // no validated archive is published for the direction of the gust
    DIREZIONE_VENTO_RAFFICA("Direzione Vento", Operator.MAX, "Direzione vento raffica", InterpolationType.MAX);

    /**
     * A validated archive dataset, covering the years in [fromYear, toYear) (null means unbounded)
     */
    public static class Archive {

	private final String datasetId;
	private final Integer fromYear;
	private final Integer toYear;

	private Archive(String datasetId, Integer fromYear, Integer toYear) {
	    this.datasetId = datasetId;
	    this.fromYear = fromYear;
	    this.toYear = toYear;
	}

	public String getDatasetId() {
	    return datasetId;
	}

	/**
	 * @param fromYear first requested year
	 * @param toYear last requested year
	 * @return true if the archive covers at least one year of the given range
	 */
	public boolean overlaps(int fromYear, int toYear) {
	    return (this.fromYear == null || toYear >= this.fromYear) && (this.toYear == null || fromYear < this.toYear);
	}
    }

    private static Archive archive(String datasetId, Integer fromYear, Integer toYear) {
	return new Archive(datasetId, fromYear, toYear);
    }

    private final String tipologia;
    private final String operator;
    private final String label;
    private final InterpolationType interpolation;
    private final List<Archive> archives;

    private LombardiaOpenDataProperty(String tipologia, String operator, String label, InterpolationType interpolation,
	    Archive... archives) {
	this.tipologia = tipologia;
	this.operator = operator;
	this.label = label;
	this.interpolation = interpolation;
	this.archives = Arrays.asList(archives);
    }

    /**
     * @return the sensor type, as in the {@code tipologia} field of the sensor registry
     */
    public String getTipologia() {
	return tipologia;
    }

    /**
     * @return the aggregation operator, as in the {@code idoperatore} field of the real time dataset
     */
    public String getOperator() {
	return operator;
    }

    public String getLabel() {
	return label;
    }

    public InterpolationType getInterpolation() {
	return interpolation;
    }

    public List<Archive> getArchives() {
	return archives;
    }

    /**
     * @return true for wind direction series, whose values include conventional codes for calm and variable wind
     */
    public boolean isWindDirection() {
	return tipologia.equals(DIREZIONE_VENTO.tipologia);
    }

    /**
     * @param tipologia a sensor type of the sensor registry
     * @return the series published for sensors of the given type
     */
    public static List<LombardiaOpenDataProperty> fromTipologia(String tipologia) {
	List<LombardiaOpenDataProperty> ret = new ArrayList<>();
	for (LombardiaOpenDataProperty property : values()) {
	    if (property.tipologia.equalsIgnoreCase(tipologia)) {
		ret.add(property);
	    }
	}
	return ret;
    }

    public static Optional<LombardiaOpenDataProperty> decode(String name) {
	return Arrays.stream(values()).filter(p -> p.name().equals(name)).findFirst();
    }

    /**
     * Operator codes of the real time dataset ({@code idoperatore})
     */
    private static class Operator {

	private static final String AVERAGE = "1";
	private static final String MAX = "3";
	private static final String TOTAL = "4";
    }
}
