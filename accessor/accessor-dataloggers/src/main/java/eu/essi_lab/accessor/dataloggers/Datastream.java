package eu.essi_lab.accessor.dataloggers;

import java.time.OffsetDateTime;

public class Datastream {
    private Integer uomId;
    private Integer varId;
    private String uomCod;
    private String varCod;
    private Integer dataloggerId;
    private Integer datastreamId;
    private String dataloggerCod;
    private String tipologiaRete;
    private Integer dataproviderId;
    private Integer datastreamStep;
    private String dataproviderCod;
    private String dataloggerLocation;
    private OffsetDateTime datastreamAvailableSince;
    private OffsetDateTime datastreamAvailableUntil;

    public Integer getUomId() {
        return uomId;
    }

    public void setUomId(Integer uomId) {
        this.uomId = uomId;
    }

    public Integer getVarId() {
        return varId;
    }

    public void setVarId(Integer varId) {
        this.varId = varId;
    }

    public String getUomCod() {
        return uomCod;
    }

    public void setUomCod(String uomCod) {
        this.uomCod = uomCod;
    }

    public String getVarCod() {
        return varCod;
    }

    public void setVarCod(String varCod) {
        this.varCod = varCod;
    }

    public Integer getDataloggerId() {
        return dataloggerId;
    }

    public void setDataloggerId(Integer dataloggerId) {
        this.dataloggerId = dataloggerId;
    }

    public Integer getDatastreamId() {
        return datastreamId;
    }

    public void setDatastreamId(Integer datastreamId) {
        this.datastreamId = datastreamId;
    }

    public String getDataloggerCod() {
        return dataloggerCod;
    }

    public void setDataloggerCod(String dataloggerCod) {
        this.dataloggerCod = dataloggerCod;
    }

    public String getTipologiaRete() {
        return tipologiaRete;
    }

    public void setTipologiaRete(String tipologiaRete) {
        this.tipologiaRete = tipologiaRete;
    }

    public Integer getDataproviderId() {
        return dataproviderId;
    }

    public void setDataproviderId(Integer dataproviderId) {
        this.dataproviderId = dataproviderId;
    }

    public Integer getDatastreamStep() {
        return datastreamStep;
    }

    public void setDatastreamStep(Integer datastreamStep) {
        this.datastreamStep = datastreamStep;
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

    public OffsetDateTime getDatastreamAvailableSince() {
        return datastreamAvailableSince;
    }

    public void setDatastreamAvailableSince(OffsetDateTime datastreamAvailableSince) {
        this.datastreamAvailableSince = datastreamAvailableSince;
    }

    public OffsetDateTime getDatastreamAvailableUntil() {
        return datastreamAvailableUntil;
    }

    public void setDatastreamAvailableUntil(OffsetDateTime datastreamAvailableUntil) {
        this.datastreamAvailableUntil = datastreamAvailableUntil;
    }

    @Override
    public String toString() {
        return "Datastream{" +
                "datastreamId=" + datastreamId +
                ", varCod='" + varCod + '\'' +
                ", uomCod='" + uomCod + '\'' +
                ", datastreamStep=" + datastreamStep +
                '}';
    }
}

