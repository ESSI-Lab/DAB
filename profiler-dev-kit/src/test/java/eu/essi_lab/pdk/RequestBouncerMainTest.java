package eu.essi_lab.pdk;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.TaskListExecutor;

public class RequestBouncerMainTest {
    public static void main(String[] args) {
	int max = 2;
	long start = System.currentTimeMillis();
	TaskListExecutor<Boolean> tle = new TaskListExecutor<>(max);
	for (int i = 0; i < max; i++) {
	    tle.addTask(new Callable<Boolean>() {

		@Override
		public Boolean call() throws Exception {
		    String correct = "";
		    Downloader downloader = new Downloader();
		    // String url =
		    // "http://localhost:9090/gs-service/services/essi/view/whos-plata/cuahsi_1_1.asmx?request=GetSiteInfo&site=0AA051730821BB74ABF93AA6C276D09A86663A09";
		    // String url =
		    // "http://localhost:9090/gs-service/services/essi/view/gs-view-source(bolivia-bufr)/cuahsi_1_1.asmx?request=GetValuesObject&site=05667427AC57D8EB10D8841B82A571BD831EDA5D&variable=01543B74A2C5E673D8AC398142F681477282CCF3&beginDate=2019-11-05T12:00:00Z&endDate=2019-12-05T12:00:00Z";
		    // String url =
		    // "http://gs-service-production.geodab.eu/gs-service/services/essi/view/gs-view-source(bolivia-bufr)/cuahsi_1_1.asmx?request=GetValuesObject&site=05667427AC57D8EB10D8841B82A571BD831EDA5D&variable=01543B74A2C5E673D8AC398142F681477282CCF3&beginDate=2019-11-05T12:00:00Z&endDate=2019-12-05T12:00:00Z";
		    // String url =
		    // "http://localhost:9090/gs-service/services/essi/view/whos-plata/cuahsi_1_1.asmx?request=GetValuesObject&site=A51A84E1BF37705F99AFF0761B0556F3F2BE1F0D&variable=13C613D77FED7D5863A85AEF47BB9FF710C698E8&beginDate=2020-01-01T12:00:00Z&endDate=2020-06-05T12:00:00Z";

		    // brazil-ana Correct: 10/10 Elapsed time: 26063 ms
		    // String url =
		    // "http://gs-service-production.geodab.eu/gs-service/services/essi/view/whos-plata/cuahsi_1_1.asmx?request=GetValuesObject&site=000BE3CB65DF1F7D571EF045272E048FCD0072BD&variable=80B052462E277E0F7D3002CA6C67E481CD953CDC&beginDate=2020-05-15T00:00:00Z&endDate=2020-05-20T00:00:00Z";

		    // brazil-ana-sar 10/10 Correct: 10/10 Elapsed time: 15700 ms
		    // String url =
		    // "http://gs-service-production.geodab.eu/gs-service/services/essi/view/whos-plata/cuahsi_1_1.asmx?request=GetValuesObject&site=0216E67FF34D5078A689FEB9981D3A9D15ABA585&variable=4847D38D5EC11F3A87B3821604DDC622DF6FB5CB&beginDate=2003-01-03T00:00:00Z&endDate=2003-03-03T00:00:00Z";

		    // brazil-inmet Correct: 10/10 Elapsed time: 52870 ms
		    // String url =
		    // "http://gs-service-production.geodab.eu/gs-service/services/essi/view/whos-plata/cuahsi_1_1.asmx?request=GetValuesObject&site=A51A84E1BF37705F99AFF0761B0556F3F2BE1F0D&variable=13C613D77FED7D5863A85AEF47BB9FF710C698E8&beginDate=2020-01-01T12:00:00Z&endDate=2020-06-05T12:00:00Z";

		    // uruguay-dinagua
		    // String url =
		    // "http://gs-service-production.geodab.eu/gs-service/services/essi/view/whos-plata/cuahsi_1_1.asmx?request=GetValuesObject&site=05316F01469AC6FF83F927B1D3602F1BDD1B83F8&variable=F7B4831EA151DB33AA187F9C4248506E0CE6690C&beginDate=2020-05-15T00:00:00Z&endDate=2020-05-17T00:00:00Z";

		    // PRODUCTION

		    // argentina-ina get values old implementation: Correct: 10/10 Elapsed time: 10086 ms (production) 73339 ms preprod before
		    // POST: Correct: 10/10  Elapsed time: 25142 ms
//		     correct = "<values>";
		     correct = "termFrequency";
		     String url =
//			     "http://gs-service-preproduction.geodab.eu/gs-service/services/essi/view/whos-plata/cuahsi_1_1.asmx?request=GetValuesObject&site=0631B7B04F01E90D4B4EBCC982CCDF085789D708&variable=E02E2A436A24C1DB98D819A4705B8089856A9579&beginDate=2019-08-20T12:00:00Z&endDate=2019-08-23T12:00:00Z";
//		     "http://gs-service-production.geodab.eu/gs-service/services/essi/view/whos-plata/cuahsi_1_1.asmx?request=GetValuesObject&site=0631B7B04F01E90D4B4EBCC982CCDF085789D708&variable=E02E2A436A24C1DB98D819A4705B8089856A9579&beginDate=2019-08-20T12:00:00Z&endDate=2019-08-23T12:00:00Z";
//			     "http://gs-service-preproduction.geodab.eu/gs-service/services/essi/view/whos-plata/cuahsi_1_1.asmx?request=GetValuesObject&site=0631B7B04F01E90D4B4EBCC982CCDF085789D708&variable=E02E2A436A24C1DB98D819A4705B8089856A9579&beginDate=2019-08-20T12:00:00Z&endDate=2019-08-23T12:00:00Z";
//			     "http://localhost:9090/gs-service/services/essi/view/whos-plata/cuahsi_1_1.asmx?request=GetValuesObject&site=0631B7B04F01E90D4B4EBCC982CCDF085789D708&variable=E02E2A436A24C1DB98D819A4705B8089856A9579&beginDate=2019-08-20T12:00:00Z&endDate=2019-08-23T12:00:00Z";

			     "http://gs-service-preproduction.geodab.eu/gs-service/services/essi/view/whos-plata/opensearch/query?si=1&ct=10&st=&kwd=&frmt=&prot=&kwdOrBbox=&sscScore=&instrumentTitle=&platformTitle=&attributeTitle=&organisationName=&searchFields=&bbox=-60.775,-39.609,-30.775,-8.892&rel=CONTAINS&tf=providerID,keyword,format,protocol&ts=&te=&targetId=&from=&until=&subj=&rela=&token=null&outputFormat=application/json&callback=jQuery11130162830668253942_1707472250570&_=1707472250573";
//		     "http://localhost:9090/gs-service/services/essi/view/whos-plata/opensearch/query?si=1&ct=10&st=&kwd=&frmt=&prot=&kwdOrBbox=&sscScore=&instrumentTitle=&platformTitle=&attributeTitle=&organisationName=&searchFields=&bbox=-60.775,-39.609,-30.775,-8.892&rel=CONTAINS&tf=providerID,keyword,format,protocol&ts=&te=&targetId=&from=&until=&subj=&rela=&token=null&outputFormat=application/json&callback=jQuery11130162830668253942_1707472250570&_=1707472250573";
//=======
			   			     
//			     "http://localhost:9090/gs-service/services/essi/view/geoss/opensearch/query?si=1&ct=1000&parents=ROOT";

		     
//		     "http://gs-service-preproduction.geodab.eu/gs-service/services/essi/view/whos-plata/opensearch/query?si=1&ct=10&st=&kwd=&frmt=&prot=&kwdOrBbox=&sscScore=&instrumentTitle=&platformTitle=&attributeTitle=&organisationName=&searchFields=&bbox=-60.775,-39.609,-30.775,-8.892&rel=CONTAINS&tf=providerID,keyword,format,protocol&ts=&te=&targetId=&from=&until=&subj=&rela=&token=null&outputFormat=application/json&callback=jQuery11130162830668253942_1707472250570&_=1707472250573";
//		     "http://localhost:9090/gs-service/services/essi/view/whos-plata/opensearch/query?si=1&ct=10&st=&kwd=&frmt=&prot=&kwdOrBbox=&sscScore=&instrumentTitle=&platformTitle=&attributeTitle=&organisationName=&searchFields=&bbox=-60.775,-39.609,-30.775,-8.892&rel=CONTAINS&tf=providerID,keyword,format,protocol&ts=&te=&targetId=&from=&until=&subj=&rela=&token=null&outputFormat=application/json&callback=jQuery11130162830668253942_1707472250570&_=1707472250573";

				 
		    // root request old implementation: Correct: 10/10 Elapsed time: 1548 ms
		    // POST: Correct: 10/10  Elapsed time: 4244 ms
//		    String url = "http://gs-service-production.geodab.eu/gs-service/services/essi/view/geoss/opensearch/query?si=1&ct=1000&parents=ROOT";
//		    correct = "UNESCO-IHP Water";

		    // OS query old implementation: Correct: 10/10 Elapsed time: 7006 ms
		    // POST: Correct: 10/10 Elapsed time: 20102 ms
		    // String url =
		    // "http://whos.geodab.eu/gs-service/services/essi/view/whos-plata/opensearch/query?si=1&ct=10&st=&kwd=&frmt=&prot=&kwdOrBbox=&sscScore=&instrumentTitle=&platformTitle=&attributeTitle=&organisationName=&searchFields=&bbox=-60.775,-39.609,-30.775,-8.892&rel=CONTAINS&tf=providerID,keyword,format,protocol&ts=&te=&targetId=&from=&until=&subj=&rela=&token=null&outputFormat=application/json&callback=jQuery11130162830668253942_1707472250570&_=1707472250573";
		    // correct = "resultSet";
		    //

		    boolean localhost = false;
		    if (localhost) {
			url = url.replace("gs-service-production.geodab.eu", "localhost:9090");
		    }

		    System.out.println("GET");
		    String str = downloader.downloadOptionalString(url).get();
		    System.out.println(str);

		    if (str.contains(correct

		    //
		    //
		    )) {
			return true;
		    }
		    // System.out.println("CODE: "+code.get());
		    return false;
		}
	    });
	}
	List<Future<Boolean>> futures = tle.executeAndWait();
	int correct = 0;
	for (Future<Boolean> future : futures) {
	    try {
		if (future.get()) {
		    correct++;
		}
	    } catch (InterruptedException | ExecutionException e) {
		e.printStackTrace();
	    }
	}

	long end = System.currentTimeMillis();
	System.out.println("Correct: " + correct + "/" + max + "  Elapsed time: " + (end - start) + " ms");
    }
}
