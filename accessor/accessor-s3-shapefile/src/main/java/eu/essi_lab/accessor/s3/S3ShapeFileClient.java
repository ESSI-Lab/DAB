package eu.essi_lab.accessor.s3;

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

import java.io.File;

import eu.essi_lab.lib.net.s3.S3TransferWrapper;

public class S3ShapeFileClient {

    private String bucket;
    private String folder;

    private static final String S3_LINK = ".s3.amazonaws.com";

    public S3ShapeFileClient(String endpoint) {

	if (endpoint != null && endpoint.contains(S3_LINK)) {

	    String[] split = endpoint.split(S3_LINK + "/");

	    this.bucket = split[0].replace("//", "").replace("https:", "").replace("http:", "");
	    this.folder = split[1];

	    if (folder.startsWith("/")) {
		folder = folder.substring(1);
	    }

	    if (folder.contains("/")) {
		folder = folder.substring(0, folder.indexOf("/"));
	    }

	    folder = folder.replace("/", "");
	}
    }

    public void downloadTo(File destinationFolder) {
	S3TransferWrapper wrapper = new S3TransferWrapper();
	wrapper.downloadDir(destinationFolder, bucket, folder);
    }
}
