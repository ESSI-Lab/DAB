package eu.essi_lab.model.auth;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.model.GSProperty;

public class GSUserTest extends GSAuthorizationEntityTest {

    private Validator validator;

    @Before
    public void initTest() {
	validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    public void marshalTest() throws Exception {

	GSUser user = new GSUser();
	user.setAuthProvider("facebook");
	user.setIdentifier("user@whoswho.org");
	user.setRole("admin");

	GSProperty<String> attribute1 = new GSProperty<>("attr1", "val1");
	GSProperty<String> attribute2 = new GSProperty<>("attr2", "val2");

	user.setAttributes(Arrays.asList(attribute1, attribute2));

	System.out.println(user.asString(true));
    }

    @Test
    public void marshalUnmarshalTest() throws Exception {

	GSUser user = new GSUser();
	user.setAuthProvider("facebook");
	user.setIdentifier("user@whoswho.org");
	user.setRole("admin");

	GSProperty<String> attribute1 = new GSProperty<>("xxx", "yyy");
	GSProperty<String> attribute2 = new GSProperty<>("pippo", "ciccio");

	user.setAttributes(Arrays.asList(attribute1, attribute2));

	InputStream asStream = user.asStream();

	GSUser unmarhalledUser = GSUser.create(asStream);

	Assert.assertEquals(user, unmarhalledUser);
    }

    @Test
    public void userCanComputeHisHashcode() {
	GSUser user = new GSUser();
	user.setAuthProvider("facebook");
	user.setIdentifier("user@whoswho.org");
	int expected = Objects.hash(user.getAuthProvider(), user.getIdentifier(), user.getProperties());
	int actual = user.hashCode();
	Assert.assertEquals(expected, actual);
    }

    @Test
    public void userIsInvalidBecauseOfSomeSlashesInHisEmail() {
	GSUser user = new GSUser();
	user.setAuthProvider("facebook");
	user.setIdentifier("user@whos/who.org");
	Set<ConstraintViolation<GSUser>> errors = validator.validate(user);
	Assert.assertFalse(errors.isEmpty());
    }

    @Test
    public void userIsInvalidBecauseOfSomeBackslashesInHisEmail() {
	GSUser user = new GSUser();
	user.setAuthProvider("facebook");
	user.setIdentifier("user@whos\\who.org");
	Set<ConstraintViolation<GSUser>> errors = validator.validate(user);
	Assert.assertFalse(errors.isEmpty());
    }

    @Test
    public void userIsInvalidBecauseOfItsAuthorizationProvider() {
	GSUser user = new GSUser();
	user.setAuthProvider("fcebook");
	user.setIdentifier("user@whoswho.org");
	Set<ConstraintViolation<GSUser>> errors = validator.validate(user);
	Assert.assertFalse(errors.isEmpty());
    }

    @Test
    public void userIsValid() {
	GSUser user = new GSUser();
	user.setAuthProvider("facebook");
	user.setIdentifier("user@whoswho.org");
	Set<ConstraintViolation<GSUser>> errors = validator.validate(user);
	Assert.assertTrue(errors.isEmpty());
    }

    @Test
    public void usersAreDifferentBecauseOfTheirAuthProviders() {
	GSUser user1 = new GSUser();
	user1.setAuthProvider("facebook");
	user1.setIdentifier("user@whoswho.org");
	GSUser user2 = new GSUser();
	user2.setAuthProvider("fcbook");
	user2.setIdentifier("user@whoswho.org");
	Assert.assertFalse(user1.equals(user2));
    }

    @Test
    public void usersAreDifferentBecauseOfTheirEmail() {
	GSUser user1 = new GSUser();
	user1.setAuthProvider("facebook");
	user1.setIdentifier("user@whoswho.biz");
	GSUser user2 = new GSUser();
	user2.setAuthProvider("facebook");
	user2.setIdentifier("user@whoswho.org");
	Assert.assertFalse(user1.equals(user2));
    }

    @Test
    public void usersAreIdentical() {
	GSUser user1 = new GSUser();
	user1.setAuthProvider("facebook");
	user1.setIdentifier("user@whoswho.org");
	GSUser user2 = new GSUser();
	user2.setAuthProvider("facebook");
	user2.setIdentifier("user@whoswho.org");
	Assert.assertTrue(user1.equals(user2));
    }
}
