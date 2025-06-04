package eu.essi_lab.lib.whos;

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

public enum WIS2Level10Topic {

    SURFACE_WATER_QUANTITY_OBSERVATIONS("surface-water-quantity-observations", WIS2Level9Topic.SURFACE_WATER_OBSERVATIONS), //
    SURFACE_WATER_QUALITY_OBSERVATIONS("surface-water-quality-observations", WIS2Level9Topic.SURFACE_WATER_OBSERVATIONS), //
    SEDIMENT_SURFACE_WATER_OBSERVATIONS("sediment-surface-water-observations", WIS2Level9Topic.SURFACE_WATER_OBSERVATIONS), //
    SOIL_MOISTURE_OBSERVATIONS("soil-moisture-observations", WIS2Level9Topic.SURFACE_WATER_OBSERVATIONS), //
    LAND_COVER("land-cover", WIS2Level9Topic.SURFACE_WATER_OBSERVATIONS), //
    LAND_BASED_ATMOSPHERIC_OBSERVATIONS("land-based-atmospheric-observations", WIS2Level9Topic.SURFACE_WATER_OBSERVATIONS), //
    GROUNDWATER_QUANTITY_OBSERVATIONS("groundwater-quantity-observations", WIS2Level9Topic.GROUNDWATER_OBSERVATIONS), //
    GROUNDWATER_QUALITY_OBSERVATIONS("groundwater-quality-observations", WIS2Level9Topic.GROUNDWATER_OBSERVATIONS), //
    SPACE_BASED_OBSERVATIONS("space-based-observations", WIS2Level9Topic.SPACE_BASED_OBSERVATIONS), //
    ADVISORY_MESSSAGES_TYPES("advisory-messages-types", WIS2Level9Topic.ALERTS_ADVISORIES_BULLETINS), //
    ALERT_MESSAGES_TYPES("alert-messages-types", WIS2Level9Topic.ALERTS_ADVISORIES_BULLETINS), //
    FLOOD_BULLETINS_DURATION("flood-bulletins-duration", WIS2Level9Topic.ALERTS_ADVISORIES_BULLETINS), //
    DROUGHT_BULLETINS_DURATION("drought-bulletins-duration", WIS2Level9Topic.ALERTS_ADVISORIES_BULLETINS), //
    STATUS_ASSESSMENT_REPORT("status-assessment-report", WIS2Level9Topic.ALERTS_ADVISORIES_BULLETINS), //
    FORECAST_TIME_RANGE("forecast-time-range", WIS2Level9Topic.ANALYSIS_PREDICTION_FORECASTS), //
    UNHARMONIZED("unharmonized", WIS2Level9Topic.OTHER), //
    UNCATEGORIZED("uncategorized", WIS2Level9Topic.OTHER);//

    private String id;
    private WIS2Level9Topic broaderLevel;

    public WIS2Level9Topic getBroaderLevel() {
	return broaderLevel;
    }

    public String getId() {
	return id;
    }

    private WIS2Level10Topic(String id, WIS2Level9Topic broaderLevel) {
	this.id = id;
	this.broaderLevel = broaderLevel;
    }

    public static WIS2Level10Topic decode(String conceptURI) {
	WIS2Level10Topic wisLevel = null;
	if (conceptURI != null && !conceptURI.isEmpty()) {

	    switch (conceptURI) {
	    case "http://hydro.geodab.eu/hydro-ontology/concept/11":
	    case "http://hydro.geodab.eu/hydro-ontology/concept/12":
	    case "http://hydro.geodab.eu/hydro-ontology/concept/15":
	    case "http://hydro.geodab.eu/hydro-ontology/concept/51":
	    case "http://hydro.geodab.eu/hydro-ontology/concept/78":
		wisLevel = WIS2Level10Topic.SURFACE_WATER_QUANTITY_OBSERVATIONS;
		break;

	    case "http://hydro.geodab.eu/hydro-ontology/concept/2297b":
		wisLevel = WIS2Level10Topic.SURFACE_WATER_QUALITY_OBSERVATIONS;
		break;

	    case "http://hydro.geodab.eu/hydro-ontology/concept/2c":
	    case "http://hydro.geodab.eu/hydro-ontology/concept/2d":
	    case "http://hydro.geodab.eu/hydro-ontology/concept/27":
	    case "http://hydro.geodab.eu/hydro-ontology/concept/33":
	    case "http://hydro.geodab.eu/hydro-ontology/concept/49":
	    case "http://hydro.geodab.eu/hydro-ontology/concept/55":
	    case "http://hydro.geodab.eu/hydro-ontology/concept/55b":
	    case "http://hydro.geodab.eu/hydro-ontology/concept/65":
	    case "http://hydro.geodab.eu/hydro-ontology/concept/67":
	    case "http://hydro.geodab.eu/hydro-ontology/concept/133":
	    case "http://hydro.geodab.eu/hydro-ontology/concept/134":
	    case "http://hydro.geodab.eu/hydro-ontology/concept/5167":
	    case "http://hydro.geodab.eu/hydro-ontology/concept/5033":
	    case "http://hydro.geodab.eu/hydro-ontology/concept/5296":
		wisLevel = WIS2Level10Topic.LAND_BASED_ATMOSPHERIC_OBSERVATIONS;
		break;

	    default:
		wisLevel = WIS2Level10Topic.UNCATEGORIZED;
		break;
	    }
	} else {
	    wisLevel = WIS2Level10Topic.UNHARMONIZED;
	}
	return wisLevel;
    }
}
