package eu.essi_lab.gssrv.conf;

import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.value.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public abstract class AbstractGridFilter<G extends GridDataModel> {

    /**
     *
     */
    private final HashMap<String, String> selectionMap;

    /**
     *
     */
    public AbstractGridFilter() {

	selectionMap = new HashMap<>();
    }

    /**
     * @param gridData
     * @return
     */
    public boolean test(G gridData) {

	boolean match = true;

	for (String colum : selectionMap.keySet()) {

	    String itemValue = getItemValue(colum, gridData);

	    match &= itemValue == null || itemValue.toLowerCase().contains(selectionMap.get(colum).toLowerCase());
	}

	return match;
    }

    /**
     * @param colum
     * @param gridData
     * @return
     */
    protected abstract String getItemValue(String colum, G gridData);

    /**
     * @param columnKey
     * @param value
     */
    public void filter(String columnKey, String value) {

	selectionMap.put(columnKey, value);
    }
}
