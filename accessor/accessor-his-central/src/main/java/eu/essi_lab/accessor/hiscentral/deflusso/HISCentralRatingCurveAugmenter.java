package eu.essi_lab.accessor.hiscentral.deflusso;

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

import java.util.Optional;

import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * After harvesting a rating curve from SharePoint, looks up the corresponding HIS-Central station metadata in the
 * broker database (matching {@code sourceId} + {@code platformIdentifier}) and enriches the resource with the
 * station's unique platform id, bounding box and regional source id.
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

            DatabaseFinder finder = DatabaseProviderFactory.getFinder(ConfigurationWrapper.getStorageInfo());

            Bond sourceBond = BondFactory.createSourceIdentifierBond(sourceId);
            Bond platformBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PLATFORM_IDENTIFIER,
                    stationId);
            LogicalBond andBond = BondFactory.createAndBond(sourceBond, platformBond);

            DiscoveryMessage message = new DiscoveryMessage();
            message.setPermittedBond(andBond);
            message.setNormalizedBond(andBond);
            message.setUserBond(andBond);
            message.setPage(new Page(1, 1));
            message.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
            message.setExcludeResourceBinary(true);

            ResultSet<GSResource> resultSet = finder.discover(message);

            if (resultSet.getResultsList().isEmpty()) {
                GSLoggerFactory.getLogger(getClass()).warn("No station metadata found for source={} platform={}", sourceId,
                        stationId);
                return Optional.of(resource);
            }

            augmentFromMatch(resource, resultSet.getResultsList().get(0), sourceId);

            GSLoggerFactory.getLogger(getClass()).info("Rating curve augmentation completed for source={} station={}", sourceId,
                    stationId);

        } catch (Exception e) {

            throw GSException.createException(getClass(), HISCENTRAL_RATING_CURVE_AUGMENT_ERROR, e);
        }

        return Optional.of(resource);
    }

    private void augmentFromMatch(GSResource resource, GSResource matched, String sourceId) {

        matched.getExtensionHandler().getUniquePlatformIdentifier()
                .ifPresent(id -> resource.getExtensionHandler().setUniquePlatformIdentifier(id));

        GeographicBoundingBox boundingBox = matched.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
        if (boundingBox != null) {
            resource.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(//
                    boundingBox.getBigDecimalNorth(), //
                    boundingBox.getBigDecimalWest(), //
                    boundingBox.getBigDecimalSouth(), //
                    boundingBox.getBigDecimalEast());
        }

        if (matched.getSource() != null && matched.getSource().getUniqueIdentifier() != null) {
            resource.setSource(matched.getSource());
        }
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
