
package eu.essi_lab.accessor.ckan.datamodel;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

public class CKANDataset {
    // these fields are taken from the specs, at http://docs.ckan.org/en/ckan-1.8/domain-model-dataset.html
    private String id;
    private String name;
    private String title;
    private String url;
    private String author;
    private String authorEmail;
    private String maintainer;
    private String maintainerEmail;
    private String license;
    private String licenseId;
    private String licenseTitle;
    private String version;
    private String notes;
    private List<CKANTag> tags = new ArrayList<CKANTag>();
    private String state;
    private List<CKANResource> resources = new ArrayList<CKANResource>();
    private List<CKANRelationship> relations = new ArrayList<CKANRelationship>();

    private List<CKANGroup> groups = new ArrayList<CKANGroup>();
    private List<SimpleEntry<String, String>> extras = new ArrayList<SimpleEntry<String,String>>();

    // ////

    // additional fields, in use
    private String unpublished;
    private String primaryTheme;
    private String organization;
    private String type;
    private String revisionTimestamp;
    private String metadataCreated;
    private String metadataModified;
    private String creatorUserId;
    private String accessConstraints;

    // even additional fields (from danube scenario)
    private String bboxEastLongitude;
    private String bboxNorthLatitude;
    private String bboxSouthLatitude;
    private String bboxWestLongitude;

    private String bboxExtended;
    private String contactEmail;
    private String datasetCreationDate;
    private String datasetPublicationDate;
    private String datasetRevisionDate;
    private String datasetStartDate;
    private String datasetEndDate;
    private String frequencyOfUpdate;
    private String guid;
    private String harvestTimestamp;
    private String metadataDate;
    private String progress;
    private String resourceType;
    private String responsibleParty;
    private List<CKANPoint> polygon = new ArrayList<CKANPoint>();
    private String spatialDataServiceType;
    private String spatialReferenceSystem;
    private String spatialHarvester;
    private String harvestObjectId;
    private String harvestSourceId;
    private String harvestSourceTitle;
    private String metadataLanguage;

    public List<CKANRelationship> getRelations() {
	return relations;
    }

    public String getBboxEastLongitude() {
	return bboxEastLongitude;
    }

    public void setBboxEastLongitude(String bboxEastLongitude) {
	this.bboxEastLongitude = bboxEastLongitude;
    }

    public String getBboxNorthLatitude() {
	return bboxNorthLatitude;
    }

    public void setBboxNorthLatitude(String bboxNorthLatitude) {
	this.bboxNorthLatitude = bboxNorthLatitude;
    }

    public String getBboxSouthLatitude() {
	return bboxSouthLatitude;
    }

    public void setBboxSouthLatitude(String bboxSouthLatitude) {
	this.bboxSouthLatitude = bboxSouthLatitude;
    }

    public String getBboxWestLongitude() {
	return bboxWestLongitude;
    }

    public void setBboxWestLongitude(String bboxWestLongitude) {
	this.bboxWestLongitude = bboxWestLongitude;
    }

    public String getBboxExtended() {
	return bboxExtended;
    }

    public void setBboxExtended(String bboxExtended) {
	this.bboxExtended = bboxExtended;
    }

    public String getContactEmail() {
	return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
	this.contactEmail = contactEmail;
    }

    public String getDatasetPublicationDate() {
	return datasetPublicationDate;
    }

    public void setDatasetPublicationDate(String datasetPublicationDate) {
	this.datasetPublicationDate = datasetPublicationDate;
    }

    public String getDatasetCreationDate() {
	return datasetCreationDate;
    }

    public void setDatasetCreationDate(String datasetCreationDate) {
	this.datasetCreationDate = datasetCreationDate;
    }

    public String getDatasetRevisionDate() {
	return datasetRevisionDate;
    }

    public void setDatasetRevisionDate(String datasetRevisionDate) {
	this.datasetRevisionDate = datasetRevisionDate;
    }

    public String getFrequencyOfUpdate() {
	return frequencyOfUpdate;
    }

    public void setFrequencyOfUpdate(String frequencyOfUpdate) {
	this.frequencyOfUpdate = frequencyOfUpdate;
    }

    public String getGuid() {
	return guid;
    }

    public void setGuid(String guid) {
	this.guid = guid;
    }

    public String getHarvestTimestamp() {
	return harvestTimestamp;
    }

    public void setHarvestTimestamp(String harvestTimestamp) {
	this.harvestTimestamp = harvestTimestamp;
    }

    public String getMetadataDate() {
	return metadataDate;
    }

    public void setMetadataDate(String metadataDate) {
	this.metadataDate = metadataDate;
    }

    public String getProgress() {
	return progress;
    }

    public void setProgress(String progress) {
	this.progress = progress;
    }

    public String getResourceType() {
	return resourceType;
    }

    public void setResourceType(String resourceType) {
	this.resourceType = resourceType;
    }

    public String getResponsibleParty() {
	return responsibleParty;
    }

    public void setResponsibleParty(String responsibleParty) {
	this.responsibleParty = responsibleParty;
    }

    public List<CKANPoint> getPolygon() {
	return polygon;
    }

    public void setPolygon(List<CKANPoint> polygon) {
	this.polygon = polygon;
    }

    public String getSpatialDataServiceType() {
	return spatialDataServiceType;
    }

    public void setSpatialDataServiceType(String spatialDataServiceType) {
	this.spatialDataServiceType = spatialDataServiceType;
    }

    public String getSpatialReferenceSystem() {
	return spatialReferenceSystem;
    }

    public void setSpatialReferenceSystem(String spatialReferenceSystem) {
	this.spatialReferenceSystem = spatialReferenceSystem;
    }

    public String getSpatialHarvester() {
	return spatialHarvester;
    }

    public void setSpatialHarvester(String spatialHarvester) {
	this.spatialHarvester = spatialHarvester;
    }

    public String getHarvestObjectId() {
	return harvestObjectId;
    }

    public void setHarvestObjectId(String harvestObjectId) {
	this.harvestObjectId = harvestObjectId;
    }

    public String getHarvestSourceId() {
	return harvestSourceId;
    }

    public void setHarvestSourceId(String harvestSourceId) {
	this.harvestSourceId = harvestSourceId;
    }

    public String getHarvestSourceTitle() {
	return harvestSourceTitle;
    }

    public void setHarvestSourceTitle(String harvestSourceTitle) {
	this.harvestSourceTitle = harvestSourceTitle;
    }

    public void setResources(List<CKANResource> resources) {
	this.resources = resources;
    }

    public void setGroups(List<CKANGroup> groups) {
	this.groups = groups;
    }

    public void setExtras(List<SimpleEntry<String, String>> extras) {
	this.extras = extras;
    }

    public String getRevisionTimestamp() {
	return revisionTimestamp;
    }

    public void setRevisionTimestamp(String revisionTimestamp) {
	this.revisionTimestamp = revisionTimestamp;
    }

    public String getMetadataCreated() {
	return metadataCreated;
    }

    public void setMetadataCreated(String metadataCreated) {
	this.metadataCreated = metadataCreated;
    }

    public String getMetadataModified() {
	return metadataModified;
    }

    public void setMetadataLanguage(String metadataLanguage) {
	this.metadataLanguage = metadataLanguage;
    }

    public String getMetadataLanguage() {
	return metadataLanguage;
    }

    public void setMetadataModified(String metadataModified) {
	this.metadataModified = metadataModified;
    }

    public String getCreatorUserId() {
	return creatorUserId;
    }

    public void setCreatorUserId(String creatorUserId) {
	this.creatorUserId = creatorUserId;
    }

    public String getType() {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    public String getOrganization() {
	return organization;
    }

    public void setOrganization(String organization) {
	this.organization = organization;
    }

    public String getId() {
	return id;
    }

    public void setId(String ide) {
	this.id = ide;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    public String getAuthor() {
	return author;
    }

    public void setAuthor(String author) {
	this.author = author;
    }

    public String getAuthorEmail() {
	return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
	this.authorEmail = authorEmail;
    }

    public String getMaintainer() {
	return maintainer;
    }

    public void setMaintainer(String maintainer) {
	this.maintainer = maintainer;
    }

    public String getMaintainerEmail() {
	return maintainerEmail;
    }

    public void setMaintainerEmail(String maintainerEmail) {
	this.maintainerEmail = maintainerEmail;
    }

    public String getLicense() {
	return license;
    }

    public void setLicense(String license) {
	this.license = license;
    }

    public String getLicenseId() {
	return licenseId;
    }

    public void setLicenseId(String licenseId) {
	this.licenseId = licenseId;
    }

    public String getLicenseTitle() {
	return licenseTitle;
    }

    public void setLicenseTitle(String licenseTitle) {
	this.licenseTitle = licenseTitle;
    }

    public String getVersion() {
	return version;
    }

    public void setVersion(String version) {
	this.version = version;
    }

    public String getNotes() {
	return notes;
    }

    public void setNotes(String notes) {
	this.notes = notes;
    }

    public List<CKANTag> getTags() {
	return tags;
    }

    public void setTags(List<CKANTag> tags) {
	this.tags = tags;
    }

    public String getState() {
	return state;
    }

    public void setState(String state) {
	this.state = state;
    }

    public List<CKANResource> getResources() {
	return resources;
    }

    public List<CKANGroup> getGroups() {
	return groups;
    }

    public List<SimpleEntry<String, String>> getExtras() {
	return extras;
    }

    public void setAccessConstraints(String accessConstraints) {
	this.accessConstraints = accessConstraints;

    }

    public String getAccessConstraints() {
	return accessConstraints;
    }

    public String getPrimaryTheme() {
	return primaryTheme;
    }

    public void setPrimaryTheme(String primaryTheme) {
	this.primaryTheme = primaryTheme;
    }

    public String getUnpublished() {
	return unpublished;
    }

    public void setUnpublished(String unpublished) {
	this.unpublished = unpublished;
    }

    public String getDatasetStartDate() {
	return datasetStartDate;
    }

    public void setDatasetStartDate(String datasetStartDate) {
	this.datasetStartDate = datasetStartDate;
    }

    public String getDatasetEndDate() {
	return datasetEndDate;
    }

    public void setDatasetEndDate(String datasetEndDate) {
	this.datasetEndDate = datasetEndDate;
    }

}
