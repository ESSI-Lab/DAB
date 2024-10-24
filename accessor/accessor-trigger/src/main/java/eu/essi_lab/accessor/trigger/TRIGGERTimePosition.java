package eu.essi_lab.accessor.trigger;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.time.LocalDateTime;

public class TRIGGERTimePosition {
    
    private Double longitude;
    private Double latitude;
    private LocalDateTime dateTime;

    public TRIGGERTimePosition(Double longitude, Double latitude, LocalDateTime dateTime) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.dateTime = dateTime;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "TimePosition{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                ", dateTime=" + dateTime +
                '}';
    }

    public static void main(String[] args) {
        // Create a LocalDateTime object with year, month, day, hour, minute, and second components
        LocalDateTime dateTime = LocalDateTime.of(2024, 4, 22, 10, 15, 30);
        
        TRIGGERTimePosition position = new TRIGGERTimePosition(-73.9857, 40.7489, dateTime);
        System.out.println(position);
    }
}
