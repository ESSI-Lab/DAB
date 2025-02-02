package eu.essi_lab.pdk.wrt;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * Represents a parameter of a {@link WebRequest} with <i>GET</i> method and optionally provides
 * a {@link Bond} representation of such parameter (see {@link #asBond(String)})
 * <h3>Parameters declaration and usage</h3>
 * If the parameters are declared as <i>static</i> fields in a container class, they can be retrieved in toto using
 * {@link #findParameters(Class)} or by name using {@link #findParameter(String, Class)}.<br>
 * <br>
 * A possible {@link DiscoveryRequestTransformer#getUserBond(WebRequest)} implementation can be like this:<br>
 * <br>
 * <ol>
 * <li>creates a {@link KeyValueParser} with {@link WebRequest#getQueryString()}</li>
 * <li>find all the parameters using {@link #findParameters(Class)}</li>
 * <li>for each parameter:
 * <ul>
 * <li>get its current value invoking {@link KeyValueParser#getValue(String)} with the parameter
 * name ({@link WebRequestParameter#getName()}) as argument</li>
 * <li>validate the parameter value according to an expected content or type (optional)</li>
 * <li>invoke {@link WebRequestParameter#asBond(String)} with the parameter value</li>
 * </ul>
 * <li>in case of multiple {@link Bond}s (according to the declared {@link WebRequestParameter}s), they can be grouped
 * in a single {@link LogicalBond}<br>
 * </li>
 * </ol>
 * <b>Example code</b><br>
 * 
 * <pre>
 * &#64;Override
 * protected Optional&lt;Bond&gt; asBond(WebRequest request) throws GSException {
 * 
 *     // creates the parser
 *     KeyValueParser parser = new KeyValueParser(request.getQueryString());
 * 
 *     // finds all the parameters declared in a container class
 *     List&lt;WebRequestParameter&gt; parameters = WebRequestParameter.findParameters(MyParameters.class);
 * 
 *     // creates the bond list
 *     ArrayList&lt;Bond&gt; bondList = new ArrayList&lt;&gt;();
 * 
 *     for (WebRequestParameter param : parameters) {
 * 
 * 	// get the current parameter value
 * 	String value = parser.getValue(param.getName());
 * 
 * 	// if value is null, the current parameter is not used in the web request
 * 	if (value == null) {
 * 	    // using a default value
 * 	    value = param.getDefaultValue();
 * 	}
 * 
 * 	if (value != null) {
 * 
 * 	    // validates the value according to an expected content or type (optional)
 * 	    // ....
 * 
 * 	    // get the bond representation of the parameter
 * 	    Optional&lt;Bond&gt; bond = param.asBond(value);
 * 
 * 	    if (bond.isPresent()) {
 * 		bondList.add(bond.get());
 * 	    }
 * 	}
 *     }
 * 
 *     Bond bond = null;
 *     if (bondList.size() > 1) {
 * 	// groups all the bonds in a logical bond
 * 	bond = BondFactory.createAndBond(bondList.toArray(new Bond[] {}));
 *     } else if (bondList.size() == 1) {
 * 	bond = bondList.get(0);
 *     }
 * 
 *     return bond;
 * }
 * </pre>
 * 
 * @see DiscoveryRequestTransformer#getUserBond(WebRequest)
 * @see DiscoveryRequestTransformer#validate(WebRequest)
 * @see DiscoveryRequestTransformer#transform(WebRequest)
 * @see #asBond(String)
 * @see DiscoveryMessage#setUserBond(Bond)
 * @author Fabrizio
 */
public abstract class WebRequestParameter {

    private String valueType;
    private String defaultValue;
    private String name;
    private boolean mandatory;

    /**
     * Creates a new parameter with the supplied <code>name</code>, <code>type</code> and <code>defaultValue</code>
     * 
     * @see #setMandatory(boolean)
     * @param name the parameter name
     * @param type the parameter type (e.g: "integer", "freeText", etc)
     * @param defaultValue a default value for the parameter. Usually a parameter with a default value is also optional,
     *        and the default value can be used in case the parameter is missing
     */
    public WebRequestParameter(String name, String type, String defaultValue) {
	this.name = name;
	this.valueType = type;
	this.defaultValue = defaultValue;
    }

    /**
     * Creates a new parameter with the supplied <code>name</code>, <code>type</code> and no default value
     * 
     * @see #setMandatory(boolean)
     * @param name the parameter name
     * @param type the parameter type (e.g: "integer", "freeText", etc)
     */
    public WebRequestParameter(String name, String type) {
	this(name, type, null);
    }

    /**
     * Returns this parameter name
     * 
     * @return a non <code>null</code> string
     */
    public String getName() {
	return name;
    }

    /**
     * Returns the value type of this parameter (e.g: "integer", "freetext", etc)
     * 
     * @return a non <code>null</code> string
     */
    public String getValueType() {
	return valueType;
    }

    /**
     * Returns <code>true</code> if this is a mandatory parameter, <code>false</code> otherwise
     * 
     * @return <code>true</code> if this is a mandatory parameter, <code>false</code> otherwise
     */
    public boolean isMandatory() {
	return mandatory;
    }

    /**
     * Set the {@link #isMandatory()} property
     * 
     * @param mandatory
     */
    public void setMandatory(boolean mandatory) {
	this.mandatory = mandatory;
    }

    /**
     * Get the optional default value. Usually a parameter with a default value is also optional,
     * and the default value can be used in case the parameter is missing
     * 
     * @see #setMandatory(boolean)
     * @see #WebRequestParameter(String, String, String)
     * @return a string with the default value, or <code>null</code> if it not set
     */
    public String getDefaultValue() {
	return defaultValue;
    }

    /**
     * Finds all the {@link WebRequestParameter}s declared with the <i>static</i> modifier in the given
     * <code>containerClass</code>.<br>
     * <br>
     * <b>Usage example</b><br>
     * 
     * <pre>
     * List&ltMyParamater&gt params = WebRequestParameter.findParameters(MyContainerClass.class);
     * for (MyParamater param : params) {
     *     String name = param.getName();
     *     ...
     * }
     * </pre>
     * 
     * @param containerClass the class where the {@link WebRequestParameter}s are declared
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends WebRequestParameter> List<T> findParameters(Class<?> containerClass) {

	ArrayList<WebRequestParameter> out = new ArrayList<WebRequestParameter>();
	Field[] fields = containerClass.getDeclaredFields();
	for (Field field : fields) {
	    try {
		boolean valid = Modifier.isStatic(field.getModifiers());
		valid &= Modifier.isFinal(field.getModifiers());
		if (valid) {
		    WebRequestParameter param = (WebRequestParameter) field.get(null);
		    out.add(param);
		}
	    } catch (Exception e) {
	    }
	}
	return (List<T>) out;
    }

    /**
     * Finds the {@link WebRequestParameter} with the given <code>name</code> declared with the <i>static</i>
     * modifier in the given <code>containerClass</code>
     * 
     * @see WebRequestParameter#findParameters(Class)
     * @param name
     * @param containerClass
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends WebRequestParameter> T findParameter(String name, Class<?> containerClass) {

	List<WebRequestParameter> findParameters = findParameters(containerClass);
	for (WebRequestParameter webRequestParameter : findParameters) {
	    if (webRequestParameter.getName().equals(name)) {
		return (T) webRequestParameter;
	    }
	}
	return null;
    }

    /**
     * Creates the {@link Bond} representation of this parameter.<br>
     * In some particular cases the represented {@link Bond} can be a {@link LogicalBond} of several
     * {@link Bond}s, but more often it will be a single {@link QueryableBond}<br>
     * <br>
     * <b>Implementation example</b><br>
     * <br>
     * In this example, the {@link WebRequest} is a GET request with a parameter that, basing on its semantic, can be
     * mapped on the {@link MetadataElement#TITLE} and a non exact match. The following bond representation can be
     * provided:
     * 
     * <pre>
     * &#64;Override
     * public Optional&lt;Bond&gt; asBond(String value) {
     * 
     *     return Optional.of(BondFactory.createSimpleElementBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, value));
     * }
     * </pre>
     * 
     * If the semantic of the parameter relates to an exact match, the {@link BondOperator#EQUAL} should be used instead
     * of {@link BondOperator#TEXT_SEARCH}
     * 
     * <pre>
     * &#64;Override
     * public Optional&lt;Bond&gt; asBond(String value) {
     * 
     *     return Optional.of(BondFactory.createSimpleElementBond(BondOperator.EQUAL, MetadataElement.TITLE, value));
     * }
     * </pre>
     * 
     * @param value the current parameter value
     * @param relatedValues the optional values from other related parameters
     * @return an {@code Optional} describing the the {@link Bond} representation of this parameter, if non-null,
     *         otherwise returns an empty {@code Optional}
     * @throws IllegalArgumentException if the given value is <code>null</code>
     */
    public abstract Optional<Bond> asBond(String value, String... relatedValues) throws Exception;
}
