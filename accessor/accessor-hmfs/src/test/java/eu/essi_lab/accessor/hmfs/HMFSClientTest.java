package eu.essi_lab.accessor.hmfs;

import java.util.List;

public class HMFSClientTest {
    public static void main(String[] args) throws Exception {
	HMFSClient client = new HMFSClient();
	String stationCode;
//	stationCode = "1005704398";
	stationCode = "1005802104";

	List<HMFSSeries> series = client.getSeries(stationCode);

	for (HMFSSeries serie : series) {
	    HMFSVariable v = serie.getVariable();
	    String lastDate = client.getLastForecast(stationCode, "" + serie.getId(), "" + v.getId());
//	    List<HMFSSeriesInformation> infos = client.getSeriesInformation(stationCode, "" + serie.getId(), "" + v.getId(), lastDate);
//	    if (infos == null) {
//		System.out.println("0 " + v.getName() + " " + serie.getId());
//	    } else {
//		System.out.println(infos.size() + " " + v.getName() + " "+v.getId()+" " + serie.getId()+ " "+lastDate+" "+infos.get(0).getBeginDate()+" "+infos.get(0).getEndDate());
//	    }

	}
    }
}
