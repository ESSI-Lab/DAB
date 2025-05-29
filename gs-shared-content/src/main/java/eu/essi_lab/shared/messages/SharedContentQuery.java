package eu.essi_lab.shared.messages;

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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.shared.driver.ISharedRepositoryDriver;

/**
 * This class encapsulates the mesage for querying a shared repository. It is used to enhance basic interface which had
 * been designed for
 * {@link ISharedRepositoryDriver}. Utilizing this object, which has multiple time axis, we support the case in which
 * more than one time
 * axis can be queried (e.g. for batch jobs we might want all jobs started after a date and completed before another
 * date). In addition,
 * by extending {@link RequestMessage} we get pagination and other features for free.
 *
 * @author ilsanto
 */
public class SharedContentQuery extends RequestMessage {

    private ArrayList<String> idsList;

    /**
     * 
     */
    private static final long serialVersionUID = -5656909904075040375L;

    public static final String DEFAULT_TIME_AXIS = "defaultaxis";

    private static final String TIME_CONSTRAINTS = "TIME_CONSTRAINTS";

    /**
     * 
     */
    public SharedContentQuery() {

	setTimeConstraint(new ArrayList<>());

	idsList = new ArrayList<>();
    }

    /**
     * @param identifier
     */
    public void addIdentifier(String identifier) {

	idsList.add(identifier);
    }

    /**
     * @return the idsList
     */
    public ArrayList<String> getIdsList() {

	return idsList;
    }

    /**
     * @param list
     */
    public void setTimeConstraint(List<SharedContentTimeConstraint> list) {

	getPayload().add(new GSProperty<>(TIME_CONSTRAINTS, list));
    }

    /**
     * Adding a time constraint allows to define queries on multiple time axis. By default all time constraints are in
     * AND relationship.
     *
     * @param contraint
     */
    public void addTimeConstraint(SharedContentTimeConstraint constraint) {

	List<SharedContentTimeConstraint> timeConstraints = getTimeConstraints();

	timeConstraints.add(constraint);
    }

    @SuppressWarnings("unchecked")
    public List<SharedContentTimeConstraint> getTimeConstraints() {

	return getPayload().get(TIME_CONSTRAINTS, List.class);
    }

    /**
     * @return
     */
    public Long getTo() {

	return getTo(DEFAULT_TIME_AXIS);
    }

    /**
     * @param timeaxis
     * @return
     */
    public Long getTo(String timeaxis) {

	Optional<SharedContentTimeConstraint> optional = findTimeConstraint(timeaxis);

	if (optional.isPresent()) {
	    return optional.get().getTo();
	}

	return null;
    }

    /**
     * @param to
     */
    public void setTo(Long to) {

	setTo(to, DEFAULT_TIME_AXIS);
    }

    /**
     * @param to
     * @param timeAxis
     */
    public void setTo(Long to, String timeAxis) {

	Optional<SharedContentTimeConstraint> optional = findTimeConstraint(timeAxis);

	if (optional.isPresent()) {
	    optional.get().setTo(to);
	} else {
	    SharedContentTimeConstraint contraint = new SharedContentTimeConstraint();

	    contraint.setTimeAxis(timeAxis);
	    contraint.setTo(to);

	    addTimeConstraint(contraint);
	}
    }

    /**
     * @param timeaxis
     * @return
     */
    public Long getFrom(String timeaxis) {

	Optional<SharedContentTimeConstraint> optional = findTimeConstraint(timeaxis);

	if (optional.isPresent()) {
	    return optional.get().getFrom();
	}

	return null;
    }

    /**
     * @return
     */
    public Long getFrom() {

	return getFrom(DEFAULT_TIME_AXIS);
    }

    /**
     * @param from
     */
    public void setFrom(Long from) {

	setFrom(from, DEFAULT_TIME_AXIS);
    }

    /**
     * @param from
     * @param timeAxis
     */
    public void setFrom(Long from, String timeAxis) {

	Optional<SharedContentTimeConstraint> optional = findTimeConstraint(timeAxis);

	if (optional.isPresent()) {

	    optional.get().setFrom(from);
	} else {
	    SharedContentTimeConstraint contraint = new SharedContentTimeConstraint();

	    contraint.setTimeAxis(timeAxis);
	    contraint.setFrom(from);

	    addTimeConstraint(contraint);
	}
    }

    @Override
    public HashMap<String, List<String>> provideInfo() {

	return new HashMap<>();
    }

    @Override
    public String getName() {

	return "SHARED_CONTENT_QUERY";
    }

    private Optional<SharedContentTimeConstraint> findTimeConstraint(String timeaxis) {

	return getTimeConstraints().stream().filter(c -> c.getTimeAxis().equals(timeaxis)).findFirst();
    }
}
