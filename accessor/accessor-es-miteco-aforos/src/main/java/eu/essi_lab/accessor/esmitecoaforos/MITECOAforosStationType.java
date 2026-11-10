package eu.essi_lab.accessor.esmitecoaforos;

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

/**
 * Types of stations of the Anuario de Aforos, each one described by a table of each basin folder of the archive
 *
 * @author boldrini
 */
public enum MITECOAforosStationType {

    /**
     * Gauging station on a river (table ESTAF)
     */
    RIVER("estaf.csv", "indroea", "lugar", "River gauging station", "Estación de aforo en río"), //
    /**
     * Gauging station on a canal (table CANAL)
     */
    CANAL("canal.csv", "ref_ceh", "canal", "Canal gauging station", "Estación de aforo en canal"), //
    /**
     * Reservoir (table EMBALSE)
     */
    RESERVOIR("embalse.csv", "ref_ceh", "nom_embalse", "Reservoir", "Embalse"), //
    /**
     * Evaporation station, usually associated with a reservoir (table ESTEV)
     */
    EVAPORATION("estev.csv", "ref_evap", "lugar", "Evaporation station", "Estación evaporimétrica");

    private final String fileName;
    private final String idColumn;
    private final String nameColumn;
    private final String label;
    private final String spanishLabel;

    /**
     * @param fileName
     * @param idColumn
     * @param nameColumn
     * @param label
     * @param spanishLabel
     */
    private MITECOAforosStationType(String fileName, String idColumn, String nameColumn, String label, String spanishLabel) {

	this.fileName = fileName;
	this.idColumn = idColumn;
	this.nameColumn = nameColumn;
	this.label = label;
	this.spanishLabel = spanishLabel;
    }

    /**
     * @return the name of the table of the stations in each basin folder, e.g. "estaf.csv"
     */
    public String getFileName() {

	return fileName;
    }

    /**
     * @return the column of the station identifier, e.g. "indroea"
     */
    public String getIdColumn() {

	return idColumn;
    }

    /**
     * @return the column of the station name, e.g. "lugar"
     */
    public String getNameColumn() {

	return nameColumn;
    }

    /**
     * @return
     */
    public String getLabel() {

	return label;
    }

    /**
     * @return
     */
    public String getSpanishLabel() {

	return spanishLabel;
    }
}
