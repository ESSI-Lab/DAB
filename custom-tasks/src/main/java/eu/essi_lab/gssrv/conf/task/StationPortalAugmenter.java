package eu.essi_lab.gssrv.conf.task;

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

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

public class StationPortalAugmenter extends ResourceAugmenter<AugmenterSetting> {

    private String viewId = "";

    public StationPortalAugmenter() {

    }

    public StationPortalAugmenter(AugmenterSetting setting) {

	super(setting);
    }

    @Override
    public String getType() {
	return "StationPortalAugmenter";
    }

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {
	try {
	    Optional<String> optionalPlatformIdentifier = resource.getExtensionHandler().getUniquePlatformIdentifier();

	    if (optionalPlatformIdentifier.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).error("Not a station");
		return Optional.empty();
	    }

	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.MINUTES, 2);
	    String baseUrl = "https://gs-service-production.geodab.eu/";
	    String url = baseUrl + "gs-service/services/view/" + viewId + "/bnhs/station/" + optionalPlatformIdentifier.get()
		    + "/timeseries";
	    Optional<String> str = downloader.downloadOptionalString(url);
	    if (str.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).error("Unable to download timeseries");
		resource.getPropertyHandler().setLastFailedDownloadDate();
		return Optional.of(resource);
	    }

	    JSONArray array = new JSONArray(str.get());
	    if (array.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).error("No timeseries available");
		resource.getPropertyHandler().setLastFailedDownloadDate();
		return Optional.of(resource);
	    }
	    JSONObject series = array.getJSONObject(0);
	    String seriesIdentifier = series.getJSONObject("attribute_id").getString("value");
	    String endTime = series.getJSONObject("time_end").getString("value");
	    Optional<Date> endDate = ISO8601DateTimeUtils.parseISO8601ToDate(endTime);
	    if (endDate.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).error("No end date for this series");
		endDate = Optional.of(new Date());
		endTime = ISO8601DateTimeUtils.getISO8601DateTime(endDate.get());
	    }

	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(endDate.get());
	    calendar.add(Calendar.MONTH, -1);
	    Date startDate = calendar.getTime();
	    String startTime = ISO8601DateTimeUtils.getISO8601DateTime(startDate);
	    String dataUrl = baseUrl + "gs-service/services/view/" + viewId + "/gwis/iv/?format=rdb,1.0&startDT=" + startTime + "&endDT=" + endTime + "&sites="
		    + optionalPlatformIdentifier.get() + "&parmCd=" + seriesIdentifier;

	    Optional<String> data = downloader.downloadOptionalString(dataUrl);
	    if (data.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).error("Unable to download data");
		resource.getPropertyHandler().setLastFailedDownloadDate();
		return Optional.of(resource);
	    }
	    String dataRow = optionalPlatformIdentifier.get(); // each row has this info
	    if (data.get().contains(dataRow)) {
		// success
		resource.getPropertyHandler().setLastDownloadDate();
		return Optional.of(resource);
	    } else {
		// failure.. no data
		GSLoggerFactory.getLogger(getClass()).error("No data available in the last two months: check this");

		resource.getPropertyHandler().setLastFailedDownloadDate();
		return Optional.of(resource);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error("not handled exception");

	    resource.getPropertyHandler().setLastFailedDownloadDate();
	    return Optional.of(resource);
	}

    }

    @Override
    protected String initName() {
	return "Station portal augmenter";
    }

    @Override
    protected AugmenterSetting initSetting() {
	return new AugmenterSetting();
    }

    public void setView(String viewId) {
	this.viewId = viewId;

    }

}
