package eu.essi_lab.dab.auth;

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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
	// For now, we'll return a success response with user details
	// Replace this with your actual authentication logic
//	List<GSUser> users = new ArrayList<GSUser>();
//	try {
//	    users = UserFinder.create().getUsers(false);
//	} catch (Exception e) {
//	    e.printStackTrace();
//	}
//	for (GSUser user : users) {
//	    String id = user.getIdentifier();
//	    System.out.println(id);
//	}
	boolean valid = false;
	if (valid) {
	    
	    
	    LoginResponse response = new LoginResponse(true, "Login successful", "John", "Doe", request.getEmail(), request.getApiKey());
	    return ResponseEntity.ok(response);
	} else {
	    LoginResponse response = new LoginResponse(false, "Invalid credentials", null, null, null, null);
	    return ResponseEntity.ok(response);
	}
    }

 

}
