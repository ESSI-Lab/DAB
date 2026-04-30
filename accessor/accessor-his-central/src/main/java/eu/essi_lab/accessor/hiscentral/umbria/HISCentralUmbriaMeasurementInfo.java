package eu.essi_lab.accessor.hiscentral.umbria;

public class HISCentralUmbriaMeasurementInfo {

    private String type;
    private String date;
    private String resourceId;


     public HISCentralUmbriaMeasurementInfo(String type, String date, String resourceId){
	 this.type = type;
        this.date = date;
        this.resourceId = resourceId;
     }

    public String getDate() {
	return date;
    }

    public void setDate(String date) {
	 this.date = date;
    }

    public String getResourceId() {
	 return resourceId;
    }

    public void setResourceId(String resourceId) {
	 this.resourceId = resourceId;
    }

    public String getType() {
	 return type;
    }

    public void setType(String type) {
	 this.type = type;
    }
}
