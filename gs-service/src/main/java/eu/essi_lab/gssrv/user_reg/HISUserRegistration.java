package eu.essi_lab.gssrv.user_reg;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import eu.essi_lab.authorization.userfinder.UserFinder;
import eu.essi_lab.cfga.gs.ConfiguredGmailClient;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.auth.UserIdentifierType;

/**
 * @author Fabrizio
 */
public class HISUserRegistration {

    private String fname;
    private String lname;
    private String email;
    private String institution;
    private String institutionType;
    private String otherInstitutionType;
    private boolean isValid;
    private List<String> validationMessages;
    private String registrationURI;
    private Date registrationDate;

    /**
     * @param fname
     * @param lname
     * @param email
     * @param institution
     * @param institutionType
     * @param otherInstitutionType
     * @param registrationURI
     */
    public HISUserRegistration( //
	    String fname, //
	    String lname, //
	    String email, //
	    String institution, //
	    String institutionType, //
	    String otherInstitutionType, //
	    String registrationURI) { //

	super();

	this.fname = fname;
	this.lname = lname;
	this.email = email;
	this.institution = institution;
	this.institutionType = institutionType;
	this.otherInstitutionType = otherInstitutionType;
	this.registrationURI = registrationURI;

	this.validationMessages = new ArrayList<>();

	checkValidation();
    }

    public static void activateUser(String id) throws Exception {

	UserFinder finder = UserFinder.create();

	finder.enableUser(id);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List<HISUserRegistration> getUsers() throws Exception {

	List<HISUserRegistration> ret = new ArrayList<>();
	UserFinder finder = UserFinder.create();

	List<GSUser> users = finder.getUsers(false);

	for (GSUser user : users) {

	    String role = user.getRole();

	    if (role != null && role.equals("his_central")) {

		String myFname = null;
		String myLname = null;
		String myEmail = null;
		String myInstitution = null;
		String myInstitutionType = null;
		String myOtherInstitutionType = null;
		String myId = user.getIdentifier();
		String myRegistrationDate = null;

		List<GSProperty> properties = user.getProperties();

		for (GSProperty<String> property : properties) {

		    String name = property.getName();
		    String value = property.getValue();

		    switch (name) {
		    case "firstName":
			myFname = value;
			break;
		    case "lastName":
			myLname = value;
			break;
		    case "email":
			myEmail = value;
			break;
		    case "institution":
			myInstitution = value;
			break;
		    case "institutionType":
			myInstitutionType = value;
			break;
		    case "otherInstitutionType":
			myOtherInstitutionType = value;
			break;
		    case "registrationDate":
			myRegistrationDate = value;
			break;
		    default:
			break;
		    }
		}

		HISUserRegistration wuser = new HISUserRegistration(//
			myFname, //
			myLname, //
			myEmail, //
			myInstitution, //
			myInstitutionType, //
			myOtherInstitutionType, //
			myId);

		Optional<Date> parsed = ISO8601DateTimeUtils.parseISO8601ToDate(myRegistrationDate);

		if (parsed.isPresent()) {
		    wuser.setRegistrationDate(parsed.get());
		}

		ret.add(wuser);
	    }
	}

	Collections.sort(ret, new Comparator<HISUserRegistration>() {

	    @Override
	    public int compare(HISUserRegistration o1, HISUserRegistration o2) {
		Date d1 = o1.getRegistrationDate();
		Date d2 = o2.getRegistrationDate();
		if (d1 == null && d2 == null) {
		    return 0;
		}
		if (d1 == null) {
		    return -1;
		}
		if (d2 == null) {
		    return 1;
		}
		return d1.compareTo(d2);
	    }
	});
	return ret;
    }

    public String getFname() {
	return fname;
    }

    public String getLname() {
	return lname;
    }

    public String getEmail() {
	return email;
    }

    public String getInstitutionType() {
	return institutionType;
    }

    public String getOtherInstitutionType() {
	return otherInstitutionType;
    }

    public String getRegistrationURI() {
	return registrationURI;
    }

    /**
     * @return the institution
     */
    public String getInstitution() {
	return institution;
    }

    public Date getRegistrationDate() {
	return registrationDate;
    }

    public boolean isValid() {
	return isValid;
    }

    public List<String> getValidationMessages() {
	return validationMessages;
    }

    public boolean addUserAndSendMail() {

	String subject = "Registrazione utente a HIS-Central";

	GSUser user;
	try {
	    user = addUser();
	} catch (Exception e) {
	    e.printStackTrace();
	    return false;
	}

	String message = "Si prega di confermare la registrazione usando il seguente link di attivazione:\n\n" + registrationURI
		+ "?verify=" + user.getIdentifier()
		+ "\n\nSe hai ricevuto questa E-mail per errore, si prega di informarci rispondendo a questa E-mail ed eliminandola.";

	String recipient = this.email;
	
	System.out.println("Sending mail to: " + recipient);
	System.out.println(message);

	return ConfiguredGmailClient.sendEmail(subject, message, recipient);
    }

    public GSUser addUser() throws Exception {

	UserFinder finder = UserFinder.create();

	String id = "his_central-" + UUID.randomUUID().toString();

	GSUser user = new GSUser(id, UserIdentifierType.USER_TOKEN, "his_central");
	user.setEnabled(false);

	List<GSProperty<String>> properties = new ArrayList<>();

	properties.add(new GSProperty<String>("firstName", fname));
	properties.add(new GSProperty<String>("lastName", lname));
	properties.add(new GSProperty<String>("email", email));
	properties.add(new GSProperty<String>("institution", institution));
	properties.add(new GSProperty<String>("institutionType", institutionType));

	if (otherInstitutionType != null && !otherInstitutionType.trim().isEmpty()) {
	    properties.add(new GSProperty<String>("otherInstitutionType", otherInstitutionType));
	}

	properties.add(new GSProperty<String>("registrationDate", ISO8601DateTimeUtils.getISO8601DateTime()));
	user.getProperties().addAll(properties);

	finder.getUsersWriter().store(user);

	return user;
    }

    private void checkValidation() {

	isValid = true;

	if (isEmpty(fname)) {
	    isValid = false;
	    validationMessages.add("Nome mancante");
	}

	if (isEmpty(lname)) {
	    isValid = false;
	    validationMessages.add("Cognome mancante");
	}

	if (isEmpty(email)) {
	    isValid = false;
	    validationMessages.add("E-mail mancante");
	}

	if (isEmpty(institutionType)) {
	    isValid = false;
	    validationMessages.add("Ente mancante");

	} else {

	    if (institutionType.equals("other")) {
		if (isEmpty(otherInstitutionType)) {
		    isValid = false;
		    validationMessages.add("Altro tipo di ente mancante");
		}
	    }
	}
    }

    private boolean isEmpty(String string) {

	return string == null || string.trim().isEmpty();
    }

    private void setRegistrationDate(Date date) {

	this.registrationDate = date;
    }
}
