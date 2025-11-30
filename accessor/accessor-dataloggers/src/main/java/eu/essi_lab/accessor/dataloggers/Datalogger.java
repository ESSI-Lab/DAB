package eu.essi_lab.accessor.dataloggers;

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

