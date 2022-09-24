package eu.essi_lab.shared.resultstorage;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.lib.net.s3.S3TransferManager;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageUri;

/**
 * @author Fabrizio
 */
public class AmazonS3ResultStorage extends ResultStorage {

    /**
     * @param resultStorageURI
     */
    public AmazonS3ResultStorage(StorageUri storageURI) {

	super(storageURI);
    }

    public void store(String objectName, File file) throws Exception {

	S3TransferManager manager = new S3TransferManager();
	manager.setAccessKey(getResultStorageURI().getUser());
	manager.setSecreteKey(getResultStorageURI().getPassword());
	
	manager.setACLPublicRead(true);

	GSLoggerFactory.getLogger(getClass()).debug("Uploading file {} to bucket {} STARTED", file, getResultStorageURI().getStorageName());

	manager.uploadFile(//
		file.getAbsolutePath(), //
		getResultStorageURI().getStorageName(), //
		objectName);

	GSLoggerFactory.getLogger(getClass()).debug("Uploading file {} to bucket {} ENDED", file, getResultStorageURI().getStorageName());

	// AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(
	// new BasicAWSCredentials(getResultStorageURI().getUser(), getResultStorageURI().getPassword()));
	//
	// AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider).build();
	//
	// s3.putObject(new PutObjectRequest(getResultStorageURI().getStorageName(), objectName, file)
	// .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    @Override
    public String getStorageLocation(String objectName) {

	return getResultStorageURI().getUri() + getResultStorageURI().getStorageName() + "/" + objectName;
    }
}
