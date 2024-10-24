<%@page import="eu.essi_lab.lib.utils.ISO8601DateTimeUtils"%>
<%@page import="net.sf.saxon.expr.instruct.ForEach"%>
<%@page import="java.util.List"%>
<%@page import="eu.essi_lab.gssrv.user_reg.WHOSUserRegistration"%>
<html>
<head>
<title>WMO Hydrological Observing System (WHOS) Users</title>
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

		<div class="main_title">WMO Hydrological Observing System (WHOS)</div>
		<div class="sub_title">Users</div>

		<a target=_blank href="https://public.wmo.int/en"><div
				class="wmo_logo"></div></a> <a target=_blank
			href="https://public.wmo.int/en"><div class="wmo_logo_below"></div></a>

	</div>
	<%
	String verify = request.getParameter("verify");

	if (verify == null || verify.trim().isEmpty() || !verify.trim().equals("CnR1289")) {

	    // registration link

	    out.println("<p>This page needs authorization.</p>\n");

	} else {

	    // registration validation
		List<WHOSUserRegistration> users = WHOSUserRegistration.getUsers();
	    out.println("<div class=\"page-form\">");
	    out.println("<h2>Users: ("+(users.size()-1)+")</h2>");
	    
	    for (WHOSUserRegistration user : users) {

		if (user.getRegistrationURI()!=null && user.getRegistrationURI().equals("whos")){
		    continue;
		}
		
		out.println("<p><b>Id: </b>: " + user.getRegistrationURI() + "</p>");
		out.println("<p><b>First name</b>: " + user.getFname() + "</p>");
		out.println("<p><b>Last name</b>: " + user.getLname() + "</p>");
		out.println("<p><b>E-mail address</b>: " + user.getEmail() + "</p>");
		out.println("<p><b>Country</b>: " + user.getCountry() + "</p>");
		out.println("<p><b>Institution type</b>: " + user.getInstitutionType() + "</p>");
		if (user.getOtherInstitutionType()!=null){
		out.println("<p><b>Other institution type</b>: " + user.getOtherInstitutionType() + "</p>");
		}
		out.println("<p><b>Position</b>: " + user.getPosition() + "</p>");	
		if (user.getRegistrationDate()!=null){
		out.println("<p><b>Date</b>: " + ISO8601DateTimeUtils.getISO8601DateTime(user.getRegistrationDate()) + "</p>");
		}

		out.println("<br/><br/>");
	    }
	    
		out.println("</div>");

	}
	%>
</body>
</html>