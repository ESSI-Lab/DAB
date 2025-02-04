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

import static com.amazonaws.services.s3.internal.Constants.MB;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.ObjectCannedAclProvider;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.github.jsonldjava.shaded.com.google.common.base.Optional;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3CrtAsyncClientBuilder;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedDirectoryDownload;
import software.amazon.awssdk.transfer.s3.model.DirectoryDownload;
import software.amazon.awssdk.transfer.s3.model.DownloadDirectoryRequest;
import software.amazon.awssdk.transfer.s3.model.DownloadDirectoryRequest.Builder;

/**
 * @author Fabrizio
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

	TransferManager xfer_mgr = createManager();

	ObjectCannedAclProvider aclProvider = aclPublicRead ? f -> CannedAccessControlList.PublicRead : null;

	try {
	    MultipleFileUpload xfer = xfer_mgr.uploadDirectory(bucketName, keyPrefix, new File(dirPath), recursive, null, null,
		    aclProvider);

	    S3UploadProgress.showTransferProgress(xfer);

	    S3UploadProgress.waitForCompletion(xfer);

	} catch (AmazonServiceException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	xfer_mgr.shutdownNow();
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
		targetThroughputInGbps(20.0).//
		minimumPartSizeInBytes(8L * MB);

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

    private Integer downloadObjectsToDirectory(//
	    S3TransferManager transferManager, //
	    File destination, //
	    String bucketName) {
	return downloadObjectsToDirectory(//
		transferManager, //
		destination, //
		bucketName, null);
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

	TransferManager xfer_mgr = createManager();

	ObjectCannedAclProvider aclProvider = aclPublicRead ? f -> CannedAccessControlList.PublicRead : null;

	try {
	    MultipleFileUpload xfer = xfer_mgr.uploadFileList(bucketName, virtualDirectoryKeyPrefix, files.get(0).getParentFile(), files,
		    null, null, aclProvider);

	    S3UploadProgress.showTransferProgress(xfer);

	    S3UploadProgress.waitForCompletion(xfer);

	} catch (AmazonServiceException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	xfer_mgr.shutdownNow();
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

	TransferManager xfer_mgr = createManager();

	PutObjectRequest request = new PutObjectRequest(bucketName, keyName, file);

	if (aclPublicRead) {

	    request = new PutObjectRequest(bucketName, keyName, file).withCannedAcl(CannedAccessControlList.PublicRead);
	}

	try {
	    Upload xfer = xfer_mgr.upload(request);

	    S3UploadProgress.showTransferProgress(xfer);

	    S3UploadProgress.waitForCompletion(xfer);

	} catch (AmazonServiceException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	xfer_mgr.shutdownNow();
    }

    /**
     * @return
     */
    public List<Bucket> listBuckets() {

	AmazonS3 client = createClient();

	return client.listBuckets();
    }

    /**
     * @param bucketName
     * @param virtualDirectoryKeyPrefix
     * @return
     */
    public int countObjects(String bucketName, String virtualDirectoryKeyPrefix) {

	ListObjectsV2Result objects = listObjects(bucketName, virtualDirectoryKeyPrefix);
	String nextContinuationToken = null;

	int count = 0;
	do {

	    count += objects.getObjectSummaries().size();
	    nextContinuationToken = objects.getNextContinuationToken();
	    objects = listObjects(bucketName, virtualDirectoryKeyPrefix, nextContinuationToken);

	} while (nextContinuationToken != null);

	return count;
    }

    /**
     * @param bucketName
     * @param objectKey
     * @param destination
     * @throws MalformedURLException
     */
    public void download(String bucketName, String objectKey, File destination) throws MalformedURLException {

	TransferManager xfer_mgr = createManager();

	Download download = xfer_mgr.download(bucketName, objectKey, destination);

	while (!download.isDone()) {

	    try {
		Thread.sleep(DOWNLOAD_SLEEP_TIME);

	    } catch (InterruptedException e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	}
    }

    /**
     * @param bucketName
     * @return
     */
    public List<S3ObjectSummary> listObjectsSummaries(String bucketName) {

	return listObjectSummaries(bucketName, null);
    }

    /**
     * @param bucketName
     * @param virtualDirectoryKeyPrefix
     * @return
     */
    public List<S3ObjectSummary> listObjectSummaries(String bucketName, String virtualDirectoryKeyPrefix) {

	List<S3ObjectSummary> out = new ArrayList<>();

	ListObjectsV2Result objects = virtualDirectoryKeyPrefix == null ? listObjects(bucketName)
		: listObjects(bucketName, virtualDirectoryKeyPrefix);

	String nextContinuationToken = null;

	do {

	    out.addAll(objects.getObjectSummaries());

	    nextContinuationToken = objects.getNextContinuationToken();
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
    public ListObjectsV2Result listObjects(String bucketName, String virtualDirectoryKeyPrefix, String nextContinuationToken) {

	AmazonS3 client = createClient();

	ListObjectsV2Request request = new ListObjectsV2Request();
	if (nextContinuationToken != null) {
	    request.setContinuationToken(nextContinuationToken);
	}

	request.setBucketName(bucketName);
	if (virtualDirectoryKeyPrefix != null) {
	    request.setPrefix(virtualDirectoryKeyPrefix);
	}

	return client.listObjectsV2(request);
    }

    /**
     * @param request
     * @return
     */
    public ListObjectsV2Result listObjects(ListObjectsV2Request request) {

	AmazonS3 client = createClient();

	return client.listObjectsV2(request);
    }

    /**
     * @param bucketName
     * @param prefix
     * @return
     */
    public ListObjectsV2Result listObjects(String bucketName, String prefix) {

	AmazonS3 client = createClient();

	return client.listObjectsV2(bucketName, prefix);
    }

    /**
     * @param bucketName
     * @return
     */
    public ListObjectsV2Result listObjects(String bucketName) {

	AmazonS3 client = createClient();

	return client.listObjectsV2(bucketName);
    }

    /**
     * @param bucketName
     * @param objectKey
     */
    public void deleteObject(String bucketName, String objectKey) {

	AmazonS3 client = createClient();

	try {
	    client.deleteObject(bucketName, objectKey);

	} catch (AmazonServiceException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @param bucketName
     * @param key
     * @return
     */
    public URL getObjectURL(String bucketName, String key) {

	AmazonS3 s3Client = createClient();

	return s3Client.getUrl(bucketName, key);
    }

    /**
     * @param namePrefix
     * @return
     */
    private TransferManager createManager() {

	AmazonS3 s3Client = createClient();

	if (this.endpoint != null) {
	    s3Client.setEndpoint(this.endpoint);
	}

	return TransferManagerBuilder.//
		standard().//
		withS3Client(s3Client).//
		build();
    }

    /**
     * @return
     */
    private AmazonS3 createClient() {

	AWSCredentials awsCredentials = new BasicAWSCredentials(this.accessKey, this.secreteKey);

	AmazonS3 s3Client = AmazonS3ClientBuilder.//
		standard().//
		withRegion(Regions.US_EAST_1).//
		withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).//
		build();

	return s3Client;
    }
}
