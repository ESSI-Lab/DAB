package eu.essi_lab.accessor.waf.onamet;

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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.lib.net.s3.S3TransferManager;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class ONAMETAugmenter extends ResourceAugmenter<ONAMETAugmenterSetting> {

    /**
    * 
    */
    private List<String> foldersURLList;

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	if (!resource.getPropertyHandler().isDeleted()) {

	    String endpoint = resource.getSource().getEndpoint();

	    if (foldersURLList == null) {

		GSLoggerFactory.getLogger(getClass()).debug("Listing d0x/ folders STARTED");

		foldersURLList = ONAMETConnector.getFoldersURLList(endpoint);
		foldersURLList = foldersURLList.//
			stream().//
			map(url -> {
			    String out = url.replace(endpoint, "");
			    out = out.substring(0, out.indexOf("/"));
			    return out;
			}).distinct().//
			collect(Collectors.toList());

		GSLoggerFactory.getLogger(getClass()).debug("Listing d0x/ folders ENDED");
	    }

	    Optional<String> s3BucketName = getSetting().getS3BucketName();

	    GSLoggerFactory.getLogger(getClass()).debug("S3 bucket name: {}", s3BucketName);

	    String folderName = ONAMETMapper.readDirectoryName(resource);

	    GSLoggerFactory.getLogger(getClass()).debug("NC file folder name: {}", folderName);

	    String ncFilePath = ONAMETMapper.readNcFilePath(resource);

	    GSLoggerFactory.getLogger(getClass()).debug("NC file path: {}", ncFilePath);

	    File ncFile = new File(ncFilePath);

	    //
	    // delete nc files from the file system, this is done during harvesting when nc files have been processed
	    //
	    if (ncFile.exists() && getSetting().deleteNCFiles()) {

		GSLoggerFactory.getLogger(getClass()).debug("Deleting nc file {} STARTED", ncFilePath);

		boolean deleted = ncFile.delete();
		if (!deleted) {

		    GSLoggerFactory.getLogger(getClass()).error("Unable to delete nc file {}, deleting on exit", ncFilePath);

		    ncFile.deleteOnExit();
		}

		GSLoggerFactory.getLogger(getClass()).debug("Deleting nc file {} ENDED", ncFilePath);
	    }

	    //
	    // all the files with the name starting with directoryName
	    // (e.g.: directoryName -> 2021112600, nc file -> 2021112600_d03.nc)
	    // will be removed from S3
	    // this is normally done when augmenter runs on its own
	    //
	    // nc files are deleted from s3 also if they are corrupted, this is normally done during harvesting
	    //
	    if (resource.getExtensionHandler().isNCFileCorrupted() || !foldersURLList.contains(folderName)) {

		if (resource.getExtensionHandler().isNCFileCorrupted()) {

		    GSLoggerFactory.getLogger(getClass()).debug("Related nc file {} is corrupted, resource marked as deleted",
			    ncFile.getName());

		    resource.getPropertyHandler().setIsDeleted(true);

		} else {

		    GSLoggerFactory.getLogger(getClass()).debug("Folder {} no longer available, resource marked as deleted", folderName);

		    resource.getPropertyHandler().setIsDeleted(true);
		}

		//
		// removes also from the S3 bucket
		//
		if (s3BucketName.isPresent()) {

		    GSLoggerFactory.getLogger(getClass()).debug("Deleting nc file {} from S3 bucket {} STARTED", ncFile.getName(),
			    s3BucketName.get());

		    S3TransferManager manager = new S3TransferManager();
		    manager.setAccessKey(getSetting().getS3AccessKey().get());
		    manager.setSecreteKey(getSetting().getS3SecretKey().get());

		    manager.deleteObject(s3BucketName.get(), ncFile.getName());

		    GSLoggerFactory.getLogger(getClass()).debug("Deleting nc file {} from S3 bucket {} ENDED", ncFile.getName(),
			    s3BucketName.get());
		} else {

		    GSLoggerFactory.getLogger(getClass()).debug("URL of S3 bucket URL not provided, nothing to remove");
		}
		
		return Optional.of(resource);
	    }
	} else {

	    GSLoggerFactory.getLogger(getClass()).debug("Resource already deleted");
	}

	return Optional.empty();
    }

    @Override
    public String getType() {

	return "ONAMETAugmenter";
    }

    @Override
    protected ONAMETAugmenterSetting initSetting() {

	return new ONAMETAugmenterSetting();
    }

    @Override
    protected String initName() {

	return "ONAMET Augmenter";
    }

}
