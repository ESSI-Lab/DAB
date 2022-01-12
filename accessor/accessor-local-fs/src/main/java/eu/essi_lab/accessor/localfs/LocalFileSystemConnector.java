package eu.essi_lab.accessor.localfs;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.cdk.harvest.AbstractHarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.common.NameSpace;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
public class LocalFileSystemConnector extends AbstractHarvestedQueryConnector {

    /**
     * 
     */
    private static final long serialVersionUID = -3290735249137667577L;
    @JsonIgnore
    private  transient ArrayList<LocalFile> files;
    @JsonIgnore
    private transient  int gmdCount;
    @JsonIgnore
    private transient  int gmiCount;
    @JsonIgnore
    private  transient int cswCount;
    @JsonIgnore
    private  transient int gsCount;
    @JsonIgnore
    private transient  int dirCount;
    @JsonIgnore
    private transient  int filesCount;
    @JsonIgnore
    private  transient int validFiles;

    public LocalFileSystemConnector() {
    }

    @Override
    public boolean enableMaxRecordsOption() {

	return false;
    }

    @Override
    public String getLabel() {

	return "Local FileSystem Connector";
    }

    private class LocalFile {

	private ClonableInputStream file;
	private String xPath;
	private String nsURI;

	public LocalFile(ClonableInputStream f, String nsURI, String xPath) {
	    file = f;
	    this.nsURI = nsURI;
	    this.xPath = xPath;
	}

	public ClonableInputStream getFile() {
	    return file;
	}

	public String getxPath() {
	    return xPath;
	}

	public String getNsURI() {
	    return nsURI;
	}
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();

	String token = request.getResumptionToken();

	int listIndex = 0;

	if (token == null) {

	    files = new ArrayList<LocalFile>();

	    gmdCount = 0;
	    gmiCount = 0;
	    cswCount = 0;
	    gsCount = 0;
	    dirCount = 0;
	    filesCount = 0;
	    validFiles = 0;

	    GSLoggerFactory.getLogger(getClass()).debug(" --- Connection started ---");
	    GSLoggerFactory.getLogger(getClass()).debug("Scanning STARTED");

	    File file = new File(getSourceURL());
	    if (file.isDirectory()) {
		File[] listFiles = file.listFiles();

		recursiveScan(ret, listFiles);
		dirCount++;

	    } else {

		checkFile(file);
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Scanning ENDED\n");

	    logScanningReport();

	} else {

	    listIndex = Integer.valueOf(token);
	}

	gmdCount = 0;
	gmiCount = 0;
	cswCount = 0;
	gsCount = 0;

	List<LocalFile> l = getList(listIndex);

	GSLoggerFactory.getLogger(getClass()).debug("List # " + listIndex);
	GSLoggerFactory.getLogger(getClass()).debug("List size: " + l.size());

	int listValids = 0;
	for (LocalFile localFile : l) {
	    try {

		String nsURI = localFile.getNsURI();
		OriginalMetadata metadata = handleFile(localFile.getFile(), nsURI, localFile.getxPath());

		if (metadata != null) {

		    switch (nsURI) {
		    case CommonNameSpaceContext.GMD_NS_URI:
			gmdCount++;
			break;
		    case CommonNameSpaceContext.GMI_NS_URI:
			gmiCount++;
			break;
		    case CommonNameSpaceContext.CSW_NS_URI:
			cswCount++;
			break;
		    case NameSpace.GS_DATA_MODEL_SCHEMA_URI:
			gsCount++;
			break;
		    }
		    validFiles++;
		    listValids++;
		    ret.addRecord(metadata);
		}
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(this.getClass()).error("Error handling local file " + l.toString());
		e.printStackTrace();
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("List invalid files: " + (l.size() - listValids));
	GSLoggerFactory.getLogger(getClass()).debug("List valid files: " + listValids + "\n");

	if (listIndex < getListsCount()) {
	    listIndex++;
	    ret.setResumptionToken(String.valueOf(listIndex));
	} else {

	    logFinalReport();

	    ret.setResumptionToken(null);
	}

	return ret;
    }

    private int getListsCount() {

	return files.size() / 50;
    }

    private List<LocalFile> getList(int listIndex) {

	int from = listIndex * 50;
	int to = from + 50;
	if (to > files.size()) {
	    to = files.size();
	}

	List<LocalFile> subList = files.subList(from, to);
	return subList;
    }

    private OriginalMetadata handleFile(ClonableInputStream clone, String schemeURI, String xPath) throws Exception {

	if (xPath == null) {

	    GSResource resource = GSResource.create(clone.clone());

	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setSchemeURI(schemeURI);
	    originalMetadata.setMetadata(resource.asString(true));

	    return originalMetadata;
	}

	OriginalMetadata metadataRecord = new OriginalMetadata();
	metadataRecord.setSchemeURI(schemeURI);

	XMLDocumentReader reader = new XMLDocumentReader(clone.clone());
	String id = reader.evaluateString("//*:" + xPath);
	if (id != null && !id.equals("")) {

	    String originalMetadata = new String(IOStreamUtils.asUTF8String(clone.clone()));
	    metadataRecord.setMetadata(originalMetadata);

	    return metadataRecord;

	} else {

	    GSLoggerFactory.getLogger(getClass()).warn("Skipping record without original identifier");
	}

	return null;
    }

    private void recursiveScan(ListRecordsResponse<OriginalMetadata> ret, File[] listFiles) {

	for (File file : listFiles) {

	    if (file.isDirectory()) {

		dirCount++;
		recursiveScan(ret, file.listFiles());

	    } else {

		checkFile(file);
	    }
	}
    }

    private boolean identifiyFile(InputStream clone, String recordName) {
	try {
	    XMLDocumentReader reader = new XMLDocumentReader(clone);
	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    return reader.evaluateBoolean("exists(" + recordName + ")");
	} catch (Exception ex) {
	    return false;
	}
    }

    private void checkFile(File file) {

	filesCount++;

	try {

	    FileInputStream fis = new FileInputStream(file);
	    ClonableInputStream clone = new ClonableInputStream(fis);

	    LocalFile local = null;

	    if (identifiyFile(clone.clone(), "csw:Record")) {

		local = new LocalFile(clone, CommonNameSpaceContext.CSW_NS_URI, "identifier");
		cswCount++;

	    } else if (identifiyFile(clone.clone(), "gmd:MD_Metadata")) {

		local = new LocalFile(clone, CommonNameSpaceContext.GMD_NS_URI, "fileIdentifier");
		gmdCount++;

	    } else if (identifiyFile(clone.clone(), "gmi:MI_Metadata")) {

		local = new LocalFile(clone, CommonNameSpaceContext.GMI_NS_URI, "fileIdentifier");
		gmiCount++;

	    } else if (identifiyFile(clone.clone(), "gs:Dataset")) {

		local = new LocalFile(clone, NameSpace.GS_DATA_MODEL_SCHEMA_URI, null);
		gsCount++;
	    }

	    if (local != null) {
		files.add(local);
	    }

	} catch (Exception ex) {
	    GSLoggerFactory.getLogger(getClass()).warn("Error occurred, unable to check file");
	    GSLoggerFactory.getLogger(getClass()).warn(ex.getMessage(), ex);
	}
    }

    private void logFinalReport() {

	GSLoggerFactory.getLogger(getClass()).debug(" --- Connection end ---\n");
	GSLoggerFactory.getLogger(getClass()).debug("Invalid files: " + (files.size() - validFiles));
	GSLoggerFactory.getLogger(getClass()).debug("Valid files: " + validFiles);
	GSLoggerFactory.getLogger(getClass()).debug("Valid GMD files: " + gmdCount);
	GSLoggerFactory.getLogger(getClass()).debug("Valid GMI files: " + gmiCount);
	GSLoggerFactory.getLogger(getClass()).debug("Valid CSW files: " + cswCount);
	GSLoggerFactory.getLogger(getClass()).debug("Valid GS files: " + gsCount + "\n");
	GSLoggerFactory.getLogger(getClass()).debug("-----------------------");
    }

    private void logScanningReport() {

	GSLoggerFactory.getLogger(getClass()).debug("--- Scanning report ---\n");
	GSLoggerFactory.getLogger(getClass()).debug("Directories: " + dirCount);
	GSLoggerFactory.getLogger(getClass()).debug("Files: " + filesCount);
	GSLoggerFactory.getLogger(getClass()).debug("Supported files: " + files.size());
	GSLoggerFactory.getLogger(getClass()).debug("GMD files: " + gmdCount);
	GSLoggerFactory.getLogger(getClass()).debug("GMI files: " + gmiCount);
	GSLoggerFactory.getLogger(getClass()).debug("CSW files: " + cswCount);
	GSLoggerFactory.getLogger(getClass()).debug("GS files: " + gsCount + "\n");
	GSLoggerFactory.getLogger(getClass()).debug("Lists to handle: " + getListsCount() + "\n");
	GSLoggerFactory.getLogger(getClass()).debug("------------------------\n");
    }

    @JsonIgnore
    @Override
    public List<String> listMetadataFormats() throws GSException {

	List<String> ret = new ArrayList<>();
	ret.add("CSW");
	ret.add("OAI-DC");
	ret.add("GMD");
	ret.add("GMI");
	ret.add(NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX);

	return ret;
    }

    @Override
    public boolean supports(Source source) {

	return source.getEndpoint().startsWith("file");
    }

    @Override
    public String getSourceURL() {

	return super.getSourceURL().replace("file://", "");
    }
}
