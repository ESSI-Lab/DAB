package eu.essi_lab.accessor.dinaguaws.client;

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

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.model.resource.InterpolationType;

/**
 * @author Fabrizio
 */
public abstract class DinaguaClient {

    public static SimpleDateFormat DATE_TIME_SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat DATE_SDF = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat INVERTED_DATE_TIME_SDF = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    public static SimpleDateFormat YEAR_MONTH_SDF = new SimpleDateFormat("yyyy-MM");

    static {
	DATE_TIME_SDF.setTimeZone(TimeZone.getTimeZone(ZoneOffset.ofHours(-3)));
	DATE_SDF.setTimeZone(TimeZone.getTimeZone(ZoneOffset.ofHours(-3)));
	INVERTED_DATE_TIME_SDF.setTimeZone(TimeZone.getTimeZone(ZoneOffset.ofHours(-3)));
	YEAR_MONTH_SDF.setTimeZone(TimeZone.getTimeZone(ZoneOffset.ofHours(-3)));

    }

    /**
     * 
     */
    protected static ExpiringCache<DinaguaStation> stations = new ExpiringCache<>();
    static {
	stations.setDuration(TimeUnit.DAYS.toMillis(1));
    }
    
    protected static ExpiringCache<DinaguaStation> statusStations = new ExpiringCache<>();
    static {
	statusStations.setDuration(TimeUnit.DAYS.toMillis(1));
    }

    public InterpolationType[] interpolations = new InterpolationType[] { InterpolationType.CONTINUOUS, InterpolationType.AVERAGE,
	    InterpolationType.MAX, InterpolationType.MIN };

    public InterpolationType[] getInterpolations() {
	return interpolations;
    }

    /**
     * 
     */
    private String endpoint;
    private String user;
    private String password;

    /**
     * @param endpoint
     */
    public DinaguaClient(String endpoint) {

	this.endpoint = endpoint;
    }

    /**
     * @return the user
     */
    public Optional<String> getUser() {

	return Optional.ofNullable(user);
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
	this.user = user;
    }

    /**
     * @return the password
     */
    public Optional<String> getPassword() {

	return Optional.ofNullable(password);
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
	this.password = password;
    }

    /**
     * @return
     */
    public String getEndpoint() {

	return endpoint;
    }

    /**
     * @param identifier
     * @return
     * @throws Exception
     */
    public DinaguaStation getStation(String identifier) throws Exception {
	retrieveStations();
	return stations.get(identifier);
    }

    /**
     * @return
     * @throws Exception
     */
    public Set<String> getStationIdentifiers() throws Exception {
	retrieveStations();
	return stations.keySet();
    }

    /**
     * @return
     * @throws Exception
     */
    public Set<DinaguaStation> getStations() throws Exception {
	retrieveStations();
	Set<Entry<String, DinaguaStation>> entries = stations.entrySet();
	Set<DinaguaStation> ret = new HashSet<DinaguaStation>();
	for (Entry<String, DinaguaStation> entry : entries) {
	    ret.add(entry.getValue());
	}
	return ret;
    }
    
    public DinaguaStation getStatusStation(String stationCode) throws Exception {
	retrieveStatusStations();
	Set<Entry<String, DinaguaStation>> entries = statusStations.entrySet();
	DinaguaStation ret = null;
	for (Entry<String, DinaguaStation> entry : entries) {
	    if (entry.getValue().getId().equals(stationCode)) {
		return entry.getValue();
	    }
	}
	return ret;
    }
    
    public Set<DinaguaStation> getStatusStations() throws Exception {
	retrieveStatusStations();
	Set<Entry<String, DinaguaStation>> entries = statusStations.entrySet();
	Set<DinaguaStation> ret = new HashSet<DinaguaStation>();
	for (Entry<String, DinaguaStation> entry : entries) {
	    ret.add(entry.getValue());
	}
	return ret;
    }

    /**
     * @param stationId
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws Exception
     */
    public abstract DinaguaData getData(String stationId, String seriesCode, Date begin, Date end, InterpolationType interpolation) throws Exception;

    public abstract DinaguaData getStatusData(String stationId, String temporalidad, Date start, Date end) throws Exception;
    
    /**
     * @throws Exception
     */
    protected abstract void retrieveStations() throws Exception;
    
    protected abstract void retrieveStatusStations() throws Exception;

    

    


}
