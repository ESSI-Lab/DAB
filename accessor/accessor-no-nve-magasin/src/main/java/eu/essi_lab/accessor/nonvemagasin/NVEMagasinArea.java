package eu.essi_lab.accessor.nonvemagasin;

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
 * The areas of the NVE reservoir statistics, as listed by the HentOmråder operation: the whole country (type NO), the electricity
 * price areas NO1-NO5 (type EL) and the watercourse regions (type VASS).<br>
 * The API publishes only names and textual descriptions of the areas, so this table adds an English name and an approximate
 * bounding box of each area, drawn by hand from the descriptions of the API (counties included in each area) and from the maps of
 * the price areas and of the regions published by NVE and Statnett. The boxes are only indicative (the areas are irregular and
 * the boxes of neighbouring areas overlap); the box of the whole country covers mainland Norway.
 *
 * @author boldrini
 */
public enum NVEMagasinArea {

    NORWAY("NO", 0, "Norway", 57.9, 4.5, 71.2, 31.2), //
    NO1("EL", 1, "Electricity price area NO1 (South-East Norway)", 58.8, 8.0, 62.7, 12.9), //
    NO2("EL", 2, "Electricity price area NO2 (South-West Norway)", 57.9, 4.9, 60.5, 10.6), //
    NO3("EL", 3, "Electricity price area NO3 (Central Norway)", 61.0, 4.8, 64.9, 14.4), //
    NO4("EL", 4, "Electricity price area NO4 (North Norway)", 64.5, 10.5, 71.2, 31.2), //
    NO5("EL", 5, "Electricity price area NO5 (West Norway)", 59.8, 4.8, 61.3, 9.5), //
    VASS1("VASS", 1, "Watercourse region 1 (South-East Norway)", 57.9, 5.8, 62.7, 12.9), //
    VASS2("VASS", 2, "Watercourse region 2 (West Norway)", 58.5, 4.5, 62.3, 8.4), //
    VASS3("VASS", 3, "Watercourse region 3 (Central Norway)", 61.9, 5.0, 66.5, 14.5), //
    VASS4("VASS", 4, "Watercourse region 4 (North Norway)", 66.0, 12.0, 71.2, 31.2);

    private final String type;
    private final int number;
    private final String englishName;
    private final double south;
    private final double west;
    private final double north;
    private final double east;

    /**
     * @param type
     * @param number
     * @param englishName
     * @param south
     * @param west
     * @param north
     * @param east
     */
    private NVEMagasinArea(String type, int number, String englishName, double south, double west, double north, double east) {

	this.type = type;
	this.number = number;
	this.englishName = englishName;
	this.south = south;
	this.west = west;
	this.north = north;
	this.east = east;
    }

    /**
     * @return the area type of the API (omrType), e.g. "EL"
     */
    public String getType() {

	return type;
    }

    /**
     * @return the area number of the API (omrnr)
     */
    public int getNumber() {

	return number;
    }

    /**
     * @return
     */
    public String getEnglishName() {

	return englishName;
    }

    /**
     * @return south, west, north, east
     */
    public double[] getBoundingBox() {

	return new double[] { south, west, north, east };
    }

    /**
     * @param type
     * @param number
     * @return
     */
    public static Optional<NVEMagasinArea> decode(String type, int number) {

	for (NVEMagasinArea area : values()) {
	    if (area.type.equalsIgnoreCase(type) && area.number == number) {
		return Optional.of(area);
	    }
	}

	return Optional.empty();
    }
}
