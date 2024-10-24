package eu.essi_lab.accessor.eiffel;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.DownloadSetting;
import eu.essi_lab.cfga.gs.setting.S3StorageSetting;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class EiffelS3Connector extends HarvestedQueryConnector<EiffelS3ConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "EiffelS3Connector";
    private static final String BUCKET_NAME = "eiffel-data";

    private int recordsCount;
    private static final int TARGET_COUNT = 2888086;
    private static final Integer PAGE_SIZE = 100;
    private File eiffelDir;

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("s3/buckets/eiffel-data");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	//
	//
	//

	if (request.isFirst()) {

	    GSLoggerFactory.getLogger(getClass()).debug("Download from S3 STARTED");

	    try {
		eiffelDir = Files.createTempDirectory("eiffel-bulk").toFile();

		DownloadSetting downloadSetting = ConfigurationWrapper.getDownloadSetting();
		S3StorageSetting s3StorageSetting = downloadSetting.getS3StorageSetting();

		GSLoggerFactory.getLogger(getClass()).debug("Using access key: {}", s3StorageSetting.getAccessKey().get());
		GSLoggerFactory.getLogger(getClass()).debug("Using secret key: {}", s3StorageSetting.getSecretKey().get());

		S3TransferWrapper wrapper = new S3TransferWrapper();
		wrapper.setAccessKey(s3StorageSetting.getAccessKey().get());
		wrapper.setSecretKey(s3StorageSetting.getSecretKey().get());

		wrapper.downloadDir(eiffelDir, BUCKET_NAME);

	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Download from S3 ENDED");
	}

	// eiffelDir = new File("C:/Users/paped/AppData/Local/Temp/eiffel-bulk2450398037114718265");

	//
	//
	//

	String resumptionToken = request.getResumptionToken();

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<OriginalMetadata>();

	int index = 1;

	if (resumptionToken != null) {

	    index = Integer.valueOf(resumptionToken);
	}

	int end = index + PAGE_SIZE < TARGET_COUNT ? index + PAGE_SIZE : TARGET_COUNT;

	GSLoggerFactory.getLogger(getClass()).debug("Creating records [{}/{}] STARTED", index, end);

	GSLoggerFactory.getLogger(getClass()).debug("Starting from index: {}", index);

	for (int i = index; i <= end; i++) {

	    try {

		File file = new File(eiffelDir, i + ".json");

		GSLoggerFactory.getLogger(getClass()).debug("File name: " + file);

		FileInputStream fileInputStream = new FileInputStream(file);
		String json = IOStreamUtils.asUTF8String(fileInputStream);

		JSONObject jsonObject = new JSONObject(json);

		OriginalMetadata originalMetadata = new OriginalMetadata();
		originalMetadata.setMetadata(jsonObject.toString(3));
		originalMetadata.setSchemeURI(EiffelS3Mapper.EIFFEL_SCHEMA);

		response.addRecord(originalMetadata);

		recordsCount++;

		fileInputStream.close();
		boolean delete = file.delete();

		if (!delete) {
		    GSLoggerFactory.getLogger(getClass()).warn("Unable to delete file!");
		    file.deleteOnExit();
		}

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("Creating records [{}/{}] ENDED", index, end);

	String percent = StringUtils.format(((double) recordsCount / (double) TARGET_COUNT) * 100);

	GSLoggerFactory.getLogger(getClass()).debug("Progress: [{}/{}] {}%", //
		recordsCount, //
		StringUtils.format(TARGET_COUNT), //
		percent);
	//
	//
	//

	if (index + PAGE_SIZE + 1 < TARGET_COUNT) {

	    int nextContinuationToken = index + PAGE_SIZE + 1;

	    GSLoggerFactory.getLogger(getClass()).debug("Next continuation token: {}", nextContinuationToken);

	    response.setResumptionToken(String.valueOf(nextContinuationToken));

	} else {

	    GSLoggerFactory.getLogger(getClass()).debug("Next continuation token not found, exit");
	}

	return response;
    }

    // private Integer downloadObjectsToDirectory(software.amazon.awssdk.transfer.s3.S3TransferManager transferManager,
    // URI destinationPathURI,
    // String bucketName) {
    //
    // GSLoggerFactory.getLogger(getClass()).debug("DirectoryDownload STARTED");
    //
    // DirectoryDownload directoryDownload = transferManager.//
    // downloadDirectory(//
    // DownloadDirectoryRequest.//
    // builder().//
    // destination(Paths.get(destinationPathURI)).//
    // bucket(bucketName).//
    // build());
    //
    // GSLoggerFactory.getLogger(getClass()).debug("DirectoryDownload ENDED");
    //
    // GSLoggerFactory.getLogger(getClass()).debug("CompletedDirectoryDownload STARTED");
    //
    // CompletedDirectoryDownload completedDirectoryDownload = directoryDownload.//
    // completionFuture().//
    // join();
    //
    // GSLoggerFactory.getLogger(getClass()).debug("CompletedDirectoryDownload ENDED");
    //
    // completedDirectoryDownload.failedTransfers()
    // .forEach(fail -> System.out.println("Object [" + fail.toString() + "] failed to transfer"));
    //
    // return completedDirectoryDownload.failedTransfers().size();
    // }
    //
    // private void doDownload(URI destination) {
    //
    // DownloadSetting downloadSetting = ConfigurationWrapper.getDownloadSetting();
    // S3StorageSetting s3StorageSetting = downloadSetting.getS3StorageSetting();
    //
    // GSLoggerFactory.getLogger(getClass()).debug("Using access key: {}", s3StorageSetting.getAccessKey().get());
    // GSLoggerFactory.getLogger(getClass()).debug("Using secret key: {}", s3StorageSetting.getSecretKey().get());
    //
    // GSLoggerFactory.getLogger(getClass()).debug("Creating AWS credentials STARTED");
    //
    // AwsBasicCredentials awsCreds = AwsBasicCredentials.create(//
    // s3StorageSetting.getAccessKey().get(), s3StorageSetting.getSecretKey().get());
    //
    // GSLoggerFactory.getLogger(getClass()).debug("Creating AWS credentials ENDED");
    //
    // GSLoggerFactory.getLogger(getClass()).debug("Creating credentials provider STARTED");
    //
    // StaticCredentialsProvider staticCredentials = StaticCredentialsProvider.create(awsCreds);
    //
    // GSLoggerFactory.getLogger(getClass()).debug("Creating credentials provider ENDED");
    //
    // GSLoggerFactory.getLogger(getClass()).debug("Creating client STARTED");
    //
    // S3AsyncClient s3AsyncClient = S3AsyncClient.//
    // crtBuilder().//
    // credentialsProvider(staticCredentials).//
    // region(Region.US_EAST_1).//
    // targetThroughputInGbps(20.0).//
    // minimumPartSizeInBytes(8L * MB).//
    // build();
    //
    // GSLoggerFactory.getLogger(getClass()).debug("Creating client ENDED");
    //
    // GSLoggerFactory.getLogger(getClass()).debug("Creating transfer manager STARTED");
    //
    // software.amazon.awssdk.transfer.s3.S3TransferManager transferManager = //
    // software.amazon.awssdk.transfer.s3.S3TransferManager.//
    // builder().//
    // s3Client(s3AsyncClient).build();
    //
    // GSLoggerFactory.getLogger(getClass()).debug("Creating transfer manager ENDED");
    //
    // downloadObjectsToDirectory(transferManager, destination, BUCKET_NAME);
    // }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(EiffelS3Mapper.EIFFEL_SCHEMA);
    }

    @Override
    protected EiffelS3ConnectorSetting initSetting() {

	return new EiffelS3ConnectorSetting();
    }

}
