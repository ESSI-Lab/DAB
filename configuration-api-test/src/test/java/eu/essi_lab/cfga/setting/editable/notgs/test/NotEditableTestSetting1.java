package eu.essi_lab.cfga.setting.editable.notgs.test;

import java.util.UUID;

/**
 * This is almost equals to {@link EditableTestSetting} except that setting 1 has a random identifier
 * 
 * @author Fabrizio
 */
public class NotEditableTestSetting1 extends EditableTestSetting {

    /**
     * 
     */
    public NotEditableTestSetting1() {

	getSetting1().setIdentifier(UUID.randomUUID().toString());
    }
}
