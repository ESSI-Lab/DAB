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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.jsonldjava.shaded.com.google.common.base.Optional;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3CrtAsyncClientBuilder;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedDirectoryDownload;
import software.amazon.awssdk.transfer.s3.model.CompletedDirectoryUpload;
import software.amazon.awssdk.transfer.s3.model.CompletedFileDownload;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;
import software.amazon.awssdk.transfer.s3.model.DirectoryDownload;
import software.amazon.awssdk.transfer.s3.model.DownloadDirectoryRequest;
import software.amazon.awssdk.transfer.s3.model.DownloadDirectoryRequest.Builder;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;
import software.amazon.awssdk.transfer.s3.model.FileDownload;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadDirectoryRequest;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;

/**
 * @author Fabrizio
 * @author boldrini
 */
public class S3TransferWrapper {

    private static final long DOWNLOAD_SLEEP_TIME = TimeUnit.SECONDS.toMillis(1);

    /**
     * 
     */
    public S3TransferWrapper() {
    }

    /**
     * 
     */
    private String accessKey;
    /**
     * 
     */
    private String secreteKey;
    /**
     * 
     */
    private String endpoint;
    /**
     * 
     */
    private boolean aclPublicRead;

    /**
     * @return the accessKey
     */
    public String getAccessKey() {

	return accessKey;
    }

    /**
     * @param accessKey the accessKey to set
     */
    public void setAccessKey(String accessKey) {

	this.accessKey = accessKey;
    }

    /**
     * @return the secreteKey
     */
    public String getSecretKey() {

	return secreteKey;
    }

    /**
     * @param secreteKey the secreteKey to set
     */
    public void setSecretKey(String secreteKey) {

	this.secreteKey = secreteKey;
    }

    /**
     * @return the endpoint
     */
    public Optional<String> getEndpoint() {

	return Optional.fromNullable(endpoint);
    }

    /**
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(String endpoint) {

	this.endpoint = endpoint;
    }

    /**
     * @param aclPublicRead
     */
    public void setACLPublicRead(boolean aclPublicRead) {

	this.aclPublicRead = aclPublicRead;
    }

    /**
     * @return
     */
    public boolean isAclPublicReadSet() {

	return aclPublicRead;
    }

    /**
     * @param dirPath
     * @param bucketName
     * @param recursive
     */
    public void uploadDir(String dirPath, String bucketName, boolean recursive) {

	uploadDir(dirPath, bucketName, null, recursive);
    }

    /**
     * @param dirPath
     * @param bucketName
     * @param keyPrefix
     * @param recursive
     */
    public void uploadDir(String dirPath, String bucketName, String keyPrefix, boolean recursive) {

	File directory = new File(dirPath);

	File[] children = directory.listFiles();

	for (File file : children) {
	    if (file.isDirectory()) {
		if (recursive) {
		    uploadDir(file.getAbsolutePath(), bucketName, keyPrefix + "/" + file.getName(), recursive);
		}
	    }
	    if (file.isFile()) {
		uploadFile(file.getAbsolutePath(), bucketName, keyPrefix + "/" + file.getName());
	    }
	}

    }

    public void downloadDir(File destination, String bucketName) {
	downloadDir(destination, bucketName, null);
    }

    /**
     * @param destination
     * @param bucketName
     */
    public void downloadDir(File destination, String bucketName, String folderName) {

	GSLoggerFactory.getLogger(getClass()).debug("Creating AWS credentials STARTED");

	AwsBasicCredentials awsCreds = null;

	String myAccessKey = getAccessKey();

	String mySecretKey = getSecretKey();

	if (myAccessKey != null && mySecretKey != null) {
	    awsCreds = AwsBasicCredentials.create(//
		    myAccessKey, mySecretKey);
	    GSLoggerFactory.getLogger(getClass()).debug("Creating AWS credentials ENDED");
	} else {

	    GSLoggerFactory.getLogger(getClass()).debug("Creating AWS credentials not needed");
	}

	S3CrtAsyncClientBuilder builder = S3AsyncClient.//
		crtBuilder().//
		region(Region.US_EAST_1).//
		targetThroughputInGbps(20.0);

	if (awsCreds != null) {
	    GSLoggerFactory.getLogger(getClass()).debug("Creating credentials provider STARTED");

	    StaticCredentialsProvider staticCredentials = StaticCredentialsProvider.create(awsCreds);

	    builder = builder.credentialsProvider(staticCredentials);

	    GSLoggerFactory.getLogger(getClass()).debug("Creating credentials provider ENDED");

	}

	GSLoggerFactory.getLogger(getClass()).debug("Creating client STARTED");

	S3AsyncClient s3AsyncClient = builder.build();

	GSLoggerFactory.getLogger(getClass()).debug("Creating client ENDED");

	GSLoggerFactory.getLogger(getClass()).debug("Creating transfer manager STARTED");

	S3TransferManager transferManager = //
		S3TransferManager.//
			builder().//
			s3Client(s3AsyncClient).build();

	GSLoggerFactory.getLogger(getClass()).debug("Creating transfer manager ENDED");

	downloadObjectsToDirectory(transferManager, destination, bucketName, folderName);
    }

    /**
     * @param transferManager
     * @param destination
     * @param bucketName
     * @return
     */
    private Integer downloadObjectsToDirectory(//
	    S3TransferManager transferManager, //
	    File destination, //
	    String bucketName, String folderName) {

	GSLoggerFactory.getLogger(getClass()).debug("DirectoryDownload STARTED");

	Builder builder = DownloadDirectoryRequest.builder().destination(destination.toPath()).bucket(bucketName);
	if (folderName != null && !folderName.isEmpty()) {
	    builder = builder.listObjectsV2RequestTransformer(transformer -> transformer.prefix(folderName).delimiter("/"));
	}
	DownloadDirectoryRequest request = builder.build();
	DirectoryDownload directoryDownload = transferManager.downloadDirectory(request);

	GSLoggerFactory.getLogger(getClass()).debug("DirectoryDownload ENDED");

	GSLoggerFactory.getLogger(getClass()).debug("CompletedDirectoryDownload STARTED");

	CompletedDirectoryDownload completedDirectoryDownload = directoryDownload.//
		completionFuture().//
		join();

	GSLoggerFactory.getLogger(getClass()).debug("CompletedDirectoryDownload ENDED");

	completedDirectoryDownload.failedTransfers()
		.forEach(fail -> System.out.println("Object [" + fail.toString() + "] failed to transfer"));

	return completedDirectoryDownload.failedTransfers().size();
    }

    /**
     * @param files
     * @param bucketName
     */
    public void uploadFileList(List<File> files, String bucketName) {

	uploadFileList(files, bucketName, null);
    }

    /**
     * @param files
     * @param bucketName
     * @param virtualDirectoryKeyPrefix
     */
    public void uploadFileList(List<File> files, String bucketName, String virtualDirectoryKeyPrefix) {

	for (File file : files) {
	    uploadFile(file.getAbsolutePath(), bucketName, virtualDirectoryKeyPrefix + file.getName());
	}

	System.out.println("All files uploaded successfully.");
    }

    /**
     * @param filePath
     * @param bucketName
     */
    public void uploadFile(String filePath, String bucketName) {

	uploadFile(filePath, bucketName, null);
    }

    /**
     * @param filePath
     * @param bucketName
     * @param namePrefix
     */
    public void uploadFile(String filePath, String bucketName, String keyName) {

	File file = new File(filePath);

	if (keyName == null) {
	    keyName = file.getName();
	}

	S3TransferManager xfer_mgr = createManager();

	PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder().bucket(bucketName).key(keyName);

	if (aclPublicRead) {
	    requestBuilder.acl(ObjectCannedACL.PUBLIC_READ);
	} else {
	    requestBuilder.acl(ObjectCannedACL.PRIVATE);
	}

	PutObjectRequest putObjectRequest = requestBuilder.build();

	// Create UploadFileRequest
	UploadFileRequest uploadFileRequest = UploadFileRequest.builder().putObjectRequest(putObjectRequest).source(file.toPath()).build();

	// Start the upload
	FileUpload upload = xfer_mgr.uploadFile(uploadFileRequest);

	// Show progress and wait for completion
	CompletableFuture<CompletedFileUpload> uploadCompletion = upload.completionFuture();
	uploadCompletion.join(); // Blocking call to wait for upload completion

	System.out.println("Upload completed!");

	// Shutdown transfer manager
	xfer_mgr.close();
    }

    /**
     * @return
     */
    public List<Bucket> listBuckets() {

	S3AsyncClient client = createClient();

	try {
	    return client.listBuckets().get().buckets();
	} catch (InterruptedException | ExecutionException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * @param bucketName
     * @param virtualDirectoryKeyPrefix
     * @return
     */
    public int countObjects(String bucketName, String virtualDirectoryKeyPrefix) {

	ListObjectsV2Response listResponse = listObjects(bucketName, virtualDirectoryKeyPrefix);
	String nextContinuationToken = null;

	int count = 0;
	do {

	    count += listResponse.contents().size();
	    nextContinuationToken = listResponse.nextContinuationToken();
	    listResponse = listObjects(bucketName, virtualDirectoryKeyPrefix, nextContinuationToken);

	} while (nextContinuationToken != null);

	return count;
    }

    public Date getObjectDate(String bucketName, String objectKey) {
	try {
	    SimpleEntry<S3AsyncClient, S3TransferManager> pair = createManagerAndClient();
	    HeadObjectRequest headRequest = HeadObjectRequest.builder().bucket(bucketName).key(objectKey).build();
	    HeadObjectResponse response = pair.getKey().headObject(headRequest).get();
	    return new Date(1000l * response.lastModified().getEpochSecond());
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * @param bucketName
     * @param objectKey
     * @param destination
     * @throws MalformedURLException
     */
    public boolean download(String bucketName, String objectKey, File destination) throws MalformedURLException {

	if (getObjectDate(bucketName, objectKey)==null) {
	    return false;
	}

	S3TransferManager xfer_mgr = createManager();

	GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(objectKey).build();

	DownloadFileRequest downloadRequest = DownloadFileRequest.builder().getObjectRequest(getObjectRequest).destination(destination)
		.build();

	FileDownload download = xfer_mgr.downloadFile(downloadRequest);

	CompletableFuture<CompletedFileDownload> future = download.completionFuture();

	try {
	    future.get();
	} catch (Exception e) {
	    return false;
	}

	return true;
    }

    /**
     * @param bucketName
     * @return
     */
    public List<S3Object> listObjectsSummaries(String bucketName) {

	return listObjectSummaries(bucketName, null);
    }

    /**
     * @param bucketName
     * @param virtualDirectoryKeyPrefix
     * @return
     */
    public List<S3Object> listObjectSummaries(String bucketName, String virtualDirectoryKeyPrefix) {

	List<S3Object> out = new ArrayList<>();

	ListObjectsV2Response objects = virtualDirectoryKeyPrefix == null ? listObjects(bucketName)
		: listObjects(bucketName, virtualDirectoryKeyPrefix);

	String nextContinuationToken = null;

	do {

	    out.addAll(objects.contents());

	    nextContinuationToken = objects.nextContinuationToken();
	    objects = listObjects(bucketName, virtualDirectoryKeyPrefix, nextContinuationToken);

	} while (nextContinuationToken != null);

	return out;
    }

    /**
     * @param bucketName
     * @param virtualDirectoryKeyPrefix it can be <code>null</code>
     * @param nextContinuationToken
     * @return
     */
    public ListObjectsV2Response listObjects(String bucketName, String virtualDirectoryKeyPrefix, String nextContinuationToken) {

	S3AsyncClient client = createClient();

	ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder().bucket(bucketName);

	// Set continuation token if provided
	if (nextContinuationToken != null) {
	    requestBuilder.continuationToken(nextContinuationToken);
	}

	// Set prefix for virtual directory if provided
	if (virtualDirectoryKeyPrefix != null) {
	    requestBuilder.prefix(virtualDirectoryKeyPrefix);
	}

	// Build the request and call listObjectsV2
	ListObjectsV2Request request = requestBuilder.build();
	ListObjectsV2Response response = null;
	try {
	    response = client.listObjectsV2(request).get();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return response;
    }

    /**
     * @param request
     * @return
     */
    public ListObjectsV2Response listObjects(ListObjectsV2Request request) {

	S3AsyncClient client = createClient();

	ListObjectsV2Response ret = null;
	try {
	    ret = client.listObjectsV2(request).get();
	} catch (InterruptedException | ExecutionException e) {
	    e.printStackTrace();
	}

	return ret;
    }

    /**
     * @param bucketName
     * @param prefix
     * @return
     */
    public ListObjectsV2Response listObjects(String bucketName, String prefix) {
	S3AsyncClient client = createClient();

	ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix).build();

	CompletableFuture<ListObjectsV2Response> response = client.listObjectsV2(request);

	try {
	    return response.get();
	} catch (InterruptedException | ExecutionException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * @param bucketName
     * @return
     */
    public ListObjectsV2Response listObjects(String bucketName) {
	S3AsyncClient client = createClient();

	ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).build();

	CompletableFuture<ListObjectsV2Response> response = client.listObjectsV2(request);

	try {
	    return response.get();
	} catch (InterruptedException | ExecutionException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * @param bucketName
     * @param objectKey
     */
    public void deleteObject(String bucketName, String objectKey) {

	S3AsyncClient client = createClient();

	try {
	    DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build();

	    CompletableFuture<DeleteObjectResponse> future = client.deleteObject(request);

	    future.join();

	} catch (S3Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.awsErrorDetails().errorMessage(), e);
	}
    }

    /**
     * @param bucketName
     * @param key
     * @return
     */
    public URL getObjectURL(String bucketName, String key) {

	S3AsyncClient s3Client = createClient();
	S3Utilities s3Utilities = s3Client.utilities();

	return s3Utilities.getUrl(GetUrlRequest.builder().bucket(bucketName).key(key).build());
    }

    /**
     * @param namePrefix
     * @return
     */
    private SimpleEntry<S3AsyncClient, S3TransferManager> createManagerAndClient() {

	S3AsyncClient s3AsyncClient = createClient();

	S3TransferManager transferManager = //
		S3TransferManager.//
			builder().//
			s3Client(s3AsyncClient).build();

	return new SimpleEntry<S3AsyncClient, S3TransferManager>(s3AsyncClient, transferManager);
    }

    private S3TransferManager createManager() {

	return createManagerAndClient().getValue();
    }

    /**
     * @return
     */
    private S3AsyncClient createClient() {

	GSLoggerFactory.getLogger(getClass()).debug("Creating AWS credentials STARTED");

	AwsBasicCredentials awsCreds = null;

	String myAccessKey = getAccessKey();

	String mySecretKey = getSecretKey();

	if (myAccessKey != null && mySecretKey != null) {
	    awsCreds = AwsBasicCredentials.create(//
		    myAccessKey, mySecretKey);
	    GSLoggerFactory.getLogger(getClass()).debug("Creating AWS credentials ENDED");
	} else {

	    GSLoggerFactory.getLogger(getClass()).debug("Creating AWS credentials not needed");
	}

	S3CrtAsyncClientBuilder builder = S3AsyncClient.//
		crtBuilder().//
		region(Region.US_EAST_1).//
		targetThroughputInGbps(20.0).//
		minimumPartSizeInBytes(8L * 1048576l);

	if (awsCreds != null) {
	    GSLoggerFactory.getLogger(getClass()).debug("Creating credentials provider STARTED");

	    StaticCredentialsProvider staticCredentials = StaticCredentialsProvider.create(awsCreds);

	    builder = builder.credentialsProvider(staticCredentials);

	    GSLoggerFactory.getLogger(getClass()).debug("Creating credentials provider ENDED");

	}

	GSLoggerFactory.getLogger(getClass()).debug("Creating client STARTED");

	S3AsyncClient s3AsyncClient = builder.build();

	return s3AsyncClient;
    }
}
