package eu.essi_lab.cfga.setting.editable.notgs.test;

import java.util.UUID;

/**
 * This is almost equals to {@link EditableTestSetting} except that option 1 has a random identifier
 * 
 * @author Fabrizio
 */
public class NotEditableTestSetting2 extends EditableTestSetting {

    /**
     * 
     */
    public NotEditableTestSetting2() {

	getSetting1().getOption("option1", String.class).get().setKey(UUID.randomUUID().toString());
    }
}
