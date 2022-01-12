package eu.essi_lab.shared.messages;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.shared.driver.ISharedRepositoryDriver;
public class SharedContentQuery extends RequestMessage {


    @Override
    public String getBaseType() {
	return "shared-content-message";
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = -5656909904075040375L;

    public static final String DEFAULT_TIME_AXIS = "defaultaxis";

    private static final String TIME_CONSTRAINTS = "TIME_CONSTRAINTS";

    public void setTimeConstraint(SharedContentTimeContraintCollection contraint) {

	getPayload().add(new GSProperty<>(TIME_CONSTRAINTS, contraint));

    }

    /**
     * Adding a time constraint allows to define queries on multiple time axis. By default all time constraints are in
     * AND relationship.
     *
     * @param contraint
     */
    public void addTimeConstraint(SharedContentTimeContraint contraint) {

	Optional<SharedContentTimeContraintCollection> optional = getTimeConstraints();

	if (optional.isPresent())
	    optional.get().getList().add(contraint);
	else {
	    SharedContentTimeContraintCollection collection = new SharedContentTimeContraintCollection();

	    collection.getList().add(contraint);

	    setTimeConstraint(collection);
	}

    }

    public Optional<SharedContentTimeContraintCollection> getTimeConstraints() {

	return Optional.ofNullable(getPayload().get(TIME_CONSTRAINTS, SharedContentTimeContraintCollection.class));

    }

    public Long getTo() {

	return getTo(DEFAULT_TIME_AXIS);
    }

    private Optional<SharedContentTimeContraint> findTimeConstraint(String timeaxis) {

	Optional<SharedContentTimeContraintCollection> tc = getTimeConstraints();

	if (tc.isPresent())

	    return tc.get().getList().stream().filter(c -> c.getTimeAxis().equals(timeaxis)).findFirst();

	return Optional.empty();
    }

    public Long getTo(String timeaxis) {

	Optional<SharedContentTimeContraint> optional = findTimeConstraint(timeaxis);

	if (optional.isPresent())
	    return optional.get().getTo();

	return null;
    }

    public void setTo(Long to) {
	setTo(to, DEFAULT_TIME_AXIS);
    }

    public void setTo(Long to, String timeAxis) {

	Optional<SharedContentTimeContraint> optional = findTimeConstraint(timeAxis);

	if (optional.isPresent())
	    optional.get().setTo(to);
	else {
	    SharedContentTimeContraint contraint = new SharedContentTimeContraint();

	    contraint.setTimeAxis(timeAxis);
	    contraint.setTo(to);

	    addTimeConstraint(contraint);
	}
    }

    public Long getFrom(String timeaxis) {

	Optional<SharedContentTimeContraint> optional = findTimeConstraint(timeaxis);

	if (optional.isPresent())
	    return optional.get().getFrom();

	return null;
    }

    public Long getFrom() {
	return getFrom(DEFAULT_TIME_AXIS);
    }

    public void setFrom(Long from) {
	setFrom(from, DEFAULT_TIME_AXIS);
    }

    public void setFrom(Long from, String timeAxis) {

	Optional<SharedContentTimeContraint> optional = findTimeConstraint(timeAxis);

	if (optional.isPresent())
	    optional.get().setFrom(from);
	else {
	    SharedContentTimeContraint contraint = new SharedContentTimeContraint();

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

}
