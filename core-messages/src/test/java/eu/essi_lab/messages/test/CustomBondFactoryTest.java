package eu.essi_lab.messages.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.CustomBondFactory;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.resource.MetadataElement;

public class CustomBondFactoryTest {

    @Test
    public void test1() {

	try {
	    CustomBondFactory.createCustomBond(new Queryable() {

		@Override
		public boolean isVolatile() {
		    return false;
		}

		@Override
		public String getName() {
		    return MetadataElement.TITLE.getName();
		}

		@Override
		public boolean isEnabled() {

		    return true;
		}

		@Override
		public ContentType getContentType() {
		    return null;
		}
		
		@Override
		    public void setEnabled(boolean enabled) {
 		    }
	    }, BondOperator.EQUAL, "");

	} catch (IllegalArgumentException ex) {

	    // OK invalid name
	    return;
	}

	fail("Exception not thrown");
    }

    @Test
    public void test2() {

	try {
	    CustomBondFactory.createCustomBond(new Queryable() {

		@Override
		public boolean isVolatile() {
		    return false;
		}

		@Override
		public String getName() {
		    return "pippo";
		}

		@Override
		public ContentType getContentType() {
		    return null;
		}

		@Override
		public boolean isEnabled() {

		    return true;
		}
		@Override
		    public void setEnabled(boolean enabled) {
		    }
	    }, BondOperator.EQUAL, "");

	} catch (IllegalArgumentException ex) {

	    // OK missing content type
	    return;
	}

	fail("Exception not thrown");
    }

    @Test
    public void test3() {

	try {
	    CustomBondFactory.createCustomBond(new Queryable() {

		@Override
		public boolean isVolatile() {
		    return false;
		}

		@Override
		public String getName() {
		    return "pippo";
		}

		@Override
		public ContentType getContentType() {
		    return ContentType.BOOLEAN;
		}

		@Override
		public boolean isEnabled() {

		    return true;
		}
		@Override
		    public void setEnabled(boolean enabled) {
		    }
	    }, BondOperator.EQUAL, "");

	} catch (IllegalArgumentException ex) {

	    // OK unsupported content type boolean
	    return;
	}

	fail("Exception not thrown");

    }

    @Test
    public void test4() {

	try {
	    CustomBondFactory.createCustomBond(new Queryable() {

		@Override
		public boolean isVolatile() {
		    return false;
		}

		@Override
		public String getName() {
		    return "pippo";
		}

		@Override
		public ContentType getContentType() {
		    return ContentType.TEXTUAL;
		}

		@Override
		public boolean isEnabled() {

		    return true;
		}
		
		@Override
		    public void setEnabled(boolean enabled) {
		    }
	    }, BondOperator.EQUAL, "");

	} catch (IllegalArgumentException ex) {

	    fail("Exception thrown");
	}
    }

    @Test
    public void test5() {

	try {
	    CustomBondFactory.createCustomBond(new Queryable() {

		@Override
		public boolean isVolatile() {
		    return false;
		}

		@Override
		public String getName() {
		    return "pippo1";
		}

		@Override
		public ContentType getContentType() {
		    return ContentType.TEXTUAL;
		}

		@Override
		public boolean isEnabled() {

		    return true;
		}
		
		@Override
		    public void setEnabled(boolean enabled) {
		    }
	    }, BondOperator.EQUAL, "");

	} catch (IllegalArgumentException ex) {

	    // OK unsupported name
	    return;
	}

	fail("Exception not thrown");
    }

    @Test
    public void test6() {

	try {
	    CustomBondFactory.createCustomBond(new Queryable() {

		@Override
		public boolean isVolatile() {
		    return false;
		}

		@Override
		public String getName() {
		    return "pippo_rt";
		}

		@Override
		public ContentType getContentType() {
		    return ContentType.TEXTUAL;
		}

		@Override
		public boolean isEnabled() {

		    return true;
		}
		
		@Override
		    public void setEnabled(boolean enabled) {
		    }
	    }, BondOperator.EQUAL, "");

	} catch (IllegalArgumentException ex) {

	    fail("Exception thrown");
	}
    }
}
