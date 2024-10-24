package eu.essi_lab.accessor.wof.stress;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.Date;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class HydroStressTest2 {
    public static void main(String[] args) throws Exception {
	long start = System.currentTimeMillis();
	System.out.println(ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(new Date(start)));
	String url = "http://gs-service-production.geodab.eu/gs-service/services/essi/view/gs-view-source(bolivia-bufr)/cuahsi_1_1.asmx?request=GetValuesObject&site=1DCD7EC960028D28EDC4DB5ACDFB398499611CFC&variable=F6D99CAFFD087370BE392945D05F2F998815023C&beginDate=2019-11-01T21:00:00Z&endDate=2019-11-06T21:00:00Z";

	Downloader executor = new Downloader();
	HttpResponse<InputStream> response = executor.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url));

	XMLDocumentReader reader = new XMLDocumentReader(response.body());
	System.out.println(reader.asString());
	long end = System.currentTimeMillis();
	System.out.println(ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(new Date(end)));
	System.out.println("Total time: " + (end - start) + "ms");
    }
}
