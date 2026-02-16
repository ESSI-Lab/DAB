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
import eu.essi_lab.cfga.gs.setting.SystemSetting.KeyValueOptionKeys;

import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.model.exceptions.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * The {@link ViewManager} connects to the database trough {@link DatabaseReader} to resolve inner views and to handle dynamic views.<br> To
 * improve performances, this component uses a cache to store the resolved views. The cache holds a
 * maximum of 10 entries; the entries expire after 30 minutes.<br>
 *
 * @author boldrini
 */
public class ViewManager {

    private static final long CACHE_DURATION = TimeUnit.MINUTES.toMillis(30);
    private static final int CACHE_SIZE = 10;

    private DatabaseReader reader;

    /**
     * @param executor
     */
    public void setDatabaseReader(DatabaseReader executor) {

	this.reader = executor;
    }

    private static ExpiringCache<View> resolvedViewCache = null;
    private static ExpiringCache<String> resolvedViewLocks = null;

    static {
	resolvedViewCache = new ExpiringCache<>();
	resolvedViewCache.setDuration(CACHE_DURATION);
	resolvedViewCache.setMaxSize(CACHE_SIZE);

	resolvedViewLocks = new ExpiringCache<>();
	resolvedViewLocks.setDuration(CACHE_DURATION);
	resolvedViewLocks.setMaxSize(CACHE_SIZE);
    }

    /**
     * Gets the view associated with the given view identifier, resolved (inner views are converted into concrete bonds).
     *
     * @param viewId the view identifier
     * @return the resolved view, or null if not found
     * @throws GSException
     */
    public Optional<View> getResolvedView(String viewId) throws GSException {

	if( !ConfigurationWrapper.getSystemSettings().//
		readKeyValue(KeyValueOptionKeys.VIEWS_CACHE.getLabel()).//
		map(Boolean::parseBoolean). //
		orElse(true)){

	    GSLoggerFactory.getLogger(getClass()).info("Views cache disabled");

	    return getAndResolve(viewId);
	}

	String lock = null;

	synchronized (resolvedViewLocks) {
	    lock = resolvedViewLocks.get(viewId);
	    if (lock == null) {
		resolvedViewLocks.put(viewId, viewId);
		lock = resolvedViewLocks.get(viewId);
	    }
	}

	synchronized (lock) {

	    View cachedView = resolvedViewCache.get(viewId);

	    if (cachedView != null) {
		GSLoggerFactory.getLogger(getClass()).trace("View cache HIT: " + viewId);
		return Optional.of(cachedView);
	    }

	    GSLoggerFactory.getLogger(getClass()).trace("View cache MISS: " + viewId);

	    Optional<View> view = getAndResolve(viewId);

	    view.ifPresent(v ->  resolvedViewCache.put(viewId, v));

	    GSLoggerFactory.getLogger(getClass()).trace("Resolving view {} bonds ENDED", view.get().getId());

	    return view;
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
    public Optional<View> getView(String viewId) throws GSException {

	if (viewId == null) {
	    return Optional.empty();
	}

	Optional<DynamicView> dynamicView = DynamicView.resolveDynamicView(viewId);

	if (dynamicView.isPresent()) {
	    View view = dynamicView.get();
	    return Optional.of(view);
	}

	return reader.getView(viewId);
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
     *
     * @param viewId
     * @return
     * @throws GSException
     */
    private Optional<View> getAndResolve(String viewId) throws GSException {

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
    private void resolve(View view) throws GSException {

	Bond bond = view.getBond();

	if (bond instanceof ViewBond viewBond) {

	    Optional<View> optionalResolved = getResolvedView(viewBond.getViewIdentifier());

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
