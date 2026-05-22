package eu.essi_lab.accessor.opensearch.shape;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import eu.essi_lab.authorization.BasicRole;
import eu.essi_lab.authorization.userfinder.UserFinder;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.spatial.ShapeLayerOwner;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.auth.GSUser;

/**
 * Filters predefined shape WMS layers by {@link eu.essi_lab.api.database.opensearch.index.mappings.ShapeFileMapping#OWNER}.
 */
public final class ShapeWmsLayerFilter {

    /**
     * Resolved viewer and bulk-loaded entry owners (one OpenSearch scan per request).
     */
    public static final class LayerFilterContext {

	private final String viewerOwner;
	private final boolean viewerIsAdmin;
	private final Map<String, String> entryOwners;

	LayerFilterContext(String viewerOwner, boolean viewerIsAdmin, Map<String, String> entryOwners) {

	    this.viewerOwner = viewerOwner == null ? "" : viewerOwner;
	    this.viewerIsAdmin = viewerIsAdmin;
	    this.entryOwners = entryOwners == null ? Map.of() : entryOwners;
	}

	public String getViewerOwner() {

	    return viewerOwner;
	}

	public boolean isViewerAdmin() {

	    return viewerIsAdmin;
	}

	public Map<String, String> getEntryOwners() {

	    return entryOwners;
	}
    }

    private ShapeWmsLayerFilter() {
    }

    /**
     * @param servletContextPath request context path (e.g. {@code /gs-service})
     * @param tokenSegment value from {@code /token/{token}/view/...} ({@link ShapeLayerOwner#PUBLIC_TOKEN} when not logged in)
     * @param shapeView view id (e.g. {@code his-central-shapes})
     * @return relative WMS endpoint path
     */
    public static String buildWmsEndpointPath(String servletContextPath, String tokenSegment, String shapeView) {

	String context = servletContextPath == null ? "" : servletContextPath;
	String token = normalizeToken(tokenSegment);

	return context + "/services/essi/token/" + token + "/view/" + shapeView + "/wms";
    }

    /**
     * @param tokenSegment token from the WMS layers request
     * @return filter context with viewer owner and all entry owners loaded from OpenSearch
     */
    public static LayerFilterContext createContext(String tokenSegment) {

	Optional<GSUser> viewer = resolveViewer(tokenSegment);
	String viewerOwner = viewer.map(GSUser::getUri).orElse("");
	boolean viewerIsAdmin = viewer.map(ShapeWmsLayerFilter::isAdminUser).orElse(false);

	try {

	    Map<String, String> entryOwners = new OpenSearchShapefileClient().loadEntryOwners();
	    return new LayerFilterContext(viewerOwner, viewerIsAdmin, entryOwners);

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(ShapeWmsLayerFilter.class).error("Unable to load shape entry owners: {}", ex.getMessage());
	    return new LayerFilterContext(viewerOwner, viewerIsAdmin, Collections.emptyMap());
	}
    }

    /**
     * @param tokenSegment token from the WMS layers request
     * @return whether the viewer is an administrator
     */
    public static boolean isViewerAdmin(String tokenSegment) {

	return resolveViewer(tokenSegment).map(ShapeWmsLayerFilter::isAdminUser).orElse(false);
    }

    /**
     * @param tokenSegment token from the WMS URL
     * @return owner id of the viewer, or empty when anonymous
     */
    public static Optional<String> resolveViewerOwner(String tokenSegment) {

	return resolveViewer(tokenSegment).map(GSUser::getUri);
    }

    private static Optional<GSUser> resolveViewer(String tokenSegment) {

	String token = normalizeToken(tokenSegment);

	if (ShapeLayerOwner.PUBLIC_TOKEN.equals(token)) {
	    return Optional.empty();
	}

	try {

	    for (GSUser user : UserFinder.create().getUsers(false)) {

		if (token.equals(user.getUri())) {
		    return Optional.of(user);
		}
	    }

	} catch (Exception e) {
	    return Optional.empty();
	}

	return Optional.empty();
    }

    private static boolean isAdminUser(GSUser user) {

	if (user == null) {
	    return false;
	}

	if (BasicRole.ADMIN_ROLE_VALUE.equals(user.getRole())) {
	    return true;
	}

	List<String> adminUsers = ConfigurationWrapper.getAdminUsers();
	if (adminUsers == null || adminUsers.isEmpty()) {
	    return false;
	}

	String uri = user.getUri();
	String identifier = user.getIdentifier();
	Optional<GSProperty> email = user.getProperty("email");
	String mail = null;
	if (email.isPresent()) {
	    mail = email.get().getValue().toString();
	}

	for (String adminUser : adminUsers) {

	    if (adminUser == null || adminUser.isBlank()) {
		continue;
	    }

	    if (adminUser.equals(uri)) {
		return true;
	    }

	    if (identifier != null && adminUser.equalsIgnoreCase(identifier)) {
		return true;
	    }

	    if (mail!=null&&mail.equals(adminUser)) {
		return true;
	    }
	}

	return false;
    }

    /**
     * @param layerOnlineId WMS layer name
     * @param context preloaded owners and viewer
     * @return whether the layer should be exposed to this viewer
     */
    public static boolean isLayerVisible(String layerOnlineId, LayerFilterContext context) {

	if (context == null) {
	    return true;
	}

	Optional<String> entryName = ShapeLayerOwner.entryNameFromOnlineId(layerOnlineId);

	if (entryName.isEmpty()) {
	    // Legacy S3-harvested layers: treated like admin-owned predefined areas
	    return ShapeLayerOwner.isVisible(ShapeLayerOwner.ADMIN_OWNER, context.getViewerOwner(), context.isViewerAdmin());
	}

	String resourceOwner = context.getEntryOwners().getOrDefault(entryName.get(), "");
	return ShapeLayerOwner.isVisible(resourceOwner, context.getViewerOwner(), context.isViewerAdmin());
    }

    private static String normalizeToken(String tokenSegment) {

	if (tokenSegment == null || tokenSegment.isBlank()) {
	    return ShapeLayerOwner.PUBLIC_TOKEN;
	}

	return tokenSegment;
    }
}
