package eu.essi_lab.accessor.canada;

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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.TreeSet;

import org.slf4j.Logger;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class ECStation {

    String stationCode; // e.g. 01AP003

    String name;
    String lat;
    String lon;
    String prov; // e.g. NB
    String timezone; // e.g. UTC-04:00
    Long resolutionMs;

    String startDate;

    List<String> values = new ArrayList<String>();

    private Downloader downloader;

    public Downloader getDownloader() {
	return downloader == null ? new Downloader() : downloader;
    }

    public void setDownloader(Downloader downloader) {
	this.downloader = downloader;
    }

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public boolean hasValues() {
	return !values.isEmpty();
    }

    private Calendar[] estimatedFirstDate = new Calendar[10];

    private Calendar[] estimatedLastDate = new Calendar[10];

    public Calendar getMoreAccurateEstimatedFirstDate(int column) {
	if (hasValues() && estimatedFirstDate[column] == null) {
	    for (String value : values) {
		if (value.contains("hourly")) {

		    try {
			Optional<InputStream> is = getDownloader().downloadOptionalStream(value);

			if (is.isPresent()) {
			    BufferedReader bfReader = null;

			    bfReader = new BufferedReader(new InputStreamReader(is.get()));
			    String temp = null;
			    bfReader.readLine(); // skip header line
			    Calendar firstDate = null;
			    Calendar lastDate = null;
			    while ((temp = bfReader.readLine()) != null) {

				if (temp != null && !temp.equals("")) {
				    String[] split = temp.split(",");
				    String stationCode = split[0];
				    String time = split[1];
				    if (column < (split.length)) {
					String v = split[column];
					if (v != null && !v.equals("")) {
					    try {

						Date parsed = ISO8601DateTimeUtils.parseISO8601(time);

						Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

						calendar.setTime(parsed);

						if (firstDate == null || firstDate.after(calendar)) {
						    firstDate = calendar;
						}
						if (lastDate == null || lastDate.before(calendar)) {
						    lastDate = calendar;
						}

					    } catch (Exception e) {
						e.printStackTrace();
					    }
					}
				    }
				}
			    }

			    if (firstDate != null) {
				firstDate.add(Calendar.DATE, -29);
			    }

			    estimatedFirstDate[column] = firstDate;
			    estimatedLastDate[column] = lastDate;
			}

		    } catch (Exception e) {
			e.printStackTrace();
		    }

		}
	    }
	}
	return estimatedFirstDate[column];
    }

    public Calendar getEstimatedFirstDate(int column) {
	if (estimatedFirstDate[column] == null) {

	    try {
		Integer ec = getEstimatedCount();

		Calendar lastDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

		Calendar firstDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

		if (firstDate != null) {
		    firstDate.add(Calendar.DATE, -(ec / 290));
		}

		estimatedFirstDate[column] = firstDate;
		estimatedLastDate[column] = lastDate;

	    } catch (Exception e) {
		e.printStackTrace();
	    }

	}
	return estimatedFirstDate[column];
    }

    public Calendar getEstimatedLastDate(int column) {
	return estimatedLastDate[column];
    }

    public List<String> getValues() {
	return values;
    }

    public Calendar getWaterLevelEstimatedFirstDate() {
	return getEstimatedFirstDate(2);
    }

    public Calendar getWaterLevelEstimatedLastDate() {
	return getEstimatedLastDate(2);
    }

    public Calendar getDischargeEstimatedFirstDate() {
	return getEstimatedFirstDate(6);
    }

    public Calendar getDischargeEstimatedLastDate() {
	return getEstimatedLastDate(6);
    }

    public Measurement getFirstDischargeMeasurement() {
	TreeSet<Measurement> ret = getDischargeMeasurements();
	return ret.first();
    }

    public Measurement getLastDischargeMeasurement() {
	TreeSet<Measurement> ret = getDischargeMeasurements();
	return ret.last();
    }

    public TreeSet<Measurement> getDischargeMeasurements() {
	return getMeasurements(6);
    }

    public int getDischargeMeasurementCount() {
	return getDischargeMeasurements().size();
    }

    public Measurement getFirstWaterLevelMeasurement() {
	TreeSet<Measurement> ret = getWaterLevelMeasurements();
	return ret.first();
    }

    public Measurement getLastWaterLevelMeasurement() {
	TreeSet<Measurement> ret = getWaterLevelMeasurements();
	return ret.last();
    }

    public int getWaterLevelMeasurementCount() {
	return getWaterLevelMeasurements().size();
    }

    public TreeSet<Measurement> getWaterLevelMeasurements() {
	return getMeasurements(2);
    }

    public Measurement getFirstMeasurement(int column) {
	return getMeasurements(column).first();
    }

    public Measurement getLastMeasurement(int column) {
	return getMeasurements(column).last();
    }

    public int getMeasurementCount(int column) {
	return getMeasurements(column).size();
    }

    public TreeSet<Measurement> getMeasurements(int column) {
	TreeSet<Measurement> ret = new TreeSet<Measurement>();
	for (String value : values) {
	    try {
		Optional<InputStream> is = getDownloader().downloadOptionalStream(value);

		if (is.isPresent()) {
		    BufferedReader bfReader = null;

		    bfReader = new BufferedReader(new InputStreamReader(is.get()));
		    String temp = null;
		    bfReader.readLine(); // skip header line
		    while ((temp = bfReader.readLine()) != null) {

			if (temp != null && !temp.equals("")) {
			    String[] split = temp.split(",");
			    String stationCode = split[0];
			    String time = split[1];
			    if (column < (split.length)) {
				String v = split[column];
				if (v != null && !v.equals("")) {
				    try {
					Measurement m = new Measurement();
					m.setDate(ISO8601DateTimeUtils.parseISO8601(time));
					m.setValue(Double.parseDouble(v));
					ret.add(m);
				    } catch (Exception e) {
					e.printStackTrace();
				    }
				}
			    }
			}
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return ret;
    }

    public ECStation() {

    }

    public String getStationCode() {
	return stationCode;
    }

    public void setStationCode(String stationCode) {
	this.stationCode = stationCode;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getLat() {
	return lat;
    }

    public void setLat(String lat) {
	this.lat = lat;
    }

    public String getLon() {
	return lon;
    }

    public void setLon(String lon) {
	this.lon = lon;
    }

    public Long getResolutionMs() {
	return resolutionMs;
    }

    public void setResolutionMs(Long resolutionMs) {
	this.resolutionMs = resolutionMs;
    }

    public String getProvince() {
	return prov;
    }

    public void setProv(String prov) {
	this.prov = prov;
    }

    public String getTimezone() {
	return timezone;
    }

    public void setTimezone(String timezone) {
	this.timezone = timezone;
    }

    @Override
    public String toString() {
	return this.name;
    }

    private Integer estimatedCount = null;

    public Integer getEstimatedCount() {
	if (estimatedCount == null) {
	    List<String> values = getValues();
	    double totalSize = 0;
	    for (String value : values) {
		logger.info("Checking URL stream size: " + value);
		Optional<HttpResponse<InputStream>> response = getDownloader().downloadOptionalResponse(value);
		String cl = "0";
		if (response.isPresent()) {

		    HttpHeaders headers = response.get().headers();
		    if (headers.firstValue("Content-Length").isPresent()) {

			cl = headers.firstValue("Content-Length").get();
		    }
		}
		Integer cli = Integer.parseInt(cl);
		totalSize += cli;
	    }

	    estimatedCount = (int) (totalSize / 56.6);

	}
	return estimatedCount;
    }

    public static void main(String[] args) {
	Downloader downloader = new Downloader();
	Optional<HttpResponse<InputStream>> response = downloader
		.downloadOptionalResponse("http://dd.weather.gc.ca/hydrometric/csv/BC/daily/BC_08HD018_daily_hydrometric.csv");
	String cl = "";
	if (response.isPresent()) {
	    HttpHeaders header = response.get().headers();

	    if (header.firstValue("Content-Length").isPresent()) {

		cl = header.firstValue("Content-Length").get();
	    }
	}
	Integer cli = Integer.parseInt(cl);
	System.out.println(cl);

	System.out.println("HEADERS");
    }

    public String getStartDate() {
	return startDate;
    }

    public void setStartDate(String startDate) {
	this.startDate = startDate;
    }

}
