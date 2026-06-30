package eu.essi_lab.accessor.hiscentral.deflusso;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import eu.essi_lab.model.ratings.Formula;
import eu.essi_lab.model.ratings.FormulaRange;
import eu.essi_lab.model.ratings.RatingCurve;
import eu.essi_lab.model.ratings.RatingCurves;

/**
 * Harvests rating curves ("scale di deflusso") from a shared SharePoint folder of {@code .xlsx} files.
 * <p>
 * Each spreadsheet row layout is: <em>station id | station name | level range | discharge formula</em>. A new
 * station starts whenever the first column is non-empty; following rows with an empty first column add further
 * range/formula pieces to the same station. Stations whose id is {@code skip} are ignored. The piece-wise formula
 * of each station is sampled into {@link RatingCurve} points.
 *
 * @author boldrini
 */
public class RatingCurvesClient {

    private static final String STATION_HEADER = "Id stazione";
    private static final String SKIP_MARKER = "skip";

    private static final int COL_ID = 0;
    private static final int COL_NAME = 1;
    private static final int COL_RANGE = 2;
    private static final int COL_FORMULA = 3;
    private static final int COL_MIN = 4;
    private static final int COL_MAX = 5;
    private static final int COL_POINTS = 6;

    private final String endpoint;

    /**
     * @param endpoint SharePoint folder sharing link (publicly readable)
     */
    public RatingCurvesClient(String endpoint) {
	this.endpoint = endpoint;
    }

    /**
     * Scans every folder reachable from the sharing link, parsing each {@code .xlsx} into a list of per-station
     * {@link RatingCurves}.
     */
    public List<RatingCurves> harvest() throws IOException {

	List<RatingCurves> stations = new ArrayList<>();
	SharePointClient client = new SharePointClient(endpoint);

	parseFolderFiles(client, stations, null);

	for (String folder : client.listFolders()) {
	    client.cd(folder);
	    parseFolderFiles(client, stations, folder);
	    client.cdRoot();
	}
	return stations;
    }

    /**
     * Harvests the shared folder and returns the rating curves of the station with the given identifier in the
     * given HIS-Central source (SharePoint sub-folder name).
     *
     * @return the matching station, or {@code null} if no station matches
     */
    public RatingCurves getRatingCurves(String sourceId, String stationIdentifier) throws IOException {

	for (RatingCurves station : harvest()) {
	    if (station.getStationIdentifier() != null && station.getStationIdentifier().equals(stationIdentifier)
		    && (sourceId == null || sourceId.equals(station.getSourceId()))) {
		return station;
	    }
	}
	return null;
    }

    /**
     * @deprecated use {@link #getRatingCurves(String, String)}
     */
    @Deprecated
    public RatingCurves getRatingCurves(String stationIdentifier) throws IOException {
	return getRatingCurves(null, stationIdentifier);
    }

    private void parseFolderFiles(SharePointClient client, List<RatingCurves> stations, String sourceId) throws IOException {

	for (String xlsx : client.listFiles("*.xlsx")) {
	    try (XSSFWorkbook workbook = client.readXlsx(xlsx)) {
		stations.addAll(parseWorkbook(workbook, sourceId));
	    }
	}
    }

    /**
     * Parses the first sheet of a workbook into per-station {@link RatingCurves}, generating sample points for each.
     */
    static List<RatingCurves> parseWorkbook(XSSFWorkbook workbook) {
	return parseWorkbook(workbook, null);
    }

    static List<RatingCurves> parseWorkbook(XSSFWorkbook workbook, String sourceId) {

	List<RatingCurves> stations = new ArrayList<>();
	DataFormatter formatter = new DataFormatter();
	Sheet sheet = workbook.getSheetAt(0);

	Formula currentFormula = null;
	boolean skipping = false;

	for (Row row : sheet) {

	    String id = cell(formatter, row, COL_ID).trim();
	    String name = cell(formatter, row, COL_NAME).trim();
	    String rangeExpression = cell(formatter, row, COL_RANGE).trim();
	    String formulaExpression = cell(formatter, row, COL_FORMULA).trim();

	    if (!id.isEmpty()) {

		if (id.equalsIgnoreCase(STATION_HEADER)) {
		    currentFormula = null;
		    skipping = false;
		    continue;
		}
		if (id.equalsIgnoreCase(SKIP_MARKER)) {
		    currentFormula = null;
		    skipping = true;
		    continue;
		}

		skipping = false;
		RatingCurve curve = new RatingCurve();
		currentFormula = new Formula();
		curve.setFormula(currentFormula);
		curve.setMinLevel(parseNumber(cell(formatter, row, COL_MIN)));
		curve.setMaxLevel(parseNumber(cell(formatter, row, COL_MAX)));
		curve.setNumberOfPoints((int) Math.round(parseNumber(cell(formatter, row, COL_POINTS))));

		RatingCurves station = new RatingCurves(id, name);
		station.setSourceId(sourceId);
		station.getCurves().add(curve);
		stations.add(station);
	    }

	    if (skipping || currentFormula == null) {
		continue;
	    }

	    FormulaRange range = FormulaRange.parse(rangeExpression, formulaExpression);
	    if (range != null) {
		currentFormula.addRange(range);
	    }
	}

	for (RatingCurves station : stations) {
	    for (RatingCurve curve : station.getCurves()) {
		curve.generatePoints();
	    }
	}
	return stations;
    }

    private static String cell(DataFormatter formatter, Row row, int index) {

	Cell cell = row.getCell(index);
	return cell == null ? "" : formatter.formatCellValue(cell);
    }

    /**
     * Parses a numeric cell value, tolerating comma decimal separators. Returns {@link Double#NaN} when empty or
     * not a number.
     */
    private static double parseNumber(String value) {

	String normalized = value.replace(',', '.').trim();
	if (normalized.isEmpty()) {
	    return Double.NaN;
	}
	try {
	    return Double.parseDouble(normalized);
	} catch (NumberFormatException e) {
	    return Double.NaN;
	}
    }

    /**
     * Example: harvests the shared rating-curve folder and prints every parsed station.
     */
    public static void main(String[] args) throws Exception {

	String url = args.length > 0 ? args[0]
		: "https://cnrsc-my.sharepoint.com/:f:/g/personal/enrico_boldrini_cnr_it/IgDKzX2pfPsjSItF5ROJac6-AXIn4fWaJP6_1bGif-8w5hw?e=z5v4xO";

	RatingCurvesClient harvester = new RatingCurvesClient(url);
	List<RatingCurves> stations = harvester.harvest();

	System.out.println("Parsed " + stations.size() + " stations\n");
	for (RatingCurves station : stations) {
	    station.print();
	    System.out.println();
	}
    }
}
