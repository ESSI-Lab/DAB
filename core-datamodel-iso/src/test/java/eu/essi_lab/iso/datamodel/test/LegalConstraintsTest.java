package eu.essi_lab.iso.datamodel.test;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import net.opengis.iso19139.gmd.v_20060504.MDLegalConstraintsType;

public class LegalConstraintsTest extends MetadataTest<LegalConstraints, MDLegalConstraintsType> {

    public LegalConstraintsTest() {
	super(LegalConstraints.class, MDLegalConstraintsType.class);
    }

    @Override
    public void setProperties(LegalConstraints legal) {
	legal.addUseLimitation("Use Limitation");
	legal.addAccessConstraintsCode("patent");
	legal.addUseConstraintsCode("trademark");
	legal.addOtherConstraints("Other Constraints");

    }

    @Override
    public void checkProperties(LegalConstraints legal) {
	Assert.assertEquals("Use Limitation", legal.getUseLimitation());
	Assert.assertEquals("Use Limitation", legal.getUseLimitations().next());
	Assert.assertEquals("patent", legal.getAccessConstraintCode());
	Assert.assertEquals("patent", legal.getAccessConstraintCodes().next());
	Assert.assertEquals("trademark", legal.getUseConstraintsCode());
	Assert.assertEquals("trademark", legal.getUseConstraintsCodes().next());
	Assert.assertEquals("Other Constraints", legal.getOtherConstraint());
	Assert.assertEquals("Other Constraints", legal.getOtherConstraints().next());

    }

    @Override
    public void clearProperties(LegalConstraints legal) {
	legal.clearAccessConstraints();
	legal.clearOtherConstraints();
	legal.clearUseConstraints();
	legal.clearUseLimitation();

    }

    @Override
    public void checkNullProperties(LegalConstraints legal) {
	Assert.assertNull(legal.getAccessConstraintCode());
	Assert.assertNull(legal.getOtherConstraint());
	Assert.assertNull(legal.getUseConstraintsCode());
	Assert.assertNull(legal.getUseLimitation());
	Assert.assertFalse(legal.getUseLimitations().hasNext());
	Assert.assertFalse(legal.getOtherConstraints().hasNext());
	Assert.assertFalse(legal.getAccessConstraintCodes().hasNext());
	Assert.assertFalse(legal.getUseConstraintsCodes().hasNext());

    }
}
