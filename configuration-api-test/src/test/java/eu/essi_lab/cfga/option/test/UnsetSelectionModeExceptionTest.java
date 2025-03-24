/**
 * 
 */
package eu.essi_lab.cfga.option.test;

import static org.junit.Assert.assertThrows;

import java.util.List;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Selectable.SelectionMode;
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.UnsetSelectionModeException;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public class UnsetSelectionModeExceptionTest {

    @Test
    public void test() {

	//
	// non clean option with single selection
	// it has both values "Yes","No"
	//
	Option<BooleanChoice> option = BooleanChoiceOptionBuilder.get().//
		withKey("key").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.FALSE).//
		cannotBeDisabled().//
		build();

	SelectionMode selectionMode = option.getSelectionMode();
	Assert.assertEquals(SelectionMode.SINGLE, selectionMode);

	{

	    JSONArray selectedIndexes = option.getObject().getJSONArray("selectedIndexes");
	    Assert.assertEquals(1, selectedIndexes.length());
	    Assert.assertEquals(1, selectedIndexes.getInt(0));

	    BooleanChoice value = option.getValue();
	    Assert.assertEquals(BooleanChoice.TRUE, value);

	    List<BooleanChoice> values = option.getValues();
	    Assert.assertEquals(BooleanChoice.TRUE, values.get(0));
	    Assert.assertEquals(BooleanChoice.FALSE, values.get(1));

	    BooleanChoice selectedValue = option.getSelectedValue();
	    Assert.assertEquals(BooleanChoice.FALSE, selectedValue);

	    List<BooleanChoice> selectedValues = option.getSelectedValues();
	    Assert.assertEquals(1, selectedValues.size());
	    Assert.assertEquals(BooleanChoice.FALSE, selectedValues.get(0));
	}

	//
	// since the option is non clean, values can be selected
	//

	{
	    option.select(v -> v == BooleanChoice.TRUE);

	    JSONArray selectedIndexes = option.getObject().getJSONArray("selectedIndexes");
	    Assert.assertEquals(1, selectedIndexes.length());
	    Assert.assertEquals(0, selectedIndexes.getInt(0));

	    BooleanChoice selectedValue = option.getSelectedValue();
	    Assert.assertEquals(BooleanChoice.TRUE, selectedValue);

	    List<BooleanChoice> selectedValues = option.getSelectedValues();
	    Assert.assertEquals(1, selectedValues.size());
	    Assert.assertEquals(BooleanChoice.TRUE, selectedValues.get(0));

	    BooleanChoice value = option.getValue();
	    Assert.assertEquals(BooleanChoice.TRUE, value);

	    List<BooleanChoice> values = option.getValues();
	    Assert.assertEquals(BooleanChoice.TRUE, values.get(0));
	    Assert.assertEquals(BooleanChoice.FALSE, values.get(1));
	}

	{
	    option.select(v -> v == BooleanChoice.FALSE);

	    JSONArray selectedIndexes = option.getObject().getJSONArray("selectedIndexes");
	    Assert.assertEquals(1, selectedIndexes.length());
	    Assert.assertEquals(1, selectedIndexes.getInt(0));

	    BooleanChoice selectedValue = option.getSelectedValue();
	    Assert.assertEquals(BooleanChoice.FALSE, selectedValue);

	    List<BooleanChoice> selectedValues = option.getSelectedValues();
	    Assert.assertEquals(1, selectedValues.size());
	    Assert.assertEquals(BooleanChoice.FALSE, selectedValues.get(0));

	    BooleanChoice value = option.getValue();
	    Assert.assertEquals(BooleanChoice.TRUE, value);

	    List<BooleanChoice> values = option.getValues();
	    Assert.assertEquals(BooleanChoice.TRUE, values.get(0));
	    Assert.assertEquals(BooleanChoice.FALSE, values.get(1));
	}

	//
	// now the option is clean, and the last selected value if "No" (ooleanChoice.FALSE)
	// the selection mode is now UNSET
	//

	option.clean();

	selectionMode = option.getSelectionMode();
	Assert.assertEquals(SelectionMode.UNSET, selectionMode);

	{

	    JSONArray selectedIndexes = option.getObject().getJSONArray("selectedIndexes");
	    Assert.assertEquals(1, selectedIndexes.length());
	    Assert.assertEquals(0, selectedIndexes.getInt(0));

	    //
	    //
	    //

	    assertThrows(UnsetSelectionModeException.class, () -> {
		option.select(v -> v == BooleanChoice.TRUE);
	    });
	}

    }
}
