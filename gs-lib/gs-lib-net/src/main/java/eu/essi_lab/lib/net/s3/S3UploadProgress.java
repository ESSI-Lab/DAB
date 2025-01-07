package eu.essi_lab.lib.net.s3;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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
// snippet-start:[s3.java1.s3_xfer_mgr_progress.import]

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.Transfer.TransferState;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.Upload;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class S3UploadProgress {

    /**
     * 
     */
    private static final DecimalFormat DECIMAL_FORMAT;

    static {

	DECIMAL_FORMAT = new DecimalFormat();
	DECIMAL_FORMAT.setGroupingSize(3);
	DECIMAL_FORMAT.setGroupingUsed(true);

	DecimalFormatSymbols symbols = new DecimalFormatSymbols();
	symbols.setGroupingSeparator('.');
	symbols.setDecimalSeparator(',');

	DECIMAL_FORMAT.setDecimalFormatSymbols(symbols);
    }

    /**
     * @param xfer
     */
    public static void waitForCompletion(Transfer xfer) {

	try {
	    xfer.waitForCompletion();
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(S3UploadProgress.class).error(e.getMessage(), e);
	}
    }

    /**
     * @param xfer
     */
    public static void showTransferProgress(Transfer xfer) {

	do {
	    try {
		Thread.sleep(100);
	    } catch (InterruptedException e) {
		return;
	    }

	    TransferProgress progress = xfer.getProgress();

	    long so_far = progress.getBytesTransferred();
	    long total = progress.getTotalBytesToTransfer();
	    double pct = progress.getPercentTransferred();

	    printProgressBar(pct, so_far, total);

	} while (xfer.isDone() == false);

	TransferState xfer_state = xfer.getState();
	GSLoggerFactory.getLogger(S3UploadProgress.class).debug(xfer_state.toString());
    }

    /**
     * @param multi_upload
     */
    public static void showMultiUploadProgress(MultipleFileUpload multi_upload) {
	// print the upload's human-readable description
	GSLoggerFactory.getLogger(S3UploadProgress.class).debug(multi_upload.getDescription());

	Collection<? extends Upload> sub_xfers = new ArrayList<Upload>();
	sub_xfers = multi_upload.getSubTransfers();

	do {
	    GSLoggerFactory.getLogger(S3UploadProgress.class).debug("\nSubtransfer progress:\n");
	    for (Upload u : sub_xfers) {
		GSLoggerFactory.getLogger(S3UploadProgress.class).debug("  " + u.getDescription());
		if (u.isDone()) {
		    TransferState xfer_state = u.getState();
		    GSLoggerFactory.getLogger(S3UploadProgress.class).debug("  " + xfer_state);
		} else {
		    TransferProgress progress = u.getProgress();
		    double pct = progress.getPercentTransferred();
		    printProgressBar(pct);
		    GSLoggerFactory.getLogger(S3UploadProgress.class).debug("");
		}
	    }

	    // wait a bit before the next update.
	    try {
		Thread.sleep(200);
	    } catch (InterruptedException e) {
		return;
	    }
	} while (multi_upload.isDone() == false);

	TransferState xfer_state = multi_upload.getState();
	GSLoggerFactory.getLogger(S3UploadProgress.class).debug("\nMultipleFileUpload " + xfer_state);
    }

    /**
     * @param pct
     * @param total
     * @param soFar
     */
    static void printProgressBar(double pct, long soFar, long total) {

	final int barSize = 40;

	final String emptyBar = "                                        ";
	final String filledBar = "########################################";

	int amtFull = (int) (barSize * (pct / 100.0));

	String info = soFar > 0 ? DECIMAL_FORMAT.format(soFar) + "/" + DECIMAL_FORMAT.format(total) : "";

	GSLoggerFactory.getLogger(S3UploadProgress.class).debug(//
		"[{}{}] [{}]", //
		filledBar.substring(0, amtFull), //
		emptyBar.substring(0, barSize - amtFull), //
		info);
    }

    /**
     * @param pct
     * @param total
     * @param so_far
     */
    static void printProgressBar(double pct) {

	printProgressBar(pct, -1, -1);
    }

    /**
    * 
    */
    static void eraseProgressBar() {

	final String erase_bar = "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b";
	GSLoggerFactory.getLogger(S3UploadProgress.class).debug(erase_bar);
    }

    /**
     * @param file_path
     * @param bucket_name
     * @param key_prefix
     * @param pause
     */
    static void uploadFileWithListener(String file_path, String bucket_name, String key_prefix, boolean pause) {
	GSLoggerFactory.getLogger(S3UploadProgress.class).debug("file: " + file_path + (pause ? " (pause)" : ""));

	String key_name = null;
	if (key_prefix != null) {
	    key_name = key_prefix + '/' + file_path;
	} else {
	    key_name = file_path;
	}

	// snippet-start:[s3.java1.s3_xfer_mgr_progress.progress_listener]
	File f = new File(file_path);
	TransferManager xfer_mgr = TransferManagerBuilder.standard().build();
	try {
	    Upload u = xfer_mgr.upload(bucket_name, key_name, f);
	    // print an empty progress bar...
	    printProgressBar(0.0);
	    u.addProgressListener(new ProgressListener() {
		public void progressChanged(ProgressEvent e) {
		    double pct = e.getBytesTransferred() * 100.0 / e.getBytes();
		    eraseProgressBar();
		    printProgressBar(pct);
		}
	    });
	    // block with Transfer.waitForCompletion()
	    S3UploadProgress.waitForCompletion(u);
	    // print the final state of the transfer.
	    TransferState xfer_state = u.getState();
	    GSLoggerFactory.getLogger(S3UploadProgress.class).debug(": " + xfer_state);
	} catch (AmazonServiceException e) {
	    System.err.println(e.getErrorMessage());
	    System.exit(1);
	}
	xfer_mgr.shutdownNow();
	// snippet-end:[s3.java1.s3_xfer_mgr_progress.progress_listener]
    }

    /**
     * @param dir_path
     * @param bucket_name
     * @param key_prefix
     * @param recursive
     * @param pause
     */
    static void uploadDirWithSubprogress(String dir_path, String bucket_name, String key_prefix, boolean recursive, boolean pause) {
	GSLoggerFactory.getLogger(S3UploadProgress.class)
		.debug("directory: " + dir_path + (recursive ? " (recursive)" : "") + (pause ? " (pause)" : ""));

	TransferManager xfer_mgr = new TransferManager();
	try {
	    MultipleFileUpload multi_upload = xfer_mgr.uploadDirectory(bucket_name, key_prefix, new File(dir_path), recursive);
	    // loop with Transfer.isDone()
	    S3UploadProgress.showMultiUploadProgress(multi_upload);
	    // or block with Transfer.waitForCompletion()
	    S3UploadProgress.waitForCompletion(multi_upload);
	} catch (AmazonServiceException e) {
	    System.err.println(e.getErrorMessage());
	    System.exit(1);
	}
	xfer_mgr.shutdownNow();
    }
}
