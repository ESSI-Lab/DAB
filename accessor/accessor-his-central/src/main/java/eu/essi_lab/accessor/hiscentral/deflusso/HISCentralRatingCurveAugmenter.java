package eu.essi_lab.accessor.hiscentral.deflusso;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.util.Collections;
import java.util.Optional;

import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;

/**
 * After harvesting a rating curve from SharePoint, looks up the corresponding HIS-Central station metadata in the
 * broker database (matching {@code sourceId} + {@code platformIdentifier}) and enriches the resource with the
 * station's unique platform id, platform identifier, platform title, bounding box and regional source id.
 *
 * @author boldrini
 */
public class HISCentralRatingCurveAugmenter extends ResourceAugmenter<AugmenterSetting> {

    private static final String HISCENTRAL_RATING_CURVE_AUGMENT_ERROR = "HISCENTRAL_RATING_CURVE_AUGMENT_ERROR";

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

        if (!resource.getPropertyHandler().isRatingCurve()) {
            return Optional.of(resource);
        }

        OriginalMetadata originalMD = resource.getOriginalMetadata();
        if (originalMD == null
                || !CommonNameSpaceContext.HISCENTRAL_RATING_CURVES_NS_URI.equals(originalMD.getSchemeURI())) {
            return Optional.of(resource);
        }

        String sourceId = HISCentralRatingCurvesMapper.readSourceId(originalMD);
        String stationId = HISCentralRatingCurvesMapper.readStationId(originalMD);

        if (sourceId == null || sourceId.isEmpty() || stationId == null || stationId.isEmpty()) {
            GSLoggerFactory.getLogger(getClass()).warn("Missing sourceId or stationId, skipping rating curve augmentation");
            return Optional.of(resource);
        }

        try {

            GSLoggerFactory.getLogger(getClass()).info("Augmenting rating curve source={} station={}", sourceId, stationId);

            GSSource lookupSource = ConfigurationWrapper.getSource(sourceId);
            if (lookupSource == null) {
                GSLoggerFactory.getLogger(getClass()).warn("Unknown source id {}, skipping rating curve augmentation", sourceId);
                return Optional.of(resource);
            }

            DatabaseFinder finder = DatabaseProviderFactory.getFinder(ConfigurationWrapper.getStorageInfo());

            Bond sourceBond = BondFactory.createSourceIdentifierBond(sourceId);
            Bond platformBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PLATFORM_IDENTIFIER,
                    stationId);
            LogicalBond andBond = BondFactory.createAndBond(sourceBond, platformBond);

            DiscoveryMessage message = new DiscoveryMessage();
            message.setSources(Collections.singletonList(lookupSource));
            message.setPermittedBond(andBond);
            message.setNormalizedBond(andBond);
            message.setUserBond(andBond);
            message.setPage(new Page(1, 1));
            message.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
            // Core metadata (including bbox) lives in the GS resource binary, not in index fields.
            message.setExcludeResourceBinary(false);

            ResourceSelector selector = message.getResourceSelector();
            selector.setIncludeOriginal(false);
            selector.setSubset(ResourceSelector.ResourceSubset.SOURCE_CORE);

            ResultSet<GSResource> resultSet = finder.discover(message);

            if (resultSet.getResultsList().isEmpty()) {
                GSLoggerFactory.getLogger(getClass()).warn("No station metadata found for source={} platform={}", sourceId,
                        stationId);
                return Optional.of(resource);
            }

            augmentFromMatch(resource, resultSet.getResultsList().get(0), sourceId, stationId);

            GSLoggerFactory.getLogger(getClass()).info("Rating curve augmentation completed for source={} station={}", sourceId,
                    stationId);

        } catch (Exception e) {

            throw GSException.createException(getClass(), HISCENTRAL_RATING_CURVE_AUGMENT_ERROR, e);
        }

        return Optional.of(resource);
    }

    private void augmentFromMatch(GSResource resource, GSResource matched, String sourceId, String stationId) {

        GeographicBoundingBox boundingBox = matched.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
        if (boundingBox != null && boundingBox.getNorth() != null && boundingBox.getEast() != null) {
            resource.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(//
                    boundingBox.getBigDecimalNorth(), //
                    boundingBox.getBigDecimalWest(), //
                    boundingBox.getBigDecimalSouth(), //
                    boundingBox.getBigDecimalEast());
        } else {
            GSLoggerFactory.getLogger(getClass()).warn(
                    "Matched station source={} platform={} has no core metadata bounding box", sourceId, stationId);
        }

        if (matched.getSource() != null && matched.getSource().getUniqueIdentifier() != null) {
            resource.setSource(matched.getSource());
        }

        copyPlatformMetadata(resource, matched);
        syncUniquePlatformIdentifier(resource, stationId);
    }

    private void copyPlatformMetadata(GSResource resource, GSResource matched) {

        MIPlatform matchedPlatform = getFirstPlatform(matched);
        if (matchedPlatform == null) {
            return;
        }

        MIPlatform platform = getOrCreatePlatform(resource);

        String platformId = matchedPlatform.getMDIdentifierCode();
        if (platformId != null && !platformId.isEmpty()) {
            platform.setMDIdentifierCode(platformId);
        }

        String description = matchedPlatform.getDescription();
        if (description != null && !description.isEmpty()) {
            platform.setDescription(description);
        }

        Citation matchedCitation = matchedPlatform.getCitation();
        if (matchedCitation != null) {
            String title = matchedCitation.getTitle();
            if (title != null && !title.isEmpty()) {
                Citation citation = platform.getCitation();
                if (citation == null) {
                    citation = new Citation();
                    platform.setCitation(citation);
                }
                citation.setTitle(title);
            }
        }
    }

    /**
     * Aligns {@link eu.essi_lab.model.resource.MetadataElement#UNIQUE_PLATFORM_IDENTIFIER} with the regional source and
     * platform id, same as {@link AbstractResourceMapper#handleUniqueIdentifiers}.
     */
    private void syncUniquePlatformIdentifier(GSResource resource, String stationId) {

        if (resource.getSource() == null || resource.getSource().getUniqueIdentifier() == null) {
            return;
        }

        String platformId = stationId;
        MIPlatform platform = getFirstPlatform(resource);
        if (platform != null) {
            String mdIdentifierCode = platform.getMDIdentifierCode();
            if (mdIdentifierCode != null && !mdIdentifierCode.isEmpty()) {
                platformId = mdIdentifierCode;
            }
        }

        resource.getExtensionHandler().setUniquePlatformIdentifier(
                AbstractResourceMapper.generateCode(resource.getSource().getUniqueIdentifier(), platformId));
    }

    private static MIPlatform getFirstPlatform(GSResource resource) {

        MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
        if (miMetadata == null) {
            return null;
        }
        return miMetadata.getMIPlatform();
    }

    private static MIPlatform getOrCreatePlatform(GSResource resource) {

        MIPlatform platform = getFirstPlatform(resource);
        if (platform != null) {
            return platform;
        }
        platform = new MIPlatform();
        resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addMIPlatform(platform);
        return platform;
    }

    @Override
    public String getType() {

        return "HISCentralRatingCurveAugmenter";
    }

    @Override
    protected AugmenterSetting initSetting() {

        return new AugmenterSetting();
    }

    @Override
    protected String initName() {

        return "HIS-Central Rating Curve augmenter";
    }
}
