<%@page import="org.json.JSONArray"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="com.amazonaws.util.IOUtils"%>
<%@page import="org.json.JSONObject"%>
<%@page import="java.net.http.HttpResponse"%>
<%@page import="java.io.InputStream"%>
<%@page import="eu.essi_lab.lib.net.downloader.Downloader"%>
<html>
<head></head>
<body>

	<%
 
	String source = request.getParameter("source");
	String token = request.getParameter("token");

	if (source != null && !source.trim().isEmpty()) {
		
		String scheme = request.getScheme();             // http or https
        String serverName = request.getServerName();     // hostname or IP address
        int serverPort = request.getServerPort();        // port number
        String contextPath = request.getContextPath();   // /yourwebapp
        String servletPath = request.getServletPath();   // /servlet or /jsp
        String pathInfo = request.getPathInfo();         // /pathInfo (if any)
        String queryString = request.getQueryString();   // query string

        // Reconstruct the URL
        StringBuilder endpoint = new StringBuilder();
        endpoint.append(scheme).append("://").append(serverName);

        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
        	endpoint.append(":").append(serverPort);
        }

        endpoint.append(contextPath);

        endpoint.append("/services/essi/token/"+token+"/view/blue-cloud-terms/terms-api/terms?offset=1&limit=10&source="+source+"&type=");
    
        out.println("<h1>Metadata terms for "+source+"</h1>");
        
	
     String[] types = new String[]{"keyword", "keyword_uri", "observed_property", "observed_property_uri", "instrument", "instrument_uri", "platform", "platform_uri", "organization", "organization_uri", "cruise", "cruise_uri", "project", "project_uri"};

 	Downloader downloader = new Downloader();

     
     for(String type: types){
     
 	String url = endpoint.toString()+type;

     
	HttpResponse<InputStream> res = downloader.downloadResponse(url);
	
	
			
// 	out.println(url);
			
    out.println("<h2>"+type+"</h2>");

			
			
	ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			
	IOUtils.copy(res.body(), baos);
	
	String str = new String(baos.toByteArray());
			
	JSONObject json = new JSONObject(str);
	
	JSONArray terms = json.getJSONArray("terms");
	out.println("<p>");
	for(int i = 0;i<terms.length();i++){
		String term = terms.getString(i);
		out.println(term+"<br/>");
	}
	out.println("</p>");
	
     }
		
	}else{
		out.println("missing source parameter");
	}

%>

</body>
</html>