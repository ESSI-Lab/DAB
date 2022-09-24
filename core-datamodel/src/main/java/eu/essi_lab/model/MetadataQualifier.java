package eu.essi_lab.model;

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

import eu.essi_lab.model.resource.HarmonizedMetadata;

/**
 * Computes the document quality of a given {@link HarmonizedMetadata}.<br> Implementation to be completed, in particular download quality
 * is missing
 *
 * @author Fabrizio
 */
public class MetadataQualifier {

    private static final int MAX_QUALITY = getMaxQuality();

    private static int wcsWeight = 10;

    private static int wmsWeight = 4;

    private static int wfsWeight = 2;

    private static int sosWeight = 1;

    private static int asWeight = 6000;

    private static int cdWeight = 3000;

    private static int gsWeight = 1500;

    private static int ulWeight = 1000;
    private HarmonizedMetadata md;

    private enum QualityIndicator {

	/**
	 * Category: Simple download Sub-category: Access Service Service : WCS
	 */
	AS_WCS(asWeight + wcsWeight),
	/**
	 * Category: Simple download Sub-category: Access Service Service : WFS
	 */
	AS_WFS(asWeight + wfsWeight),
	/**
	 * Category: Simple download Sub-category: Access Service Service : WMS
	 */
	AS_WMS(asWeight + wmsWeight),
	/**
	 * Category: Simple download Sub-category: Access Service Service : SOS
	 */
	AS_SOS(asWeight + sosWeight),

	/**
	 * Category: Simple download Sub-category: Generic Service
	 */
	GS(gsWeight),

	/**
	 * Category: Complex download Service : WCS
	 */
	CD_WCS(cdWeight + wcsWeight),
	/**
	 * Category: Complex download Service : WFS
	 */
	CD_WFS(cdWeight + wfsWeight),
	/**
	 * Category: Complex download Service : WMS
	 */
	CD_WMS(cdWeight + wmsWeight),
	/**
	 * Category: Complex download Service : SOS
	 */
	CD_SOS(cdWeight + sosWeight),
	/**
	 * Category: Complex download Service : GI-AXE
	 */
	CD_GIAXE(cdWeight),

	/**
	 * Category: Undefined link
	 */
	UL(ulWeight),

	/**
	 *
	 */
	GEOSS_DATA_CORE(200),
	/**
	 *
	 */
	ONLINE_RES(0),
	/**
	 *
	 */
	FILE_IDENTIFIER(50),
	/**
	 *
	 */
	ABSTRACT(50),
	/**
	 *
	 */
	ENVELOPE(30),
	/**
	 *
	 */
	TIME(20),
	/**
	 *
	 */
	TITLE(10);

	private int quality;

	private QualityIndicator(int quality) {
	    this.quality = quality;
	}

	public int getQuality() {
	    return quality;
	}
    }

    public MetadataQualifier(HarmonizedMetadata meta) {

	this.md = meta;
    }

    public int getQuality() {

	int quality = 0;

	if (isGeossDataCore()) {
	    quality += QualityIndicator.GEOSS_DATA_CORE.getQuality();
	}

	if (hasOnlineResource()) {
	    quality += QualityIndicator.ONLINE_RES.getQuality();
	}

	if (hasFileIdentifier()) {
	    quality += QualityIndicator.FILE_IDENTIFIER.getQuality();
	}

	if (hasAbstract()) {
	    quality += QualityIndicator.ABSTRACT.getQuality();
	}

	if (hasEnvelope()) {
	    quality += QualityIndicator.ENVELOPE.getQuality();
	}

	if (hasTime()) {
	    quality += QualityIndicator.TIME.getQuality();
	}

	if (hasTitle()) {
	    quality += QualityIndicator.TITLE.getQuality();
	}

	return normalizeQuality(quality);

    }

    public static int normalizeQuality(int quality) {

	return (int) Math.round(((double) quality / MAX_QUALITY) * 10.0);
    }

    private boolean hasTitle() {

	return md.getCoreMetadata().getTitle() != null;
    }

    private boolean hasTime() {

	return md.getCoreMetadata().getTemporalExtent() != null;
    }

    private boolean hasEnvelope() {

	return md.getCoreMetadata().getBoundingBox() != null;
    }

    private boolean hasAbstract() {

	return md.getCoreMetadata().getAbstract() != null;
    }

    private boolean hasFileIdentifier() {

	return md.getCoreMetadata().getIdentifier() != null;
    }

    private boolean hasOnlineResource() {

	return md.getCoreMetadata().getOnline() != null;
    }

    private boolean isGeossDataCore() {

	// XPathResult res = doc.evaluateXPath(
	// "exists(//gmd:identificationInfo//gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString[contains(lower-case(text()),'geossdatacore')])");
	// Boolean useLimit = res.asBoolean();
	//
	// res = doc.evaluateXPath(
	// "exists(//gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString[contains(lower-case(text()),'geossdatacore')])");
	// Boolean kwd = res.asBoolean();

	return false;
    }

    private static int getMaxQuality() {

	int maxq = QualityIndicator.AS_WCS.getQuality();
	maxq += QualityIndicator.AS_WMS.getQuality();
	maxq += QualityIndicator.AS_WFS.getQuality();
	maxq += QualityIndicator.AS_SOS.getQuality();
	maxq += QualityIndicator.GS.getQuality();
	maxq += QualityIndicator.CD_WCS.getQuality();
	maxq += QualityIndicator.CD_WMS.getQuality();
	maxq += QualityIndicator.CD_WFS.getQuality();
	maxq += QualityIndicator.CD_SOS.getQuality();
	maxq += QualityIndicator.CD_GIAXE.getQuality();
	maxq += QualityIndicator.UL.getQuality();
	maxq += QualityIndicator.GEOSS_DATA_CORE.getQuality();
	maxq += QualityIndicator.ONLINE_RES.getQuality();
	maxq += QualityIndicator.FILE_IDENTIFIER.getQuality();
	maxq += QualityIndicator.ABSTRACT.getQuality();
	maxq += QualityIndicator.ENVELOPE.getQuality();
	maxq += QualityIndicator.TIME.getQuality();
	maxq += QualityIndicator.TITLE.getQuality();

	return maxq;
    }

}
