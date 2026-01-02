package eu.essi_lab.accessor.ecv;

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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.wrapper.WrappedConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author roncella
 */
public class ECVInventoryConnector extends WrappedConnector {

    /**
     * SERVICE ENDPOINT: static xslx file
     * http://climatemonitoring.info/wp-content/uploads/2019/01/ECV_Inventory_v2.0.xlsx
     * The Essential Climate Variable (ECV) Inventory houses information on Climate Data Records (CDR) provided by CEOS
     * and CGMS member agencies.
     * The metadata columns are:
     * USED FIELDS FOR MAPPING
     * RecordID, Responder name,Responder E-mail, Co-editor E-mail (optional), Data record identifier, Data record name
     * and version (optional),Responsible
     * Organisation, Status of peer-review
     * Maintenance and user support commitment, QA Process, ECV,ECV Product,Physical quantity,SI units, Satellites and
     * Instruments (Data),
     * Link to source,Extent (Lat/Long),Domain,Horizontal resolution,Vertical resolution,Temporal resolution, Start date
     * of TCDR,End-date of TCDR,
     * CDR-generation documentation (link),Data documentation (link),Scientific-review process (link),Access point,
     * Data record (link),FCDR availability (link),Data format, Release date (yyyy)
     * ALL OTHER FIELDS
     * ,Published,Existing or Planned data record,Observer E-mail (optional),
     * TCDR family,Official citation reference (optional),
     * Collection Organisation,Calibration Organisation,FCDR Organisation,Inter-calibration,Organisation,TCDR
     * Organisation,GCOS Requirements Organisation,
     * Peer Review Organisation,Archiving Organisation,User-service organisation,User-feedback organisation,Level of
     * commitment,
     * Assessment Body,QA Organisation,GCOS-requirements compliance assessment,
     * GCOS-guidelines peer-review compliance assessment,Quantitative maturity index assessment,
     * Satellite/Instrument combination,Comments (Optional),Satellites and Instruments (Inter-calibration),
     * Inter-calibration Satellite/Instrument combination Comments (Optional),Ground-based network,
     * Domain Comment (Optional),,Accuracy,Stability,
     * TCDR heritage,,Type of access,
     * Restrictions to access,Registration / ordering ,Metadata standard,Dissemination mechanisms,
     * Release date (yyyy),Climate applications,Users
     */
    private static final String ECV_READ_ERROR = "Unable to find stations URL";

    private static final String ECV_URL_NOT_FOUND_ERROR = "ECV_STATIONS_URL_NOT_FOUND_ERROR";

    private static final String ECV_ESCAPE_SEPARATOR = "ECV_ESCAPE_SEPARATOR";

    public List<String> getECVListFiles() {
	return ECVListFiles;
    }

    private List<String> ECVListFiles;

    private Downloader downloader;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private static final int DEFAULT_PAGE_SIZE = 50;

    /**
     * 
     */
    public static final String TYPE = "ECV Inventory Connector";

    private int partialNumbers;

    public ECVInventoryConnector() {

	this.downloader = new Downloader();

	this.ECVListFiles = new ArrayList<>();

	getSetting().setPageSize(DEFAULT_PAGE_SIZE);
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest listRecords) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	Optional<InputStream> ecvStream = getECVInventoryStream();

	if (ecvStream.isPresent()) {

	    XSSFWorkbook wb = null;
	    try {
		wb = new XSSFWorkbook(ecvStream.get());
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	    XSSFSheet sheet = wb.getSheetAt(0);

	    XSSFRow headerRow = sheet.getRow(0);

	    Map<String, Integer> headerColumnMap = getECVHeaderRow(headerRow);

	    int totalRows = sheet.getPhysicalNumberOfRows();

	    logger.trace("Number of ECV colums found: {}", headerColumnMap.size());

	    // if (ECVListFiles.isEmpty()) {
	    // Optional<List<String>> stationsFile = getStationFileList();
	    //
	    // if (stationsFile.isPresent()) {
	    //
	    // ECVListFiles = stationsFile.get();
	    //
	    // logger.trace("Number of ECV stations found: {}", ECVListFiles.size());
	    //
	    // } else {
	    //
	    // throw GSException.createException(//
	    // this.getClass(), //
	    // ECV_READ_ERROR, //
	    // null, //
	    // ErrorInfo.ERRORTYPE_SERVICE, //
	    // ErrorInfo.SEVERITY_ERROR, //
	    // ECV_URL_NOT_FOUND_ERROR);
	    //
	    // }
	    // }

	    String token = listRecords.getResumptionToken();
	    int start = 1;
	    if (token != null) {

		start = Integer.valueOf(token);
	    }

	    int pageSize = getSetting().getPageSize();
	 
	    Optional<Integer> mr = getSetting().getMaxRecords();
	    boolean maxNumberReached = false;
	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && start > mr.get() - 1) {
		// max record set
		maxNumberReached = true;
	    }
	    if (start < totalRows && !maxNumberReached) {

		int end = start + pageSize;
		if (end > totalRows) {
		    end = totalRows;
		}

		if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && end > mr.get()) {
		    end = mr.get();
		}

		int count = 0;

		ArrayList<Integer> idxList = new ArrayList<Integer>();

		idxList = createIdxList(headerColumnMap);

		for (int i = start; i < totalRows; i++) {

		    Row row = sheet.getRow(i);

		    OriginalMetadata original = new OriginalMetadata();
		    original.setSchemeURI(CommonNameSpaceContext.ECV_INVENTORY);

		    String originalMetadata = createOriginalMetadata(idxList, row);

		    if (originalMetadata == null) {
			logger.debug("Record from row {} skipped. No Domain matching or Dataset title is missing", i);
			continue;
		    }

		    OriginalMetadata metadata = new OriginalMetadata();
		    metadata.setSchemeURI(CommonNameSpaceContext.ECV_INVENTORY);
		    metadata.setMetadata(originalMetadata);
		    ret.addRecord(metadata);
		    partialNumbers++;
		    count++;
		}

		ret.setResumptionToken(null);
		logger.debug("ADDED {} records. Number of analyzed stations: {}", partialNumbers, String.valueOf(start + count));

	    } else {
		ret.setResumptionToken(null);

		logger.debug("Added Collection records: {} . TOTAL STATION SIZE: {}", partialNumbers, headerColumnMap.size());
		partialNumbers = 0;
		return ret;
	    }

	    return ret;

	} else {

	    throw GSException.createException(//
		    this.getClass(), //
		    ECV_READ_ERROR, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ECV_URL_NOT_FOUND_ERROR);

	}
    }

    public ArrayList<Integer> createIdxList(Map<String, Integer> map) {

	ArrayList<Integer> idxList = new ArrayList<Integer>();

	// int idxForColumn1 = map.get("Domain");
	// int idxForColumn2 = map.get("Responder name");
	// int idxForColumn3 = map.get("Responder E-mail");
	// int idxForColumn4 = map.get("Co-editor E-mail (optional)");
	// int idxForColumn5 = map.get("Responsible Organisation");
	// int idxForColumn6 = map.get("Status of peer-review");
	// int idxForColumn7 = map.get("Maintenance and user support commitment");
	// int idxForColumn8 = map.get("QA Process");
	// int idxForColumn9 = map.get("ECV");
	// int idxForColumn10 = map.get("ECV Product");
	// int idxForColumn11 = map.get("Physical quantity");
	// int idxForColumn12 = map.get("SI units");
	// int idxForColumn13 = map.get("Satellites and Instruments (Data)");
	// int idxForColumn14 = map.get("Link to source");
	// int idxForColumn15 = map.get("Extent (Lat/Long)");
	// int idxForColumn16 = map.get("Horizontal resolution");
	// int idxForColumn17 = map.get("Vertical resolution");
	// int idxForColumn18 = map.get("Temporal resolution");
	// int idxForColumn19 = map.get("Start date of TCDR");
	// int idxForColumn20 = map.get("End-date of TCDR");
	// int idxForColumn21 = map.get("Data record (link)");
	// int idxForColumn22 = map.get("FCDR availability (link)");
	// int idxForColumn23 = map.get("Data format");

	idxList.add(map.get("Domain"));
	idxList.add(map.get("Responder name"));
	idxList.add(map.get("Responder E-mail"));
	idxList.add(map.get("Co-editor E-mail (optional)"));
	idxList.add(map.get("Responsible Organisation"));
	idxList.add(map.get("Status of peer-review"));
	idxList.add(map.get("Maintenance and user support commitment"));
	idxList.add(map.get("QA Process"));
	idxList.add(map.get("ECV"));
	idxList.add(map.get("ECV Product"));
	idxList.add(map.get("Physical quantity"));
	idxList.add(map.get("SI units"));
	idxList.add(map.get("Satellites and Instruments (Data)"));
	idxList.add(map.get("Link to source"));
	idxList.add(map.get("Extent (Lat/Long)"));
	idxList.add(map.get("Horizontal resolution"));
	idxList.add(map.get("Vertical resolution"));
	idxList.add(map.get("Temporal resolution"));
	idxList.add(map.get("Start date of TCDR"));
	idxList.add(map.get("End-date of TCDR"));
	idxList.add(map.get("Data record (link)"));
	idxList.add(map.get("FCDR availability (link)"));
	idxList.add(map.get("Data format"));
	idxList.add(map.get("Data record identifier"));
	idxList.add(map.get("Data record name and version (optional)"));
	idxList.add(map.get("RecordID"));
	idxList.add(map.get("Release date (yyyy)"));
	return idxList;

    }

    public Downloader getDownloader() {
	return downloader == null ? new Downloader() : downloader;
    }

    public void setDownloader(Downloader downloader) {

	this.downloader = downloader;
    }

    /**
     * @return
     * @throws GSException
     */

    public Optional<InputStream> getECVInventoryStream() throws GSException {

	logger.trace("Stations URL finding STARTED");

	String ECVUrl = getSourceURL();

	logger.trace("ECV URL: {}", ECVUrl);

	return downloader.downloadOptionalStream(ECVUrl);

    }

    /**
     * @param stationsURL
     * @throws GSException
     * @throws IOException
     */

    public Map<String, Integer> getECVHeaderRow(XSSFRow row) throws GSException {

	Map<String, Integer> ECVMap = new HashMap<>();

	int minColIx = row.getFirstCellNum(); // get the first column index for a row
	int maxColIx = row.getLastCellNum(); // get the last column index for a row
	for (int colIx = minColIx; colIx < maxColIx; colIx++) { // loop from first to last index
	    Cell cell = row.getCell(colIx); // get the cell
	    ECVMap.put(cell.getStringCellValue(), cell.getColumnIndex()); // add the cell contents (name of column) and
									  // cell index to the map
	}

	return ECVMap;
    }

    /**
     * @param station
     * @param name
     * @return
     */

    private String createOriginalMetadata(ArrayList<Integer> idxList, Row row) {

	logger.trace("Creating metadata results for row number {} STARTED", row.getRowNum());

	ECVInventorySatellite station = getECVInventoryFields(idxList, row);

	if (station == null)
	    return null;

	String metadataRecord = createRecord(station);

	logger.trace("Creating metadata results row number {} ENDED", row.getRowNum());

	return metadataRecord;

    }

    public ECVInventorySatellite getECVInventoryFields(ArrayList<Integer> idxList, Row row) {

	ECVInventorySatellite station = new ECVInventorySatellite();

	Cell domainCell = row.getCell(idxList.get(0)); // Get the cells for each of the indexes
	Cell responderName = row.getCell(idxList.get(1));
	Cell responderMail = row.getCell(idxList.get(2));
	Cell editorMail = row.getCell(idxList.get(3));
	Cell responsibleOrg = row.getCell(idxList.get(4));
	Cell peerReview = row.getCell(idxList.get(5));
	Cell maintenance = row.getCell(idxList.get(6));
	Cell qaProcess = row.getCell(idxList.get(7));
	Cell ecv = row.getCell(idxList.get(8));
	Cell ecvProduct = row.getCell(idxList.get(9));
	Cell physicalQuantity = row.getCell(idxList.get(10));
	Cell siUnit = row.getCell(idxList.get(11));
	Cell satInstrument = row.getCell(idxList.get(12));
	Cell linkToSource = row.getCell(idxList.get(13));
	Cell extent = row.getCell(idxList.get(14));
	Cell hresoluzion = row.getCell(idxList.get(15));
	Cell vresolution = row.getCell(idxList.get(16));
	Cell tresolution = row.getCell(idxList.get(17));
	Cell startDate = row.getCell(idxList.get(18));
	Cell endDate = row.getCell(idxList.get(19));
	Cell dataLink = row.getCell(idxList.get(20));
	Cell availabilityLink = row.getCell(idxList.get(21));
	Cell dataFormat = row.getCell(idxList.get(22));
	Cell dataRecordID = row.getCell(idxList.get(23));
	Cell dataRecordName = row.getCell(idxList.get(24));
	Cell recordID = row.getCell(idxList.get(25));
	Cell releaseDate = row.getCell(idxList.get(26));

	String domain = domainCell.getStringCellValue();
	// if there are more domains, we take into account only the first
	if (domain.contains(",")) {
	    String[] splittedString = domain.split(",");
	    domain = splittedString[0];
	}

	ECVDomain ecvDomain = getDomain();

	if (!ecvDomain.getLabel().toLowerCase().equals(domain.toLowerCase())) {
	    return null;
	}
	if (!(dataRecordName.getStringCellValue() != null && !dataRecordName.getStringCellValue().equals(""))) {
	    return null;
	}

	station.setAvailabilityLink(availabilityLink.getStringCellValue());
	station.setDataFormat(dataFormat.getStringCellValue());
	station.setDataLink(dataLink.getStringCellValue());
	station.setDataRecordID(dataRecordID.getStringCellValue());
	station.setDataRecordName(dataRecordName.getStringCellValue());
	station.setDomain(domain);
	station.setEcv(ecv.getStringCellValue());
	station.setEcvProduct(ecvProduct.getStringCellValue());
	station.setEditorMail(editorMail.getStringCellValue());
	station.setEndDate(endDate.getStringCellValue());
	station.setExtent(extent.getStringCellValue());
	station.setHresolution(hresoluzion.getStringCellValue());
	station.setLinkToSource(linkToSource.getStringCellValue());
	station.setMaintenance(maintenance.getStringCellValue());
	station.setPeerReview(peerReview.getStringCellValue());
	station.setPhysicalQuantity(physicalQuantity.getStringCellValue());
	station.setQaProcess(qaProcess.getStringCellValue());
	station.setRecordID(recordID.getStringCellValue());
	station.setReleaseDate(releaseDate.getStringCellValue());
	station.setResponderMail(responderMail.getStringCellValue());
	station.setResponderName(responderName.getStringCellValue());
	station.setResponsibleOrg(responsibleOrg.getStringCellValue());
	station.setSatInstrument(satInstrument.getStringCellValue());
	station.setSiUnit(siUnit.getStringCellValue());
	station.setStartDate(startDate.getStringCellValue());
	station.setTresolution(tresolution.getStringCellValue());
	station.setVresolution(vresolution.getStringCellValue());

	return station;
    }

    private String createRecord(ECVInventorySatellite station) {

	StringBuilder sb = new StringBuilder();

	sb.append(station.getAvailabilityLink());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getDataFormat());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getDataLink());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getDataRecordID());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getDataRecordName());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getDomain());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getEcv());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getEcvProduct());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getEditorMail());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getEndDate());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getExtent());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getHresolution());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getLinkToSource());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getMaintenance());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getPeerReview());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getPhysicalQuantity());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getQaProcess());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getRecordID());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getReleaseDate());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getResponderMail());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getResponderName());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getResponsibleOrg());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getSatInstrument());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getSiUnit());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getStartDate());
	sb.append(ECV_ESCAPE_SEPARATOR);
	sb.append(station.getTresolution());
	sb.append(ECV_ESCAPE_SEPARATOR);
	if (station.getVresolution() == null || station.getVresolution().isEmpty()) {
	    sb.append("N/A");
	} else {
	    sb.append(station.getVresolution());
	}
	sb.append(ECV_ESCAPE_SEPARATOR);

	// sb.append(txtname);

	return sb.toString();
    }

    @Override
    public List<String> listMetadataFormats() {
	List<String> toret = new ArrayList<>();
	toret.add(CommonNameSpaceContext.ECV_INVENTORY);
	return toret;
    }

    @Override
    public boolean supports(GSSource source) {

	String endpoint = source.getEndpoint();
	return endpoint.contains("climatemonitoring.info");
    }

    protected ECVDomain getDomain() {
	return ECVDomain.ALL_DOMAINS;

    }

    @Override
    public String getType() {

	return TYPE;
    }
}
