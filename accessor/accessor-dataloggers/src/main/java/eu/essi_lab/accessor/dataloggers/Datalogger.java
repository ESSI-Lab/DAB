package eu.essi_lab.accessor.dataloggers;

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

import java.time.OffsetDateTime;
import java.util.List;

public class Datalogger {
    private Integer dataloggerId;
    private String dataloggerCod;
    private Integer dataproviderId;
    private String dataproviderCod;
    private String dataloggerLocation;
    private OffsetDateTime dataloggerAvailableSince;
    private OffsetDateTime dataloggerAvailableUntil;
    private List<Datastream> datastreams;

    public Integer getDataloggerId() {
        return dataloggerId;
    }

    public void setDataloggerId(Integer dataloggerId) {
        this.dataloggerId = dataloggerId;
    }

    public String getDataloggerCod() {
        return dataloggerCod;
    }

    public void setDataloggerCod(String dataloggerCod) {
        this.dataloggerCod = dataloggerCod;
    }

    public Integer getDataproviderId() {
        return dataproviderId;
    }

    public void setDataproviderId(Integer dataproviderId) {
        this.dataproviderId = dataproviderId;
    }

    public String getDataproviderCod() {
        return dataproviderCod;
    }

    public void setDataproviderCod(String dataproviderCod) {
        this.dataproviderCod = dataproviderCod;
    }

    public String getDataloggerLocation() {
        return dataloggerLocation;
    }

    public void setDataloggerLocation(String dataloggerLocation) {
        this.dataloggerLocation = dataloggerLocation;
    }

    public OffsetDateTime getDataloggerAvailableSince() {
        return dataloggerAvailableSince;
    }

    public void setDataloggerAvailableSince(OffsetDateTime dataloggerAvailableSince) {
        this.dataloggerAvailableSince = dataloggerAvailableSince;
    }

    public OffsetDateTime getDataloggerAvailableUntil() {
        return dataloggerAvailableUntil;
    }

    public void setDataloggerAvailableUntil(OffsetDateTime dataloggerAvailableUntil) {
        this.dataloggerAvailableUntil = dataloggerAvailableUntil;
    }

    public List<Datastream> getDatastreams() {
        return datastreams;
    }

    public void setDatastreams(List<Datastream> datastreams) {
        this.datastreams = datastreams;
    }

    @Override
    public String toString() {
        return "Datalogger{" +
                "dataloggerId=" + dataloggerId +
                ", dataloggerCod='" + dataloggerCod + '\'' +
                ", dataproviderCod='" + dataproviderCod + '\'' +
                ", dataloggerLocation='" + dataloggerLocation + '\'' +
                ", datastreamsCount=" + (datastreams != null ? datastreams.size() : 0) +
                '}';
    }
}

