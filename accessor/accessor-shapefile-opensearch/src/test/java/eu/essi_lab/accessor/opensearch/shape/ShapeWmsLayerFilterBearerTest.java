package eu.essi_lab.accessor.opensearch.shape;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.authorization.userfinder.KeycloakBearerTokenResolver;
import eu.essi_lab.messages.bond.spatial.ShapeLayerOwner;
import eu.essi_lab.model.auth.GSUser;

/**
 * Predefined shapes of users authenticated with a bearer token (Keycloak users)
 */
public class ShapeWmsLayerFilterBearerTest {

    @Test
    public void ownerIds() {

	Assert.assertEquals("keycloak:f69e4670-576b-4a80-b0ab-7b4679acb7bd", ShapeWmsLayerFilter.ownerOf(keycloakUser()));

	// users of the users database: their personal token, as before
	GSUser tokenUser = new GSUser();
	tokenUser.setIdentifier("his_central-568a4888");

	Assert.assertEquals("his_central-568a4888", ShapeWmsLayerFilter.ownerOf(tokenUser));
    }

    @Test
    public void bearerUserIsTheViewerWhateverTheToken() {

	GSUser user = keycloakUser();

	Assert.assertEquals("keycloak:f69e4670-576b-4a80-b0ab-7b4679acb7bd",
		ShapeWmsLayerFilter.resolveViewerOwner(ShapeLayerOwner.PUBLIC_TOKEN, user).get());

	Assert.assertEquals("keycloak:f69e4670-576b-4a80-b0ab-7b4679acb7bd",
		ShapeWmsLayerFilter.resolveViewerOwner(null, user).get());
    }

    @Test
    public void bearerUsersAreNeverAdministrators() {

	GSUser user = keycloakUser();
	user.setRole("admin");
	user.setPropertyValue("email", "admin@example.org");

	Assert.assertFalse(ShapeWmsLayerFilter.isViewerAdmin(ShapeLayerOwner.PUBLIC_TOKEN, user));
    }

    @Test
    public void anonymousWithoutBearerUser() {

	Assert.assertTrue(ShapeWmsLayerFilter.resolveViewerOwner(ShapeLayerOwner.PUBLIC_TOKEN, null).isEmpty());
	Assert.assertFalse(ShapeWmsLayerFilter.isViewerAdmin(ShapeLayerOwner.PUBLIC_TOKEN, null));

	// a user not authenticated with a bearer token does not replace the token
	GSUser other = new GSUser();
	other.setIdentifier("someone");

	Assert.assertTrue(ShapeWmsLayerFilter.resolveViewerOwner(ShapeLayerOwner.PUBLIC_TOKEN, other).isEmpty());
    }

    @Test
    public void visibilityOfOwnShapes() {

	String owner = ShapeWmsLayerFilter.ownerOf(keycloakUser());

	Assert.assertTrue(ShapeLayerOwner.isVisible(owner, owner, false));
	Assert.assertFalse(ShapeLayerOwner.isVisible(owner, "keycloak:another-user", false));
	Assert.assertFalse(ShapeLayerOwner.isVisible("his_central-568a4888", owner, false));
    }

    private static GSUser keycloakUser() {

	GSUser user = new GSUser();
	user.setIdentifier("f69e4670-576b-4a80-b0ab-7b4679acb7bd");
	user.setRole("his_central");
	user.setAuthProvider(KeycloakBearerTokenResolver.AUTH_PROVIDER);
	user.setEnabled(true);

	return user;
    }
}
