package eu.essi_lab.api.database;

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

import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.SystemSetting.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.model.exceptions.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * The {@link ViewManager} connects to the database trough {@link DatabaseReader} to resolve inner views and to handle dynamic views.<br> To
 * improve performances, this component uses a cache to store the resolved views. The cache holds a maximum of 10 entries; the entries
 * expire after 30 minutes.<br>
 *
 * @author boldrini
 */
public class ViewManager {

    /**
     *
     */
    private static final int REFRESH_INTERVAL_MINUTES = 5;

    private DatabaseReader reader;

    private static SnapshotStore<View> viewsStore;
    private static SnapshotStore<View> resolvedViewsStore;

    public ViewManager() {
    }

    /**
     * @param reader
     */
    public ViewManager(DatabaseReader reader) throws Exception {

	this.reader = reader;

	if (viewsStore == null) {

	    GSLoggerFactory.getLogger(getClass()).info("Creating views store STARTED");

	    viewsStore = new SnapshotStore<>(reader::getViews, TimeUnit.MINUTES, REFRESH_INTERVAL_MINUTES);

	    GSLoggerFactory.getLogger(getClass()).info("Creating views store ENDED");

	    GSLoggerFactory.getLogger(getClass()).info("Creating resolved views store STARTED");

	    resolvedViewsStore = new SnapshotStore<>(() -> reader.getViews().parallelStream().peek(v -> {

		try {
		    resolve(v);

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e);
		}

	    }).filter(Objects::nonNull).toList(), TimeUnit.MINUTES, REFRESH_INTERVAL_MINUTES);

	    GSLoggerFactory.getLogger(getClass()).info("Creating resolved views store ENDED");
	}
    }

    /**
     * Gets the view associated with the given view identifier, not resolved (inner views are returned as initially defined). A view Id such
     * as viewId1,viewId2 is to be intended as the AND of the views.
     *
     * @param viewId the view identifier
     * @return the given view, or null if not found
     * @throws GSException
     */
    public Optional<View> getView(String viewId) throws Exception {

	if (viewId == null) {

	    throw new IllegalArgumentException("viewId is null");
	}

	Optional<DynamicView> dynamicView = DynamicView.resolveDynamicView(viewId);

	if (dynamicView.isPresent()) {

	    View view = dynamicView.get();
	    return Optional.of(view);
	}

	if (!ConfigurationWrapper.getSystemSettings().//
		readKeyValue(KeyValueOptionKeys.VIEWS_CACHE.getLabel()).//
		map(Boolean::parseBoolean). //
		orElse(true)) {

	    return reader.getView(viewId);
	}

	return viewsStore. //
		getSnapshots().//
		stream().//
		filter(v -> v.getId().equals(viewId)).//
		findFirst();
    }

    /**
     * Gets the view associated with the given view identifier, resolved (inner views are converted into concrete bonds).
     *
     * @param viewId the view identifier
     * @return the resolved view, or null if not found
     * @throws GSException
     */
    public Optional<View> getResolvedView(String viewId) throws Exception {

	if (viewId == null) {

	    throw new IllegalArgumentException("viewId is null");
	}

	if (!ConfigurationWrapper.getSystemSettings().//
		readKeyValue(KeyValueOptionKeys.VIEWS_CACHE.getLabel()).//
		map(Boolean::parseBoolean). //
		orElse(true)) {

	    return getAndResolve(viewId);
	}

	return resolvedViewsStore. //
		getSnapshots().//
		stream().//
		filter(v -> v.getId().equals(viewId)).//
		findFirst();
    }

    /**
     * Gets the list of view identifiers
     *
     * @return
     * @throws GSException
     */
    public List<String> getViewIdentifiers(GetViewIdentifiersRequest vir) throws GSException {

	return reader.getViewIdentifiers(vir);
    }

    /**
     * @param viewId
     * @return
     * @throws GSException
     */
    private Optional<View> getAndResolve(String viewId) throws Exception {

	Optional<View> ret = getView(viewId);

	if (ret.isEmpty()) {

	    return ret;
	}

	View view = ret.get();

	resolve(view);

	return Optional.of(view);
    }

    /**
     * Resolve the given bond, recursively converting possible inner view bonds into concrete bonds
     *
     * @param bond
     * @return
     */
    private void resolve(View view) throws Exception {

	Bond bond = view.getBond();

	if (bond instanceof ViewBond viewBond) {

	    Optional<View> optionalResolved = getAndResolve(viewBond.getViewIdentifier());

	    if (optionalResolved.isPresent()) {

		bond = optionalResolved.get().getBond();

		if (view.getCreator() == null) {

		    view.setCreator(optionalResolved.get().getCreator());
		}
	    }

	} else if (bond instanceof LogicalBond logicalBond) {

	    ArrayList<Bond> resolvedOperands = new ArrayList<Bond>();
	    HashSet<String> creators = new HashSet<String>();

	    for (Bond operand : logicalBond.getOperands()) {

		View childView = new View();
		childView.setBond(operand);

		resolve(childView);
		resolvedOperands.add(childView.getBond());

		String creator = childView.getCreator();

		if (creator != null) {

		    creators.add(creator);
		}
	    }

	    if (view.getCreator() == null && creators.size() == 1) {

		view.setCreator(creators.iterator().next());
	    }

	    bond = BondFactory.createLogicalBond(logicalBond.getLogicalOperator(), resolvedOperands);
	}

	view.setBond(bond);
    }
}
