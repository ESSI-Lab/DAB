package eu.essi_lab.model.operation;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.resource.AugmentedMetadataElement;
import eu.essi_lab.model.resource.HarmonizedMetadata;

public class HarmonizedMetadataLastUpdateTime implements HarmonizedMetadataOperation {

    private List<AugmentedMetadataElement> augmentedMetadataElements;

    @Override
    public void perform(HarmonizedMetadata harmonizedMetadata) {
	this.augmentedMetadataElements = harmonizedMetadata.getAugmentedMetadataElements();

    }

    public Date getLastUpdateTime() throws ParseException {
	Date lastUpdateTime = null;
	for (AugmentedMetadataElement element : augmentedMetadataElements) {
	    if (lastUpdateTime == null || lastUpdateTime.getTime() < ISO8601DateTimeUtils.parseISO8601(element.getUpdateTimeStamp()).getTime())
		lastUpdateTime = ISO8601DateTimeUtils.parseISO8601(element.getUpdateTimeStamp());
	}
	return lastUpdateTime;
    }
}
