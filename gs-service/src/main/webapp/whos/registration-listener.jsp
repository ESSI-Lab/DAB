<%@page import="net.sf.saxon.expr.instruct.ForEach"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Enumeration"%>
<%@page import="eu.essi_lab.gssrv.user_reg.WHOSUserRegistration"%>
<html>
<head>
<title>WMO Hydrological Observing System (WHOS) User Registration</title>
<style>
			#otherInstitutionDiv {
 				display: none;
			}
			label{
				font-size: x-large;
				font-weight: bold;
			}
			
			.page-form { 
			  display: block;
			  margin-top: 30px;
			  margin-left: 12px;
			}
			
			/** 
			 *  Colors	 
			 */
			 
			:root {
			  --color1: #00529c;
			  --color2: #00abd0; 
			  --color3: lightgray;
		  	  --color4: white;			 
			}
									
		    body{
		      font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
		    }
		    
		    /** 
			 *  Put the loading GIF	 
			 */
			 
		    .loading_div{
				  position: fixed;
				  top: 50%;
				  left: 50%;
				  transform: translate(-50%, -50%);
		    	  content: url("../../../../bnhs/station/gwis-loading.gif");		    	
		    }

		 	/** 
			 *  Header and logos	 
			 */
			 
		 	.page_header{	
				  padding: 10px;
			      background-color: var(--color1);
			      margin-left: 12px;
		          margin-right: 12px;
			      font-size: 30px;
			      color: var(--color4);
			}
							
   			.main_title{
   			    margin-left: 50px;   			
   				font-weight:bold;
   			}
   			
   			.sub_title{
   			    margin-left: 50px;
   			    margin-top: 10px;  		
   				font-size:80%;
   			}
   			  					
			.page_info{
				line-height: 1.6;
			 	padding: 10px;
				margin-left: 12px;
		        margin-right: 12px;
		        border: 1px solid var(--color3);
		        color: var(--color1);
			}
			
			.page_info_text{
				margin-left: 50px;
			}
   			  			
   			.wmo_logo{
				float:right; 
				margin-right: 50px;
				display:inline-block; 
				margin-top: -70px;
				width: 190px;
				content: url("../bnhs/station/WMO_logo_white_ENG.png");
			}
			
			.wmo_logo_below{
				float:right; 
				margin-right: 40px;
				display:inline-block; 
				margin-top: 12px;
				width: 200px;
				content: url("../bnhs/station/wmo-bar2.PNG");
			}
								
			#hidden-div{
				visibility: hidden;
			}
		 	
		 	#dialog-download{
		 	
		 	}
		 	#format-fieldset{
		 		border: none;
		 	}
		 	
		 	.custom_button {
			  background-color: #4CAF50; /* Green */
			  border: none;
			  color: white;
			  padding: 15px 32px;
			  text-align: center;
			  text-decoration: none;
			  display: inline-block;
			  font-size: 16px;
			}
			
			.registerbtn {
			  background-color: #04AA6D;
			  color: white;
			  padding: 16px 20px;
			  margin: 8px 0;
			  border: none;
			  cursor: pointer;
			  width: 100%;
			  opacity: 0.9;
			}
			
</style>
</head>
<body>
	<!-- <h1>WMO Hydrological Observing System (WHOS)</h1>
	<h1>User Registration</h1>  -->
	 
	 <div class="page_header">
		
			<div class="main_title">WMO Hydrological Observing System (WHOS)</div><div class="sub_title">User Registration</div>
						
			<a target=_blank href="https://public.wmo.int/en"><div class="wmo_logo"></div></a>
			<a target=_blank href="https://public.wmo.int/en"><div class="wmo_logo_below"></div></a>
						 									
 	</div> 
	<%
	String verify = request.getParameter("verify");

	if (verify != null && !verify.trim().isEmpty()) {

	    // registration link

	    try {
		WHOSUserRegistration.activateUser(verify);
		out.println("<div class=\"page-form\">");
		out.println("<h2>Your registration has been completed successfully.</h2>");
		out.println("<h2>Your personal token identifier: " + verify + "</h2>");
		out.println("</div>");
		
		
	    } catch (Exception e) {
	    out.println("<p>A problem occurred registering WHOS user. Please contact WHOS representatives to proceed.</p>\n");

	    }

	} else {
	    
	    // human test
	    boolean humanTestValid = false;	    	    
	    String[] humanTest = request.getParameterValues("humanTest");
	     
	    if(humanTest != null && humanTest.length == 1 && humanTest[0].equals("human")){
			humanTestValid = true;
	    }

	    // registration validation

	    String fname = request.getParameter("fname");
	    String lname = request.getParameter("lname");
	    String email = request.getParameter("email");
	    String country = request.getParameter("country");
	    //String institution = request.getParameter("institution");
	    String institutionType = request.getParameter("institutionType");
	    String otherInstitutionType = request.getParameter("otherInstitutionType");
	    String position = request.getParameter("position");
	    out.println("<div class=\"page-form\">");
	    out.println("<h2>Registration request received</h2>");
	    out.println("<p><b>First name</b>: " + fname + "</p>");
	    out.println("<p><b>Last name</b>: " + lname + "</p>");
	    out.println("<p><b>E-mail address</b>: " + email + "</p>");
	    out.println("<p><b>Country</b>: " + country + "</p>");
	    //out.println("<p>Institution: " + institution + "</p>");
	    out.println("<p><b>Institution type</b>: " + institutionType + "</p>");
	    if (otherInstitutionType != null && !otherInstitutionType.trim().isEmpty()) {
			out.println("<p><b>Other institution type</b>: " + otherInstitutionType + "</p>");
	    }
	    out.println("<p><b>Position</b>: " + position + "</p>");

	    String registrationPage = request.getRequestURL().toString();

	    WHOSUserRegistration registration = new WHOSUserRegistration(		    
		    fname, 
		    lname, 
		    email, 
		    country, 
		    null, 
		    institutionType,
	    	otherInstitutionType, 
	    	position, 
	    	registrationPage);

	    Boolean isValid = registration.isValid();
	    
	    if(!humanTestValid){
		
			out.println("<h2>Registration aborted: provided test response is not valid</h2>\n");
	    
	    }else if (!isValid) {
			
			out.println("<h2>Registration aborted: invalid parameters</h2>\n");
			
			List<String> errorMessages = registration.getValidationMessages();
			
			for (String error : errorMessages) {
	    		out.println("<p>" + error + "</p>\n");
			}
			
	    } else {
			out.println("<h2>Registration in progress</h2>\n");

			boolean sent = registration.addUserAndSendMail();
			if (sent) {
		    	out.println(
				    "<p>You will shortly receive an email to confirm your registration.</p>\n");
			} else {
		   	    out.println(
				    "<p>A problem occurred sending e-mail message to the provided e-mail. Please contact WHOS representatives to proceed.</p>\n");
			}
			out.println("</div>");
	    }

	}
	%>
</body>
</html>