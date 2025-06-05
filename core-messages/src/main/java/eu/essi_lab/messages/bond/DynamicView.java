package eu.essi_lab.messages.bond;

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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Pluggable;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.model.pluggable.Provider;

/**
 * A dynamic view is a predefined view that is resolved at runtime without looking in the db. It is recognized by a
 * reserved prefix (gs-) and a dynamic view prefix.
 * 
 * @author boldrini
 */
public abstract class DynamicView extends View implements Pluggable {

    protected List<String> arguments;

    public static List<DynamicView> getAvailableDynamicViews() {
	PluginsLoader<DynamicView> pluginsLoader = new PluginsLoader<>();
	List<DynamicView> views = pluginsLoader.loadPlugins(DynamicView.class);
	return views;
    }

    public static final String RESERVED_PREFIX = "gs-view-";

    public static final String ARGUMENT_START = "(";

    public static final String ARGUMENT_END = ")";

    public static final String ARGUMENT_SEPARATOR = ",";

    public DynamicView() {
	arguments = new ArrayList<>();
    }

    @Override
    public String getId() {

	if (arguments.isEmpty()) {
	    return RESERVED_PREFIX + getPrefix();
	}

	String argument = "";

	for (String arg : arguments) {
	    argument += arg + ARGUMENT_SEPARATOR;
	}
	argument = argument.substring(0, argument.length() - 1);

	return RESERVED_PREFIX + getPrefix() + ARGUMENT_START + argument + ARGUMENT_END;

    }

    @Override
    public String getLabel() {
	return "Dyanamic view with id: " + getId();
    }

    @Override
    public Date getCreationTime() {
	return new Date();
    }

    @Override
    public Date getExpirationTime() {
	return null;
    }

    /**
     * The postifx associated with the dynamic view (in addition to the reserved prefix). Each dynamic view type has its
     * own prefix.
     * 
     * @return
     */

    public abstract String getPrefix();

    /**
     * Sets the argument part of the view
     * 
     * @param postfix
     */
    public void setPostfix(String postfix) {

	try {
	    postfix = URLDecoder.decode(postfix, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}

	if (postfix == null || postfix.equals("") || postfix.equals(ARGUMENT_START + ARGUMENT_END)) {
	    // null
	    // ""
	    // ()

	    // nothing to do
	} else

	if (postfix.contains(ARGUMENT_SEPARATOR)) {

	    // could be one of the cases:

	    // (v1,v2)
	    // (v1,gs-view-source(s1))
	    // (v1,gs-view-and(gs-view-source(s1),v2))

	    if (postfix.startsWith(ARGUMENT_START) && postfix.endsWith(ARGUMENT_END)) {
		postfix = postfix.substring(1, postfix.length() - 1);
	    }

	    // 1) v1,v2
	    // 2) v1,gs-view-source(s1)
	    // 3) v1,gs-view-and(gs-view-source(s1),v2)

	    String tmpArgument = "";
	    for (int i = 0; i < postfix.length(); i++) {

		String c = "" + postfix.charAt(i);
		switch (c) {
		case ARGUMENT_SEPARATOR:
		    arguments.add(tmpArgument);
		    tmpArgument = "";
		    break;

		case ARGUMENT_START:
		    // we are in case 2) or 3)
		    int balance = 0;
		    for (; i < postfix.length(); i++) {
			String d = "" + postfix.charAt(i);
			switch (d) {
			case ARGUMENT_START:
			    balance++;
			    tmpArgument += d;
			    break;
			case ARGUMENT_END:
			    balance--;
			    tmpArgument += d;
			    break;
			case ARGUMENT_SEPARATOR:
			    if (balance == 0) {
				arguments.add(tmpArgument);
				tmpArgument = "";
				break;
			    } else {
				tmpArgument += d;
			    }
			    break;
			default:
			    tmpArgument += d;
			    break;
			}
		    }
		    break;

		default:
		    tmpArgument += c;
		    break;
		}

	    }

	    if (!tmpArgument.equals("")) {
		arguments.add(tmpArgument);
	    }

	} else {

	    // v1
	    // (v1)

	    arguments.add(postfix.replace(ARGUMENT_START, "").replace(ARGUMENT_END, ""));
	}

    }

    @Override
    public Provider getProvider() {
	return new ESSILabProvider();
    }

    public static Optional<DynamicView> resolveDynamicView(String viewId) {
	List<DynamicView> dynamicViews = getAvailableDynamicViews();
	for (DynamicView dynamicView : dynamicViews) {
	    String prefix = RESERVED_PREFIX + dynamicView.getPrefix();
	    if (viewId.startsWith(prefix)) {
		String postfix = viewId.substring(prefix.length(), viewId.length());
		dynamicView.setPostfix(postfix);
		dynamicView.setId(viewId);
		return Optional.of(dynamicView);
	    }
	}
	return Optional.empty();
    }

    @Override
    public Bond getBond() {
	if (bond == null) {
	    return getDynamicBond();
	}
	return super.getBond();
    }

    /**
     * Subclasses must implement this method according to the (optional) arguments
     * 
     * @return
     */
    public abstract Bond getDynamicBond();
}
