package eu.essi_lab.accessor.rarcticnet;

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

import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.JSONObjectWrapper;

/**
 * An R-ArcticNet time series: the discharge of a gauge (site) in one of the datasets (monthly or daily), described by the row of
 * the gauge in the site attributes table of the dataset, completed with the extent of the values
 *
 * @author boldrini
 */
public class RArcticNetEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public RArcticNetEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public RArcticNetEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "5855|MONTHLY"
     */
    public String getSeriesId() {

	return getObject().optString(RArcticNetClient.SERIES_ID, null);
    }

    /**
     * @return the identification number of the gauge, unique to R-ArcticNet, e.g. "5855"
     */
    public String getPointId() {

	return getObject().optString(RArcticNetClient.POINT_ID, null);
    }

    /**
     * @return the PointID as a number, for sorting
     */
    public long getPointIdNumber() {

	try {
	    return Long.parseLong(getPointId());
	} catch (NumberFormatException e) {
	    return Long.MAX_VALUE;
	}
    }

    /**
     * @return the code of the gauge assigned by the source agency, e.g. "1001", or an empty string if not set
     */
    public String getCode() {

	return getString(RArcticNetClient.CODE);
    }

    /**
     * @return
     */
    public String getName() {

	String name = getString(RArcticNetClient.NAME);
	return name.isEmpty() ? getPointId() : name;
    }

    /**
     * @return
     */
    public Optional<RArcticNetDataset> getDataset() {

	return RArcticNetDataset.decode(getObject().optString(RArcticNetClient.DATASET, null));
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
