package eu.essi_lab.accessor.sos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Node;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class SOSBOMTest {
	public SOSBOMTest() throws Exception {
		String base = "http://www.bom.gov.au/waterdata/services?";
		File root = new File("/home/boldrini/aus/");
		XMLDocumentReader reader = download(new File(root, "cap.xml"),
				base + "service=SOS&version=2.0&request=GetCapabilities");
		Node[] nodes = reader.evaluateNodes("//*:procedure");
		System.out.println("Found " + nodes.length + " procedures");
		int i = 0;
		List<String> procedures = new ArrayList<String>();
		List<String> good = new ArrayList<String>();
		List<String> bad = new ArrayList<String>();
		for (Node node : nodes) {
			String procedure = reader.evaluateString(node, ".");
			procedures.add(procedure);
			if (SOSBOMConnector.isToBeSkipped(procedure)) {
				bad.add(procedure);
			} else {
				good.add(procedure);
			}
			System.out.println("At " + i++ + "/" + nodes.length + " procedure");
//			if (procedure.contains("http://bom.gov.au/waterdata/services/tstypes/R")) {
//				System.out.println("Skipping " + procedure);
//			} else {
//
//				download(new File(root, "avail-" + procedure.hashCode() + ".xml"),
//						base + "SERVICE=SOS&VERSION=2.0.0&REQUEST=GetDataAvailability&procedure=" + procedure);
//
//			}
		}

		System.out.println("Procedures");
		print(procedures);

		System.out.println("\nFor WHOS");
		print(good);

		System.out.println("\nDiscarded");
		print(bad);

	}



	private void print(List<String> procedures) {
		procedures.sort(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});

		for (String proc : procedures) {
			System.out.println(proc);
		}

	}

	private XMLDocumentReader download(File file, String url) throws Exception {
		Downloader d = new Downloader();
		d.setConnectionTimeout(TimeUnit.MINUTES, 5);
		d.setResponseTimeout(TimeUnit.MINUTES, 5);
		InputStream s = d.downloadOptionalStream(url).get();
		FileOutputStream fos = new FileOutputStream(file);
		IOUtils.copy(s, fos);
		s.close();
		fos.close();
		return new XMLDocumentReader(file);
	}

	public static void main(String[] args) throws Exception {
		new SOSBOMTest();
	}
}
