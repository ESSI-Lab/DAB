package eu.essi_lab.gssrv.validator;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;

import eu.essi_lab.accessor.wfs._1_1_0.WFS_1_1_0Connector;
import eu.essi_lab.accessor.wms._1_1_1.WMS_1_1_1Connector;
import eu.essi_lab.accessor.wms._1_3_0.WMS_1_3_0Connector;
import eu.essi_lab.model.GSSource;

public class Validator {

	public static String getReport(String linkageParameter, String protocol) {
		HttpClient client = HttpClient.newHttpClient();

		String ret = "Basic connection\n";
		URI uri = null;
		try {
			uri = URI.create(linkageParameter);
		} catch (Exception e) {
			ret += "Not an URI: " + linkageParameter;
			return ret;
		}

		if (uri.getAuthority() == null || uri.getAuthority().isEmpty()) {
			ret += "Empty authority in URL (missing http://?): " + linkageParameter;
			return ret;
		}

		HttpRequest hrequest = HttpRequest.newBuilder().uri(uri).method("HEAD", HttpRequest.BodyPublishers.noBody())
				.build();

		try {
			ret += "Sending HEAD request to: " + uri + "\n";
			java.net.http.HttpResponse<Void> hresponse = client.send(hrequest,
					java.net.http.HttpResponse.BodyHandlers.discarding());
			ret += "Response code: " + hresponse.statusCode() + "\n";
			List<String> contentType = hresponse.headers().map().get("Content-Type");
			if (contentType.isEmpty()) {
				ret += "No content type header\n";

			} else {
				ret += "Content type: " + contentType.get(0) + "\n";

			}
			String contentLength = hresponse.headers().firstValue("Content-Length").orElse("Unknown");
			System.out.println("Expected File Size: " + contentLength + " bytes");
		} catch (Exception e) {
			e.printStackTrace();
		}
		ret += "\nProtocol checking (" + protocol + ")\n";

		if (protocol.toLowerCase().contains("wms") || protocol.toLowerCase().contains("map service")) {
			if (protocol.contains("1.3")) {
				ret += "\nGuessing WMS from protocol\n";
				WMS_1_3_0Connector connector = new WMS_1_3_0Connector();
				GSSource s = new GSSource("test");
				s.setEndpoint(linkageParameter);
				boolean b = connector.supports(s);
				if (b) {
					ret += "\nWMS 1.3 client connected succesfully\n";
				} else {
					ret += "\nError connecting with WMS 1.3 client\n";
				}
			} else {
				ret += "\nGuessing WMS from protocol\n";
				WMS_1_1_1Connector connector = new WMS_1_1_1Connector();
				GSSource s = new GSSource("test");
				s.setEndpoint(linkageParameter);
				boolean b = connector.supports(s);
				if (b) {
					ret += "\nWMS 1.1 client connected succesfully\n";
				} else {
					ret += "\nError connecting with WMS 1.1 client\n";
				}
			}
		} else if (protocol.toLowerCase().contains("wfs") || protocol.toLowerCase().contains("feature service")) {
			ret += "\nGuessing WFS from protocol\n";
			WFS_1_1_0Connector connector = new WFS_1_1_0Connector();
			GSSource s = new GSSource("test");
			s.setEndpoint(linkageParameter);
			boolean b = connector.supports(s);
			if (b) {
				ret += "\nWFS 1.1 client connected succesfully\n";
			} else {
				ret += "\nError connecting with WFS 1.1 client\n";
			}

		}

		else {
			ret += "\nNo supported protocol identified\n";
		}

		return ret;
	}

}
