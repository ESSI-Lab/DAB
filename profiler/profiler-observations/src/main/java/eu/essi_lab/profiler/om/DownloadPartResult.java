package eu.essi_lab.profiler.om;

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

import java.io.File;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Result of a partial download. When size limits are used, the download may
 * return multiple parts; each part is published and the resumption token is
 * used to continue the next part.
 */
public class DownloadPartResult {

    private final File partFile;
    private final String resumptionToken;
    private final boolean maxSizeReached;
    private final String errorMessage;
    private final BigDecimal sizeInMB;
    private final BigDecimal uncompressedSizeInMB;
    private final boolean finalPart;
    private final List<String> downloadedFileNames;

    public DownloadPartResult(File partFile, String resumptionToken, boolean maxSizeReached, String errorMessage,
	    BigDecimal sizeInMB, BigDecimal uncompressedSizeInMB, boolean finalPart, List<String> downloadedFileNames) {
	this.partFile = partFile;
	this.resumptionToken = resumptionToken;
	this.maxSizeReached = maxSizeReached;
	this.errorMessage = errorMessage;
	this.sizeInMB = sizeInMB;
	this.uncompressedSizeInMB = uncompressedSizeInMB;
	this.finalPart = finalPart;
	this.downloadedFileNames = downloadedFileNames != null ? downloadedFileNames : Collections.emptyList();
    }

    public File getPartFile() {
	return partFile;
    }

    public String getResumptionToken() {
	return resumptionToken;
    }

    public boolean isMaxSizeReached() {
	return maxSizeReached;
    }

    public String getErrorMessage() {
	return errorMessage;
    }

    public BigDecimal getSizeInMB() {
	return sizeInMB;
    }

    public BigDecimal getUncompressedSizeInMB() {
	return uncompressedSizeInMB;
    }

    public boolean isFinalPart() {
	return finalPart;
    }

    public List<String> getDownloadedFileNames() {
	return downloadedFileNames;
    }
}
