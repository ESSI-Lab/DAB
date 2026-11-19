package eu.essi_lab.accessor.hiscentral.deflusso;

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

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.ratings.Formula;
import eu.essi_lab.model.ratings.FormulaRange;
import eu.essi_lab.model.ratings.RatingCurve;
import eu.essi_lab.model.ratings.RatingCurves;

/**
 * Harvests rating curves ("scale di deflusso") from a shared SharePoint folder of {@code .xlsx} files. Each
 * sub-folder of the shared folder is named after a HIS-Central source identifier.
 * <p>
 * The first sheet of each workbook is parsed. Columns are located through the header row (the one whose first cell
 * is {@value #STATION_HEADER}), matching the header labels:
 * <ul>
 * <li><em>Id stazione</em>: station identifier (mandatory)</li>
 * <li><em>Scale di deflusso in uso</em> / <em>Nome</em>: station name</li>
 * <li><em>Range del livello</em>: water level range of the formula piece (mandatory)</li>
 * <li><em>Data inizio</em>, <em>Data fine</em>: validity period of the curve (optional, ISO 8601 or dd/MM/yyyy;
 * an empty end means the curve is still in use)</li>
 * <li><em>Scala delle portate</em>: discharge formula of the piece (mandatory); either plain text or a spreadsheet
 * formula producing the text (its cached value is used)</li>
 * <li><em>Minimo</em>, <em>Massimo</em>, <em>Punti</em>: level range and number of points to sample</li>
 * </ul>
 * Without a header row the legacy layout <em>id | name | range | formula | min | max | points</em> is assumed.
 * <p>
 * A row with a station identifier starts a new curve when the identifier changes, or when the row carries a validity
 * start or the min/max/points values; otherwise (empty identifier, or the same identifier repeated) the row adds a
 * further range/formula piece to the current curve. All the curves of the same station found in a folder are grouped
 * into a single {@link RatingCurves}, sorted by validity start. Stations whose id is {@value #SKIP_MARKER} are
 * ignored.
 *
 * @author boldrini
 */
public class RatingCurvesClient {

    public static final String STATION_HEADER = "Id stazione";
    public static final String SKIP_MARKER = "skip";

    private enum Column {
	ID, NAME, RANGE, BEGIN, END, FORMULA, MIN, MAX, POINTS
    }

    private static final DateTimeFormatter[] DATE_TIME_PATTERNS = { //
	    DateTimeFormatter.ofPattern("d/M/yyyy H:mm[:ss]"), //
	    DateTimeFormatter.ofPattern("d/M/yyyy") };

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

	stations.addAll(parseFolderFiles(client, null));

	for (String folder : client.listFolders()) {
	    client.cd(folder);
	    stations.addAll(parseFolderFiles(client, folder));
	    client.cdRoot();
	}
	return stations;
    }

    /**
     * Parses only the {@code .xlsx} files of the sub-folder named after the given HIS-Central source id.
     *
     * @param sourceId HIS-Central source id (SharePoint sub-folder name)
     * @return the parsed stations, or an empty list if no sub-folder with this name exists
     */
    public List<RatingCurves> harvest(String sourceId) throws IOException {

	SharePointClient client = new SharePointClient(endpoint);

	if (!client.listFolders().contains(sourceId)) {
	    return new ArrayList<>();
	}

	client.cd(sourceId);
	return parseFolderFiles(client, sourceId);
    }

    /**
     * Returns the rating curves of the station with the given identifier in the given HIS-Central source (SharePoint
     * sub-folder name). If the source is {@code null} every folder is scanned.
     *
     * @return the matching station, or {@code null} if no station matches
     */
    public RatingCurves getRatingCurves(String sourceId, String stationIdentifier) throws IOException {

	List<RatingCurves> stations = sourceId == null ? harvest() : harvest(sourceId);

	for (RatingCurves station : stations) {
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

    /**
     * Parses all the workbooks of the current folder, grouping the curves of the same station
     */
    private List<RatingCurves> parseFolderFiles(SharePointClient client, String sourceId) throws IOException {

	Map<String, RatingCurves> stations = new LinkedHashMap<>();

	for (String xlsx : client.listFiles("*.xlsx")) {
	    try (XSSFWorkbook workbook = client.readXlsx(xlsx)) {
		parseWorkbook(workbook, sourceId, stations);
	    }
	}

	return finalizeStations(stations);
    }

    /**
     * Parses the first sheet of a workbook into per-station {@link RatingCurves}, generating sample points for each.
     */
    static List<RatingCurves> parseWorkbook(XSSFWorkbook workbook) {
	return parseWorkbook(workbook, null);
    }

    static List<RatingCurves> parseWorkbook(XSSFWorkbook workbook, String sourceId) {

	Map<String, RatingCurves> stations = new LinkedHashMap<>();
	parseWorkbook(workbook, sourceId, stations);
	return finalizeStations(stations);
    }

    private static void parseWorkbook(XSSFWorkbook workbook, String sourceId, Map<String, RatingCurves> stations) {

	DataFormatter formatter = new DataFormatter();
	Sheet sheet = workbook.getSheetAt(0);

	EnumMap<Column, Integer> layout = legacyLayout();

	String currentId = null;
	Formula currentFormula = null;
	boolean skipping = false;

	for (Row row : sheet) {

	    String id = cell(formatter, row, layout.get(Column.ID));

	    if (id.equalsIgnoreCase(STATION_HEADER)) {
		layout = readLayout(formatter, row);
		currentId = null;
		currentFormula = null;
		skipping = false;
		continue;
	    }

	    if (id.equalsIgnoreCase(SKIP_MARKER)) {
		currentId = null;
		currentFormula = null;
		skipping = true;
		continue;
	    }

	    String rangeExpression = cell(formatter, row, layout.get(Column.RANGE));
	    String formulaExpression = cell(formatter, row, layout.get(Column.FORMULA));

	    if (!id.isEmpty()) {

		skipping = false;

		String begin = cell(formatter, row, layout.get(Column.BEGIN));
		String end = cell(formatter, row, layout.get(Column.END));
		String min = cell(formatter, row, layout.get(Column.MIN));
		String max = cell(formatter, row, layout.get(Column.MAX));
		String points = cell(formatter, row, layout.get(Column.POINTS));

		boolean newCurve = currentFormula == null || //
			!id.equals(currentId) || //
			!begin.isEmpty() || //
			!min.isEmpty() || //
			!max.isEmpty() || //
			!points.isEmpty();

		if (newCurve) {

		    String name = cell(formatter, row, layout.get(Column.NAME));

		    RatingCurves station = stations.computeIfAbsent(id, k -> new RatingCurves(id, name));
		    station.setSourceId(sourceId);
		    if ((station.getName() == null || station.getName().isEmpty()) && !name.isEmpty()) {
			station.setName(name);
		    }

		    RatingCurve curve = RatingCurve.ofInstants(parseInstant(begin, row), parseInstant(end, row));

		    currentFormula = new Formula();
		    curve.setFormula(currentFormula);
		    curve.setMinLevel(parseNumber(min));
		    curve.setMaxLevel(parseNumber(max));
		    double numberOfPoints = parseNumber(points);
		    curve.setNumberOfPoints(Double.isNaN(numberOfPoints) ? 0 : (int) Math.round(numberOfPoints));

		    station.getCurves().add(curve);

		    currentId = id;

		} else if (!end.isEmpty()) {

		    // the validity period is defined per curve, an end date on a further piece is not supported
		    GSLoggerFactory.getLogger(RatingCurvesClient.class).debug(
			    "Ignoring end date {} of range {} at row {} of station {}: the curve validity is read from its first row",
			    end, rangeExpression, row.getRowNum() + 1, id);
		}
	    }

	    if (skipping || currentFormula == null) {
		continue;
	    }

	    FormulaRange range = FormulaRange.parse(rangeExpression, formulaExpression);
	    if (range != null) {
		currentFormula.addRange(range);
	    } else if (!rangeExpression.isEmpty() || !formulaExpression.isEmpty()) {
		GSLoggerFactory.getLogger(RatingCurvesClient.class).warn("Unable to parse range '{}' with formula '{}' at row {}",
			rangeExpression, formulaExpression, row.getRowNum() + 1);
	    }
	}
    }

    /**
     * Generates the curve points and sorts the curves of each station by validity start
     */
    private static List<RatingCurves> finalizeStations(Map<String, RatingCurves> stations) {

	for (RatingCurves station : stations.values()) {

	    for (RatingCurve curve : station.getCurves()) {
		curve.generatePoints();
	    }

	    if (station.getCurves() instanceof List<RatingCurve> curves) {
		curves.sort(Comparator.comparing(RatingCurvesClient::validityStart, Comparator.nullsFirst(Comparator.naturalOrder())));
	    }
	}

	return new ArrayList<>(stations.values());
    }

    private static Instant validityStart(RatingCurve curve) {

	return curve.getBeginTime().orElse(curve.getBeginDate() == null ? null : curve.getBeginDate().atStartOfDay().toInstant(ZoneOffset.UTC));
    }

    /**
     * @return the column positions of the legacy layout (no header row)
     */
    private static EnumMap<Column, Integer> legacyLayout() {

	EnumMap<Column, Integer> layout = new EnumMap<>(Column.class);
	layout.put(Column.ID, 0);
	layout.put(Column.NAME, 1);
	layout.put(Column.RANGE, 2);
	layout.put(Column.FORMULA, 3);
	layout.put(Column.MIN, 4);
	layout.put(Column.MAX, 5);
	layout.put(Column.POINTS, 6);
	return layout;
    }

    /**
     * Reads the column positions from the header row, falling back to the legacy layout if the mandatory columns are
     * not found
     */
    private static EnumMap<Column, Integer> readLayout(DataFormatter formatter, Row header) {

	EnumMap<Column, Integer> layout = new EnumMap<>(Column.class);

	for (Cell cell : header) {

	    String label = cell(formatter, header, cell.getColumnIndex()).toLowerCase();
	    Column column = null;

	    if (label.startsWith("id")) {
		column = Column.ID;
	    } else if (label.contains("range")) {
		column = Column.RANGE;
	    } else if (label.contains("inizio")) {
		column = Column.BEGIN;
	    } else if (label.contains("fine")) {
		column = Column.END;
	    } else if (label.contains("portat") || label.contains("formula")) {
		column = Column.FORMULA;
	    } else if (label.startsWith("min")) {
		column = Column.MIN;
	    } else if (label.startsWith("max") || label.startsWith("mas")) {
		column = Column.MAX;
	    } else if (label.startsWith("punti") || label.startsWith("points")) {
		column = Column.POINTS;
	    } else if (label.contains("deflusso") || label.contains("nome") || label.contains("stazione")) {
		column = Column.NAME;
	    }

	    if (column != null) {
		layout.putIfAbsent(column, cell.getColumnIndex());
	    }
	}

	if (!layout.containsKey(Column.ID) || !layout.containsKey(Column.RANGE) || !layout.containsKey(Column.FORMULA)) {

	    GSLoggerFactory.getLogger(RatingCurvesClient.class).warn("Unrecognized header at row {}, using the legacy layout",
		    header.getRowNum() + 1);

	    return legacyLayout();
	}

	return layout;
    }

    /**
     * @return the trimmed text of the cell; formula cells return their cached result, date cells an ISO local date
     *         time
     */
    private static String cell(DataFormatter formatter, Row row, Integer index) {

	if (index == null) {
	    return "";
	}

	Cell cell = row.getCell(index);
	if (cell == null) {
	    return "";
	}

	CellType type = cell.getCellType() == CellType.FORMULA ? cell.getCachedFormulaResultType() : cell.getCellType();

	String value = switch (type) {
	case STRING -> cell.getStringCellValue();
	case NUMERIC -> DateUtil.isCellDateFormatted(cell) ? //
		cell.getLocalDateTimeCellValue().toString() : //
		cell.getCellType() == CellType.FORMULA ? NumberToTextConverter.toText(cell.getNumericCellValue()) : formatter.formatCellValue(cell);
	case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
	default -> "";
	};

	return value == null ? "" : value.trim();
    }

    /**
     * Parses a validity date. ISO 8601 values without offset and dd/MM/yyyy values are considered UTC.
     *
     * @return the instant, or {@code null} if empty or not recognized
     */
    static Instant parseInstant(String value, Row row) {

	if (value == null || value.isBlank()) {
	    return null;
	}

	String v = value.trim();

	try {
	    return OffsetDateTime.parse(v).toInstant();
	} catch (DateTimeParseException e) {
	}
	try {
	    return LocalDateTime.parse(v).toInstant(ZoneOffset.UTC);
	} catch (DateTimeParseException e) {
	}
	try {
	    return LocalDate.parse(v).atStartOfDay().toInstant(ZoneOffset.UTC);
	} catch (DateTimeParseException e) {
	}
	for (DateTimeFormatter pattern : DATE_TIME_PATTERNS) {
	    try {
		return LocalDateTime.parse(v, pattern).toInstant(ZoneOffset.UTC);
	    } catch (DateTimeParseException e) {
	    }
	    try {
		return LocalDate.parse(v, pattern).atStartOfDay().toInstant(ZoneOffset.UTC);
	    } catch (DateTimeParseException e) {
	    }
	}

	GSLoggerFactory.getLogger(RatingCurvesClient.class).warn("Unrecognized date '{}' at row {}", v,
		row == null ? "?" : row.getRowNum() + 1);

	return null;
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
     * Example: harvests the shared rating-curve folder (or only the given source sub-folder) and prints every parsed
     * station.
     */
    public static void main(String[] args) throws Exception {
	String url = args.length > 0 ? args[0]
		: "https://cnrsc-my.sharepoint.com/:f:/g/personal/enrico_boldrini_cnr_it/IgDKzX2pfPsjSItF5ROJac6-AXIn4fWaJP6_1bGif-8w5hw?e=z5v4xO";
	RatingCurvesClient harvester = new RatingCurvesClient(url);
	List<RatingCurves> stations = args.length > 1 ? harvester.harvest(args[1]) : harvester.harvest();
	System.out.println("Parsed " + stations.size() + " stations\n");
	for (RatingCurves station : stations) {
	    station.print();
	    System.out.println();
	}
    }
}
