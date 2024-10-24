package eu.essi_lab.thredds;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.lib.net.downloader.Downloader;

public class THREDDSTest {

    long min1 = 0;
    long min2 = 0;
    long max1 = 0;
    long max2 = 0;
    long avg1 = 0;
    long avg2 = 0;
    Downloader d = new Downloader();

    public THREDDSTest() throws Exception {

	int a = 4;
	int b = 5000;
	int c = 5000;
	String endpoint = "https://thredds.geodab.eu";
	String endpoint2 = "https://thredds-test.geodab.eu";

	long tries = 200;

	Stats thredds = new Stats();
	Stats threddsTest = new Stats();
	for (int i = 0; i < tries; i++) {

	    long s = 1 + (long) (Math.random() * 3);

	    long time = (long) (Math.random() * a);
	    long latMin = (long) (Math.random() * b);
	    long latMax = Math.min(b, latMin + 10 * s);
	    long lonMin = (long) (Math.random() * c);
	    long lonMax = Math.min(c, lonMin + 10 * s);

	    long n = 1 + (long) (Math.random() * 98);

	    String parameters = "/thredds/dodsC/data/all/sample.nc" + n + ".ascii?pres[" + time + ":1:" + time + "][" + latMin + ":" + s
		    + ":" + latMax + "][" + lonMin + ":" + s + ":" + lonMax + "]";

	    if (Math.random() < 0.5) {
		addValue(thredds, endpoint, parameters);
		addValue(threddsTest, endpoint2, parameters);
	    } else {
		addValue(threddsTest, endpoint2, parameters);
		addValue(thredds, endpoint, parameters);
	    }

	    thredds.print();
	    threddsTest.print();

	    long gap = thredds.getAverage() - threddsTest.getAverage();
	    double percent = ((double) gap) / thredds.getAverage();
	    System.out.println(percent * 100);

	}

    }

    private void addValue(Stats thredds, String endpoint, String parameters) throws IOException, URISyntaxException, InterruptedException {
	String url = endpoint + parameters;
	long start = System.currentTimeMillis();
	HttpResponse<InputStream> response = d.downloadResponse(url);
	Integer code = response.statusCode();
	if (code != 200) {
	    System.out.println("Exit code: " + code);
	    System.exit(1);
	}
	OutputStream out = new OutputStream() {
	    @Override
	    public void write(int b) throws IOException {

	    }
	};
	InputStream stream = response.body();
	IOUtils.copy(stream, System.out);
	// IOUtils.copy(stream, out);
	stream.close();
	long end = System.currentTimeMillis();
	thredds.addValue(end - start);

    }

    public static void main(String[] args) throws Exception {
	new THREDDSTest();
    }
}
