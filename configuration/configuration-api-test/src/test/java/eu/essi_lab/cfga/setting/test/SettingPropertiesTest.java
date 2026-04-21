package eu.essi_lab.cfga.setting.test;

import eu.essi_lab.cfga.setting.*;
import org.junit.*;

import java.util.*;

/**
 *
 */
public class SettingPropertiesTest {

    /**
     *
     */
    @Test
    public void settingPropertiesTest() {

	List<Property<?>> properties = Setting.getDeclaredProperties().//
		stream().//
		sorted(Comparator.comparing(Property::getName)).//
		toList();

	Assert.assertEquals(20, properties.size());

	// (AfterCleanFunction, afterCleanFunction, false, true, Optional.empty)
	// (CanBeCleaned, canBeCleaned, true, true, Optional[true])
	// (CanBeDisabled, canBeDisabled, true, true, Optional[true])
	// (CanBeRemoved, canBeRemoved, true, true, Optional[false])
	// (CompactMode, compactMode, true, true, Optional[true])
	// (ConfigurableType, configurableType, false, true, Optional.empty)
	// (Description, description, true, false, Optional.empty)
	// (Editable, editable, true, true, Optional[true])
	// (Enabled, enabled, true, true, Optional[true])
	// (Extension, extensionClass, false, true, Optional.empty)
	// (FoldedMode, foldedMode, true, true, Optional[false])
	// (Identifier, settingId, true, false, Optional.empty)
	// (Name, settingName, true, false, Optional.empty)
	// (ObjectType, type, true, false, Optional.empty)
	// (Selected, selected, true, true, Optional[false])
	// (SelectionMode, selectionMode, true, true, Optional[unset])
	// (SettingClass, settingClass, true, false, Optional.empty)
	// (ShowHeader, showHeader, true, true, Optional[true])
	// (Validator, validatorClass, false, true, Optional.empty)
	// (Visible, visible, true, true, Optional[true])

	Assert.assertEquals("(AfterCleanFunction, afterCleanFunction, false, false, Optional.empty)", properties.get(0).toString());
	Assert.assertEquals("(CanBeCleaned, canBeCleaned, true, true, Optional[true])", properties.get(1).toString());
	Assert.assertEquals("(CanBeDisabled, canBeDisabled, true, true, Optional[true])", properties.get(2).toString());
	Assert.assertEquals("(CanBeRemoved, canBeRemoved, true, true, Optional[false])", properties.get(3).toString());
	Assert.assertEquals("(CompactMode, compactMode, true, true, Optional[false])", properties.get(4).toString());
	Assert.assertEquals("(ConfigurableType, configurableType, false, false, Optional.empty)", properties.get(5).toString());
	Assert.assertEquals("(Description, description, false, false, Optional.empty)", properties.get(6).toString());
	Assert.assertEquals("(Editable, editable, true, true, Optional[true])", properties.get(7).toString());
	Assert.assertEquals("(Enabled, enabled, true, true, Optional[true])", properties.get(8).toString());
	Assert.assertEquals("(Extension, extensionClass, false, false, Optional.empty)", properties.get(9).toString());
	Assert.assertEquals("(FoldedMode, foldedMode, true, true, Optional[false])", properties.get(10).toString());
	Assert.assertEquals("(Identifier, settingId, true, false, Optional.empty)", properties.get(11).toString());
	Assert.assertEquals("(Name, settingName, true, false, Optional.empty)", properties.get(12).toString());
	Assert.assertEquals("(ObjectType, type, true, false, Optional.empty)", properties.get(13).toString());
	Assert.assertEquals("(Selected, selected, true, true, Optional[false])", properties.get(14).toString());
	Assert.assertEquals("(SelectionMode, selectionMode, true, true, Optional[unset])", properties.get(15).toString());
	Assert.assertEquals("(SettingClass, settingClass, true, false, Optional.empty)", properties.get(16).toString());
	Assert.assertEquals("(ShowHeader, showHeader, true, true, Optional[true])", properties.get(17).toString());
	Assert.assertEquals("(Validator, validatorClass, false, false, Optional.empty)", properties.get(18).toString());
	Assert.assertEquals("(Visible, visible, true, true, Optional[true])", properties.get(19).toString());
    }
}
