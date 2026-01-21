package eu.essi_lab.views;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.GetViewIdentifiersRequest;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.DynamicView;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.model.exceptions.GSException;

/**
 * The {@link DefaultViewManager} connects to the Database trough Database Reader and Database Writer, in order to
 * implement the {@link
 * IViewManager} interface.
 *
 * @author boldrini
 */
public class DefaultViewManager implements IViewManager {

    private static final long CACHE_DURATION = TimeUnit.MINUTES.toMillis(30);
    private static final int CACHE_SIZE = 10;

    private DatabaseWriter writer;
    private DatabaseReader reader;

    /**
     * @param dataBaseWriter
     */
    public void setDatabaseWriter(DatabaseWriter dataBaseWriter) {

	this.writer = dataBaseWriter;
    }

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
     * Gets the view associated with the given view identifier, resolved (inner views are converted into concrete
     * bonds).
     *
     * @param viewId the view identifier
     * @return the resolved view, or null if not found
     * @throws GSException
     */
    public Optional<View> getResolvedView(String viewId) throws GSException {

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

	    Optional<View> ret = getView(viewId);

	    if (!ret.isPresent()) {
		return ret;
	    }

	    View view = ret.get();

	    resolveViewBonds(view);

	    GSLoggerFactory.getLogger(getClass()).trace("Resolving view {} bonds ENDED", view.getId());

	    resolvedViewCache.put(viewId, view);

	    return Optional.of(view);
	}
    }

    /**
     * Resolve the given bond, recursively converting possible inner view bonds into concrete bonds
     * 
     * @param bond
     * @return
     */
    private void resolveViewBonds(View view) throws GSException {
	Bond bond = view.getBond();
	if (bond instanceof ViewBond) {

	    ViewBond viewBond = (ViewBond) bond;

	    Optional<View> optionalResolved = getResolvedView(viewBond.getViewIdentifier());

	    if (optionalResolved.isPresent()) {
		bond = optionalResolved.get().getBond();
		if (view.getCreator() == null) {
		    view.setCreator(optionalResolved.get().getCreator());
		}
	    }

	} else if (bond instanceof LogicalBond) {
	    LogicalBond logicalBond = (LogicalBond) bond;
	    ArrayList<Bond> resolvedOperands = new ArrayList<Bond>();
	    HashSet<String> creators = new HashSet<String>();
	    for (Bond operand : logicalBond.getOperands()) {
		View childView = new View();
		childView.setBond(operand);
		resolveViewBonds(childView);
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

    /**
     * Gets the view associated with the given view identifier, not resolved (inner views are returned as initially
     * defined). A view Id such as viewId1,viewId2 is to be intended as the AND of the views.
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
     * Puts the given bond associated with the view identifier, overwriting a possible existing view with the same
     * identifier
     *
     * @param viewId
     * @param view
     * @throws GSException
     */
    public void putView(View view) throws GSException {
	String viewId = view.getId();
	Optional<DynamicView> dynamicView = DynamicView.resolveDynamicView(viewId);
	if (dynamicView.isPresent()) {
	    throw new IllegalArgumentException("A dynamic view can't be stored: please change the view id.");
	}
	writer.store(view);
    }

    /**
     * Removes the view associated with the given identifier
     *
     * @param viewId
     * @throws GSException
     */
    public void removeView(String viewId) throws GSException {
	writer.removeView(viewId);

    }

    /**
     * Gets the list of view identifiers
     *
     * @return
     * @throws GSException
     */
    public List<String> getViewIdentifiers(int start, int count) throws GSException {
	return reader.getViewIdentifiers(GetViewIdentifiersRequest.create(start, count));

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

}
