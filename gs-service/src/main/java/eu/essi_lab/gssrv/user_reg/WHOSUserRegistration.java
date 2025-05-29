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

public class WHOSUserRegistration {

    private String fname;
    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public String getEmail() {
        return email;
    }

    public String getCountry() {
        return country;
    }

    public String getInstitutionType() {
        return institutionType;
    }

    public String getOtherInstitutionType() {
        return otherInstitutionType;
    }

    public String getPosition() {
        return position;
    }

    public String getRegistrationURI() {
        return registrationURI;
    }

    private String lname;
    private String email;
    private String country;
    // private String institution;
    private String institutionType;
    private String otherInstitutionType;
    private String position;
    private boolean isValid;
    private List<String> validationMessages = new ArrayList<>();
    private String registrationURI = null;
    private Date registrationDate = null;

    public Date getRegistrationDate() {
	return registrationDate;
    }

    public WHOSUserRegistration(String fname, String lname, String email, String country, String institution, String institutionType,
	    String otherInstitutionType, String position, String registrationURI) {
	super();
	this.fname = fname;
	this.lname = lname;
	this.email = email;
	this.country = country;
	// this.institution = institution;
	this.institutionType = institutionType;
	this.otherInstitutionType = otherInstitutionType;
	this.position = position;
	this.registrationURI = registrationURI;
	checkValidation();
    }

    private void checkValidation() {
	isValid = true;

	if (isEmpty(fname)) {
	    isValid = false;
	    validationMessages.add("Empty first name");
	}
	if (isEmpty(lname)) {
	    isValid = false;
	    validationMessages.add("Empty last name");
	}
	if (isEmpty(email)) {
	    isValid = false;
	    validationMessages.add("Empty email");
	}
	if (isEmpty(country)) {
	    isValid = false;
	    validationMessages.add("Empty country");
	}
	if (isEmpty(institutionType)) {
	    isValid = false;
	    validationMessages.add("Empty institution type");
	} else {
	    if (institutionType.equals("other")) {
		if (isEmpty(otherInstitutionType)) {
		    isValid = false;
		    validationMessages.add("Empty other institution type");
		}
	    }
	}

    }

    private boolean isEmpty(String string) {
	return string == null || string.trim().isEmpty();
    }

    public boolean isValid() {
	return isValid;
    }

    public List<String> getValidationMessages() {
	return validationMessages;
    }

    public boolean addUserAndSendMail() {
	String subject = "WHOS user registration";
	GSUser user;
	try {
	    user = addUser();
	} catch (Exception e) {
	    e.printStackTrace();
	    return false;
	}

	String message = "Please confirm your registration using the following activation link:\n\n" + registrationURI + "?verify="
		+ user.getIdentifier()
		+ "\n\nIf you have received this email by mistake, please inform us by replying to this email and then delete it.";
	String recipient = this.email;
	System.out.println("Sending mail to: " + recipient);
	System.out.println(message);

	return ConfiguredGmailClient.sendEmail(subject, message, recipient);
    }

    public static void activateUser(String id) throws Exception {

	UserFinder finder = UserFinder.create();

	finder.enableUser(id);
    }

    public GSUser addUser() throws Exception {

	UserFinder finder = UserFinder.create();

	String id = "whos-" + UUID.randomUUID().toString();
	GSUser user = new GSUser(id,  UserIdentifierType.USER_TOKEN, "whos");
	user.setEnabled(false);
	List<GSProperty<String>> properties = new ArrayList<>();
	properties.add(new GSProperty<String>("firstName", fname));
	properties.add(new GSProperty<String>("lastName", lname));
	properties.add(new GSProperty<String>("email", email));
	properties.add(new GSProperty<String>("country", country));
	// if (institution != null && !institution.trim().isEmpty()) {
	// properties.add(new GSProperty<String>("institution", institution));
	// }
	properties.add(new GSProperty<String>("institutionType", institutionType));
	if (otherInstitutionType != null && !otherInstitutionType.trim().isEmpty()) {
	    properties.add(new GSProperty<String>("otherInstitutionType", otherInstitutionType));
	}
	if (position != null && !position.trim().isEmpty()) {
	    properties.add(new GSProperty<String>("position", position));
	}
	properties.add(new GSProperty<String>("registrationDate", ISO8601DateTimeUtils.getISO8601DateTime()));
	user.getProperties().addAll(properties);
	finder.getWriter().store(user);

	return user;

    }

    public static List<WHOSUserRegistration> getUsers() throws Exception {

	List<WHOSUserRegistration> ret = new ArrayList<>();
	UserFinder finder = UserFinder.create();

	List<GSUser> users = finder.getUsers(false);
	for (GSUser user : users) {
	    String role = user.getRole();
	    if (role != null && role.equals("whos")) {
		String myFname = null;
		String myLname = null;
		String myEmail = null;
		String myCountry = null;
		String myInstitution = null;
		String myInstitutionType = null;
		String myOtherInstitutionType = null;
		String myPosition = null;
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
		    case "country":
			myCountry = value;
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
		    case "position":
			myPosition = value;
			break;
		    case "registrationDate":
			myRegistrationDate = value;
			break;

		    default:
			break;
		    }
		}
		WHOSUserRegistration wuser = new WHOSUserRegistration(myFname, myLname, myEmail, myCountry, myInstitution,
			myInstitutionType, myOtherInstitutionType, myPosition, myId);
		Optional<Date> parsed = ISO8601DateTimeUtils.parseISO8601ToDate(myRegistrationDate);
		if (parsed.isPresent()) {
		    wuser.setRegistrationDate(parsed.get());
		}
		ret.add(wuser);
	    }
	}
	Collections.sort(ret, new Comparator<WHOSUserRegistration>() {

	    @Override
	    public int compare(WHOSUserRegistration o1, WHOSUserRegistration o2) {
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

    private void setRegistrationDate(Date date) {
	this.registrationDate = date;

    }

    

}
