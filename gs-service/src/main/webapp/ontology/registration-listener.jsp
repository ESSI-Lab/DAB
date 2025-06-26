<%@page import="net.sf.saxon.expr.instruct.ForEach"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Enumeration"%>
<%@page import="eu.essi_lab.gssrv.user_reg.HISUserRegistration"%>
<html>
<head>
<title>Registrazione utente HIS-Central</title>
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
 	 
	 <div class="page_header">
		
			<div class="main_title">HIS-Central</div>
			<div class="sub_title">Registrazione utente</div>
													 									
 	</div> 
	<%
	String verify = request.getParameter("verify");

	if (verify != null && !verify.trim().isEmpty()) {

	    // registration link

	    try {
			HISUserRegistration.activateUser(verify);
			out.println("<div class=\"page-form\">");
			out.println("<h2>La registrazione è stata effettuata con successo.</h2>");
			out.println("<h2>Il tuo token personale di identificazione: " + verify + "</h2>");
			out.println("</div>");
			
	    } catch (Exception e) {
		
	   		 out.println("<p>Si è verificato un problema con la registrazione utente a HIS-Central. Si prega di contattare ISPRA per procedere.</p>\n");
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

	    String institution = request.getParameter("institution");
	    String institutionType = request.getParameter("institutionType");
	    String otherInstitutionType = request.getParameter("otherInstitutionType");
	    
	    out.println("<div class=\"page-form\">");
	    out.println("<h2>Richiesta di registrazione ricevuta</h2>");
	    out.println("<p><b>Nome</b>: " + fname + "</p>");
	    out.println("<p><b>Cognome</b>: " + lname + "</p>");
	    out.println("<p><b>Indirizzo E-mail</b>: " + email + "</p>");
	    out.println("<p><b>Ente</b>: " + institution + "</p>");
	    out.println("<p><b>Tipo di ente</b>: " + (institutionType == null ? "" : institutionType) + "</p>");

	    if (otherInstitutionType != null && !otherInstitutionType.trim().isEmpty()) {
		
			out.println("<p><b>Altro tipo di ente</b>: " + otherInstitutionType + "</p>");
	    }
	    
	    String registrationPage = request.getRequestURL().toString();

	    HISUserRegistration registration = new HISUserRegistration(		    
		    fname, 
		    lname, 
		    email, 
		    institution, 
		    institutionType,
	    	otherInstitutionType, 
	    	registrationPage);

	    Boolean isValid = registration.isValid();
	    
	    if(!humanTestValid){
		
			out.println("<h2>Registrazione fallita: la risposta al test non è corretta</h2>\n");
	    
	    }else if (!isValid) {
			
			out.println("<h2>Registrazione fallita: parametri inseriti non corretti</h2>\n");
			
			List<String> errorMessages = registration.getValidationMessages();
			
			for (String error : errorMessages) {
	    		out.println("<p>" + error + "</p>\n");
			}
			
	    } else {
			out.println("<h2>Registrazione in corso</h2>\n");

			boolean sent = registration.addUserAndSendMail();
			if (sent) {
		    	out.println(
				    "<p>Riceverai al più presto una E-mail per confermare la tua registrazione.</p>\n");
			} else {
		   	    out.println(
				    "<p>Si è verificato un problema nell'invio della E-mail. Si prega di contattare ISPRA.</p>\n");
			}
			out.println("</div>");
	    }
	}
	%>
</body>
</html>