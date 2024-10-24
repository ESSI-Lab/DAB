package eu.essi_lab.model.auth;

import java.time.LocalDateTime;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

import eu.essi_lab.model.GSProperty;

public class GSAttributeTest extends GSAuthorizationEntityTest {

    @Test
    public void attributeCanComputeItsOwnHash() {
	GSProperty<String> attribute = new GSProperty<>();
	String name = "name";
	String value = "value";
	attribute.setName(name);
	attribute.setValue(value);
	int expected = Objects.hash(name, value);
	int actual = attribute.hashCode();
	Assert.assertEquals(expected, actual);
    }

    @Test
    public void attributesDifferBecauseOneIsNull() {
	GSProperty<String> expected = new GSProperty<>();
	GSProperty<String> actual = null;
	Assert.assertNotEquals(expected, actual);
    }

    @Test
    public void attributeIsComparedAgainstNotAnAttribute() {
	GSProperty<String> expected = new GSProperty<>();
	JsonNode actual = new TextNode("IAmNotAnAttribute");
	Assert.assertNotEquals(expected, actual);
    }

    @Test
    public void attributesDifferBecauseNameIsNull1() {
	GSProperty<Integer> expected = new GSProperty<>();
	expected.setName(null);
	expected.setValue(5);
	GSProperty<Integer> actual = new GSProperty<>();
	actual.setName("actual");
	actual.setValue(5);
	expected.equals(actual);
	Assert.assertNotEquals(expected, actual);
    }

    @Test
    public void attributesDifferBecauseNameIsNull2() {
	GSProperty<Integer> expected = new GSProperty<>();
	expected.setName("expected");
	expected.setValue(5);
	GSProperty<Integer> actual = new GSProperty<>();
	actual.setName(null);
	actual.setValue(5);
	Assert.assertNotEquals(expected, actual);
    }

    @Test
    public void attributesDifferByName() {
	GSProperty<Integer> expected = new GSProperty<>();
	expected.setName("expected");
	expected.setValue(5);
	GSProperty<Integer> actual = new GSProperty<>();
	actual.setName("actual");
	actual.setValue(5);
	Assert.assertNotEquals(expected, actual);
    }

    @Test
    public void attributesDifferByValueType() {
	GSProperty<String> expected = new GSProperty<>();
	expected.setName("attribute");
	expected.setValue("five");
	GSProperty<Integer> actual = new GSProperty<>();
	actual.setName("attribute");
	actual.setValue(5);
	Assert.assertNotEquals(expected, actual);
    }

    @Test
    public void attributesDifferBecauseValueIsNull() {
	GSProperty<Integer> expected = new GSProperty<>();
	expected.setName("attribute");
	expected.setValue(null);
	GSProperty<Integer> actual = new GSProperty<>();
	actual.setName("attribute");
	actual.setValue(15);
	Assert.assertNotEquals(expected, actual);
    }

    @Test
    public void attributesDifferByValue() {
	GSProperty<Integer> expected = new GSProperty<>();
	expected.setName("attribute");
	expected.setValue(5);
	GSProperty<Integer> actual = new GSProperty<>();
	actual.setName("attribute");
	actual.setValue(15);
	Assert.assertNotEquals(expected, actual);
    }

    @Test
    public void attributesAreTheSameObject() {
	int year = 2000;
	int month = 1;
	int dayOfMonth = 1;
	int hour = 12;
	int minute = 0;
	GSProperty<LocalDateTime> expected = new GSProperty<>();
	expected.setName("attribute");
	expected.setValue(LocalDateTime.of(year, month, dayOfMonth, hour, minute));
	GSProperty<LocalDateTime> actual = expected;
	Assert.assertEquals(expected, actual);
    }

    @Test
    public void attributesAreIdenticalBecauseValuesAreNull() {
	GSProperty<LocalDateTime> expected = new GSProperty<>();
	expected.setName("attribute");
	expected.setValue(null);
	GSProperty<LocalDateTime> actual = new GSProperty<>();
	actual.setName("attribute");
	actual.setValue(null);
	Assert.assertEquals(expected, actual);
    }

    @Test
    public void attributesAreIdentical1() {
	LocalDateTime timeNow = LocalDateTime.now();
	GSProperty<LocalDateTime> expected = new GSProperty<>("attribute", timeNow);
	GSProperty<LocalDateTime> actual = new GSProperty<>("attribute", timeNow);
	Assert.assertEquals(expected, actual);
    }

    @Test
    public void attributesAreIdentical2() {
	int year = 2000;
	int month = 1;
	int dayOfMonth = 1;
	int hour = 12;
	int minute = 0;
	GSProperty<LocalDateTime> expected = new GSProperty<>();
	expected.setName("attribute");
	expected.setValue(LocalDateTime.of(year, month, dayOfMonth, hour, minute));
	GSProperty<LocalDateTime> actual = new GSProperty<>();
	actual.setName("attribute");
	actual.setValue(LocalDateTime.of(year, month, dayOfMonth, hour, minute));
	Assert.assertEquals(expected, actual);
    }
}
