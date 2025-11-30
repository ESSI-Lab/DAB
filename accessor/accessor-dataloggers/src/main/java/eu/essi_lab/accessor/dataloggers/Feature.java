package eu.essi_lab.accessor.dataloggers;

public class Feature {
    private String id;
    private String type;
    private FeatureProperties properties;
    private Geometry geometry;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FeatureProperties getProperties() {
        return properties;
    }

    public void setProperties(FeatureProperties properties) {
        this.properties = properties;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @Override
    public String toString() {
        return "Feature{" +
                "id='" + id + '\'' +
                ", date=" + (properties != null ? properties.getDate() : null) +
                ", value=" + (properties != null && properties.getAdditionalAttributes() != null && 
                              properties.getAdditionalAttributes().getMeasurement() != null ? 
                              properties.getAdditionalAttributes().getMeasurement().getValue() : null) +
                ", parameter=" + (properties != null && properties.getAdditionalAttributes() != null && 
                                  properties.getAdditionalAttributes().getMeasurement() != null ? 
                                  properties.getAdditionalAttributes().getMeasurement().getParameter() : null) +
                '}';
    }
}

