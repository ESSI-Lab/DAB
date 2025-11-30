package eu.essi_lab.accessor.dataloggers;

import java.time.OffsetDateTime;

public class FeatureProperties {
    private String id;
    private OffsetDateTime date;
    private AdditionalAttributes additionalAttributes;
    private Integer datastreamId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public AdditionalAttributes getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(AdditionalAttributes additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    public Integer getDatastreamId() {
        return datastreamId;
    }

    public void setDatastreamId(Integer datastreamId) {
        this.datastreamId = datastreamId;
    }
}

